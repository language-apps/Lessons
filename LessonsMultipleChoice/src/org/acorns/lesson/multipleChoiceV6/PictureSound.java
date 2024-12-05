/**
 * SelectPictureSound.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.lesson.multipleChoiceV6;

import java.util.*;
import org.acorns.data.*;

public class PictureSound
{  private int[] freePictures;
	  private int   pictureCount;
	
	  private int[][] freeSounds;
	  private int[]   soundCounts;
	
	  // Array of pictures and sounds.
	  PictureChoice[]     pictures;
      Vector<SoundData>[] sounds;
		
	  // Index to last selected picture.
	  private int     selection;
	  
	  // Random object to choose pictures
	  private Random  random;
	
	/** Constructor to create object of available picture and sound
	 *  for multiple choice lessons
	 */
   @SuppressWarnings("unchecked")
public PictureSound(LessonsMultipleChoice lesson)
	{ 
	   // Create pointers to pictures and the attached sounds.
	   pictures = lesson.getActivePictureData();
       int size = lesson.getActiveChoices();
       sounds = new Vector[size];
	   freePictures = new int[size];
	   freeSounds   = new int[size][];
       soundCounts  = new int[size];
       
       random = new Random();
	
       createFreePictures();
	   for (int i=0; i<size; i++)
	   {
		   sounds[i] = pictures[i].getQuestions(lesson.getLayer()).getVector();
		   freeSounds[i] = new int[sounds[i].size()];
		   createFreeSounds(i);
	   }
	}
	
	/** Create array of free pictures that 
	 *  are useable in multiple choice lessons.
	 */
	private void createFreePictures()
	{  for (int i=0; i<freePictures.length; i++) {freePictures[i] = i; }
		  pictureCount = freePictures.length;
	}
	
	/** Create array of free sounds going with each picture
	 *  useable in multiple choice lessons.
	 */
	private void createFreeSounds(int index)
	{ 
		int size = freeSounds[index].length;
		for (int i=0; i<size; i++) { freeSounds[index][i] = i; }
		soundCounts[index] = size;
	}
	 
	 /** select a picture from the array of pictures
	  *  @return picture and sound object.
	  */
	 public PictureChoice selectPicture()
	 {  
         if (pictureCount == 0) createFreePictures();
		 int which = random.nextInt(pictureCount--);
		 selection = freePictures[which];
		 freePictures[which] = freePictures[pictureCount];
		 return pictures[selection];	 
	 }
	 
	 public int getSelection() { return selection; }
	  
	 /** Select a sound recording attached to a given picture
	  *  @return Sound recording object.
	  */
	 public SoundData selectSound(int selection)
	 {	 
		 if (soundCounts[selection]==0) createFreeSounds(selection);
		 int soundSelect = random.nextInt(soundCounts[selection]--);
		 int sound = freeSounds[selection][soundSelect];
		 freeSounds[selection][soundSelect] 
                        = freeSounds[selection][soundCounts[selection]];
		 return (SoundData)sounds[selection].elementAt(sound);
	 }
  
}  // End of PictureSound.
