/**
 * StoryScrollPane.java
 * @author HarveyD
 * @version 5.00 Beta
 *
 * Copyright 2009-2015, all rights reserved
 */

package org.acorns.lesson.storyBookV5;

import javax.swing.text.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.*;
import java.beans.*;
import java.awt.image.*;
import java.io.*;
import java.util.AbstractList;
import java.util.List;

import org.acorns.visual.*;
import org.acorns.data.*;
import org.acorns.language.*;

public class StoryScrollPane extends JScrollPane
                implements PropertyChangeListener
{
	private final static long serialVersionUID = 1;
    private LessonsStoryBook lesson;
    private JTextPane        textPane;

    /** The following handle are used for play mode playbacks */
    private long rewindTime;
    private int  frameNumber;

    private int[] offsetsInData;
    private AnnotationNode[] nodeList;
    private int nodeCount;
    private static PictureData background;

    /** Constructor create a display for the entered story text
     *
     * @param lesson The story book lesson object
     */
    public StoryScrollPane(LessonsStoryBook lesson)
    {   initialize(lesson);
    }

    /** Constructor to initialize at a predefined size.
     *
     * @param lesson The story book lesson object
     * @param size The size of the panel
     */
    public StoryScrollPane(LessonsStoryBook lesson, Dimension size)
    {   
    	setSize(size);
        setPreferredSize(size);
        initialize(lesson);
    }

    /** Method to initialize the object
     *
     * @param lesson The story book lesson object.
     */
    private void initialize(final LessonsStoryBook lesson)
    {
        this.lesson = lesson;
        frameNumber = 0;
        rewindTime = 0;
        
        textPane = new JTextPane()
        {
			private static final long serialVersionUID = 1;
			
        	@Override
            protected void paintComponent(Graphics g) 
        	{
               if (background!=null)
               {
            	   int width = textPane.getWidth();
            	   int height = textPane.getHeight();
            	   BufferedImage image = background.getImage
        	        (null,  new Rectangle(0,0,width, height));
            	   g.drawImage(image,  0, 0, width, height, null);
            	   
               }
               super.paintComponent(g);
            }
        };
        
        textPane.getDocument().addDocumentListener(new DocumentListener() {
        	private String previousText = "";
        	private String currentText = "";
 
			public void changedUpdate(DocumentEvent arg0) {
			}

			public void insertUpdate(DocumentEvent arg0) {
				currentText = textPane.getText();
				currentText = currentText.replaceAll("\\r+", "");
				previousText = lesson.getTextEntered();
				if (previousText.length() * currentText.length() !=0 && !currentText.equals(previousText)) {
					lesson.setDirty(true);
				}
			}

			public void removeUpdate(DocumentEvent arg0) {
				currentText = textPane.getText();
				currentText = currentText.replaceAll("\\r+", "");
				previousText = lesson.getTextEntered();
				if (previousText.length() * currentText.length() !=0 && !currentText.equals(previousText)) {
					lesson.setDirty(true);
				}
			}
        	
        });
        
        DefaultCaret caret = (DefaultCaret) textPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); 
        if (background!=null) textPane.setOpaque(false);
        
        /*  Note: 3/13/2012
         *  A picture can be dragged into the background if
         * 		this comment is enabled. However additional work
         * 		is needed to make the feature fully operational. 
         * 		
         * 		The remove mechanism is needed. When the user clicks
         * 		on remove image, a dialog would ask if it the user wants
         * 		the story book picture to be removed, or if they
         * 		want to remove the background picture.
         * 
         * 		Additionally, the picture must be permanently stored
         * 		as part of the lesson, which means a lesson format
         * 		conversion is needed. Also, a get/set background
         * 		picture method is needed to be consistent with other
         * 		lesson types. It is a bit clumsy to use a static
         * 		variable for background in this class.
         * 
         *  	The web-based version for mobile technology
         *  	need to have the logic to properly display the 
         *  	background picture.
         *  
         *  	Export and import facilities need to be expanded
         *  	to port the lesson to/from the xml versions.
         *  
         *  Since the scroll area in play mode is small and no one
         *  	has asked for this feature, it is 
         *  	probably not worth the effort at this time.
         
        if (!lesson.isPlay())
        {
           textPane.setDropTarget(new DropTarget()
           {
			  private static final long serialVersionUID = 1;
			
			  public synchronized void drop(DropTargetDropEvent dtde)      
			  {
	             dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		         File file = getTransferObjects(dtde.getTransferable());
		         if (file == null) 
		         { dtde.dropComplete(false);
		           return;
		         }
		         String extension;
		         String path = file.getPath();
		         extension = path.substring(path.lastIndexOf(".")+1);
		         if (PictureData.isImage(extension))
		         { 
		            try
		            {
		               URL url = new File(path).toURI().toURL(); 
		               Dimension size = textPane.getSize();
		               background = new PictureData(url, size);
		               textPane.setOpaque(false);
		               textPane.repaint();
		            }
		            catch (Exception e) {}
		         }
		         dtde.dropComplete(true);
			}});
        }
        */
        configureDisplay(false);
        setViewportView(textPane);
        addPropertyChangeListener("PlayBack", this);
    }

    public void configureDisplay() { configureDisplay(true); }

    /** Method to configure the display for font and alignment
     *
     * @param save true if data should be saved
     */
    public void configureDisplay(boolean save)
    {
        if (save) saveText();

        AnnotationData data = lesson.getAnnotationData();
        ColorScheme colors = lesson.getColors();

        int align = StyleConstants.ALIGN_LEFT;
        if (data.isCentered()) align = StyleConstants.ALIGN_CENTER;

        SimpleAttributeSet set=new SimpleAttributeSet();
        StyleConstants.setAlignment(set,align);
        StyleConstants.setForeground(set, colors.getColor(false));
        StyleConstants.setBackground(set, colors.getColor(true));
        StyleConstants.setFontSize(set, colors.getSize());

        String language = data.getKeyboard();
        Font font = KeyboardFonts.getLanguageFonts().getFont(language);
        StyleConstants.setFontFamily(set, font.getName());

        textPane.selectAll();
        textPane.setParagraphAttributes(set, true);
        textPane.requestFocus();
        textPane.setBackground(colors.getColor(true));
        textPane.setText(lesson.getTextEntered().replaceAll("\\r+", ""));
        textPane.setCaretPosition(0);
        KeyboardFonts.getLanguageFonts().setFont(language, textPane);
        offsetsInData = null;
   }

   /** Save the text entered into the annotation data object */
   public void saveText()
   {  
	   lesson.setTextEntered(textPane.getText());  
   }

   /** Get the entered text */
   public String getText()
   { return textPane.getText(); }

   /** Save new text in the panel */
   public void setText(String data)
   { textPane.setText(data.replaceAll("\\r+", "")); }
   
   /** Method to highlight a portion of the displayed data
    * 
    * @param offset Offset to the text
    * @param length length of the text to highlight
    */
   public void highlight(int offset, int length)
   {   Color highlight = new Color(170,255,140, 150);
       alterText(highlight, offset, length);
       textPane.setCaretPosition(offset + length);
   }

   /** Method to set a portion of the text back to normal
    *
    * @param offset Offset of the text
    * @param length The length of the text
    */
   public void normal(int offset, int length)
   { Color c = lesson.getColors().getColor(true);
     alterText(c, offset, length);
     textPane.setCaretPosition(offset);
   }

   private synchronized void alterText(Color color, int offset, int length)
   {
       StyledDocument style = textPane.getStyledDocument();
       SimpleAttributeSet set = new SimpleAttributeSet();

       // Some initial validation
       int textLength = style.getLength();
       if (offset<0 || length<=0 || offset>=textLength) return;
       if (offset+length > textLength) length = textLength - offset;
       if (length<=0) return;

       ColorScheme colors = lesson.getColors();
       StyleConstants.setBackground(set, color);
       StyleConstants.setForeground(set, colors.getColor(false));

       String language = lesson.getAnnotationData().getKeyboard();
       Font font = KeyboardFonts.getLanguageFonts().getFont(language);
       StyleConstants.setFontFamily(set, font.getName());
       StyleConstants.setFontSize(set, colors.getSize());

       try
       {   String data = style.getText(offset, length);
           style.remove(offset, length);
           style.insertString(offset, data, set);
       }
       catch (Exception e)  {}
   }   // End of alter text

   /** Method to play the audio from the current frame number */
   public void play()
   {
       AnnotationData sound = lesson.getAnnotationData();
       if (sound.isActive())
       {   Toolkit.getDefaultToolkit().beep();
           return;
       }
       adjustFrameNumber();
       sound.playBack(this, frameNumber, -1);
   }

   /** Method to pause the recording at the current spot */
   public void pause()
   {   AnnotationData sound = lesson.getAnnotationData();
       sound.stopSound();
   }

   /** Method to stop the recording from playing */
   public void stop()
   {   AnnotationData sound = lesson.getAnnotationData();
       sound.stopSound();
       frameNumber = 0;
       rewindTime = 0;
   }

   /** Method to rewind back towards the beginning */
   public void rewind()
   {
       AnnotationData sound = lesson.getAnnotationData();
       sound.stopSound();
       rewindTime = System.currentTimeMillis();
   }

   /* Method to adjust the frame count after a rewind */
   private void adjustFrameNumber()
   {
       if (rewindTime<=0) return;

       long   now = System.currentTimeMillis();
       double deltaTime = (now - rewindTime)/1000.;

       AnnotationData sound = lesson.getAnnotationData();
       float rate = sound.getFrameRate();

       frameNumber -= deltaTime * rate;
       if (frameNumber<0) frameNumber = 0;
   }

   /** Method to listen to sound playback events to highlight
    *       the word in the annotation
    *
    * @param event The object created for handling this occurrence
    */
   public void propertyChange(PropertyChangeEvent event)
   {
	   if (!event.getPropertyName().equals("PlayBack")) return;
			   
       if (offsetsInData == null)
       {
           SetAnnotations set = new SetAnnotations(lesson, lesson.getLayer());
           offsetsInData = set.getStoryOffsets();

           AnnotationData sound = lesson.getAnnotationData();
           nodeCount = sound.getAnnotationCount();
           nodeList = sound.getAnnotationNodes();
       }

      try
      {
         long oldSpot = Long.parseLong(event.getOldValue().toString());
         long newSpot = Long.parseLong(event.getNewValue().toString());
         if (newSpot>0) frameNumber = (int)newSpot;

         int oldNode = findNode(oldSpot);
         int newNode = findNode(newSpot);

         if (oldNode!=newNode)
         {  
        	displayWord(oldNode, false);
            displayWord(newNode, true);
         }
         if (newNode<0) textPane.setCaretPosition(0);
      }
      catch (Exception e) 
      {
    	  e.printStackTrace();
      }
   }

   /** Method to find the annotation node matching a sound frame */
   private int findNode(long spot)
   {   if (spot<=0) return -1;
       for (int i=0; i<nodeCount; i++)
       {  
    	   if (nodeList[i].getOffset()>=spot) { return i; }
       }
       return nodeCount;
   }

   /** Method to display the word being audio played back.
    *
    * @param nodeOffset Offset to correct word being played
    * @param highlight true if to highlight, false to display normal
    * @return true if something displayed, false otherwise
    */
   private boolean displayWord(int nodeOffset, boolean highlight)
   {
       if (nodeOffset<=0) return false;
       String word = nodeList[nodeOffset].getText();
       word = word.replaceAll("\\\\n", "");
       int offset = offsetsInData[nodeOffset-1];
       if (highlight) highlight(offset, word.length());
       else           normal(offset, word.length());

       return true;
   }
   
   /** Method to disable editing */
   public void setEditable(boolean enable)
   {
       textPane.setEditable(enable);
   }
   
   /** Method to get the transferable list of files
    * 
    * @param transfer The transferable object
    * @return a file object being dropped (null if none)
    */
   
   public File getTransferObjects(Transferable transfer)
   {
       DataFlavor[] flavors = transfer.getTransferDataFlavors();
       DataFlavor listFlavor = null;
 
       for (int i=0; i<flavors.length; i++)
       {  if (flavors[i].getRepresentationClass() == List.class)
               listFlavor = flavors[i];
          if (flavors[i].getRepresentationClass() == AbstractList.class)
               listFlavor = flavors[i];
       }

       try
       {  if (listFlavor!=null)
          {
    	      AbstractList<?> list = 
    	          (AbstractList<?>)transfer.getTransferData(listFlavor);
              return (File)list.get(0); 
          }
       }
       catch (Throwable e) {}
       return null;
   }   // End acceptIt()

}      // End of StoryScrollPane class
