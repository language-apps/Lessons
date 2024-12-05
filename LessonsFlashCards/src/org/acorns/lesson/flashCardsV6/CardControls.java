/**
 * CardControls.java
 * @author HarveyD
 * @version 5.00 Beta
 *
 * Copyright 2009-2015, all rights reserved
 */

package org.acorns.lesson.flashCardsV6;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.acorns.language.*;
import org.acorns.lesson.*;

public class CardControls extends JPanel implements ActionListener
{
	private final static long serialVersionUID = 1;
    CardPanel parent;
    Lesson lesson;

    public CardControls(CardPanel parent, Lesson lesson, boolean first, boolean last)
    {
        this.parent = parent;
        this.lesson = lesson;

        String[] messageData = LanguageText.getMessageList(lesson, 2);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(Box.createHorizontalGlue());
        if (!first)
        {
        	add( makeButton(
        			"moveleft", messageData[0], AcornsProperties.MOVE_LEFT));
        
        	add(Box.createHorizontalStrut(5));
        }
        add( makeButton("prev", messageData[0], AcornsProperties.PREVIOUS));
        add(Box.createHorizontalStrut(5));
        add( makeButton("random", messageData[1], AcornsProperties.RANDOM));
        add(Box.createHorizontalStrut(5));
        add( makeButton("next", messageData[2], AcornsProperties.NEXT));
        if (!last)
        {
        	add(Box.createHorizontalStrut(5));
        	add( makeButton(
        			"moveright", messageData[0], AcornsProperties.MOVE_RIGHT));
        }
        add(Box.createHorizontalGlue());
        setOpaque(false);
    }
    
    /** Method to respond to button clicks */
    public void actionPerformed(ActionEvent event)
    {
        int direction;

        String action = event.getActionCommand();
        if (action.equals("moveleft"))
        {
        	parent.moveCard(false);
        	return;
        }
        if (action.equals("moveright")) 
        {
        	parent.moveCard(true);
        	return;
        }
        else if (action.equals("prev"))   direction = -1;
        else if (action.equals("next"))   direction = +1;
        else if (action.equals("random")) direction = 0;
        else return;

        parent.displayNewCard(direction);
    }

    /** Method to create one of the panel's buttons
     *
     * @param name The text to appear in the button
     * @param tooltip The tool tip associated with the button
     * @return The created button
     */
    /** Method to make a button with a tool tip */
    private JButton makeButton(String name, String toolTip, int iconName)
    {
    	ImageIcon icon = lesson.getIcon(iconName, 32);
        JButton button = new JButton(icon);
        button.setActionCommand(name);
        button.setToolTipText(toolTip);
        button.addActionListener(this);
        Dimension size = new Dimension(32,32);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        button.setSize(size);
        return button;
    }
}
