/*
 * LessonsStoryBook.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2009-2015, all rights reserved
 */
package org.acorns.lesson.storyBookV5;

import java.io.*;
import java.net.*;
import java.lang.reflect.Field;

import org.w3c.dom.*;

import javax.sound.sampled.*;

import java.awt.*;

import javax.swing.*;

import org.acorns.lesson.*;
import org.acorns.data.*;
import org.acorns.visual.*;
import org.acorns.editor.*;
import org.acorns.language.*;

public class LessonsStoryBook extends Lesson  implements Serializable
{  
  private AnnotationData annotationData;
  private ColorScheme colorScheme;
  private String[]    textEntered;
   
  private transient FillPanel fillPanel;
  private transient FillPanel playPanel;
  private transient JPanel    controlPanel;
  private transient StoryScrollPane storyPanel;
   
  private static final long serialVersionUID = 1;
  public transient static final int  MIN_ANNOTATION_PCT = 15;
   
  //--------------------------------------------------------
  // Constructor for initially instantiating the lesson.
  //--------------------------------------------------------
  public LessonsStoryBook()
  {  
     super("Story Book;" + LanguageText.getMessage("LessonsStoryBook",6));
     colorScheme    = new ColorScheme(Color.blue, Color.white);
      
     String[] text = {"","",""};
     annotationData = new AnnotationData(text, null);
     textEntered = new String[AcornsProperties.MAX_LAYERS];
     for (int i=0; i<AcornsProperties.MAX_LAYERS; i++) textEntered[i] = "";
     fillPanel      = null;
  }
  
  public LessonsStoryBook(Object object)
  {
	  this();

	  try
	  {
		  Class<?> objClass = object.getClass();
		  Field field = objClass.getDeclaredField("annotationData");
		  field.setAccessible(true);
		  annotationData = (AnnotationData)field.get(object);

		  field = objClass.getDeclaredField("colorScheme");
		  field.setAccessible(true);
		  colorScheme = (ColorScheme)field.get(object);
		  
		  int size = textEntered.length;
		  for (int i=0; i<size; i++) {
			  textEntered[i] = annotationData.getAnnotationText(i);
		  }
	  } catch (Exception e) {
		  return;
	  }
  }

  
  /** Method to return the lesson category name */
  public String getCategory() { return "CategoryAnnotations"; }
    
  //--------------------------------------------------------
  // Method to return the panel for displaying lesson data.
  //--------------------------------------------------------
  public JPanel[] getLessonData()
  {   
     JPanel[] panels = new JPanel[2];

     setAnnotationSlider();
     annotationData.clearAllHighlights();
     if (fillPanel==null) { fillPanel = new FillPanel(SETUP, this); }
        
     panels[AcornsProperties.DATA]  = fillPanel;
       
     controlPanel = SetupPanel.createSetupPanel
             (this, "StorySetup",
              SetupPanel.FGBG +  SetupPanel.ALIGN +
              SetupPanel.FONT + SetupPanel.PICTURE + SetupPanel.SCALE);
     panels[AcornsProperties.CONTROLS] = controlPanel;
     return panels;
  }

  /** Method to save any data not yet saved */
  public void save()  
  {  
	 getStoryPanel().saveText();
  	 new SetAnnotations(this, getLayer());
     setDirty(true);
  }
   
  /** Method to get the sound associated with this data
   * 
   * @param image PictureChoice to which it is attached
   * @param layer Which layer is it attached to
   * @param point The point x,y position (must be 0,0)
   * @param index which recorded sound attached to this picture
   * @return The sound data object
   */
  public SoundData getSoundData
          (int image, int layer, Point point, int index)
  {
      if (layer>0) return null;
      return annotationData;
  }

  /** Method to get the background and foreground color schemes */
  public ColorScheme getColors()  { return colorScheme; }

  /** Get panel holding the text input */
  public StoryScrollPane getStoryPanel()
  {   if (storyPanel==null)   {  storyPanel = new StoryScrollPane(this);  }
      return storyPanel;
  }

  /** Method to retrieve the text for this lesson */
  public String getTextEntered()  {   return textEntered[layer-1].trim();  }
  
  public String getTextEntered(int layer) { return textEntered[layer-1].trim(); }

