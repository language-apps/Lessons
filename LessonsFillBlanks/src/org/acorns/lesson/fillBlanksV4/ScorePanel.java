/*
 * ScorePanel.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.lesson.fillBlanksV4;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.ActionListener;

import org.acorns.data.SoundData;
import org.acorns.lesson.*;
import org.acorns.visual.*;
import org.acorns.widgets.RecordPanel;
import org.acorns.language.*;

public class ScorePanel extends JPanel
{
    private final static int FONT = 14;
    private final static int SIZE  = 25; 
    private final static int STRUT = 20;
    
    private JTextField  textField;
    private RecordPanel recorder;
    private Lesson lesson;
    public ActionListener listener;
    
    private static final long serialVersionUID = 1;
    
    public ScorePanel(Lesson lesson, Dimension size, ColorScheme colors, ActionListener listener)
    {
      this.lesson = lesson;
      this.listener = listener;
      
      Dimension controlsSize 
              = new Dimension(size.width*11/16-10, size.height/2); 

      Font font = new Font(null, Font.PLAIN, FONT);
      
      PlayControls controls = new PlayControls(colors);
      controls.setBackground(colors.getColor(true));
      controls.setBorder(BorderFactory.createEtchedBorder
         (EtchedBorder.LOWERED, new Color(200,200,200), new Color(50,50,50)));
      controls.setPreferredSize(controlsSize);
      controls.setMaximumSize(controlsSize);
 
      // Create the panel with the text field for entering the answer
      JLabel enterLabel = new JLabel(LanguageText.getMessage(lesson, 8));
      enterLabel.setFont(font);
      enterLabel.setForeground(colors.getColor(false));
      
      textField = new JTextField("");
      textField.setSize(new Dimension(300, SIZE));
      textField.setPreferredSize(textField.getSize());
      textField.setMaximumSize(textField.getSize());
   
      JPanel textPanel = new JPanel();
      textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
      textPanel.add(Box.createHorizontalGlue());
      textPanel.add(enterLabel);
      textPanel.add(textField);
      textPanel.setBackground(colors.getColor(true));
      textPanel.add(Box.createHorizontalGlue());
      
      // Add the instructions panel to hold the left side of the panel.
      JPanel instructions = new JPanel();
      instructions.setLayout(new GridLayout(2,1));
      instructions.setBackground(colors.getColor(true));
      instructions.setBorder(BorderFactory.createEtchedBorder
           (EtchedBorder.LOWERED, new Color(200,200,200), new Color(50,50,50)));
      
      // Add the instructions and the text field
      instructions.add(textPanel);
      instructions.add(controls);

      // Get score label with appropriate font and text.
      JLabel scoreDisplay 
            = Score.getScoreLabel(new Dimension(size.width*5/16, size.height));
      Score.calculateScore();
      scoreDisplay.setForeground(colors.getColor(false));
      scoreDisplay.setBackground(colors.getColor(true));

      // Initialize the scorePanel
      setBackground(colors.getColor(true));
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      add(instructions);
      add(Box.createHorizontalStrut(5));
      add(scoreDisplay);
      add(Box.createHorizontalGlue());
      
   
   }  // End of ScorePanel constructor
    
    /** Nested class to control the audio play back */
    class PlayControls extends JPanel
    {
    	private final static long serialVersionUID = 1;

        public PlayControls(ColorScheme colors)
        {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            String[] playControls = LanguageText.getMessageList(lesson, 11);
            add(Box.createHorizontalGlue());
            add(makeButton("left", playControls[0], AcornsProperties.REPLAY));
            add(Box.createHorizontalStrut(STRUT));
            add(makeButton("up", playControls[1], AcornsProperties.ANSWERS));
            add(Box.createHorizontalStrut(STRUT));
            add(makeButton("down", playControls[2], AcornsProperties.PAUSE));
            add(Box.createHorizontalStrut(STRUT));
            add(makeButton("right", playControls[3], AcornsProperties.PLAY));
            add(Box.createHorizontalStrut(STRUT));
            add(makeButton("check", playControls[4], AcornsProperties.CHECK));
            recorder = new RecordPanel(new SoundData(), SIZE, STRUT);
            recorder.setBackground(colors.getColor(true));
            recorder.setAlignmentY(TOP_ALIGNMENT);
            add(Box.createHorizontalGlue());
            add(recorder);
            add(Box.createHorizontalGlue());
            setOpaque(false);
        }
        /** Method to make a button with a tool tip */
        private JButton makeButton(String name, String toolTip, int iconName)
        {
        	ImageIcon icon = lesson.getIcon(iconName, SIZE);
            JButton button = new JButton(icon);
            button.setAlignmentY(TOP_ALIGNMENT);
            button.setActionCommand(name);
            button.setToolTipText(toolTip);
            button.addActionListener(listener);
            button.setPreferredSize(new Dimension(SIZE, SIZE));
            return button;
        }
    }       // End of PlayControls class

    
   /** Get text field for entering the score 
    *  @return score entry text field.
    */
   public JTextField getScoreTextField()  {  return textField;  }
   
   /** Get Audio recorder panel 
    *  @return score entry text field.
    */
   public RecordPanel getScoreRecorder() { return recorder; }
    
}    // End of ScorePanel class

