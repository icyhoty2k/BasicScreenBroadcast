/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.davidbuzatto.basicscreenbroadcast.gui;

import br.com.davidbuzatto.basicscreenbroadcast.gui.model.BroadcastArea;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

/**
 *
 * @author David
 */
public class ImageWindow extends javax.swing.JFrame {
    
    private BroadcastArea broadcastArea;
    private int xDiff;
    private int yDiff;
    
    /**
     * Creates new form ImageWindow
     */
    public ImageWindow() {
        initComponents();
        setUndecorated( true );
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        imagePanel = new br.com.davidbuzatto.basicscreenbroadcast.gui.ImagePanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.MOVE_CURSOR));
        setIconImage(new ImageIcon( getClass().getResource( 
            "/br/com/davidbuzatto/basicscreenbroadcast/gui/icons/application_view_tile.png" ) ).getImage());
setResizable(false);
addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
    public void mouseDragged(java.awt.event.MouseEvent evt) {
        formMouseDragged(evt);
    }
    });
    addMouseListener(new java.awt.event.MouseAdapter() {
        public void mousePressed(java.awt.event.MouseEvent evt) {
            formMousePressed(evt);
        }
    });

    javax.swing.GroupLayout imagePanelLayout = new javax.swing.GroupLayout(imagePanel);
    imagePanel.setLayout(imagePanelLayout);
    imagePanelLayout.setHorizontalGroup(
        imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 500, Short.MAX_VALUE)
    );
    imagePanelLayout.setVerticalGroup(
        imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 100, Short.MAX_VALUE)
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(imagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(imagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );

    setBounds(0, 0, 516, 139);
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
        
        if ( SwingUtilities.isLeftMouseButton( evt ) ) {
            setLocation( evt.getXOnScreen() - xDiff, evt.getYOnScreen() - yDiff );
        }
        
    }//GEN-LAST:event_formMouseDragged

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        
        xDiff = evt.getX();
        yDiff = evt.getY();
        
        if ( SwingUtilities.isRightMouseButton( evt ) ) {
            setExtendedState( ICONIFIED );
        }
        
    }//GEN-LAST:event_formMousePressed

    public void setBroadcastArea( BroadcastArea broadcastArea ) {
        this.broadcastArea = broadcastArea;
        imagePanel.setBroadcastArea( broadcastArea );
    }
    
    public void setData( BufferedImage image, Point cursorPosition ) {
        
        setSize( broadcastArea.getRectangle().width, broadcastArea.getRectangle().height );
        setTitle( broadcastArea.getName() );
        
        Point newPoint = new Point();
        newPoint.x = cursorPosition.x - broadcastArea.getRectangle().x;
        newPoint.y = cursorPosition.y - broadcastArea.getRectangle().y;
        
        imagePanel.setImage( image );
        imagePanel.setCursorPosition( newPoint );
        
        imagePanel.repaint();
        
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private br.com.davidbuzatto.basicscreenbroadcast.gui.ImagePanel imagePanel;
    // End of variables declaration//GEN-END:variables
}
