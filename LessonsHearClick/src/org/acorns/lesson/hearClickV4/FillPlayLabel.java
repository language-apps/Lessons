/*
 * FillPlayLabel.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */

package org.acorns.lesson.hearClickV4;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.*;
import org.acorns.visual.*;
import org.acorns.editor.*;
import org.acorns.data.*;
import org.acorns.lesson.*;
import org.acorns.language.*;

/** class that will handle play mode for hear and click lessons */
public class FillPlayLabel extends JPanel implements ActionListener, Runnable
{
   private final static long serialVersionUID = 1;

   // Height of the score panel at the top, and control panel at the bottom
   private final int SCOREHEIGHT = 60, CONTROLHEIGHT = 30;
   
   // Number of rows and columns to display the pictures
   private final int ROWS = 4, COLUMNS = 5;
   
   private LessonsHearClick   lesson;
   private RootSoundPanel     soundPanelProperties;
   
   // Annotation data objects
   private ImageAnnotationData annotations;

   private int[]   selections;             // List of selected nodes
   private int[]   pictureList;            // List of node to picture map

   private int     select;                 // Offset to current selection
   private long    offset;                 // Sound Offset at start of selection
   private boolean continuousPlay;         // Continuous play option
   private boolean start;                  // Flags to control play thread
   private BufferedImage image;
   
   // Instance variables to process the control buttons.
   private JPanel             displayPanel;   // Panel for annotation pictures.
   private LessonPlayControls lessonControls; // Panel for control buttons
   private LessonPopupMenu    popup;          // supplemental popup menu
   private JMenuItem[]        items;          // JMenuItems in the pop up menu
   private Thread             playThread;     // Thread for continuous playback
   
   // Start of constructor.            
   public FillPlayLabel
           (ColorScheme colors, FillPanel panel,LessonsHearClick lesson)   
   {  
      // Initialize instance variables
      this.lesson           = lesson;      

      PictureData picture = colors.getPicture();
      Dimension playSize = lesson.getDisplaySize();
      if (picture!=null)
           image = picture.getImage
              (this, new Rectangle(0,0,playSize.width,playSize.height));
      
      items = new JMenuItem[4];
      continuousPlay = false;
      
      // Get the ACORNS property change listener for annotation data.
      PropertyChangeListener[] pcl = Toolkit.getDefaultToolkit()
                                 .getPropertyChangeListeners("SoundListeners");
      
      if (pcl.length>0) soundPanelProperties = (RootSoundPanel)pcl[0];
      
      // Define the layout and size of the play panel
      setLayout(new BorderLayout());
      Dimension size = lesson.getDisplaySize();
      size = new Dimension(size.width, size.height - 30);
      setSize(size);
      setMinimumSize(size);
      setPreferredSize(size);
      setBackground(new Color(80,80,80)); 
 
      // Create score panel at the north.
      ScorePanel score 
              = new ScorePanel(lesson, new Dimension(size.width, SCOREHEIGHT), colors, this);
      add(score, BorderLayout.NORTH);
  
      // Add the button controls on the bottom.
      lessonControls = getPictureControls();
      add(lessonControls, BorderLayout.SOUTH);      
      
      // Create the center display panel with annotation buttons
      displayPanel = new JPanel();
      Dimension center = new Dimension
                    (size.width, size.height - CONTROLHEIGHT - SCOREHEIGHT);
      displayPanel.setSize(center);
      displayPanel.setMinimumSize(center);
      displayPanel.setPreferredSize(center);
      displayPanel.setOpaque(false);
      displayPanel.setFocusable(true);
      
      // Create the button components for the display area
      Dimension buttonSize 
              = new Dimension(center.width/ROWS, center.height/COLUMNS);
     
      JButton button;
      displayPanel.setLayout(new GridLayout(ROWS, COLUMNS));
      for (int row = 0; row<ROWS; row++)
      {   for (int column = 0; column<COLUMNS; column++)
          {
              button = new PictureButton(buttonSize, colors);
              button.addActionListener(this);
              displayPanel.add(button);
          }
      }
      add(displayPanel, BorderLayout.CENTER);      
      
      // Start the continuous play thead
      annotations = (ImageAnnotationData)
                                   soundPanelProperties.getAnnotationData(this);
      playThread = new Thread(this);
      playThread.start();  
   }
   
