/**
 * LessonsMovingPictures.java
 *
 *   @author  HarveyD
 *   @version 6.00
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.lesson.magnetGameV6;

import java.util.*;
import java.io.*;
import java.net.*;
import org.w3c.dom.*;
import java.awt.*;
import javax.swing.*;
import javax.sound.sampled.*;

import org.acorns.data.*;
import org.acorns.lesson.*;
import org.acorns.language.*;
import org.acorns.visual.*;

public class LessonsMagnetGame extends Lesson implements Serializable
{
   private static final long serialVersionUID = 1;

   public static final String lessonName = "Magnet Game;";

   private Vector<SentenceData>[] sentences;
   private ColorScheme colorScheme;
   
   public LessonsMagnetGame()
   { 
       super("Magnet Game;"
             + LanguageText.getMessage("LessonsMagnetGame",1)); 
   }

   /** Get the array of vectors that are needed to convert this lesson */
   public Vector<SentenceData>[] getSentenceData()  { return sentences;   }

   /** Method to get background/foreground color object (for the setup panel) */
   public ColorScheme getColorScheme() { return colorScheme; }


   /** Method to convert between older ACORNS versions 
    *
    * @param version The current version number
    * @return The same lesson object converted
    */
   public Lesson convert(float version) 
   {   
       org.acorns.lesson.magnetGameV7.LessonsMagnetGame magnetGame
               = new org.acorns.lesson.magnetGameV7.LessonsMagnetGame
                                                       (sentences, colorScheme);
       return magnetGame;
   }

   /** Methods to satisfy abstract parent class */
   public JPanel[] getLessonData()   {   return null;   }
   public void insertPicture(URL url, int scaleFactor, int angle)
                                               throws IOException  {}
   public void removePicture(int row) {}
   public SoundData getSoundData(int sentence, int layer, Point p, int index)
   {  PicturesSoundData sounds = sentences[layer-1].get(sentence).getAudio();
      return sounds.getVector().elementAt(index);
   }
   public PictureData getPictureData(int pictureNum) { return null; }
   public boolean print(Document document, Element lessonNode
                        , File directory, String[] extensions) { return false; }
   public boolean export
           (File directoryName, String[] formats, int lesson) throws IOException
   { return false; }
   public void importData
            (int myLayer, Point point, URL file, String[] data, int type)
                       throws IOException, UnsupportedAudioFileException  {}
  public  UndoRedoData getData()  {  return null;  }
  public void redo(UndoRedoData dataRecord) {}
  public void undo(UndoRedoData dataRecord) {}
  public String isPlayable(int layer) { return "old lesson type"; }
  public JPanel play() { return null; }
  public int[] getPlayOptions() { return null; }
  public void setPlayOptions(int[] options)  {}

}  // End LessonsMagnetGames