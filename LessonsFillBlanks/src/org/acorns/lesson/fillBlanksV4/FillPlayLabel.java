/*
 * FillPlayLabel.java
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
import java.awt.event.*;
import java.beans.*;

import org.acorns.visual.*;
import org.acorns.widgets.RecordPanel;
import org.acorns.editor.*;
import org.acorns.audio.SoundDefaults;
import org.acorns.data.*;
import org.acorns.lesson.*;
import org.acorns.language.*;

// Nested class that will draw icons that go over the top.
public class FillPlayLabel extends JPanel 
        implements ActionListener, Runnable, PropertyChangeListener
{
   private final int SCORE_HGT = 60, MAX_DELAY=5, DELAY_TIME=1000, BORDER = 75;
   
   private final static long serialVersionUID = 1;
  
   private ColorScheme        colors;
   private LessonsFillBlanks  lesson;
   private FillPanel          fillPanel;
   private RootSoundPanel     soundPanelProperties;
   
   // Information related to missing word
   private Point             range;        // Range of points to play back
   private int               displayPoint; // Starting spot to display
   private int               lastPoint;    // Last node displayed
   private String            missingWord;  // Text of last missing word
   private AnnotationNode[]  blanks;       // List of nodes that are blank
   private AnnotationNode[]  nodeList;     // List of all annotation nodes
   private int[]             blankIndices; // Annotation indices to blank words
   private int               blankPtr;     // Pointer to current missing word
   
   // Instance variables to process the control buttons.
   private LessonPlayControls lessonControls; 
   private LessonPopupMenu    popup;       // supplemental popup menu
   private JMenuItem[]        items;       // JMenuItems in the pop up menu
   private JTextField         field;       // Text field for entering words
   private RecordPanel        recorder;    // Recorder panel for audio speech recognition
   private JLabel             dialog;      // Label to show correct answer
   private KeyboardListener   keyListener; // Key listener for the text fields

   // Start of constructor.            
   public FillPlayLabel
           (ColorScheme colors, FillPanel panel,LessonsFillBlanks lesson)   
   {  
      // Initialize instance variables
      this.colors     = colors;
      this.fillPanel  = panel;
      this.lesson     = lesson;      
      this.items      = new JMenuItem[8];
      // Get the ACORNS property change listener.
      PropertyChangeListener[] pcl = Toolkit.getDefaultToolkit()
                                 .getPropertyChangeListeners("SoundListeners");
     if (pcl.length>0) soundPanelProperties = (RootSoundPanel)pcl[0];
   
      setLayout(new BorderLayout());
      
      // Create the score panel and add it to the north part.
      Dimension size = lesson.getDisplaySize();
      size = new Dimension(size.width, size.height-30);
      setSize(size);
      setMinimumSize(getSize());
      setPreferredSize(getSize());
   
      ScorePanel scores = new ScorePanel
                    (lesson, new Dimension(size.width, SCORE_HGT), colors, this);
      add(scores, BorderLayout.NORTH);
      field = scores.getScoreTextField();
      recorder = scores.getScoreRecorder();
      field.setFocusTraversalKeysEnabled(false);
      field.setRequestFocusEnabled(true);
      keyListener = new KeyboardListener();
      dialog = new JLabel();  // Label to display correct answers
  
      // Add the button controls on the bottom.
      lessonControls = getPictureControls();
      add(lessonControls, BorderLayout.SOUTH);    

      addPropertyChangeListener("PlayBack", this);
      new Thread(this).start();
      stopPlay();
      initialize();  // Initialize the lesson for display.
   }
   
   // Method to paint this component on a drag operation.
   public @Override void paintComponent(Graphics graphics)
   {  
      super.paintComponent(graphics);      

      Rectangle visible = this.getVisibleRect();
      visible.y += SCORE_HGT;
      visible.height -= SCORE_HGT;

      setBackground(colors.getColor(true));

      Border border = BorderFactory.createRaisedBevelBorder();
      setBorder(border);

      AnnotationData sound
              = (AnnotationData)soundPanelProperties.getAnnotationData(null);
      AnnotationImage anImage = new AnnotationImage();
      lastPoint = anImage.drawAnnotation
         (graphics, sound, visible.width, visible ,false, displayPoint, colors);
      if (lastPoint >1) lastPoint--;
   }  // End of PaintComponent

   /** Initialize lesson for next round of user interaction
    *
    */
   public final void initialize()
   {  // Make sure the menu items are correct.
      if (items[1]!=null)
          items[1].setText(adjustContinuousMode(lesson.getPlayback()));
      if (items[3]!=null) items[3].setText(adjustDifficultyLevelTip(true));
      if (items[4]!=null) items[4].setText(adjustDifficultyLevelTip(false));
      if (items[6]!=null) items[6].setText(adjustFontSizeTip(true));
      if (items[7]!=null) items[7].setText(adjustFontSizeTip(false));
  
      // Get the annotation data and set all nodes visible
      AnnotationData sound 
              = (AnnotationData)soundPanelProperties.getAnnotationData(null);
      sound.setAllVisible();
      sound.clearAllHighlights();
     
       // Make sure the components have the correct language.
      String language = sound.getKeyboard();
      KeyboardFonts kbFonts = KeyboardFonts.getLanguageFonts();
      kbFonts.setFont(language, dialog);
      field.removeKeyListener(keyListener);
      kbFonts.setFont(language, field);
      field.addKeyListener(keyListener);

      // Get the annotation nodes and count
      AnnotationNode[] nodes = sound.getAnnotationNodes();
      int nodeCount = sound.getAnnotationSize();
      
      // Create list of words that can be blank
      int[] freeNodes = new int[nodeCount];
      int   freeCount = 0;

      for (int n=0; n<nodeCount; n++)
      {  if (nodes[n].getText().length()!=0) 
         {  
            freeNodes[freeCount++] = n;
         }
      }
      nodeList = new AnnotationNode[freeCount];
      for (int f=0; f<freeCount; f++)  nodeList[f] = nodes[freeNodes[f]];

      // Play entire story if in continuous play mode
      if (lesson.getPlayback())
      {  range = new Point(0, -1);
         displayPoint = 0;
         play(range);  // Echo the appropriate sound
         field.requestFocusInWindow();
         return;
      }

      // Choose the blank words
      int difficultyLevel = Score.getDifficultyLevel();
      int blankCount = freeCount * difficultyLevel 
                                 * LessonsFillBlanks.MIN_ANNOTATION_PCT / 100;
      if (blankCount==0) blankCount = 1;
      
      int choice;
      blanks = new AnnotationNode[blankCount];
      blankIndices = new int[blankCount];
      
      for (int b=0; b<blankCount; b++)
      {   choice = (int)(Math.random()*freeCount);
          nodes[freeNodes[choice]].setVisible(false);
          freeNodes[choice] = freeNodes[--freeCount];
      }
      
      // Sort the nodes that will be blank.
      int blankIndex = 0;
      for (int n=0; n<nodeCount; n++)
      {   if (!nodes[n].isVisible())
          {  blanks[blankIndex] = nodes[n]; 
             blankIndices[blankIndex++] = n;
          }          
      }

      // Set up for proper display with missing word
      displayPoint = 1;
      lastPoint    = -1;
      blankPtr     = 0;
      range = new Point(0, (int)blanks[blankPtr].getOffset());
      blanks[blankPtr].setHighlight(true);
      missingWord = blanks[blankPtr].getText();
      missingWord = missingWord.replaceAll("\\\\n", " ");
      missingWord = missingWord.replaceAll("\\s+", " ").trim();
      
      stopPlay();
      play(range);  // Echo the appropriate sound
      revalidate();
      repaint();
      field.requestFocusInWindow();
   }
   
    @SuppressWarnings("static-access")
   public void run()
   {  try {Thread.currentThread().sleep(1000);} 
       catch (InterruptedException iex) {}
       field.requestFocus();
   }
   
   /** Methods for comparing, playing, stop playback of audio
    */
    
   public double compare(Point select)
   {
	   stopPlay();
       AnnotationData sound 
               = (AnnotationData)soundPanelProperties.getAnnotationData(null);
       return recorder.compare(select, sound);
       
   }
   public void play(Point select)
   {  
	   stopPlay();
       AnnotationData sound 
               = (AnnotationData)soundPanelProperties.getAnnotationData(null);
       if (!sound.playBack(this, select.x, select.y))
       { JOptionPane.showMessageDialog
                 (null, LanguageText.getMessage(lesson, 3));
       }
   }
   
   /** Stop current play back
    */
   public void stopPlay()
   {   AnnotationData sound 
               = (AnnotationData)soundPanelProperties.getAnnotationData(null);
       sound.stopSound();
   }
   
   /** Determine if answer was correct
    *  @return true if yes, false otherwise
    */
   public boolean correct()
   {   if (blankPtr<0) return true;  // Return true if at end of the recording
          
       String answer = getMissingWord();
       String entry  
           = field.getText().replaceAll("\\s+", " ").toLowerCase().trim();
       return answer.equals(entry);       
   }
   
   /** Determine if answer is a misspelling
    *  @return true if a spelling error, false otherwise
    */
   public boolean spell()
   {   String entry  
           = field.getText().replaceAll("\\s+", " ").toLowerCase().trim();
       String answer = getMissingWord();
       
       int distance = SpellCheck.editDistance(entry, answer);
       int maxDistance = 1;
       if (answer.length()>=5) maxDistance = 2;
       return (distance<=maxDistance);
   }

   /** Get the missing word
    *  @return missing word string
    */
   public String getMissingWord()
   {   String answer = missingWord.toLowerCase(); 
       
       int index = answer.indexOf(" (");
       if (index >0) answer = answer.substring(0, index);
       return answer;
   }
   
   /** Proceed to the next blank word  */
   public void nextPlay()
   {   
	   if (lesson.getPlayback()) { initialize(); return; }
       if (blankPtr == -1 && displayPoint==1) initialize();
       else
       {  if (blankPtr>=0)
          {   blanks[blankPtr].setVisible(true);
              blanks[blankPtr].setHighlight(false);
              field.setText("");
              blankPtr++;
          }

          if (blankPtr<0 || blankPtr >= blanks.length)
          {  // Handle case where more to display, but no blanks
             if (range.y>0 && lastPoint>= blankIndices[blankPtr-1])
             {   if (blankPtr<0) blankPtr = -1;
                 else blankPtr = -2;
                 lastPoint = -1;
                 range = new Point(range.y, -1);
             }
             else
             {   range = new Point(-1, -1);
                 displayPoint = 1;
                 lastPoint = -1;
                 blankPtr = -1;
             }
          }
          else
          {  // Set up for proper display with missing word
             range = new Point(range.y, (int)blanks[blankPtr].getOffset());
             blanks[blankPtr].setHighlight(true);
             missingWord = blanks[blankPtr].getText();
             missingWord = missingWord.replaceAll("\\\\n", " ");
             missingWord = missingWord.replaceAll("\\s+", " ").trim();
         
             if (lastPoint < blankIndices[blankPtr] && lastPoint>displayPoint)
             {   displayPoint = (int)(displayPoint+(lastPoint-displayPoint)*.5);
                 lastPoint = -1;
             }
          }
          
          try
          {  Thread.sleep(1000);
             play(range);  // Echo the appropriate sound
          }
          catch (Exception e) {}
       }
       repaint();
   }
   
   // Verify the answer
   public void verify()
   {
       if (blankPtr<0 || lesson.getPlayback())
       {   Toolkit.getDefaultToolkit().beep();
           return;
       }

       boolean ok = false;
       field.selectAll();
	   SoundData feedback;
	   
	   int from = findFromOffset(range.y);
	   Point spot = new Point(from, range.y);
	   double similarity = compare(spot);
	   double correct = SoundDefaults.getDTWCorrectness(SoundDefaults.CORRECT);
	   double close = SoundDefaults.getDTWCorrectness(SoundDefaults.CLOSE);
	   System.out.println("Similarity = " + similarity + " " + spot);
	   
       if (similarity >= correct || correct()) 
       {   ok = true;
           feedback = lesson.getSound(AcornsProperties.CORRECT);
       }
       else if (spell() || similarity>=close)
            feedback = lesson.getSound(AcornsProperties.SPELL);
       else feedback  = lesson.getSound(AcornsProperties.INCORRECT);

       int count = 0;
       feedback.playBack(null, 0, -1);
       try
       {
	       while(feedback!=null && feedback.isActive())
	       {  if (count++==MAX_DELAY) feedback.stopSound();
	          Thread.sleep(DELAY_TIME);
	       }
	       Thread.sleep(DELAY_TIME);
       }
       catch (Exception e) {}
       if (similarity >= correct || correct()) nextPlay();
       else           play(range);

       Score.nextScore(ok);
       Score.calculateScore();
   }
   
   /** Create the control panel at the bottom of the play screen
    *  @return LessonPictureControls pop up menu
    */
   public final LessonPlayControls getPictureControls()
   {
      // Reset Option
      items[0] = new JMenuItem(LanguageText.getMessage(lesson, 10));
      items[0].addActionListener(this);

      // Initially set continuous playback off
      items[1] = new JMenuItem(adjustContinuousMode(false));
      items[1].addActionListener(this);

      // Create menu items for changing the difficulty level
      items[2] = null;
      items[3] = new JMenuItem(adjustDifficultyLevelTip(true));
      items[3].addActionListener(this);
      
      items[4] = new JMenuItem(adjustDifficultyLevelTip(false));
      items[4].addActionListener(this);
         
      // Create menu items to adjust font sizes.
      items[5] = null;
      items[6] = new JMenuItem(adjustFontSizeTip(true));
      items[6].addActionListener(this);
               
      items[7] = new JMenuItem(adjustFontSizeTip(false));
      items[7].addActionListener(this);

      // Create Popup menu
      popup = new LessonPopupMenu(lesson, this, items);
			    
      LessonPlayControls controls = new LessonPlayControls
                                             (lesson, popup, "AnnotationPlay");
      return controls; 
   }    // End of getPictureControls()

   /** Turn on and off the continuous mode for story layback
    *
    * @param flag True for continuous mode, false otherwise
    * @return
    */
   private String adjustContinuousMode(boolean flag)
   {   String[] continuousPlay = LanguageText.getMessageList(lesson, 4);
       if (lesson.getPlayback())
             return continuousPlay[0];
       else  return continuousPlay[1];
   }

   // Get the difficulty level text for either tool tips or a menu item
   private String adjustDifficultyLevelTip(boolean flag)
   {
      String[] difficulties = LanguageText.getMessageList(lesson, 5);
      String[] difficultyLevel = LanguageText.getMessageList(lesson, 6);

       int level = Score.getDifficultyLevel();
       if (flag)
       {   if (level >= Score.MAX_DIFFICULTY)
                return difficultyLevel[0];
           else return difficultyLevel[2] + " " + difficulties[level-1];
       }
       else
       {   if (level <= Score.MIN_DIFFICULTY)
                return difficultyLevel[1];
           else return difficultyLevel[3] + " " + difficulties[level-1];
       }
   }        // End of AdjustDifficultyLevelTip()
   
   // Get the font adjustment text for either tool tips or a menu item
   private String adjustFontSizeTip(boolean flag)
   {   int fontSize = colors.getSize();
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
      field.requestFocus();   

      AnnotationData data 
              = (AnnotationData)fillPanel.getAnnotationData(null);
      data.getAnnotationLevel();
      int level = Score.getDifficultyLevel();
      
      // Handle keyboard commands
      String action = event.getActionCommand();
      if (action.equals("down")) { stopPlay(); return; }
     
      if (action.equals("up"))
      {   
          if (blankPtr<0 || lesson.getPlayback())
              Toolkit.getDefaultToolkit().beep();
          else
          {  dialog.setText(missingWord);
             JOptionPane.showMessageDialog
                     (null, dialog, LanguageText.getMessage(lesson, 7)
                                  , JOptionPane.PLAIN_MESSAGE);
          }
          return;           
      }
      if (action.equals("left")) 
      {
    	  int from = findFromOffset(range.y);
   	   	  Point spot = new Point(from, range.y);
   	      play(spot);
          return;           
      }
      if (action.equals("right"))  
      {   nextPlay(); 
          return; 
      }
      if (action.equals("check"))
      {
    	  verify();
    	  return;
      }
            
      JMenuItem source = (JMenuItem)event.getSource(); 
      if (!popup.isArmed()) {  return; }
      popup.cancel();      //Disable the Popup menu.         

      // Check if this is the reset option
      if (source==items[0])  
      {  
    	  initialize(); return;  
      }

      // Check if we should be in continuous playback mode
      if (source==items[1])
      {   lesson.setPlayback(!lesson.getPlayback());
          this.adjustContinuousMode(lesson.getPlayback());
          lesson.setDirty(true);
          initialize();
          return;
      }

      // Check if we are to Adjust difficulty level
      if (source == items[3])
      {  if (level<Score.MAX_DIFFICULTY)
         {   Score.setDifficultyLevel(++level);
             lesson.setDirty(true);
             initialize();
             return;
         }
      }
      if (source == items[4])
      {   if (level>Score.MIN_DIFFICULTY)
          {  Score.setDifficultyLevel(--level);
             lesson.setDirty(true);
             initialize();
             return;
          }
      }

      // Check if we are to adjust font sizes
      int fontSize = colors.getSize();
      if (source == items[6])
      {  int newSize = fontSize * 120 / 100;
         if (newSize > ColorScheme.MAX_FONT_SIZE)
             newSize = ColorScheme.MAX_FONT_SIZE;
         
         if (fontSize < ColorScheme.MAX_FONT_SIZE)
         {  colors.setSize( newSize );
            lesson.setDirty(true);
            initialize();
            return;
         }
      }
      if (source == items[7])
      {  int newSize = fontSize * 100 / 120;
         if (newSize < ColorScheme.MIN_FONT_SIZE)
             newSize = ColorScheme.MIN_FONT_SIZE;

         if (fontSize > ColorScheme.MIN_FONT_SIZE)
         {  colors.setSize( newSize);
            lesson.setDirty(true);
            initialize();
            return;
         }
      }      // End of checks for various JMenu items               
      
      Toolkit.getDefaultToolkit().beep();
      return;
   }      // End of actionPerformed()
   
   /** Method to listen for sound replay events */
   public void propertyChange(PropertyChangeEvent event)
   { 
          soundPanelProperties.getAnnotationData(null);

          long oldSpot = Long.parseLong(event.getOldValue().toString());
          long newSpot = Long.parseLong(event.getNewValue().toString());

          AnnotationNode oldWord = findNode(oldSpot);
          if (oldWord!=null)
          {  if (!oldWord.isHighlight()) oldWord = null;
             else oldWord.setHighlight(false);
          }

          AnnotationNode newWord = findNode(newSpot);
          if (newWord!=null)
          {   if (newWord.isHighlight()) newWord = null;
              else
              {   newWord.setHighlight(true);
                  Point spot = newWord.getDisplayPoint();
                  Rectangle visible = getVisibleRect();
                  if (spot!=null && visible!=null 
                                 && spot.y + BORDER >visible.height + visible.y)
                     displayPoint += 1;
              }
          }
          if (oldWord!=null || newWord!=null)   repaint();
   }

   /** Method to find the annotation node matchin a sound frame */
   private AnnotationNode findNode(long spot)
   {   if (spot<=0) return null;
       for (int i=0; i<nodeList.length - 1; i++)
       {  if (nodeList[i+1].getOffset()>spot) return nodeList[i]; }
       return nodeList[nodeList.length - 1];
   }
   
   private int findFromOffset(long spot)
   {   
	   if (spot<=0) return 0;
	   if (nodeList[0].getOffset()>=spot) return 0;
	   
	   for (int i=0; i<nodeList.length - 1; i++)
	   {   
		   int offset = (int)nodeList[i].getOffset();
		   if (nodeList[i+1].getOffset()>=spot) return offset; }
	   return (int)nodeList[nodeList.length - 1].getOffset();
   }
  

  /**
   * Internal keyboard listener class
   */
  public class KeyboardListener implements KeyListener 
  {
    public void keyTyped (KeyEvent event) {}

    public void keyPressed (KeyEvent event)
    {   int code = event.getKeyCode();
        try
        {  switch (code)
           {   case KeyEvent.VK_ENTER:
        	   		verify();
                    break;
                
               case KeyEvent.VK_UP:
                   if (blankPtr<0 || lesson.getPlayback())
                       Toolkit.getDefaultToolkit().beep();
                   else
                   {  dialog.setText(missingWord);
                      JOptionPane.showMessageDialog
                              (null, dialog, LanguageText.getMessage(lesson, 7)
                                           , JOptionPane.PLAIN_MESSAGE);
                   }
                   break;
               case KeyEvent.VK_RIGHT:
                   nextPlay();
                   break;
               case KeyEvent.VK_LEFT:
                   play(range);  // Replay the appropriate sound
                   break; 
               case KeyEvent.VK_DOWN:
                   stopPlay();
                   break;
           }
       }
       catch (Exception e) {}
        
       field.requestFocus();
    }
    public void keyReleased (KeyEvent event) {} 
  }   
}     // End of FillPlayLabel