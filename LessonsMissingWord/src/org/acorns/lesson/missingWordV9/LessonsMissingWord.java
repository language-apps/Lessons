package org.acorns.lesson.missingWordV9;
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

import java.io.*;
import javax.swing.*;

import org.acorns.lesson.*;
import org.acorns.lesson.categories.relatedphrases.CategoryRelatedPhrases;
import org.acorns.language.*;

public class LessonsMissingWord
		extends CategoryRelatedPhrases implements Serializable
{
   private static final long serialVersionUID = 1;

   public LessonsMissingWord()
   { 
	   super("Missing Word;"
             + LanguageText.getMessage("LessonsMissingWord",1));
   }
   
   
   public LessonsMissingWord(Object lessonObject)
   {  
	   super(lessonObject,
              LanguageText.getMessage("LessonsMissingWord",1)); 
   
   }

   /** Method to convert between older ACORNS versions 
    *
    * @param version The current version number
    * @return The same lesson object converted
    */
   public Lesson convert(float version) 
   { 
	   org.acorns.lesson.missingWordV11.LessonsMissingWord missingWord;
	   missingWord = new org.acorns.lesson.missingWordV11.LessonsMissingWord(this);
       return missingWord;
   }


@Override
public JPanel play() {
	// TODO Auto-generated method stub
	return null;
}

}  // End LessonsMissingWords