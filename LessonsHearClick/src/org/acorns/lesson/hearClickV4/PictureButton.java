/**
 * PictureButton.java
 * @author HarveyD
 * @version 4.00 Beta
 *
 * Copyright 2007-2015, all rights reserved
 */

package org.acorns.lesson.hearClickV4;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;
import org.acorns.data.*;
import org.acorns.visual.*;

public class PictureButton extends JButton
{
	private final static long serialVersionUID = 1;
    private final static int THICKNESS = 4;
    private final Dimension ANNOTATION_SIZE = new Dimension(300,300);

    
    private PictureData picture;
    private ColorScheme colors;

    public PictureButton(Dimension buttonSize, ColorScheme colors)
    {
       this.colors = colors;
       setSize(buttonSize);
       setPreferredSize(buttonSize);
       setMinimumSize(buttonSize);
       setName("");
    }
    
   /** Method to paint this component with the BufferedImage
    * 
    * @param graphics The graphics object for this component
    */
   public @Override void paintComponent(Graphics graphics)
   {  
      super.paintComponent(graphics);

      Color color = colors.getColor(true);
      graphics.setColor(color);
      graphics.fillRect(0, 0, getWidth(), getHeight());
     
      // If we find an object, it is a picture to display
      if (picture!=null)
      {
         // Now scale the picture to the available space
         picture.loadImages(true, ANNOTATION_SIZE);
         Dimension size = picture.getSize();
         if (size == null) return;

         double scaleX = 1.0 * getWidth() / size.width;
         double scaleY = 1.0 * getHeight() / size.height;
         double scale = scaleX;
         if (scaleX > scaleY) scale = scaleY;

         int newWidth = (int)(size.width * scale);
         int newHeight = (int)(size.height * scale);
         int x = (getWidth() - newWidth)/2;
         int y = (getHeight() - newHeight) /2;
         Rectangle pictureSize = new Rectangle(0, 0, newWidth, newHeight);
         BufferedImage pictureImage = picture.getImage(this, pictureSize);
         graphics.drawImage(pictureImage, x, y, newWidth, newHeight, null); 

      }   // End if picture within bounds 
   }      // End of paintComponent method
   
   /** Method to create the correct border for a button
   * 
   * @param button The button to which to attach the border
   * @param select true if button should be selected
   */
   public void createBorder(boolean select)
   {
       Border outer, inner, compound;
       if (select)
       {   outer = BorderFactory.createLineBorder(Color.RED, THICKNESS);
           inner = BorderFactory.createLoweredBevelBorder();
           compound = BorderFactory.createCompoundBorder(outer, inner);
       }
       else
       {   outer = BorderFactory.createLineBorder(new Color(80,80,80), THICKNESS);
           inner = BorderFactory.createRaisedBevelBorder();
           compound = BorderFactory.createCompoundBorder(outer, inner);
       }
       setBorder(compound);
       
   }    // End of createBorder() method    
   
   /** Method to set a picture into this component
    * 
    * @param picture The PictureData object
    * @param name The annotation offset for this button
    */
   public void setImage(PictureData picture, String name)  
   { 
       this.picture = picture;  
       if (picture==null) 
       {
           setVisible(false);
           setName("");
       }
       else               
       {
           setName(name);
           setVisible(true);
       }
       repaint();
   }
}       // End of PictureButton class
