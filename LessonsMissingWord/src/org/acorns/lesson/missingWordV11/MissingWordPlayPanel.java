package org.acorns.lesson.missingWordV11;
/*
 * MagnetPlayComponent.java
 *
 *   @author  HarveyD
 *   @version 6.00
 *
 *   Copyright 2007-2015, all rights reserved
 */



import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;

import org.acorns.language.*;
import org.acorns.data.*;
import org.acorns.lesson.*;
import org.acorns.visual.ColorScheme;

/** Class to execute magnet game lessons */
public class MissingWordPlayPanel extends JPanel implements ActionListener
{
   private final static long serialVersionUID = 1;
   
   private LessonsMissingWord  lesson;
   private ColorScheme  	   colors;      // Object to control fonts
   private SentencePhrases     phrases;       // Object processing a sequence of words

   private DisplaySentence		area;
   private LessonPlayControls   lessonControls;
   private SentenceControls     sentenceControls;
   
   private LessonPopupMenu      popup;       // supplemental popup menu
   private JMenuItem[]          items;       // JMenuItems in the pop up menu

   /** Constructor to initialize the game */
   public MissingWordPlayPanel(LessonsMissingWord lesson)
   {  
      this.lesson = lesson;
      colors = lesson.getColorScheme();
      phrases = new SentencePhrases(lesson, colors);
      
      items = new JMenuItem[9];
      setLayout(new BorderLayout());
      setBackground(new Color(80, 80, 80));
            
      JPanel north = new JPanel();
      north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
      
      area = new DisplaySentence(lesson, phrases);
      sentenceControls = new SentenceControls(lesson, this, phrases, area);
      lessonControls = getControls();

      north.add(sentenceControls);
      add(north, BorderLayout.NORTH);
      add(area, BorderLayout.CENTER);
      add(lessonControls, BorderLayout.SOUTH);
      Score.reset();
      resetGame();
   }

   /** Set playback options differently if both select and show are set to gloss
    * 
    * @param options User display and play back selection
    * @param messageData Array of popup selection text messsages
    */
   public void setPlaybackOptions(boolean[] options, String[] messageData)
   {
	   if (options[AcornsProperties.DISPLAY] != options[AcornsProperties.SELECT] )
		    items[3].setText(messageData[0]);
	   else
	   {
		   if (options[AcornsProperties.DISPLAY])
			    items[3].setText(messageData[1]);
		   else items[3].setText(messageData[5]);
 	  }
   }
   /** Method to reset the game
    *    It creates a new set of magnets and disburses them on the display
    */
   public void resetGame()
   {
	   phrases.reset(false);
	   area.resetGame();
	   sentenceControls.resetGame();
   }  // End of resetGame()

   /** Create the control panel at the bottom of the play screen
    *  @return LessonPictureControls pop up menu
    */
   public final LessonPlayControls getControls()
   {
	  String[] messageData = LanguageText.getMessageList(lesson, 5);
	  
      // Update the popup options if necessary
	  // Reset game
      items[0] = new JMenuItem(LanguageText.getMessage(lesson, 7));
      items[0].addActionListener(this);
      
      // Select next category
      items[1] = new JMenuItem();
	  items[1].setText(messageData[4] + lesson.getCategoryDescription());
      items[1].addActionListener(this);
      items[2] = null;

      // Display and select options
      boolean[]  options = lesson.getOptions();
      items[3] = new JMenuItem();
      items[3].addActionListener(this);
      setPlaybackOptions(options, messageData);

      items[4] = new JMenuItem();
      if (options[AcornsProperties.SELECT])
           items[4].setText(messageData[3]);
      else items[4].setText(messageData[2]);
      items[4].addActionListener(this);
      items[5] = null;

      // Create menu items to adjust font sizes.
      items[6] = new JMenuItem(adjustFontSizeTip(true));
      items[6].addActionListener(this);

      items[7] = new JMenuItem(adjustFontSizeTip(false));
      items[7].addActionListener(this);

      // Create Popup menu
      popup = new LessonPopupMenu(lesson, this, items);
      items[8] = null;

      LessonPlayControls controls
              = new LessonPlayControls(lesson, popup, "MissingWordPlay");

      return controls;

   }    // End of getPictureControls()


