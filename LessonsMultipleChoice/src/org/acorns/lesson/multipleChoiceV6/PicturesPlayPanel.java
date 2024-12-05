/**
 * PicturesPlayPanel.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.lesson.multipleChoiceV6;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.border.EtchedBorder;

import java.awt.image.*;
import java.util.Arrays;

import org.acorns.data.*;
import org.acorns.lesson.*;
import org.acorns.language.*;
import org.acorns.visual.*;
import org.acorns.widgets.*;

// Nested class that will draw icons that go over the top.
public class PicturesPlayPanel extends JPanel 
                 implements MouseListener, ActionListener, ComponentListener
{
   public final static long serialVersionUID = 1;
  
   private final static int DISPLAY_NUM = 4;
   private final static int LABEL_HEIGHT = 60, SCORE_WIDTH = 175;
   private final static int DELAY_TIME = 2000, MAX_DELAY = 5, FONT = 16;
      
   private JPanel pictureDisplay;
   private Container scoreLabel;
   private JLabel questionDisplay, nativeDisplay, scoreDisplay;
   
   private LessonsMultipleChoice lesson;
   private PictureSound pictureSound;
      
   private PictureChoice correctAnswer;
   private SoundData soundData, chosenSound, feedbackSound;
   private SoundThread soundThread;
   private BufferedImage image;
   private ColorScheme colors;
   
   private JMenuItem[]        items;          // JMenuItems in the pop up menu
   private LessonPopupMenu    popup;          // Popup menu with extra options
   private LessonPlayControls lessonControls; // Object with control buttons
   private String[]           messageData;    // Item menu text
   private String[]           lastSoundText;  // Text from previous text
   
   public PicturesPlayPanel(LessonsMultipleChoice lesson)   
   {  
      super();
      this.lesson = lesson;
      this.items  = new JMenuItem[3];

      messageData = LanguageText.getMessageList("commonHelpSets", 1);
		    pictureSound = new PictureSound(lesson);
      lesson.select(null);  // Turn off selections.
      
      setLayout(new BorderLayout());

      colors = lesson.getColorScheme();
      PictureData picture = colors.getPicture();
      Dimension playSize = lesson.getDisplaySize();
      if (picture!=null)
         image = picture.getImage
           (this, new Rectangle(0,0,playSize.width,playSize.height));

      pictureDisplay = new JPanel();
      pictureDisplay.setLayout(new GridLayout(2,2,10,10));
      Dimension pictureSize = lesson.getPictureSize();
      Dimension displaySize 
              = new Dimension(2*pictureSize.width+10, 2*pictureSize.height+10);
      pictureDisplay.setSize(displaySize);
      pictureDisplay.setPreferredSize(displaySize);
      pictureDisplay.setOpaque(false);
 
      JPanel centerPanel = new JPanel();
      centerPanel.add(Box.createHorizontalGlue());
      centerPanel.add(pictureDisplay);
      centerPanel.add(Box.createHorizontalGlue());
      centerPanel.setOpaque(false);
      add(centerPanel, BorderLayout.CENTER);
            
      // Add the button controls on the bottom.
      scoreLabel = makeLabelPanel();
      add(scoreLabel, BorderLayout.NORTH);
      
      // Add the button controls on the bottom.
      lessonControls = getPictureControls();
      add(lessonControls, BorderLayout.SOUTH);    
      setComponents(true);
      addComponentListener(this);
 
    }  // End PicturesPlayContainer()
   
   
   // Make the panel to hold the questions and the score.
   private Container makeLabelPanel()
   {
      // Set the proportions of the pictures and labels.
      Dimension pictureSize  = lesson.getPictureSize();
      int picWidth = pictureSize.width;
       
      Container labelDisplay = new JPanel();
      labelDisplay.setBackground(colors.getColor(true));
      labelDisplay.setLayout(new BoxLayout(labelDisplay, BoxLayout.X_AXIS));
 
      // Add score label with appropriate font and text.
      Font font = new Font(null, Font.PLAIN, FONT);
      scoreDisplay = new JLabel("");
      
      Dimension displaySize = new Dimension
              (SCORE_WIDTH, LABEL_HEIGHT);
      scoreDisplay = Score.getScoreLabel(displaySize);
      scoreDisplay.setBackground(colors.getColor(true));
      scoreDisplay.setForeground(colors.getColor(false));

      // Add the question label with appropriate font and size.
      JPanel questions = new JPanel();
      questions.setSize(new Dimension
              (picWidth*2 + 150 - SCORE_WIDTH, LABEL_HEIGHT));
      questions.setPreferredSize(questions.getSize());
      questions.setMinimumSize(questions.getSize());
      questions.setLayout(new BoxLayout(questions, BoxLayout.Y_AXIS));
      questions.setBackground(colors.getColor(true));
      questions.setBorder(BorderFactory.createEtchedBorder
           (EtchedBorder.LOWERED, new Color(200,200,200), new Color(50,50,50)));
      
      questionDisplay = new JLabel(" ");
      questionDisplay.setBackground(colors.getColor(true));
      questionDisplay.setOpaque(true);
      questionDisplay.setForeground(colors.getColor(false));
      questionDisplay.setFont(font);       
      
      nativeDisplay = new JLabel(" ");
      nativeDisplay.setBackground(colors.getColor(true));
      nativeDisplay.setOpaque(true);
      nativeDisplay.setForeground(colors.getColor(false));
      questions.add(questionDisplay);
      questions.add(nativeDisplay);

      // Finish the label display panel.
      labelDisplay.add(Box.createHorizontalGlue());
      labelDisplay.add(questions);
      labelDisplay.add(Box.createHorizontalStrut(5));
      labelDisplay.add(scoreDisplay);
      labelDisplay.add(Box.createHorizontalGlue());
      return labelDisplay;
   }
   
   // Layout the components for the next question.
   private void setComponents(boolean correct)
   {
      // Remove the listeners to all buttons.
      if (correct)
      {  
         lesson.removeListeners();
         pictureDisplay.removeAll();
      
      // Randomly choose the next question and add mouse listeners.
         displayRandomPictureSet();
       }
      // Update the labels to display the question and the current score.
      Score.calculateScore();
      
      // Play the audio.
      soundData = chosenSound;
      boolean options[] = lesson.getOptions();
      if(options[AcornsProperties.SPEECH])
      {  soundThread = new SoundThread();
         soundThread.start();
      }
      
      // Repaint everything.
      revalidate();
      repaint();
      requestFocus();
   }
   
   // Select a random picture.
   private void displayRandomPictureSet()
   {
	  // Select which picture will be the correct choice. 
	  int rand = (int) (Math.random() * DISPLAY_NUM);
	  PictureChoice[] choices = new PictureChoice[DISPLAY_NUM];
	  int[] selections = new int[DISPLAY_NUM];
	  
	  PictureChoice choice;
      boolean done;
      
      // Update the popup options if necessary
      boolean[]  options = lesson.getOptions();
      if (options[AcornsProperties.SPEECH])
           items[0].setText(messageData[0]);
      else items[0].setText(messageData[1]);
      if (options[AcornsProperties.SPELLING])
           items[1].setText(messageData[2]);
      else items[1].setText(messageData[3]);
      if (options[AcornsProperties.GLOSS])
           items[2].setText(messageData[4]);
      else items[2].setText(messageData[5]);

      // Now pick a set of pictures
      for (int i=0; i<DISPLAY_NUM; i++)
      {  do
         {
            choice = choices[i] = pictureSound.selectPicture();
            selections[i] = pictureSound.getSelection();
            
            done = true;
            for (int c=0; c<i; c++)
            {
                if (choice.getButton()==pictureDisplay.getComponent(c)) 
                {  done = false; break; }
            }
         }  while (!done);

         ChoiceButton button = choice.getButton();
         button.addMouseListener(this);
         pictureDisplay.add(button);
         
      }  // End for

      String[] soundText = null;
      for (int again=0; again<2; again++)
      {
       	 correctAnswer = choices[rand];
         chosenSound = pictureSound.selectSound(selections[rand]);
         soundText = chosenSound.getSoundText();
         boolean same = Arrays.equals(soundText,  lastSoundText);
         if (!same)
         {
        	 lastSoundText = soundText;
        	 break;
         }
         rand = (rand+1)%DISPLAY_NUM;
       }
  
       String gloss  = "";
       String spell  = "";
        
       if (options[AcornsProperties.GLOSS])    gloss = soundText[0];
       if (options[AcornsProperties.SPELLING]) spell = soundText[1];
       questionDisplay.setText(gloss);
       nativeDisplay.setText(spell);
        
       String language = "English";
       if (soundText.length>2 && soundText[2]!=null) language = soundText[2];
        
       KeyboardFonts.getLanguageFonts().setFont(language, nativeDisplay);
   }        // End choosePictureSet()
   
     /** Method to draw the picture in the background
     *
     * @param g The graphics object associated with this component
     */
    public void paintComponent(Graphics g)
    {
       if (image!=null)
          g.drawImage
           (image, 0, 0, image.getWidth(), image.getHeight(), this);
       else
       {
          Color color = colors.getColor(true);
          g.setColor(color);
          g.fillRect(0, 0, getWidth(), getHeight());
       }
    }

   public void paintComponents(Graphics page)
   { 
      super.paintComponents(page);

   }  // End paintComponents()

   /** Method to stop the audio.
    *
    */
   public void stopSound()
   {
      if (soundData!=null) soundData.stopSound();
      if (soundThread!=null && soundThread.isAlive()) soundThread.interrupt();
         soundThread = null;
         soundData   = null;
   }
   
   /** Override JCOmponent removeNotify to make sure that playback stopped. */
   public void removeNotify()  { super.removeNotify(); stopSound(); }
   
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
   public void mouseClicked(MouseEvent event)
   {
   }  // End mouseClicked()
   
   public void mousePressed (MouseEvent event) 
   {   
   }
   public void mouseReleased(MouseEvent event) 
   { 
      ChoiceButton choice = getChoiceButton(event);
      if (choice!=null)
      {
         if (soundThread!=null) stopSound();
               // Load the sounds to indicate if the user entered a good answer.

         boolean correct = event.getSource()==correctAnswer.getButton();
         if (correct)
         {
             feedbackSound = lesson.getSound(AcornsProperties.CORRECT);
         }
         else
         {
             feedbackSound = lesson.getSound(AcornsProperties.INCORRECT);
         }
         feedbackSound.playBack(null,0,-1);
         
         Score.nextScore(correct);
         setComponents(correct);
      }
   }

   public void mouseEntered(MouseEvent event)   {}
   public void mouseExited(MouseEvent event)    {}
  
   //-----------------------------------------------------------------
   // Nested class to start sounds after a delay.
   //-----------------------------------------------------------------
   class SoundThread extends Thread
   {   
       public SoundThread()   {}
       
       public void run()
       {  
           try
          {  int count = 0;
             while (feedbackSound!=null && feedbackSound.isActive())
             {  sleep(DELAY_TIME/4);
                if (count++==MAX_DELAY) feedbackSound.stopSound();
             }
             sleep(DELAY_TIME);
             if (soundData != null)
             {   soundData.playBack(null,0,-1);
             }
          }
          catch (InterruptedException ex) 
          {
              if (soundData!=null) soundData.stopSound();
              if (feedbackSound!=null) feedbackSound.stopSound();
          }

       }
   }
   
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
              = new LessonPlayControls(lesson, popup, "MultPlay");
         
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

}  // End of PicturesPlayLabel class.