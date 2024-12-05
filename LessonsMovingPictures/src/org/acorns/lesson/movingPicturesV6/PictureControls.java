/**
 * PictureControls.java
 * @author HarveyD
 * @version 4.00 Beta
 *
 * Copyright 2009-2015, all rights reserved
 */
package org.acorns.lesson.movingPicturesV6;

import javax.swing.*;
import java.awt.*;

import org.acorns.widgets.*;

/** Class to control the animation of a particular picture */
public class PictureControls 
{
    private static final int DIRECTIONS = 16, DISTANCE = 2;
    private static final int MIN_KILL = 20000, MAX_KILL = 40000;
    private static final int MIN_CHANGE = 3000, MAX_CHANGE = 10000;

    private int time;      // Current time in milliseconds
    private int change;    // Time to change directions
    private int direction; // Current direction picture is moving

    private static int killTime; // current kill time in milliseconds
    private static int kill;     // time to select another picture

    private int[][] deltas
        = { { 0,  4}, { 1,  3}, { 2,  2}, { 3,  1}
          , { 4,  0}, { 3, -1}, { 2, -2}, { 1, -3}
          , { 0, -4}, {-1, -3}, {-2, -2}, {-3, -1}
          , {-4,  0}, {-3,  1}, {-2,  2}, {-1,  3} };

    /** Constructor to initialize parameters for this picture */
    public PictureControls()   {  resetPicture();  }

    /** Method to move the picture in question within the component
     *
     * @param parent The panelholding the picture
     * @param button The button containing the picture
     * @param calling interval in milliseconds
     * @return true if can move, false if need time expires
     */
    public boolean movePicture
            (JComponent parent, ChoiceButton button, int interval)
    {
        if (killTime >= kill) return false;

        newDirection();

        time += interval;
        killTime += interval;

        Rectangle bounds = button.getBounds();
        Rectangle parentBounds = new Rectangle(parent.getSize());
        Point location = newLocation(parentBounds, bounds);
        button.setLocation(location);
        return true;
    }   // End of movePicture()

    /** Reset the parameters for this picture */
    public void resetPicture()
    {
        time = change = killTime = 0;
        kill = (int)(Math.random()*(MAX_KILL-MIN_KILL+1))+MIN_KILL;
        newDirection();
    }

    /** Method to compute a component's new location
     *
     * @param bounds The bounds of the parent component
     * @param location The picture's current position
     * @return The new location
     */
    private Point newLocation(Rectangle bounds, Rectangle location)
    {
        int deltaX = deltas[direction][0] * DISTANCE;
        int deltaY = deltas[direction][1] * DISTANCE;
        
        if (!bounds.intersection(location).equals(location))
		{
        	location.x = (int)(Math.random()*(bounds.width/2));
        	location.y = (int)(Math.random()*(bounds.height/3));
		}

        Rectangle newSpot = new Rectangle(location);
        newSpot.translate(deltaX, deltaY);

        if (bounds.intersection(newSpot).equals(newSpot))
            return new Point(newSpot.x, newSpot.y);

        // Change direction but don't move this time.
        direction = (int)(Math.random() * DIRECTIONS);
        change = time;
        return new Point(location.x, location.y);
    }

    /** Method to compute the next direction for a component to move */
    private void newDirection()
    {   if (time >= change)
        {  direction = (int) (Math.random() * DIRECTIONS);
           change = time +
              (int)(Math.random()*(MAX_CHANGE-MIN_CHANGE+1))+MIN_CHANGE;
        }
    }
}  // End of PictureControls class
