/*
 * PicturesSoundData.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.lesson.picturesV4;

import java.awt.*;
import java.util.*;
import org.acorns.data.*;



public class LessonPicturesData extends UndoRedoData
{
   public static final long serialVersionUID = 1;
	
   Hashtable<Point,PicturesSoundData> data;

   // Constructor for non-sound data.
   public LessonPicturesData(Hashtable<Point,PicturesSoundData> data)
	  {  this.data = data; }
	
   // Method needed to complete the UndoRedoData class.
   public Hashtable<Point,PicturesSoundData> getData() {return data;}
}