  /** Method to store text entered for this lesson */
  public void setTextEntered(String data)  
  { 
	  textEntered[layer-1] = data; 
  }

  /** Method to get background/foreground color object (for the setup panel) */
  public ColorScheme getColorScheme() { return colorScheme; }
  
  /** Method to get object with the scale factor (for the setup panel) */
  public PictureData getPictureData(String msg)
  { return colorScheme.getPicture(); }

  /**  Methods to retrieve background picture
   *
   * @param pictureNum Not used - only one picture
   * @return The background picture or null
   */
  public PictureData getPictureData(int pictureNum) 
  {
     return colorScheme.getPicture();
  }

  /** Method to insert a picture to this lesson
   *
   * @param url the source identifying the location of the picture
   * @param scaleFactor how the picture should be scaled
   * @param angle Rotation angle
   * @throws java.io.IOException
   */
  public void insertPicture(URL url, int scaleFactor, int angle)
                                                             throws IOException
  {  PictureData picture = new PictureData(url, null);
     picture.setAngle(angle);
     picture.setScale(scaleFactor);
   
     colorScheme.setPicture(picture);
     setDirty(true);
     if (fillPanel!=null) fillPanel.repaint();
  }   

  /** Method to remove the picture from the lesson
   *
   * @param pictureNum unused
   */
  public void removePicture(int pictureNum) 
  {  try  {  colorScheme.setPicture(null); }
     catch (Exception e) {}  // Should never happen
     resetUndoRedo();
     setDirty(true);
     fillPanel.repaint();
  }
   
  //------------------------------------------------------------
  // Method to execute a lesson.
  //------------------------------------------------------------
  public JPanel play()
  {  setAnnotationSlider();

     if (!isPlayable()) return null;
     if (annotationData==null) return null;

     if (!annotationData.isRecorded()) return null;
     playPanel = new FillPanel(PLAY, this);
     return playPanel;
  }
   
  //--------------------------------------------------------
  // Method to determine if a lesson is playable.
  //--------------------------------------------------------
  public String isPlayable(int thisLevel) 
  {  if (annotationData==null) return LanguageText.getMessage(this, 1);
 
     if (!annotationData.isRecorded())
        return LanguageText.getMessage(this, 2);

     if (textEntered[thisLevel-1].trim().length()==0)
         return LanguageText.getMessage(this, 3);

     if (getColors().getPicture()==null)
         return LanguageText.getMessage(this, 4);

     return null;
  }
   
  /** Method to set options for play mode
   *
   * @param options array of lesson play mode options (none)scale factor for picture in index 0
   */
  public void setPlayOptions(int[] options) 
  {  if (options.length>=1)
     {  if (options[0]>=ColorScheme.MIN_FONT_SIZE 
                  && options[0]<=ColorScheme.MAX_FONT_SIZE)
        {  colorScheme.setSize(options[0]);  }
     }
  }
   
  /** Method to return play mode options
   * 
   * @return array of options (scale factor in index 0).
   */
  public int[] getPlayOptions()  
  {  int[] options = new int[2];
     options[0] = colorScheme.getSize();
     return options;
  }
   
  //--------------------------------------------------------
  // Method to write images and sounds in a standard format to a subdirectory.
  //--------------------------------------------------------
  public boolean export
      (File directoryName, String[] formats, int number) throws Exception
  {
     Point point = new Point(0,0);
     writeImage(colorScheme.getPicture(), directoryName
             , number, 0, formats[AcornsProperties.IMAGE_TYPE]);       
     writeSound(annotationData, directoryName, number, 0, 
             0, point, 0, formats[AcornsProperties.SOUND_TYPE]);
     return true;
   }                    // End of export method.
	
  transient int sampleRate; // Samples per second.
   
