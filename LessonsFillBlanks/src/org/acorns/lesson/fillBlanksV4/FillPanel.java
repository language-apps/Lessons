/*
 * FillPanel.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.lesson.fillBlanksV4;

import javax.swing.*;
import java.awt.*;
import java.beans.*;
import org.acorns.editor.*;
import org.acorns.visual.*;
import org.acorns.data.*;
import org.acorns.lesson.*;

public class FillPanel extends RootSoundPanel
{
   private final static long serialVersionUID = 1;
   
   public final static int       MIN_LAYER=1, MAX_LAYER=10;
 
   // Components for the control panel.
   private FillPlayLabel  playPanel;
   private SoundPanel     soundPanel;
   
   // Data needed to process this panel.  
   private LessonsFillBlanks lesson;
   
   public FillPanel(int type, LessonsFillBlanks lesson, ColorScheme colors)
   {
       this.lesson       = lesson;
       
       setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
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
             panelSize.height -= 60;
             panelSize.width  -= 30;

             soundPanel = new SoundPanel('a', colors, panelSize);
             soundPanel.removeListenerStatus(false);
             SoundDisplayPanel displayPanel = soundPanel.getDisplayPanel();
             new ImageAudioDropTargetListener(lesson, displayPanel);

             add(soundPanel);
             break;
             
          case Lesson.PLAY:
              // Grid  Bag because we want the subpanel to be in the middle.
             setLayout(new GridBagLayout());     
             GridBagConstraints c = new GridBagConstraints();
             c.weighty = c.weightx = 0.0;
             c.anchor = GridBagConstraints.CENTER;
             c.fill = GridBagConstraints.NONE;
             
             // Create play panel.
             playPanel = new FillPlayLabel(colors , this, lesson);
             add(playPanel, c);
             break;
       }
   }
   
   /** Initialize the play panel
    */
   public void initialize()   {  if (playPanel!=null) playPanel.initialize(); }
   
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

