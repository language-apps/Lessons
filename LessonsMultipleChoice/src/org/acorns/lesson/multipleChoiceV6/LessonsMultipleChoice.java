/**
 * LessonsMultipleChoice.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.lesson.multipleChoiceV6;

import java.io.*;
import javax.swing.*;
import org.acorns.language.*;

import org.acorns.lesson.categories.multiplepictures.*;

public class LessonsMultipleChoice
        extends CategoryMultiplePictures implements Serializable
{
   public static final long serialVersionUID = 1;
   public LessonsMultipleChoice()
   { super("Multiple Choice;"
             + LanguageText.getMessage("LessonsMultipleChoice",1)); }
   public LessonsMultipleChoice(Object lessonObject)
   {  super(lessonObject, "Multiple Choice;"
              + LanguageText.getMessage("LessonsMultipleChoice",1));  }

  /** Polymorphic method to execute Multiple Choice Lesson.
   *  @return the play panel
   */
   public JPanel play()
   {
      if(!isPlayable()) return null;

      resizeButtons();
      select(null); // No buttons should be selected
      return new PicturesPlayPanel(this);
   }  // End MakePanels2

}  // End MultipileChoice