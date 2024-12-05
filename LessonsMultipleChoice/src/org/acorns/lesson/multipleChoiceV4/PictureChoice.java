/**
 * PictureChoice.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.lesson.multipleChoiceV4;

import java.io.*;
import java.awt.*;
import org.acorns.data.*;

/** Lesson class that contains all information about a single picture */
public class PictureChoice implements Serializable, Cloneable
{
   public final static long serialVersionUID = 1;

   // The following data saves to and loads from disk.
   private PictureData data;
   private Dimension buttonSize;
   private PicturesSoundData[] questions;
   private int type;

   org.acorns.data.PictureChoice convert()
   {
       return new org.acorns.data.PictureChoice
               (data, buttonSize, questions, type);
   }
       
}  // End PictureChoice