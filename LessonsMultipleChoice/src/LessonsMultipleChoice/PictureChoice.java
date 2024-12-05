/**
 * PictureChoice.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package LessonsMultipleChoice;

import java.io.*;
import java.awt.*;
import Tools.*;

/** Lesson class that contains all information about a single picture */
public class PictureChoice implements Serializable
{
   public final static long serialVersionUID = 1;
 
   // The following data saves to and loads from disk.
   private byte[] bytes;
   private Dimension buttonSize;
   private PicturesSoundData questions;
   private int type;
   private int angle;

   /* convert to version 6.0 */
   public org.acorns.data.PictureChoice convert(float version) throws IOException
   {
       org.acorns.data.PicturesSoundData newQuestions
                                 = questions.convert(version);
       org.acorns.data.PictureChoice newChoice 
               = new org.acorns.data.PictureChoice
                      (bytes, buttonSize, newQuestions, type, angle);
       return newChoice;
   }
 
}  // End PictureChoice