   // Method to paint this component on a drag operation.
   public @Override void paintComponent(Graphics graphics)
   {  super.paintComponent(graphics);

       if (image!=null)
          graphics.drawImage
           (image, 0, 0, image.getWidth(), image.getHeight(), this);
   }
  
   /** Initialize lesson for next round of user interaction
    *
    */
   public synchronized void initialize()
   {
      // Make sure the menu items are correct.
      if (items[1]!=null) items[1].setText(adjustDifficultyLevelTip(true));
      if (items[2]!=null) items[2].setText(adjustDifficultyLevelTip(false));
      if (items[3]!=null) items[3].setText(adjustContinuousPlayTip(continuousPlay));

      // Choose the blank words
      int difficultyLevel = Score.getDifficultyLevel();
      int pictureCount = difficultyLevel * 4;
      if (pictureCount > ROWS*COLUMNS) pictureCount = ROWS*COLUMNS;
      pictureList = getPictureList();
      selections = displayPictures
              (displayPanel, pictureList, pictureCount);
      select = 0;
      offset = 0;
      repaint();
   }
  
   /** Method to get part of sound wave to display
    *  @param currentSelection offset of the annotation node with picture
    */
   private Point getAnnotationRange(int currentSelection)
   {
       long prevOffset, thisOffset;
       AnnotationNode[] nodes = annotations.getAnnotationNodes();
   
       // Playing to the end of the recording or playing the whole thing
       if (currentSelection == -1 || currentSelection >= selections.length)
       {   // Playing to the end of the recording
           if (currentSelection == -1) prevOffset = 0;
           else prevOffset = nodes[selections[selections.length-1]].getOffset();
           thisOffset = annotations.getSound().getFrames();
           offset = 0;  // Next selection will be not annotated.               
           return new Point((int)prevOffset, (int)thisOffset);
       }
       
       else if (offset<0)
       {  if (selections[select]==0) prevOffset = 0;
          else prevOffset = (int)nodes[selections[select] -1].getOffset();
          thisOffset = (int)nodes[selections[select]].getOffset();
       }
       else if (offset>0)
       {   // Playing second half of a sound (annotated portion)
           prevOffset    = offset;
           thisOffset    = nodes[selections[currentSelection]].getOffset();
           offset = -1;
       }
       else
       {   // Playing first half of sound (non-annotated portion)
           if (currentSelection > 0)
           {
                prevOffset = nodes[selections[currentSelection-1]].getOffset();
                thisOffset = nodes[selections[currentSelection]-1].getOffset();
           }
           else 
           {
               prevOffset = 0;
               thisOffset = nodes[selections[currentSelection]].getOffset();
           }
           
           // Set end point to play sound
           offset = thisOffset;
       }
       return new Point((int)prevOffset, (int)thisOffset);
   }
   
   /** Display from the startPoint to the currentPoint
    */
   private void play(Point select)
   {
       SoundData sound = annotations.getSound();
       if (sound.isActive()) stopPlay();
       if ((select.x < select.y) && !sound.playBack(null, select.x, select.y))
       { JOptionPane.showMessageDialog(this, LanguageText.getMessage(lesson,6));
       }
   }
   
   /** Stop current play back  */
   private void stopPlay()
   {
       SoundData sound = annotations.getSound();
       if (sound.isActive())
       {
          sound.stopSound();
          playThread.interrupt();
          offset = -1;
       }
   }
   
   /** Reset current play back, and start over */
   private void reset()
   {
       stopPlay();
       playThread.interrupt();
       initialize();
       start = true;
   }
   
   /** Method to wait till the current playback is finished  
    * 
    * @return true if interrupted or panel no longer is active
    */
    
   private boolean waitTillPlayed(int delay)
   {
       if (!lesson.isPlay()) return true;
       if (getRootPane()==null) return true;
       
       if (Thread.interrupted()) return true;
       
       SoundData sound = annotations.getSound();
       try
       {
          while (sound.isActive()) 
          {
              Thread.sleep(delay);
              if ( (!lesson.isPlay()) 
                   || (getRootPane()==null)) 
              {
                 sound.stopSound();
                 return true;
              }
          }
       }
       catch (InterruptedException interrupted)  
       {   if (continuousPlay) { sound.stopSound(); return true; }  }
       if (!lesson.isPlay()) return true;
       if (getRootPane()==null) return true;
       return false;
   }
   
