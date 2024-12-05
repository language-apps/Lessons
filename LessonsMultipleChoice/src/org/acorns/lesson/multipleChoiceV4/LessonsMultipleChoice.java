/**
 * LessonsMultipleChoice.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.lesson.multipleChoiceV4;

import java.awt.*;
import javax.swing.*;

import java.io.*;
import java.net.*;
import org.w3c.dom.*;

import javax.sound.sampled.*;
import org.acorns.lesson.*;
import org.acorns.data.*;

public class LessonsMultipleChoice extends Lesson implements Serializable
{
   public static final long serialVersionUID = 1;
    
   private PictureChoice[] choices;
   private int count;
   
   public LessonsMultipleChoice()
   {
      super("Multiple Choice");
   }  // End MultipleChoice()
   

    /********* Method to convert between versions  *************/
   public Lesson convert(float version)  
   { return 
       new org.acorns.lesson.multipleChoiceV6.LessonsMultipleChoice(this);
   }
   
   /** Methods for conversion to latest format */
   public org.acorns.data.PictureChoice[] getPictureData()
   {   
       org.acorns.data.PictureChoice[] newChoices
               = new org.acorns.data.PictureChoice[choices.length];
			   
       for (int c=0; c<choices.length; c++)
       {
           if (choices[c]!=null) newChoices[c] = choices[c].convert();
       }
	      return newChoices;
   }
   public Integer getChoices() { return count; }

   /** Methods to satisfy the polymorphic Lesson requirements */
   public JPanel[] getLessonData() { return null; }
   public void insertPicture(URL imageFile, int scale, int angle)
                            throws IOException {}
   
   public void removePicture(int pictureNum) {}
   public SoundData getSoundData(int image, int layer, Point point, int index) { return null; }
   public PictureData getPictureData(int pictureNum) { return null; }
   public String isPlayable(int layer) { return ""; }
   public JPanel play() { return null; }
   public boolean export(File directoryFile, String[] formats
                                   , int number)   throws IOException { return false; }
																						  
   public void	importData(int layer, Point point, URL fileName
               , String[] data, int type) throws IOException
               , UnsupportedAudioFileException {}
   
   public void redo(UndoRedoData data) {}
   public void undo(UndoRedoData data) {}
   public UndoRedoData getData() { return null; }
    public int[] getPlayOptions() { return null; }
   public void setPlayOptions(int[] playOptions) {}
   public boolean print(Document document, Element node
            , File directory, String[] extensions) { return false; }
  
}  // End MultipileChoice