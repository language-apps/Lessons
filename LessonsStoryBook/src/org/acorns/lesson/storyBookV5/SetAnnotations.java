/**
 * SetAnnotations.java
 * @author HarveyD
 * @version 5.00 Beta
 *
 * Copyright 2009-2015, all rights reserved
 */

package org.acorns.lesson.storyBookV5;

import java.awt.*;
import java.awt.image.*;

import org.acorns.data.*;
import org.acorns.language.*;

/** Class to set the annotations for the playback option */
public class SetAnnotations 
{
    private int[] offsetsInData;
    
    /** Constructor to set the annotation object */
    public SetAnnotations(LessonsStoryBook lesson, int layer)
    {
        AnnotationData data = lesson.getAnnotationData();
        data.setAnnotationLevel(layer-1);
        AnnotationNode[] nodeList = data.getAnnotationNodes();

        // Remove null nodes from list
       	int size = nodeList.length;
       	int offset = 0;
       	String text;
       	for (int i = size-1; i>0; i--)
       	{
       		if (nodeList[i]==null) continue;
       		text = nodeList[i].getText();
       		if (text==null || text.length()==0)
       		{
           		offset = (int)nodeList[i].getOffset();
           		if (offset>0)
           			data.delete(offset);
       		}
       	}

       	// Determine if the text changed
        String annotations = data.getAnnotationText(layer-1);
        String story = lesson.getTextEntered(layer);
        String compareString = story.replaceAll("[ \\t\\f\\r]+", " ");
        boolean same = annotations.trim().replaceAll("[ \\t\\f\\r]+", " ").equals(compareString.trim());
        size = data.getAnnotationCount();

        String[] words;
        if (same) {
             words = new String[size];     
             int start = (nodeList[0]==null || nodeList[0].getText().length()==0) ? 1 : 0;
             for (int i=start; i<=size; i++)
             {
           		words[i-start] = nodeList[i].getText().replaceAll("\\\\n+", "");
             }
            	
        } else {
        	String editStory = story.replaceAll("\\r+", "");
        	editStory = editStory.replaceAll("(\\n+)", "$1 ");
        	editStory = editStory.replaceAll("\\n", "\\\\n");
        	words = editStory.split("\\s+");
        	
        }
        
        // Create offsets to words in the text entered data
    	offsetsInData = new int[words.length];

        // Get the font metrics being used
        String language = data.getKeyboard();
        KeyboardFonts kbFonts = KeyboardFonts.getLanguageFonts();

        BufferedImage image
             = new BufferedImage(800, 250, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();
        size = lesson.getColors().getSize();
        graphics.setFont(kbFonts.getFont(language).deriveFont(Font.PLAIN, size));
        FontMetrics metrics = image.getGraphics().getFontMetrics();

        offset = 0;
        offsetsInData = new int[words.length];
        int[] offsetValues = new int[words.length + 1];
        String word;
        for (int i=0; i<words.length; i++)
        {   
        	word = words[i].trim().replaceAll("\\\\n", "");
        	offsetValues[i+1]
                  = offsetValues[i] + metrics.stringWidth(word);

            offsetsInData[i] = story.indexOf(word, offset);
            offset = offsetsInData[i] + word.length();
        }

        // Recreate the annotation data if the text has changed 
        // Scale the word sizes to the position in the sound wave
        if (!same)
        {
        	int lastOffset = offsetValues[words.length];
        	int frames = data.getFrames();
        	for (int i=1; i<offsetValues.length; i++)
        	{
        		offsetValues[i]
                    = (int)(1.0 * offsetValues[i] / lastOffset * frames);
        	}

        	// Create the annotation list
        	Point point = new Point(0, Integer.MAX_VALUE);
        	data.delete(point); // Clear all the existing annotations.

        	for (int i=0; i<offsetValues.length - 1; i++)
        	{   point = new Point(offsetValues[i], offsetValues[i+1]);
            	data.insertAnnotation(words[i], point);
        	}
        }
    }       // End of constructor

    /** Method to return the word offsets in the story text */
    public int[] getStoryOffsets()
    { return offsetsInData; }
}           // End of SetAnnotations class
