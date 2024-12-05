/**
 * CardPanel.java
 * @author HarveyD
 * @version 5.00 Beta
 *
 * Copyright 2009-2015, all rights reserved
 */
package org.acorns.lesson.flashCardsV6;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.acorns.visual.*;
import org.acorns.widgets.*;
import org.acorns.data.*;
import org.acorns.lesson.*;

/** Panel to maintain a list of cards for one of the piles */
public class CardPanel extends JPanel implements MouseListener
{
	private final static long serialVersionUID = 1;
	
    private ArrayList<ChoiceButton> cards;
    private ArrayList<double[]> statistics; // [0]=correct, [1]=total
    private int audioSelection;
    private int index;

    LessonsFlashCards lesson;
    CardPiles         parent;
    int 			  cardNum;
    CardControls      controls;

    JPanel cardDisplay;

    public CardPanel(CardPiles parent, LessonsFlashCards lesson
    								 , int cardNum, int cardLength)
    {
        this.parent = parent;
        this.lesson = lesson;
        this.cardNum = cardNum;

        index = -1;
        cards = new ArrayList<ChoiceButton>();
        statistics = new ArrayList<double[]>();

        cardDisplay = new JPanel();
        ColorScheme scheme = lesson.getColorScheme();
        cardDisplay.setBackground(scheme.getColor(true));
        cardDisplay.setForeground(scheme.getColor(false));
        cardDisplay.setBorder(BorderFactory.createEtchedBorder());
        cardDisplay.setLayout(null);

        Dimension panelSize = getCardSize();
        cardDisplay.setSize(panelSize);
        cardDisplay.setMinimumSize(panelSize);
        cardDisplay.setMaximumSize(panelSize);
        cardDisplay.setPreferredSize(panelSize);

        controls = new CardControls(this, lesson, cardNum==0, cardNum == cardLength-1);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(cardDisplay);
        add(controls);

        setOpaque(false);
    }

    /** Method to remove the top card from the pile
     *
     * @return the card removed
     */
    public ChoiceButton removeCard()
    {
        if (index<0) return null;

        ChoiceButton card = cards.get(index);

        card.removeListeners();
        cards.remove(index);
        statistics.remove(index);
        if (cards.size()==0) 
        {  
           index = -1;
        }
        else
        {  if (index == cards.size()) index = 0;
           displayCard();
        }
        return card;
    }

    /** method to add a new card to the pile
     *
     * @param button The card to add
     */
    public void addCard(ChoiceButton card)
    {
        card.removeListeners();
        card.addMouseListener(this);
        card.addMouseMotionListener(parent);
        
        Dimension CardSize = getCardSize();
        card.resizeButton(CardSize);

        cards.add(card);
        statistics.add(new double[]{0,0});

        index = cards.size() - 1; // Display the card just added
        displayCard();
    }

    /** Method to determine if a card from this pile is selected
     *
     * @return true if yes, false otherwise
     */
    public boolean isCardSelected()
    {
        if (index<0 || index>=cards.size()) return false;
        return cards.get(index).isChoiceSelected();
    }

    /* Method to get the card that is selected
     *
     * @param The text associated with the selected entry
     * @param true if gloss text, false otherwise
     * @return true if successful answer
     */
    @SuppressWarnings("static-access")
	public boolean updateStatistics(String data, boolean gloss)
    {
        ChoiceButton card = cards.get(index);
        try
        {
            PictureChoice pictureChoice = card.getPictureChoice();
            PicturesSoundData sounds
                  = pictureChoice.getQuestions(lesson.getLayer());

             Vector<SoundData> soundVector = sounds.getVector();
             SoundData sound = soundVector.get(audioSelection);
             String[] text  = sound.getSoundText();

             // Update statistics count
             boolean match = false;
             double[] stats = statistics.get(index);
             stats[1] +=1.0;
             String value = text[1];
             if (gloss) value = text[0];
             if (value.equals(data)) match = true;

             if (match)
             {   stats[0] += 1.0;
                 sound = lesson.getSound(AcornsProperties.CORRECT);
             }
             else sound = lesson.getSound(AcornsProperties.INCORRECT);

             sound.playBack(null, 0, -1);
             Thread.currentThread().sleep(300);
             statistics.set(index, stats);
             return match;
        }
        catch (Exception e) {};
        return false;
    }

    /** Get the statistics value for the selected card */
    public double[] getStatistics() { return statistics.get(index); }

    /** Method to display another card on the panel
     *
     * @param direction -1 = previous, +1 = next, - = random
     */
    public void displayNewCard(int direction)
    {
        int size = cards.size();
        if (direction>0)
        {
            index++;
            if (index>=size) index = 0;
        }
        else if (direction<0)
        {
            index--;
            if (index<0) index = size - 1;
        }
        else
        {
            if (size<=1) index = -1;
            else if (size==2) { index = 1 - index; }
            else
            {
               int oldChoice = index;
               while ( ((index = (int)(Math.random()*size))
                                                 == oldChoice));
            }
        }
        displayCard();
    }

    /** Move card between piles
     * 
     * @param next true if move to right, false if move to left
     */
    public void moveCard(boolean next)
    {
    	if (index<0) Toolkit.getDefaultToolkit().beep();  
    	else if (next) parent.moveCard(cardNum, cardNum+1);
    	else 	  parent.moveCard(cardNum, cardNum-1);
    }

    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}
    public void mouseClicked(MouseEvent event) {}
    public void mousePressed(MouseEvent event) {}

    public void mouseReleased(MouseEvent event)
    {
        if (parent.doDragDrop()) return;

        ChoiceButton card = (ChoiceButton)event.getSource();
        lesson.select(null);
        card.selectBorders(true, true);
        selectAudio(card);
    }

    /** Echo a sound to display and return the text for it
     *
     * @param pictureChoice The object containing audio information
     * @return Which sound is selected
     */
  private int selectAudio(ChoiceButton card)
  {
      PictureChoice pictureChoice = card.getPictureChoice();
      PicturesSoundData sounds
              = pictureChoice.getQuestions(lesson.getLayer());

      Vector<SoundData> soundVector = sounds.getVector();

      // Randomly pick one to echo.
      int size = soundVector.size();
      audioSelection = (int)(Math.random() * size);

      SoundData sound = soundVector.get(audioSelection);
      if (sound.isRecorded())  sound.playBack(null, 0, -1);
      return audioSelection;
  }

    private Dimension getCardSize()
    {
        Dimension size = lesson.getDisplaySize();
        size.width = size.width * 7 /24 + 25;
        size.height = size.height * 7 / 24 + 25;

        Dimension pictureSize = lesson.getPictureSize();

        if (size.width < pictureSize.width) return size;
        else return pictureSize;
    }

    /** Method to display a particular card */
    private void displayCard()
    {
        cardDisplay.removeAll();
        if (index >= cards.size() || index<0)
        {
            index = -1;
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        ChoiceButton button = cards.get(index);
        cardDisplay.add(button);
        button.setLocation(0, 0);
        lesson.select(null);
        cardDisplay.repaint();
    }
    
 } // End of CardPanel class
