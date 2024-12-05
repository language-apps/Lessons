/*
 * Magnet.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */

package org.acorns.lesson.magnetGameV7;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import org.acorns.language.*;
import org.acorns.lesson.categories.relatedphrases.CategoryRelatedPhrases;
import org.acorns.visual.*;

/** Class to create magnets containing native words or partial sentences */
public class Phrase extends JLabel
{
private final static long serialVersionUID = 1;

   private final static Color BEVEL_COLOR = Color.LIGHT_GRAY;
   private final static Color BEVEL_SHADOW = Color.DARK_GRAY;
   private final static int GAP = 20;
   
   private String  controlString;
   private boolean isPlus;

   /** Constructor to create a phrase component
    *
    * @param lesson the lesson object
    * @param word The word or partial sentence to be in this lesson category
    * @param colors The object containing colors and font size
    * @param language The native language
    */
   public Phrase(LessonsMagnetGame lesson, String word, ColorScheme colors, String language)
   {
	  isPlus = word.startsWith("+");
	  if (isPlus) 
		  word = word.substring(1);
	  
	  controlString = word;
	  String displayWord = lesson.getPhrasesForDisplay(word, false, false, false);
      configure(displayWord, colors, language);
   }
   
   /** Constructor to create a phrase component
   *
   * @param prefix The word or partial sentence to be in this lesson category
   * @param suffix The word or partial sentence to be in this lesson category
   * @param colors The object containing colors and font size
   * @param language The native language
   */
   public Phrase(LessonsMagnetGame lesson, String prefix, String suffix, ColorScheme colors, String language)
   {
	   String displayPrefix = lesson.formatControlString(prefix,  false);

	   String displaysuffix = lesson.formatControlString(suffix,  true);
	   controlString = displayPrefix + "..." + displaysuffix;

	   String displayWord = displayPrefix + "..." + displaysuffix;
	   displayWord = lesson.getPhrasesForDisplay(displayWord, false, false, false);
	   configure(displayWord, colors, language);
   }
    
  /** Configure the magnet label
   * @param word The word or partial sentence to be in this lesson category
   * @param colors The object containing colors and font size
   * @param language The native language
   */
   private void configure(String word, ColorScheme colors, String language)
   {
	   setText(word);
       setHorizontalAlignment( SwingConstants.CENTER );
       setOpaque(true);
       setBorder(BorderFactory.createBevelBorder
                      (BevelBorder.RAISED, BEVEL_COLOR, BEVEL_SHADOW));
       setForeground(colors.getColor(false));
       setBackground(colors.getColor(true));
       Font font = KeyboardFonts.getLanguageFonts().getFont(language);
       if (font == null) font = new Font(null, Font.PLAIN, colors.getSize());
       else
       {  
    	   font = font.deriveFont(Font.PLAIN, colors.getSize());  
       }
       setFont(font);

       setText(word);
       updateMetrics(word);
   }
   
   private void updateMetrics(String text)
   {
	  Font font = getFont();
      FontMetrics metrics = getFontMetrics(font);
      int width = metrics.stringWidth(controlString);
      int height = metrics.getHeight();
      setSize(new Dimension(width + GAP, height+GAP));
   }
      
   public String getControlString()
   {
	   return controlString;
   }
  
   @Override public void setText(String text) 
   {
 	  super.setText(text);
   }

   /** Determine if this is an extra word. */
   public boolean isPlus()
   {
	   return isPlus;
   }
   
   public  void setPlus() 
   {
	   isPlus = true;
   }
   
   public boolean isCircumfix()
   {
	   return controlString.contains("...");
   }

   /** Get string of words in this magnet */
   
   /** Update the control string 
    * 
    * @param controlString The control string with the morphological controls
    * @param finish Format for final display before morphological merging
    */
   public void setControlString(CategoryRelatedPhrases lesson, String controlString, boolean finish)
   {
	   this.controlString = controlString;
	   String text = lesson.getPhrasesForDisplay(controlString, finish, false, false);
	   updateMetrics(text);
	   setText(text);
   }


   /** Method to determine if this magnet is equal to another
    *
    * @param magnet The other magnet
    * @return true if yes, false if no
    */
   public boolean equals(Phrase magnet)
   {  
	   return magnet.getText().equals(getText()); 
   }

}  // End of Magnet class
