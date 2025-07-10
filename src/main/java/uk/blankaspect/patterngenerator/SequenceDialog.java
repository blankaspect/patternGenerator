/*====================================================================*\

SequenceDialog.java

Class: sequence dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.ui.swing.colour.Colours;

import uk.blankaspect.ui.swing.menu.FMenuItem;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.text.TextRendering;

//----------------------------------------------------------------------


// CLASS: SEQUENCE DIALOG


class SequenceDialog
	extends JDialog
	implements ActionListener, MouseListener, MouseMotionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		IMAGE_LAYER			= 1;
	private static final	int		BELOW_IMAGE_LAYER	= IMAGE_LAYER - 1;
	private static final	int		ABOVE_IMAGE_LAYER	= IMAGE_LAYER + 1;

	private static final	int		BUTTON_PANEL_VERTICAL_MARGIN	= 2;
	private static final	int		PREFERRED_BUTTON_PANEL_GAP		= 8;

	private static final	int		BUTTON_PANEL_DELAY	= 1000;

	private static final	Color	BUTTON_PANEL_BORDER_COLOUR		= new Color(176, 184, 176);
	private static final	Color	BUTTON_PANEL_BACKGROUND_COLOUR	= new Color(200, 208, 200);

	private static final	String	DOCUMENT_CREATED_STR	= "The document '%s' was created.";

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Point	location;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	PatternDocument		document;
	private	MouseEventListener	mouseEventListener;
	private	Point				mouseCoordinates;
	private	Timer				timer;
	private	boolean				buttonPanelOnTop;
	private	boolean				paused;
	private	JLayeredPane		layeredPane;
	private	ImagePanel			imagePanel;
	private	JPanel				buttonPanel;
	private	JButton				playPauseButton;
	private	JPopupMenu			contextMenu;
	private	JMenuItem			playPauseMenuItem;
	private	Popup				frameIndexPopUp;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public SequenceDialog(Window          owner,
						  PatternDocument document)
	{
		// Call superclass constructor
		super(owner, PatternGeneratorApp.SHORT_NAME, ModalityType.APPLICATION_MODAL);

		// Initialise instance variables
		this.document = document;
		mouseEventListener = new MouseEventListener();
		timer = new Timer(BUTTON_PANEL_DELAY, this);
		timer.setRepeats(false);

		// Initialise layered pane
		BufferedImage image = document.getImage();
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		layeredPane = new JLayeredPane();
		layeredPane.setPreferredSize(new Dimension(imageWidth, imageHeight));

		// Initialise commands and add them to action map
		for (Command command : Command.values())
		{
			command.createAction(this);
			if (command != Command.PLAY)
				command.action.put();
		}
		updateCommands();

		// Initialise context menu
		contextMenu = new JPopupMenu();
		playPauseMenuItem = contextMenu.add(new FMenuItem(Command.PAUSE.action));
		contextMenu.add(new FMenuItem(Command.STOP.action));
		contextMenu.addSeparator();
		contextMenu.add(new FMenuItem(Command.CREATE_DOCUMENT.action));


		//----  Image panel

		imagePanel = new ImagePanel(image);
		imagePanel.setSize(imageWidth, imageHeight);
		layeredPane.add(imagePanel, Integer.valueOf(IMAGE_LAYER));


		//----  Button panel

		buttonPanel = new JPanel(null);
		buttonPanel.setBackground(BUTTON_PANEL_BACKGROUND_COLOUR);
		GuiUtils.setPaddedLineBorder(buttonPanel, BUTTON_PANEL_VERTICAL_MARGIN,
									 BUTTON_PANEL_BORDER_COLOUR);
		layeredPane.add(buttonPanel, Integer.valueOf(BELOW_IMAGE_LAYER));

		// Button: play/pause
		playPauseButton = new TransportButton(Command.PAUSE);
		buttonPanel.add(playPauseButton);

		// Button: stop
		JButton stopButton = new TransportButton(Command.STOP);
		buttonPanel.add(stopButton);

		// Calculate the size of the gap between buttons and the adjustment to the width of each button
		int numButtons = buttonPanel.getComponentCount();
		int maxButtonHeight = 0;
		int totalButtonWidth = 0;
		int totalGap = -PREFERRED_BUTTON_PANEL_GAP;
		for (Component button : buttonPanel.getComponents())
		{
			Dimension size = button.getPreferredSize();
			totalButtonWidth += size.width;
			totalGap += PREFERRED_BUTTON_PANEL_GAP;
			if (maxButtonHeight < size.height)
				maxButtonHeight = size.height;
		}

		int excessWidth = totalButtonWidth + totalGap - imageWidth;
		if (excessWidth > 0)
		{
			if (excessWidth > totalGap)
			{
				excessWidth -= totalGap;
				totalGap = 0;
			}
			else
			{
				totalGap -= excessWidth;
				excessWidth = 0;
			}
		}
		else
			excessWidth = 0;
		int gap = totalGap / (numButtons - 1);
		int deltaW = (excessWidth + totalGap - gap * (numButtons - 1) + numButtons - 1) / numButtons;

		// Set bounds of buttons
		int x = (imageWidth - (totalButtonWidth - deltaW * numButtons) - gap * (numButtons - 1)) / 2;
		for (Component button : buttonPanel.getComponents())
		{
			Dimension size = button.getPreferredSize();
			if (deltaW > 0)
			{
				size.width -= deltaW;
				button.setPreferredSize(size);
			}
			button.setBounds(x, BUTTON_PANEL_VERTICAL_MARGIN, size.width, size.height);
			x += size.width + gap;
		}
		int panelHeight = maxButtonHeight + 2 * BUTTON_PANEL_VERTICAL_MARGIN;

		// Set bounds of button panel
		buttonPanel.setBounds(0, imageHeight - panelHeight, imageWidth, panelHeight);


		//----  Main panel

		JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
		mainPanel.add(layeredPane);
		mainPanel.addMouseListener(this);
		mainPanel.addMouseMotionListener(this);


		//----  Window

		// Set content pane
		setContentPane(mainPanel);

		// Add mouse event listener
		Toolkit.getDefaultToolkit().
						addAWTEventListener(mouseEventListener,
											AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);

		// Omit frame from dialog
		setUndecorated(true);

		// Dispose of window explicitly
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// Handle window closing
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent event)
			{
				onStop();
			}
		});

		// Prevent dialog from being resized
		setResizable(false);

		// Resize dialog to its preferred size
		pack();

		// Keep on top of other windows
		if (AppConfig.INSTANCE.isKeepSequenceWindowOnTop())
			setAlwaysOnTop(true);

		// Set location of dialog
		if (location == null)
			location = PatternGeneratorApp.INSTANCE.getView().getViewportLocation();
		setLocation(location);

	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void actionPerformed(ActionEvent event)
	{
		Command command = Command.get(event.getActionCommand());
		if (command == null)
			onTimer();
		else
		{
			switch (command)
			{
				case PLAY:
					onPlay();
					break;

				case PAUSE:
					onPause();
					break;

				case STOP:
					onStop();
					break;

				case CREATE_DOCUMENT:
					onCreateDocument();
					break;

				case SHOW_CONTEXT_MENU:
					onShowContextMenu();
					break;
			}
		}
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
		if (SwingUtilities.isLeftMouseButton(event))
		{
			mouseCoordinates = event.getPoint();
			setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		}

		showContextMenu(event);
	}

	//------------------------------------------------------------------

	@Override
	public void mouseReleased(MouseEvent event)
	{
		if (SwingUtilities.isLeftMouseButton(event))
		{
			mouseCoordinates = null;
			setCursor(Cursor.getDefaultCursor());
		}

		showContextMenu(event);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : MouseMotionListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void mouseDragged(MouseEvent event)
	{
		if (SwingUtilities.isLeftMouseButton(event) && (mouseCoordinates != null))
		{
			Point location = getLocation();
			location.translate(event.getX() - mouseCoordinates.x, event.getY() - mouseCoordinates.y);
			setLocation(location);
		}
	}

	//------------------------------------------------------------------

	@Override
	public void mouseMoved(MouseEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public void setImage(BufferedImage image)
	{
		imagePanel.setImage(image);
	}

	//------------------------------------------------------------------

	private void setPaused(boolean paused)
	{
		// Pause animation
		this.paused = paused;
		document.setPaused(paused);

		// Update play/pause button and menu item
		CommandAction action = paused ? Command.PLAY.action : Command.PAUSE.action;
		action.put();
		playPauseButton.setAction(action);
		playPauseMenuItem.setAction(action);

		// Update commands
		updateCommands();

		// Show/hide frame-index pop-up
		if (paused)
		{
			if (frameIndexPopUp == null)
			{
				FrameIndexLabel component = new FrameIndexLabel(document.getAbsoluteFrameIndex());
				Point location = getLocationOnScreen();
				frameIndexPopUp = PopupFactory.getSharedInstance().getPopup(this, component, location.x, location.y);
				frameIndexPopUp.show();
			}
		}
		else
		{
			if (frameIndexPopUp != null)
			{
				frameIndexPopUp.hide();
				frameIndexPopUp = null;
			}
		}
	}

	//------------------------------------------------------------------

	private void updateCommands()
	{
		Command.CREATE_DOCUMENT.setEnabled(paused && !PatternGeneratorApp.INSTANCE.isDocumentsFull());
	}

	//------------------------------------------------------------------

	private void showContextMenu(MouseEvent event)
	{
		if ((event == null) || event.isPopupTrigger())
		{
			// Update commands for menu items
			updateCommands();

			// Display menu
			if (event == null)
				contextMenu.show(this, 0, 0);
			else
				contextMenu.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	//------------------------------------------------------------------

	private void onTimer()
	{
		if (paused)
			timer.restart();
		else
		{
			layeredPane.setLayer(buttonPanel, BELOW_IMAGE_LAYER);
			buttonPanelOnTop = false;
		}
	}

	//------------------------------------------------------------------

	private void onPlay()
	{
		setPaused(false);
		mouseEventListener.kick();
	}

	//------------------------------------------------------------------

	private void onPause()
	{
		setPaused(true);
		if (!buttonPanelOnTop)
		{
			layeredPane.setLayer(buttonPanel, ABOVE_IMAGE_LAYER);
			buttonPanelOnTop = true;
		}
	}

	//------------------------------------------------------------------

	private void onStop()
	{
		document.stopPlaying();
		Toolkit.getDefaultToolkit().removeAWTEventListener(mouseEventListener);

		location = getLocation();
		setVisible(false);
		dispose();
	}

	//------------------------------------------------------------------

	private void onCreateDocument()
	{
		try
		{
			if (!PatternGeneratorApp.INSTANCE.isDocumentsFull())
			{
				PatternDocument newDocument = document.createDefinitionDocument(false);
				PatternGeneratorApp.INSTANCE.addDocument(newDocument);
				String str = String.format(DOCUMENT_CREATED_STR, newDocument.getName(false));
				JOptionPane.showMessageDialog(this, str, PatternGeneratorApp.SHORT_NAME,
											  JOptionPane.INFORMATION_MESSAGE);
			}
		}
		catch (AppException e)
		{
			JOptionPane.showMessageDialog(this, e, PatternGeneratorApp.SHORT_NAME, JOptionPane.ERROR_MESSAGE);
		}
	}

	//------------------------------------------------------------------

	private void onShowContextMenu()
	{
		showContextMenu(null);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: COMMAND


	private enum Command
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		PLAY
		(
			"play",
			KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
			Icons.PLAY,
			"Play",
			"Play"
		),

		PAUSE
		(
			"pause",
			KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
			Icons.PAUSE,
			"Pause",
			"Pause"
		),

		STOP
		(
			"stop",
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			Icons.STOP,
			"Stop",
			"Stop"
		),

		CREATE_DOCUMENT
		(
			"createDocument",
			KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK),
			null,
			"Create a document",
			null
		),

		SHOW_CONTEXT_MENU
		(
			"showContextMenu",
			KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0),
			null,
			null,
			null
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String			key;
		private	KeyStroke		keyStroke;
		private	Icon			icon;
		private	String			text;
		private	String			tooltipText;
		private	CommandAction	action;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Command(String    key,
						KeyStroke keyStroke,
						Icon      icon,
						String    text,
						String    tooltipText)
		{
			this.key = key;
			this.keyStroke = keyStroke;
			this.icon = icon;
			this.text = text;
			this.tooltipText = tooltipText;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static Command get(String key)
		{
			for (Command value : values())
				if (value.key.equals(key))
					return value;
			return null;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void createAction(SequenceDialog dialog)
		{
			action = dialog.new CommandAction(key, keyStroke, icon, text, tooltipText, dialog);
		}

		//--------------------------------------------------------------

		private void setEnabled(boolean enabled)
		{
			action.setEnabled(enabled);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: TRANSPORT BUTTON


	private static class TransportButton
		extends JButton
		implements MouseListener, MouseMotionListener
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	HORIZONTAL_MARGIN	= 8;
		private static final	int	VERTICAL_MARGIN		= 3;

		private static final	Color	BORDER_COLOUR					= BUTTON_PANEL_BORDER_COLOUR;
		private static final	Color	BACKGROUND_COLOUR				= BUTTON_PANEL_BACKGROUND_COLOUR;
		private static final	Color	HIGHLIGHTED_BACKGROUND_COLOUR	= new Color(224, 232, 224);
		private static final	Color	ACTIVE_BACKGROUND_COLOUR		= Colours.FOCUSED_SELECTION_BACKGROUND;

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	boolean	mouseOver;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private TransportButton(Command command)
		{
			// Call superclass constructor
			super(command.action);

			// Set properties
			setBorder(null);
			setBackground(null);
			setFocusable(false);
			setDefaultCapable(false);

			// Add listeners
			addMouseListener(this);
			addMouseMotionListener(this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : MouseListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void mouseClicked(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		@Override
		public void mouseEntered(MouseEvent event)
		{
			mouseOver = true;
		}

		//--------------------------------------------------------------

		@Override
		public void mouseExited(MouseEvent event)
		{
			mouseOver = false;
		}

		//--------------------------------------------------------------

		@Override
		public void mousePressed(MouseEvent event)
		{
			mouseOver = contains(event.getPoint());
		}

		//--------------------------------------------------------------

		@Override
		public void mouseReleased(MouseEvent event)
		{
			mouseOver = contains(event.getPoint());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : MouseMotionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void mouseDragged(MouseEvent event)
		{
			mouseOver = contains(event.getPoint());
		}

		//--------------------------------------------------------------

		@Override
		public void mouseMoved(MouseEvent event)
		{
			mouseOver = contains(event.getPoint());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Dimension getPreferredSize()
		{
			Icon icon = getIcon();
			return new Dimension(2 * HORIZONTAL_MARGIN + icon.getIconWidth(),
								 2 * VERTICAL_MARGIN + icon.getIconHeight());
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Get dimensions
			int width = getWidth();
			int height = getHeight();

			// Fill interior
			boolean active = (isSelected() != getModel().isArmed());
			gr.setColor(active ? ACTIVE_BACKGROUND_COLOUR
							   : mouseOver ? HIGHLIGHTED_BACKGROUND_COLOUR
										   : BACKGROUND_COLOUR);
			gr.fillRect(0, 0, width, height);

			// Draw icon
			Icon icon = getIcon();
			icon.paintIcon(this, gr, (width - icon.getIconWidth()) / 2,
						   (height - icon.getIconHeight()) / 2);

			// Draw border
			if (active || mouseOver)
			{
				gr.setColor(BORDER_COLOUR);
				gr.drawRect(0, 0, width - 1, height - 1);
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: FRAME-INDEX LABEL


	private static class FrameIndexLabel
		extends JComponent
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	VERTICAL_MARGIN		= 2;
		private static final	int	HORIZONTAL_MARGIN	= 4;

		private static final	Color	BACKGROUND_COLOUR	= new Color(248, 232, 192);
		private static final	Color	TEXT_COLOUR			= Color.BLACK;
		private static final	Color	BORDER_COLOUR		= new Color(224, 144, 88);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private FrameIndexLabel(int frameIndex)
		{
			// Initialise instance variables
			text = Integer.toString(frameIndex);

			// Set preferred size
			AppFont.MAIN.apply(this);
			FontMetrics fontMetrics = getFontMetrics(getFont());
			int width = 2 * HORIZONTAL_MARGIN + getFontMetrics(getFont()).stringWidth(text);
			int height = 2 * VERTICAL_MARGIN + fontMetrics.getAscent() + fontMetrics.getDescent();
			setPreferredSize(new Dimension(width, height));

			// Set properties
			setOpaque(true);
			setFocusable(false);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Create copy of graphics context
			gr = gr.create();

			// Get dimensions
			int width = getWidth();
			int height = getHeight();

			// Fill background
			gr.setColor(BACKGROUND_COLOUR);
			gr.fillRect(0, 0, width, height);

			// Set rendering hints for text antialiasing and fractional metrics
			TextRendering.setHints((Graphics2D)gr);

			// Draw text
			FontMetrics fontMetrics = gr.getFontMetrics();
			gr.setColor(TEXT_COLOUR);
			gr.drawString(text, HORIZONTAL_MARGIN, VERTICAL_MARGIN + fontMetrics.getAscent());

			// Draw border
			gr.setColor(BORDER_COLOUR);
			gr.drawRect(0, 0, width - 1, height - 1);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: COMMAND ACTION


	private class CommandAction
		extends AbstractAction
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	KeyStroke		keyStroke;
		private	ActionListener	listener;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CommandAction(String         command,
							  KeyStroke      keyStroke,
							  Icon           icon,
							  String         text,
							  String         tooltipText,
							  ActionListener listener)
		{
			this.keyStroke = keyStroke;
			this.listener = listener;

			putValue(ACTION_COMMAND_KEY, command);
			if (keyStroke != null)
				putValue(ACCELERATOR_KEY, keyStroke);
			if (icon != null)
				putValue(LARGE_ICON_KEY, icon);
			if (text != null)
				putValue(NAME, text);
			if (tooltipText != null)
				putValue(SHORT_DESCRIPTION, tooltipText);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void actionPerformed(ActionEvent event)
		{
			listener.actionPerformed(event);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void put()
		{
			String command = getValue(ACTION_COMMAND_KEY).toString();
			layeredPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, command);
			layeredPane.getActionMap().put(command, this);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: MOUSE-EVENT LISTENER


	private class MouseEventListener
		implements AWTEventListener
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	MouseEvent	lastMouseEvent;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private MouseEventListener()
		{
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : AWTEventListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void eventDispatched(AWTEvent event)
		{
			if (event instanceof MouseEvent)
			{
				MouseEvent mouseEvent = (MouseEvent)event;
				mouseEvent = SwingUtilities.convertMouseEvent(mouseEvent.getComponent(), mouseEvent,
															  buttonPanel);
				lastMouseEvent = new MouseEvent(buttonPanel, 0, 0, 0, mouseEvent.getX(),
												mouseEvent.getY(), 0, false);
				if (buttonPanel.contains(mouseEvent.getPoint()))
				{
					timer.stop();
					if (!buttonPanelOnTop)
					{
						layeredPane.setLayer(buttonPanel, ABOVE_IMAGE_LAYER);
						buttonPanelOnTop = true;
					}
				}
				else
					timer.restart();
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void kick()
		{
			if (lastMouseEvent == null)
				lastMouseEvent = new MouseEvent(buttonPanel, 0, 0, 0, -1, -1, 0, false);
			eventDispatched(lastMouseEvent);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