  //--------------------------------------------------------
  // Method to import a record to add the the lesson.
  //--------------------------------------------------------
  public void importData
          (int layer, Point point, URL file, String[] data, int type)
                           throws IOException, UnsupportedAudioFileException
  {   switch (type)
      {   case AcornsProperties.PARAM:
              if (data[0].toLowerCase().equals("text"))
              {  textEntered[layer-1] = data[1];
                 getStoryPanel().setText(data[1]);
              }
              break;

          case AcornsProperties.SOUND:
              if (layer==0 && point.x==0 && point.y==0 && file!=null)
              {
                 String[] newAnnotationData = new String[4];
                 newAnnotationData[3] = data[0];
                 newAnnotationData[0] = "";
                 annotationData
                         = new AnnotationData(newAnnotationData, null);
                 annotationData.readFile(file);

                 try
                 {  sampleRate = (int)(Double.parseDouble(data[SoundData.FRAMERATE])); }
                 catch (Exception e)
                 { sampleRate = (int)AudioSystem.getAudioFileFormat(file)
                                        .getFormat().getSampleRate();
                 }

              }
              if (layer==0) layer = 1;
              annotationData.setAnnotationLevel(layer-1);
              annotationData.insertAnnotation(data[1], point, sampleRate);
              break;

          case AcornsProperties.LAYER:
              if (layer==0) layer = 1;
              annotationData.setAnnotationLevel(layer-1);
              if (data[0]!=null) annotationData.setKeyboard(data[0]);

              annotationData.setCentered(false);
              if (data.length>1)
              {  if (data[1].toLowerCase().equals("center"))
                     annotationData.setCentered(true);
              }
              break;

           case AcornsProperties.LINK:
              link = data[0];
              break;
                 
           case AcornsProperties.FONT:
              if (!data[0].equals(""))
                 colorScheme.changeColor(getColor(data[0]), true);
              if (!data[1].equals(""))
                 colorScheme.changeColor(getColor(data[1]), false);
              if (!data[2].equals(""))
                 colorScheme.setSize(Integer.parseInt(data[2]));
              fillPanel = null;
              break;
        }
        annotationData.setAnnotationLevel(0);
        getStoryPanel().configureDisplay(false);
        
   }        // End of importData()
   
        
   /** Method to create XML for printing and exporting
    *
    * @param document The XML document
    * @param lessonNode The lesson node to which to append XML data
    * @param directory Path to export directory (null if just printing)
    * @param extension for image or sound data (null if just printing)
    * @return true if successful, false otherwise
    */
   public boolean print( Document document, Element lessonNode
            , File directory, String[] extensions)
   {
      String[] values;

      // Get default extensions if object null
      if (extensions == null)
      {   extensions = new String[2];
          extensions[AcornsProperties.SOUND_TYPE] = "wav";
          extensions[AcornsProperties.IMAGE_TYPE] = "jpg";
      }

      // Get the lesson number index in this file
      int lessonNo
          = Integer.parseInt(lessonNode.getAttribute("number")) -1;

      PictureData picture = colorScheme.getPicture();
      if (picture != null)
      {
         values = new String[]{"0", "", ""+picture.getScale(), "" };
         Element imageNode = makeImageNode(document, directory
                           , extensions[AcornsProperties.IMAGE_TYPE]
                           , picture, values, lessonNo);
         lessonNode.appendChild(imageNode);
      }

      String[] fontAttributes
              =  {"background", "foreground", "size"};

      Color f = colorScheme.getColor(false);
      Color b = colorScheme.getColor(true);
      String fg = f.getRed() + "," + f.getGreen() + "," + f.getBlue();
      String bg = b.getRed() + "," + b.getGreen() + "," + b.getBlue();
      String size = "" + colorScheme.getSize();
      values = new String[]{bg, fg, size, ""};
      Element fontNode
              = makeNode(document, "font", fontAttributes, values);
      lessonNode.appendChild(fontNode);

      String[] layerAttributes = {"value", "align", "language"};
      values = new String[] {"0", "", ""};
      Element layerNode
              = makeNode(document, "layer", layerAttributes, values);
      lessonNode.appendChild(layerNode);

      String prefix = "", fileName = "";
      if (directory!=null) prefix = directory.getName() + "/";

      Point point = new Point(0,0);
      Element pointNode, spellNode;

      if (annotationData.isRecorded())
      {
         SoundData sound = getAnnotationData().getSound();
          try
          {  File file = getSoundName
                (directory, lessonNo+1, 0, 0, point, 0
                          , extensions[AcornsProperties.SOUND_TYPE], false);

             if (file!=null) fileName = prefix + file.getName();
          }
          catch (IOException ioe)
          {}

          pointNode = makeSoundPointNode(document, sound, fileName, point);
          layerNode.appendChild(pointNode);
      }

      // Output the annotation information for each point.
      AnnotationNode[] nodes;
      String language, annotation;
      int last = 0, current = 0;
      String[] pointAttributes = {"x", "y", "type"};
      String[] spellAttributes = {"language"};
      Text textNode;

      for (int i=0; i<AcornsProperties.MAX_LAYERS; i++)
      {
         new SetAnnotations(this, i+1);
    	  
         nodes = annotationData.getScaledAnnotationNodes
                       (i, extensions[AcornsProperties.SOUND_TYPE]);
         if (nodes==null) continue;
         if (annotationData.getAnnotationCount(i)==0
                 && !(annotationData.isCentered(i)))   continue;

         language = annotationData.getKeyboard();
         values = new String[] {"" + (i+1), "", language };
         if (annotationData.isCentered(i)) values[1] = "center";
         layerNode = makeNode(document, "layer", layerAttributes, values);
         lessonNode.appendChild(layerNode);

         values = new String[] {textEntered[i].trim()};
         String[] storyAttributes = {"text"};
         Element storyNode
              = makeNode(document, "param", storyAttributes, values);
         layerNode.appendChild(storyNode);

         last = 0;
         for (int n=1; n<nodes.length; n++)
         {  if (nodes[n]==null) { last = current; continue; }

            current = (int)nodes[n].getOffset();
            annotation = nodes[n].getText();
            if (annotation.length()==0) 
            {  last = current; continue; }

            values = new String[]{""+last, ""+current, "sound"};
            pointNode = makeNode
                        (document, "point", pointAttributes, values);

            values = new String[]{""};
            if (!language.equals("English")) values[0] = language;
            spellNode = makeNode
                       (document, "spell", spellAttributes, values);
            textNode = document.createTextNode(annotation);

            layerNode.appendChild(pointNode);
            pointNode.appendChild(spellNode); 
            spellNode.appendChild(textNode);

            last = current;
        }  // End for points in a particular layer
      }    // End for each annotation layer
      return true;
   }   // End of print method

