/**
 * LessonsFlashCards.java
 *
 *   @author  HarveyD
 *   @version 5.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.lesson.flashCardsV6;

import javax.swing.*;

import java.io.*;

import org.acorns.lesson.categories.multiplepictures.*;
import org.acorns.language.*;

public class LessonsFlashCards 
        extends CategoryMultiplePictures implements Serializable
{
   public static final long serialVersionUID = 1;
   public static final String lessonName = "Flash Cards;";
   
   public LessonsFlashCards()
   {
      super(lessonName
              + LanguageText.getMessage("LessonsFlashCards",3));
   }  // End LessonFlashCards()

   public LessonsFlashCards(Object lessonObject)
   {  super(lessonObject, lessonName
                 + LanguageText.getMessage("LessonsFlashCards",3));
   }
   
  /** Polymorphic method to execute Flash Card Lesson.
   *  @return the play panel
   */
   public JPanel play()
   {
      if(!isPlayable()) return null;

      resizeButtons();
      select(null); // No buttons should be selected
      return new PicturesPlayPanel(this);
   }  // End MakePanels2

}  // End LessonsFlashCards