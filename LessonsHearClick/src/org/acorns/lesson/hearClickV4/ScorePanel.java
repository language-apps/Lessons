/*
 * ScorePanel.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.lesson.hearClickV4;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionListener;

import org.acorns.lesson.*;
import org.acorns.visual.*;
import org.acorns.language.*;

public class ScorePanel extends JPanel
{
	private final static long serialVersionUID = 1;
 
    private Lesson lesson;
    public ActionListener listener;
    
    public ScorePanel(Lesson lesson, Dimension size, ColorScheme colors, ActionListener listener)
    {
    	this.lesson = lesson;
    	this.listener = listener;
    	
        Dimension controlsSize 
              = new Dimension(size.width*11/16-10, size.height); 

        PlayControls controls = new PlayControls();
        controls.setBackground(colors.getColor(true));
        controls.setBorder(BorderFactory.createEtchedBorder
           (EtchedBorder.LOWERED, new Color(200,200,200), new Color(50,50,50)));
        controls.setPreferredSize(controlsSize);
      
        // Get score label with appropriate font and text.
        JLabel scoreDisplay 
            = Score.getScoreLabel(new Dimension(size.width*5/16, size.height));
        scoreDisplay.setBackground(colors.getColor(true));
        scoreDisplay.setForeground(colors.getColor(false));
        Score.calculateScore();

        // Initialize the scorePanel
        setBackground(colors.getColor(true));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(Box.createHorizontalGlue());
        add(controls);
        add(Box.createHorizontalGlue());
        add(scoreDisplay);
        add(Box.createHorizontalGlue());
      
   }  // End of ScorePanel constructor
    
    /** Nested class to control the audio play back */
    class PlayControls extends JPanel
    {
    	private final static long serialVersionUID = 1;

        public PlayControls()
        {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            String[] playControls = LanguageText.getMessageList(lesson, 12);
            add(Box.createHorizontalGlue());
            add(makeButton("left", playControls[0], AcornsProperties.REPLAY));
            add(Box.createHorizontalStrut(20));
            add(makeButton("up", playControls[1], AcornsProperties.ANSWERS));
            add(Box.createHorizontalStrut(20));
            add(makeButton("down", playControls[2], AcornsProperties.PAUSE));
            add(Box.createHorizontalStrut(20));
            add(makeButton("right", playControls[3], AcornsProperties.PLAY));
            add(Box.createHorizontalGlue());
            setOpaque(false);
        }
        /** Method to make a button with a tool tip */
        private JButton makeButton(String name, String toolTip, int iconName)
        {
        	ImageIcon icon = lesson.getIcon(iconName, 40);
            JButton button = new JButton(icon);
            button.setActionCommand(name);
            button.setToolTipText(toolTip);
            button.addActionListener(listener);
            button.setPreferredSize(new Dimension(40,40));
            return button;
        }
    }       // End of PlayControls class

    
}    // End of ScorePanel class

