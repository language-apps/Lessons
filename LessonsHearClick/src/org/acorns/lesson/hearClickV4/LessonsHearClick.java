/*
 * LessonsFillBlanks.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.lesson.hearClickV4;

import java.awt.*;
import javax.swing.*;

import java.io.*;
import java.net.*;
import org.w3c.dom.*;

import javax.sound.sampled.*;

import org.acorns.lesson.*;
import org.acorns.data.*;
import org.acorns.visual.*;
import org.acorns.editor.*;
import org.acorns.language.*;

public class LessonsHearClick extends Lesson implements Serializable
{  
   private ImageAnnotationData imageAnnotationData;
   private ColorScheme colorScheme;   
   
   private transient FillPanel fillPanel;
   private transient JPanel    controlPanel;
   
   private static final long serialVersionUID = 1;
   private static final Dimension MAX_PICTURE = new Dimension(300,300);
   
   //--------------------------------------------------------
   // Constructor for initially instantiating the lesson.
   //--------------------------------------------------------
   public LessonsHearClick()
   {  
      super("Hear and Click;" + LanguageText.getMessage("LessonsHearClick",10));
      colorScheme    = new ColorScheme(Color.blue, Color.white);
      imageAnnotationData = new ImageAnnotationData();
      fillPanel      = null;
   }
      
   /** Method to return the panel for lesson set up mode
    *  @return index 0 = control panel, index 1 = data panel
    */
   public JPanel[] getLessonData()
   {   
       JPanel[] panels = new JPanel[2];
       setAnnotationData(imageAnnotationData);
       setAnnotationSlider();
       if (fillPanel==null)
       {   fillPanel = new FillPanel(SETUP, this, colorScheme);
       }
       panels[AcornsProperties.DATA]  = fillPanel;

       controlPanel = SetupPanel.createSetupPanel(this, "AnnotationClickSetup",
       SetupPanel.ROTATE + SetupPanel.FGBG + SetupPanel.PICTURE);
       panels[AcornsProperties.CONTROLS] = controlPanel;
       return panels;
   }
   
    /** Abstract method to get a recorded sound for this lesson 
    * 
    * @param image which image is this sound attached to
    * @param layer which lesson layer this sound is attached to
    * @param point the corresponding x,y point location
    * @param index the index of the sound at the x,y point location
    * @return SoundData object that corresponds to this request
    */
   public SoundData getSoundData(int image, int layer, Point point, int index)
   {  return imageAnnotationData.getSound(layer - 1);  }
    
   /** Method to get the area the user selected in the sound wave */
   private Point getSelectedArea()
   {
       if (fillPanel != null)
       {
          SoundDisplayPanel panel 
                  = fillPanel.getSoundPanel().getDisplayPanel();
          if (panel != null)
          { return panel.getSelectedFrames(imageAnnotationData);  }
       }
       return null;
   }

   /** Method to get the range of pixels */
   private Point getSelectedPixels()
   {
       if (fillPanel != null)
       {
          SoundDisplayPanel panel
                  = fillPanel.getSoundPanel().getDisplayPanel();
          if (panel != null)
          {  return panel.getSelectedPixels();  }
       }
       return null;

   }

   /** Method to return object for background/foreground buttons. */
   public ColorScheme getColorScheme() { return colorScheme; }
   public PictureData getPictureData(String errMsg)
   { PictureData data =  getPictureData(-1);
     if  (data==null && errMsg!=null)
         JOptionPane.showMessageDialog
            (null, LanguageText.getMessage("commonHelpSets", 3));
    return data;
   }

   //--------------------------------------------------------------------------
   //  Methods to retrieve, insert or remove a PictureChoice from the lesson.
   //--------------------------------------------------------------------------
   public PictureData getPictureData(int pictureNum) 
   { 
       // Calls from print preview and export modules define
       //   pictureNum as 1000 * (layer+1) + node offset
       if (pictureNum < 1000)
       {
           Point point = getSelectedArea();
           if (point!=null)
           {
               return imageAnnotationData.getObject(point);
           }
           return colorScheme.getPicture();
       }
       else
       {
           int level = pictureNum / 1000 - 1; // Callers number layers from one
           int nodeOffset = pictureNum % 1000;
           
           AnnotationNode[] nodes 
                   = imageAnnotationData.getAnnotationNodes(level);
           PictureData picture = (PictureData)nodes[nodeOffset].getObject();
           return picture;
       }
   }
   
   //------------------------------------------------------------
   //  Methods to insert or remove a picture into/from the lesson.
   //------------------------------------------------------------
   public void insertPicture(URL url, int scaleFactor, int angle) 
                                               throws IOException
   {  
      PictureData picture = new PictureData(url, null);
      picture.setAngle(angle);
      picture.setScale(scaleFactor);
      UndoRedoData oldData = imageAnnotationData.undoRedoObject();

      Point pixels = getSelectedPixels();
      Point point = getSelectedArea();
      if (pixels!=null && pixels.x!=pixels.y && point.x==point.y)
      {  Toolkit.getDefaultToolkit().beep();
         return;
      }
      if (point != null && point.x!=point.y) 
      {   picture.rewrite(MAX_PICTURE);
          if (!imageAnnotationData.insertAnnotation(picture, point))
          {  setText("Picture insertion failed"); }
          else pushUndo(oldData); 
  
      }
      else  colorScheme.setPicture(picture);
      setDirty(true);
      if (fillPanel!=null) fillPanel.repaint();
   }   
      
   public void removePicture(int pictureNum) 
   {  
       Point point = getSelectedArea();
       PictureData picture = null;
       UndoRedoData oldData = imageAnnotationData.undoRedoObject();
       Frame root = JOptionPane.getRootFrame();
       
       try
       {
           if (point != null) 
           {
               if (point.x != point.y)
               {
                   JOptionPane.showMessageDialog(root,
                                             LanguageText.getMessage(this, 2));
                   return;
               }
               picture = imageAnnotationData.getObject(point);
               if (!imageAnnotationData.deleteObject(picture))
               {  if (colorScheme.getPicture(new Dimension(100,100))!=null)
                  {
                      colorScheme.setPicture(null);
                  }
                  else setText(LanguageText.getMessage(this, 3));
               }
               else pushUndo(oldData); 
           }
           else 
           {
               if (colorScheme.getPicture()!=null) colorScheme.setPicture(null);
               else { setText(LanguageText.getMessage("commonHelpSets", 33)); }
           }
       }
       catch (IOException e) {setText(e.toString()); }
       
       setDirty(true);
       fillPanel.repaint();
   }
   
   //------------------------------------------------------------
   // Method to execute a lesson.
   //------------------------------------------------------------
   public JPanel play()
   {
      if (imageAnnotationData==null) return null;
      if (!isPlayable()) return null;
 
      imageAnnotationData.setAnnotationLevel(getLayer()-1);
      SoundData sound = imageAnnotationData.getSound();
 
      if (sound == null) return null;
      if (!sound.isRecorded()) return null;

      if (imageAnnotationData.getAnnotationCount()< 1) return null;
      
      FillPanel playPanel = new FillPanel(PLAY, this, colorScheme);
      return playPanel;
   }
   
   //--------------------------------------------------------
   // Method to determine if a lesson is playable.
   //--------------------------------------------------------
   public String isPlayable(int newLevel) 
   {  
       int level = newLevel - 1;
       SoundData sound = null;
       if (imageAnnotationData==null) return LanguageText.getMessage(this, 4);
       sound = imageAnnotationData.getSound(level);
       
       if (sound==null || !sound.isRecorded())
       {  return LanguageText.getMessage("commonHelpSets", 27);  }
       if (imageAnnotationData.getAnnotationCount(level) < 1)
       {   return LanguageText.getMessage(this, 5); }
       return null;
   }
   
   /** Method to set options for play mode
    * 
    * @param options array of lesson play mode options (none for this lesson)
    */
   public void setPlayOptions(int[] options) {}
   
   /** Method to return play mode options
    * 
    * @return array of options (scale factor in index 0).
    */
   public int[] getPlayOptions()  {return new int[0];}
      
   
   //--------------------------------------------------------
   // Method to write images and sounds in a standard format to a subdirectory.
   //--------------------------------------------------------
   public boolean export
           (File directoryName, String[] formats, int number) 
        		   throws Exception
   {
      Point point = new Point(0,0);
      writeImage(colorScheme.getPicture(), directoryName
              , number, 0, formats[AcornsProperties.IMAGE_TYPE]);  
      
      SoundData sound;
      AnnotationNode[] nodes;
      int count, imageNo;
      PictureData picture;
      for (int thisLayer=0; thisLayer<AcornsProperties.MAX_LAYERS; thisLayer++)
      {
          // See if the sound for the next layer is recorded
          sound = imageAnnotationData.getSound(thisLayer);
          if (sound.isRecorded())
          {
              writeSound(sound, directoryName, number, 0, 
              thisLayer+1, point, 0, formats[AcornsProperties.SOUND_TYPE]);
              
              nodes = imageAnnotationData.getAnnotationNodes(thisLayer);
              count = imageAnnotationData.getAnnotationSize(thisLayer);
              if (count>1)
              {
                  for (int node=1; node<count; node++)
                  {
                      picture = (PictureData)nodes[node].getObject();
                      if (picture!=null)
                      {   imageNo = (thisLayer+1)*1000 + node;
                          picture.rewrite(MAX_PICTURE);
                          writeImage(picture, directoryName, number, imageNo
                                      , formats[AcornsProperties.IMAGE_TYPE]);  
                      }
                  }
              }
          }
          
      }
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
                if (file!=null)
                {
                    String[] newAnnotationData = new String[4];
                    newAnnotationData[0] = "";

                    try
                    {  sampleRate = (int)(Double.parseDouble(data[SoundData.FRAMERATE])); }
                    catch (Exception e)
                    {  sampleRate = (int)AudioSystem.getAudioFileFormat(file)
                                           .getFormat().getSampleRate();
                    }
                    SoundData sound = imageAnnotationData.getSound();
                    sound.readFile(file);
               }
                break;

            case AcornsProperties.LAYER:
                 if (layer==0) layer = 1;
                 imageAnnotationData.setAnnotationLevel(layer-1);
                 setLayer(layer);
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
                
            case AcornsProperties.PICTURE:
                PictureData picture = new PictureData(file, null);
                imageAnnotationData.insertAnnotation
                        (picture, point, sampleRate);
                break;
        }
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
      // Get default extensions if object null
      if (extensions == null)
      {   extensions = new String[2];
          extensions[AcornsProperties.SOUND_TYPE] = "wav";
          extensions[AcornsProperties.IMAGE_TYPE] = "jpg";
      }

      // Get the lesson number index in this file
      int lessonNo
          = Integer.parseInt(lessonNode.getAttribute("number")) -1;

      // Make attributes for elements to be created
      String[] layerAttributes = {"value" };
      String[] fontAttributes = {"background", "foreground", "size"};
      String[] pointAttributes = {"x", "y", "type"};
      String[] pictureAttributes = {"value", "src", "scale", "angle"};

      Element layerNode, fontNode, pointNode, pictureNode;
      String[] values;

      PictureData picture = colorScheme.getPicture();
      if (picture != null)
      {
         values = new String[]
                        {"0", "", ""+picture.getScale(), ""+picture.getAngle() };
         pictureNode = makeImageNode(document, directory
                           , extensions[AcornsProperties.IMAGE_TYPE]
                           , picture, values, lessonNo);
         lessonNode.appendChild(pictureNode);
      }

      Color f = colorScheme.getColor(false);
      Color b = colorScheme.getColor(true);
      String fg = f.getRed() + "," + f.getGreen() + "," + f.getBlue();
      String bg = b.getRed() + "," + b.getGreen() + "," + b.getBlue();
      String size = "" + colorScheme.getSize();
      values = new String[]{bg, fg, size, ""};
      fontNode = makeNode(document, "font", fontAttributes, values);
      lessonNode.appendChild(fontNode);

      String prefix = "", fileName = "";
      if (directory!=null) prefix = directory.getName() + "/";
      File file;

      Point point = new Point(0,0);
      AnnotationNode[] nodes;
      int last = 0, current = 0;
      SoundData sound;
      int imageNo;

      for (int level=0; level<AcornsProperties.MAX_LAYERS; level++)
      {
         sound = imageAnnotationData.getSound(level);

         if ( sound==null || !sound.isRecorded()) continue;
         
         values = new String[] {""+(level+1)};
         layerNode = makeNode(document, "layer", layerAttributes, values);
         lessonNode.appendChild(layerNode);

         try
         {  file = getSoundName
                (directory, lessonNo+1, 0, level+1, new Point(0,0), 0
                          , extensions[AcornsProperties.SOUND_TYPE], false);

             if (file!=null) fileName = prefix + file.getName();
         }
         catch (IOException ioe)
         {}

         pointNode = makeSoundPointNode(document, sound, fileName, point);
         layerNode.appendChild(pointNode);

         nodes = imageAnnotationData.getScaledAnnotationNodes
                       (level, extensions[AcornsProperties.SOUND_TYPE]);
         if (nodes==null) continue;

         last = 0;
         for (int node=1; node<nodes.length; node++)
         {  if (nodes[node]==null) { last = current; continue; }

            current = (int)nodes[node].getOffset();
            picture = (PictureData)nodes[node].getObject();
            if (nodes[node].getObject()==null) 
            {  last = current; continue; }

            values = new String[]{""+last, ""+current, "picture"};
            pointNode = makeNode
                        (document, "point", pointAttributes, values);

            imageNo = (level + 1) * 1000 + node;
            values = new String[] {""+imageNo, "", ""+picture.getScale()
                                    , ""+picture.getAngle()};
            try
            {  file = getImageName(directory, lessonNo, imageNo
                                            , extensions[AcornsProperties.IMAGE_TYPE], false);
               values[1] = prefix + file.getName();
            }
            catch (IOException e)  {}

            pictureNode = makeNode
                    (document, "picture", pictureAttributes, values);

            layerNode.appendChild(pointNode);
            pointNode.appendChild(pictureNode);
            last = current;
        }  // End for points in a particular layer
      }    // End for each annotation layer
      return true;
   }   // End of print method

   //--------------------------------------------------------
   // Methods to respond to the redo and undo commands.
   //--------------------------------------------------------
   
   public  UndoRedoData getData() 
   {  return new SoundUndoRedoData(imageAnnotationData.clone()); }

   public void redo(UndoRedoData dataRecord)
   {
      SoundUndoRedoData undoRedoData = (SoundUndoRedoData)dataRecord;
      imageAnnotationData = (ImageAnnotationData)undoRedoData.getData();
      if (fillPanel != null) { fillPanel.repaint(); }
   }
   public void undo(UndoRedoData dataRecord)
   {
      SoundUndoRedoData undoRedoData = (SoundUndoRedoData)dataRecord;
      imageAnnotationData = (ImageAnnotationData)undoRedoData.getData();
      System.gc();
      imageAnnotationData.setSoundEditor(getUndoRedoStack());

      if (fillPanel != null) {  fillPanel.repaint(); }
   }

   /** Method to alter the JSlider showing the current annotation level
   */
  public void setAnnotationSlider()
  {
     if (fillPanel==null) return;

     SoundPanel soundPanel = fillPanel.getSoundPanel();
     if (soundPanel!=null) soundPanel.setAnnotationSlider();
  }

   /********* Method to convert between versions, nothing to do for now  *************/
   public Lesson convert(float version)   { return this; }
   
   /********* Respond to property change listener in fill panel  *************/
   public void setAnnotationData(ImageAnnotationData data) 
   { 
     imageAnnotationData = data;
     imageAnnotationData.setAnnotationLevel(getLayer()-1);
     imageAnnotationData.setLesson(this);
     if ((imageAnnotationData!=null)
             && (imageAnnotationData.getSoundEditor()==null))
          imageAnnotationData.setSoundEditor(getUndoRedoStack());
   }
   public ImageAnnotationData getAnnotationData(JPanel panel)          
   { 
       return imageAnnotationData; 
   }

   public void save()
   {  PictureData picture;
      AnnotationNode[] nodes;
      int count;

      for (int thisLayer=0; thisLayer<AcornsProperties.MAX_LAYERS; thisLayer++)
      {   nodes = imageAnnotationData.getAnnotationNodes(thisLayer);
          count = imageAnnotationData.getAnnotationSize(thisLayer);
          if (count>1)
          {   for (int node=1; node<count; node++)
              {  picture = (PictureData)nodes[node].getObject();
                 if (picture!=null)  {   picture.rewrite(MAX_PICTURE);    }
              }
          }
      }
   }      // End save()
}         // End of LessonsFillBlanks class.
