/*
 * LessonsPictures.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.lesson.picturesV4;

import java.awt.*;

import javax.swing.*;

import java.io.*;
import java.net.*;
import org.w3c.dom.*;
import java.util.*;
import javax.sound.sampled.*;

import org.acorns.data.*;
import org.acorns.lesson.*;
import org.acorns.language.*;
import org.acorns.visual.*;

/**
 * 
 * Note: in the hash table, there points are normally used to position
 * 			acorns and links to lessons. However, there are a number of
 * 			entries with data reserved for special purposes. These follow:
 * 
 * 			(1, -1) Number of acorns points, if missing then 50 is used
 * 			(1, -2) background color RGB value
 * 			(1, -3) foreground color RGB value
 * 
 * 	These were added so we wouldn't have incompatibility between ACORNS
 *  versions.
 *
 */
public class LessonsPictures extends Lesson implements Serializable
{
   private Hashtable<Point,PicturesSoundData> sounds;
   private PictureData picture;

   private transient int copyLayer = -1;
   private transient PicturesPanel picturesPanel;
   private transient int acornsPoints = -1;
   private transient ColorScheme colors;

   public transient static final long serialVersionUID = 1;
     
   //--------------------------------------------------------
   // Note: PicturesPlayPanel and PicturesSetUpLabel classes doesn't use this constant.
   //       They probably should. They hardcode the constant to 50.
   //--------------------------------------------------------
   public static int DATA = 13;          // Offset to data in print records.
   public static int ACORNSPOINTS = 200; // Current number of acorn points
   public static int DEFAULTPOINTS = 50; // Default number of acorns points

   //--------------------------------------------------------
   // Constructor for initially instantiating the lesson.
   //--------------------------------------------------------
   public LessonsPictures()
   {  
      super("Pictures and Sounds;"
              + LanguageText.getMessage("LessonsPictures",13));
      picture       = null;
      picturesPanel = null;
      sounds = new Hashtable<Point,PicturesSoundData>();
      copyLayer = -1;
   }
   
   public LessonsPictures(int scaleFactor, int angle, byte[] bytes)
                                                      throws IOException
   {
       super("Pictures and Sounds;"
               + LanguageText.getMessage("LessonsPictures",13));
       picture = new PictureData(bytes, null);
       picture.setAngle(angle);
       picture.setScale(scaleFactor);
       picturesPanel = null;
       sounds = new Hashtable<Point,PicturesSoundData>();
       copyLayer = -1;
   }
   
   //--------------------------------------------------------
   // Methods to adjust scale factor for the image.
   //--------------------------------------------------------
   public int getScaleFactor() 
   {   if (picture==null) return 100;
       return picture.getScale();
   }

   public boolean setScaleFactor(int value)
   { if (picture!=null) return picture.setScale(value);
     else return false;
   }
 
   //--------------------------------------------------------
   // Methods to adjust scale factor for the image.
   //--------------------------------------------------------
   public int getAngle() 
   {  if (picture == null) return 0;
       return picture.getAngle();
   }

   /** Get the number of places to put acorns on screens */
   public int getPoints()
   {
      if (sounds.isEmpty())
      {
          PicturesSoundData data = new PicturesSoundData("" + ACORNSPOINTS);
          sounds.put(new Point(1,-1), data);
          acornsPoints = ACORNSPOINTS;
      }

       if (acornsPoints<=0)
       {
          try
          {
             PicturesSoundData data = sounds.get(new Point(1,-1));
             acornsPoints = Integer.parseInt(data.getText());
             
          }
          catch (Exception e)
          {  acornsPoints = DEFAULTPOINTS; }
       }
       return acornsPoints;
   }
   
