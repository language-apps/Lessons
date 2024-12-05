/*
 * LessonsFillBlanks.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.lesson.fillBlanksV4;

import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import org.w3c.dom.*;
import javax.sound.sampled.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.acorns.lesson.*;
import org.acorns.data.*;
import org.acorns.visual.*;
import org.acorns.editor.*;
import org.acorns.language.*;

public class LessonsFillBlanks extends Lesson 
        implements Serializable, ComponentListener
{  
  private AnnotationData annotationData;
  private ColorScheme colorScheme;  
   
  private transient FillPanel fillPanel;
  private transient JPanel    controlPanel;
  private transient boolean   playback;
   
  private static final long serialVersionUID = 1;
  public transient static final int  MIN_ANNOTATION_PCT = 15;
   
  //--------------------------------------------------------
  // Constructor for initially instantiating the lesson.
  //--------------------------------------------------------
  public LessonsFillBlanks()
  {  
     super("Hear and Respond;"
             + LanguageText.getMessage("LessonsFillBlanks",9));
     colorScheme    = new ColorScheme(Color.blue, Color.white);
      
     String[] text = {"","",""};
     annotationData = new AnnotationData(text, null);
     fillPanel      = null;
     playback       = false;
  }
   
  public LessonsFillBlanks
    (AnnotationData annotationData, String link, ColorScheme colorScheme)
  {
       super("Hear and Respond;"
               + LanguageText.getMessage("LessonsFillBlanks",9), link);
       this.annotationData = annotationData;
       this.colorScheme = colorScheme;
       fillPanel = null;
       playback  = false;
  }
  
  public LessonsFillBlanks(Object object)
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
	  } catch (Exception e) {
		  return;
	  }

  }
  
  /** Method to return the lesson category name */
  public String getCategory() { return "CategoryAnnotations"; }

  
  /** Method to alter the JSlider showing the current annotation level
   */
  public void setAnnotationSlider()  
  { 
     if (fillPanel==null) return;
     
     SetupPanel.changeLayer(layer);
       
     SoundPanel soundPanel = fillPanel.getSoundPanel();
     if (soundPanel!=null) soundPanel.setAnnotationSlider(); 
  }
  
  //--------------------------------------------------------
  // Method to return the panel for displaying lesson data.
  //--------------------------------------------------------
  public JPanel[] getLessonData()
  {   
     JPanel[] panels = new JPanel[2];
       
     annotationData.setAnnotationLevel(layer - 1);
     setAnnotationData(annotationData);
     setAnnotationSlider();
     annotationData.clearAllHighlights();
     if (fillPanel==null)
     {   fillPanel = new FillPanel(SETUP, this, colorScheme);
         fillPanel.addComponentListener(this);  
     }
        
     panels[AcornsProperties.DATA]  = fillPanel;
       
     controlPanel = SetupPanel.createSetupPanel
             (this, "AnnotationSetup",
              SetupPanel.FGBG + SetupPanel.PICTURE +
              SetupPanel.ALIGN + SetupPanel.FONT);
     panels[AcornsProperties.CONTROLS] = controlPanel;
     return panels;
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
   
  /** Method to get the object with the background/forground colors. */
  public ColorScheme getColorScheme() { return colorScheme; }

  /** to retrieve a PictureData item
   *
   * @param pictureNum Which picture to get (unused)
   * @return
   */
  public PictureData getPictureData(int pictureNum) 
  {
     return colorScheme.getPicture();
  }

   
  /** Methods to insert or remove a picture into the lesson.
   *
   * @param url The URL that indicates the picture resource
   * @param scaleFactor The scale factor that alters the picture size
   * @param angle Angle to rotate the picture
   * @throws IOException
   */
  public void insertPicture(URL url, int scaleFactor, int angle) 
                                               throws IOException
  {  
     PictureData picture = new PictureData(url, null);
     picture.setAngle(angle);
     picture.setScale(scaleFactor);
   
     colorScheme.setPicture(picture);
     setDirty(true);
     if (fillPanel!=null) fillPanel.repaint();
  }   

  /** Method to remove a picture
   *
   * @param pictureNum Which picture (unused)
   */
  public void removePicture(int pictureNum) 
  {  
     try  {  colorScheme.setPicture(null); }
     catch (Exception e) {}  // Should never happen
     resetUndoRedo();
     setDirty(true);
     fillPanel.repaint();
  }
   
  //------------------------------------------------------------
  // Method to execute a lesson.
  //------------------------------------------------------------
  public JPanel play()
  {
     if (!isPlayable()) return null;
     if (annotationData==null) return null;
 
     annotationData.setAnnotationLevel(layer - 1);
     if (!annotationData.isRecorded()) return null;
     setAnnotationSlider();
     JPanel playPanel = new FillPanel(PLAY, this, colorScheme);
     return playPanel;
  }
   
  //--------------------------------------------------------
  // Method to determine if a lesson is playable.
  //--------------------------------------------------------
  public String isPlayable(int thisLevel) 
  {  
     if (annotationData==null) return LanguageText.getMessage(this, 1);
 
     if (!annotationData.isRecorded()) 
         return LanguageText.getMessage("commonHelpSets", 27);
     if (annotationData.getAnnotationCount(thisLevel-1) < 1)
     {
        return LanguageText.getMessage(this, 2);
     }
      return null;
  }

  /** Method to return the continuous mode playback option */
  public boolean getPlayback() {  return playback; }

  /** Method to set the continuous mode playback option */
  public void setPlayback(boolean flag) { playback = flag; }

  /** Method to set options for play mode
   *
   * @param options array of lesson play mode options
   *                  (none)scale factor for picture in index 0
   */
  public void setPlayOptions(int[] options) 
  {  if (options.length>=1)
     {  if (options[0]>=ColorScheme.MIN_FONT_SIZE 
                  && options[0]<=ColorScheme.MAX_FONT_SIZE)
        {  colorScheme.setSize(options[0]);  }

        if (options.length>=2)
        {   if (options[1]==1) playback = true;
            else playback = false;
        }
     }
  }
   
  /** Method to return play mode options
   * 
   * @return array of options (scale factor in index 0).
   */
  public int[] getPlayOptions()  
  {  int[] options = new int[2];
     options[0] = colorScheme.getSize();
     if (playback) options[1] = 1;
     else options[1] = 0;
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
	 {
      switch (type)
      {
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
                {
                    if (data[1].toLowerCase().equals("center"))
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
   }        // End of importData()
   
       
   /** Method to create xml for printing and exporting
    *
    * @param document The xml document
    * @param lessonNode The lesson node to which to append xml data
    * @param directory Path to export directory (null if just printing)
    * @param extension for image or sound data (null if just printing)
    * @return true if successful, false otherwise
    */
   public boolean print( Document document, Element lessonNode
            , File directory, String[] extensions)
   {
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
         String[] values = new String[]
                      {"0", "", ""+picture.getScale(), "" };
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
      String[] values = {bg, fg, size, ""};
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
   {
      SoundUndoRedoData undoRedoData = (SoundUndoRedoData)dataRecord;
      annotationData = (AnnotationData)undoRedoData.getData();
      if (fillPanel != null) 
      {
         fillPanel.getComponent(0);
         fillPanel.repaint();
      }
   }
   public void undo(UndoRedoData dataRecord)
   {
      SoundUndoRedoData undoRedoData = (SoundUndoRedoData)dataRecord;
      annotationData = (AnnotationData)undoRedoData.getData();
      System.gc();
      annotationData.setSoundEditor(getUndoRedoStack());
      if (fillPanel != null) 
      {  fillPanel.getComponent(0);
         fillPanel.repaint();
      }
   }
   
   /********* Method to convert between versions, nothing to do for now  *************/
   public Lesson convert(float version)   { return this; }
   
   /********* Respond to property change listener in fill panel  *************/
   public void setAnnotationData(AnnotationData data) 
   { annotationData = data; 
     if (annotationData!=null)
     {
         if (layer!= data.getAnnotationLevel() + 1)
         { 
            layer = data.getAnnotationLevel() + 1;
            if (fillPanel!=null)
            {  setAnnotationSlider(); }
            setDirty(true);
         }
         if  (annotationData.getSoundEditor()==null)
            annotationData.setSoundEditor(getUndoRedoStack());
     }
   }
   public AnnotationData getAnnotationData()          
   { return annotationData; }

   /********* Respond when frame is resized *****************************/
   public void componentHidden(ComponentEvent e) {}

    public void componentMoved(ComponentEvent e) {}

    public void componentResized(ComponentEvent e)
    {
        fillPanel = new FillPanel(SETUP, this, colorScheme);
        fillPanel.addComponentListener(this);
    }

    public void componentShown(ComponentEvent e) {}
   
}     // End of LessonsFillBlanks class.
