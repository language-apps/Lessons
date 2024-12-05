/*
 * MagnetPlayComponent.java
 *
 *   @author  HarveyD
 *   @version 6.00
 *
 *   Copyright 2007-2015, all rights reserved
 */

package org.acorns.lesson.magnetGameV7;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import java.util.*;

import org.acorns.language.*;
import org.acorns.visual.*;
import org.acorns.data.*;
import org.acorns.lesson.*;
import org.acorns.widgets.*;


/** Class to execute magnet game lessons */
public class MagnetPlayComponent extends JPanel 
        implements ActionListener, MouseListener, MouseMotionListener
{
   private final static long serialVersionUID = 1;
	
   private final int NUM_SENTENCES[] = {1, 2, 3, 4, 5};
   private final int INSET = 25, MAX_DELAY=25, DELAY_TIME=200, CONTROL = 55;

   private LessonsMagnetGame lesson;
   private ColorScheme       colors;
   private MagnetPanel       magnetPanel;

   private LessonPlayControls lessonControls;
   private LessonPopupMenu    popup;       // supplemental popup menu
   private JMenuItem[]        items;       // JMenuItems in the pop up menu
   
   private ArrayList<SentencePhrases> sentenceMagnets;

   /** Constructor to initialize the game */
   public MagnetPlayComponent(LessonsMagnetGame lesson)
   {  this.lesson = lesson;
      colors = lesson.getColorScheme();
      items = new JMenuItem[7];
      sentenceMagnets = new ArrayList<SentencePhrases>();

      setLayout(new BorderLayout());
      setBackground(new Color(80, 80, 80));
      magnetPanel = new MagnetPanel(lesson.getDisplaySize());
      add(magnetPanel, BorderLayout.CENTER);

      lessonControls = getPictureControls();
      add(lessonControls, BorderLayout.SOUTH);
      resetGame();
   }

   /** Method to reset the game
    *    It creates a new set of magnets and disburses them on the display
    */
   private void resetGame()
   {
      magnetPanel.removeAll();
      sentenceMagnets.clear();
      ArrayList<Phrase> magnets = new ArrayList<Phrase>();
      Vector<SentenceAudioPictureData> sentences = lesson.getSentenceData();

      int which, index;
      ArrayList<Integer> free = new ArrayList<Integer>();
      ArrayList<Phrase>  magnetList;
      SentencePhrases sentence;
      
      ArrayList<SentenceAudioPictureData> choices 
      		= new ArrayList<SentenceAudioPictureData>();
       
      int choice, phrase;
      for (choice=0; choice<sentences.size(); choice++)   
      { 
          choices.add(sentences.get(choice));
          sentence 
             = new SentencePhrases(lesson, choices.get(choice), colors);

          for (phrase=0; phrase<sentence.size(); phrase++)
 		  {
 		     free.add(choice*1000 + phrase);
 		  }
      }

      Dimension panelSize = magnetPanel.getSize();
      if (panelSize.height==0 || panelSize.width==0)
      {  
    	 panelSize = lesson.getDisplaySize();
         panelSize.height -= 50;
         setSize(panelSize);
      }
      
      int difficultyLevel = Score.getDifficultyLevel() - 1;
      int count = 0;
      
      while (free.size()>0)
      {  
    	 index = (int)(Math.random() * free.size());
         which = free.get(index);
         free.remove(index);
         
         choice = which/1000;
         phrase = which%1000;
         
         sentence 
           = new SentencePhrases(lesson, choices.get(choice), colors);
         magnetList = sentence.reset(phrase);
         
         if (magnetList.size()>0)
         {  count++;
            magnets.addAll(magnetList);
            sentenceMagnets.add(sentence);
         }
         if (count>=NUM_SENTENCES[difficultyLevel]) break;
      }

      Phrase magnet;
      int x, y;
      for (int i=0; i<magnets.size(); i++)
      {
         x = (int)(Math.random()*(panelSize.width - INSET));
         y = (int)(Math.random()*(panelSize.height - INSET - CONTROL));

         magnet = magnets.get(i);
         magnet.addMouseListener(this);
         magnet.addMouseMotionListener(this);
         magnetPanel.add(magnet);
         magnet.setLocation(x, y);
         repaint(); 
      }
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

      // Create menu items to adjust font sizes.
      items[4] = null;
      items[5] = new JMenuItem(adjustFontSizeTip(true));
      items[5].addActionListener(this);

      items[6] = new JMenuItem(adjustFontSizeTip(false));
      items[6].addActionListener(this);

      // Create Popup menu
      popup = new LessonPopupMenu(lesson, this, items);

      LessonPlayControls controls
              = new LessonPlayControls(lesson, popup, "MagnetGamePlay");

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

   // Get the font adjustment text for either tool tips or a menu item
   private String adjustFontSizeTip(boolean flag)
   {
       int fontSize = colors.getSize();
       String[] fontText = LanguageText.getMessageList("commonHelpSets", 31);
       if (flag)
       {   if (fontSize >= ColorScheme.MAX_FONT_SIZE)
                return fontText[0];
           else return fontText[2] + " " + fontSize;
       }
       else
       {    if (fontSize <= ColorScheme.MIN_FONT_SIZE)
                return fontText[1];
            else return fontText[3] + " " + fontSize;
       }
   }        // End of AdjustFontSizeTip()

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

      // Check if we are to adjust font sizes
      int fontSize = colors.getSize();
      if (source == items[5])
      {
         int newSize = fontSize * 120 / 100;
         if (newSize > ColorScheme.MAX_FONT_SIZE)
             newSize = ColorScheme.MAX_FONT_SIZE;

         if (fontSize < ColorScheme.MAX_FONT_SIZE)
         {  colors.setSize( newSize );
            lesson.setDirty(true);
            items[5].setText(adjustFontSizeTip(true));
            items[6].setText(adjustFontSizeTip(false));
            resetGame();
            return;
         }
      }
      if (source == items[6])
      {
         int newSize = fontSize * 100 / 120;
         if (newSize < ColorScheme.MIN_FONT_SIZE)
             newSize = ColorScheme.MIN_FONT_SIZE;

         if (fontSize > ColorScheme.MIN_FONT_SIZE)
         {  colors.setSize( newSize);
            lesson.setDirty(true);
            items[5].setText(adjustFontSizeTip(true));
            items[6].setText(adjustFontSizeTip(false));
            resetGame();
            return;
         }
      }      // End of checks for various JMenu items

      Toolkit.getDefaultToolkit().beep();
      return;
   }      // End of actionPerformed()

   /** Listener methods to handle moving magnets about the display */
   Phrase dragMagnet;
   public void mousePressed(MouseEvent event)  {}
   public void mouseReleased(MouseEvent event)
   {   if (dragMagnet!=null)
       {  moveMagnet();
          handleIntersection(dragMagnet);
       }
       dragMagnet = null;
   }
   public void mouseClicked(MouseEvent event) 
   {Point point = magnetPanel.getMousePosition();
       Frame frame = (Frame) SwingUtilities.getAncestorOfClass
                                    (Frame.class, this);
       Object object = event.getSource();
       if (object instanceof Phrase)
       {
          Phrase magnet = (Phrase)object;
          SentencePhrases sentenceObject;
          for (int i=0; i<sentenceMagnets.size(); i++)
          {  sentenceObject = sentenceMagnets.get(i);

             if (sentenceObject.canDisplay(magnet))
             {
                String sentence = sentenceObject.getSentence();
                PictureData picture = sentenceObject.getPicture();
                SoundData sound = sentenceObject.getAudio();
                String[] text = sound.getSoundText().clone(); 
                text[1] = lesson.getPhrasesForDisplay(text[1], true, true, true);
                SoundData phraseAudio = sentenceObject.getPhraseAudio();
                new DictionaryDialog(frame, sentence, picture, phraseAudio, sound, text
                                    , point, lesson, lesson.getColorScheme());
             }
          }
       }
   }
   public void mouseEntered(MouseEvent event) {}
   public void mouseExited(MouseEvent event)  {}
   public void mouseMoved(MouseEvent event)   {}
   public void mouseDragged(MouseEvent event) 
   {  Object object = event.getSource();
      if (object instanceof Phrase)
      {  if (dragMagnet == null) { dragMagnet = (Phrase)object; }
         moveMagnet();
      }
   }

   /** Method to determine if a point is within the panel
    *
    * @param point The point of the object
    * @return true if yes, false if no
    */
   private boolean isWithinPanel(Point point)
   {  if (point==null) return false;

      Dimension size = magnetPanel.getSize();
      if (point.x > size.width - INSET) return false;
      if (point.y > size.height - INSET) return false;
      return true;
   }

   /** Method to move a magnet to a new position */
   private void moveMagnet()
   {  if (dragMagnet!=null)
      {  Point point = magnetPanel.getMousePosition();
         if (isWithinPanel(point)) { dragMagnet.setLocation(point); }
      } 
   }

   /** Method to handle drops of a magnet on another
    *
    * @param dropMagnet The magnet being dropped
    */
   private void handleIntersection(Phrase dropMagnet)
   {
      int count = magnetPanel.getComponentCount();
      Phrase sourceMagnet;
      Rectangle dropBounds = dropMagnet.getBounds(), sourceBounds;

      SentencePhrases sentence = null;
      int sourceCenter, dropCenter;
      boolean left;
      for (int i=0; i<count; i++)
      {  sourceMagnet = (Phrase)magnetPanel.getComponent(i);
         if (sourceMagnet != dropMagnet)
         {  sourceBounds = sourceMagnet.getBounds();
            if (dropBounds.intersects(sourceBounds))
            { 
            	boolean circumfix = sourceMagnet.getText().contains("...")
            			|| dropMagnet.getText().contains("...");
            			
           		sourceCenter = sourceBounds.x + sourceBounds.width/2;
           		dropCenter = dropBounds.x + dropBounds.width/2;
           		left = sourceCenter <= dropCenter;
            	
            	if (left || circumfix) 
            		sentence = join(sourceMagnet, dropMagnet);
                if ((!left || circumfix) && sentence==null) sentence = join(dropMagnet, sourceMagnet);
                if (sentence!=null)
                {   
                   if (sentence.isComplete())
                	  if (!sourceMagnet.isPlus())
                		  outputFeedbackMessage(AcornsProperties.CORRECT);
                   break;
                }
                else outputFeedbackMessage(AcornsProperties.INCORRECT);
            }
         }     // End if components net same
      }        // End for
   }           // End handleIntersection

   /** Method to play the feedback message
    *
    * @param type AcornsProperties.CORRECT or AcornsProperties.INCORRECT
    */
   public void outputFeedbackMessage(int type)
   {  
	  SoundData feedback = lesson.getSound(type);
      int count = 0;
      feedback.playBack(null, 0, -1);
      try {  while(feedback!=null && feedback.isActive())
             {  if (count++==MAX_DELAY) feedback.stopSound();
                Thread.sleep(DELAY_TIME);
             }
          }  catch (Exception e) {}
   }

   /** Method that attempts to join two magnets together
    *
    * @param source The stationary magnet
    * @param drop The dragged magnet to combine with the stationary one
    * @return SentenceMagnets object or null if not joined
    */
   private SentencePhrases join(Phrase source, Phrase drop)
   {  Phrase joinedMagnet;
      SentencePhrases sentence;
      for (int i=0; i<sentenceMagnets.size(); i++)
      {  
    	 sentence = sentenceMagnets.get(i);
         joinedMagnet = sentence.join(source, drop);
         if (joinedMagnet!=null)
         {  Point point = source.getLocation();
            magnetPanel.remove(source);
            magnetPanel.remove(drop);
            joinedMagnet.addMouseListener(this);
            joinedMagnet.addMouseMotionListener(this);

            magnetPanel.add(joinedMagnet);
            joinedMagnet.setLocation(point);
            magnetPanel.repaint();
            return sentence;
         }
      }
      return null;
   }  // End of join()

   /** Class to hold the magnets */
   class MagnetPanel extends JLabel
   {
      private final static long serialVersionUID = 1;
      
      public MagnetPanel(Dimension size)
      {  super();
         setLayout(null);

         setSize(size);
         setPreferredSize(size);
         size.height += 50;
         setBackground(colors.getColor(true));
         PictureData picture = colors.getPicture();
         if (picture!=null)
         {  ImageIcon icon = picture.getIcon(size);
            setIcon(icon);
            this.setHorizontalAlignment(SwingConstants.CENTER);
         }
      }
   }       // End of MagnetPanel class
}     // End of MagnetPlayComponent
