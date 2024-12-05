package org.acorns.lesson.missingWordV11;
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


import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.util.*;

import org.acorns.language.*;
import org.acorns.lesson.*;
import org.acorns.data.*;
import org.acorns.lesson.categories.relatedphrases.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LessonsMissingWord
        extends CategoryRelatedPhrases implements Serializable
{
   private static final long serialVersionUID = 1;
 
   // The special characters used for the character buttons
   private String[] specials;
   
   private int category;  // Active category
   

   public LessonsMissingWord()
   { 
	   super("Missing Word;"
             + LanguageText.getMessage("LessonsMissingWord",1));
	   
	   specials = new String[AcornsProperties.MAX_LAYERS];
	   for (int i=0; i<AcornsProperties.MAX_LAYERS; i++)
		      specials[i] = "";
	   
      String language = KeyboardFonts.getLanguageFonts().getLanguage();
	  String spe = KeyboardFonts.getLanguageFonts().getSpecials(language);
	  setSpecials(spe);
   }
   
   public LessonsMissingWord(Object lessonObject)
   {  
	   super(lessonObject, "Missing Word;"
              + LanguageText.getMessage("LessonsMissingWord",1));

	   if (specials==null)
	   {
		   specials = new String[AcornsProperties.MAX_LAYERS];
		   for (int i=0; i<AcornsProperties.MAX_LAYERS; i++)
			      specials[i] = "";
		   
	      String language = KeyboardFonts.getLanguageFonts().getLanguage();
		  String spe = KeyboardFonts.getLanguageFonts().getSpecials(language);
		  setSpecials(spe);
	   }
   }
   
   public String getSpecials()
   {
	   return specials[getLayer()-1];
   }

   /* used by CategoryRelatedPhrases class */
   public String getSpecials(int layer)
   {
	   if (layer<0 || layer >= specials.length)
		   return "";
	   
	   return specials[layer - 1];
   }
   
   public void setSpecials(String specials)
   {
	   this.specials[getLayer()-1] = specials;
   }

   /** Polymorphic method to execute (play) this lesson
    *  @return the play panel
    */
   public JPanel play()
   {  
	  if(!isPlayable()) return null;
	   return new MissingWordPlayPanel(this);
   }
   
   /** Determine if the lesson is playable
   *     There must be at least one magnet found
   *
   * @param layer The layer in question
   * @return error message if no, or null if yes
   */
   public @Override String isPlayable(int layer)
   {  
		  Vector<SentenceAudioPictureData> sentenceData = getSentenceData(layer);
	      int size = sentenceData.size();
	      if (size==0)
	    	  return LanguageText.getMessage("commonHelpSets", 80);
	      
	      for (int row=0; row<size; row++)
	      {  
	    	  if (!isRecordingComplete(row))
	           return LanguageText.getMessage("commonHelpSets", 84);
	      }
      
	      return null;
   }

   @Override
   public boolean print(Document document, Element lessonNode
           , File directory, String[] extensions)
   {
	   return super.print(this, document, lessonNode, directory, extensions);
   }

    @Override
	public void importData
    (int myLayer, Point point, URL file, String[] data, int type)
                        throws IOException, UnsupportedAudioFileException
    {
    	super.importData(myLayer, point, file, data, type);

    	// Set special characteers if this is layer data.
    	if (type == AcornsProperties.LAYER)
    	{
    		if (data.length >= 4)
    		{   
			  String language = data[0];
		      String specials = data[3];
		      if (language != null && specials != null && language.length()>0)
		      {
		    	  KeyboardFonts.getLanguageFonts().setSpecials(language, specials);
		    	  this.specials[myLayer-1] = data[3];
		      }
    		}
    	}
    }

   
   /** Reset category */
   public void resetCategory()
   {
	   category = 0;
   }
   
   /** Determine if we are at the last category */
   public boolean hasMoreCategories()
   {
	   Vector<SentenceAudioPictureData> categories = getSentenceData();
	   return category<categories.size()-1;
   }
   
   /** Move to next category */
   public SentenceAudioPictureData getNextCategory()
   {
	   category++;
	   return getCurrentCategory();
   }

   /** Get the current category */
   public SentenceAudioPictureData getCurrentCategory()
   {
	   Vector<SentenceAudioPictureData> categories = getSentenceData();
	   if (category<0 || category>=categories.size()) category = 0;

	   if (categories.size()==0) return null;
	   return categories.get(category);
   }
   
   /** Get the description that goes with the category */
   public String getCategoryDescription()
   {
	   SentenceAudioPictureData category = getCurrentCategory();
	   return category.getSentence();
   }
   
   /** Get the audio describing the category */
   public SoundData getCategoryAudio()
   {
	   SentenceAudioPictureData category = getCurrentCategory();
	   return category.getSound();
   }
   
   /** Get the picture that goes with the category */
   public PictureData getCategoryPicture()
   {
	   SentenceAudioPictureData category = getCurrentCategory();
	   return category.getPicture();
   }
   
   /** Get the vector of sentences that goes with the category */
   public Vector<SoundData> getCategorySentenceVector()
   {
	   SentenceAudioPictureData category = getCurrentCategory();
	   return category.getAudio().getVector();
   }
   
   @Override
   public JPanel[] getLessonData()
   {
	   JPanel[] lessonData = super.getLessonData();
	   adjustLessonData(lessonData[AcornsProperties.CONTROLS]);
	   return lessonData;
   }
 
   /** Method to add a button and its action listener to the setup panel
    * 
    */
   private void adjustLessonData(JPanel controlPanel)
   {
	   Lesson lesson = this;
 	   String language = KeyboardFonts.getLanguageFonts().getLanguage();
 	   ImageIcon icon = lesson.getIcon(AcornsProperties.STAR, Lesson.ICON);
	   JButton button = new JButton(icon);
	   String tip = LanguageText.getMessage(this, 17);
	   button.setToolTipText(tip);
       button.setBorder(BorderFactory.createRaisedBevelBorder());
       button.setSize(new Dimension(Lesson.ICON+15, Lesson.ICON+5));
       button.setPreferredSize(button.getSize());
       button.setMinimumSize(button.getSize());
       button.setMaximumSize(button.getSize());

	   Font font 
	      = new Font("Times New Roman", 
	    		  Font.BOLD, 30);
	   button.setFont(font);
       button.setBorder(BorderFactory.createRaisedBevelBorder());
	   button.setPreferredSize(new Dimension(Lesson.ICON, Lesson.ICON));
	   button.addActionListener(
           new ActionListener()
           {
              public void actionPerformed(ActionEvent event)
              {
            	  JTextField field = new JTextField(getSpecials(), 50);
            	  KeyboardFonts.getLanguageFonts().setFont(language, field);
               	  String title = LanguageText.getMessage(lesson, 17);
            	  int okCxl = JOptionPane.showConfirmDialog(controlPanel,
                      field, title, JOptionPane.OK_CANCEL_OPTION);

            	  if (okCxl == JOptionPane.OK_OPTION) 
            	  {
            		  setSpecials(field.getText());
            		  KeyboardFonts.getLanguageFonts().setSpecials(language, getSpecials());
            	  }
              }  // End of actionPerformed.
           }     // End of anonymous ActionListener.
       );           // End of addActionListener.

	   // Insert prior to the last component.
	   int count = controlPanel.getComponentCount();
       controlPanel.add(Box.createHorizontalStrut(2), count-2);
	   count = controlPanel.getComponentCount();
	   controlPanel.add(button, count-2);
   }
   
   

}  // End MultipileChoice
