/*====================================================================*\

PatternView.java

Pattern view class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.menu.FMenuItem;

import uk.blankaspect.ui.swing.misc.GuiUtils;

//----------------------------------------------------------------------


// PATTERN VIEW CLASS


class PatternView
	extends JScrollPane
	implements ActionListener, MouseListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	MIN_WIDTH	= 128;
	private static final	int	MIN_HEIGHT	= 64;

	// Commands
	private interface Command
	{
		String	SHOW_CONTEXT_MENU	= "showContextMenu";
	}

	private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
	{
		KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0), Command.SHOW_CONTEXT_MENU)
	};

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	PatternDocument	document;
	private	ImagePanel		imagePanel;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public PatternView(PatternDocument document)
	{
		// Call superclass constructor
		super(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		// Initialise instance variables
		this.document = document;

		// Set view and size of viewport
		BufferedImage image = document.getImage();
		if (image == null)
			setViewportView(new NoImagePanel(null));
		else
		{
			imagePanel = new ImagePanel(image);
			setViewportView(imagePanel);
		}
		viewport.setFocusable(true);
		getVerticalScrollBar().setFocusable(false);
		getHorizontalScrollBar().setFocusable(false);

		// Set properties
		setBorder(null);

		// Add commands to action map
		KeyAction.create(this, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this, KEY_COMMANDS);

		// Add listeners
		addMouseListener(this);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals(Command.SHOW_CONTEXT_MENU))
			onShowContextMenu();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : MouseListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void mouseClicked(MouseEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	@Override
	public void mouseEntered(MouseEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	@Override
	public void mouseExited(MouseEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	@Override
	public void mousePressed(MouseEvent event)
	{
		showContextMenu(event);
	}

	//------------------------------------------------------------------

	@Override
	public void mouseReleased(MouseEvent event)
	{
		showContextMenu(event);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public BufferedImage getImage()
	{
		return (imagePanel == null) ? null : imagePanel.getImage();
	}

	//------------------------------------------------------------------

	public void setImage(BufferedImage image)
	{
		if (image == null)
		{
			if (imagePanel != null)
			{
				imagePanel = null;
				setViewportView(new NoImagePanel(viewport.getExtentSize()));
			}
		}
		else
		{
			if (imagePanel == null)
			{
				imagePanel = new ImagePanel(image);
				setViewportView(imagePanel);
			}
			else
				imagePanel.setImage(image);
		}
		repaint();
	}

	//------------------------------------------------------------------

	public void setPreferredViewportSize(Dimension preferredSize)
	{
		preferredSize = new Dimension(Math.max(MIN_WIDTH, preferredSize.width),
									  Math.max(MIN_HEIGHT, preferredSize.height));
		if (!preferredSize.equals(viewport.getExtentSize()))
		{
			PatternGeneratorApp app = PatternGeneratorApp.INSTANCE;
			for (int i = 0; i < app.getNumDocuments(); i++)
				app.getView(i).setViewportSize(preferredSize);
			app.getMainWindow().resize();
		}
	}

	//------------------------------------------------------------------

	public Point getViewportLocation()
	{
		Point location = viewport.getLocation();
		SwingUtilities.convertPointToScreen(location, this);
		return location;
	}

	//------------------------------------------------------------------

	private void showContextMenu(MouseEvent event)
	{
		if ((event == null) || event.isPopupTrigger())
		{
			// Create context menu
			JPopupMenu menu = new JPopupMenu();
			menu.add(new FMenuItem(PatternDocument.Command.EDIT_PATTERN_PARAMETERS));
			menu.add(new FMenuItem(PatternDocument.Command.EDIT_DESCRIPTION));
			menu.addSeparator();
			menu.add(new FMenuItem(PatternDocument.Command.REGENERATE_PATTERN_WITH_NEW_SEED));
			menu.addSeparator();
			menu.add(new FMenuItem(PatternDocument.Command.SHOW_IMAGE_RENDERING_TIME));
			menu.addSeparator();
			menu.add(new FMenuItem(PatternDocument.Command.START_SLIDE_SHOW));
			menu.add(new FMenuItem(PatternDocument.Command.START_ANIMATION));
			menu.addSeparator();
			menu.add(new FMenuItem(PatternDocument.Command.RESIZE_WINDOW_TO_IMAGE));

			// Update commands for menu items
			document.updateCommands();

			// Display menu
			if (event == null)
				menu.show(this, 0, 0);
			else
				menu.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	//------------------------------------------------------------------

	private void setViewportSize(Dimension size)
	{
		viewport.setPreferredSize(size);
		viewport.setExtentSize(size);
	}

	//------------------------------------------------------------------

	private void onShowContextMenu()
	{
		showContextMenu(null);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// NO IMAGE PANEL CLASS


	private static class NoImagePanel
		extends JPanel
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int		VERTICAL_PADDING	= 2;
		private static final	int		HORIZONTAL_PADDING	= 8;

		private static final	Color	BACKGROUND_COLOUR	= new Color(248, 240, 184);
		private static final	Color	BORDER_COLOUR		= new Color(224, 216, 168);

		private static final	String	NO_IMAGE_STR	= "No image";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private NoImagePanel(Dimension size)
		{
			// Call superclass constructor
			super(new FlowLayout(FlowLayout.LEADING));

			// Set properties
			if (size != null)
				setPreferredSize(size);

			// Label: no image
			FLabel noImageLabel = new FLabel(NO_IMAGE_STR);
			noImageLabel.setOpaque(true);
			noImageLabel.setBackground(BACKGROUND_COLOUR);
			GuiUtils.setPaddedLineBorder(noImageLabel, VERTICAL_PADDING, HORIZONTAL_PADDING, BORDER_COLOUR);
			add(noImageLabel);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
