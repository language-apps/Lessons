/*
 * Magnet.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */

package org.acorns.lesson.translateV9;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.*;

import org.acorns.language.*;
import org.acorns.visual.*;

/** Class to create magnets containing native words or partial sentences */
public class Phrase extends JButton
{
private final static long serialVersionUID = 1;

   private final Color BEVEL_COLOR = Color.LIGHT_GRAY;
   private final Color BEVEL_SHADOW = Color.DARK_GRAY;
   private final int GAP = 20;

   private SentencePhrases sentencePhrases;
   private String controlString;
   private ArrayList<Phrase> phrases;
   private Point range;

   /** Constructor to create a phrase component
   *
   * @param sentencePhrases The object controlling the magnets
   * @param controlString The word or partial sentence to be in this lesson category
   * @param scheme Background/Foreground color and font size
   * @param language The native language
   * @param phrases Array list of embedded magnets
   * @param range range of phrase indices that this phrase represents (eliminate search)
   */
   public Phrase(SentencePhrases sentencePhrases, String controlString, ColorScheme scheme, String language, ArrayList<Phrase> phrases, Point range)
   {
	  this.sentencePhrases = sentencePhrases;
	  this.controlString = controlString;
	  this.phrases = phrases;
	  this.range = range;

	  setText(controlString);
      setHorizontalAlignment( SwingConstants.CENTER );
      setOpaque(true);
      setBorder(BorderFactory.createBevelBorder
                      (BevelBorder.RAISED, BEVEL_COLOR, BEVEL_SHADOW));
       
      setForeground(scheme.getColor(false));
      setBackground(new Color(80,80,80));
       
      int size = scheme.getSize();
      Font font = KeyboardFonts.getLanguageFonts().getFont(language);
      if (font == null) font = new Font(null, Font.PLAIN, size);
      else
      {  
    	   font = font.deriveFont(Font.PLAIN, size);  
      }
      this.setFont(font);

      int x = (int)Math.abs(range.x);
      int y = (int)Math.abs(range.y);
      
      String text = sentencePhrases.getPhrasesForDisplay(controlString, false, x==0, y==sentencePhrases.size()-1);
      updateMetrics(text);
      setText(text);
   }

   /** Update the control string 
    * 
    * @param controlString The control string with the morphological controls
    * @param finish Format for final display before morphological merging
    */
   public void setControlString(String controlString, boolean finish)
   {
	   this.controlString = controlString;
	   String text = sentencePhrases.getPhrasesForDisplay(controlString, finish, range.x==0, range.y==phrases.size()-1);
	   updateMetrics(text);
	   setText(text);
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
   
   public Point getRange()
   {
	   return range;
   }
   
   public void setRange(Point range)
   {
	   this.range = range;
   }
   
   public ArrayList<Phrase> getEmbeddedPhrases()
   {
	   return phrases;
   }
   
   public boolean isPlus()
   {
	   return controlString.startsWith("+");
   }

   /** Check if the called object has the same controlString pattern */
   public boolean isEqual(Phrase phrase)
   {
	   ArrayList<Phrase> phraseList = phrase.phrases;
	   int len = phrases.size();
	   
	   if (len != phrase.phrases.size()) return false;
	   for (int r=0; r<phrases.size(); r++)
	   {
		   String source = phrases.get(r).controlString;
		   String dest   = phraseList.get(r).controlString;
		   if (!source.equals(dest)) return false;
	   }
	   if (len==0 && !controlString.equals(phrase.controlString))
			return false;
			
		return true;
   }
   
  
}  // End of Magnet class
