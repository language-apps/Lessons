/**
 * PicturesPlayPanel.java
 *
 *   @author  HarveyD
 *   @version 5.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.lesson.flashCardsV6;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

import org.acorns.lesson.*;
import org.acorns.data.*;
import org.acorns.visual.*;
import org.acorns.language.*;

// Nested class that will draw icons that go over the top.
public class PicturesPlayPanel extends JPanel 
                 implements ActionListener, ComponentListener
{
   public final static long serialVersionUID = 1;

   private boolean gloss;
   private JPanel pictureDisplay;
   private FlashScrollPane scroll;
   private CardPiles piles;

   private LessonsFlashCards lesson;
   private BufferedImage     image;
      
   private JMenuItem[]        items;  // JMenuItems in the pop up menu
   private LessonPopupMenu    popup;  // Popup menu with extra options
   private LessonPlayControls lessonControls;  // control button object
   private String[] messageData;  // Item menu interface text
   
   public PicturesPlayPanel(LessonsFlashCards lesson)
   {  
      super();
      
      messageData = LanguageText.getMessageList(lesson, 1);

      lesson.select(null);  // Turn off selections (red borders).

      this.lesson = lesson;
      this.items  = new JMenuItem[2];
      gloss       = true;

      setLayout(new BorderLayout());

      pictureDisplay = new JPanel();
      pictureDisplay.setOpaque(true);

      ColorScheme colors = lesson.getColorScheme();

      PictureData picture = colors.getPicture();
      Dimension playSize = lesson.getDisplaySize();
      if (picture!=null)
           image = picture.getImage
              (this, new Rectangle(0,0,playSize.width,playSize.height));

      // Add the pack of cards to a card panel and place it in the north
      piles = new CardPiles(lesson);
      add(piles, BorderLayout.NORTH);

      scroll = new FlashScrollPane(lesson, piles);
      pictureDisplay.add(scroll);
      Dimension pictureSize = lesson.getDisplaySize();
      Dimension displaySize 
              = new Dimension(pictureSize.width, pictureSize.height);
      pictureDisplay.setSize(displaySize);
      pictureDisplay.setPreferredSize(displaySize);
      pictureDisplay.setMinimumSize(displaySize);
      pictureDisplay.setMaximumSize(displaySize);
      add(pictureDisplay, BorderLayout.CENTER);
 
      // Add the button controls on the bottom.
      lessonControls = getPictureControls();
      add(lessonControls, BorderLayout.SOUTH);

      addComponentListener(this);
      
    }  // End PicturesPlayContainer()

    /** Method to draw the picture in the background
     *
     * @param g The graphics object associated with this component
     */
    public void paintComponent(Graphics g)
    {
       Dimension size = lesson.getDisplaySize();
       if (image!=null)
          g.drawImage
           (image, 0, 0, image.getWidth(), image.getHeight(), this);
       else
       {
          Color color = new Color(204,204,204);
          g.setColor(color);
          g.fillRect(0, 0, size.width, size.height);
       }
    }

   // Methods for handling if the panel is resized
   boolean resize = false;
   Dimension lastSize = new Dimension(-1,-1);
   public void componentMoved(ComponentEvent ev) {}
   public void componentResized(ComponentEvent ev) 
   { 
     if (lastSize.width<=0)  lastSize = getSize();
     
     Dimension thisSize = getSize();
     if (lastSize.equals(thisSize)) return;
     
     lastSize = new Dimension(getSize());
     lesson.displayLesson();
   }
   public void componentHidden(ComponentEvent ev) {}
   public void componentShown(ComponentEvent ev)  {}
 
   /** Create the control panel at the bottom of the play screen
    *  @return LessonPictureControls pop up menu
    */
   public LessonPlayControls getPictureControls()
   {
      String glossNative = messageData[0];
      if (!gloss) glossNative = messageData[1];

      items[0] = new JMenuItem(glossNative);
      items[0].addActionListener(this);

      items[1] = new JMenuItem(messageData[2]);
      items[1].addActionListener(this);
 
      // Create Popup menu
      popup = new LessonPopupMenu(lesson, this, items);
      LessonPlayControls controls = new LessonPlayControls
                                           (lesson, popup, "FlashPlay");
      return controls;
      
   }    // End of getPictureControls()
   
   //------------------------------------------------------------
   // Method to respond to drop down menu.
   //------------------------------------------------------------
   public void actionPerformed(ActionEvent event)
   {
      JMenuItem source = (JMenuItem)event.getSource(); 
      if (!popup.isArmed()) {  return; }
      popup.cancel();      //Disable the Popup menu.         

      if (source == items[0])
      {
         // Adjust the play options.
         gloss = !gloss;
         String glossNative = messageData[0];
         if (!gloss) glossNative = messageData[1];
         items[0].setText(glossNative);
         scroll.reset(gloss);
         piles.reset();
      }
      else if (source == items[1]) 
      {  scroll.reset(gloss);
         piles.reset();
      }
      else  Toolkit.getDefaultToolkit().beep();
   }  // End of actionPerformed.

 
}  // End of PicturesPlayLabel class.