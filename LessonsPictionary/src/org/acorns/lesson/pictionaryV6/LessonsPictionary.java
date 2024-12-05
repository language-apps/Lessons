/**
 * LessonsPictionary.java
 *
 *   @author  HarveyD
 *   @version 6.00
 *
 *   Copyright 2010, all rights reserved
*/
package org.acorns.lesson.pictionaryV6;

import javax.swing.*;
import java.io.*;
import org.acorns.language.*;

import org.acorns.lesson.categories.multiplepictures.*;

   public class LessonsPictionary 
        extends CategoryMultiplePictures implements Serializable
   {
      public static final long serialVersionUID = 1;
	   public static final String lessonName = "Pictionary;";
      
      /** Flag to indicate that description should be entered in audio dialog */
      public static final boolean description = true;
	
      public LessonsPictionary()
      {
         super(lessonName
                 + LanguageText.getMessage("LessonsPictionary",1));
      }  // End LessonMovingPictures()

      public LessonsPictionary(Object lessonObject)
      {  super(lessonObject, lessonName
                 + LanguageText.getMessage("LessonsPictionary",1));
      }
   
	  /** Polymorphic method to execute MovingPictures Lesson.
 	      *  @return the play panel
	      */
	   public JPanel play()
	   {
	      if(!isPlayable()) return null;

	      resizeButtons();
	      select(null); // No buttons should be selected
	      return new PicturesPlayPanel(this);
	   }  // End MakePanels2	
}  // End LessonsPictionary