package org.acorns.lesson.missingWordV11;
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import org.acorns.data.PictureData;
import org.acorns.data.SoundData;
import org.acorns.language.KeyboardFonts;
import org.acorns.language.LanguageText;
import org.acorns.lesson.AcornsProperties;
import org.acorns.visual.ColorScheme;

public class DisplaySentence extends JPanel 
{
	private static final long serialVersionUID = 1L;

	private static final int FONT_SIZE = 20;
	private static final Font FONT = new Font("Times New Roman", Font.PLAIN, FONT_SIZE);
	private static final Dimension SIZE = new Dimension(FONT_SIZE*20, FONT_SIZE);
	private static final Dimension SENTENCE_SIZE = new Dimension(700, 100);
	private static final int STRUT = 20;
	

	private SentencePhrases phrases;
	private LessonsMissingWord lesson;
	private ColorScheme scheme;
		
	private JLabel leftPart, rightPart;
	private JTextField middlePart, textEntry;
	
	
	/** Constructor to instantiate the panel holding phrases that the user selected
	 * 
	 * @param lesson The current lesson
	 */
	public DisplaySentence(LessonsMissingWord lesson, SentencePhrases phrases)
	{
		this.lesson = lesson;
		this.phrases = phrases;
		
		setLayout( new BoxLayout(this, BoxLayout.PAGE_AXIS) );
		ColorScheme scheme = lesson.getColorScheme();
		this.scheme = scheme;
		
		Color background = scheme.getColor(true);
		Color foreground = scheme.getColor(false);
		setBackground(scheme.getColor(true));
		setForeground(scheme.getColor(false));
		
		Dimension size = new Dimension(super.getWidth(), HEIGHT);
		setPreferredSize(size);
		
		setBorder(new BevelBorder(BevelBorder.RAISED));
		
		JPanel sentencePanel = new JPanel();
		sentencePanel.setLayout(new FlowLayout());
		sentencePanel.setOpaque(false);
		
		leftPart = new JLabel();
		leftPart.setForeground(foreground);
		leftPart.setBackground(background);
		middlePart = new JTextField();
		middlePart.setEditable(false);
		middlePart.setBackground(background);

		rightPart = new JLabel();
		rightPart.setForeground(foreground);
		rightPart.setBackground(background);

		sentencePanel.add(leftPart);
		sentencePanel.add(middlePart);
		sentencePanel.add(rightPart);
		sentencePanel.setPreferredSize(SENTENCE_SIZE);
		sentencePanel.setMaximumSize(SENTENCE_SIZE);
		
		JPanel answerPanel = new JPanel();
		answerPanel.setLayout(new BoxLayout(answerPanel, BoxLayout.X_AXIS));
		
		String messageData = LanguageText.getMessage(lesson, 9);
		JLabel label = new JLabel(messageData);
		label.setFont(FONT);
		label.setForeground(foreground);
		label.setForeground(foreground);
		answerPanel.setOpaque(false);
		answerPanel.add(label);
		textEntry = new JTextField(50);
		textEntry.setFont(FONT);
		answerPanel.add(textEntry);
		answerPanel.setPreferredSize(SIZE);
		answerPanel.setMaximumSize(SIZE);
		
		JPanel buttonPanel = makeUnicodeButtons();
		
		add(Box.createVerticalStrut(2*STRUT));
		add(sentencePanel);
		add(Box.createVerticalStrut(3*STRUT));
		add(answerPanel);
		add(Box.createVerticalStrut(STRUT));
		if (buttonPanel !=null) add(buttonPanel);
		//add(Box.createVerticalGlue());
	}
	
