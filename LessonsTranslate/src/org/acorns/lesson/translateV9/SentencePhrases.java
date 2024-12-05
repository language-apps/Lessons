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
package org.acorns.lesson.translateV9;

import java.awt.Point;
import java.util.*;

import org.acorns.data.*;
import org.acorns.lesson.AcornsProperties;
import org.acorns.visual.*;

/** Class to maintain the phrases for a single sentence of the lesson */
public class SentencePhrases
{
   private LessonsTranslate  lesson;
   private ColorScheme       colors;

   private SoundData sentence;
   private ArrayList<Phrase> phrases;
   private String            language;
   
   private ArrayList<Integer> free = new ArrayList<Integer>();
   
   /** Constructor to initialize the magnets for a particular sentence */
   public SentencePhrases
           (LessonsTranslate lesson, ColorScheme colors)
   {  
      this.colors   = colors;
      this.lesson = lesson;
      
      phrases = new ArrayList<Phrase>();
   }
   
    /** Method to reset the game and create a new set of magnets
     * 
     * @return next True if we should change the sentence being processed
     */
   public ArrayList<Phrase> reset(boolean next)
   {
	  SoundData sentence;
	  if (next)
		  sentence = getNextSentence();
	  else
		  sentence = getCurrentSentence();

      phrases = new ArrayList<Phrase>();

      // Get the current sentence
      String[] soundText = sentence.getSoundText();
      language = soundText[SoundData.LANGUAGE];
      String[] words = soundText[SoundData.NATIVE].split("\\s+");

      boolean options[] = lesson.getOptions();
      if (!options[AcornsProperties.SELECT])
      {
          language = "";
          words = soundText[SoundData.GLOSS].split("\\s+");
      }
      int count = words.length;

      // Move the words starting with '+' to the back
      ArrayList<String> plusPhrases = new ArrayList<String>();
      ArrayList<String> allPhrases = new ArrayList<String>();
      
      // Remove hanging dashes
      for (int k=0; k<count; k++)
      {
	   	  if (words[k].charAt(0) == '+')
	   	  {
	   		  if (plusPhrases.isEmpty())
	   		  {
	   			  words[k] = words[k].replaceAll("^\\+\\-", "+");
	   	      	  words[k] = lesson.formatControlString(words[k], true);
	   		  }
	    	     
	   		  words[k] = words[k].replaceAll("\\.{3}", "");
	 		  plusPhrases.add(words[k]);
	   	  }
	   	  else  
	   	  {
	   		  if (allPhrases.isEmpty())
	   		  {
	   			  words[k] = words[k].replaceAll("^[-]","");
	   	      	  words[k] = lesson.formatControlString(words[k], true);
	   		  }
	   		  allPhrases.add(words[k]);
	   	  }
      }
      
      int size = plusPhrases.size();
      String word;
      if (size>0)
      {
	   	  word = plusPhrases.get(size-1);
	   	  word = word.replaceAll("[-]$", "");
      	  word = lesson.formatControlString(word, false);
	   	  plusPhrases.set(size-1,  word);
      }

      size = allPhrases.size();
      if (size>0)
      {
	   	  word = allPhrases.get(size-1);
	   	  word = word.replaceAll("[-]$", "");
      	  word = lesson.formatControlString(word, false);
	   	  allPhrases.set(size-1,  word);
      }

      allPhrases.addAll(plusPhrases);
     
      for (int k=0; k<count; k++)
      {  
    	  word = allPhrases.get(k);
    				
    	  if (word.replaceAll("[-]","").length()==0) // Don't add phrases that are empty
    		  continue;
    	  if (word.replaceAll("[\\+\\-\\.\\[\\]\\(\\) ]*", "").length()==0)
    		  continue;

    	Phrase phrase = new Phrase(this, word, colors, language, new ArrayList<Phrase>(), new Point(k,k));
    	phrases.add(phrase);
     }
      
     String prefix, suffix, center;
     int p = phrases.size()-1;
     while (p>1)
     {
    	 prefix = phrases.get(p-2).getControlString();
    	 center = phrases.get(p-1).getControlString();
    	 suffix = phrases.get(p).getControlString();
  
     	 if (suffix.startsWith("+"))
     	 {
     		 p--;
     		 continue;
     	 }

     	 if (suffix.startsWith("...") &&
    			 prefix.endsWith("...") && !center.contains("..."))
    	 {
    		 if (prefix.startsWith("...")) prefix = "-" + prefix.substring(3);
    		 
    		 int len = suffix.length()-3;
    		 if (suffix.endsWith("...")) suffix = suffix.substring(0, len) + "-";
    		 
    		 prefix = prefix.replaceAll("\\.{3}", "");
    		 center = center.replaceAll("\\.{3}", "");
    		 suffix = suffix.replaceAll("\\.{3}", "");
    		 if (suffix.replaceAll("[-]", "").length()==0)
    		 {   // only a dash left
    			 phrases.remove(phrases.get(p));
    			 p--;
    			 continue;
    		 }
    		 		
    		 phrases.get(p-2).setControlString(prefix + "..." + suffix, false);
    		 
    	     phrases.remove(p);
    	     p -= 2;
    	 }
    	 else 
    	 {
    		 suffix = suffix.replaceAll("\\.{3}", "");
        	 phrases.get(p).setControlString(suffix, false);
    	 }
    	 p--;
      }

      // Set the ranges for each magnet for easy locating without a search
      Phrase phrase;
      for (p = 0; p<phrases.size(); p++)
      {
    	 phrase = phrases.get(p);
   		 phrase.setRange(new Point(p, p));
      }
      return phrases;
   }
 
