/*
 * LessonsFillBlanks.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package LessonsFillBlanks;

import java.io.*;
import Tools.*;

public class LessonsFillBlanks extends Lesson implements Serializable
{  
   private AnnotationData annotationData;
   private String link = "";
   private ColorScheme colorScheme;   
   
   private static final long serialVersionUID = 1;

   public LessonsFillBlanks(String name) {  super(name); }
   
   /********* Method to convert between versions, nothing to do for now  *************/
   public org.acorns.lesson.Lesson convert(float version)
   { 
       org.acorns.lesson.fillBlanksV4.LessonsFillBlanks newLesson 
               = new org.acorns.lesson.fillBlanksV4.LessonsFillBlanks
               (annotationData.convert(version), link, colorScheme.convert(version));
       return newLesson; 
   }
}     // End of LessonsFillBlanks class.
