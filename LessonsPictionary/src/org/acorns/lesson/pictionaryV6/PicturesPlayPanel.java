/**
 * PicturesPlayPanel.java
 *
 *   @author  HarveyD
 *   @version 6.00
 *
 *   Copyright 2010, all rights reserved
*/
package org.acorns.lesson.pictionaryV6;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.awt.image.*;

import org.acorns.lesson.*;
import org.acorns.data.*;
import org.acorns.visual.*;
import org.acorns.widgets.*;

// Nested class that will draw icons that go over the top.
public class PicturesPlayPanel extends JPanel 
                 implements MouseListener, ActionListener, ComponentListener
{
   public final static long serialVersionUID = 1;
   private final static int DISPLAY_SIZE = 100;

   private JPanel pictureDisplay;
   private LessonsPictionary lesson;
   private SoundData         sound;
      
   private LessonPlayControls controls; // Object with control buttons

   public PicturesPlayPanel(LessonsPictionary lessonObject)
   {  
      super();

      lesson = lessonObject;
      lesson.select(null);  // Turn off selections (red borders).

      pictureDisplay = new JPanel()
      {
         private final static long serialVersionUID = 1;
    	  
         public @Override void paintComponent(Graphics graphics)
         {
             super.paintComponents(graphics);
             ColorScheme colors = lesson.getColorScheme();
             PictureData picture = colors.getPicture();
             Dimension size = getSize();

             if (picture !=null)
             {  BufferedImage image = picture.getImage
                         (this, new Rectangle(0, 0, size.width, size.height));
                graphics.drawImage(image, 0, 0, size.width, size.height, null);
             }
             else
             {
                Color color = new Color(204,204,204);
                graphics.setColor(color);
                graphics.fillRect(0, 0, size.width, size.height);
             }
         }
      };

      FlowLayout layout = new FlowLayout();
      pictureDisplay.setLayout(layout);

      PictureChoice[] choices = lesson.getActivePictureData();
      ChoiceButton choice;
      int numChoices = lesson.getActiveChoices();
      for (int i=0; i<numChoices; i++)
      {   choice = choices[i].getButton();
          pictureDisplay.add(choice);
          choice.resizeButton(new Dimension(DISPLAY_SIZE, DISPLAY_SIZE));
          choice.addMouseListener(this);
      }

      Dimension displaySize = lesson.getDisplaySize();
      displaySize.width -= 30;
      displaySize.height -= 200;

      int vGap = layout.getVgap();
      int hGap = layout.getHgap();
      double rows = .999 + 1.0*numChoices*(DISPLAY_SIZE+hGap)/displaySize.width;

      int newHeight = (DISPLAY_SIZE +vGap)* (int)rows;
      if (newHeight > displaySize.height) displaySize.height = newHeight;

      pictureDisplay.setSize(displaySize);
      pictureDisplay.setPreferredSize(displaySize);

      setLayout(new BorderLayout());
      JScrollPane scroll = new JScrollPane(pictureDisplay);
      add(scroll, BorderLayout.CENTER);
      controls = new LessonPlayControls(lesson, null, "PictionaryPlay");
      controls.setBackground(new Color(80,80,80));
      controls.setOpaque(true);
      add(controls, BorderLayout.SOUTH);
      addComponentListener(this);
    }  // End PicturesPlayContainer()
  
   //------------------------------------------------------------
   // Unused MouseListener methods.
   //------------------------------------------------------------
   // Method to get the button object that was pressed.
   private ChoiceButton getChoiceButton(MouseEvent event)
   {
       String sourceName = event.getSource().getClass().getSimpleName();
       if (!sourceName.equals("ChoiceButton")) return null;
       
       return (ChoiceButton)event.getSource();       
   }

   //------------------------------------------------------------------------------
   // Method to use to insert, modify, or change an icon.
   //------------------------------------------------------------------------------
   public void mouseClicked(MouseEvent event)  {}
   public void mouseEntered(MouseEvent event)  {}
   public void mouseExited(MouseEvent event)   {}
   public void mousePressed (MouseEvent event) {}
   public void mouseReleased(MouseEvent event) 
   { 
      ChoiceButton choice = getChoiceButton(event);
      if (choice!=null)
      {
         if (!choice.isVisible()) return;

         PictureChoice pictureChoice = choice.getPictureChoice();
         PicturesSoundData soundData
                 = pictureChoice.getQuestions(lesson.getLayer());
         selectAudio(soundData);
         lesson.select(null);
         choice.selectBorders(true, true);

         Frame frame = (Frame) SwingUtilities.getAncestorOfClass
                                    (Frame.class, this);
         Point point = frame.getMousePosition();
         String title = lesson.getTitle();
         //Point point = event.getLocationOnScreen();

         ColorScheme colors = lesson.getColorScheme();
         Color fg = colors.getColor(false);
         Color bg = colors.getColor(true);
         new WindowDialog(lesson, frame, title, sound, point, fg, bg);
      }
   }     // End of mouseListener

   public void actionPerformed(ActionEvent event)
   {
      if (sound!=null && sound.isRecorded())  sound.playBack(null, 0, -1);
   }

   /** Echo a sound to display and return the text for it */
   public String[] selectAudio(PicturesSoundData sounds)
   {
      Vector<SoundData> soundVector = sounds.getVector();

      // Randomly pick one to echo.
      int size = soundVector.size();
      int selection = (int)(Math.random() * size);

      sound = soundVector.get(selection);
      String[] text = sound.getSoundText();
      if (sound.isRecorded())  sound.playBack(null, 0, -1);
      return text;
   }

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
   public void componentShown(ComponentEvent ev) {}

}  // End of PicturesPlayLabel class.