   //--------------------------------------------------------
   // Methods to respond to the redo and undo commands.
   //--------------------------------------------------------
   
   public  UndoRedoData getData() 
   {  return new SoundUndoRedoData(annotationData.clone()); }

   public void redo(UndoRedoData dataRecord)
   {  SoundUndoRedoData undoRedoData = (SoundUndoRedoData)dataRecord;
      annotationData = (AnnotationData)undoRedoData.getData();
      if (fillPanel != null) { fillPanel.repaint();  }
   }
   public void undo(UndoRedoData dataRecord)
   {  SoundUndoRedoData undoRedoData = (SoundUndoRedoData)dataRecord;
      annotationData = (AnnotationData)undoRedoData.getData();
      System.gc();
      annotationData.setSoundEditor(getUndoRedoStack());
      if (fillPanel != null) 
      {  
    	  fillPanel.repaint();
      }
   }
   
   /********* Method to convert between versions, nothing to do for now  *************/
   public Lesson convert(float version)   { return this; }
   
   /********* Respond to property change listener in fill panel  *************/
   public void setAnnotationData(AnnotationData data) 
   { annotationData = data;
     boolean save = true;
     if (annotationData!=null)
     {
         if (layer!= data.getAnnotationLevel() + 1)
         {  save = false;
            getStoryPanel().saveText();
            layer = data.getAnnotationLevel() + 1;
            setDirty(true);
            getStoryPanel().setText(textEntered[layer-1]);
            displayLesson();
         }
         getStoryPanel().configureDisplay(save);

         if  (annotationData.getSoundEditor()==null)
            annotationData.setSoundEditor(getUndoRedoStack());
     }
   }
   public AnnotationData getAnnotationData()          
   { return annotationData; }

  /** Method to alter the JSlider showing the current annotation level
   */
  public void setAnnotationSlider()
  {
     if (fillPanel==null) return;

     int newLayer = annotationData.getAnnotationLevel() + 1;
     annotationData.setAnnotationLevel(layer - 1);
     layer = newLayer;

     SoundPanel soundPanel = fillPanel.getSoundPanel();
     if (soundPanel!=null) soundPanel.setAnnotationSlider();
  }
 
}     // End of LessonsFillBlanks class.