   //--------------------------------------------------------
   // Method to return the panel for displaying lesson data.
   //--------------------------------------------------------
   public JPanel[] getLessonData()
   {   
       JPanel[] panels = new JPanel[2];

       if (sounds == null) sounds = new Hashtable<Point,PicturesSoundData>(); 
       if (picture == null)  {return panels; }
       picturesPanel = new PicturesPanel(Lesson.SETUP, this);
       panels[AcornsProperties.DATA]  = picturesPanel; 
       panels[AcornsProperties.CONTROLS] = picturesPanel.getControlPanel();
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
   public SoundData getSoundData(int image, int layer, Point point, int index)
   {
       point = new Point(point.x * PicturesPanel.KEY_LAYERS + layer, point.y);
       PicturesSoundData pictureSound = (PicturesSoundData)sounds.get(point);
       if (pictureSound==null) return null;
       Vector<SoundData> vector = pictureSound.getVector();
       if (vector==null) return null;
       if (vector.size()<=index) return null;
       return vector.get(index);
   }   
   //--------------------------------------------------------------------------
   //  Methods to retrieve, insert or remove a PictureChoice from the lesson.
   //--------------------------------------------------------------------------
   public PictureData getPictureData(String errMsg)
   { return getPictureData(-1);}
   
   public PictureData getPictureData(int pictureNum) 
   {  return picture;  }
   
   //------------------------------------------------------------
   //  Methods to insert or remove a picture into/from the lesson.
   //------------------------------------------------------------
    public void insertPicture(URL url, int scaleFactor, int angle) 
                                               throws IOException
   {
      picture = new PictureData(url, null);

      Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension picSize = picture.getSize();

      if (picSize.width > size.width || picSize.height > size.height)
      {
          int scale = 100 * size.width / picSize.width;
          int scaleY = 100 * size.height / picSize.height;
          if (scaleY < scale) scale = scaleY;
          if (scale < scaleFactor) scaleFactor = scale;
      }

      picture.setAngle(angle);
      picture.setScale(scaleFactor);
      setDirty(true);
   }
   
   public void removePicture(int pictureNum) 
   {  
      picture       = null;
      picturesPanel = null;
      sounds        = null;
      resetUndoRedo();
      setDirty(true);
   }
   
   //------------------------------------------------------------
   // Method to execute a lesson.
   //------------------------------------------------------------
   public JPanel play()
   {
      if (picture==null) return null;
      
      PicturesPanel picturesPanel = new PicturesPanel(Lesson.PLAY, this);
      return picturesPanel;
   }
   
   //--------------------------------------------------------
   // Method to determine if a lesson is playable.
   //--------------------------------------------------------
   public String isPlayable(int level) 
   {  
       if (picture==null) return LanguageText.getMessage(this, 11);
       if (level<=1) return null; // Initial layer doesn't need acorns.

      Point point, key;
      PicturesSoundData sound;
      Enumeration<Point>  pointList = sounds.keys();
      while (pointList.hasMoreElements())
      {
         key    = (Point)pointList.nextElement();
         if (key.x==1 && key.y<0) continue;

         point  = new Point(key.x/PicturesPanel.KEY_LAYERS, key.y);
         sound = sounds.get(key);

         if (sound==null||point.x<0||point.y<0
                 ||point.x>=getPoints()||point.y>=getPoints())
         {  sounds.remove(key); }
         else  if (key.x % PicturesPanel.KEY_LAYERS==level) return null;
      }
      return LanguageText.getMessage(this, 12);
   }
   
   /** Method to set options for play mode
    * 
    * @param options scale factor for picture in index 0
    */
   public void setPlayOptions(int[] options) 
   {  if (options.length>=1)
      {   if (options[0]>=PictureData.MIN_SCALE
                   && options[0]<=PictureData.MAX_SCALE)
              setScaleFactor(options[0]); 
      }
   }
   
   /** Method to return play mode options
    * 
    * @return array of options (scale factor in index 0).
    */
   public int[] getPlayOptions() 
   {  
       int[] options = new int[1];
      options[0] = getScaleFactor(); 
      return options;
   }
   
   //--------------------------------------------------------
   // Method to write images and sounds in a standard format to a sub-directory.
   //--------------------------------------------------------
   public boolean export(File directoryName, String[] formats, int number)
                                    throws Exception
   {
      if (picture!=null)
      {
          writeImage(picture, directoryName, number, 0
                       , formats[AcornsProperties.IMAGE_TYPE]);
          
          // Variables for data for each point in the hash table.              
          Point             key, point;
          Enumeration<Point>       pointList = sounds.keys();
          PicturesSoundData pointData;
          Vector<SoundData> soundVector;
          SoundData         sound;
          int               layerNo;
			 
          // For each sound recorded, create a file name and write it to disk.
          while (pointList.hasMoreElements())
          {  
             key       = (Point)pointList.nextElement();
             if (key.y<0) continue;

             pointData = sounds.get(key);
             if (pointData==null)  { sounds.remove(key); continue; }

             if (!pointData.isSound()) continue;

             // Get the layer number.    
             layerNo  = key.x % PicturesPanel.KEY_LAYERS;
           
             // Convert the point to percentage from top left of display.
             point  = new Point(key.x/PicturesPanel.KEY_LAYERS, key.y);
         
             soundVector = pointData.getVector();
             for (int i=0; i<soundVector.size(); i++)
             {
                sound = (SoundData)soundVector.elementAt(i);
                if (sound.isRecorded())
                   writeSound(sound, directoryName
                           , number, 0, layerNo, new Point(point)
                           , i, formats[AcornsProperties.SOUND_TYPE]);
             }  // End of for to loop through sound vector.
         }          // End of while more points.
      }             // End of if (image != null).
      return true;
   }                    // End of export method.
	
   //--------------------------------------------------------
   // Method to import a record to add the the lesson.
   //--------------------------------------------------------
   public void importData(int layer,Point point,URL file,String[] data,int type)
	                       throws IOException, UnsupportedAudioFileException
   {   		
       if (type==AcornsProperties.LINK && point.x==0 && point.y==0)
       { link = data[0]; return;  }
       
       if (type==AcornsProperties.PARAM)
       {
           try  { acornsPoints = Integer.parseInt(data[1]); }
           catch (NumberFormatException e) {}

           PicturesSoundData points
                   = new PicturesSoundData("" + ACORNSPOINTS);
           sounds.put(new Point(1,-1), points);
           return;
       }
       
       if (type == AcornsProperties.FONT)
       {
    	  getColorScheme();
          if (!data[0].equals(""))
              colors.changeColor(getColor(data[0]), true);
          if (!data[1].equals(""))
              colors.changeColor(getColor(data[1]), false);
          if (!data[2].equals(""))
              colors.setSize(Integer.parseInt(data[2]));
       }
     
       if (type!=AcornsProperties.SOUND && type !=AcornsProperties.LINK)
       {   return; }

       if (sounds==null) sounds = new Hashtable<Point,PicturesSoundData>();

       Point key
            = new Point(point.x * PicturesPanel.KEY_LAYERS + layer, point.y);
       if (point.y<0) key = point;

       PicturesSoundData picturesSounds;
       Vector<SoundData> soundVector;
       SoundData         sound;

       // Create a new record if the key doesn't exist.
       
       if (type == AcornsProperties.SOUND)
       {
          // Decompose file name into section.
          sound = new SoundData(data);
          if (sounds.containsKey(key))
          {   picturesSounds = sounds.get(key);
	      if (!picturesSounds.isSound()) throw new IOException();
	          soundVector = picturesSounds.getVector();
 	      soundVector.add(sound);
          }
          else
          {  picturesSounds = new PicturesSoundData();
		     soundVector = picturesSounds.getVector();
		     soundVector.add(sound);
	   	     sounds.put(key, picturesSounds);
          }
          if (file!=null)
          {   String fileName = file.toString();
              file = makeURL(fileName);
              try  { sound.readFile(file); }
              catch (Exception e)
              {  int nameIndex   = fileName.lastIndexOf('/');
                 int suffixIndex = fileName.lastIndexOf('.');
                 if ((suffixIndex - nameIndex ==19) && nameIndex>0)
                 {  fileName = fileName.substring(0,nameIndex+5) + 
                                       fileName.substring(nameIndex+8);
                    file = makeURL(fileName);                 
                 }
                 sound.readFile(file);
              }
          }
       }
       else
       {   if (sounds.containsKey(key))
           {  picturesSounds = sounds.get(key);
		      if (picturesSounds.isSound()) throw new IOException();
	              picturesSounds.setText(data[0]); 
           }
           else  
           {   picturesSounds = new PicturesSoundData(data[0]);
               sounds.put(key, picturesSounds);
           }
       }
   }      // End of import method.

   /** Method to set the layer to be copied */
   public void setCopyLayer(int copyLayer)
   {this.copyLayer = copyLayer; }

   // Method to get the layer to be copied */
   public int getCopyLayer()  { return copyLayer; }
  
   // Method to create a URL from a string. It can be local or remote.
   private static URL makeURL(String file) throws MalformedURLException
   {   URL url = null;
       try { url = new URI(file).toURL(); }
       catch (Exception e)
       {  url =  new File(file).toURI().toURL(); }
       return url;       
   }
       
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
      if (picture==null) return true;

      getPoints();  // Set the number of points if not already set.
      int lessonNo
               = Integer.parseInt(lessonNode.getAttribute("number")) -1;

      // Get name of picture
      if (extensions == null)
      {   extensions = new String[2];
          extensions[AcornsProperties.SOUND_TYPE] = "wav";
          extensions[AcornsProperties.IMAGE_TYPE] = "jpg";
      }

      String[] imageValues
               = {"0", "", ""+picture.getScale(), ""};
      Element imageNode = makeImageNode(document, directory
                            , extensions[AcornsProperties.IMAGE_TYPE]
                            , picture, imageValues, lessonNo);
      lessonNode.appendChild(imageNode);

      if (sounds==null) return true;

      String[] layerAttributes = {"value"};
      String[] layerValues = {"0", ""};
      String[] paramAttributes = new String[]{"points"};
      String[] paramValues = new String[] {"" + getPoints()};

      // Variables for data for each point in the hash table.
      Enumeration<Point>       pointList = sounds.keys();
      Point             key, point;
      int               layerNo;
      PicturesSoundData pointData;
      Vector<SoundData> soundVector;
      Element  pointNode, paramNode, layerNode;

      // Create node for foreground and background colors
      Element fontNode = makeFontNode(document, getColorScheme());
      lessonNode.appendChild(fontNode);

      // Create nodes for all of the possible layers (Don't append yet)
      Element[] layerElements = new Element[AcornsProperties.MAX_LAYERS];
      for (int i=0; i<layerElements.length; i++)
      {   layerValues[0] = "" + (i+1);
          layerElements[i] = makeNode(document, "layer"
                                , layerAttributes, layerValues);
      }           

      // Create sorted array of data. Sort by layer, point.x, and point.y.
      while (pointList.hasMoreElements())
      {   key  = (Point)pointList.nextElement();

          // Get the layer number.
          layerNo = (key.x % PicturesPanel.KEY_LAYERS);

          // Convert the point to percentage from top left of display.
          point  = new Point(key.x/PicturesPanel.KEY_LAYERS, key.y);

          if (key.y==-1) // See if this is the points entry
          {   paramNode = makeNode
                      (document, "param", paramAttributes, paramValues);
              lessonNode.appendChild(paramNode);
              continue;
          }
          if (key.y<0) continue;  // Skip color points

          pointData = sounds.get(key);
          if (pointData==null)  { sounds.remove(key); }
          else
          {   if (!pointData.isSound())
              {   pointNode = makeLinkPointNode
                                (document, point, pointData.getText());
                  layerElements[layerNo-1].appendChild(pointNode);
              }
              else
              {   // Consider all of the sounds for this point
                  soundVector = pointData.getVector();
                  layerNode = layerElements[layerNo-1];
                  makeListOfPointNodes( document, directory, soundVector
                               , point, layerNode
                               , extensions[AcornsProperties.SOUND_TYPE]
                               , lessonNo, layerNo, 0);
              }      // End if sound or link
          }          // End if soundData not null
       }             // End while more points

       for (int i=0; i<layerElements.length; i++)
       {   if (layerElements[i].getFirstChild() != null)
           {  imageNode.appendChild(layerElements[i]);   }
       }
       return true;
   }   // End of print() method

   
   /********* Method to convert between versions  *************/
   public Lesson convert(float version)  { return this; }
   
