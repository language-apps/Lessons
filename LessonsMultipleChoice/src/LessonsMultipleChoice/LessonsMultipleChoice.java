/**
 * LessonsMultipleChoice.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package LessonsMultipleChoice;

import java.io.*;

public class LessonsMultipleChoice extends Tools.Lesson implements Serializable
{
   public static final long serialVersionUID = 1;
   
   private PictureChoice[] choices;
   private String link = "";
   private int count;

   private static float version;
   
   public LessonsMultipleChoice()
   {
      super("Multiple Choice");
   }  // End MultipleChoice()

      
  /********* Method to convert between versions  *************/
   public org.acorns.lesson.Lesson convert(float ver) throws IOException
   {
       LessonsMultipleChoice.version = ver;

       return 
          new org.acorns.lesson.multipleChoiceV6.LessonsMultipleChoice(this);
   }

    /** Methods for conversion to latest format */
   public org.acorns.data.PictureChoice[] getPictureData() throws IOException
   {
       org.acorns.data.PictureChoice[] newChoices
               = new org.acorns.data.PictureChoice[choices.length];

       for (int c=0; c<choices.length; c++)
       {
           if (choices[c]!=null) newChoices[c] = choices[c].convert(version);
       }
	      return newChoices;
   }
   public Integer getChoices() { return count; }

   public String getMyLink()
   { return link; }
   
}  // End MultipileChoice