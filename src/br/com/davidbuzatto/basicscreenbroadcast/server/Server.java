/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.davidbuzatto.basicscreenbroadcast.server;

import br.com.davidbuzatto.basicscreenbroadcast.gui.MainWindow;
import br.com.davidbuzatto.basicscreenbroadcast.gui.model.BroadcastArea;
import br.com.davidbuzatto.basicscreenbroadcast.gui.model.BroadcastData;
import br.com.davidbuzatto.basicscreenbroadcast.utils.Constants;
import br.com.davidbuzatto.basicscreenbroadcast.utils.Utils;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.net.ServerSocketFactory;

/**
 *
 * @author David
 */
public class Server {

    private static Robot robot;
    private int port;
    private int fps;
    
    private ServerSocket serverSocket;
    private Socket newClientSocket;
    private List<Socket> clientSockets;
    private List<ObjectOutputStream> clientOOS;
    
    private List<BroadcastArea> broadcastAreas;
    
    private ServerConnectionThread serverConnectionThread;
    private ServerDataThread serverDataThread;
    private ExecutorService executorService;
    
    private MainWindow mainWindow;

    public Server( int port, int fps, MainWindow mainWindow, List<BroadcastArea> broadcastAreas ) throws AWTException, IOException {

        if ( robot == null ) {
            try {
                robot = new Robot();
            } catch ( AWTException exc ) {
                throw new AWTException( "Can't create Robot!" );
            }
        }
        
        this.port = port;
        this.fps = fps;
        this.mainWindow = mainWindow;
        
        this.clientSockets = Collections.synchronizedList( new ArrayList<Socket>() );
        this.clientOOS = Collections.synchronizedList( new ArrayList<ObjectOutputStream>() );
        this.broadcastAreas = Collections.synchronizedList( new ArrayList<BroadcastArea>() );
        setBroadcastAreas( broadcastAreas );

        this.serverSocket = ServerSocketFactory.getDefault().createServerSocket( port );
        
        this.serverConnectionThread = new ServerConnectionThread();
        this.serverDataThread = new ServerDataThread();
        
        this.executorService = new ThreadPoolExecutor( 10, 10, 1, 
                TimeUnit.SECONDS, 
                new ArrayBlockingQueue<Runnable>( 10 ) );

    }

    public void start() {
        executorService.execute( this.serverConnectionThread );
        executorService.execute( this.serverDataThread );
    }
    
    public void stop() throws IOException {
        serverSocket.close();
        serverConnectionThread.stop();
        serverDataThread.stop();
        executorService.shutdown();
    }
    
    private void stopServer() {
        try {
            stop();
        } catch ( IOException exc ) {
            Utils.insertFormattedTextJTextPane( mainWindow.getTxtPaneOutputAndError(), 
                    "--- I/O Exception - Stop server didn't work as intended! ---\n", Color.RED );
            Utils.insertFormattedExceptionTextJTextPane( mainWindow.getTxtPaneOutputAndError(), exc, Color.RED );
        }
    }
    
    public void setBroadcastAreas( List<BroadcastArea> broadcastAreas ) {
        
        for ( BroadcastArea ba : broadcastAreas ) {
            this.broadcastAreas.add( ba );
        }
        
    }
    
    private class ServerConnectionThread implements Runnable {

        private boolean running = true;
        
        @Override
        public void run() {
            
            Utils.insertFormattedTextJTextPane( mainWindow.getTxtPaneOutputAndError(), 
                    "Server Connection Thread is Running!\n", 
                    Constants.OK_OUTPUT_MESSAGE_COLOR );
            
            while ( running ) {
                try {
                    Utils.insertFormattedTextJTextPane( mainWindow.getTxtPaneOutputAndError(), 
                            "Server Connection Thread is waiting for a new connection...\n", 
                            Constants.OK_OUTPUT_MESSAGE_COLOR );
                    newClientSocket = serverSocket.accept();
                } catch ( IOException exc ) {
                    
                    Utils.insertFormattedTextJTextPane( mainWindow.getTxtPaneOutputAndError(), 
                            "--- I/O Exception - Can't accept a new connection! ---\n", Color.RED );
                    Utils.insertFormattedExceptionTextJTextPane( mainWindow.getTxtPaneOutputAndError(), exc, Color.RED );
                    stopServer();
                    
                }
            }
            
        }
        
