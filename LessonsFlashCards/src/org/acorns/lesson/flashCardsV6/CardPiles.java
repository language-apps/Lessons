/**
 * CardPiles.java
 * @author HarveyD
 * @version 5.00 Beta
 *
 * Copyright 2009-2015, all rights reserved
 */

package org.acorns.lesson.flashCardsV6;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.acorns.data.*;
import org.acorns.widgets.*;

/** Class to contain and manage three piles of flash cards */
public class CardPiles extends JPanel 
        implements MouseMotionListener, ListSelectionListener
{
	private final static long serialVersionUID = 1;
	
    private CardPanel[] piles;
    private ChoiceButton dragButton;

    public CardPiles(LessonsFlashCards lesson)
    {
       piles = new CardPanel[3];
       dragButton = null;

       for (int i=0; i<piles.length; i++)
           piles[i] = new CardPanel(this, lesson, i, piles.length);

       PictureChoice[] choices = lesson.getActivePictureData();
       int activeCount = lesson.getActiveChoices();
       PictureChoice[] randomChoices = new PictureChoice[activeCount];
       for (int i=0; i<activeCount; i++)
           randomChoices[i] = choices[i];

       ChoiceButton choice;
       int select;
       for (int i=activeCount; i>0; i--)
       {   select = (int)(Math.random() * i);
           choice = randomChoices[select].getButton();
           piles[0].addCard(choice);
           randomChoices[select] = randomChoices[i-1];
       }

       setOpaque(false);
       setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
       add(Box.createHorizontalGlue());
       add(piles[0]);
       add(Box.createHorizontalStrut(10));
       add(piles[1]);
       add(Box.createHorizontalStrut(10));
       add(piles[2]);
       add(Box.createHorizontalGlue());
    }

    /** Method to update the statistics when the user selects an item
     *
     * @param data The selected JList text data
     * @param gloss true if gloss display, false if indigenous
     * @return true if found a selected button, false otherwise
     */
    public boolean updateStatistics(String data, boolean gloss)
    {
        for (int i=0; i<piles.length; i++)
        {  if (piles[i].isCardSelected())
           {
              boolean match = piles[i].updateStatistics(data, gloss);
              double[] stats = piles[i].getStatistics();
              if (stats==null || stats[1] == 0) continue;

              double ratio = stats[0] / stats[1];
              int[] minimumToSwitchRight = {  5, 10, -1 };
              if (minimumToSwitchRight[i]!=-1)
              {   double[] ratioRight = { .7, .9, -1 };
                  if (stats[1] >= minimumToSwitchRight[i])
                  {   if (ratio >= ratioRight[i])
                      {   moveCard(i, i+1);
                          return true;
                      }
                  }
              }
              int[] minimumToSwitchLeft  = { -1, 1, 1 };
              if (minimumToSwitchLeft[i]!=-1)
              {
                  double[] ratioLeft = { -1, .8, .9 };
                  if (stats[1] >= minimumToSwitchLeft[i])
                  {   if (ratio < ratioLeft[i])
                      {   moveCard(i, i-1);
                          return true;
                      }
                  }
              }
              if (match) piles[i].displayNewCard(+1);
              return true;
           }  // End if selected
        }     // End for
        return false;
    }         // End updateStatistics()

    /** Method to move all the cards back to the first pile */
    public void reset()
    {   ChoiceButton card;
        for (int i=1; i<piles.length; i++)
        {   while ( (card = piles[i].removeCard()) != null)
            {  piles[0].addCard(card);  }
        }
    }

    /** Method to move a card from one pile to another */
    public void moveCard(int from, int to)
    {   ChoiceButton card = piles[from].removeCard();
        piles[to].addCard(card);
        invalidate();
        repaint();
    }

    // Mouse motion listener methods
    public void mouseMoved(MouseEvent event) {}
    public void mouseDragged(MouseEvent event)
    {  dragButton = (ChoiceButton)event.getComponent(); }

    /** Method to drag a card from one pile to another
     *
     * @return true if drag/drop executed, false otherwise
     */
    public boolean doDragDrop()
    {
        if (dragButton == null) return false;
        ChoiceButton card = dragButton;
        
        try
        {
            CardPanel to;
            CardPanel from
                    = (CardPanel)(dragButton.getParent().getParent());
            dragButton = null;

            // Find which component the mouse is over
            for (int i=0; i<piles.length; i++)
            {   if (piles[i].getMousePosition()!=null)
                {   to = piles[i];
                    if (from == to)  { return false;  }
                    // Perform the DnD operation
                    from.removeCard();
                    to.addCard(card);
                    invalidate();
                    repaint();
                    return true;
                }
            }
        }
        catch (Exception e) {}
        return false;
    }

      /** Method to listen for selections to the JList
    *
    * @param event The object triggering this event
    */
   public synchronized void valueChanged(ListSelectionEvent event)
   {   JList<?> list = (JList<?>)event.getSource();
       boolean gloss = false;

       if (event.getValueIsAdjusting()!=false) return;

       String selection = (String)list.getSelectedValue();
       if (!list.isSelectionEmpty()) 
       {
           list.clearSelection();
           if (list.getName().equals("gloss")) gloss = true;
           boolean updated = updateStatistics(selection, gloss);
           if (!updated) Toolkit.getDefaultToolkit().beep();
       }
   }

}   // End of CardPiles class
