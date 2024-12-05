/*
 * LessonsPictures.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package LessonsPictures;

import java.util.*;
import java.awt.*;
import java.io.*;

public class LessonsPictures extends Tools.Lesson implements Serializable
{
   private Hashtable<Point,Tools.PicturesSoundData> sounds;
   private int scaleFactor;
   private int angle;
   private byte[] bytes;

   public transient static final long serialVersionUID = 1;
   
   public LessonsPictures()
   {
       super("Picture and Sounds");
   }
   
   /********* Method to convert between versions  *************/
   public org.acorns.lesson.Lesson convert(float version) throws IOException
   {
       org.acorns.lesson.picturesV4.LessonsPictures newLesson
               = new org.acorns.lesson.picturesV4.LessonsPictures(scaleFactor, angle, bytes);
       
	      Hashtable<Point, org.acorns.data.PicturesSoundData> newSounds = newLesson.getSoundData();
       Enumeration<Point> keys = sounds.keys();
       org.acorns.data.PicturesSoundData newData;
       Point key;
       while (keys.hasMoreElements())
       {
          key = (Point)keys.nextElement();
          newData = sounds.get(key).convert(version); 
          newSounds.put(key, newData);
       }       
       return newLesson; 
   }
}     // End of LessonsPictures class.