   /** Thread for playing back the recorded sound */
   public void run()
   {
       Point range;
       
       try{ Thread.sleep(1000); } catch (Exception e) {}
       requestFocus();
       start  = true;
       initialize();
       while (true)
       {
           Thread.interrupted(); // Clear flag
           if (!lesson.isPlay()) return;
           if (getRootPane()==null) return;
           if (start)
           {
               annotations.getAnnotationNodes();
               start = false;
               
               if (continuousPlay)
               {   range = getAnnotationRange(select);
                   play(range);
                   while (select>=0 && select<selections.length)
                   {
                       if (waitTillPlayed(100)) break;
                       {
                            if (offset > 0)
                            {
                               PictureButton button = findButton();
                               if (button!=null) button.createBorder(true);
                               repaint();
                               if (!continuousPlay) break;
                               play(getAnnotationRange(select)); 
                               if (waitTillPlayed(1000)) break;
                            }
                       }
                       nextPlay();
                       range = getAnnotationRange(select); 
                       if (!continuousPlay) break;
                       play(range);
                   }
               }
               else
               {
                   play(getAnnotationRange(select));
                   if (!waitTillPlayed(100)) 
                   {  if (offset>0) play(getAnnotationRange(select));  }
               }
               
           }  // End of if (start)
           else 
           {
               try { Thread.sleep(200); }
               catch (InterruptedException ie) {}
           }

       }      // End of while(true) loop
   }          // End of run() method
   
   /** Determine if answer was correct
    *  @return true if yes, false otherwise
    */
   private boolean correct(String componentString)
   {
       boolean ok = false;
       int componentNumber;
   
       try { componentNumber = Integer.parseInt(componentString); }
       catch (Exception e) { componentNumber = -1; }
       
       if (select==-1) return false;

       SoundData sound;

       if (componentNumber == pictureList[selections[select]] && offset<=0)
       {
           sound = lesson.getSound(AcornsProperties.CORRECT);
           sound.playBack(null, 0, -1);
           ok = true;
       }
       else
       {
           sound = lesson.getSound(AcornsProperties.INCORRECT);
           sound.playBack(null, 0, -1);
       }
       try { while (sound.isActive()) Thread.sleep(200); }
       catch (InterruptedException ie) {}
       
       Score.nextScore(ok);
       Score.calculateScore();
       return ok;
   }   // End of correct() method
     
   /** Proceed to the next blank word  */
   private void nextPlay()
   {
      if (select < 0) {   initialize();  return; }
      else
      {  
         PictureButton button = findButton();
         if (button!=null) button.createBorder(false);
         select++;
         if (select > selections.length) { select = -1; }
         offset = 0;
      }
   }  // End of nextPlay() method
   
   /** Create the control panel at the bottom of the play screen
    *  @return LessonPictureControls pop up menu
    */
   private LessonPlayControls getPictureControls()
   {
      // Create menu items for changing the difficulty level
      items[0] = new JMenuItem(LanguageText.getMessage(lesson, 11));
      items[0].addActionListener(this);
      
      items[1] = new JMenuItem(adjustDifficultyLevelTip(true));
      items[1].addActionListener(this);
      
      items[2] = new JMenuItem(adjustDifficultyLevelTip(false));
      items[2].addActionListener(this);
         
      // Create menu items to alter continuous play option.
      items[3] = new JMenuItem(adjustContinuousPlayTip(continuousPlay));
      items[3].addActionListener(this);
   
      // Create Popup menu
      popup = new LessonPopupMenu(lesson, this, items);
			    
      LessonPlayControls controls = new LessonPlayControls
                                      (lesson, popup, "AnnotationClickPlay");
         
      return controls;
     
   }    // End of getPictureControls()

