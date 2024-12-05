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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.acorns.lesson.translateV9;

import java.io.*;
import javax.swing.*;
import java.util.*;

import org.acorns.language.*;
import org.acorns.data.*;
import org.acorns.lesson.categories.relatedphrases.*;

public class LessonsTranslate
        extends CategoryRelatedPhrases implements Serializable
{
   private static final long serialVersionUID = 1;
   
   private int category;  // Active category

   public LessonsTranslate()
   { 
	   super("Translate;"
             + LanguageText.getMessage("LessonsTranslate",1));
   }
   
   
   public LessonsTranslate(Object lessonObject)
   {  
	   super(lessonObject, "Translate;"
              + LanguageText.getMessage("LessonsTranslate",1));  
   }

   /** Polymorphic method to execute (play) this lesson
    *  @return the play panel
    */
   public JPanel play()
   {  
	  if(!isPlayable()) return null;
	   return new TranslatePlayPanel(this);
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
	    	  return LanguageText.getMessage("commonHelpSets", 95);
	      
	      for (int row=0; row<size; row++)
	      {  
	    	  if (!isRecordingComplete(row))
	           return LanguageText.getMessage("commonHelpSets", 84);
	      }
	      return null;
   }
   
   /** Determine if we are at the last category */
   public boolean hasMoreCategories()
   {
	   Vector<SentenceAudioPictureData> categories = getSentenceData();
	   return category<categories.size()-1;
   }
   
   /** Reset category */
   public void resetCategory()
   {
	   category = 0;
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

}  // End MultipileChoice
