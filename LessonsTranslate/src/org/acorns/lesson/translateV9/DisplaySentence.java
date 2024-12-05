/**
 * DisplaySentence.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2019, all rights reserved
 *   
 *   Maintain the list of phrases that make up a sentence
 */
package org.acorns.lesson.translateV9;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.acorns.visual.ColorScheme;

public class DisplaySentence extends JPanel implements ActionListener, MouseListener {
	private static final long serialVersionUID = 1L;
	
	private static int HEIGHT = 200;

	private SentencePhrases phrases;
	private LessonsTranslate lesson;
	
	private Phrase pastePhrase;
	private PropertyChangeSupport pcs;
	
	/** Constructor to instantiate the panel holding phrases that the user selected
	 * 
	 * @param lesson The current lesson
	 */
	public DisplaySentence(LessonsTranslate lesson, SentencePhrases phrases)
	{
		this.lesson = lesson;
		this.phrases = phrases;
		pastePhrase = null;
		
		setLayout( new FlowLayout(FlowLayout.CENTER, 10, 5) );
		ColorScheme scheme = lesson.getColorScheme();
		setBackground(scheme.getColor(true));
		setForeground(scheme.getColor(false));
		
		Dimension size = new Dimension(super.getWidth(), HEIGHT);
		setPreferredSize(size);
		
		pcs = new PropertyChangeSupport(DisplaySentence.class);
		addMouseListener(this);
		
		setBorder(new BevelBorder(BevelBorder.RAISED));
		
	}
	
	public PropertyChangeSupport getPropertyChangeSupport() { return pcs; }
	
	public void resetGame()
	{
		removeAll();
		resetPanel();
	}

	/** Return the answer that the user has so far constructed */
	public String getUserAnswer()
	{
		int count = getComponentCount();
		Component[] component = getComponents();
		Phrase phrase;
		StringBuilder bytes = new StringBuilder();
		
		for (int c=0; c<count; c++)
		{
			try
			{
				phrase = (Phrase)component[c];
				bytes.append(phrase.getText());
				if (c<count-1) bytes.append(" ");
			}
			catch (Exception e) {}
		}
		
		return bytes.toString();
	}
	
	/** Add a phrase to the end of the panel */
	public void insertPhrase(Phrase phrase)
	{
		pastePhrase = null;
		
		Phrase lastPhrase;
		
		String control = phrase.getControlString();
		String displayText = control;
		if (!control.startsWith("-"))
		{
			displayText = lesson.formatControlString(control, true);
		}
		if (!control.endsWith("-"))
		{
			displayText = lesson.formatControlString(displayText, false);
		}
		
		if (!displayText.contentEquals(control))
		{
			phrase = phrases.makePhrase(phrase,  displayText);
		}
		
		try 
		{
			int last = getComponentCount()-1;
			Component component = getComponent(last);
			lastPhrase = (Phrase)component;

			Phrase newPhrase = phrases.join(lastPhrase,  phrase);
			if (newPhrase!=null)
			{
				lastPhrase.removeActionListener(this);
				remove(lastPhrase);
				newPhrase.addActionListener(this);
				add(newPhrase);
				resetPanel();
				return;
			}
		}
		catch(Exception e) {}
				
		add(phrase); 
		phrase.addActionListener(this);
		resetPanel(); 
		return; 
	}

	/** Insert a phrase at a new spot after it has been removed by a click event */
	private void insertPhrase(int pos)
	{
		if (pastePhrase == null) return;
		
		Phrase phrase, newPhrase;
		int len = getComponentCount();
		
		if (pos>0)
		{
			try // Attempt to join with previous phrase
			{
				Component component = getComponent(pos-1);
				phrase = (Phrase)component;
				newPhrase = phrases.join(phrase,  pastePhrase);
				if (newPhrase != null)
				{
					phrase.removeActionListener(this);
					remove(phrase);
					pastePhrase = newPhrase;
					pos--;
					len--;
				}
			}
			catch(Exception e) {}
		}

		if (pos < len)
		{
			try   // Attempt to join with next phrase
			{
				Component component = getComponent(pos);
				phrase = (Phrase)component;
				newPhrase = phrases.join(pastePhrase, phrase);
				if (newPhrase != null)
				{
					phrase.removeActionListener(this);
					remove(phrase);
					pastePhrase = newPhrase;
				}
			}
			catch(Exception e) {}
		}
		
		len = getComponentCount();
		pastePhrase.addActionListener(this);
		if (len==0 || pos>=len) add(pastePhrase);
		else add(pastePhrase, pos);

		fireEvent("insert", pastePhrase);
		pastePhrase = null;
	}

	/** Remove a component from the panel after a click event */
	private void removePhrase(Phrase phrase)
	{
		remove(phrase);
	    phrase.removeActionListener(this);
	    pastePhrase = phrase;
	    fireEvent("delete", phrase);
		return;
	}
	
	/** Fire property change event when components added or removed from the panel
	 * 
	 * @param type "insert" or "delete"
	 * @param phrase The Phrase component containing one or more words
	 */
	private void fireEvent(String type, Phrase phrase)
	{
		Phrase oldValue = null, newValue = null;
		switch(type) {
		case "insert":
			newValue = phrase;
			break;
		case "delete":
			oldValue = phrase;
			break;
		};
		
        pcs.firePropertyChange(type, oldValue, newValue);
        resetPanel();
		return;
	}
	
	/** Redraw panel when the list of components change */
	private void resetPanel()
	{
		super.invalidate();
		validate();
		repaint();
	}

	/** Listen to mouse clicks */
    @Override
	public void actionPerformed(ActionEvent e) { 
    	try { 
        	Object o = e.getSource(); 
        	Phrase phrase = (Phrase)o;
        	Component[] components = getComponents();
        	int count = getComponentCount();
        	
        	for (int i=0; i<count; i++)
        	{
        		if (components[i]==phrase)
        		{
    				removePhrase(phrase);
        		}
        	}
    	}
    	catch (Exception ex) 
    	{
    		System.out.println(ex);
    	}
	}
    
    @Override
    public void mouseClicked(MouseEvent e)
    {
    	if (pastePhrase != null)
    	{
    		Component[] components = getComponents();
    		int count = getComponentCount();
    		Point mousePoint = e.getPoint();
    		
    		// Find the place to insert the deleted phrase
    		Rectangle current;
    		int spot = -1;
    		while (++spot<count)
    		{
    			if (!(components[spot] instanceof Phrase)) continue;
    			
    			current = components[spot].getBounds();
    			if (mousePoint.y>current.y+current.height) continue;  // Find correct row
    			if (mousePoint.x>current.x) continue; // Find correct column
    			
    			break;
    		}
    		
    		insertPhrase(spot);
    	}
    }

    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
	
}

