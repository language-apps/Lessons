/*
 * PicturesPanel.java
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
import org.acorns.language.*;
import org.acorns.data.*;

public class PicturesPanel extends JPanel 
{
   public final static long serialVersionUID = 1;
   
   public final static int  MINICON=15;
   public final static int  KEY_LAYERS = 64;
   public final static int  FACTOR_INCREMENT = 10;

   // Components for the control panel.
   private OptionButtons     optionButtons;

   // Data needed to process this panel.  
   public LessonsPictures    lesson;
   private JPanel            imagePanel;
    
   // Icon sizes
   private int iconSize = Lesson.ICON;
   
   // Icons for drawing.
   private ImageIcon       acorn, anchor;

   public PicturesPanel(int type, LessonsPictures lesson) 
           throws NoSuchElementException
   {
      this.lesson = lesson;
        
      setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
      setBackground(new Color(80,80,80));
                  
       switch (type)
       {
          case Lesson.SETUP:
             imagePanel = new PicturesSetUpLabel(this,lesson);
             setSize(imagePanel.getSize());
             setPreferredSize(getSize());
             setMinimumSize(getSize());
             add(imagePanel);
             break;
             
          case Lesson.PLAY:

             // Create play panel.
             PictureData picture = lesson.getPictureData(0);
             picture.stopDisplayLoop();  // Stop setup panel from animating

             PicturesPlayLabel playPanel = new PicturesPlayLabel(this, lesson);
             setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
             imagePanel = playPanel;

             add(Box.createVerticalGlue());
             add(imagePanel);
             add(Box.createVerticalGlue());
             JPanel playControls = playPanel.getPlayControls();
             add(playControls);
             break;
       }
   }

   /** Get the icon size for the current screen width.
    *  @param screenSize Dimension of the image panel
    *  @return icon size
    */
   private int getIconSize(Dimension screenSize)
   {   int newSize = screenSize.width / lesson.getPoints();
       if (screenSize.height < screenSize.width)
          iconSize = screenSize.height / lesson.getPoints();
       if (newSize<MINICON) iconSize = MINICON;

       if (anchor==null || newSize!=iconSize)
       {  // Configure the icon used for sound locations.
          acorn  = lesson.getIcon  (AcornsProperties.ACORN, iconSize);
          anchor = lesson.getIcon  (AcornsProperties.ANCHOR, iconSize);
       }
       return iconSize;
   }

   public JPanel getControlPanel()
   {
	  JPanel optionButtons = getOptionButtons();
      JPanel controlPanel = SetupPanel.createSetupPanel(lesson, optionButtons, "PicturesSetup"
                            , SetupPanel.FGBG + SetupPanel.ROTATE + SetupPanel.SCALE);
      return controlPanel;
   }

   public boolean isAcornSelected()
   {  return getOptionButtons().isAcornSelected();   }
	
   private OptionButtons getOptionButtons()
   {
      if (optionButtons == null)  
      { optionButtons = new OptionButtons(lesson); }
      return optionButtons;
   }

   // Methods common to picture play and picture set up panels.
   //-------------------------------------------------------------
   // Method to paint an icon to display
   //-------------------------------------------------------------   
   public void paintIcon(Graphics page, Point point, boolean sound)
   {
       point = displaySpot(point);
       ImageIcon icon  = null;
            
       int size =  getIconSize(lesson.getPictureData(0).getSize());
       if (sound)  icon = acorn;
       else        icon = anchor;
		 
       icon.paintIcon(this, page, point.x, point.y);
       Color color = page.getColor();
       page.setColor(Color.white);
       page.drawRect(point.x, point.y, size, size);
       page.setColor(color);
   }
      
   //-------------------------------------------------------------
   // Mouse handling methods.
   //-------------------------------------------------------------

   //-------------------------------------------------------------
   // Method to compute the area of the display corresponding to
   //   an acorn or link based on the display angle of the image
   //-------------------------------------------------------------
   public Point iconLocation(Point point, int angle)
   {
       int points = lesson.getPoints();
       switch (angle)
       {   
           case 90:  return new Point(points - 1 - point.y, point.x); 
           case 180: return new Point(points - 1 - point.x, points - 1 - point.y);
           case 270: return new Point(point.y, points - 1 - point.x);
       }  
       return point;
   }    
   
   //--------------------------------------------------------------
   // Method to compute where icon should display on the panel.
   //--------------------------------------------------------------  
   public Point displaySpot(Point point)
   {
      Dimension size = imagePanel.getSize();
      Rectangle view = new Rectangle(0, 0, size.width, size.height);

      if (imagePanel instanceof PicturesPlayLabel)
      {
          PicturesPlayLabel playLabel = (PicturesPlayLabel)imagePanel;
          size = lesson.getPictureData(0).getSize();
          view = playLabel.getViewableArea();
      }

      int points = lesson.getPoints();
  
      // layer already removed from the point.
      int iconSize =  getIconSize(lesson.getPictureData(0).getSize());
      Point spot = new Point();
      spot.x = point.x * size.width  / points;
      if (spot.x + iconSize > size.width ) spot.x = size.width - iconSize;
      spot.y = point.y * size.height / points;
      if (spot.y + iconSize > size.height ) spot.y = size.height - iconSize;
      spot.x += view.x;
      spot.y += view.y;
      return spot;
   }
   
   /** Method to convert an on-screen location to a key for lookup
    * 
    * @param point On screen location
    * @return key for lookup
    */
   public Point convertLocationToKey(Point location)
   {  
	  Dimension size = imagePanel.getSize();
	  Rectangle view = new Rectangle(0, 0, size.width, size.height);

	  if (imagePanel instanceof PicturesPlayLabel)
	  {
	      PicturesPlayLabel playLabel = (PicturesPlayLabel)imagePanel;
	      size = lesson.getPictureData(0).getSize();
	      view = playLabel.getViewableArea();
	  }
	    
	  // Translate to the ICON point number.
	  int points  = lesson.getPoints();
	  Point point = new Point();
      point.x = (location.x - view.x)* points / size.width;
	  point.y = (location.y - view.y) * points / size.height;
	  Point key = new Point(point.x, point.y);

	  int angle = lesson.getAngle();
	  switch (angle)
	  {
	  	 case 90:
	  		 key = new Point(point.y, points - 1 - point.x);
	  		 break;
	  		 
	  	 case 180:
	  		 key = new Point(points - 1 - point.x, points - 1 - point.y);
	  		 break;
	  		 
	  	 case 270:
	  		 key = new Point(points - 1 - point.y, point.x);
	  		 break;
	  }
      return key;
   }
   
   /** Determine if there is an intersecting acorn.
    *  @param point The position within a picture as a key value.
    *  @return A point object specifying an intersection key or null.
    */
   public Point intersectingAcorn(Point point)
   {
       int points = lesson.getPoints();
       Dimension size = lesson.getPictureData(0).getSize();
       int pixelsPerPointWidth = size.width/points;
       if (pixelsPerPointWidth == 0) pixelsPerPointWidth = 1;

       int pixelsPerPointHeight = size.height/points;
       if (pixelsPerPointHeight == 0) pixelsPerPointHeight = 1;

       int icon = getIconSize(size);
       int loopH  = (icon + pixelsPerPointHeight - 1)/ pixelsPerPointHeight;
       int loopW  = (icon + pixelsPerPointWidth  - 1) / pixelsPerPointWidth;
       
       int widthX = (int)Math.ceil(icon / pixelsPerPointWidth);
       int widthY = (int)Math.ceil(icon / pixelsPerPointHeight);
       int startX = point.x;
       int endX = point.x;
       
       int startY = point.y;
       int endY = point.y;
 
       // Expand setup search appropriately
       if (!lesson.isPlay())
       {
    	  // We clicked to the right and down from the original key
    	  //   since the key is the top left corner. Therefore,
    	  //   we need to extend the search up and to the left, which
    	  //   varies depending on the rotation angle.
    	  int angle = lesson.getAngle();
    	  switch(angle)
    	  {
    	  	 case 0:
    	    	 startX -= widthX;
    	         startY -= widthY;
    	  		 break;
    	     case 90:
    	    	 startX -= widthY;
    	    	 endY += widthX;
    	    	 break;
    	     case 180:
    	    	 endX += widthX;
    	    	 endY += widthY;
    	    	 break;
    	     case 270:
    	    	 endX += widthY;
    	    	 startY -= widthX;
    	    	 break;
    	  }
    	  
    	  int widthMax = (int)Math.max(widthX, widthY);
    	  if (startX<=widthMax) startX = 0;
    	  if (startY<=widthMax) startY = 0;
    	  if (endX + widthMax >= points - 1)  endX = points - 1;
    	  if (endY + widthMax >= points - 1)  endY = points - 1;
       }
       
       Hashtable<Point, PicturesSoundData> data = lesson.getSoundData();
       Point key = lookForPoint(data, startX, endX, startY, endY);
       if (key!=null) return key;

       // Expand the search if in play mode.
       if (lesson.isPlay())
       {   int w = loopW; // If not found expand search horizontally
           int h = loopH; // If not found expand search vertically
           int[] bounds
                = {startX-w*3/2, endX+w, startY-h*3/2, endY+h};

          for (int i=0; i<bounds.length; i++)
          {
              if (bounds[i]<0) bounds[i] = 0;
              if (bounds[i]>=points) bounds[i] = points-1;
          }

          // Look to the left
          if (startX != bounds[0])
          {  key = lookForPoint
                  (data, bounds[0], startX, bounds[2], bounds[3]);
             if (key!=null) return key;
          }

          // Look to the right
          if (endX!=bounds[1])
          {  key = lookForPoint
                  (data, endX, bounds[1], bounds[2], bounds[3]);
             if (key!=null) return key;
          }

          // Look above
          if (startY!=bounds[2])
          {  key = lookForPoint
                  (data, startX, endX, bounds[2], startY);
             if (key!=null) return key;
          }

          // look below
          if (endY!=bounds[3])
          {  key = lookForPoint
                  (data, startX, endX, endY, bounds[3]);
             if (key!=null) return key;
          }
       }
       return null;
   }   // End of intersecting acorn.

   /** Method to look for points for a selected acorn
    *
    * @param data  The data containing all the acorns and links
    * @param startWidth The starting x position
    * @param endWidth  The ending x position
    * @param startHeight The starting y position
    * @param endHeight The ending y position
    * @return The matching point or null
    */
   private Point lookForPoint(Hashtable<Point, PicturesSoundData> data
           , int startWidth, int endWidth, int startHeight, int endHeight)
   {
       int   layer = lesson.getLayer();
       PicturesSoundData sounds;
       Point key;

       for (int x=startWidth; x<=endWidth; x++)
       {   for (int y=startHeight; y<=endHeight; y++)
           {
              key = new Point(x * PicturesPanel.KEY_LAYERS + layer, y);
              if (data.containsKey(key))
              {   sounds = data.get(key);
                  if (sounds==null) data.remove(key);
                  else   { return key; }
              }
           }
       }
       return null;
   }   // End lookForPoint()

}  // End of PicturesPanel class.