   /** Return ArrayList of the phrases in the current sentence */
   public ArrayList<Phrase> getPhrases(boolean next)
   {
	   if (phrases.isEmpty())
		   reset(next);
	   return phrases;
   }

   /** Get total phrase count */
   public int size()
   {
	   return phrases.size();
   }

   /** Make a new phrase with the modified control string
    * 
    * @param phrase The phrase to alter
    * @param controlString the new control string
    * @return The newly generated phrase
    */
   public Phrase makePhrase(Phrase phrase, String controlString)
   {
	  ArrayList<Phrase> nestedPhrases = new ArrayList<Phrase>();
	  ArrayList<Phrase> phraseList = phrase.getEmbeddedPhrases();
	  nestedPhrases.addAll(phraseList);
	  if (phraseList.isEmpty())  nestedPhrases.add(phrase);
	  
	  Point range = phrase.getRange();
	  
	  Phrase newPhrase = new Phrase(this, controlString, colors, language, nestedPhrases, range);
	  return newPhrase;
   }
   
   /** Method to attempt to join two magnets.
    *
    * @param first The first magnet to join
    * @param second The second magnet to join
    * @return The joined magnet (the two originals are removed)
    */
   public Phrase join(Phrase first, Phrase second)
   {
      Point firstRange = first.getRange();
      Point secondRange = second.getRange();
      
      if (first.isPlus() != second.isPlus()) // Both or neither phrases must start with +
    	  return null;
      
	  int alternatePosition = secondRange.x - 1; // Position of phrase before second
	  if (alternatePosition>=0)
	  {
		  Phrase phrase = phrases.get(alternatePosition);
		  if (phrase.getText().equals(first.getText()))
		  {
			  firstRange = phrase.getRange();
		  }
	  }
      
	  if (firstRange.y + 1 != secondRange.x)
	  { return null; }

      String firstText = first.getControlString();
      String secondText = second.getControlString();
      String newText = "";

      if (firstRange.x > 0 && !firstText.contains("..."))
      {
    	  Phrase previous = phrases.get(firstRange.x - 1);
    	  if (previous.getControlString().contains("..."))
    		  return null;
      }
      
      if (firstText.contains("..."))
      {
    	  String[] splitText = firstText.split("\\.{3}");
    	  if (splitText.length < 2) return null;
    	  
    	  splitText[0] = lesson.formatControlString(splitText[0], false);
    	  splitText[1] = lesson.formatControlString(splitText[1], true);
    	  
    	  secondText = secondText.replaceAll("^[-]*", "");
    	  secondText = secondText.replaceAll("[-]*$", "");
     	  newText = splitText[0] + secondText + splitText[1];
    	  newText = lesson.formatControlString(newText, true);
      }
      else
      {
    	  secondText = secondText.replaceAll("^\\+","");
    	  boolean firstEnd = firstText.endsWith("-"), secondStart = secondText.startsWith("-");
    	  if ( !(firstEnd || secondStart)) { return null; }
    	  
    	  firstText = lesson.formatControlString(firstText, false);
    	  firstText = firstText.replaceAll("[-]$", "");
    	  secondText = lesson.formatControlString(secondText, true);
    	  secondText = secondText.replaceAll("^[-]", "");
    	  newText = firstText + secondText;
    	  newText = newText.replaceAll("[-]{2}","");
	      newText = newText.replaceAll("\\([^\\) ]*\\)", "");
	      newText = newText.replaceAll("[\\[\\]]", "");
      }
      
	  ArrayList<Phrase> nestedPhrases = new ArrayList<Phrase>();
	  ArrayList<Phrase> firstPhrases = first.getEmbeddedPhrases();
	  ArrayList<Phrase> secondPhrases = second.getEmbeddedPhrases();
	  nestedPhrases.addAll(firstPhrases);
	  nestedPhrases.addAll(secondPhrases);
	  if (firstPhrases.isEmpty())  nestedPhrases.add(first);
	  if (secondPhrases.isEmpty()) nestedPhrases.add(second);
	  
	  Point newRange = new Point(firstRange.x, secondRange.y);
	  
	  Phrase newPhrase = new Phrase(this, newText, colors, language, nestedPhrases, newRange );
	  return newPhrase;
   }	// End of join method
    
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
   
   // Call method that was moved to the lesson category level
   public String formatControlString(String controlString, boolean left)
   {
	   return lesson.formatControlString(controlString, left);
   }

   // Call method that was moved to the lesson category level
   public String getPhrasesForDisplay(String sentence, boolean finish, boolean first, boolean last)
   {
	   return lesson.getPhrasesForDisplay(sentence, finish, first, last);
   }
  
}     // End of SentenceMagnets class
