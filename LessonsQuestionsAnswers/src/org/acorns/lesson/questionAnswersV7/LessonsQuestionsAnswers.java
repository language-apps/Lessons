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
package org.acorns.lesson.questionAnswersV7;

import java.io.*;
import javax.swing.*;
import java.util.*;

import org.acorns.language.*;
import org.acorns.data.*;

import org.acorns.lesson.categories.relatedphrases.*;

public class LessonsQuestionsAnswers
        extends CategoryRelatedPhrases implements Serializable
{
   private static final long serialVersionUID = 1;
   public LessonsQuestionsAnswers()
   { super("Questions and Answers;"
             + LanguageText.getMessage("LessonsQuestionsAnswers",1)); }
   
   public LessonsQuestionsAnswers(Object lessonObject)
   {  super(lessonObject, "Questions and Answers;"
              + LanguageText.getMessage("LessonsQuestionsAnswers",1));  }

   /** Polymorphic method to execute (play) this lesson
    *  @return the play panel
    */
   public JPanel play()
   {  if(!isPlayable()) return null;
	   return new QuestionsAnswersPlayPanel(this);
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
	    	  if (!(sentenceData.get(row).getSound().isRecorded())) 
	    		  return LanguageText.getMessage("commonHelpSets", 93);;

	    	  if (!isRecordingComplete(row))
	           return LanguageText.getMessage("commonHelpSets", 84);
	      }
	      
	      return null;
   }




}  // End MultipileChoice