// class to listen for button events.
class OptionButtons extends JPanel implements ActionListener
{
   public static final long serialVersionUID = 1;
   
   JButton acornButton, anchorButton, helpButton;
   boolean acornSelected; 
      
   public OptionButtons(LessonsPictures lesson)
   {  
      // Create buttons for control panel.
      acornButton  = new JButton
              (lesson.getIcon(AcornsProperties.ACORN, Lesson.ICON));
      acornButton.setBorder(BorderFactory.createLoweredBevelBorder());
      acornButton.setToolTipText(LanguageText.getMessage(lesson, 9));
      
      acornButton.addActionListener(this);
      acornSelected = true;
      
      anchorButton = new JButton
              (lesson.getIcon(AcornsProperties.ANCHOR, Lesson.ICON));
      anchorButton.setBorder(BorderFactory.createRaisedBevelBorder());
      anchorButton.setToolTipText(LanguageText.getMessage(lesson, 10));
      anchorButton.addActionListener(this);
	
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      add(acornButton);
      add(Box.createHorizontalStrut(5));
      add(anchorButton);
      setBorder(BorderFactory.createLineBorder(Color.black, 1));
      setSize(new Dimension(55,30));
      setPreferredSize(getSize());
      setMaximumSize(getSize());
   }


   //------------------------------------------------------------  
   // Method to check and set whether acorn button is depressed.
   //------------------------------------------------------------  
   public boolean isAcornSelected()    {  return acornSelected; }
	
	public void setAcorn(boolean acornSelected)
	{
	     this.acornSelected = acornSelected;
		    if (acornSelected)
      {
         acornButton.setBorder(BorderFactory.createLoweredBevelBorder());
         anchorButton.setBorder(BorderFactory.createRaisedBevelBorder());
      }
      else        
		    {
         acornButton.setBorder(BorderFactory.createRaisedBevelBorder());
         anchorButton.setBorder(BorderFactory.createLoweredBevelBorder());
      }
		    repaint();
	}
       
   public void actionPerformed(ActionEvent event)
   {
      if (event.getSource() == acornButton)
      {
         acornSelected = true;
         acornButton.setBorder(BorderFactory.createLoweredBevelBorder());
         anchorButton.setBorder(BorderFactory.createRaisedBevelBorder());
      }
        
      if (event.getSource() == anchorButton)
      {
         acornSelected = false;
         acornButton.setBorder(BorderFactory.createRaisedBevelBorder());
         anchorButton.setBorder(BorderFactory.createLoweredBevelBorder());
      }
   }     // End of actionPerformed method.
   
}        // End of OptionButtons class.
