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
package org.acorns.lesson.magnetGameV7;

import java.io.*;
import javax.swing.*;
import java.util.*;
import org.acorns.language.*;

import org.acorns.data.*;
import org.acorns.visual.*;

import org.acorns.lesson.categories.relatedphrases.*;

public class LessonsMagnetGame
        extends CategoryRelatedPhrases implements Serializable
{
   private static final long serialVersionUID = 1;
    
   /** Constructor for creating new lessons */
   public LessonsMagnetGame()
   { 
       super("Magnet Game;"
             + LanguageText.getMessage("LessonsMagnetGame",1)); 
   }
   
   /** Constructor for copying and pasting lessons */
   public LessonsMagnetGame(Object lessonObject)
   {  
       super(lessonObject, "Magnet Game;"
              + LanguageText.getMessage("LessonsMagnetGame",1));  
   }

   /** Constructor for converting from version 6.0 lessons to 7.0 */
   public LessonsMagnetGame(Vector<SentenceData>[] sentences, ColorScheme colors)
   {  
       super(sentences, colors, "Magnet Game;"
              + LanguageText.getMessage("LessonsMagnetGame",1));  
   }

   /** Polymorphic method to execute (play) this lesson
    *  @return the play panel
    */
   public JPanel play()
   {  if(!isPlayable()) return null;
	   return new MagnetPlayComponent(this);
   }


}  // End LessonsMagnetGame class
