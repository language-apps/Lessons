/*
 * SentenceMagnets.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */

package org.acorns.lesson.magnetGameV7;

import java.util.*;

import org.acorns.data.*;
import org.acorns.visual.*;

/** Class to maintain the phrases for a single sentence of the lesson */
public class SentencePhrases
{
   private SentenceAudioPictureData  sentence;
   private ArrayList<Phrase> phrases;
   private Vector<SoundData> sounds;
   private ColorScheme       colors;
   private String            language;
   private int               selectedSentence;
   private int               plusCount;
   private LessonsMagnetGame lesson;
   
   /** Constructor to initialize the magnets for a particular sentence */
   public SentencePhrases
           (LessonsMagnetGame lesson, SentenceAudioPictureData sentence, ColorScheme colors)
   {  
	  this.lesson   = lesson;
	  this.sentence = sentence;
      this.colors   = colors;
   }
   
   public int size()
   {
	   return sentence.getAudio().getVector().size();
   }
   
    /** Method to reset the game and create a new set of magnets */
   public ArrayList<Phrase> reset(int index)
   {
	  selectedSentence = index;
      PicturesSoundData audioData = sentence.getAudio();
      sounds = audioData.getVector();

      phrases = new ArrayList<Phrase>();

      String[] soundText = sounds.get(index).getSoundText();
      String[] words = soundText[SoundData.NATIVE].split("\\s+");
      language = soundText[SoundData.LANGUAGE];
     
      // Move the words starting with '+' to the back
      ArrayList<String> plusPhrases = new ArrayList<String>();
      ArrayList<String> allPhrases = new ArrayList<String>();
     
      // Remove hanging dashes
      int count = words.length;
      for (int k=0; k<count; k++)
      { 
	   	  if (words[k].charAt(0) == '+')
	   	  {
	   		  if (plusPhrases.isEmpty())
	   		  {
	   			  words[k] = words[k].replaceAll("^[+][-]","+");
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
     
      // Move words starting with + to the back
      plusCount = plusPhrases.size();
      String word;
      if (plusCount>0)
      {
	   	  word = plusPhrases.get(plusCount-1);
	   	  word = word.replaceAll("[-]$", "");
      	  word = lesson.formatControlString(word, false);
	   	  plusPhrases.set(plusCount-1,  word);
      }

      int size = allPhrases.size();
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

   	    
   	    if (k==count-1)
   	    	word = lesson.formatControlString(word,  true);

    	Phrase phrase = new Phrase(lesson, word, colors, language);
    	phrases.add(phrase);
     }
      
      String prefix, suffix, center;
      int p = phrases.size()-1;
      while (p>1)
      {
     	 prefix = phrases.get(p-2).getControlString();
     	 center = phrases.get(p-1).getControlString();
     	 suffix = phrases.get(p).getControlString();
     	 
     	 if (phrases.get(p).isPlus())
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
     		 		
     		 phrases.get(p-2).setControlString(lesson, prefix + "..." + suffix, false);
     		 
     	     phrases.remove(p);
     	     p -= 2;
     	 }
     	 else 
     	 {
     		 suffix = suffix.replaceAll("\\.{3}", "");
         	 phrases.get(p).setControlString(lesson, suffix, false);
     	 }
     	 p--;
       }
       return phrases;
   }

   /** Method to get the selected audio object that corresponds to the selected
    *       sentence
    *
    * @return SoundData object
    */
   public SoundData getAudio()
   {   PicturesSoundData audioData = sentence.getAudio();
       return audioData.getVector().get(selectedSentence);
   }

   /** Method to get a picture object (if it exists)
    *
    * @return PictureData object or null if doesn't exist
    */
   public PictureData getPicture() { return sentence.getPicture(); }

   /** Method to get the sentence describing a set of magnets
    *
    * @return The sentence of description
    */
   public String getSentence() 
   { 
	   return sentence.getSentence(); 
   }
   
   /** Get the audio recording that goes with the phrase
    * 
    * @return The audio object
    */
   public SoundData getPhraseAudio() { return sentence.getSound(); }

   /** Method to attempt to join two magnets.
    *
    * @param first The first magnet to join
    * @param second The second magnet to join
    * @return The joined magnet (the two originals are removed)
    */
   public Phrase join(Phrase first, Phrase second)
   {
      int i = -1, size = phrases.size();
      
      if (first.isPlus() != second.isPlus())
    	  return null;
      
      Phrase temp;
      if (second.getText().contains("..."))
      {
    	  temp = second;
    	  second = first;
    	  first = temp;
      }

      if (second.getText().contains("..."))
    	  return null;
      
      while (++i < size - 1)
      {  
    	 i = findMagnet(first, i);
         if (i<0 || i==size-1) return null;
         if (phrases.get(i+1).equals(second))  
         {  
        	 if (i>0 && phrases.get(i-1).isCircumfix() && !phrases.get(i).isCircumfix())
        		 return null;
        	 
       	 return join(i, i+1); 
         }
      }
      return null;
   }

   /** Method to determine if the sentence has been reconstructed
    *
    * @return true if yes, false if no
    */
   public boolean isComplete()  
   { 
	   return phrases.size()-plusCount <=1; 
   }

   /** Determine if magnet is the complete sentence represented by this object
    *
    * @param magnet The magnet in question
    * @return true if yes, false otherwise
    */
   public boolean canDisplay(Phrase magnet)
   {  if (!isComplete()) return false;
      return phrases.get(0).equals(magnet);
   }

   /** Find a matching magnet in the array list of magnets
    *
    * @param magnet The magnet to find
    * @param index The starting index
    * @return The index to the matching magnet or -1 if not found
    */
   private int findMagnet(Phrase magnet, int index)
   {  for (int i=index; i<phrases.size(); i++)
      { if (phrases.get(i).equals(magnet)) return i; }
      return -1;
   }

   /** Method to join two adjacent magnets
    *
    * @param first The index to the first magnet
    * @param second The index to the second magnet
    * @return joined magnet
    */
   private Phrase join(int first, int second)
   {  
	  if (first>second) join(second, first);
	  
	  Phrase firstMagnet = phrases.get(first);
	  String firstText = firstMagnet.getText();
      Phrase secondMagnet = phrases.get(second);
      String secondText = secondMagnet.getText();
      String newText = "";
      
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
    	  boolean firstEnd = firstText.endsWith("-"), secondStart = secondText.startsWith("-");
    	  String gap = "";
    	  if ( !(firstEnd || secondStart)) { gap = " "; }
    	  
    	  firstText = lesson.formatControlString(firstText, false);
    	  firstText = firstText.replaceAll("[-]$", "");
    	  secondText = lesson.formatControlString(secondText, true);
    	  secondText = secondText.replaceAll("^[-]", "");
    	  newText = firstText + gap + secondText;
    	  newText = newText.replaceAll("[-]{2}","");
	      newText = newText.replaceAll("\\([^\\) ]*\\)", "");
	      newText = newText.replaceAll("[\\[\\]]", "");
      }

      phrases.remove(firstMagnet);
      phrases.remove(secondMagnet);

      Phrase joinMagnet = new Phrase(lesson, newText, colors, language);
      if (firstMagnet.isPlus()) 
      {
       	 joinMagnet.setPlus();
       	 plusCount--;
   	  }
      
      phrases.add(first, joinMagnet);
      return joinMagnet;
   }
}     // End of SentenceMagnets class