   // Get the difficulty level text for either tool tips or a menu item
   private String adjustDifficultyLevelTip(boolean flag)
   {
       String[] difficulties = LanguageText.getMessageList(lesson, 7);
 
       int level = Score.getDifficultyLevel();
       String[] difficultyLevels = LanguageText.getMessageList(lesson, 8);
       if (flag)
       {   if (level >= Score.MAX_DIFFICULTY)
                return difficultyLevels[0];
           else return difficultyLevels[2] + " " + difficulties[level-1];
       }
       else
       {   if (level <= Score.MIN_DIFFICULTY)
                return difficultyLevels[1];
           else return difficultyLevels[3] + " " + difficulties[level-1];
       }
   }        // End of AdjustDifficultyLevelTip()
   
   // Set tool tip for continuous play option
   private String adjustContinuousPlayTip(boolean flag)
   {
       String[] continuousPlayText = LanguageText.getMessageList(lesson, 9);
       if (flag)  return continuousPlayText[0];
       else       return continuousPlayText[1];
   }        // End of continuousPlayTip()
 
   /** Respond to pop up menu and button commands
    *  @param event object triggering the listener
    */
   public void actionPerformed(ActionEvent event)
   {
      annotations.getAnnotationNodes();
     
     // Handle keyboard commands
     String action = event.getActionCommand();
     if (action.equals("down")) { stopPlay(); return; }
    
     if (action.equals("up"))
     {   if (continuousPlay) { reset(); return; }  
         if (offset > 0) 
         { Toolkit.getDefaultToolkit().beep(); return; }
 
         PictureButton button = findButton();
         if (button!=null) button.createBorder(true);
         else  Toolkit.getDefaultToolkit().beep();
         return;           
     }
     if (action.equals("left")) 
     {   if (continuousPlay) { reset(); return; }  
         if (offset > 0) 
         { Toolkit.getDefaultToolkit().beep(); return; }
         stopPlay();
         start = true;
         return;           
     }
     if (action.equals("right"))  
     {   if (continuousPlay) { reset(); return; }  
         if (offset > 0) 
         { Toolkit.getDefaultToolkit().beep(); return; }
         stopPlay();
         nextPlay(); 
         start = true;
         return; 
     }
 
     int level = Score.getDifficultyLevel();
    
      // See if a button click triggered the event.
      try 
      {  JButton button = (JButton)event.getSource();
         if (button.getParent() == displayPanel)
         {
             if (continuousPlay) { Toolkit.getDefaultToolkit().beep(); return; }
             stopPlay();
             if (correct(button.getName())) { nextPlay(); }
             start = true;
         }
         return;
      }      // End of try clause
      
      catch (Exception e) 
      {  JMenuItem source = null;
         try { source = (JMenuItem)event.getSource(); }
         catch (Exception cast) { Toolkit.getDefaultToolkit().beep(); return;  }
         
         if (!popup.isArmed()) {  return; }
         popup.cancel();      //Disable the Popup menu.         

         // Check if reset
         if (source == items[0])
         {   reset();
             return;
         }
         // Check if we are to Adjust difficulty level
         if (source == items[1])
         { 
            if (level<Score.MAX_DIFFICULTY)
            {   Score.setDifficultyLevel(++level);
                lesson.setDirty(true);
                reset();
                return;
            } else  {   Toolkit.getDefaultToolkit().beep();  return;  }

         }
         if (source == items[2])
         { 
             if (level>Score.MIN_DIFFICULTY)
             {  Score.setDifficultyLevel(--level);
                lesson.setDirty(true);
                reset();
                return;
            } else {   Toolkit.getDefaultToolkit().beep();  return;  }

         }
         
         if (source == items[3])
         {
             if (continuousPlay)  { continuousPlay = false;  }
             else                 { continuousPlay = true; }
       
             reset();
             return;
         }

      }         // End of try/catch clause
      Toolkit.getDefaultToolkit().beep();
      return;
   }      // End of actionPerformed()
  
