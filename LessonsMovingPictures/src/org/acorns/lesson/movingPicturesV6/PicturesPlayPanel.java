/**
 * PicturesPlayPanel.java
 *
 *   @author  HarveyD
 *   @version 5.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.lesson.movingPicturesV6;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.EtchedBorder;

import org.acorns.lesson.*;
import org.acorns.language.*;
import org.acorns.data.*;
import org.acorns.visual.*;
import org.acorns.widgets.*;

// Nested class that will draw icons that go over the top.
public class PicturesPlayPanel extends JPanel 
                 implements MouseListener, ActionListener, ComponentListener
{
   public final static long serialVersionUID = 1;

   private final static int DELAY_TIME  = 100;
   private final static int FONT_SIZE   = 16;
      
   private JLabel pictureDisplay;
   private JLabel glossDisplay, nativeDisplay;
   
   private LessonsMovingPictures lesson;
   private PictureSound          pictureSound;
   private PictureControls[]     pictureControls;
      
   private JMenuItem[]        items;          // JMenuItems in the pop up menu
   private LessonPopupMenu    popup;          // Popup menu with extra options
   private LessonPlayControls lessonControls; // Object with control buttons

   private String[] messageData; // item menu text
      
   public PicturesPlayPanel(LessonsMovingPictures lesson)
   {  
      super();

      messageData = LanguageText.getMessageList("commonHelpSets", 1);

      lesson.select(null);  // Turn off selections (red borders).

      this.lesson = lesson;
      this.items  = new JMenuItem[3];

      int activeChoices =  lesson.getActiveChoices();
      PictureChoice[] choices = lesson.getActivePictureData();
	  pictureSound
          = new PictureSound(choices, activeChoices);

      setLayout(new BorderLayout());

      pictureDisplay = new JLabel();
      ColorScheme colors = lesson.getColorScheme();
      setBackground(colors.getColor(true));

      PictureData picture = colors.getPicture();
      ImageIcon icon = null;
      if (picture!=null)
          icon =picture.getIcon(lesson.getDisplaySize());
      if (icon!=null)  pictureDisplay.setIcon(icon);

      pictureDisplay.setLayout(null);
      Dimension pictureSize = lesson.getDisplaySize();
      Dimension displaySize 
              = new Dimension(pictureSize.width, pictureSize.height-70);
      pictureDisplay.setSize(displaySize);
      pictureDisplay.setPreferredSize(displaySize);
      add(pictureDisplay, BorderLayout.CENTER);

      // Create the picture component and their controls
      pictureControls = new PictureControls[activeChoices];

      ChoiceButton choice;
      for (int i=0; i<activeChoices; i++)
      {
          choice = choices[i].getButton();
          pictureDisplay.add(choice);
          choice.addMouseListener(this);
          choice.setVisible(false);
          pictureControls[i] = new PictureControls();
      }
      for (int i=0; i<PictureSound.DISPLAY_COUNT; i++)
      {   choice = pictureSound.selectPicture();
          choice.setVisible(true);
      }

      // Add the gloss and native text display to the top
      add(makeLabelPanel(), BorderLayout.NORTH);
      
      // Add the button controls on the bottom.
      lessonControls = getPictureControls();
      add(lessonControls, BorderLayout.SOUTH);

      addComponentListener(this);
      
      Thread thread = new PlayThread(this);
      thread.start();
 
    }  // End PicturesPlayContainer()
   
   
   // Make the panel to hold the gloss and native text labels display
   private JPanel makeLabelPanel()
   {
      // Set the proportions of the pictures and labels.
      Dimension labelSize  = lesson.getDisplaySize();
      labelSize.height = 60;
       
      // Add score label with appropriate font and text.
      Font font = new Font(null, Font.PLAIN, FONT_SIZE);

      // Add the question label with appropriate font and size.
      ColorScheme colors = lesson.getColorScheme();
      JPanel labels = new JPanel();
      labels.setSize(labelSize);
      labels.setPreferredSize(labelSize);
      labels.setMinimumSize(labelSize);
      labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
      labels.setBackground(colors.getColor(true));
      labels.setBorder(BorderFactory.createEtchedBorder
           (EtchedBorder.LOWERED, new Color(200,200,200)
                                , new Color(50,50,50)));
      
      glossDisplay = new JLabel(" ");
      glossDisplay.setBackground(colors.getColor(true));
      glossDisplay.setForeground(colors.getColor(false));
      glossDisplay.setOpaque(true);
      glossDisplay.setFont(font);
      
      nativeDisplay = new JLabel(" ");
      nativeDisplay.setBackground(colors.getColor(true));
      nativeDisplay.setForeground(colors.getColor(false));
      nativeDisplay.setOpaque(true);
      labels.add(glossDisplay);
      labels.add(nativeDisplay);

      // Finish the label display panel.
      JPanel labelDisplay = new JPanel();
      labelDisplay.setBackground(colors.getColor(true));
      labelDisplay.setLayout
              (new BoxLayout(labelDisplay, BoxLayout.X_AXIS));
      labelDisplay.add(Box.createHorizontalGlue());
      labelDisplay.add(labels);
      labelDisplay.add(Box.createHorizontalGlue());
      return labelDisplay;
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
   public void componentShown(ComponentEvent ev)  {}

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

         boolean[]  options = lesson.getOptions();
         PictureChoice pictureChoice = choice.getPictureChoice();
         PicturesSoundData soundData
                 = pictureChoice.getQuestions(lesson.getLayer());
         lesson.select(null);
         choice.selectBorders(true, true);

         String[] soundText = pictureSound.selectAudio(soundData);

         String gloss  = "";
         String spell  = "";

         if (options[AcornsProperties.GLOSS])    gloss = soundText[0];
         if (options[AcornsProperties.SPELLING]) spell = soundText[1];

         glossDisplay.setText(gloss);
         nativeDisplay.setText(spell);

         String language = "English";
         if (soundText.length>2 && soundText[2]!=null)
              language = soundText[2];

         KeyboardFonts.getLanguageFonts().setFont(language, nativeDisplay);
      }
   }     // End of mouseListener

   
   
   /** Create the control panel at the bottom of the play screen
    *  @return LessonPictureControls pop up menu
    */
   public LessonPlayControls getPictureControls()
   {
      // Create menu items for changing the difficulty level
      boolean[]  options = lesson.getOptions();
      if (options[AcornsProperties.SPEECH])
           items[0] = new JMenuItem(messageData[0]);
      else items[0] = new JMenuItem(messageData[1]);
      items[0].addActionListener(this);
 
        
      if (options[AcornsProperties.SPELLING])
           items[1] = new JMenuItem(messageData[2]);
      else items[1] = new JMenuItem(messageData[3]);
      items[1].addActionListener(this);
        
      if (options[AcornsProperties.GLOSS])
           items[2] = new JMenuItem(messageData[4]);
      else items[2] = new JMenuItem(messageData[5]);
      items[2].addActionListener(this);
 
      // Create Popup menu
      popup = new LessonPopupMenu(lesson, this, items);
			    
      LessonPlayControls controls 
              = new LessonPlayControls(lesson, popup, "MovingPicturesPlay");
         
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

      // Adjust the play options.
      boolean[]  options = lesson.getOptions();
      if (source == items[0])
      {  
         if (options[AcornsProperties.SPEECH])
         {
              options[AcornsProperties.SPEECH] = false;
              items[0].setText(messageData[1]);
         }
         else 
         {
             options[AcornsProperties.SPEECH] = true;
             items[0].setText(messageData[0]);
         }
         lesson.setOptions(options);
         return;
      }
      if (source == items[1])
      {
         if (options[AcornsProperties.SPELLING])
         {
              options[AcornsProperties.SPELLING] = false;
              items[1].setText(messageData[3]);
         }
         else 
         {
             options[AcornsProperties.SPELLING] = true;
             items[1].setText(messageData[2]);
         }
         lesson.setOptions(options);
         return;
      }
      if (source == items[2])
      {
         if (options[AcornsProperties.GLOSS])
         {   options[AcornsProperties.GLOSS] = false;
             items[2].setText(messageData[5]);
         }
         else 
         {
             options[AcornsProperties.GLOSS] = true;
             items[2].setText(messageData[4]);
         }            
         lesson.setOptions(options);
         return;
      }
      
      // Unrecognized option
      Toolkit.getDefaultToolkit().beep();
      
   }  // End of actionPerformed.

      /** Nested thread to control the animation */
   private class PlayThread extends Thread
   {
       public PlayThread(PicturesPlayPanel parent)
       {}
       /** Thread method to change animation periodically */
       @SuppressWarnings("static-access")
       public void run()
       {
           ChoiceButton button;
           int count;
           try
           {
               while (true)
               {  Thread.currentThread().sleep(DELAY_TIME);
                  count = pictureDisplay.getComponentCount();
                  if (count==0)  { lesson.select(null); return; }
   
                  for (int i=0; i<pictureDisplay.getComponentCount(); i++)
                  {
                      button = (ChoiceButton)pictureDisplay.getComponent(i);

                      if (!button.isVisible()) continue;
                      if (!pictureControls[i].movePicture
                              (pictureDisplay, button, DELAY_TIME))
                      {
                         pictureControls[i].resetPicture();
                         button.setVisible(false);
                         remove(pictureSound.removePicture(button));

                         lesson.select(null);
                         button = pictureSound.selectPicture();
                         button.selectBorders(true, true);
                         button.setVisible(true);
                         Thread.currentThread().sleep(1000);
                         repaint();
                      }  // End if
                  }      // End for getComponentCount()
               }         // End while
           }             // End try
           catch (Exception e) {}
           lesson.select(null);
       }   // End run()
   }

}  // End of PicturesPlayLabel class.