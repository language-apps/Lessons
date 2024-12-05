/*
 * MagnetPlayComponent.java
 *
 *   @author  HarveyD
 *   @version 6.00
 *
 *   Copyright 2007-2015, all rights reserved
 */

package org.acorns.lesson.questionAnswersV7;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;
import java.util.*;

import org.acorns.language.*;
import org.acorns.visual.*;
import org.acorns.data.*;
import org.acorns.lesson.*;

/** Class to execute magnet game lessons */
public class QuestionsAnswersPlayPanel extends JPanel implements ActionListener
{
   private final static long serialVersionUID = 1;
   
   private final int NUM_SENTENCES[] = {1, 2, 3, 4, 5};
   private final int ROWS = 3, COLS = 4;

   private LessonsQuestionsAnswers lesson;
   private ColorScheme       colors;
   private DisplayArea       displayArea;

   private LessonPlayControls lessonControls;
   private LessonPopupMenu    popup;       // supplemental popup menu
   private JMenuItem[]        items;       // JMenuItems in the pop up menu

   /** Constructor to initialize the game */
   public QuestionsAnswersPlayPanel(LessonsQuestionsAnswers lesson)
   {  
      this.lesson = lesson;
      colors = lesson.getColorScheme();
      
      items = new JMenuItem[4];
      setLayout(new BorderLayout());
      setBackground(new Color(80, 80, 80));
      displayArea = new DisplayArea(colors);
      add(displayArea, BorderLayout.CENTER);

      lessonControls = getPictureControls();
      add(lessonControls, BorderLayout.SOUTH);
      Score.reset();
      resetGame();
   }

   /** Method to reset the game
    *    It creates a new set of magnets and disburses them on the display
    */
   private void resetGame()
   {
      Vector<SentenceAudioPictureData> sentences = lesson.getSentenceData();

      int size = sentences.size();
      int which, index;
      int[] free = new int[size];
      for (int i=0; i<sentences.size(); i++)   
      {
          free[i] = i;  
      }

      PhrasePanel  phrase;
      int difficultyLevel = Score.getDifficultyLevel() - 1;
      int count = 0;
      int freeSize = ROWS*COLS;
      int[] spots = new int[ROWS*COLS];
      JPanel panel;
      for (int i=0; i<spots.length; i++)
      {
         spots[i] = i;
         panel = (JPanel)displayArea.getComponent(i);
         if (panel.getComponentCount()!=0)
         {
             panel.removeAll();
             panel.repaint();
         }
      }
      
      while (size>0)
      {  
         which = (int)(Math.random() * size--);
         index = free[which];
         phrase = new PhrasePanel(lesson, sentences.get(index), colors);
         free[index] = free[size];
         
         which = (int)(Math.random()*freeSize--);
         index = spots[which];
         spots[index] = spots[freeSize];
         
         panel = (JPanel)displayArea.getComponent(index);
         panel.add(phrase, BorderLayout.CENTER);
         if (++count>=NUM_SENTENCES[difficultyLevel]) break;
      }
      displayArea.validate(); 

   }  // End of resetGame()

  /** Create the control panel at the bottom of the play screen
    *  @return LessonPictureControls pop up menu
    */
   public final LessonPlayControls getPictureControls()
   {
      // Initially set continuous playback off
      items[0] = new JMenuItem(LanguageText.getMessage(lesson, 7));
      items[0].addActionListener(this);

      // Create menu items for changing the difficulty level
      items[1] = null;
      items[2] = new JMenuItem(adjustDifficultyLevelTip(true));
      items[2].addActionListener(this);

      items[3] = new JMenuItem(adjustDifficultyLevelTip(false));
      items[3].addActionListener(this);

      // Create Popup menu
      popup = new LessonPopupMenu(lesson, this, items);

      LessonPlayControls controls
              = new LessonPlayControls(lesson, popup, "QuestionsAnswersPlay");

      return controls;

   }    // End of getPictureControls()


   // Get the difficulty level text for either tool tips or a menu item
   private String adjustDifficultyLevelTip(boolean flag)
   {
      String[] difficulties = LanguageText.getMessageList(lesson, 5);
      String[] difficultyTips = LanguageText.getMessageList(lesson, 6);

       int level = Score.getDifficultyLevel();
       if (flag)
       {   if (level >= Score.MAX_DIFFICULTY)
                return difficultyTips[0];
           else return difficultyTips[2] + " " + difficulties[level-1];
       }
       else
       {   if (level <= Score.MIN_DIFFICULTY)
                return difficultyTips[1];
           else return difficultyTips[3] + " " + difficulties[level-1];
       }
   }        // End of AdjustDifficultyLevelTip()

   /** Respond to pop up menu and button commands
    *  @param event object triggering the listener
    */
   public void actionPerformed(ActionEvent event)
   {
      int level = Score.getDifficultyLevel();

      JMenuItem source = (JMenuItem)event.getSource();
      if (!popup.isArmed()) {  return; }
      popup.cancel();      //Disable the Popup menu.

      // Check if we should be in continuous playback mode
      if (source==items[0])  { resetGame(); return; }

      // Check if we are to Adjust difficulty level
      if (source == items[2])
      {
         if (level<Score.MAX_DIFFICULTY)
         {   Score.setDifficultyLevel(++level);
             lesson.setDirty(true);
             items[2].setText(adjustDifficultyLevelTip(true));
             items[3].setText(adjustDifficultyLevelTip(false));
             resetGame();
             return;
         }
      }
      if (source == items[3])
      {
          if (level>Score.MIN_DIFFICULTY)
          {  Score.setDifficultyLevel(--level);
             lesson.setDirty(true);
             items[2].setText(adjustDifficultyLevelTip(true));
             items[3].setText(adjustDifficultyLevelTip(false));
             resetGame();
             return;
          }
      }

      Toolkit.getDefaultToolkit().beep();
      return;
   }      // End of actionPerformed()
 
   /** The panel to display all of  the phrases for the game
     * 
     * @param size The size of the display panel
     */
   public class DisplayArea extends JPanel
   {
	  private final static long serialVersionUID = 1;
	  	   
      PictureData picture;
      Color       background;
            
      public DisplayArea(ColorScheme colors)
      {  super();
         setLayout(new GridLayout(ROWS, COLS));
         
         // Display the background picture if it exists
         picture = colors.getPicture();
         background = colors.getColor(true);
         
         // Fill the grid with empty panels
         JPanel panel;
         for (int i=0; i<ROWS*COLS; i++)  
         { 
             panel = new JPanel();
             panel.setLayout(new BorderLayout());
             panel.setOpaque(false);
             add(panel); 
             panel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
         }

      }
      
      public @Override void paintComponent(Graphics g)
      {
          super.paintComponent(g);
          if (picture != null)
          {
              g.setColor(background);
              Dimension size = getSize();
              g.fillRect(0, 0, size.width, size.height);
                
              if (picture !=null)
              {  
                 BufferedImage image = picture.getImage
                         (this, new Rectangle(0, 0, size.width, size.height));
                 g.drawImage(image,0,0,size.width,size.height,null);
              }
          }
      }
   }
          
}     // End of QuestionAnswerPlayPanel
