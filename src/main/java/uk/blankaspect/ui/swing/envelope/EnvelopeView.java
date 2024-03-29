/*====================================================================*\

EnvelopeView.java

Class: envelope view.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.envelope;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.blankaspect.common.envelope.AbstractEnvelope;
import uk.blankaspect.common.envelope.AbstractNode;
import uk.blankaspect.common.envelope.NodeId;

import uk.blankaspect.common.geometry.Point2D;

import uk.blankaspect.common.range.DoubleRange;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.colour.Colours;

import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.icon.ColourSampleIcon;

import uk.blankaspect.ui.swing.menu.FMenu;
import uk.blankaspect.ui.swing.menu.FMenuItem;
import uk.blankaspect.ui.swing.menu.FRadioButtonMenuItem;

import uk.blankaspect.ui.swing.text.TextRendering;

//----------------------------------------------------------------------


// CLASS: ENVELOPE VIEW


public class EnvelopeView<S extends AbstractNode & IViewNode, T extends AbstractEnvelope<S> & IViewEnvelope>
	extends JComponent
	implements ActionListener, FocusListener, MouseListener, MouseMotionListener, Scrollable
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int		TOP_MARGIN		= 2;
	public static final		int		BOTTOM_MARGIN	= TOP_MARGIN;
	public static final		int		LEFT_MARGIN		= 2;
	public static final		int		RIGHT_MARGIN	= LEFT_MARGIN;

	public static final		int		NODE_WIDTH			= 5;
	public static final		int		NODE_HEIGHT			= NODE_WIDTH;
	public static final		int		NODE_HALF_WIDTH		= NODE_WIDTH / 2;
	public static final		int		NODE_HALF_HEIGHT	= NODE_HEIGHT / 2;

	private static final	int		NODE_CAPTURE_WIDTH			= 7;
	private static final	int		NODE_CAPTURE_HEIGHT			= NODE_CAPTURE_WIDTH;
	private static final	int		NODE_CAPTURE_HALF_WIDTH		= NODE_CAPTURE_WIDTH / 2;
	private static final	int		NODE_CAPTURE_HALF_HEIGHT	= NODE_CAPTURE_HEIGHT / 2;

	private static final	int		SCALE_MARK_WIDTH		= 4;
	private static final	int		SCALE_MARK_HEIGHT		= SCALE_MARK_WIDTH;
	private static final	int		X_SCALE_TOP_MARGIN		= 2;
	private static final	int		X_SCALE_BOTTOM_MARGIN	= SCALE_MARK_HEIGHT;
	private static final	int		Y_SCALE_LEFT_MARGIN		= 2;
	private static final	int		Y_SCALE_RIGHT_MARGIN	= Y_SCALE_LEFT_MARGIN + SCALE_MARK_WIDTH;
	private static final	int		X_SCALE_STR_OFFSET		= -2;

	private static final	int		COLOUR_SAMPLE_WIDTH		= 20;
	private static final	int		COLOUR_SAMPLE_HEIGHT	= 12;

	private static final	double			DELTA_COORDINATE		= 0.0001;
	private static final	int				COORDINATE_FIELD_LENGTH	= 6;
	private static final	DecimalFormat	COORDINATE_FORMAT		= new DecimalFormat("0.0###");

	private static final	long	MIN_BEEP_INTERVAL	= 400;

	// Default values
	private static final	int		DEFAULT_SCROLL_INCREMENT_UNIT	= 1;
	private static final	int		DEFAULT_SCROLL_INCREMENT_BLOCK	= 32;

	public static final		Color	DEFAULT_SEGMENT_COLOUR	= Color.BLUE;
	public static final		Color	DEFAULT_NODE_COLOUR		= Color.BLACK;

	private static final	Color	DEFAULT_BACKGROUND_COLOUR				= Colours.BACKGROUND;
	private static final	Color	DEFAULT_GRID_COLOUR						= new Color(224, 224, 224);
	private static final	Color	DEFAULT_AXIS_COLOUR						= new Color(176, 176, 176);
	private static final	Color	DEFAULT_NODE_SELECTED_COLOUR			= new Color(192, 0, 0);
	private static final	Color	DEFAULT_NODE_FOCUSED_SELECTED_COLOUR	= new Color(255, 0, 0);
	private static final	Color	DEFAULT_NODE_ACTIVE_COLOUR				= new Color(252, 176, 64);
	private static final	Color	DEFAULT_SCALE_BACKGROUND_COLOUR			= new Color(224, 224, 220);
	private static final	Color	DEFAULT_SCALE_FOCUSED_BACKGROUND_COLOUR	= new Color(220, 232, 220);
	private static final	Color	DEFAULT_SCALE_FOREGROUND_COLOUR			= Color.BLACK;

	// Commands
	private interface Command
	{
		String	MODE_SELECT				= "modeSelect";
		String	MODE_DRAW				= "modeDraw.";
		String	MODE_ERASE				= "modeErase";
		String	MODE_EDIT				= "modeEdit";
		String	SCROLL_LEFT_UNIT		= "scrollLeftUnit";
		String	SCROLL_RIGHT_UNIT		= "scrollRightUnit";
		String	SCROLL_LEFT_BLOCK		= "scrollLeftBlock";
		String	SCROLL_RIGHT_BLOCK		= "scrollRightBlock";
		String	SCROLL_LEFT_MAX			= "scrollLeftMax";
		String	SCROLL_RIGHT_MAX		= "scrollRightMax";
		String	CENTRE_SELECTED_NODE	= "centreSelectedNode";
		String	SELECT_NODE_UP			= "selectNodeUp";
		String	SELECT_NODE_DOWN		= "selectNodeDown";
		String	SELECT_NODE_LEFT		= "selectNodeLeft";
		String	SELECT_NODE_RIGHT		= "selectNodeRight";
		String	MOVE_NODE_UP			= "moveNodeUp";
		String	MOVE_NODE_DOWN			= "moveNodeDown";
		String	MOVE_NODE_LEFT			= "moveNodeLeft";
		String	MOVE_NODE_RIGHT			= "moveNodeRight";
		String	EDIT_NODE				= "editNode";
		String	DELETE_NODE				= "deleteNode";
		String	SHOW_CONTEXT_MENU		= "showContextMenu";
	}

	private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
	{
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
			Command.SCROLL_LEFT_UNIT
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
			Command.SCROLL_RIGHT_UNIT
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK),
			Command.SCROLL_LEFT_BLOCK
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK),
			Command.SCROLL_RIGHT_BLOCK
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
			Command.SCROLL_LEFT_MAX
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
			Command.SCROLL_RIGHT_MAX
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
			Command.CENTRE_SELECTED_NODE
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK),
			Command.SELECT_NODE_UP
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK),
			Command.SELECT_NODE_DOWN
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK),
			Command.SELECT_NODE_LEFT
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK),
			Command.SELECT_NODE_RIGHT
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK),
			Command.MOVE_NODE_UP
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK),
			Command.MOVE_NODE_DOWN
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK),
			Command.MOVE_NODE_LEFT
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK),
			Command.MOVE_NODE_RIGHT
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK),
			Command.EDIT_NODE
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.SHIFT_DOWN_MASK),
			Command.DELETE_NODE
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0),
			Command.SHOW_CONTEXT_MENU
		)
	};

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: MODE


	public enum Mode
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		SELECT
		(
			"Select",
			ModeIcon.SELECT,
			null,
			0
		),

		DRAW
		(
			"Draw",
			ModeIcon.DRAW1,
			ModeIcon.DRAW2,
			15
		),

		ERASE
		(
			"Erase",
			ModeIcon.ERASE1,
			ModeIcon.ERASE2,
			0
		),

		EDIT
		(
			"Edit value",
			ModeIcon.EDIT1,
			ModeIcon.EDIT2,
			0
		);

		//--------------------------------------------------------------

		private static final	String	CURSOR_STR	= " cursor";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Mode(String    text,
					 Icon      menuIcon,
					 ImageIcon cursorIcon,
					 int       cursorY)
		{
			this.text = text;
			this.menuIcon = menuIcon;
			cursor = (cursorIcon == null)
								? Cursor.getDefaultCursor()
								: Toolkit.getDefaultToolkit().createCustomCursor(cursorIcon.getImage(),
																				 new Point(0, cursorY),
																				 text + CURSOR_STR);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public Icon getMenuIcon()
		{
			return menuIcon;
		}

		//--------------------------------------------------------------

		public Cursor getCursor()
		{
			return cursor;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;
		private	Icon	menuIcon;
		private	Cursor	cursor;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: CORNER


	public class Corner
		extends JComponent
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	boolean	upper;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public Corner(Font    font,
					  boolean upper)
		{
			// Initialise instance variables
			this.upper = upper;

			// Set attributes
			setFont(font);
			setBackground(scaleBackgroundColour);
			setForeground(scaleForegroundColour);
			setOpaque(true);
			setFocusable(false);
			addMouseListener(EnvelopeView.this);
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

			// Draw background
			gr.setColor(getBackground());
			gr.fillRect(0, 0, getWidth(), getHeight());

			// Set rendering hints for text antialiasing and fractional metrics
			TextRendering.setHints((Graphics2D)gr);

			// Get y scale text
			String str = upper ? verticalScaleMarkings[0]
							   : verticalScaleMarkings[verticalScaleMarkings.length - 1];

			// Get text y coordinate
			int y = upper ? getHeight() + TOP_MARGIN + verticalDivOffset
						  : verticalDivOffset + (verticalScaleMarkings.length - 1) * verticalDivHeight -
																			(plotHeight + BOTTOM_MARGIN);
			FontMetrics fontMetrics = gr.getFontMetrics();
			y += FontUtils.getBaselineOffset(0, fontMetrics);

			// Draw text
			gr.setColor(getForeground());
			gr.drawString(str, getWidth() - Y_SCALE_RIGHT_MARGIN - fontMetrics.stringWidth(str), y);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: X SCALE


	public class XScale
		extends JComponent
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int	height;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private XScale(int  height,
					   Font font)
		{
			// Initialise instance variables
			FontMetrics fontMetrics = getFontMetrics(font);
			this.height = (height > 0)
								? height
								: X_SCALE_TOP_MARGIN + fontMetrics.getAscent() + fontMetrics.getDescent()
																								+ X_SCALE_BOTTOM_MARGIN;

			// Set attributes
			setFont(font);
			setBackground(scaleBackgroundColour);
			setForeground(scaleForegroundColour);
			setOpaque(true);
			setFocusable(false);
			addMouseListener(EnvelopeView.this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension(LEFT_MARGIN + Math.max(viewableWidth, plotWidth) + RIGHT_MARGIN,
								 height);
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Create copy of graphics context
			gr = gr.create();

			// Draw background
			Rectangle rect = gr.getClipBounds();
			gr.setColor(getBackground());
			gr.fillRect(rect.x, rect.y, rect.width, rect.height);

			// Set rendering hints for text antialiasing and fractional metrics
			TextRendering.setHints((Graphics2D)gr);

			// Draw scale markings
			gr.setColor(getForeground());
			int fontAscent = gr.getFontMetrics().getAscent();
			int x = LEFT_MARGIN + horizontalDivOffset;
			int width = getWidth();
			int index = 0;
			while (x < width)
			{
				String str = getXScaleString(index);
				if (str != null)
				{
					gr.drawLine(x, height - SCALE_MARK_HEIGHT, x, height - 1);
					gr.drawString(str, x + X_SCALE_STR_OFFSET, X_SCALE_TOP_MARGIN + fontAscent);
				}
				x += horizontalDivWidth;
				++index;
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: Y SCALE


	public class YScale
		extends JComponent
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int	width;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private YScale(int  width,
					   Font font)
		{
			// Initialise instance variables
			if (width > 0)
				this.width = width;
			else
			{
				int maxWidth = 0;
				for (int i = 0; i < verticalScaleMarkings.length; i++)
					maxWidth = Math.max(maxWidth,
										getFontMetrics(font).stringWidth(verticalScaleMarkings[i]));
				this.width = Y_SCALE_LEFT_MARGIN + Y_SCALE_RIGHT_MARGIN + maxWidth;
			}

			// Set attributes
			setFont(font);
			setBackground(scaleBackgroundColour);
			setForeground(scaleForegroundColour);
			setOpaque(true);
			setFocusable(false);
			addMouseListener(EnvelopeView.this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension(width, TOP_MARGIN + plotHeight + BOTTOM_MARGIN);
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Create copy of graphics context
			gr = gr.create();

			// Draw background
			Rectangle rect = gr.getClipBounds();
			gr.setColor(getBackground());
			gr.fillRect(rect.x, rect.y, rect.width, rect.height);

			// Set rendering hints for text antialiasing and fractional metrics
			TextRendering.setHints((Graphics2D)gr);

			// Draw scale markings
			gr.setColor(getForeground());
			FontMetrics fontMetrics = gr.getFontMetrics();
			int fontYOffs = FontUtils.getBaselineOffset(0, fontMetrics);
			int y = TOP_MARGIN + verticalDivOffset;
			for (int i = 0; i < verticalScaleMarkings.length; i++)
			{
				gr.drawLine(width - SCALE_MARK_WIDTH, y, width - 1, y);
				gr.drawString(verticalScaleMarkings[i],
							  width - Y_SCALE_RIGHT_MARGIN -
														fontMetrics.stringWidth(verticalScaleMarkings[i]),
							  y + fontYOffs);
				y += verticalDivHeight;
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: COMMAND ACTION


	private class CommandAction
		extends AbstractAction
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CommandAction(String command,
							  String text,
							  Icon   icon)
		{
			super(text, icon);
			putValue(Action.ACTION_COMMAND_KEY, command);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void actionPerformed(ActionEvent event)
		{
			EnvelopeView.this.actionPerformed(event);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	JPopupMenu	contextMenu;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	int						plotWidth;
	private	int						plotHeight;
	private	int						viewableWidth;
	private	int						horizontalDivOffset;
	private	int						horizontalDivWidth;
	private	int						verticalDivOffset;
	private	int						verticalDivHeight;
	private	int						axisVerticalDivIndex;
	private	int						scrollUnitIncrement;
	private	int						scrollBlockIncrement;
	private	int						drawIndex;
	private	Mode					mode;
	private	Color					gridColour;
	private	Color					axisColour;
	private	Color					nodeSelectedColour;
	private	Color					nodeFocusedSelectedColour;
	private	Color					nodeActiveColour;
	private	Color					scaleBackgroundColour;
	private	Color					scaleFocusedBackgroundColour;
	private	Color					scaleForegroundColour;
	private	String[]				verticalScaleMarkings;
	private	XScale					xScale;
	private	YScale					yScale;
	private	Corner					upperLeftCorner;
	private	Corner					lowerLeftCorner;
	private	JViewport				viewport;
	private	List<ChangeListener>	changeListeners;
	private	ChangeEvent				changeEvent;
	private	boolean					editable;
	private	List<T>					envelopes;
	private	NodeId					selectedNodeId;
	private	Dimension				dragDelta;
	private	long					beepTime;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public EnvelopeView(int      plotWidth,
						int      plotHeight,
						int      viewableWidth,
						int      horizontalDivOffset,
						int      horizontalDivWidth,
						int      verticalDivOffset,
						int      verticalDivHeight,
						int      axisVerticalDivIndex,
						String[] verticalScaleMarkings,
						Font     font)
	{
		// Initialise instance variables
		this.plotWidth = plotWidth;
		this.plotHeight = plotHeight;
		this.viewableWidth = viewableWidth;
		this.horizontalDivOffset = horizontalDivOffset;
		this.horizontalDivWidth = horizontalDivWidth;
		this.verticalDivOffset = verticalDivOffset;
		this.verticalDivHeight = verticalDivHeight;
		this.axisVerticalDivIndex = axisVerticalDivIndex;
		scrollUnitIncrement = DEFAULT_SCROLL_INCREMENT_UNIT;
		scrollBlockIncrement = DEFAULT_SCROLL_INCREMENT_BLOCK;
		mode = Mode.SELECT;
		gridColour = DEFAULT_GRID_COLOUR;
		axisColour = DEFAULT_AXIS_COLOUR;
		nodeSelectedColour = DEFAULT_NODE_SELECTED_COLOUR;
		nodeFocusedSelectedColour = DEFAULT_NODE_FOCUSED_SELECTED_COLOUR;
		nodeActiveColour = DEFAULT_NODE_ACTIVE_COLOUR;
		scaleBackgroundColour = DEFAULT_SCALE_BACKGROUND_COLOUR;
		scaleFocusedBackgroundColour = DEFAULT_SCALE_FOCUSED_BACKGROUND_COLOUR;
		scaleForegroundColour = DEFAULT_SCALE_FOREGROUND_COLOUR;
		this.verticalScaleMarkings = verticalScaleMarkings;
		xScale = new XScale(0, font);
		yScale = new YScale(0, font);
		upperLeftCorner = new Corner(font, true);
		lowerLeftCorner = new Corner(font, false);
		changeListeners = new ArrayList<>();
		editable = true;
		envelopes = new ArrayList<>();

		// Set attributes
		setBackground(DEFAULT_BACKGROUND_COLOUR);
		setOpaque(true);
		setFocusable(true);

		// Add commands to action map
		KeyAction.create(this, JComponent.WHEN_FOCUSED, this, KEY_COMMANDS);

		// Add listeners
		addFocusListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Rectangle getNodeRectangle(Point point)
	{
		return new Rectangle(point.x - NODE_HALF_WIDTH, point.y - NODE_HALF_HEIGHT, NODE_WIDTH, NODE_HEIGHT);
	}

	//------------------------------------------------------------------

	public static Rectangle getNodeCaptureRectangle(Point point)
	{
		return new Rectangle(point.x - NODE_CAPTURE_HALF_WIDTH, point.y - NODE_CAPTURE_HALF_HEIGHT,
							 NODE_CAPTURE_WIDTH, NODE_CAPTURE_HEIGHT);
	}

	//------------------------------------------------------------------

	public static Point2D uiToEnvelope(Point point,
									   int   width,
									   int   height)
	{
		double x = 0.0;
		if (--width > 0)
			x = AbstractNode.clampX((double)(point.x - LEFT_MARGIN) / (double)width);
		double y = 0.0;
		if (--height > 0)
			y = AbstractNode.clampY((double)(height - point.y + TOP_MARGIN) / (double)height);
		return new Point2D(x, y);
	}

	//------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	public static <T extends AbstractNode & IViewNode> void drawSegments(AbstractEnvelope<T> envelope,
																		 Graphics            gr,
																		 int                 bandIndex,
																		 int                 startIndex,
																		 int                 endIndex,
																		 int                 width,
																		 int                 height,
																		 Color               colour)
	{
		gr.setColor(colour);

		switch (envelope.getKind())
		{
			case LINEAR:
			{
				int index = startIndex;
				T node = envelope.getNode(index);
				Point point = node.toPoint(width, height, bandIndex);
				while (++index <= endIndex)
				{
					int x0 = point.x;
					int y0 = point.y;
					node = envelope.getNode(index);
					point = node.toPoint(width, height, bandIndex);
					gr.drawLine(x0, y0, point.x, point.y);
				}
				break;
			}

			case CUBIC_SEGMENT:
			{
				int index = startIndex;

				double x0 = 0.0;
				double y0 = 0.0;
				Point p0 = null;

				T node = envelope.getNode(index);
				double x1 = node.x;
				double y1 = node.getY(bandIndex);
				Point p1 = node.toPoint(width, height, bandIndex);

				int hMinus1 = height - 1;
				double factor = 1.0 / (double)(width - 1);

				while (++index <= endIndex)
				{
					x0 = x1;
					y0 = y1;

					node = envelope.getNode(index);
					x1 = node.x;
					y1 = node.getY(bandIndex);

					p0 = p1;
					p1 = node.toPoint(width, height, bandIndex);

					if (p0.x == p1.x)
						gr.drawLine(p0.x, p0.y, p1.x, p1.y);
					else
					{
						double a = 2.0 * (y1 - y0) / (x0 * x0 * (x0 - 3.0 * x1) - x1 * x1 * (x1 - 3.0 * x0));
						double b = -1.5 * a * (x0 + x1);
						double c = 3.0 * a * x0 * x1;
						double d = y0 + 0.5 * a * x0 * x0 * (x0 - 3.0 * x1);

						int prevY = p0.y;
						for (int i = p0.x + 1; i < p1.x; i++)
						{
							double x = (double)(i - EnvelopeView.LEFT_MARGIN) * factor;
							double y = ((a * x + b) * x + c) * x + d;
							int currY = EnvelopeView.TOP_MARGIN + hMinus1 - (int)Math.round(y * (double)hMinus1);
							gr.drawLine(i - 1, prevY, i, currY);
							prevY = currY;
						}
						gr.drawLine(p1.x - 1, prevY, p1.x, p1.y);
					}
				}
				break;
			}

			case CUBIC_SPLINE_A:
			{
				int index = 0;

				double x0 = 0.0;
				double y0 = 0.0;
				Point p0 = null;

				T node = envelope.getNode(index);
				double x1 = node.x;
				double y1 = node.getY(bandIndex);
				Point p1 = node.toPoint(width, height, bandIndex);

				node = envelope.getNode(++index);
				double x2 = node.x;
				double y2 = node.getY(bandIndex);
				Point p2 = node.toPoint(width, height, bandIndex);

				double a = 0.0;
				double b = 0.0;
				double m0 = 0.0;
				double m1 = Double.NaN;

				int hMinus1 = height - 1;
				double factor = 1.0 / (double)(width - 1);

				while (++index - 2 < endIndex)
				{
					x0 = x1;
					y0 = y1;

					x1 = x2;
					y1 = y2;

					if (index < envelope.getNumNodes())
					{
						node = envelope.getNode(index);
						x2 = node.x;
						y2 = node.getY(bandIndex);
					}
					else
					{
						x2 = x1 + (x1 - x0);
						y2 = y1 + (y1 - y0);
						node = (T)node.clone();
						node.x = x2;
						node.setY(bandIndex, y2);
					}

					double dx01 = x1 - x0;
					double dx02 = x2 - x0;
					double dx01_2 = dx01 * dx01;
					double dx01_3 = dx01_2 * dx01;
					double dy01 = y1 - y0;
					double dy02 = y2 - y0;

//XXX
//					m0 = Double.isNaN(m1) ? dy01 / dx01 - dx01 / dx02 * ((y2 - y1) / (x2 - x1) - dy01 / dx01)
					m0 = Double.isNaN(m1) ? dy01 / dx01
										  : m1;
					m1 = dy02 / dx02;

					a = (m0 + m1) / dx01_2 - 2.0 * dy01 / dx01_3;
					b = 3 * dy01 / dx01_2 - (2.0 * m0 + m1) / dx01;

					p0 = p1;
					p1 = p2;
					p2 = node.toPoint(width, height, bandIndex);

					if (index >= startIndex)
					{
						int prevY = p0.y;
						for (int i = p0.x + 1; i < p1.x; i++)
						{
							double dx = (double)(i - EnvelopeView.LEFT_MARGIN) * factor - x0;
							double y = ((a * dx + b) * dx + m0) * dx + y0;
							int currY = EnvelopeView.TOP_MARGIN + hMinus1 - (int)Math.round(y * (double)hMinus1);
							gr.drawLine(i - 1, prevY, i, currY);
							prevY = currY;
						}
						gr.drawLine(p1.x - 1, prevY, p1.x, p1.y);
					}
				}
				break;
			}

			case CUBIC_SPLINE_B:
			{
				int index = 0;

				double x0 = 0.0;
				double y0 = 0.0;
				Point p0 = null;

				T node = envelope.getNode(index);
				double x1 = node.x;
				double y1 = node.getY(bandIndex);
				Point p1 = node.toPoint(width, height, bandIndex);

				node = envelope.getNode(++index);
				double x2 = node.x;
				double y2 = node.getY(bandIndex);
				Point p2 = node.toPoint(width, height, bandIndex);

				double a = 0.0;
				double b = 0.0;
				double m0 = Double.NaN;

				int hMinus1 = height - 1;
				double factor = 1.0 / (double)(width - 1);

				while (++index - 2 < endIndex)
				{
					if ((p0 == null) || (index < envelope.getNumNodes()))
					{
						double prevDx01 = x1 - x0;

						x0 = x1;
						y0 = y1;

						x1 = x2;
						y1 = y2;

						if (index < envelope.getNumNodes())
						{
							node = envelope.getNode(index);
							x2 = node.x;
							y2 = node.getY(bandIndex);
						}
						else
						{
							x2 = x1 + (x1 - x0);
							y2 = y1 + (y1 - y0);
							node = (T)node.clone();
							node.x = x2;
							node.setY(bandIndex, y2);
						}

						double dx01 = x1 - x0;
						double dx02 = x2 - x0;
						double dx12 = x2 - x1;
						double dx01_2 = dx01 * dx01;
						double dx02_2 = dx02 * dx02;
						double dy01 = y1 - y0;
						double dy12 = y2 - y1;
						if (Double.isNaN(m0))
//XXX
//							m0 = dy01 / dx01 - dx01 / dx02 * (dy12 / dx12 - dy01 / dx01);
							m0 = dy01 / dx01;
						else
							m0 += (3.0 * a * prevDx01 + 2.0 * b) * prevDx01;
						a = dy12 / (dx12 * dx02_2) - dy01 * (dx01 + dx02) / (dx01_2 * dx02_2) + m0 / (dx01 * dx02);
						b = dy01 / dx01_2 - a * dx01 - m0 / dx01;
					}

					p0 = p1;
					p1 = p2;
					p2 = node.toPoint(width, height, bandIndex);

					if (index >= startIndex)
					{
						int prevY = p0.y;
						for (int i = p0.x + 1; i < p1.x; i++)
						{
							double dx = (double)(i - EnvelopeView.LEFT_MARGIN) * factor - x0;
							double y = ((a * dx + b) * dx + m0) * dx + y0;
							int currY = EnvelopeView.TOP_MARGIN + hMinus1 - (int)Math.round(y * (double)hMinus1);
							gr.drawLine(i - 1, prevY, i, currY);
							prevY = currY;
						}
						gr.drawLine(p1.x - 1, prevY, p1.x, p1.y);
					}
				}
				break;
			}
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.MODE_SELECT))
			onModeSelect();

		else if (command.startsWith(Command.MODE_DRAW))
			onModeDraw(StringUtils.removePrefix(command, Command.MODE_DRAW));

		else if (command.equals(Command.MODE_ERASE))
			onModeErase();

		else if (command.equals(Command.MODE_EDIT))
			onModeEdit();

		else if (command.equals(Command.SCROLL_LEFT_UNIT))
			onScrollLeftUnit();

		else if (command.equals(Command.SCROLL_RIGHT_UNIT))
			onScrollRightUnit();

		else if (command.equals(Command.SCROLL_LEFT_BLOCK))
			onScrollLeftBlock();

		else if (command.equals(Command.SCROLL_RIGHT_BLOCK))
			onScrollRightBlock();

		else if (command.equals(Command.SCROLL_LEFT_MAX))
			onScrollLeftMax();

		else if (command.equals(Command.SCROLL_RIGHT_MAX))
			onScrollRightMax();

		else if (command.equals(Command.CENTRE_SELECTED_NODE))
			onCentreSelectedNode();

		else if (command.equals(Command.SELECT_NODE_UP))
			onSelectNodeUp();

		else if (command.equals(Command.SELECT_NODE_DOWN))
			onSelectNodeDown();

		else if (command.equals(Command.SELECT_NODE_LEFT))
			onSelectNodeLeft();

		else if (command.equals(Command.SELECT_NODE_RIGHT))
			onSelectNodeRight();

		else if (command.equals(Command.MOVE_NODE_UP))
			onMoveNodeUp();

		else if (command.equals(Command.MOVE_NODE_DOWN))
			onMoveNodeDown();

		else if (command.equals(Command.MOVE_NODE_LEFT))
			onMoveNodeLeft();

		else if (command.equals(Command.MOVE_NODE_RIGHT))
			onMoveNodeRight();

		else if (command.equals(Command.EDIT_NODE))
			onEditNode();

		else if (command.equals(Command.DELETE_NODE))
			onDeleteNode();

		else if (command.equals(Command.SHOW_CONTEXT_MENU))
			onShowContextMenu();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : FocusListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void focusGained(FocusEvent event)
	{
		updateScaleBackgroundColour(scaleFocusedBackgroundColour);
		repaint();
		fireStateChanged();
	}

	//------------------------------------------------------------------

	@Override
	public void focusLost(FocusEvent event)
	{
		updateScaleBackgroundColour(scaleBackgroundColour);
		repaint();
		fireStateChanged();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : MouseListener interface
////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("unchecked")
	@Override
	public void mouseClicked(MouseEvent event)
	{
		if (editable && SwingUtilities.isLeftMouseButton(event))
		{
			switch (mode)
			{
				case DRAW:
				{
					if ((getMask() & 1 << drawIndex) == 0)
					{
						getToolkit().beep();
						return;
					}

					int index = 0;
					int envelopeIndex = 0;
					while (envelopeIndex < envelopes.size())
					{
						int numBands = getEnvelope(envelopeIndex).getNumBands();
						if ((drawIndex >= index) && (drawIndex < index + numBands))
							break;
						index += numBands;
						++envelopeIndex;
					}

					T envelope = getEnvelope(envelopeIndex);
					if (envelope.getNumNodes() >= envelope.getNumNodesRange().upperBound)
					{
						getToolkit().beep();
						return;
					}
					Point2D nodePoint = uiToEnvelope(event.getPoint());
					if (!envelope.isValidNodeX(nodePoint.getX()))
					{
						getToolkit().beep();
						return;
					}

					S node = null;
					if (envelope instanceof SimpleViewEnvelope)
						node = (S)new SimpleViewNode(nodePoint.getX(), nodePoint.getY());
					else if (envelope instanceof CompoundViewEnvelope)
					{
						double[] y = new double[envelope.getNumBands()];
						Arrays.fill(y, nodePoint.getY());
						node = (S)new CompoundViewNode(nodePoint.getX(), y);
					}

					index = Collections.binarySearch(envelope.getNodes(), node);
					if (index < 0)
						index = -1 - index;
					else
					{
						if (node.equals(envelope.getNode(index)))
						{
							getToolkit().beep();
							return;
						}
						int lastIndex = envelope.getNumNodes() - 1;
						if ((index < lastIndex) || !envelope.getNode(lastIndex).fixedX)
							++index;
					}

					setSelectedNodeId(null);
					envelope.getNodes().add(index, node);
					repaint();
					setSelectedNodeId(new NodeId(envelopeIndex, 0, index));
					break;
				}

				case EDIT:
				{
					List<NodeId> ids = findNodes(event.getPoint());
					if (!ids.isEmpty())
					{
						int index = 0;
						while (index < ids.size())
						{
							if (ids.get(index).equals(selectedNodeId))
								break;
							++index;
						}
						if (index >= ids.size())
							index = ids.size() - 1;
						NodeId id = ids.get(index);
						setSelectedNodeId(id);
						if (getNode(id).isFixed(id.bandIndex))
							getToolkit().beep();
						else
						{
							Point2D nodePoint = EnvelopeNodeValueDialog.showDialog(this, this, id);
							if (nodePoint != null)
								setSelectedNodePosition(nodePoint);
						}
					}
					break;
				}

				case SELECT:
				case ERASE:
					// do nothing
					break;
			}
		}
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
		requestFocusInWindow();

		if (SwingUtilities.isLeftMouseButton(event))
		{
			switch (mode)
			{
				case SELECT:
				{
					List<NodeId> ids = findNodes(event.getPoint());
					if (!ids.isEmpty())
					{
						int index = 0;
						while (index < ids.size())
						{
							if (ids.get(index).equals(selectedNodeId))
								break;
							++index;
						}
						if (index >= ids.size())
							index = ids.size() - 1;
						NodeId id = ids.get(index);
						setSelectedNodeId(id);
						if (editable && !getNode(id).isFixed(id.bandIndex))
						{
							Point point = nodeToPoint(id);
							dragDelta = new Dimension(event.getX() - point.x, event.getY() - point.y);
							drawNode(id);
						}
					}
					break;
				}

				case ERASE:
					if (editable)
					{
						beepTime = 0;
						erase(event);
					}
					break;

				case DRAW:
				case EDIT:
					// do nothing
					break;
			}
		}

		showContextMenu(event);
	}

	//------------------------------------------------------------------

	@Override
	public void mouseReleased(MouseEvent event)
	{
		if (!editable)
			return;

		if (SwingUtilities.isLeftMouseButton(event))
		{
			if ((mode == Mode.SELECT) && (dragDelta != null))
			{
				dragDelta = null;
				drawNode(selectedNodeId);
			}
			return;
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
		if (!editable)
			return;

		if (SwingUtilities.isLeftMouseButton(event))
		{
			switch (mode)
			{
				case SELECT:
					if (dragDelta == null)
						break;
					setSelectedNodePosition(new Point(event.getX() - dragDelta.width,
													  event.getY() - dragDelta.height));
					if (viewport != null)
					{
						Rectangle nodeRect = getNodeRectangle(nodeToPoint(selectedNodeId));
						Rectangle viewRect = viewport.getViewRect();
						if (nodeRect.x < viewRect.x)
							setViewX(nodeRect.x);
						else if (nodeRect.x + nodeRect.width > viewRect.x + viewRect.width)
							setViewX(nodeRect.x + nodeRect.width - viewRect.width);
					}
					break;

				case ERASE:
					erase(event);
					break;

				case DRAW:
				case EDIT:
					// do nothing
					break;
			}
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
//  Instance methods : Scrollable interface
////////////////////////////////////////////////////////////////////////

	@Override
	public Dimension getPreferredScrollableViewportSize()
	{
		return new Dimension(LEFT_MARGIN + viewableWidth + RIGHT_MARGIN, TOP_MARGIN + plotHeight + BOTTOM_MARGIN);
	}

	//------------------------------------------------------------------

	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}

	//------------------------------------------------------------------

	@Override
	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}

	//------------------------------------------------------------------

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
										  int       orientation,
										  int       direction)
	{
		int delta = 0;
		if (orientation == SwingConstants.HORIZONTAL)
		{
			int x = visibleRect.x;
			if (direction < 0)
				delta = x - Math.max(0, x - scrollUnitIncrement);
			else
			{
				int xMax = Math.max(0, getWidth() - visibleRect.width);
				delta = Math.min(x + scrollUnitIncrement, xMax) - x;
			}
		}
		return delta;
	}

	//------------------------------------------------------------------

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
										   int       orientation,
										   int       direction)
	{
		int delta = 0;
		if (orientation == SwingConstants.HORIZONTAL)
		{
			int x = visibleRect.x;
			if (direction < 0)
				delta = x - Math.max(0, x - scrollBlockIncrement);
			else
			{
				int xMax = Math.max(0, getWidth() - visibleRect.width);
				delta = Math.min(x + scrollBlockIncrement, xMax) - x;
			}
		}
		return delta;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(LEFT_MARGIN + Math.max(viewableWidth, plotWidth) + RIGHT_MARGIN,
							 TOP_MARGIN + plotHeight + BOTTOM_MARGIN);
	}

	//------------------------------------------------------------------

	@Override
	protected void paintComponent(Graphics gr)
	{
		// Get x coordinate and width of clip region
		Rectangle rect = gr.getClipBounds();
		int x = rect.x;
		int width = rect.width;

		// Fill background
		gr.setColor(getBackground());
		gr.fillRect(x, 0, width, TOP_MARGIN + plotHeight + BOTTOM_MARGIN);

		// Draw vertical grid lines
		int endX = LEFT_MARGIN + plotWidth - 1;
		int x1 = x;
		int x2 = Math.min(x1 + width - 1, endX);

		gr.setColor(gridColour);
		x = LEFT_MARGIN + horizontalDivOffset;
		while (x <= x2)
		{
			if ((x >= x1) && (x < endX))
				gr.drawLine(x, 0, x, TOP_MARGIN + plotHeight - 1 + BOTTOM_MARGIN);
			x += horizontalDivWidth;
		}

		// Draw horizontal grid lines
		if (x1 <= x2)
		{
			int y = TOP_MARGIN + verticalDivOffset;
			int index = 0;
			while (y < TOP_MARGIN + plotHeight)
			{
				gr.setColor((index == axisVerticalDivIndex) ? axisColour : gridColour);
				gr.drawLine(x1, y, x2, y);
				y += verticalDivHeight;
				++index;
			}
		}

		// Draw end vertical line
		if ((endX >= x1) && (endX <= x2))
		{
			gr.setColor(axisColour);
			gr.drawLine(endX, 0, endX, TOP_MARGIN + plotHeight - 1 + BOTTOM_MARGIN);
		}

		// Draw envelope segments and nodes
		boolean redrawSelectedNode = false;
		for (int envelopeIndex = 0; envelopeIndex < envelopes.size(); envelopeIndex++)
		{
			// Get first and last indices of segments and nodes
			T envelope = getEnvelope(envelopeIndex);
			int numNodes = envelope.getNumNodes();
			int s1 = numNodes;
			int n1 = s1;
			int s2 = -1;
			int n2 = s2;
			for (int i = 0; i < numNodes; i++)
			{
				int nodeX = envelope.getNode(i).toPoint(plotWidth, plotHeight, 0).x;
				if (nodeX >= x1)
				{
					if (s1 >= i)
					{
						s1 = (i == 0) ? 0 : i - 1;
						if (n1 > s1)
							n1 = s1;
					}
				}
				else
				{
					if ((nodeX + NODE_HALF_WIDTH >= x1) && (n1 > i))
						n1 = i;
				}

				if (nodeX <= x2)
				{
					if (s2 <= i)
					{
						s2 = (i == numNodes - 1) ? numNodes - 1 : i + 1;
						if (n2 < s2)
							n2 = s2;
					}
				}
				else
				{
					if ((nodeX - NODE_HALF_WIDTH <= x2) && (n2 < i))
						n2 = i;
				}
			}
			s1 = Math.min(s1, numNodes - 1);
			s2 = Math.min(s2, numNodes - 1);

			// Draw segments
			int mask = envelope.getBandMask();
			for (int i = 0; i < envelope.getNumBands(); i++)
			{
				if ((mask & 1 << i) != 0)
					drawSegments(envelope, gr, i, s1, s2, plotWidth, plotHeight, envelope.getSegmentColour(i));
			}

			// Draw nodes
			for (int i = n1; i <= n2; i++)
			{
				for (int j = 0; j < envelope.getNumBands(); j++)
				{
					if ((mask & 1 << j) != 0)
					{
						NodeId id = new NodeId(envelopeIndex, j, i);
						gr.setColor(getNodeColour(id));
						Point p = nodeToPoint(id);
						gr.fillRect(p.x - NODE_HALF_WIDTH, p.y - NODE_HALF_HEIGHT, NODE_WIDTH, NODE_HEIGHT);
					}
				}
			}

			// Test for selected node
			if ((selectedNodeId != null) && (selectedNodeId.envelopeIndex == envelopeIndex) &&
				 (selectedNodeId.nodeIndex >= n1) && (selectedNodeId.nodeIndex <= n2))
				redrawSelectedNode = true;
		}

		// Redraw selected node
		if (redrawSelectedNode)
		{
			gr.setColor(getNodeColour(selectedNodeId));
			Point p = nodeToPoint(selectedNodeId);
			gr.fillRect(p.x - NODE_HALF_WIDTH, p.y - NODE_HALF_HEIGHT, NODE_WIDTH, NODE_HEIGHT);
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int getPlotWidth()
	{
		return plotWidth;
	}

	//------------------------------------------------------------------

	public int getPlotHeight()
	{
		return plotHeight;
	}

	//------------------------------------------------------------------

	public int getViewableWidth()
	{
		return viewableWidth;
	}

	//------------------------------------------------------------------

	public int getHorizontalDivOffset()
	{
		return horizontalDivOffset;
	}

	//------------------------------------------------------------------

	public int getHorizontalDivWidth()
	{
		return horizontalDivWidth;
	}

	//------------------------------------------------------------------

	public int getVerticalDivOffset()
	{
		return verticalDivOffset;
	}

	//------------------------------------------------------------------

	public int getVerticalDivHeight()
	{
		return verticalDivHeight;
	}

	//------------------------------------------------------------------

	public int getAxisVerticalDivIndex()
	{
		return axisVerticalDivIndex;
	}

	//------------------------------------------------------------------

	public int getScrollUnitIncrement()
	{
		return scrollUnitIncrement;
	}

	//------------------------------------------------------------------

	public int getScrollBlockIncrement()
	{
		return scrollBlockIncrement;
	}

	//------------------------------------------------------------------

	public Mode getMode()
	{
		return mode;
	}

	//------------------------------------------------------------------

	public Color getGridColour()
	{
		return gridColour;
	}

	//------------------------------------------------------------------

	public Color getAxisColour()
	{
		return axisColour;
	}

	//------------------------------------------------------------------

	public Color getNodeSelectedColour()
	{
		return nodeSelectedColour;
	}

	//------------------------------------------------------------------

	public Color getNodeFocusedSelectedColour()
	{
		return nodeFocusedSelectedColour;
	}

	//------------------------------------------------------------------

	public Color getNodeActiveColour()
	{
		return nodeActiveColour;
	}

	//------------------------------------------------------------------

	public Color getScaleBackgroundColour()
	{
		return scaleBackgroundColour;
	}

	//------------------------------------------------------------------

	public Color getScaleFocusedBackgroundColour()
	{
		return scaleFocusedBackgroundColour;
	}

	//------------------------------------------------------------------

	public Color getScaleForegroundColour()
	{
		return scaleForegroundColour;
	}

	//------------------------------------------------------------------

	public XScale getXScale()
	{
		return xScale;
	}

	//------------------------------------------------------------------

	public YScale getYScale()
	{
		return yScale;
	}

	//------------------------------------------------------------------

	public Corner getUpperLeftCorner()
	{
		return upperLeftCorner;
	}

	//------------------------------------------------------------------

	public Corner getLowerLeftCorner()
	{
		return lowerLeftCorner;
	}

	//------------------------------------------------------------------

	public T getEnvelope(int index)
	{
		return envelopes.get(index);
	}

	//------------------------------------------------------------------

	public int getNumEnvelopes()
	{
		return envelopes.size();
	}

	//------------------------------------------------------------------

	public int getNumEnvelopeBands()
	{
		int numBands = 0;
		for (int i = 0; i < envelopes.size(); i++)
			numBands += getEnvelope(i).getNumBands();
		return numBands;
	}

	//------------------------------------------------------------------

	public List<S> getNodes(int index)
	{
		return getEnvelope(index).getNodes();
	}

	//------------------------------------------------------------------

	public S getNode(NodeId id)
	{
		return getEnvelope(id.envelopeIndex).getNode(id.nodeIndex);
	}

	//------------------------------------------------------------------

	public NodeId getSelectedNodeId()
	{
		return selectedNodeId;
	}

	//------------------------------------------------------------------

	public boolean isEditable()
	{
		return editable;
	}

	//------------------------------------------------------------------

	public int getMask()
	{
		int mask = 0;
		int shiftCount = 0;
		for (int i = 0; i < envelopes.size(); i++)
		{
			T envelope = getEnvelope(i);
			mask |= envelope.getBandMask() << shiftCount;
			shiftCount += envelope.getNumBands();
		}
		return mask;
	}

	//------------------------------------------------------------------

	public void setScrollUnitIncrement(int increment)
	{
		scrollUnitIncrement = increment;
	}

	//------------------------------------------------------------------

	public void setScrollBlockIncrement(int increment)
	{
		scrollBlockIncrement = increment;
	}

	//------------------------------------------------------------------

	public void setGridColour(Color colour)
	{
		gridColour = colour;
		repaint();
	}

	//------------------------------------------------------------------

	public void setAxisColour(Color colour)
	{
		axisColour = colour;
		repaint();
	}

	//------------------------------------------------------------------

	public void setNodeSelectedColour(Color colour)
	{
		nodeSelectedColour = colour;
		repaint();
	}

	//------------------------------------------------------------------

	public void setNodeFocusedSelectedColour(Color colour)
	{
		nodeFocusedSelectedColour = colour;
		repaint();
	}

	//------------------------------------------------------------------

	public void setNodeActiveColour(Color colour)
	{
		nodeActiveColour = colour;
		repaint();
	}

	//------------------------------------------------------------------

	public void setScaleBackgroundColour(Color colour)
	{
		scaleBackgroundColour = colour;
		if (!isFocusOwner())
			updateScaleBackgroundColour(colour);
	}

	//------------------------------------------------------------------

	public void setScaleFocusedBackgroundColour(Color colour)
	{
		scaleFocusedBackgroundColour = colour;
		if (isFocusOwner())
			updateScaleBackgroundColour(colour);
	}

	//------------------------------------------------------------------

	public void setScaleForegroundColour(Color colour)
	{
		scaleForegroundColour = colour;
		xScale.setForeground(colour);
		yScale.setForeground(colour);
		upperLeftCorner.setForeground(colour);
		lowerLeftCorner.setForeground(colour);
	}

	//------------------------------------------------------------------

	public void setViewport(JViewport viewport)
	{
		this.viewport = viewport;
	}

	//------------------------------------------------------------------

	public void setMode(Mode mode)
	{
		if (!editable && (mode != Mode.SELECT))
			return;
		this.mode = mode;
		setCursor(mode.getCursor());
	}

	//------------------------------------------------------------------

	public void setEditable(boolean editable)
	{
		this.editable = editable;
		if (!editable)
			setMode(Mode.SELECT);
	}

	//------------------------------------------------------------------

	public void resetSelectedNodeId()
	{
		setSelectedNodeId(new NodeId());
	}

	//------------------------------------------------------------------

	public void setSelectedNodeId(NodeId id)
	{
		if ((id == null) ? (selectedNodeId != null) : !id.equals(selectedNodeId))
		{
			NodeId oldSelectedNodeId = selectedNodeId;
			selectedNodeId = id;
			drawNode(oldSelectedNodeId);
			drawNode(selectedNodeId);
			fireStateChanged();
		}
	}

	//------------------------------------------------------------------

	public void setNodes(int     envelopeIndex,
						 List<S> nodes)
	{
		setNodes(envelopeIndex, nodes, false, false);
	}

	//------------------------------------------------------------------

	public void setNodes(int     envelopeIndex,
						 List<S> nodes,
						 boolean loop)
	{
		setNodes(envelopeIndex, nodes, loop, false);
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	public void setNodes(int     envelopeIndex,
						 List<S> nodes,
						 boolean loop,
						 boolean forceLoop)
	{
		getEnvelope(envelopeIndex).setNodes(nodes, loop, forceLoop);
		selectedNodeId = null;
		repaint();
		fireStateChanged();
	}

	//------------------------------------------------------------------

	public void setMask(int mask)
	{
		int oldMask = getMask();
		if (mask != oldMask)
		{
			for (int i = 0; i < envelopes.size(); i++)
			{
				T envelope = getEnvelope(i);
				int numBands = envelope.getNumBands();
				envelope.setBandMask(mask & (1 << numBands) - 1);
				mask >>>= numBands;
			}

			NodeId startId = new NodeId();
			if (selectedNodeId != null)
			{
				startId = selectedNodeId;
				setSelectedNodeId(null);
			}
			NodeId id = startId.clone();
			while (true)
			{
				if (isBandEnabled(id) &&
					 (id.nodeIndex < getEnvelope(id.envelopeIndex).getNumNodes()))
				{
					setSelectedNodeId(id);
					break;
				}
				if (++id.bandIndex >= getEnvelope(id.envelopeIndex).getNumBands())
				{
					id.bandIndex = 0;
					if (++id.envelopeIndex >= envelopes.size())
						id.envelopeIndex = 0;
				}
				if (id.equals(startId))
					break;
			}
			repaint();
		}
	}

	//------------------------------------------------------------------

	public void addEnvelope(T envelope)
	{
		envelopes.add(envelope);
	}

	//------------------------------------------------------------------

	public Point nodeToPoint(NodeId id)
	{
		return getEnvelope(id.envelopeIndex).getNode(id.nodeIndex).toPoint(plotWidth, plotHeight, id.bandIndex);
	}

	//------------------------------------------------------------------

	public Point2D uiToEnvelope(Point point)
	{
		return uiToEnvelope(point, plotWidth, plotHeight);
	}

	//------------------------------------------------------------------

	public void updateResolution(int plotWidth,
								 int horizontalDivWidth)
	{
		if ((this.plotWidth == plotWidth) && (this.horizontalDivWidth == horizontalDivWidth))
			return;

		int oldPlotWidth = this.plotWidth;
		this.plotWidth = plotWidth;
		this.horizontalDivWidth = horizontalDivWidth;
		if (viewport != null)
		{
			revalidate();
			int viewX = viewport.getViewPosition().x;
			int viewWidth = viewport.getWidth();
			double midX = ((double)(viewX - LEFT_MARGIN) + (double)viewWidth * 0.5) / (double)oldPlotWidth;
			int x = (int)Math.round(midX * (double)plotWidth - (double)viewWidth * 0.5) + LEFT_MARGIN;
			setViewX(Math.min(Math.max(0, x), getWidth() - viewWidth));
		}
		repaint();

		xScale.invalidate();
		xScale.repaint();
	}

	//------------------------------------------------------------------

	public JSpinner createXSpinner()
	{
		return new EnvelopeNodeValueDialog.CoordinateSpinner(DELTA_COORDINATE, COORDINATE_FIELD_LENGTH,
															 COORDINATE_FORMAT);
	}

	//------------------------------------------------------------------

	public JSpinner createYSpinner()
	{
		return new EnvelopeNodeValueDialog.CoordinateSpinner(DELTA_COORDINATE, COORDINATE_FIELD_LENGTH,
															 COORDINATE_FORMAT);
	}

	//------------------------------------------------------------------

	public void addChangeListener(ChangeListener listener)
	{
		changeListeners.add(listener);
	}

	//------------------------------------------------------------------

	public void removeChangeListener(ChangeListener listener)
	{
		changeListeners.remove(listener);
	}

	//------------------------------------------------------------------

	public ChangeListener[] getChangeListeners()
	{
		return changeListeners.toArray(ChangeListener[]::new);
	}

	//------------------------------------------------------------------

	protected void fireStateChanged()
	{
		for (int i = changeListeners.size() - 1; i >= 0; i--)
		{
			if (changeEvent == null)
				changeEvent = new ChangeEvent(this);
			changeListeners.get(i).stateChanged(changeEvent);
		}
	}

	//------------------------------------------------------------------

	protected String getXScaleString(int index)
	{
		return null;
	}

	//------------------------------------------------------------------

	protected DoubleRange getNodeXMinMax(NodeId id)
	{
		T envelope = getEnvelope(id.envelopeIndex);
		int nodeIndex = id.nodeIndex;
		AbstractNode node = envelope.getNode(nodeIndex);
		double minDeltaX = envelope.getMinDeltaX();
		double xMin = 0.0;
		double xMax = 1.0;
		if (node.fixedX)
			xMin = xMax = node.x;
		else
		{
			if (nodeIndex > 0)
				xMin = Math.max(0.0, envelope.getNode(nodeIndex - 1).x + minDeltaX);
			if (nodeIndex < envelope.getNumNodes() - 1)
				xMax = Math.min(envelope.getNode(nodeIndex + 1).x - minDeltaX, 1.0);
		}
		return new DoubleRange(xMin, xMax);
	}

	//------------------------------------------------------------------

	protected DoubleRange getNodeYMinMax(NodeId id)
	{
		double yMin = 0.0;
		double yMax = 1.0;
		S node = getNode(id);
		if (node instanceof SimpleViewNode)
		{
			SimpleViewNode node0 = (SimpleViewNode)node;
			if (node0.fixedY)
				yMin = yMax = node0.y;
		}
		else if (node instanceof CompoundViewNode)
		{
			CompoundViewNode node0 = (CompoundViewNode)node;
			if (node0.isFixedY(id.bandIndex))
				yMin = yMax = node0.ys[id.bandIndex];
		}
		return new DoubleRange(yMin, yMax);
	}

	//------------------------------------------------------------------

	private void setViewX(int x)
	{
		viewport.setViewPosition(new Point(x, 0));
	}

	//------------------------------------------------------------------

	private void incrementViewX(int deltaX)
	{
		if (deltaX != 0)
			setViewX(viewport.getViewPosition().x + deltaX);
	}

	//------------------------------------------------------------------

	private void makeNodeViewable(Point point)
	{
		if (viewport != null)
		{
			int x1 = point.x - NODE_HALF_WIDTH;
			if (x1 < viewport.getViewPosition().x)
				setViewX(x1);
			else
			{
				int x2 = point.x + NODE_HALF_WIDTH;
				int viewWidth = viewport.getWidth();
				if (x2 >= viewport.getViewPosition().x + viewWidth)
					setViewX(Math.max(0, x2 + 1 - viewWidth));
			}
		}
	}

	//------------------------------------------------------------------

	private void updateScaleBackgroundColour(Color colour)
	{
		xScale.setBackground(colour);
		yScale.setBackground(colour);
		upperLeftCorner.setBackground(colour);
		lowerLeftCorner.setBackground(colour);
	}

	//------------------------------------------------------------------

	private boolean isBandEnabled(NodeId id)
	{
		return ((getEnvelope(id.envelopeIndex).getBandMask() & 1 << id.bandIndex) != 0);
	}

	//------------------------------------------------------------------

	private Color getNodeColour(NodeId id)
	{
		return id.equals(selectedNodeId)
							? (dragDelta == null)
									? isFocusOwner()
											? nodeFocusedSelectedColour
											: nodeSelectedColour
									: nodeActiveColour
							: getEnvelope(id.envelopeIndex).getNodeColour(id.bandIndex);
	}

	//------------------------------------------------------------------

	private void setSelectedNodePosition(Point point)
	{
		setSelectedNodePosition(uiToEnvelope(point));
	}

	//------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	private void setSelectedNodePosition(Point2D nodePoint)
	{
		if (selectedNodeId == null)
			return;

		T envelope = getEnvelope(selectedNodeId.envelopeIndex);
		int nodeIndex = selectedNodeId.nodeIndex;
		S node = envelope.getNode(nodeIndex);
		int lastNodeIndex = envelope.getNumNodes() - 1;

		DoubleRange xMinMax = getNodeXMinMax(selectedNodeId);
		DoubleRange yMinMax = getNodeYMinMax(selectedNodeId);

		if (node instanceof SimpleViewNode)
		{
			SimpleViewNode oldNode = (SimpleViewNode)node;
			SimpleViewNode newNode = new SimpleViewNode(oldNode);
			newNode.x = Math.min(Math.max(xMinMax.lowerBound, nodePoint.getX()), xMinMax.upperBound);
			newNode.y = Math.min(Math.max(yMinMax.lowerBound, nodePoint.getY()), yMinMax.upperBound);
			if (!oldNode.equals(newNode))
			{
				envelope.setNode(nodeIndex, (S)newNode);
				redrawNode(selectedNodeId, (S)oldNode);
				if (envelope.isLoop())
				{
					NodeId id = selectedNodeId.clone();
					if (nodeIndex == 0)
						id.nodeIndex = lastNodeIndex;
					else
					{
						if (nodeIndex == lastNodeIndex)
							id.nodeIndex = 0;
						else
							id = null;
					}
					if (id != null)
					{
						oldNode = (SimpleViewNode)getNode(id);
						oldNode.y = newNode.y;
						redrawNode(id, (S)oldNode);
					}
				}
				fireStateChanged();
			}
		}

		else if (node instanceof CompoundViewNode)
		{
			CompoundViewNode oldNode = (CompoundViewNode)node;
			CompoundViewNode newNode = new CompoundViewNode(oldNode);
			newNode.x = Math.min(Math.max(xMinMax.lowerBound, nodePoint.getX()), xMinMax.upperBound);
			newNode.ys[selectedNodeId.bandIndex] = Math.min(Math.max(yMinMax.lowerBound, nodePoint.getY()),
															yMinMax.upperBound);
			if (!oldNode.equals(newNode))
			{
				envelope.setNode(nodeIndex, (S)newNode);
				redrawNode(selectedNodeId, (S)oldNode);
				if (envelope.isLoop())
				{
					NodeId id = selectedNodeId.clone();
					if (nodeIndex == 0)
						id.nodeIndex = lastNodeIndex;
					else
					{
						if (nodeIndex == lastNodeIndex)
							id.nodeIndex = 0;
						else
							id = null;
					}
					if (id != null)
					{
						oldNode = (CompoundViewNode)getNode(id);
						oldNode.ys = newNode.ys.clone();
						redrawNode(id, (S)oldNode);
					}
				}
				fireStateChanged();
			}
		}
	}

	//------------------------------------------------------------------

	private void redrawNode(NodeId id,
							S      oldNode)
	{
		int oldX = oldNode.toPoint(plotWidth, plotHeight, 0).x;
		T envelope = getEnvelope(id.envelopeIndex);
		NodeId tempId = id.clone();

		tempId.nodeIndex = 0;
		if (envelope.hasDiscreteSegments())
			tempId.nodeIndex = Math.max(tempId.nodeIndex, id.nodeIndex - 1);
		int x1 = Math.min(oldX, nodeToPoint(tempId).x) - NODE_HALF_WIDTH;

		tempId.nodeIndex = envelope.getNumNodes() - 1;
		if (envelope.hasDiscreteSegments())
			tempId.nodeIndex = Math.min(id.nodeIndex + 1, tempId.nodeIndex);
		int x2 = Math.max(oldX, nodeToPoint(tempId).x) + NODE_HALF_WIDTH;

		repaint(new Rectangle(x1, 0, x2 - x1 + 1, getHeight()));
	}

	//------------------------------------------------------------------

	private void drawNode(NodeId id)
	{
		if ((id != null) && isBandEnabled(id))
		{
			Point p = nodeToPoint(id);
			repaint(new Rectangle(p.x - NODE_HALF_WIDTH, p.y - NODE_HALF_HEIGHT, NODE_WIDTH, NODE_HEIGHT));
		}
	}

	//------------------------------------------------------------------

	private void moveNode(int dx,
						  int dy)
	{
		Point point = nodeToPoint(selectedNodeId);
		point.translate(dx, dy);
		setSelectedNodePosition(point);
		makeNodeViewable(nodeToPoint(selectedNodeId));
	}

	//------------------------------------------------------------------

	private List<NodeId> findNodes(Point point)
	{
		List<NodeId> ids = new ArrayList<>();
		for (int i = 0; i < envelopes.size(); i++)
		{
			T envelope = getEnvelope(i);
			for (int j = 0; j < envelope.getNumBands(); j++)
			{
				for (int k = 0; k < envelope.getNumNodes(); k++)
				{
					NodeId id = new NodeId(i, j, k);
					if (isBandEnabled(id) && getNodeCaptureRectangle(nodeToPoint(id)).contains(point))
						ids.add(id);
				}
			}
		}
		return ids;
	}

	//------------------------------------------------------------------

	private void deleteNode(NodeId id)
	{
		T envelope = getEnvelope(id.envelopeIndex);
		if (envelope.getNumNodes() <= envelope.getNumNodesRange().lowerBound)
			beep();
		else
		{
			NodeId oldSelectedNodeId = selectedNodeId;
			if (selectedNodeId.nodeIndex >= id.nodeIndex)
				setSelectedNodeId(null);
			envelope.getNodes().remove(id.nodeIndex);
			repaint();
			if (oldSelectedNodeId.nodeIndex > id.nodeIndex)
				--oldSelectedNodeId.nodeIndex;
			setSelectedNodeId(oldSelectedNodeId);
		}
	}

	//------------------------------------------------------------------

	private void erase(MouseEvent event)
	{
		List<NodeId> ids = findNodes(event.getPoint());
		int index = ids.size();
		while (--index >= 0)
		{
			NodeId id = ids.get(index);
			if (!getNode(id).isPartiallyFixed(id.bandIndex))
			{
				deleteNode(id);
				break;
			}
		}
		if (index < 0)
			beep();
	}

	//------------------------------------------------------------------

	private void beep()
	{
		long time = System.currentTimeMillis();
		if (time >= beepTime)
		{
			getToolkit().beep();
			beepTime = time + MIN_BEEP_INTERVAL;
		}
	}

	//------------------------------------------------------------------

	private void showContextMenu(MouseEvent event)
	{
		if (event.isPopupTrigger())
			showContextMenu(event.getX(), event.getY());
	}

	//------------------------------------------------------------------

	private void showContextMenu(int x,
								 int y)
	{
		if (contextMenu == null)
			contextMenu = new JPopupMenu();
		else
			contextMenu.removeAll();

		contextMenu.add(new FMenuItem(new CommandAction(Command.MODE_SELECT, Mode.SELECT.toString(),
														Mode.SELECT.getMenuIcon())));

		if (getNumEnvelopeBands() == 1)
			contextMenu.add(new FMenuItem(new CommandAction(Command.MODE_DRAW + 0, Mode.DRAW.toString(),
															Mode.DRAW.getMenuIcon()),
										  getMask() != 0));
		else
		{
			JMenu submenu = new FMenu(Mode.DRAW.toString());
			submenu.setIcon(Mode.DRAW.getMenuIcon());
			int index = 0;
			for (int i = 0; i < envelopes.size(); i++)
			{
				T envelope = getEnvelope(i);
				for (int j = 0; j < envelope.getNumBands(); j++)
				{
					Icon icon = new ColourSampleIcon(COLOUR_SAMPLE_WIDTH, COLOUR_SAMPLE_HEIGHT,
													 envelope.getSegmentColour(j));
					submenu.add(new FRadioButtonMenuItem(new CommandAction(Command.MODE_DRAW + index,
																		   envelope.getName(j), icon),
														 (mode == Mode.DRAW) && (index == drawIndex),
														 (envelope.getBandMask() & 1 << j) != 0));
					++index;
				}
			}
			contextMenu.add(submenu);
		}

		contextMenu.add(new FMenuItem(new CommandAction(Command.MODE_ERASE, Mode.ERASE.toString(),
														Mode.ERASE.getMenuIcon())));
		contextMenu.add(new FMenuItem(new CommandAction(Command.MODE_EDIT, Mode.EDIT.toString(),
														Mode.EDIT.getMenuIcon())));

		contextMenu.show(this, x, y);
	}

	//------------------------------------------------------------------

	private void onModeSelect()
	{
		if (editable)
			setMode(Mode.SELECT);
	}

	//------------------------------------------------------------------

	private void onModeDraw(String str)
	{
		if (editable)
		{
			drawIndex = Integer.parseInt(str);
			setMode(Mode.DRAW);
		}
	}

	//------------------------------------------------------------------

	private void onModeErase()
	{
		if (editable)
			setMode(Mode.ERASE);
	}

	//------------------------------------------------------------------

	private void onModeEdit()
	{
		if (editable)
			setMode(Mode.EDIT);
	}

	//------------------------------------------------------------------

	private void onScrollLeftUnit()
	{
		if (viewport != null)
			incrementViewX(-getScrollableUnitIncrement(viewport.getViewRect(), SwingConstants.HORIZONTAL, -1));
	}

	//------------------------------------------------------------------

	private void onScrollRightUnit()
	{
		if (viewport != null)
			incrementViewX(getScrollableUnitIncrement(viewport.getViewRect(), SwingConstants.HORIZONTAL, 1));
	}

	//------------------------------------------------------------------

	private void onScrollLeftBlock()
	{
		if (viewport != null)
			incrementViewX(-getScrollableBlockIncrement(viewport.getViewRect(), SwingConstants.HORIZONTAL, -1));
	}

	//------------------------------------------------------------------

	private void onScrollRightBlock()
	{
		if (viewport != null)
			incrementViewX(getScrollableBlockIncrement(viewport.getViewRect(), SwingConstants.HORIZONTAL, 1));
	}

	//------------------------------------------------------------------

	private void onScrollLeftMax()
	{
		if (viewport != null)
			setViewX(0);
	}

	//------------------------------------------------------------------

	private void onScrollRightMax()
	{
		if (viewport != null)
			setViewX(Math.max(0, getWidth() - viewport.getWidth()));
	}

	//------------------------------------------------------------------

	private void onCentreSelectedNode()
	{
		if ((viewport != null) && (selectedNodeId != null))
		{
			int viewWidth = viewport.getWidth();
			setViewX(Math.min(Math.max(0, nodeToPoint(selectedNodeId).x - viewWidth / 2),
							  Math.max(0, getWidth() - viewWidth)));
		}
	}

	//------------------------------------------------------------------

	private void onSelectNodeUp()
	{
		if (selectedNodeId != null)
		{
			NodeId id = selectedNodeId.clone();
			while (true)
			{
				if (++id.bandIndex >= getEnvelope(id.envelopeIndex).getNumBands())
				{
					id.bandIndex = 0;
					if (++id.envelopeIndex >= envelopes.size())
						id.envelopeIndex = 0;
				}
				if (id.equals(selectedNodeId))
					break;
				if (isBandEnabled(id) &&
					 (id.nodeIndex < getEnvelope(id.envelopeIndex).getNumNodes()))
				{
					setSelectedNodeId(id);
					break;
				}
			}
		}
	}

	//------------------------------------------------------------------

	private void onSelectNodeDown()
	{
		if (selectedNodeId != null)
		{
			NodeId id = selectedNodeId.clone();
			while (true)
			{
				if (--id.bandIndex < 0)
				{
					id.bandIndex = getEnvelope(id.envelopeIndex).getNumBands() - 1;
					if (--id.envelopeIndex < 0)
						id.envelopeIndex = envelopes.size() - 1;
				}
				if (id.equals(selectedNodeId))
					break;
				if (isBandEnabled(id) &&
					 (id.nodeIndex < getEnvelope(id.envelopeIndex).getNumNodes()))
				{
					setSelectedNodeId(id);
					break;
				}
			}
		}
	}

	//------------------------------------------------------------------

	private void onSelectNodeLeft()
	{
		if (selectedNodeId != null)
		{
			NodeId id = selectedNodeId.clone();
			if (id.nodeIndex > 0)
			{
				--id.nodeIndex;
				setSelectedNodeId(id);
			}
		}
	}

	//------------------------------------------------------------------

	private void onSelectNodeRight()
	{
		if (selectedNodeId != null)
		{
			NodeId id = selectedNodeId.clone();
			if (id.nodeIndex < getEnvelope(id.envelopeIndex).getNumNodes() - 1)
			{
				++id.nodeIndex;
				setSelectedNodeId(id);
			}
		}
	}

	//------------------------------------------------------------------

	private void onMoveNodeUp()
	{
		if ((selectedNodeId != null) && editable)
			moveNode(0, -1);
	}

	//------------------------------------------------------------------

	private void onMoveNodeDown()
	{
		if ((selectedNodeId != null) && editable)
			moveNode(0, 1);
	}

	//------------------------------------------------------------------

	private void onMoveNodeLeft()
	{
		if ((selectedNodeId != null) && editable)
			moveNode(-1, 0);
	}

	//------------------------------------------------------------------

	private void onMoveNodeRight()
	{
		if ((selectedNodeId != null) && editable)
			moveNode(1, 0);
	}

	//------------------------------------------------------------------

	private void onEditNode()
	{
		if ((selectedNodeId != null) && editable)
		{
			if (getNode(selectedNodeId).isFixed(selectedNodeId.bandIndex))
				getToolkit().beep();
			else
			{
				Point2D nodePoint = EnvelopeNodeValueDialog.showDialog(this, this, selectedNodeId);
				if (nodePoint != null)
					setSelectedNodePosition(nodePoint);
			}
		}
	}

	//------------------------------------------------------------------

	private void onDeleteNode()
	{
		if ((selectedNodeId != null) && editable)
		{
			if (getNode(selectedNodeId).isPartiallyFixed(selectedNodeId.bandIndex))
				getToolkit().beep();
			else
			{
				beepTime = 0;
				deleteNode(selectedNodeId);
			}
		}
	}

	//------------------------------------------------------------------

	private void onShowContextMenu()
	{
		if (editable)
		{
			if (dragDelta == null)
			{
				int x = (viewport == null) ? 0 : viewport.getViewPosition().x;
				showContextMenu(x, 0);
			}
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
