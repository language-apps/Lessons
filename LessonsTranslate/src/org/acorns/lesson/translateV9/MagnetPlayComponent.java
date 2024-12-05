/*
 * MagnetPlayComponent.java
 *
 *   @author  HarveyD
 *   @version 6.00
 *
 *   Copyright 2007-2015, all rights reserved
 */

package org.acorns.lesson.translateV9;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.*;
import java.util.*;

import org.acorns.visual.*;
import org.acorns.data.*;

/** Class to execute translate lessons */
public class MagnetPlayComponent extends JPanel implements MouseListener, PropertyChangeListener
{
   private final static long serialVersionUID = 1;
	
   private DisplaySentence  area;

private ColorScheme      colors;
   private SentencePhrases  sentencePhrases;

   //private ArrayList<Phrase> sentenceMagnets;

   /** Constructor to initialize the game */
   public MagnetPlayComponent(LessonsTranslate lesson, DisplaySentence area, SentencePhrases phrases)
   {  
      colors = lesson.getColorScheme();
      this.area = area;
      this.sentencePhrases = phrases;

      setLayout( new FlowLayout(FlowLayout.CENTER, 20, 10) );
      setBackground(new Color(80, 80, 80));
      
      Dimension size = lesson.getDisplaySize();
      setSize(size);
      setPreferredSize(size);

      setBackground(colors.getColor(true));
      
	  PropertyChangeSupport pcs = area.getPropertyChangeSupport();
	  pcs.addPropertyChangeListener("insert", this);
	  pcs.addPropertyChangeListener("delete", this);
   }

   /** Method to reset the game
    *    It creates a new set of magnets and disburses them on the display
    */
   public void resetGame()
   {
	      removeAll();
	      
	      ArrayList<Phrase> phrases = sentencePhrases.getPhrases(false);
	      ArrayList<Integer> free = new ArrayList<Integer>();
	      int size = phrases.size();
	      for (int i=0; i<size; i++)
	    	  free.add(i);
	      
	      int choice, index;
	      while (free.size()> 0)
	      {
	    	  index = (int)(Math.random() * free.size());
	    	  choice = free.get(index);
	    	  free.remove(index);
	    	  
	    	  Phrase magnet = phrases.get(choice);
		      if (phrases.size()>0)
		         { 
		            add(magnet);
		            magnet.addMouseListener(this);
		         }
	      }
	      super.validate();
	      repaint();
   }  // End of resetGame()
   
   /** Paint component draws the background
   *
   * @param graphics The graphics drawing object
   */
  public @Override void paintComponent(Graphics graphics)
  {
	  super.paintComponent(graphics);
      PictureData picture = colors.getPicture();
      Dimension size = getSize();

      if (picture !=null)
      {  BufferedImage image = picture.getImage
                  (this, new Rectangle(0, 0, size.width, size.height));
         graphics.drawImage(image, 0, 0, size.width, size.height, null);
      }
      else
      {
         Color color = colors.getColor(true);
         graphics.setColor(color);
         graphics.fillRect(0, 0, size.width, size.height);
      }
  }

  
   /** Listener methods to handle moving magnets about the display */
   Phrase dragMagnet;
   public void mousePressed(MouseEvent event)  {}
   public void mouseReleased(MouseEvent event)
   {}
   
   public void mouseClicked(MouseEvent event) 
   {
       Object object = event.getSource();
       if (object instanceof Phrase)
       {
          Phrase magnet = (Phrase)object;
    	  remove(magnet);
    	  magnet.removeMouseListener(this);
    	  area.insertPhrase(magnet);
    	  super.invalidate();
    	  validate();
    	  repaint();
       }
   }
   public void mouseEntered(MouseEvent event) {}
   public void mouseExited(MouseEvent event)  {}
   public void mouseMoved(MouseEvent event)   {}
   
   /** Listen for changes to the string of displayed phrases
    * 
    * @param e  The property change event
    * 
    * When a phrase is removed from the display area, insert it here
    * When a phrase is added to the display area, delete it here
    * 
    */
   public void propertyChange(PropertyChangeEvent e) {
		String type = e.getPropertyName();
		boolean valid = type.equals("insert") ||
				type.equals("delete");
		if (type==null || !valid) return;
		
		if (type.equals("insert")) // Into the display panel (remove here)
		{
			Phrase newValue = (Phrase)e.getNewValue();
			
			ArrayList<Phrase> phrases = newValue.getEmbeddedPhrases();
			if (phrases.isEmpty())
			{
				newValue.removeMouseListener(this);
				remove(newValue);
			}
			else
			{	
				for (Phrase phrase: phrases)
				{
					phrase.removeMouseListener(this);
					remove(phrase);
				}
			}
		}
		else if (type.equals("delete"))	// From the display panel (add here)
		{
			Phrase oldValue = (Phrase)e.getOldValue();
			ArrayList<Phrase> phrases = oldValue.getEmbeddedPhrases();
			if (phrases.isEmpty())
			{
				oldValue.addMouseListener(this);
				add(oldValue);
			}
			else
			{
				for (Phrase phrase: phrases)
				{
					phrase.addMouseListener(this);;
					add(phrase);
				}
			}
		}
		super.invalidate();
		validate();
		repaint();
	}   // End of property change listener
	
  
}     // End of MagnetPlayComponent