   //--------------------------------------------------------
   // Methods to respond to the redo and undo commands.
   //--------------------------------------------------------
   public Hashtable<Point,PicturesSoundData> 
           getSoundData() {return sounds;}
   public  UndoRedoData getData() 
   {  return new LessonPicturesData(copyData(sounds)); }
   
   public void redo(UndoRedoData dataRecord)
   {
      LessonPicturesData lessonPicturesData = (LessonPicturesData)dataRecord;
      sounds = lessonPicturesData.getData();
      if (picturesPanel != null) 
      {
         picturesPanel.repaint();
      }
   }
   public void undo(UndoRedoData dataRecord)
   {
      LessonPicturesData lessonPicturesData = (LessonPicturesData)dataRecord;
      sounds = lessonPicturesData.getData();
      if (picturesPanel != null) {  picturesPanel.repaint(); }
   }
   
   //------------------------------------------------------------
	  // Method to create clone of dialog data.
   //------------------------------------------------------------
   public Hashtable<Point, PicturesSoundData>copyData
           (Hashtable<Point, PicturesSoundData> data)
	  {
      Hashtable<Point,PicturesSoundData> oldData;
	    	oldData = new Hashtable<Point, PicturesSoundData>();
      if (data==null) data = new Hashtable<Point, PicturesSoundData>();
      
	     Enumeration<Point>       pointList = data.keys();
      PicturesSoundData sounds, oldSounds;
		    Vector<SoundData> soundVector, oldSoundVector;
		
      Point   key;
      while (pointList.hasMoreElements())
      {  
         key    = (Point)pointList.nextElement();
         sounds = data.get(key);
            
         if (sounds==null)  { data.remove(key); }
         else  
         {
	        if (sounds.isSound())  // Handle vector of sounds.
		    {
		       oldSounds      = new PicturesSoundData();
			   soundVector    = sounds.getVector();
			   oldSoundVector = oldSounds.getVector();
			
		       for (int i=0; i<soundVector.size(); i++)
			   {
			      oldSoundVector.addElement(soundVector.elementAt(i).clone());
			   }					
		    }
		    else  // Handle a link or total points.
		    {
		       oldSounds = new PicturesSoundData(sounds.getText());
		    }
	        oldData.put(key, oldSounds);
         }
      }  // End while more points.
		    return oldData;
   }  // End copyData()	
   
