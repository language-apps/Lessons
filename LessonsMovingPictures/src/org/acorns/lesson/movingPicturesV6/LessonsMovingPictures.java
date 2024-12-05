/**
 * LessonsMovingPictures.java
 *
 *   @author  HarveyD
 *   @version 5.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.lesson.movingPicturesV6;

import javax.swing.*;
import java.io.*;
import org.acorns.language.*;

import org.acorns.lesson.categories.multiplepictures.*;

   public class LessonsMovingPictures 
        extends CategoryMultiplePictures implements Serializable
   {
      public static final long serialVersionUID = 1;
	  public static final String lessonName = "Moving Pictures;";
	  
      /** Flag to indicate that description should be entered in audio dialog */
      public static final boolean description = true;
	
      public LessonsMovingPictures()
      {
         super(lessonName
                 + LanguageText.getMessage("LessonsMovingPictures",1));
      }  // End LessonMovingPictures()

      public LessonsMovingPictures(Object lessonObject)
      {  super(lessonObject, lessonName
                 + LanguageText.getMessage("LessonsMovingPictures",1));  }
   
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
}  // End LessonsMovingPictures