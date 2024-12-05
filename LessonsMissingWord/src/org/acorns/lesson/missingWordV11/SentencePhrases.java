package org.acorns.lesson.missingWordV11;
/**
 * SentencePhrases.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2019, all rights reserved
 *   
 *   Maintain the list of phrases that make up a sentence
 */


import java.util.*;

import org.acorns.data.*;
import org.acorns.visual.*;

/** Class to maintain the phrases for a single sentence of the lesson */
public class SentencePhrases
{
   private LessonsMissingWord lesson;
 
   private SoundData sentence;
   
   private ArrayList<Integer> free = new ArrayList<Integer>();
   
   /** Constructor to initialize the magnets for a particular sentence */
   public SentencePhrases
           (LessonsMissingWord lesson, ColorScheme colors)
   {  
      this.lesson = lesson;
   }
   
    /** Method to reset the game and create a new set of magnets
     * 
     * @return next True if we should change the sentence being processed
     */
   public void reset(boolean next)
   {
	  if (next)
		  sentence = getNextSentence();
	  else
		  sentence = getCurrentSentence();
   }

   /* Get the next sentence to process */
   public SoundData getNextSentence()
   {
	   int index, choice;
	   
	   Vector<SoundData> sentences = lesson.getCategorySentenceVector();
	   int size = sentences.size();
	   
       // Get number of sentences available for this category
	   ArrayList<Integer> ordered = new ArrayList<Integer>();
	   int sentenceNo = -1;
	   
	   // If there are no sentences, create a scramble the list with all of them
	   if (free.size()==0)
	   {
		   ordered.clear();
		   for (choice = 0; choice<size; choice++)
			  ordered.add(choice);
		   
		   while (ordered.size()>0)
		   {
			   index = (int)(Math.random() * ordered.size());
			   sentenceNo = ordered.get(index);
			   ordered.remove(index);
			   free.add(sentenceNo);
		   }
	   }

	   sentenceNo = free.get(0);
	   free.remove(0); 
	   return sentence = sentences.get(sentenceNo);
   }

   // Return true if there are more lessons in the category, zero otherwise
   public boolean hasMoreSentences()
   {
	   return !free.isEmpty();
   }

   // Place current sentence at the end of the free list
   public void retryCurrentSentence()
   {
	   Vector<SoundData> sentences = lesson.getCategorySentenceVector();
	   SoundData thisSentence = getCurrentSentence();
	   int sentenceNo = sentences.indexOf(thisSentence);
	   free.add(sentenceNo);
	   sentence = null;
   }
 
   /** Get the current sentence being used in the lesson */
   public SoundData getCurrentSentence()
   {
	   if (sentence==null)
		   getNextSentence();
	   
	   return sentence;
   }
   
   public void resetCategory(boolean repeat) 
   {
	   sentence = null;
	   free.clear();
	   if (!repeat) lesson.resetCategory();
   }
   
   
   /** Get ready to process next category */
   public SentenceAudioPictureData getNextCategory()
   {
	   sentence = null;
	   free.clear();
	   return lesson.getNextCategory();
   }
  
}     // End of SentenceMagnets class
