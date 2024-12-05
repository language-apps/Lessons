/*
 * PicturesPlayLabel.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.lesson.picturesV4;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import org.acorns.lesson.*;
import org.acorns.data.*;
import org.acorns.language.*;
import org.acorns.widgets.*;
import org.acorns.visual.*;

// Nested class that will draw icons that go over the top.
public class PicturesPlayLabel extends PanPanel
            implements MouseListener, ActionListener
{
   public final static long serialVersionUID = 1;
  
   public  final static int   ICON   = 20;

   private  LessonsPictures    lesson;
   private  PicturesPanel      panel;
   private  LessonPlayControls lessonControls;
   private  JMenuItem[]        items;
   private  LessonPopupMenu    popup;
   private  int                selection;
   private  String[]           scaleOptions;
   private  Color			   fg, bg;
   
   // Start of constructor.            
   public PicturesPlayLabel
           (PicturesPanel panel, LessonsPictures lesson)
   {
      super(lesson.getPictureData(0), Color.GRAY);

      this.panel  = panel;
      this.lesson = lesson;
      this.items  = new JMenuItem[2];

      ColorScheme colors = lesson.getColorScheme();
      fg = colors.getColor(false);
      bg = colors.getColor(true);
      
      Dimension  size = lesson.getFrameSize();
      Dimension imageSize = new Dimension(size.width-50, size.height-80);

      setPreferredSize(imageSize);
      setMinimumSize(imageSize);
      setSize(imageSize);

      scaleOptions = LanguageText.getMessageList("commonHelpSets", 29);
      addMouseListener(this);
      
      lessonControls = getPictureControls();
      selection = 0;
      //addComponentListener(this);
   }
   
   /** Method so parent component can get the control button panel */
   public JPanel getPlayControls()  { return lessonControls; }

   /** Method to paint additional information onto the panel
    *
    * @param page The object onto which to draw
    */
   public void paintMore(Graphics page)
   {  
      // Draw all of the icons.
      Point             key, point, rotatePoint;
      Hashtable<Point,PicturesSoundData> data = lesson.getSoundData();
      Enumeration<Point>       pointList = data.keys();
      PicturesSoundData sounds    = null;
      int points = lesson.getPoints();
        
      while (pointList.hasMoreElements())
      {  key    = (Point)pointList.nextElement();
         if (key.x>0 && key.y<0) continue;

         point  = new Point(key.x/PicturesPanel.KEY_LAYERS, key.y);
         sounds = data.get(key);

         if (sounds==null) continue;
         if (point.x<0||point.y<0||point.x>=points||point.y>=points)
         { data.remove(key); }
         else  
         {
            if (key.x % PicturesPanel.KEY_LAYERS==lesson.getLayer()) 
            {   rotatePoint = panel.iconLocation(point, lesson.getAngle());
                panel.paintIcon(page, rotatePoint, sounds.isSound() );
            }
         }
      }  // End while more points.
   }
 
   //------------------------------------------------------------------------------
   // Method to use to insert, modify, or change an icon.
   //------------------------------------------------------------------------------
   public void mouseClicked(MouseEvent event)
   {
      Dimension size = getSize();
         
      Point point = event.getPoint();
      if (point.x<0 || point.y<0 ||
                           point.x>=size.width || point.y>=size.height)
         Toolkit.getDefaultToolkit().beep();
          
      point = panel.convertLocationToKey(point);
      Point key = panel.intersectingAcorn(point);
      if (key!=null)
      {
         Hashtable<Point,PicturesSoundData> data = lesson.getSoundData();
         PicturesSoundData pointData = data.get(key);
         
         // Delete illegal points without data.
         if (pointData == null)
         { data.remove(key); repaint(); return;}
         
         if (pointData.isSound())
              playPictures( pointData.getVector(), event.getPoint());
         else 
         {
            lesson.setActiveLesson(pointData.getText());
         }
      }
   }  // End of mouse clicked.
   
   //------------------------------------------------------------
   // Unused MouseListener methods.
   //------------------------------------------------------------
   public void mouseEntered(MouseEvent event)   {}
   public void mouseExited(MouseEvent event)    {}
   public void mousePressed(MouseEvent event)   {}
   public void mouseReleased(MouseEvent event)  {}
   
      
   //------------------------------------------------------------
   // Method to execute the play option.
   //------------------------------------------------------------

   public void playPictures(Vector<SoundData> soundVector, Point point)
   {
      // Pick a random sound.        
      int size = soundVector.size();
      if (++selection <0) selection = 0;
      
      SoundData soundData = soundVector.elementAt(selection % size);

      if (soundData!=null) soundData.playBack(null, 0, -1);
      String title = lesson.getTitle();
      Frame frame = (Frame) SwingUtilities.getAncestorOfClass
                                    (Frame.class, this);
      new WindowDialog(lesson, frame, title, soundData, point, fg, bg);
   }  // End of playPictures.
 
    /** Create the control panel at the bottom of the play screen
    *  @return LessonPictureControls pop up menu
    */
   public LessonPlayControls getPictureControls()
   {
      // Create menu items for changing the difficulty level
      items[0] = new JMenuItem(adjustScaleFactorTip(true));
      items[0].addActionListener(this);
      
      items[1] = new JMenuItem(adjustScaleFactorTip(false));
      items[1].addActionListener(this);
         
      // Create Popup menu
      popup = new LessonPopupMenu(lesson, this, items);
			    
      LessonPlayControls controls 
              = new LessonPlayControls(lesson, popup, "PicturesPlay");
         
      return controls;
     
   }    // End of getPictureControls()

   // Get the scale factor adjustment text for either tool tips or a menu item
   private String adjustScaleFactorTip(boolean flag)
   {
       int scaleFactor = lesson.getScaleFactor();
       if (flag)
       {   if (scaleFactor >= PictureData.MAX_SCALE)
                return scaleOptions[0];
           else return scaleOptions[2] + " " + scaleFactor;
       }
       else
       {    if (scaleFactor <= PictureData.MIN_SCALE)
                return scaleOptions[1];
            else return scaleOptions[3] + " " + scaleFactor;
       }
   }        // End of AdjustScaleFactorTip()
   
   /** Listener to respond to buttons and popup menu options
     *  @param event object triggering this event
     */
    public void actionPerformed(ActionEvent event)
    {
         JMenuItem source = (JMenuItem)event.getSource(); 
         if (!popup.isArmed()) {  return; }
         popup.cancel();      //Disable the Popup menu.         

         // Check if resizing picture
         int scaleFactor = lesson.getScaleFactor();
         if (source == items[0])   
         {   if (scaleFactor + PicturesPanel.FACTOR_INCREMENT 
                                           <= PictureData.MAX_SCALE)
             {  
                 scaleFactor += PicturesPanel.FACTOR_INCREMENT;
                 lesson.setScaleFactor(scaleFactor);
                 source.setText(adjustScaleFactorTip(true));
                 items[1].setText(adjustScaleFactorTip(false));
                 lesson.setDirty(true);
                 resetPicture();
                 panel.repaint();
                 return;
             }
         }
				      
         if (source == items[1])   
         {   if (scaleFactor - PicturesPanel.FACTOR_INCREMENT 
                                           >= PictureData.MIN_SCALE)
             {   scaleFactor -= PicturesPanel.FACTOR_INCREMENT;
                 lesson.setScaleFactor(scaleFactor);
                 items[0].setText(adjustScaleFactorTip(true));
                 source.setText(adjustScaleFactorTip(false));
                 lesson.setDirty(true);
                 resetPicture();
                 panel.repaint();
                 return;
             }
         }
         Toolkit.getDefaultToolkit().beep();
       
    }        // End of actionPerformed()

}        // End of PicturesPlayLabel class.