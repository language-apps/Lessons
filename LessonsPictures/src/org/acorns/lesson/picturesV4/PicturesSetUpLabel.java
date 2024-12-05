/*
 * PicturesSetUpLabel.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */

package org.acorns.lesson.picturesV4;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.*;
import org.acorns.data.*;
import org.acorns.language.*;

// Class that will process the setup of a lesson.
public class PicturesSetUpLabel extends JPanel 
            implements MouseListener, MouseMotionListener, Scrollable
{
   public final static long serialVersionUID = 1;
   
   public final static int  GLOSS_SIZE = 30, IPA_SIZE = 30, NAME_SIZE = 30;

	
   private boolean       drag;
   private Point         startPosition, endPosition;
      
   private PictureData     pictureData;
   private BufferedImage   image;
   private PicturesPanel   panel;
   private LessonsPictures lesson;
	  
   public PicturesSetUpLabel(PicturesPanel panel, LessonsPictures lesson)   
   {  
      // Configure the icon used for sound locations.
      this.panel   = panel;
      this.lesson  = lesson;
      pictureData = lesson.getPictureData(0);
      image = pictureData.getImage(this, new Rectangle(0,0,-1,-1));

      addMouseListener(this);
      addMouseMotionListener(this);
      setBackground(new Color(200,200,200));

      Dimension size = pictureData.getSize();
      
      setPreferredSize(size);
      setSize(size);
      setMaximumSize(size);
  }   
   
   // Method to paint this component on a drag operation.
   public @Override void paintComponent(Graphics page)
   {
      super.paintComponent(page);
      Graphics2D graphics = (Graphics2D)page;
      Dimension panelSize = getSize();
      graphics.setColor(new Color(80,80,80));
      graphics.fillRect(0,0, panelSize.width, panelSize.height);
      
      BufferedImage newImage = image;
      if (pictureData.getNumberFrames()!= 1)
      {
         newImage = pictureData.getImage(this, new Rectangle(0,0,-1,-1));
      }
      else newImage = image;

      int imageWidth  = newImage.getWidth();
      int imageHeight = newImage.getHeight();
      System.gc();
      graphics.drawImage(newImage, 0, 0, imageWidth, imageHeight, this);

      // Draw all of the icons.
      Point             key, point;
      Hashtable<Point,PicturesSoundData> data = lesson.getSoundData();

      Enumeration<Point>       pointList = data.keys();
      PicturesSoundData sounds    = null;
      int               layer     = lesson.getLayer();
         
      while (pointList.hasMoreElements())
      {  
         key    = (Point)pointList.nextElement();
         if (key.x==1 && key.y<0) continue;
         point  = new Point(key.x/PicturesPanel.KEY_LAYERS, key.y);
         sounds = data.get(key);
            
         if (sounds==null)
         { data.remove(key); }
         else  
         {
            if (key.x % PicturesPanel.KEY_LAYERS==lesson.getLayer())
            {
                panel.paintIcon
                        (page, panel.iconLocation(point, lesson.getAngle())
                                                       , sounds.isSound() );
            }
         }
            
      }  // End while more points.
         
      // Handle the drag and drop.
      if (drag)
      {
         point = endPosition;
         key   = new Point(startPosition.x*PicturesPanel.KEY_LAYERS
			                         + layer, startPosition.y);
         sounds = data.get(key);
         if (sounds != null)  
             panel.paintIcon(page, panel.iconLocation(point, lesson.getAngle())
                                                           , sounds.isSound());
      }
   }

   //------------------------------------------------------------------------------
   // Method to use to insert, modify, or change an icon.
   //------------------------------------------------------------------------------
   public void mouseClicked(MouseEvent event)
   {
      Dimension size = getSize();
      PicturesSoundData sounds;
    
      Point location = event.getPoint();
      if (location.x<0 || location.y<0 ||
             location.x>=size.width || location.y>=size.height)
	   	 {		 
         Toolkit.getDefaultToolkit().beep();
         return;
      }         
		
      int layer = lesson.getLayer();
      Point key;

      location = panel.convertLocationToKey(location);
      if ((key = panel.intersectingAcorn(location)) !=null)
      {
         Hashtable<Point,PicturesSoundData> data = lesson.getSoundData();
    	 sounds = data.get(key);
         if (sounds.isSound())
              picturesDialog(location, layer, lesson);
         else linksDialog   (location, layer);
      }
      else if (panel.isAcornSelected())          
                picturesDialog(location, layer, lesson);
           else linksDialog   (location, layer);
      repaint();
		
   }  // End of mouse clicked.

   //------------------------------------------------------------
   // Methods to determine where to drag and drop a lesson.
   //------------------------------------------------------------
   public void mousePressed(MouseEvent event)   
   {
      startPosition = panel.convertLocationToKey(event.getPoint());
   }
      
   //------------------------------------------------------------
   // Method to reinsert key at new position after dragging.
   //------------------------------------------------------------
   public void mouseReleased(MouseEvent event)  
   {
      // Determine if release point is visible
      boolean visible = true;

      Rectangle rect = getVisibleRect();
      Point spot = event.getPoint();
      if (spot.x<rect.x) visible = false;
      if (spot.y<rect.y) visible = false;
      if (spot.y>rect.y + rect.height) visible = false;
      if (spot.x>rect.x + rect.width) visible = false;

      int points     = lesson.getPoints();
      endPosition    = panel.convertLocationToKey(event.getPoint());
      if (drag)
      {
         int layer = lesson.getLayer();
         Point keyEnd   = panel.intersectingAcorn(endPosition);
         Point keyStart = panel.intersectingAcorn(startPosition);
         if ((keyEnd==null) && (keyStart!=null))
         {
             // Remove icon and reinsert at new position.
             Hashtable<Point,PicturesSoundData> data = lesson.getSoundData();
 		     Hashtable<Point, PicturesSoundData> oldData = lesson.copyData(data);
             PicturesSoundData sound = data.get(keyStart);
             data.remove(keyStart);
             if (visible && endPosition.x>=0 && endPosition.x<points &&
                     endPosition.y>=0 && endPosition.y<points)
			 {
                keyEnd = new Point
                    (endPosition.x * PicturesPanel.KEY_LAYERS + layer
                        , endPosition.y);
                data.put(keyEnd, sound);
			 }
			 lesson.pushUndo(new LessonPicturesData(oldData));
             startPosition = new Point(-1, -1);
			 lesson.setDirty(true);
         }
         else Toolkit.getDefaultToolkit().beep();
      }
      drag    = false;
      repaint();
   }
   
   //------------------------------------------------------------
   // Unused MouseListener methods.
   //------------------------------------------------------------
   public void mouseEntered(MouseEvent event)   {}
   public void mouseExited(MouseEvent event)    {}
   
   //------------------------------------------------------------
   // Mouse Motion Listener methods to track the drag operation.
   //------------------------------------------------------------
   public void mouseDragged(MouseEvent event)   
   {
      Point keyStart = panel.intersectingAcorn(startPosition);
      if (keyStart!=null)
      {
         endPosition = panel.convertLocationToKey(event.getPoint());
         drag = true;
         repaint();
      }
   }
   
   //------------------------------------------------------------
   // Unused Mouse Motion Listener method.
   //------------------------------------------------------------
   public void mouseMoved(MouseEvent event)     {}
	
   
   //------------------------------------------------------------
   // Method to handle dialogs for creating sound data.
   //------------------------------------------------------------
   public void picturesDialog(Point point, int layer, LessonsPictures lesson)
   {
      Point key = panel.intersectingAcorn(point);
      PicturesSoundData sounds  = new PicturesSoundData();
      Hashtable<Point,PicturesSoundData> data = lesson.getSoundData();

      // Initialize data for the dialog.
      if (key!=null)
      {
         sounds = data.get(key);
        
         // Error if this is not an acorn.                           
         if (!sounds.isSound())
         {
             Toolkit.getDefaultToolkit().beep();
             return;
         }
      }
      else  key = new Point(point.x*PicturesPanel.KEY_LAYERS+layer, point.y);

      // Save original data.
      Hashtable<Point, PicturesSoundData> oldData = lesson.copyData(data);

      Frame frame = (Frame)SwingUtilities.getAncestorOfClass(Frame.class, this);
      int result = sounds.pictureDialog
                              (lesson, frame, PicturesSoundData.DESCRIPTION);

      // We're done if the user cancels.                    
      if (result == JOptionPane.CANCEL_OPTION) return;


      // Insert an element.
      if (result == JOptionPane.YES_OPTION)
      {   // Process insert option.
         if (!data.contains(key))   {  data.put(key, sounds);   }
      }  // End of insert or modify option.
         
      // Process delete option.
      if (result == JOptionPane.NO_OPTION)
      {
         // See if any more elements are at this point, remove if no.
         Vector<SoundData> soundVector = sounds.getVector();
         if (soundVector.size() == 0)
         {   if (data.containsKey(key)) 
             { data.remove(key); }  
         }
         
      }  // End of delete option.
		
      // Mark that the file needs a save operation.
      lesson.setDirty(true);
			
       // Enable the redo commands.
      lesson.pushUndo(new LessonPicturesData(oldData));
      startPosition = new Point(-1, -1);
  	
   }  // End of picturesDialog method.
      
   //--------------------------------------------------------------     
   // Method to handle dialog to create links to other lessons.
   //--------------------------------------------------------------     
   public void linksDialog(Point point, int layer)
   {
      Point key = panel.intersectingAcorn(point);
      String linkText  = "";
      PicturesSoundData pointData = new PicturesSoundData(linkText);

      // Get current link pointer from file (if it exists).
      if (key!=null)
      {
         Hashtable<Point,PicturesSoundData> data = lesson.getSoundData();
         pointData = data.get(key);
         
         // Delete illegal points without data.
         if (pointData==null)
         { data.remove(key); return; }
         
         if (pointData.isSound())
         {
             Toolkit.getDefaultToolkit().beep();
             return;
         }            
         linkText  = pointData.getText();
      }
         
      // Create panel for dialog box.
      JPanel textPanel = new JPanel();
      BoxLayout box    = new BoxLayout(textPanel, BoxLayout.Y_AXIS);
      textPanel.setLayout(box);

      String[] linkControls = LanguageText.getMessageList(lesson, 2);
      textPanel.add(new JLabel(linkControls[0]));

      JTextField linkField = new JTextField(linkText, NAME_SIZE);
      linkField.setToolTipText(LanguageText.getMessage("commonHelpSets", 20));
      textPanel.add(linkField);
         
      // Display dialog and get user response.
      
      String[] options = LanguageText.getMessageList("commonHelpSets", 30);
      String title = linkControls[1] + " [" + point.x + "," + point.y + "] "
                        + linkControls[2] + " " + layer;
                        
     Frame frame = (Frame)SwingUtilities.getAncestorOfClass(Frame.class, this);
      int result  = JOptionPane.showOptionDialog(frame, textPanel, title
                      , JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE
                      , null, options, options[2]);
                     
      // We're done if the user cancels.                    
      if (result == JOptionPane.CANCEL_OPTION) return;
         
      // If accept, then put updated data back or create new point.
      Hashtable<Point,PicturesSoundData> data = lesson.getSoundData();
      Hashtable<Point, PicturesSoundData> oldData = lesson.copyData(data);
      if (result == JOptionPane.YES_OPTION)
      {
          linkText = linkField.getText();      
		      	 pointData.setText(linkText);
          if (key==null)
          {
             key = new Point(point.x * PicturesPanel.KEY_LAYERS 
                                                  + layer, point.y);
             data.put(key, pointData);
          }
      }   // End of insert or modify.
         
      // If reject, process delete option.
      if (result == JOptionPane.NO_OPTION)
      {
         if (key!=null && data.contains(key))
         {data.remove(key); }
      }  // End of delete option.
      
      // Mark that the file needs a save operation.
      lesson.setDirty(true);

		    // Enable the redo commands.
      lesson.pushUndo(new LessonPicturesData(oldData));
      startPosition = new Point(-1, -1);
		
   }  // End of linksDialog method.

   
   // Scrollable methods
   public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction)
   {  
       if (orientation==SwingConstants.HORIZONTAL)
            return image.getWidth() / 10;
       else return image.getHeight() / 10; 
   }
   public Dimension getPreferredScrollableViewportSize()
   {  int       width  = image.getWidth();
      int       height = image.getHeight();
      Dimension size   = new Dimension(width, height);
      return size;  
   }
   public boolean getScrollableTracksViewportHeight() { return false; }
   public boolean getScrollableTracksViewportWidth()  { return false; }
   public int getScrollableUnitIncrement
           (Rectangle r, int orientation, int direction)
   {  
       return 10; 
   }
   
}  // End of PicturesSetUpLabel.
