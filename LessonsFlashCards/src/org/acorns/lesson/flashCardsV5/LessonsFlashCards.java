/**
 * LessonsFlashCards.java
 *
 *   @author  HarveyD
 *   @version 5.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.lesson.flashCardsV5;

import java.awt.*;
import javax.swing.*;

import java.io.*;
import java.net.*;
import org.w3c.dom.*;

import javax.sound.sampled.*;
import org.acorns.lesson.*;
import org.acorns.data.*;
import org.acorns.visual.*;

/** Skeleton class to convert from vertion 5.0 to 6.0 */
public class LessonsFlashCards extends Lesson implements Serializable
{
   private static final long serialVersionUID=1L;

   private PictureChoice[] choices;
   private int             count;
   private ColorScheme     colors;

   public LessonsFlashCards()  { super("Flash Cards"); }
  
  /********* Method to convert between versions  *************/
   public Lesson convert(float version)  
   { return new org.acorns.lesson.flashCardsV6.LessonsFlashCards(this);
   }
   
   /** Methods for conversion to latest format */
   public PictureChoice[] getPictureData() { return choices; }
   public Integer getChoices() { return count; }
   public ColorScheme getColorScheme() { return colors; }

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
  
}  // End LessonsFlashCards