   /** Method to create a table mapping annotation nodes to pictures 
    * 
    * @return integer array, index[i] = -1 if node has no picture
    *           or the node index if a matching picture
    */
   private int[] getPictureList()
   {
      ImageAnnotationData pictureAnnotations
         = (ImageAnnotationData)soundPanelProperties
                                     .getAnnotationData(this);

      AnnotationNode[] nodes = pictureAnnotations.getAnnotationNodes();
      int size = pictureAnnotations.getAnnotationSize();
      PictureData pictureI, pictureJ;

      // Initialize table that maps nodes to pictures
      int[] pictures = new int[size];
      for (int i=0; i<size; i++)  {  pictures[i] = -1; }

      for (int i=0; i<size; i++)
      {   if (nodes[i]==null) continue;

          pictureI = (PictureData)nodes[i].getObject();
          if (pictureI==null) continue;

          pictures[i] = i;
          for (int j=0; j<i; j++)
          {   pictureJ = (PictureData)nodes[j].getObject();
              if (pictureJ == null) continue;

              if (pictureI.equals(pictureJ))
              {  pictures[i] = j;
                 break;
              }
          }
      }   // End of for i
      return pictures;
  }   // End of getPictureList()

  /** Method to display the set of pictures as a set of buttons
   * 
   * @param displayPanel The JPanel on which the pictures should display
   * @param pictureList The map of nodes to which node has the picture
   * @param numberPictures The maximum number of pictures to display
   * @return node offset of pictures to display
   */
  private int[] displayPictures
          (JPanel displayPanel, int[] pictureList, int numberPictures)
  {
      // Get the annotation data for the current layer
      ImageAnnotationData displayAnnotations 
              = (ImageAnnotationData)soundPanelProperties
                                           .getAnnotationData(this);
      AnnotationNode[] nodes = displayAnnotations.getAnnotationNodes();

      // Compute the unique pictures for choices
      int pictureCount = 0, uniqueEntry = 0;
      for (int i=0; i<pictureList.length; i++)
      {  if (pictureList[i]==i) pictureCount++; }

      int[] pictureChoice = new int[pictureCount];
      for (int i=0; i<pictureList.length; i++)
      {  if (pictureList[i]==i) pictureChoice[uniqueEntry++] = i;  }

      // Set the number of pictures to display
      if (numberPictures > pictureCount) numberPictures = pictureCount;
      
      // Initialize the list of possible components
      int componentCount = ROWS*COLUMNS;
      int[] components = new int[componentCount];
      PictureButton button;
      for (int component=0; component<componentCount; component++)
      {  components[component] = component; 
         button = (PictureButton)displayPanel.getComponent(component);
         button.setImage(null, "");
      }
      
      // Choose random pictures and assign them to random components
      int component, picture, choose;
      int[] selected = new int[numberPictures];
      PictureData pictureData;
      for (int pictures = 0; pictures<numberPictures; pictures++)
      {
          // Randomly pick a component and a picture
          choose = (int)(Math.floor(Math.random() * componentCount--));
          component = components[choose];
          components[choose] = components[componentCount];
          
          choose  = (int)(Math.random() * pictureCount--);
          picture = pictureChoice[choose];
          pictureChoice[choose] = pictureChoice[pictureCount];
          selected[pictures] = picture;
          
          // Configure the button component based on the choice
          button = (PictureButton)displayPanel.getComponent(component);
          button.createBorder(false);
          pictureData = (PictureData)nodes[picture].getObject();
          if (pictureData!=null) button.setImage(pictureData, ""+picture);
          
      }   // End of for loop to attach the buttons to all pictures
      
      // Compute the number of nodes that apply to selected pictures.
      int selectionCount = 0;
      for (int i=0; i<pictureList.length; i++)
      {  for (int j=0; j<selected.length; j++)
         {   if (pictureList[i] == selected[j]) selectionCount++; }
      }

      int[] selectionList = new int[selectionCount];
      selectionCount = 0;
      for (int i=0; i<pictureList.length; i++)
      {  for (int j=0; j<selected.length; j++)
         {   if (pictureList[i] == selected[j])
             {  selectionList[selectionCount++] = i;  }
         }
      }
      return selectionList; // return array of selected pictures
 
  }       // End of displayPictures()
      
  /** Method to find the button pertaining to the current annotation
   * 
   * @return The correct button, or null if not found
   */
  private PictureButton findButton()
  {
     if (select<0) return null;
     if (select>= selections.length) return null;
     
     Component[] components = displayPanel.getComponents();
     for (int c=0; c<components.length; c++)
     {   if (components[c].getName().equals
                           ("" + pictureList[selections[select]]))
         {   PictureButton button = (PictureButton)components[c];
             return button;
         }
     }
     return null;
  }
   
}     // End of FillPlayLabel