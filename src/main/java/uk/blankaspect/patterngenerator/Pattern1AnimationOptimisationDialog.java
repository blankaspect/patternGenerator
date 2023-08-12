/*====================================================================*\

Pattern1AnimationOptimisationDialog.java

Pattern 1 animation optimisation dialog class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.awt.image.BufferedImage;

import java.util.EnumSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.border.TitledBorder;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.colour.Colours;

import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.spinner.FIntegerSpinner;

import uk.blankaspect.ui.swing.text.TextRendering;

//----------------------------------------------------------------------


// PATTERN 1 ANIMATION OPTIMISATION DIALOG CLASS


class Pattern1AnimationOptimisationDialog
	extends JDialog
	implements ActionListener, ChangeListener, ListSelectionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	DEFAULT_NUM_FRAMES	= 100;

	private static final	int	START_FRAME_FIELD_LENGTH	= 10;
	private static final	int	NUM_FRAMES_FIELD_LENGTH		= 6;

	private static final	int	NUM_VIEWABLE_MAP_TABLE_ROWS	= 4;

	private static final	String	TITLE_STR						= "Normalise animation brightness";
	private static final	String	ANIMATION_KINDS_STR				= "Animation kinds";
	private static final	String	FIRST_FRAME_STR					= "First frame";
	private static final	String	NUM_FRAMES_STR					= "Number of frames";
	private static final	String	SET_STR							= "Set";
	private static final	String	DELETE_STR						= "Delete";
	private static final	String	SET_BRIGHTNESS_STR				= "Set brightness";
	private static final	String	DELETE_BRIGHTNESS_RANGES_STR	= "Delete brightness ranges";
	private static final	String	DELETE_PROMPT_STR				= "Do you want to delete the " +
																		"selected brightness ranges?";

	// Commands
	private interface Command
	{
		String	SET		= "set";
		String	DELETE	= "delete";
		String	CLOSE	= "close";
	}

	private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
	{
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.SHIFT_DOWN_MASK),
			Command.DELETE
		)
	};

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// TABLE COLUMN


	private enum Column
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		POSITION
		(
			"Pos",
			SwingConstants.CENTER
		),

		PHASE
		(
			"Pha",
			SwingConstants.CENTER
		),

		ORIENTATION
		(
			"Ori",
			SwingConstants.CENTER
		),

		START_FRAME
		(
			"First frame",
			SwingConstants.TRAILING
		);

		//--------------------------------------------------------------

		private static final	Set<Column>	ANIMATION_KINDS	= EnumSet.of
		(
			POSITION,
			PHASE,
			ORIENTATION
		);

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Column(String text,
					   int    alignment)
		{
			this.text = text;
			this.alignment = alignment;
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
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;
		private	int		alignment;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// MAP KEY TABLE CLASS


	private static class MapKeyTable
		extends JTable
		implements ActionListener, FocusListener, MouseListener
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	CELL_VERTICAL_MARGIN	= 1;
		private static final	int	CELL_HORIZONTAL_MARGIN	= 5;
		private static final	int	GRID_LINE_WIDTH			= 1;

		// Commands
		private interface Command
		{
			String	FOCUS_PREVIOUS	= "focusPrevious";
			String	FOCUS_NEXT		= "focusNext";
			String	SELECT_ALL		= "selectAll";
		}

		private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
		{
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK),
				Command.FOCUS_PREVIOUS
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
				Command.FOCUS_NEXT
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK),
				Command.SELECT_ALL
			)
		};

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private MapKeyTable(AbstractTableModel tableModel)
		{
			// Call superclass constructor
			super(tableModel);

			// Set attributes
			setGridColor(Colours.Table.GRID.getColour());
			setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			setIntercellSpacing(new Dimension());
			AppFont.MAIN.apply(getTableHeader());
			AppFont.MAIN.apply(this);
			int rowHeight = 2 * CELL_VERTICAL_MARGIN + GRID_LINE_WIDTH +
																getFontMetrics(getFont()).getHeight();
			setRowHeight(rowHeight);

			// Initialise columns
			int maxWidth = 0;
			TableColumnModel columnModel = getColumnModel();
			for (Column id : Column.values())
			{
				TableColumn column = columnModel.getColumn(id.ordinal());
				column.setIdentifier(id);
				String text = null;
				TableCellRenderer headerRenderer = new TextRenderer(id.text, id.alignment);
				TableCellRenderer cellRenderer = null;
				switch (id)
				{
					case POSITION:
					case PHASE:
					case ORIENTATION:
						cellRenderer = new BooleanRenderer();
						break;

					case START_FRAME:
						text = "0".repeat(START_FRAME_FIELD_LENGTH);
						cellRenderer = new TextRenderer(text, id.alignment);
						break;
				}
				int width = Math.max(((JComponent)headerRenderer).getPreferredSize().width,
									 ((JComponent)cellRenderer).getPreferredSize().width);
				if (Column.ANIMATION_KINDS.contains(id))
				{
					if (maxWidth < width)
						maxWidth = width;
				}
				else
				{
					column.setMinWidth(width);
					column.setMaxWidth(width);
					column.setPreferredWidth(width);
				}
				column.setHeaderValue(id);
				column.setHeaderRenderer(headerRenderer);
				column.setCellRenderer(cellRenderer);
			}
			for (Column id : Column.ANIMATION_KINDS)
			{
				TableColumn column = columnModel.getColumn(id.ordinal());
				column.setMinWidth(maxWidth);
				column.setMaxWidth(maxWidth);
				column.setPreferredWidth(maxWidth);
			}

			// Set viewport size
			setPreferredScrollableViewportSize(new Dimension(columnModel.getTotalColumnWidth(),
															 getRowCount() * rowHeight));

			// Remove keys from input map
			InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			while (inputMap != null)
			{
				inputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
				inputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
				inputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK));
				inputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
				inputMap = inputMap.getParent();
			}

			// Add listeners
			addFocusListener(this);
			getTableHeader().addMouseListener(this);

			// Add commands to action map
			KeyAction.create(this, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this, KEY_COMMANDS);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		public void actionPerformed(ActionEvent event)
		{
			String command = event.getActionCommand();

			if (command.equals(Command.FOCUS_PREVIOUS))
				onFocusPrevious();

			else if (command.equals(Command.FOCUS_NEXT))
				onFocusNext();

			else if (command.equals(Command.SELECT_ALL))
				onSelectAll();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : FocusListener interface
	////////////////////////////////////////////////////////////////////

		public void focusGained(FocusEvent event)
		{
			getTableHeader().repaint();
			getParent().repaint();
		}

		//--------------------------------------------------------------

		public void focusLost(FocusEvent event)
		{
			getTableHeader().repaint();
			getParent().repaint();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : MouseListener interface
	////////////////////////////////////////////////////////////////////

		public void mouseClicked(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		public void mouseEntered(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		public void mouseExited(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		public void mousePressed(MouseEvent event)
		{
			if (SwingUtilities.isLeftMouseButton(event))
				requestFocusInWindow();
		}

		//--------------------------------------------------------------

		public void mouseReleased(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void onFocusPrevious()
		{
			if (isFocusOwner())
				transferFocusBackward();
		}

		//--------------------------------------------------------------

		private void onFocusNext()
		{
			if (isFocusOwner())
				transferFocus();
		}

		//--------------------------------------------------------------

		private void onSelectAll()
		{
			selectAll();
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// MAP KEY TABLE MODEL CLASS


	private class MapKeyTableModel
		extends AbstractTableModel
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private MapKeyTableModel(long[] keys)
		{
			this.keys = keys;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public int getRowCount()
		{
			return keys.length;
		}

		//--------------------------------------------------------------

		@Override
		public int getColumnCount()
		{
			return Column.values().length;
		}

		//--------------------------------------------------------------

		@Override
		public Object getValueAt(int row,
								 int column)
		{
			if ((column >= 0) && (column < Column.values().length))
			{
				switch (Column.values()[column])
				{
					case POSITION:
						return getAnimationKinds(row).contains(Pattern1Image.AnimationKind.POSITION);

					case PHASE:
						return getAnimationKinds(row).contains(Pattern1Image.AnimationKind.PHASE);

					case ORIENTATION:
						return getAnimationKinds(row).contains(Pattern1Image.AnimationKind.ORIENTATION);

					case START_FRAME:
						return getStartFrame(row);
				}
			}
			return null;
		}

		//--------------------------------------------------------------

		@Override
		public boolean isCellEditable(int row,
									  int column)
		{
			return false;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private Set<Pattern1Image.AnimationKind> getAnimationKinds(int index)
		{
			return Pattern1Image.AnimationKind.bitFieldToSet((int)(keys[index] >> 32));
		}

		//--------------------------------------------------------------

		private int getStartFrame(int index)
		{
			return (int)keys[index];
		}

		//--------------------------------------------------------------

		private void setKeys(long[] keys)
		{
			this.keys = keys;
			fireTableDataChanged();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	long[]	keys;

	}

	//==================================================================


	// TEXT RENDERER CLASS


	private static class TextRenderer
		extends JComponent
		implements TableCellRenderer
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private TextRenderer(String text,
							 int    alignment)
		{
			this.text = text;
			this.alignment = alignment;
			AppFont.MAIN.apply(this);
			setForeground(Colours.Table.FOREGROUND.getColour());
			setOpaque(true);
			setFocusable(false);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : TableCellRenderer interface
	////////////////////////////////////////////////////////////////////

		public Component getTableCellRendererComponent(JTable  table,
													   Object  value,
													   boolean isSelected,
													   boolean hasFocus,
													   int     row,
													   int     column)
		{
			isHeader = (row < 0);
			text = (value == null) ? null : value.toString();
			setBackground(isHeader ? table.isFocusOwner()
											? Colours.Table.FOCUSED_HEADER_BACKGROUND1.getColour()
											: Colours.Table.HEADER_BACKGROUND1.getColour()
								   : isSelected
											? table.isFocusOwner()
												? Colours.Table.FOCUSED_SELECTION_BACKGROUND.getColour()
												: Colours.Table.SELECTION_BACKGROUND.getColour()
											: Colours.Table.BACKGROUND.getColour());
			borderColour = table.isFocusOwner() ? Colours.Table.FOCUSED_HEADER_BORDER1.getColour()
												: Colours.Table.HEADER_BORDER1.getColour();
			return this;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Dimension getPreferredSize()
		{
			FontMetrics fontMetrics = getFontMetrics(getFont());
			int width = 2 * MapKeyTable.CELL_HORIZONTAL_MARGIN + MapKeyTable.GRID_LINE_WIDTH +
																			fontMetrics.stringWidth(text);
			int height = 2 * MapKeyTable.CELL_VERTICAL_MARGIN + MapKeyTable.GRID_LINE_WIDTH +
													fontMetrics.getAscent() + fontMetrics.getDescent();
			return new Dimension(width, height);
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Create copy of graphics context
			gr = gr.create();

			// Fill background
			int width = getWidth();
			int height = getHeight();
			gr.setColor(getBackground());
			gr.fillRect(0, 0, width, height);

			// Draw text
			if (text != null)
			{
				// Set rendering hints for text antialiasing and fractional metrics
				TextRendering.setHints((Graphics2D)gr);

				// Draw text
				gr.setColor(getForeground());
				FontMetrics fontMetrics = gr.getFontMetrics();
				int x = 0;
				switch (alignment)
				{
					case SwingConstants.LEADING:
						x = MapKeyTable.CELL_HORIZONTAL_MARGIN;
						break;

					case SwingConstants.CENTER:
						x = (width - fontMetrics.stringWidth(text)) / 2;
						break;

					case SwingConstants.TRAILING:
						x = width - (fontMetrics.stringWidth(text) + MapKeyTable.CELL_HORIZONTAL_MARGIN);
						break;
				}
				gr.drawString(text, x, FontUtils.getBaselineOffset(height, fontMetrics));
			}

			// Draw cell border
			--width;
			--height;
			gr.setColor(isHeader ? borderColour : Colours.Table.GRID.getColour());
			gr.drawLine(width, 0, width, height);
			gr.drawLine(0, height, width, height);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;
		private	int		alignment;
		private	boolean	isHeader;
		private	Color	borderColour;

	}

	//==================================================================


	// BOOLEAN RENDERER CLASS


	private static class BooleanRenderer
		extends JComponent
		implements TableCellRenderer
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	MARKER_WIDTH	= 6;
		private static final	int	MARKER_HEIGHT	= MARKER_WIDTH;
		private static final	int	MARKER_MARGIN	= 1;

		private static final	Color	MARKER_COLOUR	= Color.BLACK;

		private static final	BufferedImage	MARKER_IMAGE;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private BooleanRenderer()
		{
			setForeground(Colours.Table.FOREGROUND.getColour());
			setOpaque(true);
			setFocusable(false);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : TableCellRenderer interface
	////////////////////////////////////////////////////////////////////

		public Component getTableCellRendererComponent(JTable  table,
													   Object  value,
													   boolean isSelected,
													   boolean hasFocus,
													   int     row,
													   int     column)
		{
			this.value = (Boolean)value;
			setBackground(isSelected ? table.isFocusOwner()
												? Colours.Table.FOCUSED_SELECTION_BACKGROUND.getColour()
												: Colours.Table.SELECTION_BACKGROUND.getColour()
									 : Colours.Table.BACKGROUND.getColour());
			return this;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Dimension getPreferredSize()
		{
			int width = 2 * MapKeyTable.CELL_HORIZONTAL_MARGIN + MapKeyTable.GRID_LINE_WIDTH +
																		MARKER_WIDTH + 2 * MARKER_MARGIN;
			int height = 2 * MapKeyTable.CELL_VERTICAL_MARGIN + MapKeyTable.GRID_LINE_WIDTH +
																		MARKER_HEIGHT + 2 * MARKER_MARGIN;
			return new Dimension(width, height);
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Create copy of graphics context
			gr = gr.create();

			// Fill background
			int width = getWidth();
			int height = getHeight();
			gr.setColor(getBackground());
			gr.fillRect(0, 0, width, height);

			// Draw marker
			if (value)
			{
				gr.setColor(getForeground());
				int x = (width - MARKER_WIDTH) / 2;
				int y = (height - MARKER_HEIGHT + 1) / 2;
				gr.drawImage(MARKER_IMAGE, x, y, null);
			}

			// Draw cell border
			--width;
			--height;
			gr.setColor(Colours.Table.GRID.getColour());
			gr.drawLine(width, 0, width, height);
			gr.drawLine(0, height, width, height);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Static initialiser
	////////////////////////////////////////////////////////////////////

		static
		{
			MARKER_IMAGE = new BufferedImage(MARKER_WIDTH, MARKER_HEIGHT, BufferedImage.TYPE_INT_ARGB);
			Graphics2D gr = MARKER_IMAGE.createGraphics();
			gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			gr.setColor(MARKER_COLOUR);
			gr.fillOval(0, 0, MARKER_WIDTH, MARKER_HEIGHT);
		}

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	boolean	value;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// ANIMATION KIND PANEL


	private class AnimationKindsPanel
		extends Pattern1AnimationKindsPanel
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public AnimationKindsPanel(Set<Pattern1Image.AnimationKind> enabledKinds,
								   Set<Pattern1Image.AnimationKind> selectedKinds)
		{
			super(enabledKinds, selectedKinds);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected void animationKindsChanged()
		{
			updateComponents();
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private Pattern1AnimationOptimisationDialog(Window                           owner,
												Pattern1Document                 document,
												Set<Pattern1Image.AnimationKind> enabledAnimationKinds)
	{

		// Call superclass constructor
		super(owner, TITLE_STR, Dialog.ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());

		// Initialise instance variables
		this.document = document;


		//----  Map table scroll pane

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		mapKeyTableModel = new MapKeyTableModel(document.getBrightnessRangeKeys());

		mapKeyTable = new MapKeyTable(mapKeyTableModel);
		mapKeyTable.getSelectionModel().addListSelectionListener(this);
		KeyAction.create(mapKeyTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this, KEY_COMMANDS);

		JScrollPane tableScrollPane = new JScrollPane(mapKeyTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
													  JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		Dimension viewSize = new Dimension(mapKeyTable.getPreferredScrollableViewportSize().width,
										   NUM_VIEWABLE_MAP_TABLE_ROWS * mapKeyTable.getRowHeight());
		tableScrollPane.getViewport().setPreferredSize(viewSize);
		tableScrollPane.getViewport().setFocusable(false);
		tableScrollPane.getVerticalScrollBar().setFocusable(false);
		tableScrollPane.getHorizontalScrollBar().setFocusable(false);


		//----  Animation kinds panel

		JPanel outerAnimationKindsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		TitledBorder.setPaddedBorder(outerAnimationKindsPanel, ANIMATION_KINDS_STR);

		animationKindsPanel = new AnimationKindsPanel(enabledAnimationKinds, animationKinds);
		outerAnimationKindsPanel.add(animationKindsPanel);


		//----  Control panel

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		// Label: start frame
		JLabel startFrameLabel = new FLabel(FIRST_FRAME_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(startFrameLabel, gbc);
		controlPanel.add(startFrameLabel);

		// Spinner: start frame
		startFrameSpinner = new FIntegerSpinner(startFrame, ImageSequenceParams.MIN_FRAME_INDEX,
												ImageSequenceParams.MAX_FRAME_INDEX,
												START_FRAME_FIELD_LENGTH);
		startFrameSpinner.addChangeListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(startFrameSpinner, gbc);
		controlPanel.add(startFrameSpinner);

		// Label: number of frames
		JLabel numFramesLabel = new FLabel(NUM_FRAMES_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(numFramesLabel, gbc);
		controlPanel.add(numFramesLabel);

		// Spinner: number of frames
		numFramesSpinner = new FIntegerSpinner(numFrames, ImageSequenceParams.MIN_NUM_FRAMES,
											   ImageSequenceParams.MAX_NUM_FRAMES,
											   NUM_FRAMES_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(numFramesSpinner, gbc);
		controlPanel.add(numFramesSpinner);


		//----  Button panel

		JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 8, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

		// Button: set
		setButton = new FButton(SET_STR);
		setButton.setActionCommand(Command.SET);
		setButton.addActionListener(this);
		buttonPanel.add(setButton);

		// Button: delete
		deleteButton = new FButton(DELETE_STR + AppConstants.ELLIPSIS_STR);
		deleteButton.setActionCommand(Command.DELETE);
		deleteButton.addActionListener(this);
		buttonPanel.add(deleteButton);

		// Button: close
		JButton closeButton = new FButton(AppConstants.CLOSE_STR);
		closeButton.setActionCommand(Command.CLOSE);
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);


		//----  Main panel

		JPanel mainPanel = new JPanel(gridBag);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(tableScrollPane, gbc);
		mainPanel.add(tableScrollPane);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(outerAnimationKindsPanel, gbc);
		mainPanel.add(outerAnimationKindsPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		mainPanel.add(controlPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(buttonPanel, gbc);
		mainPanel.add(buttonPanel);

		// Add commands to action map
		KeyAction.create(mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
						 KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), Command.CLOSE, this);

		// Update components
		updateComponents();


		//----  Window

		// Set content pane
		setContentPane(mainPanel);

		// Dispose of window explicitly
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// Handle window closing
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent event)
			{
				onClose();
			}
		});

		// Prevent dialog from being resized
		setResizable(false);

		// Resize dialog to its preferred size
		pack();

		// Set location of dialog box
		if (location == null)
			location = GuiUtils.getComponentLocation(this, owner);
		setLocation(location);

		// Show dialog
		setVisible(true);

	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void showDialog(Component                        parent,
								  Pattern1Document                 document,
								  Set<Pattern1Image.AnimationKind> enabledAnimationKinds)
	{
		new Pattern1AnimationOptimisationDialog(GuiUtils.getWindow(parent), document,
												enabledAnimationKinds);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.SET))
			onSet();

		else if (command.equals(Command.DELETE))
			onDelete();

		else if (command.equals(Command.CLOSE))
			onClose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ChangeListener interface
////////////////////////////////////////////////////////////////////////

	public void stateChanged(ChangeEvent event)
	{
		Object eventSource = event.getSource();

		if (eventSource == startFrameSpinner)
			updateComponents();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ListSelectionListener interface
////////////////////////////////////////////////////////////////////////

	public void valueChanged(ListSelectionEvent event)
	{
		updateComponents();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private Set<Pattern1Image.AnimationKind> getAnimationKinds()
	{
		return animationKindsPanel.getAnimationKinds();
	}

	//------------------------------------------------------------------

	private void updateComponents()
	{
		setButton.setEnabled(!getAnimationKinds().isEmpty());
		deleteButton.setEnabled(mapKeyTable.getSelectedRowCount() > 0);
	}

	//------------------------------------------------------------------

	private void onSet()
	{
		try
		{
			TaskProgressDialog.showDialog(this, SET_BRIGHTNESS_STR,
										  new Task.SetBrightnessRange(document, getAnimationKinds(),
																	  startFrameSpinner.getIntValue(),
																	  numFramesSpinner.getIntValue()));
			mapKeyTableModel.setKeys(document.getBrightnessRangeKeys());
			updateComponents();
		}
		catch (AppException e)
		{
			JOptionPane.showMessageDialog(this, e, App.SHORT_NAME, JOptionPane.ERROR_MESSAGE);
		}
	}

	//------------------------------------------------------------------

	private void onDelete()
	{
		String[] optionStrs = Utils.getOptionStrings(DELETE_STR);
		if (JOptionPane.showOptionDialog(this, DELETE_PROMPT_STR, DELETE_BRIGHTNESS_RANGES_STR,
										 JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
										 optionStrs, optionStrs[1]) == JOptionPane.OK_OPTION)
		{
			for (int index : mapKeyTable.getSelectedRows())
				document.removeBrightnessRange(mapKeyTableModel.getAnimationKinds(index),
											   mapKeyTableModel.getStartFrame(index));
			mapKeyTableModel.setKeys(document.getBrightnessRangeKeys());
			updateComponents();
		}
	}

	//------------------------------------------------------------------

	private void onClose()
	{
		location = getLocation();
		animationKinds = getAnimationKinds();
		startFrame = startFrameSpinner.getIntValue();
		numFrames = numFramesSpinner.getIntValue();
		setVisible(false);
		dispose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Point								location;
	private static	Set<Pattern1Image.AnimationKind>	animationKinds	=
														EnumSet.noneOf(Pattern1Image.AnimationKind.class);
	private static	int									startFrame;
	private static	int									numFrames		= DEFAULT_NUM_FRAMES;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Pattern1Document	document;
	private	MapKeyTableModel	mapKeyTableModel;
	private	MapKeyTable			mapKeyTable;
	private	AnimationKindsPanel	animationKindsPanel;
	private	FIntegerSpinner		startFrameSpinner;
	private	FIntegerSpinner		numFramesSpinner;
	private	JButton				setButton;
	private	JButton				deleteButton;

}

//----------------------------------------------------------------------