	public void resetGame()
	{
		SoundData audio = phrases.getCurrentSentence();

		String gloss = audio.getSoundText(SoundData.GLOSS);
		String indigenous = audio.getSoundText(SoundData.NATIVE);
		String language = audio.getSoundText(SoundData.LANGUAGE);
		
		ColorScheme scheme = lesson.getColorScheme();
		int size = scheme.getSize();
	    Font indigenousFont = KeyboardFonts.getLanguageFonts().getFont(language);

	    Font glossFont = new Font(null, Font.PLAIN, size);
	    
	    if (indigenousFont == null) indigenousFont = glossFont;
	    else indigenousFont = indigenousFont.deriveFont(Font.PLAIN, size);

	    boolean[] options = lesson.getOptions();
	    Font font = indigenousFont;
		String sentence = lesson.getPhrasesForDisplay(indigenous, true, true, true);

		if (!options[AcornsProperties.SELECT])
		{
			font = glossFont;
			sentence = gloss;
		}
		
		leftPart.setFont(font);
		middlePart.setFont(font);
		middlePart.setOpaque(true);
		rightPart.setFont(font);
		
	    KeyboardFonts kbFonts = KeyboardFonts.getLanguageFonts();
	    kbFonts.setFont(language, textEntry);
		
		String[] words = sentence.split(" ");
		int index = (int)(Math.random() * words.length);
		String left = makeString(words, 0, index);
		String middle = words[index];
		String right = makeString(words, index+1, words.length);
		
		leftPart.setText(left);
		int columns = middle.length();
		middlePart.setColumns(columns);  
		rightPart.setText(right);
		textEntry.setText("");
	}
	
	   /** Paint component draws the background
	   *
	   * @param graphics The graphics drawing object
	   */
	  public @Override void paintComponent(Graphics graphics)
	  {
		  super.paintComponent(graphics);
	      PictureData picture = scheme.getPicture();
	      Dimension size = getSize();

	      if (picture !=null)
	      {  BufferedImage image = picture.getImage
	                  (this, new Rectangle(0, 0, size.width, size.height));
	         graphics.drawImage(image, 0, 0, size.width, size.height, null);
	      }
	      else
	      {
	         Color color = scheme.getColor(true);
	         graphics.setColor(color);
	         graphics.fillRect(0, 0, size.width, size.height);
	      }
	  }



	/** Return the answer that the user has so far constructed */
	public String getUserAnswer()
	{
		String left = leftPart.getText();
		String middle = textEntry.getText();
		String right = rightPart.getText();

		String result = (left.length()>0 && middle.length()>0) 
				? left + " " + middle : left + middle;
		
		if (middle.length()>0 && right.length()>0)
			 result += " " + right;
		else result += right;
		
		return result;
	}

    /** Construct a string from an array of words
     * 
     * @param words array of words
     * @param first first index
     * @param last  last index (non-inclusive)
     * @return constructed string
     */
    private String makeString(String[] words, int first, int last)
    {
		StringBuilder builder = new StringBuilder();
		try
		{
			for (int w=first; w<last; w++)
			{
				builder.append(words[w]);
				if (w<last-1) 
					builder.append(" ");
			}
			return builder.toString();
		}
		catch (Exception e) {}
		return "";
    }
    
    /** Create a list of buttons with special characters
     * 
     * @param buttonList A list of button labels
     * @param panel JPanel to which list of buttons should be added
     */
    private JPanel makeUnicodeButtons()
    {
    	String specials = lesson.getSpecials();
    	String[] specialsArray = specials.trim().split("\\s+");
    	
    	JPanel buttonPanel = new JPanel();
		buttonPanel.setOpaque(false);
    	buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
    	
    	JButton button;
    	String str;
    	boolean buttons=false;
    	for (int i=0; i<specialsArray.length; i++)
    	{
    		str = specialsArray[i];
    		if (str.length() == 0) continue;

    		buttonPanel.setOpaque(false);
    		if (FONT.canDisplayUpTo(str) >=0)
    			continue;
    		
	    	button = new JButton(str);
	    	buttons = true;
    	    button.setFont(FONT);
    	    button.addActionListener( new ActionListener() 
    	    {

    			@Override
    			public void actionPerformed(ActionEvent e) {
    			JButton button = (JButton)e.getSource();
    						
    			String specialChar = button.getText();
    			int spot = textEntry.getCaretPosition();
    			String oldText = textEntry.getText();
    			String newText = oldText.substring(0, spot) 
    					+ specialChar 
    					+ oldText.substring(spot);
    			textEntry.setText(newText);
    		}});
    	    		
    	    buttonPanel.add(button);
    	}   // end of special buttons loop
      	if (!buttons) return null;
    	return buttonPanel;
    	      	
    }   // end of special table processing
}		// end of makeUnicodeButtons