   // Get the font adjustment text for either tool tips or a menu item
   private String adjustFontSizeTip(boolean flag)
   {
       int fontSize = colors.getSize();
       String[] fontText = LanguageText.getMessageList("commonHelpSets", 31);
       if (flag)
       {   if (fontSize >= ColorScheme.MAX_FONT_SIZE)
                return fontText[0];
           else return fontText[2] + " " + fontSize;
       }
       else
       {    if (fontSize <= ColorScheme.MIN_FONT_SIZE)
                return fontText[1];
            else return fontText[3] + " " + fontSize;
       }
   }        // End of AdjustFontSizeTip()

   /** Respond to pop up menu and button commands
    *  @param event object triggering the listener
    */
   public void actionPerformed(ActionEvent event)
   {
	  boolean options[] = lesson.getOptions();
	  String[] messageData = LanguageText.getMessageList(lesson, 5);
 
      JMenuItem source = (JMenuItem)event.getSource();
      if (!popup.isArmed()) {  return; }
      popup.cancel();      //Disable the Popup menu.

      // Check if we should be in continuous playback mode
      if (source==items[0])  
      { 
    	  phrases.resetCategory(false); // Not repeating category
    	  resetGame(); 
    	  return; 
      }
      
      if (source == items[1])
      {
    		  phrases.getNextCategory();
    		  String text = lesson.getCategoryDescription();
    		  items[1].setText(messageData[4] + text);
              lesson.setDirty(true);
    		  resetGame();
    		  return;
      }

      // Select display and selection language
      if (source == items[3])
      {
 		 options[AcornsProperties.DISPLAY] = !options[AcornsProperties.DISPLAY];
         lesson.setOptions(options);
         lesson.setDirty(true);
         setPlaybackOptions(options, messageData);
         resetGame();
         return;
      }
      
      if (source == items[4])
      {
    	 options[AcornsProperties.SELECT] = !options[AcornsProperties.SELECT];
    	 lesson.setOptions(options);
         setPlaybackOptions(options, messageData);

		 lesson.setDirty(true);
		 
         if (options[AcornsProperties.SELECT])
         {
        	 items[4].setText(messageData[3]);
         }
         else
        	 items[4].setText(messageData[2]);

         resetGame();
         return;
      }

      // Check if we are to adjust font sizes
      int fontSize = colors.getSize();
      if (source == items[6])
      {
         int newSize = fontSize * 120 / 100;
         if (newSize > ColorScheme.MAX_FONT_SIZE)
             newSize = ColorScheme.MAX_FONT_SIZE;

         if (fontSize < ColorScheme.MAX_FONT_SIZE)
         {  colors.setSize( newSize );
            lesson.setDirty(true);
            items[6].setText(adjustFontSizeTip(true));
            items[7].setText(adjustFontSizeTip(false));
            resetGame();
            return;
         }
      }
      if (source == items[7])
      {
         int newSize = fontSize * 100 / 120;
         if (newSize < ColorScheme.MIN_FONT_SIZE)
             newSize = ColorScheme.MIN_FONT_SIZE;

         if (fontSize > ColorScheme.MIN_FONT_SIZE)
         {  colors.setSize( newSize);
            lesson.setDirty(true);
            items[6].setText(adjustFontSizeTip(true));
            items[7].setText(adjustFontSizeTip(false));
            resetGame();
            return;
         }
      }      // End of checks for various JMenu items

      Toolkit.getDefaultToolkit().beep();
      return;
   }      // End of actionPerformed()
 
   /** The panel to display all of  the phrases for the game
     * 
     * @param size The size of the display panel
     */
   
      public PictureData picture;
      public Color background;
      
      public @Override void paintComponent(Graphics g)
      {
          super.paintComponent(g);
          if (picture != null)
          {
              g.setColor(background);
              Dimension size = getSize();
              g.fillRect(0, 0, size.width, size.height);
                
              if (picture !=null)
              {  
                    
                 Dimension iSize = picture.getSize();
                 double scaleH = 1.0 * size.height / iSize.height;
                 double scaleW = 1.0 * size.width / iSize.width;
                 double scale = Math.min(scaleH, scaleW);
                 iSize.height *= scale;
                 iSize.width *= scale;

                 int x = (size.width - iSize.width)/2;
                 int y = (size.height - iSize.height)/2;
                 BufferedImage image = picture.getImage
                         (this, new Rectangle(x, y, iSize.width, iSize.height));
                 g.drawImage(image,x,y,iSize.width,iSize.height,null);
              }
          }
      }
   
}     // End of QuestionAnswerPlayPanel
