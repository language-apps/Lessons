/**
 * PlayPanel.java
 * @author HarveyD
 * @version 5.00 Beta
 *
 * Copyright 2009-2015, all rights reserved
 */

package org.acorns.lesson.storyBookV5;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

import org.acorns.visual.*;
import org.acorns.data.*;
import org.acorns.lesson.*;
import org.acorns.language.*;

/** Panel for executing a story book lesson */
public class PlayPanel extends JPanel implements ActionListener
{
	private final static long serialVersionUID = 1;
    private final static int PICTURE_HEIGHT = 200, CONTROL_HEIGHT = 40;

    private LessonsStoryBook lesson;
    private StoryScrollPane  story;
    private BufferedImage    image;
    private PictureData      picture;

    private LessonPlayControls lessonControls;
    private LessonPopupMenu    popup;   // supplemental popup menu
    private JMenuItem[]        items;   // JMenuItems in the pop up menu


    public PlayPanel(LessonsStoryBook lesson)
    {
        this.lesson = lesson;
        this.items  = new JMenuItem[2];

        setOpaque(true);
        setBackground(new Color(80,80,80));

        // The panel containing the story book picture
        Dimension playSize = lesson.getDisplaySize();
        picture = lesson.getColors().getPicture();
        playSize.height -=30;
        picture.reloadImages(true, null);
        setSize(playSize);
        setMinimumSize(playSize);
        setPreferredSize(playSize);
        setMaximumSize(playSize);
 
        // The controls to control the audio playback
        PlayControls controls = new PlayControls();

        // Determine size of the story book picture in the play component
        int pictureSize = picture.getSize().height;
        if (pictureSize > playSize.height - PICTURE_HEIGHT)
        	pictureSize = playSize.height - PICTURE_HEIGHT;

        // The text that goes with the story
        Dimension storySize = new Dimension(playSize.width, playSize.height - pictureSize);
        story = new StoryScrollPane(lesson, storySize);
        story.setEditable(false);

        // The lesson navigation buttons
        lessonControls = getPictureControls();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createVerticalStrut(pictureSize));
        add(controls);
        add(story);
        add(lessonControls);
        repaint();
    }

    /** Draw the background picture */
    public @Override void paintComponent(Graphics page)
    {  super.paintComponent(page);
       if (image==null)
             image = picture.getImage(this, new Rectangle(0,0,-1,-1));

       BufferedImage newImage = image;
       if (picture.getNumberFrames()!= 1)
       {
          newImage = picture.getImage(this, new Rectangle(0,0,-1,-1));
       }

      Dimension size = getSize();
      int x = (size.width - newImage.getWidth()) /2;
      int y = 0; //(size.height - newImage.getHeight() - PICTURE_HEIGHT)/2;
      
      page.drawImage(newImage,
             x, y, newImage.getWidth(), newImage.getHeight(), this);
    }

   /** Create the control panel at the bottom of the play screen
    *  @return LessonPictureControls pop up menu
    */
   public final LessonPlayControls getPictureControls()
   {
      // Create menu items to adjust font sizes.
      items[0] = new JMenuItem(adjustFontSizeTip(true));
      items[0].addActionListener(this);

      items[1] = new JMenuItem(adjustFontSizeTip(false));
      items[1].addActionListener(this);

      // Create Popup menu
      popup = new LessonPopupMenu(lesson, this, items);

      LessonPlayControls controls 
              = new LessonPlayControls(lesson, popup, "StoryPlay");

      return controls;

   }    // End of getPictureControls()

   // Get the font adjustment text for either tool tips or a menu item
   private String adjustFontSizeTip(boolean flag)
   {
       ColorScheme colors = lesson.getColors();
       int fontSize = colors.getSize();
       String[] fontControls = LanguageText.getMessageList("commonHelpSets",31);
       if (flag)
       {   if (fontSize >= ColorScheme.MAX_FONT_SIZE)
                return fontControls[0];
           else return fontControls[2] + " " + fontSize;
       }
       else
       {    if (fontSize <= ColorScheme.MIN_FONT_SIZE)
                return fontControls[1];
            else return fontControls[3] + " " + fontSize;
       }
   }        // End of AdjustFontSizeTip()

   /** Respond to pop up menu and button commands
    *  @param event object triggering the listener
    */
    public void actionPerformed(ActionEvent event)
    {
       ColorScheme colors = lesson.getColors();

       JMenuItem source = (JMenuItem)event.getSource();
       if (!popup.isArmed()) {  return; }
       popup.cancel();      //Disable the Popup menu.

       // Check if we are to adjust font sizes
       int fontSize = colors.getSize();
       if (source == items[0])
       {
          int newSize = fontSize * 120 / 100;
          if (newSize > ColorScheme.MAX_FONT_SIZE)
              newSize = ColorScheme.MAX_FONT_SIZE;

          colors.setSize( newSize );
          lesson.setDirty(true);
          lesson.displayLesson();
          items[0].setText(adjustFontSizeTip(true));
          items[1].setText(adjustFontSizeTip(false));
          return;

       }
       if (source == items[1])
       {
          int newSize = fontSize * 100 / 120;
          if (newSize < ColorScheme.MIN_FONT_SIZE)
              newSize = ColorScheme.MIN_FONT_SIZE;

          colors.setSize( newSize);
          lesson.setDirty(true);
          lesson.displayLesson();
          items[0].setText(adjustFontSizeTip(true));
          items[1].setText(adjustFontSizeTip(false));
          return;
       }      // End of checks for various JMenu items

       Toolkit.getDefaultToolkit().beep();
       return;
    }      // End of actionPerformed()

    /** Nested class to control the audio play back */
    class PlayControls extends JPanel implements ActionListener
    {
    	private final static long serialVersionUID = 1;

        public PlayControls()
        {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            String[] playControls = LanguageText.getMessageList(lesson, 5);
            add(Box.createHorizontalGlue());
            add(makeButton("<<", playControls[0], AcornsProperties.REPLAY));
            add(Box.createHorizontalStrut(20));
            add(makeButton("||", playControls[1], AcornsProperties.PAUSE));
            add(Box.createHorizontalStrut(20));
            add(makeButton("X", playControls[2], AcornsProperties.STOP));
            add(Box.createHorizontalStrut(20));
            add(makeButton(">", playControls[3], AcornsProperties.PLAY));
            add(Box.createHorizontalGlue());
            setOpaque(false);

            Dimension size = new Dimension(400, CONTROL_HEIGHT);
            setSize(size);
            setPreferredSize(size);
            setMaximumSize(size);
            setMinimumSize(size);
        }
        /** Method to make a button with a tool tip */
        private JButton makeButton(String name, String toolTip, int iconName)
        {
        	ImageIcon icon = lesson.getIcon(iconName, 35);
            JButton button = new JButton(icon);
            button.setName(name);
            button.setToolTipText(toolTip);
            button.addActionListener(this);
            button.setPreferredSize(new Dimension(35,35));
            return button;
        }

        /** Listener to handle the audio playback controls */
        public void actionPerformed(ActionEvent event)
        {   JButton button = (JButton)event.getSource();
            StoryScrollPane pane = story;
            if (button.getName().equals("<<")) { pane.rewind(); }
            else if (button.getName().equals("||")) { pane.pause(); }
            else if (button.getName().equals("X")) { pane.stop(); }
            else if (button.getName().equals(">")) { pane.play(); }
        }   // End of actionPerformed()
    }       // End of PlayControls class
}           // End of PlayPanel class
