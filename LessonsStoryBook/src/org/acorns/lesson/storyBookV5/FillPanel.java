/*
 * FillPanel.java
 *
 *   @author  HarveyD
 *   @version 5.00 Beta
 *
 *   Copyright 2009-2015, all rights reserved
*/
package org.acorns.lesson.storyBookV5;

import javax.swing.*;
import java.awt.*;
import java.beans.*;
import org.acorns.editor.*;
import org.acorns.data.*;
import org.acorns.lesson.*;

public class FillPanel extends RootSoundPanel
{
   private final static long serialVersionUID = 1;
   
   public final static int SOUND_PANEL_HEIGHT = 250;

   private PlayPanel  playPanel;
   private SoundPanel soundPanel;
   
   // Data needed to process this panel.  
   private LessonsStoryBook lesson;
   
   public FillPanel(int type, LessonsStoryBook lesson)
   {
       this.lesson = lesson;
       
       new SetAnnotations(lesson, lesson.getLayer());
       
       setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
       setBackground(new Color(80,80,80));
       
        // Remove previous "SoundListeners"
        String listener = "SoundListeners";
        PropertyChangeListener[] pcl 
            = Toolkit.getDefaultToolkit().getPropertyChangeListeners(listener);
        for (int i=0; i<pcl.length; i++)
        {   Toolkit.getDefaultToolkit().removePropertyChangeListener
                                                        (listener, pcl[0]); }
        // Add the new one.
       Toolkit.getDefaultToolkit().addPropertyChangeListener(listener, this);
       switch (type)
       {
          case Lesson.SETUP:   
             Dimension panelSize = lesson.getDisplaySize();
             int originalHeight = panelSize.height;

             panelSize.height = SOUND_PANEL_HEIGHT;
             panelSize.width  -= 10;
             
             soundPanel = new SoundPanel('b', lesson.getColors(), panelSize);
             soundPanel.removeListenerStatus(false);
             soundPanel.setPreferredSize(panelSize);
             soundPanel.setMaximumSize(panelSize);
             SoundDisplayPanel displayPanel = soundPanel.getDisplayPanel();
             new ImageAudioDropTargetListener(lesson, displayPanel);
 
             add(soundPanel);

             StoryScrollPane story = lesson.getStoryPanel();

             Dimension display = lesson.getDisplaySize();
             display.height = originalHeight - SOUND_PANEL_HEIGHT - 70;
             display.width  -= 10;
             story.setSize(display);
             story.setPreferredSize(display);
             add(story);
             break;
             
          case Lesson.PLAY:
              // Grid  Bag because we want the subpanel to be in the middle.
             setLayout(new GridBagLayout());     
             GridBagConstraints c = new GridBagConstraints();
             c.weighty = c.weightx = 0.0;
             c.anchor = GridBagConstraints.CENTER;
             c.fill = GridBagConstraints.NONE;
             
             // Create play panel.
             playPanel = new PlayPanel(lesson);
             add(playPanel, c);
             break;
       }
   }
   
   /** Return panel with the annotation slider 
    *  @return SoundPanel object
    */    
   public SoundPanel getSoundPanel()  { return soundPanel; }

   /********** Property Change Events from Sound Editor Classes **************/
     public void setAnnotationData(Annotations data, JPanel panel) 
     { lesson.setAnnotationData((AnnotationData)data);
     }
     public Annotations getAnnotationData(JPanel panel)
     {  AnnotationData annotationData = lesson.getAnnotationData();
        annotationData.setFrameRate();
        return annotationData; 
     }
     public UndoRedo getUndoRedo()  { return lesson.getUndoRedoStack(); }
 }  // End of FillPanel class.