   /** Get color scheme object */
   public ColorScheme getColorScheme() 
   { 
	   Color background = new Color(130,130,130);
	   Color foreground = Color.white;
	   
	   if (colors==null)
	   {
           PicturesSoundData data = sounds.get(new Point(1,-2));
           if (data!=null) 
               background = new Color(Integer.parseInt(data.getText()));
           
           data = sounds.get(new Point(1,-3));
           if (data!=null) 
               foreground = new Color(Integer.parseInt(data.getText()));
    	   colors = new ColorScheme(background, foreground);
 	   }
	   return colors; 
   }    
   
   /** Method to save any data not yet saved */
   public void save()  
   {  
	  ColorScheme colorScheme = getColorScheme();
	  int bg = colorScheme.getColor(true).getRGB();
	  int fg = colorScheme.getColor(false).getRGB();
	  
	  sounds.put(new Point(1, -2), new PicturesSoundData("" + bg));
	  sounds.put(new Point(1, -3), new PicturesSoundData("" + fg));
      setDirty(true);
   }
   
   public void copyPaste(JButton button)
   {
 	   String[] msgs = LanguageText.getMessageList("commonHelpSets", 101);
       if (button.getName().equals(msgs[0]))
	   {
           setCopyLayer(getLayer());
           button.setBorder(BorderFactory.createLoweredBevelBorder());
       }
       if (button.getName().equals(msgs[1]))
       {
           int destinationLayer = getLayer();
           int sourceLayer      = getCopyLayer();
           
           button.setBorder(BorderFactory.createLoweredBevelBorder());
           String msg = null;
           if (sourceLayer<=0) msg = LanguageText.getMessage(this, 5);
           else if (sourceLayer==destinationLayer)
               msg = LanguageText.getMessage(this, 6);
                       
           if (msg!=null)  
           {
        	   JOptionPane.showMessageDialog(button, msg);
           }
           else
           {
              msg = LanguageText.getMessage
                      (this, 7, ""+sourceLayer, ""+destinationLayer);

              int answer = JOptionPane.showConfirmDialog
                 ( button, msg, LanguageText.getMessage(this, 8)
                 , JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE
                 , getIcon(AcornsProperties.ACORN, Lesson.ICON));

              if (answer!=JOptionPane.CANCEL_OPTION)
              {   copyPoints(sourceLayer, destinationLayer);
              }
           }
	   }
   }

