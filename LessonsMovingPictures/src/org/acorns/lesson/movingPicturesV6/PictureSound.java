/**
 * SelectPictureSound.java
 *
 *   @author  HarveyD
 *   @version 5.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.lesson.movingPicturesV6;

import java.util.*;
import java.awt.*;

import org.acorns.data.*;
import org.acorns.widgets.*;

/** Class to control which pictures are displayed */
public class PictureSound
{  
   public static final int DISPLAY_COUNT = 4;
   public static final int DISPLAY_SIZE  = 100;

   private int[]   freePictures;
	  private int     pictureCount;

   // Array of pictures and sounds.
	  PictureChoice[] pictures;
		
	  // Index to last selected picture.
	  private int[]   selectedPictures;
   int             selectedPictureCount;
	
	/** Constructor to create object of available picture and sound
	 *  for multiple choice lessons
	 */
   public PictureSound(PictureChoice[] pictures, int choices)
	  {
      // Get list of pictures that can display
	     this.pictures = pictures;

      // Create free list of pictures
      freePictures = new int[choices];
      pictureCount = freePictures.length;
		    for (int i=0; i<freePictures.length; i++)
      {  freePictures[i] = i; }

      selectedPictureCount = 0; // Nothing selected yet
      selectedPictures = new int[DISPLAY_COUNT];
   }

 /** select a picture from the array of pictures
	  *  @return picture and sound object.
	  */
	 public ChoiceButton selectPicture()
	 {  
     // Pull a picture out of the free list

     int which = (int)(Math.random() * pictureCount--);
     int selection = freePictures[which];
     freePictures[which] = freePictures[pictureCount];

     // Add selected picture to the selection list
     selectedPictures[selectedPictureCount++] = selection;
     ChoiceButton button = pictures[selection].getButton();
     button.resizeButton(new Dimension(DISPLAY_SIZE, DISPLAY_SIZE));
		   return button;
	 }

  /** Method to switch one picture with another
   *
   * @param button to remove
   * @return new picture to be displayed
   */
  public ChoiceButton removePicture(ChoiceButton choice)
  {
      int selection;
      ChoiceButton button;

      for (int which=0; which < selectedPictureCount; which++)
      {
          selection = selectedPictures[which];
          button = pictures[selection].getButton();
          if (button==choice)
          {
              selectedPictures[which]
                      = selectedPictures[--selectedPictureCount];
              freePictures[pictureCount++] = selection;
              return button;
          }
      }
      return null;
  }

  /** Echo a sound to display and return the text for it */
  public String[] selectAudio(PicturesSoundData sounds)
  {
      Vector<SoundData> soundVector = sounds.getVector();

      // Randomly pick one to echo.
      int size = soundVector.size();
      int selection = (int)(Math.random() * size);

      SoundData sound = soundVector.get(selection);
      String[] text = sound.getSoundText();
      if (sound.isRecorded())  sound.playBack(null, 0, -1);
      return text;
  }
	 
}  // End of PictureSound.
