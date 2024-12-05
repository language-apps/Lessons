/**
 *   @author  HarveyD
 *   Dan Harvey - Professor of Computer Science
 *   Southern Oregon University, 1250 Siskiyou Blvd., Ashland, OR 97520-5028
 *   harveyd@sou.edu
 *   @version 1.00
 *
 *   Copyright 2010, all rights reserved
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * To receive a copy of the GNU Lesser General Public write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.acorns.lesson.questionAnswersV7;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

import org.acorns.audio.SoundDefaults;
import org.acorns.data.*;
import org.acorns.visual.*;
import org.acorns.lesson.*;
import org.acorns.language.*;
import org.acorns.widgets.*;

public class PhrasePanel  extends JPanel
{
    private final static long serialVersionUID = 1;
    
    private final static int ICON_SIZE = 25;
    private final static int GAP = 10;
    
    LessonsQuestionsAnswers lesson;
    JPanel panel;
    
     /** Method to display information about a sentence
      *
      * @param lesson The lesson in question
      * @param phrase The phrase with all of the possible answers
      * @param colors background and foreground colors
      */
   public PhrasePanel(LessonsQuestionsAnswers lesson, SentenceAudioPictureData phrase,
                                                        ColorScheme colorScheme)
   {
       super();
       
       this.panel = this;
       this.lesson = lesson;
       PictureData picture = phrase.getPicture();
       
       setLayout(new BorderLayout());
       add(new TopPanel(lesson, phrase), BorderLayout.NORTH);
       setBackground(colorScheme.getColor(true));

       if (picture!=null)
       {
          JPanel centerPanel = new CenterPanel(picture, colorScheme);
          add(centerPanel, BorderLayout.CENTER);
       }
       add(new BottomPanel(lesson, phrase), BorderLayout.SOUTH);
  }
   

   class ToolTipButton extends JButton
   {
	   private final static long serialVersionUID = 1;
       
       public ToolTipButton(Lesson lesson, int iconNo, String toolTip)
       {
       	   ImageIcon icon = lesson.getIcon(iconNo, ICON_SIZE);
           setIcon(icon);
           setToolTipText(toolTip);
           setPreferredSize(new Dimension(ICON_SIZE+10, ICON_SIZE));
           setAlignmentY(TOP_ALIGNMENT);
       }
   }
   
   class TopPanel extends JPanel
   {
	   private final static long serialVersionUID = 1;

       SoundData questionAudio;
       Lesson qaLesson;
       
       public TopPanel(Lesson lesson, SentenceAudioPictureData data)
       {
           String question = data.getSentence();
           questionAudio = data.getSound();
           qaLesson = lesson;
           
           setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
           add(new JLabel(question));
           add(Box.createHorizontalGlue());
           
           String tip = LanguageText.getMessage("commonHelpSets", 76);
           JButton play = new ToolTipButton(lesson, AcornsProperties.PLAY, tip);
           play.addActionListener(new ActionListener()
                   {
                       public void actionPerformed(ActionEvent event)
                       {
                           questionAudio.stopSound();
                           if (!questionAudio.playBack(null,0,-1))
                           {
                               String msg 
                                = LanguageText.getMessage("commonHelpSets", 27);
                               qaLesson.setText(msg);
                           }
                       }
                   });
           add(play);
       }
   }

   class BottomPanel extends JPanel
   {
	   private final static long serialVersionUID = 1;

       LessonsQuestionsAnswers lesson;
       SentenceAudioPictureData data;
       SoundData correct, incorrect, close;
       JTextField answer;
       RecordPanel recorder;
       
       public BottomPanel
                 (Lesson qAns, SentenceAudioPictureData d)
       {
           lesson = (LessonsQuestionsAnswers)qAns;
           data = d;
           
           correct = lesson.getSound(AcornsProperties.CORRECT);
           incorrect = lesson.getSound(AcornsProperties.INCORRECT);
           close = lesson.getSound(AcornsProperties.SPELL);
           
           PicturesSoundData pictureSound = data.getAudio();
           Vector<SoundData> audios = pictureSound.getVector();
           Font languageFont = new Font("Times New Roman", Font.PLAIN, 12);
           ColorScheme scheme = lesson.getColorScheme();

           if (!audios.isEmpty())
           {
        	   String language = audios.get(0).getSoundText()[SoundData.LANGUAGE];
        	   Font font = KeyboardFonts.getLanguageFonts().getFont(language);
       	       if (font != null) 
       	       {
           		   int size = scheme.getSize();
       	    	   languageFont = font.deriveFont(Font.PLAIN, size);  
       	       }
           }
           
           setLayout(new BorderLayout());
           
           answer = new JTextField();
           answer.setFont(languageFont);
           String tip = LanguageText.getMessage("LessonsQuestionsAnswers", 9);
           answer.setToolTipText(tip);
           add(answer, BorderLayout.NORTH);
           
           JPanel panel = new JPanel();
           panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
           
           tip = LanguageText.getMessage("commonHelpSets", 82);
           JButton answerButton = new ToolTipButton
                                       (lesson, AcornsProperties.ANSWERS, tip);
           answerButton.addActionListener(new ActionListener()
                   {
                       public void actionPerformed(ActionEvent event)
                       {
                          String title = LanguageText.getMessage(lesson, 1);
                          lesson.feedback(panel, LessonsQuestionsAnswers.SHOW, title, data, null);
                       }
                   });
           panel.add(answerButton);

           panel.add(Box.createHorizontalGlue());
           
           recorder = new RecordPanel(new SoundData(), ICON_SIZE, GAP );
           recorder.setAlignmentY(TOP_ALIGNMENT);
           panel.add(recorder);
           panel.add(Box.createHorizontalGlue());
           
           tip = LanguageText.getMessage("commonHelpSets", 83);
           
           JButton checkButton 
                   = new ToolTipButton(lesson, AcornsProperties.CHECK, tip);
           checkButton.addActionListener(new ActionListener()
           {
               public void actionPerformed(ActionEvent event)
               {
            	   double similarity = compareAudios(data);
            	   System.out.printf("Similarity = %.4f\n", similarity);
                   if (isCorrect(similarity, data, answer.getText()))
                   {
                       correct.playBack(null, 0, -1);
                       Score.nextScore(true);
                   }
                   else if (isClose(similarity, data, answer.getText()))
                   {
                       close.playBack(null, 0, -1);
                       Score.nextScore(true);
                   }
                   else
                   {
                       incorrect.playBack(null, 0, -1);
                       Score.nextScore(false);
                   }
               }
           });
           panel.add(checkButton);
           add(panel, BorderLayout.SOUTH);
       }

       /** Find maximum similarity to a correct answer
        * 
        * @param data Object containing valid answers
        * @return Best similarity measure
        */
       
       private double compareAudios(SentenceAudioPictureData data)
       {
          PicturesSoundData pictureSound = data.getAudio();
          Vector<SoundData> audios = pictureSound.getVector();
          double maxSimilarity = 0.0, similarity;
          for (SoundData audio: audios)
          {
        	  similarity = recorder.compare(null, audio);
        	  if (similarity > maxSimilarity)
        		  maxSimilarity = similarity;
          }
    	   return maxSimilarity;
       }
   }
   
   
  /** Determine if answer was correct
   *       All words must be spelled correctly and be in the right order
   *       The comparisons are case sensitive and extra spaces are ignored  
   *  
   * @param data the object containing all possible answers 
   * @return true if matches one of the possible answers, false otherwise
   */
   private boolean isCorrect(double similarity, SentenceAudioPictureData data, String ans)
   {   
	  if (similarity >= SoundDefaults.getDTWCorrectness(SoundDefaults.CORRECT))
		  	return true;
	  
      ans.replaceAll("\\s+", " ").toLowerCase().trim();
      
      PicturesSoundData pictureSound = data.getAudio();
      Vector<SoundData> audios = pictureSound.getVector();
      
      String key;
      for (SoundData temp: audios)
      {
          key = temp.getSoundText(SoundData.NATIVE);
          key = lesson.getPhrasesForDisplay(key, true, true, true);
          key.replaceAll("\\s+", " ").toLowerCase().trim();
          if (key.equals(ans)) return true;
      }
      return false;
   }
   
   /** Determine if answer misspells or is close to one of the possible answers
       *    The answer can be a rearrangement of words or one or more of the
       *    words can be misspelled
       * 
       *    The comparisons are case sensitive and extra spaces are ignored  
       *  
       * @param data the object containing all possible answers 
       * @return true if close but not exact match to a possible answer
       */
   private boolean isClose(double similarity, SentenceAudioPictureData data, String ans)
   {   
		  if (similarity >= SoundDefaults.getDTWCorrectness(SoundDefaults.CLOSE))
			  	return true;

		  ans.replaceAll("\\s+", " ").toLowerCase().trim();
          String delims = "[ .,?;!]+";
          String[] words = ans.split(delims);
          
          PicturesSoundData pictureSound = data.getAudio();
          Vector<SoundData> audios = pictureSound.getVector();
          
          String key, word;
          ArrayList<String> keys;
          int index, distance, maxDistance; 
          boolean found;
          
          for (SoundData temp: audios)
          {
              key = temp.getSoundText(SoundData.NATIVE);
              key = lesson.getPhrasesForDisplay(key, true, true, true);
              key.replaceAll("\\s+", " ").toLowerCase().trim();
              keys = new ArrayList<String>(Arrays.asList(key.split(delims)));
              if (keys.size()!=words.length) continue;
              
              for (int i=0; i<words.length; i++)
              {
                  found = false;
                  word = words[i];
                  index = keys.indexOf(word);
                  if (index>=0) { keys.remove(index); found = true; }
                  else
                  {
                      for (int k=0; k<keys.size(); k++)
                      {
                          distance = SpellCheck.editDistance(keys.get(k), word);
                          maxDistance = 1;
                          if (word.length()>=5) maxDistance = 2;
                          if (distance<=maxDistance) 
                          {
                              keys.remove(k);
                              found = true;
                              break;
                          }
                      }
                  }
                  if (!found) break;  // This answer is not a match
              }
              
              if (keys.isEmpty()) return true;  // All of the words matched
          }   // End for each loop
          
          return false;  // Failed to find a match
   }
   
   class CenterPanel extends JPanel
   {
     	 private final static long serialVersionUID = 1;

     	 ColorScheme colors;
     	 PictureData picture;
     	 
     	 public CenterPanel(PictureData picture, ColorScheme colors)
     	 {
     		 this.colors = colors;
     		 this.picture = picture;
         	 setBackground(colors.getColor(true));
     	 }
     	  
         public @Override void paintComponent(Graphics graphics)
          {
             super.paintComponent(graphics);
             
             Dimension size = getSize();
             
             if (picture !=null)
             {                      
                 Dimension iSize = picture.getSize();
                 double scaleH = 1.0 * size.height / iSize.height;
                 double scaleW = 1.0 * size.width / iSize.width;
                 double scale = Math.min(scaleH, scaleW);
                 iSize.height *= scale;
                 iSize.width *= scale;

                int x = (size.width - iSize.width)/2;
                int y = (size.height - iSize.height)/2;
               BufferedImage image = picture.getImage
                      (this, new Rectangle(x, y, iSize.width, iSize.height));
                graphics.drawImage(image,x,y, iSize.width, iSize.height, null);
             }
         }
       };
  
}