   /** Method to copy points between layers
    *
    * @param sourceLayer The source layer number
    * @param destinationLayer The destination layer number
    */
   private void copyPoints(int sourceLayer, int destinationLayer)
   {
      Hashtable<Point, PicturesSoundData> sounds
              = getSoundData();
      Hashtable<Point, PicturesSoundData> newSounds
               = new Hashtable<Point, PicturesSoundData>();

      Point             oldKey, newKey;
      Enumeration<Point>       pointList = sounds.keys();
      PicturesSoundData oldPointData, newPointData;
      Vector<SoundData> oldVector,    newVector;
      SoundData         soundData;
      String[]          text;
      int               layer;

      while (pointList.hasMoreElements())
      {
         oldKey = (Point)pointList.nextElement();
         if (oldKey.x==-1 && oldKey.y==-1) continue;

         // Get the layer number.
         layer  = oldKey.x % PicturesPanel.KEY_LAYERS;
         if (layer == sourceLayer)
         {
             oldPointData = sounds.get(oldKey);
             if (oldPointData==null) { sounds.remove(oldKey); continue; }
             oldVector = oldPointData.getVector();

             newKey = new Point(oldKey.x, oldKey.y);
             newKey.x &= ~(PicturesPanel.KEY_LAYERS - 1);
             newKey.x |= destinationLayer;

             if (oldPointData.isSound())
             {
                 newPointData = new PicturesSoundData();
                 newVector = newPointData.getVector();

                 if (oldVector!=null)
                 {
                     for (int i=0; i<oldVector.size(); i++)
                     {
                         soundData = oldVector.get(i);
                         text = soundData.getSoundText();
                         newVector.add(new SoundData(text));
                     }
                 }
                 newSounds.put(newKey, newPointData);
             }
             else
             {
               newPointData = new PicturesSoundData(oldPointData.getText());
               newSounds.put(newKey, newPointData);
             }
         }  // End of if sourceLayer
      }     // End of while more points.

      // Insert new points into the original hash table.
      pointList = newSounds.keys();
      while (pointList.hasMoreElements())
      {
         oldKey = (Point)pointList.nextElement();
         oldPointData = newSounds.get(oldKey);
         sounds.put(oldKey, oldPointData);
      }
      setDirty(true);
     displayLesson();
   }  // End of copyPoints()
}     // End of LessonsPictures class.
