package org.acorns.lesson.translateV9;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.JOptionPane;

import org.acorns.audio.TimeDomain;
import org.acorns.audio.timedomain.Psola;
import org.acorns.data.SentenceAudioPictureData;
import org.acorns.data.SoundData;
import org.acorns.language.KeyboardFonts;
import org.acorns.language.LanguageText;
import org.acorns.language.SpellCheck;
import org.acorns.lesson.AcornsProperties;
import org.acorns.lesson.Lesson;
import org.acorns.lesson.Score;
import org.acorns.visual.ColorScheme;

public class SentenceControls extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private static final int SPACE = 5;   // Space in pixels between components
	private static final int ICON  = 25;  // Size of icons
	
	private LessonsTranslate lesson;
	private ColorScheme scheme;
	private SentencePhrases phrases;
	
	private JTextField textField;
	
	private SoundData audio, newSound;
	
	private SentenceControls parent;

	/** Constructor to create a control panel for displaying and playing the sudio */
	public SentenceControls(LessonsTranslate lesson, TranslatePlayPanel play, SentencePhrases phrases, DisplaySentence display)
	{
		this.parent  = this;
		this.lesson  = lesson; 
		this.scheme  = lesson.getColorScheme();
		this.phrases = phrases;
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBackground(Color.lightGray);
		
        String tip = LanguageText.getMessage(lesson, 10);
        JButton speedButton  = new ToolTipButton(lesson, AcornsProperties.SLOW, tip);
		speedButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newSound.playBack(null, -1, -1);
			}
		});
		
        tip = LanguageText.getMessage("commonHelpSets", 76);
        JButton playButton  = new ToolTipButton(lesson, AcornsProperties.PLAY, tip);
		playButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					audio.playBack(null,  -1,  -1);
			}
		});

        tip = LanguageText.getMessage("commonHelpSets", 82);
        JButton helpButton = new ToolTipButton(lesson, AcornsProperties.ANSWERS, tip);
		helpButton.addActionListener( new ActionListener() {
;			@Override
			public void actionPerformed(ActionEvent event) {
				String categoryText = lesson.getCategoryDescription();
				
				SentenceAudioPictureData category = lesson.getCurrentCategory();			    
				lesson.feedback(parent, LessonsTranslate.HELP, categoryText, category, audio);
			}
		});

        tip = LanguageText.getMessage("commonHelpSets", 83);
        JButton checkButton = new ToolTipButton(lesson, AcornsProperties.CHECK, tip);
		checkButton.addActionListener( new ActionListener() {
;			@Override
			public void actionPerformed(ActionEvent e) {
	
			    // Get the user answer
			    String userAnswer = display.getUserAnswer();
			    
			    // Get the actual answer
			    String sentence = "";
			    
				boolean options[] = lesson.getOptions();
				if (options[AcornsProperties.SELECT])
				{
					String indigenous = audio.getSoundText(SoundData.NATIVE);
					sentence = lesson.getPhrasesForDisplay(indigenous, true, true, true);
				}
				else
				{
					String gloss = audio.getSoundText(SoundData.GLOSS);
					sentence = gloss;
			    }
			    
				/// Check the user answer against 
			    SoundData correct = lesson.getSound(AcornsProperties.CORRECT);
			    SoundData incorrect = lesson.getSound(AcornsProperties.INCORRECT);
			    SoundData close = lesson.getSound(AcornsProperties.SPELL);
			    
				SentenceAudioPictureData category = lesson.getCurrentCategory();			    
			    
			    double distance = SpellCheck.editDistance(userAnswer, sentence);
			    distance = distance/sentence.length();

		  	    String[] message = LanguageText.getMessageList(lesson,16);
			    if (distance>0.1)
			    {
			        incorrect.playBack(null, 0, -1);
			        Score.nextScore(false);
			        
					int choice = lesson.feedback(parent, LessonsTranslate.INCORRECT, message[1], category, audio);
					
			        if (choice<=JOptionPane.YES_OPTION)
			        {
						phrases.retryCurrentSentence();
			        	phrases.getNextSentence();
						play.resetGame();
			        }
			        else
			        {
						nextSentence(play);
			        }
			        return;
			    }
			    
			    if (distance>0.0)
			    {
			        close.playBack(null, 0, -1);
			        Score.nextScore(true);
					lesson.feedback(parent, LessonsTranslate.CLOSE, message[0], category, audio);
					nextSentence(play);
					return;
			    }
			    
			    if (distance==0  )
			    {
			        correct.playBack(null, 0, -1);
			        Score.nextScore(true);
			        nextSentence(play);
			        return;
			    }
			}
		});

		textField = new JTextField();
		textField.setBackground(scheme.getColor(true));
		textField.setForeground(scheme.getColor(false));

		textField.setEditable(false);
		textField.setOpaque(false);
		
	 	add(playButton);
		add(speedButton);
		
		add(helpButton);
		add(Box.createHorizontalStrut(SPACE));
		add(checkButton);
		add(Box.createHorizontalStrut(SPACE));
		add(textField);
		add(Box.createHorizontalGlue());
		
		setBorder(new EtchedBorder());
	}   // End of constructor

	/** Reset the game with a different sentence */
	public void resetGame()
	{
		audio = phrases.getCurrentSentence();
		newSound = rateChange(audio);

		String gloss = audio.getSoundText(SoundData.GLOSS);
		String indigenous = audio.getSoundText(SoundData.NATIVE);
		String language = audio.getSoundText(SoundData.LANGUAGE);
		
		int size = scheme.getSize();
	    Font font = KeyboardFonts.getLanguageFonts().getFont(language);
	    if (font == null) font = new Font(null, Font.PLAIN, size);
	    else font = font.deriveFont(Font.PLAIN, size);
		
		String sentenceText;
		boolean options[] = lesson.getOptions();
		
		textField.setText("");
		if (options[AcornsProperties.DISPLAY] == options[AcornsProperties.SELECT])
		{
			if (options[AcornsProperties.DISPLAY])
			{
				textField.setFont(new Font("", Font.PLAIN, size));
				sentenceText = gloss;
			}
			else
			{
				textField.setFont(font);
				sentenceText = lesson.getPhrasesForDisplay(indigenous, true, true, true);
		    }
			textField.setText(sentenceText);
		}

		audio.playBack(null,  -1,  -1);
	}

	/** Either category; either repeat or move to next */
	private void nextSentence(TranslatePlayPanel play)
	{
        if (phrases.hasMoreSentences())
        {
        	phrases.getNextSentence();
        	play.resetGame();
        	return;
        }
        
  	    String[] message = LanguageText.getMessageList(lesson,12);
		String categoryText = message[0] 
					+ lesson.getCategoryDescription() + message[1];

		SentenceAudioPictureData category = lesson.getCurrentCategory();			    
        int choice = lesson.feedback(parent, LessonsTranslate.CORRECT, categoryText, category, audio);
        if (choice==JOptionPane.YES_OPTION)
        {
        	phrases.getNextCategory();
        	play.resetGame();
        }
        else
        {
        	phrases.resetCategory(true); // repeat category
        	play.resetGame();
        	
        }
	}
	
   /** Create a button object with a string tooltip */
   private class ToolTipButton extends JButton
   {
	   private final static long serialVersionUID = 1;
       
       public ToolTipButton(Lesson lesson, int iconNo, String toolTip)
       {
       	    ImageIcon icon = lesson.getIcon(iconNo, ICON);
            setIcon(icon);
            setToolTipText(toolTip);
    		setMargin(new Insets(0, 0, 0, 0));
       }
   }
	   
    /** Slow down the audio 
     *  @param sound Audio recording object
     */
    private SoundData rateChange(SoundData sound)
    {   
       float changeRate = (float)0.5; // Slow down speed is 50% of original
        
       SoundData newSound = sound.clone();
        
       Psola psola = new Psola(sound, new Point(-1,-1));  // Select entire audio.
       double[] newTime = psola.getModifiedRate(changeRate);
       if (newTime==null) return null;
       
       newSound = new SoundData();
       TimeDomain timeDomainObject = new TimeDomain(newSound);
       timeDomainObject.saveTimeDomainIntoAudio(newTime);
       return newSound;
    }

	
}	// End of Display Sentence class