        public void stop() {
            running = false;
        }

    }

    private class ServerDataThread implements Runnable {

        private boolean running = true;
        private boolean removeClient;
        
        @Override
        public void run() {
            
            Utils.insertFormattedTextJTextPane( mainWindow.getTxtPaneOutputAndError(), 
                    "Server Data Thread is Running!\n", 
                    Constants.OK_OUTPUT_MESSAGE_COLOR );
            
            while ( running ) {

                List<BufferedImage> imagesToSend = new ArrayList<>();
                
                for ( BroadcastArea ba : broadcastAreas ) {
                    imagesToSend.add( robot.createScreenCapture( ba.getRectangle() ) );
                }
                
                int clientIndex = 0;
                removeClient = false;
                
                synchronized ( clientSockets ) {
                    
                    for ( Socket cs : clientSockets ) {

                        /*Utils.insertFormattedTextJTextPane( mainWindow.getTxtPaneOutputAndError(), 
                                "Sending data to client: \" + cs.toString()", 
                                Constants.OK_OUTPUT_MESSAGE_COLOR );*/

                        try {

                            ObjectOutputStream oos = clientOOS.get( clientIndex );

                            BroadcastData d = new BroadcastData( 
                                    "dataTransfer",
                                    "",
                                    broadcastAreas, 
                                    Utils.bufferedImageListToByteArrayList( imagesToSend ), 
                                    MouseInfo.getPointerInfo().getLocation() );

                            oos.writeObject( d );
                            oos.flush();
                            clientIndex++;

                        } catch ( IOException exc ) {

                            Utils.insertFormattedTextJTextPane( mainWindow.getTxtPaneOutputAndError(), 
                                    "--- I/O Exception - Can't send data to client! ---\n", Color.RED );
                            Utils.insertFormattedExceptionTextJTextPane( mainWindow.getTxtPaneOutputAndError(), exc, Color.RED );

                            Utils.insertFormattedTextJTextPane( mainWindow.getTxtPaneOutputAndError(), 
                                    "--- Client will be disconnected! ---\n", Color.RED );

                            removeClient = true;

                        }

                    }

                    if ( removeClient ) {
                        clientSockets.remove( clientIndex );
                        clientOOS.remove( clientIndex );
                    }

                    try {
                        if ( newClientSocket != null && !clientSockets.contains( newClientSocket ) ) {
                            clientSockets.add( newClientSocket );
                            clientOOS.add( new ObjectOutputStream( newClientSocket.getOutputStream() ) );
                            newClientSocket = null;
                        }
                    } catch ( IOException exc ) {

                        Utils.insertFormattedTextJTextPane( mainWindow.getTxtPaneOutputAndError(), 
                                "--- InterruptedException - Can't get client streams! ---\n", Color.RED );
                        Utils.insertFormattedExceptionTextJTextPane( mainWindow.getTxtPaneOutputAndError(), exc, Color.RED );
                        //stopServer();

                    }
                
                }
                
                try {
                    Thread.sleep( fpsToMilis( fps ) );
                } catch ( InterruptedException exc ) {
                    
                    Utils.insertFormattedTextJTextPane( mainWindow.getTxtPaneOutputAndError(), 
                            "--- InterruptedException - Can't sleep! ---\n", Color.RED );
                    Utils.insertFormattedExceptionTextJTextPane( mainWindow.getTxtPaneOutputAndError(), exc, Color.RED );
                    stopServer();
                    
                }

            }

        }

        private long fpsToMilis( int fps ) {
            return 1000L / fps;
        }
        
        public void stop() throws IOException {
            
            running = false;
            
            synchronized ( clientSockets ) {
                
                int clientIndex = 0;
                for ( Socket cs : clientSockets ) {
                    clientOOS.get( clientIndex ).close();
                    cs.close();
                    clientIndex++;
                }

                clientSockets.clear();
                clientOOS.clear();
                
            }
            
        }

    }

}
