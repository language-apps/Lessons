/**
 * FlashScrollPane.java
 * @author HarveyD
 * @version 5.00 Beta
 *
 * Copyright 2009-2015, all rights reserved
 */

package org.acorns.lesson.flashCardsV6;

import java.util.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.BorderFactory;

import org.acorns.data.*;
import org.acorns.visual.*;
import org.acorns.language.*;

public class FlashScrollPane extends JScrollPane
{
	private final static long serialVersionUID = 1;
	
    private static final int SCROLL_INCREMENT = 50;
    private static final int FONT_SIZE = 16;

    private ArrayList<String[]> soundTextList;
 
    private JList<String> list;      // List of flash card selections.
    private ListCellFontRenderer renderer; // Object to render Jlist cells

    /** Method to manage the flash card lesson scrollpane
     * 
     * @param lesson The flash card lesson object
     * @param parent The object to list for list selection changes
     */
    public FlashScrollPane
           (LessonsFlashCards lesson, ListSelectionListener parent)
    {
        // Get data to appear in the jlist from the lesson
        PicturesSoundData pictureSoundData;
        Vector<SoundData> soundVector;
        SoundData sound;
        String[] text;

        soundTextList = new ArrayList<String[]>();
 
        int layer = lesson.getLayer();
        int size  = lesson.getActiveChoices();
        PictureChoice[] choices = lesson.getActivePictureData();
        for (int index = 0; index<size; index++)
        {   pictureSoundData = choices[index].getQuestions(layer);
            soundVector = pictureSoundData.getVector();
            for (int offset=0; offset<soundVector.size(); offset++)
            {   sound = soundVector.get(offset);
                text  = sound.getSoundText();
                soundTextList.add(text);
           }
        }

        list = new JList<String>();
        list.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(-1);
        Font defaultFont = new Font(null, Font.PLAIN, FONT_SIZE);
        list.addListSelectionListener(parent);
        list.setFixedCellHeight(30);
        
        Dimension scrollSize = lesson.getDisplaySize();
        scrollSize.width -= 50;
        Dimension listSize
            = new Dimension(scrollSize.width, soundTextList.size()*(30+3));
        list.setPreferredSize(listSize);
        scrollSize.height = 400;
        setSize(scrollSize);
        setPreferredSize(scrollSize);

        setViewportView(list);
        getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
        getHorizontalScrollBar().setUnitIncrement(SCROLL_INCREMENT);

        ColorScheme colors = lesson.getColorScheme();
        Color background = colors.getColor(true);
        Color foreground = colors.getColor(false);
        setBackground(background);
        list.setBackground(background);
        list.setForeground(foreground);

        renderer = new ListCellFontRenderer
                (defaultFont, soundTextList, foreground);
        list.setCellRenderer(renderer);
        reset(true);
    }

    /** Method to fill the list with random ordered flash card text
     *
     * @param gloss True if to display gloss, false if to display native
     */
    public void reset(boolean gloss)
    {
        // Randomize the list
        String name = "indigenous";
        if (gloss) name = "gloss";
        list.setName(name);
        renderer.selectFont(!gloss);

        int size = soundTextList.size(), index;
        String[] text;

        for (int i=0; i<size; i++)
        {
            index = (int)(Math.random() * size);
            text = soundTextList.remove(index);
            soundTextList.add(text);
        }

        // Load the list with the new data
        String[] newList = new String[size];
        for (int i=0; i<size; i++)
        {   if (gloss) newList[i] = soundTextList.get(i)[0];
            else newList[i] = soundTextList.get(i)[1];
        }
        list.setListData(newList);
    }   // End of reset()

    /** Renderer class to set different cells to different fonts */
    class ListCellFontRenderer extends JLabel implements ListCellRenderer<Object>
    {
    	private final static long serialVersionUID = 1;
    	
        Font    english;
        Font[]  indigenous;
        boolean indigenousSelected;

        Border selected, notSelected;
        Color  color;

        /** Constructor to initialize the default font */
        public ListCellFontRenderer
                (Font english, ArrayList<String[]> soundText, Color color)
        {
            this.english = english;
            this.color = color;
            indigenousSelected = false;

            String   language;
            Font     font;
            KeyboardFonts keyboard = KeyboardFonts.getLanguageFonts();

            int size = soundText.size();
            indigenous = new Font[size];
            for (int i=0; i<size; i++)
            {
                language = soundText.get(i)[SoundData.LANGUAGE];
                font = new Font(null, Font.PLAIN, FONT_SIZE);
                if (!language.equals("English"))
                    font = keyboard.getFont(language);
                indigenous[i] = font;
            }

            selected = BorderFactory.createRaisedBevelBorder();
            notSelected = BorderFactory.createLoweredBevelBorder();
        }

        /** The interface method to render a particular cell
         *
         * @param list The list object
         * @param value The value to render
         * @param index The index to the cell
         * @param isSelected true if this cell is selected
         * @param cellHasFocus true if this cell has focus
         * @return
         */
        public Component getListCellRendererComponent
                (JList<?> list, Object value, int index
                           , boolean isSelected, boolean cellHasFocus)
        {
            // Get text to display.
           if (value.toString().trim().length()==0) value = " ";
           setForeground(color);
           setText(value.toString());
           if (isSelected)
                 setBorder(selected);
           else setBorder(notSelected);
           if (indigenousSelected && indigenous!=null
                                  && indigenous[index]!=null)
                setFont(indigenous[index]);
           else setFont(english);
           return this;
        }

        /** Method to set the indigenous fonts for this class
         *
         * @param true if to select the indigenous font, false otherwise
         */
        public void selectFont(boolean select)
        {  indigenousSelected = select; }

    }       // End of ListCellFontRenderer class
}           // End of FlashScrollBar class
