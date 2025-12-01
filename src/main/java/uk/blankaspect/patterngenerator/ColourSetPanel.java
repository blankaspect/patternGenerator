/*====================================================================*\

ColourSetPanel.java

Colour-set panel class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import uk.blankaspect.common.number.NumberUtils;

import uk.blankaspect.common.random.Prng01;

import uk.blankaspect.common.range.IntegerRange;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.colour.Colours;
import uk.blankaspect.ui.swing.colour.ColourUtils;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.workaround.LinuxWorkarounds;

//----------------------------------------------------------------------


// COLOUR-SET PANEL CLASS


class ColourSetPanel
	extends JPanel
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		NUM_COLUMNS	= 8;

	private static final	Insets	BUTTON_MARGINS	= new Insets(1, 4, 1, 4);

	private static final	String	SET_STR					= "Set";
	private static final	String	GRADUATE_STR			= "Graduate";
	private static final	String	RANDOMISE_STR			= "Randomise";
	private static final	String	CLEAR_SELECTION_STR		= "Clear selection";
	private static final	String	SELECTED_COLOURS_STR	= "Selected colours";
	private static final	String	GRADUATE_TITLE_STR		= "Graduate selected colours";
	private static final	String	RANDOMISE_TITLE_STR		= "Randomise selected colours";

	private static final	Color	COLOUR_BUTTON_BORDER_COLOUR				= Colours.LINE_BORDER;
	private static final	Color	COLOUR_BUTTON_DISABLED_BORDER_COLOUR	= new Color(200, 200, 200);
	private static final	Color	COLOUR_BUTTON_SELECTED_BORDER_COLOUR	= new Color(240, 160, 64);
	private static final	Color	COLOUR_BUTTON_FOCUSED_BORDER_COLOUR1	= Color.WHITE;
	private static final	Color	COLOUR_BUTTON_FOCUSED_BORDER_COLOUR2	= Color.BLACK;

	private static final	Stroke	DASH	= new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
															  10.0f, new float[] { 2.0f, 2.0f }, 0.5f);

	private enum ButtonState
	{
		NOT_PRESSED,
		PRESSED,
		ARMED
	}

	// Commands
	private interface Command
	{
		String	SELECT_ALL_IN_ROW		= "selectAllInRow.";
		String	SET						= "set";
		String	GRADUATE				= "graduate";
		String	RANDOMISE				= "randomise";
		String	CLEAR_SELECTION			= "clearSelection";
		String	MOVE_UP					= "moveUp";
		String	MOVE_DOWN				= "moveDown";
		String	MOVE_LEFT				= "moveLeft";
		String	MOVE_RIGHT				= "moveRight";
		String	MOVE_LEFT_MAX			= "moveLeftMax";
		String	MOVE_RIGHT_MAX			= "moveRightMax";

		String	CHOOSE_COLOUR			= "chooseColour";
		String	TOGGLE_COLOUR_SELECTION	= "toggleColourSelection";
	}

	private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
	{
		KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
						  Command.MOVE_UP),
		KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
						  Command.MOVE_DOWN),
		KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
						  Command.MOVE_LEFT),
		KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
						  Command.MOVE_RIGHT),
		KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
						  Command.MOVE_LEFT_MAX),
		KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
						  Command.MOVE_RIGHT_MAX)
	};

	private static final	KeyAction.KeyCommandPair[]	COLOUR_BUTTON_KEY_COMMANDS	=
	{
		KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
						  Command.CHOOSE_COLOUR),
		KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK),
						  Command.TOGGLE_COLOUR_SELECTION)
	};

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Prng01							prng			=
			new Prng01(PatternGeneratorApp.INSTANCE.getNextRandomSeed());
	private static	HueSaturationRangePanel.Params	graduateParams	= new HueSaturationRangePanel.Params();
	private static	HueSaturationRangePanel.Params	randomiseParams	= new HueSaturationRangePanel.Params();

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	int					numColours;
	private	Color				defaultColour;
	private	List<ColourButton>	colourButtons;
	private	JButton[]			rowSelectionButtons;
	private	JButton				setButton;
	private	JButton				graduateButton;
	private	JButton				randomiseButton;
	private	JButton				clearSelectionButton;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public ColourSetPanel(
		int			maxNumColours,
		List<Color>	colours,
		Color		defaultColour,
		Color		transparencyColour)
	{
		// Initialise instance variables
		this.defaultColour = defaultColour;
		numColours = colours.size();
		colourButtons = new ArrayList<>();


		//----  Colour-button panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel colourButtonPanel = new JPanel(new GridLayout(0, 1, 0, 0));
		int numRows = NumberUtils.roundUpQuotientInt(maxNumColours, NUM_COLUMNS);
		rowSelectionButtons = new JButton[numRows];
		for (int i = 0; i < numRows; i++)
		{
			JPanel rowPanel = new JPanel(gridBag);

			// Panel: row of colour buttons
			JPanel rowColourButtonPanel = new JPanel(new GridLayout(1, 0, 0, 0));
			for (int j = 0; j < NUM_COLUMNS; j++)
			{
				int index = colourButtons.size();
				if (index < maxNumColours)
				{
					ColourButton button = new ColourButton(index, (index < numColours) ? colours.get(index)
																					   : defaultColour);
					button.setEnabled(index < numColours);
					colourButtons.add(button);
					rowColourButtonPanel.add(button);
				}
				else
					rowColourButtonPanel.add(GuiUtils.spacer());
			}

			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 0, 0, 0);
			gridBag.setConstraints(rowColourButtonPanel, gbc);
			rowPanel.add(rowColourButtonPanel);

			// Button: row selection
			JButton rowSelectionButton = new RowSelectionButton();
			rowSelectionButtons[i] = rowSelectionButton;
			rowSelectionButton.setActionCommand(Command.SELECT_ALL_IN_ROW + i);
			rowSelectionButton.addActionListener(this);

			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 6, 0, 0);
			gridBag.setConstraints(rowSelectionButton, gbc);
			rowPanel.add(rowSelectionButton);

			colourButtonPanel.add(rowPanel);
		}

		updateTransparencyColour(transparencyColour);


		//----  Button panel

		JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 4));

		// Button: set
		setButton = new FButton(SET_STR + AppConstants.ELLIPSIS_STR);
		setButton.setMargin(BUTTON_MARGINS);
		setButton.setActionCommand(Command.SET);
		setButton.addActionListener(this);
		buttonPanel.add(setButton);

		// Button: graduate
		graduateButton = new FButton(GRADUATE_STR + AppConstants.ELLIPSIS_STR);
		graduateButton.setMargin(BUTTON_MARGINS);
		graduateButton.setActionCommand(Command.GRADUATE);
		graduateButton.addActionListener(this);
		buttonPanel.add(graduateButton);

		// Button: randomise
		randomiseButton = new FButton(RANDOMISE_STR + AppConstants.ELLIPSIS_STR);
		randomiseButton.setMargin(BUTTON_MARGINS);
		randomiseButton.setActionCommand(Command.RANDOMISE);
		randomiseButton.addActionListener(this);
		buttonPanel.add(randomiseButton);

		// Button: clear selection
		clearSelectionButton = new FButton(CLEAR_SELECTION_STR);
		clearSelectionButton.setMargin(BUTTON_MARGINS);
		clearSelectionButton.setActionCommand(Command.CLEAR_SELECTION);
		clearSelectionButton.addActionListener(this);
		buttonPanel.add(clearSelectionButton);


		//----  Outer panel

		setLayout(gridBag);

		int gridX = 0;

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(colourButtonPanel, gbc);
		add(colourButtonPanel);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 8, 0, 0);
		gridBag.setConstraints(buttonPanel, gbc);
		add(buttonPanel);

		// Add commands to action map
		KeyAction.create(this, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this, KEY_COMMANDS);

		// Update buttons
		updateButtons();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void actionPerformed(
		ActionEvent	event)
	{
		String command = event.getActionCommand();

		if (command.startsWith(Command.SELECT_ALL_IN_ROW))
			onSelectAllInRow(StringUtils.removePrefix(command, Command.SELECT_ALL_IN_ROW));
		else
		{
			switch (command)
			{
				case Command.SET             -> onSet();
				case Command.GRADUATE        -> onGraduate();
				case Command.RANDOMISE       -> onRandomise();
				case Command.CLEAR_SELECTION -> onClearSelection();
				case Command.MOVE_UP         -> onMoveUp();
				case Command.MOVE_DOWN       -> onMoveDown();
				case Command.MOVE_LEFT       -> onMoveLeft();
				case Command.MOVE_RIGHT      -> onMoveRight();
				case Command.MOVE_LEFT_MAX   -> onMoveLeftMax();
				case Command.MOVE_RIGHT_MAX  -> onMoveRightMax();
			}
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public Color getColour(
		int	index)
	{
		return (index < numColours) ? colourButtons.get(index).colour : null;
	}

	//------------------------------------------------------------------

	public void setNumColours(
		int	numColours)
	{
		if (this.numColours != numColours)
		{
			if (numColours < this.numColours)
			{
				int index = findFocusedColourButtonIndex();
				for (int i = numColours; i < this.numColours; i++)
				{
					ColourButton button = colourButtons.get(i);
					button.setSelected(false);
					button.setEnabled(false);
				}
				if (index >= numColours)
					colourButtons.get(numColours - 1).requestFocusInWindow();
			}
			else if (numColours > this.numColours)
			{
				for (int i = this.numColours; i < numColours; i++)
				{
					ColourButton button = colourButtons.get(i);
					button.setColour(defaultColour);
					button.setEnabled(true);
				}
			}
			this.numColours = numColours;
			updateButtons();
		}
	}

	//------------------------------------------------------------------

	public void updateTransparencyColour(
		Color	colour)
	{
		colourButtons.forEach(button -> button.setTransparencyColour(colour));
	}

	//------------------------------------------------------------------

	private void updateButtons()
	{
		int numRows = NumberUtils.roundUpQuotientInt(numColours, NUM_COLUMNS);
		for (int i = 0; i < rowSelectionButtons.length; i++)
			rowSelectionButtons[i].setEnabled(i < numRows);

		int numSelected = 0;
		for (int i = 0; i < numColours; i++)
		{
			if (colourButtons.get(i).selected)
				++numSelected;
		}
		setButton.setEnabled(numSelected > 0);
		graduateButton.setEnabled(numSelected > 1);
		randomiseButton.setEnabled(numSelected > 0);
		clearSelectionButton.setEnabled(numSelected > 0);
	}

	//------------------------------------------------------------------

	private int findFocusedColourButtonIndex()
	{
		for (int i = 0; i < numColours; i++)
		{
			if (colourButtons.get(i).isFocusOwner())
				return i;
		}
		return -1;
	}

	//------------------------------------------------------------------

	private void onSelectAllInRow(
		String	key)
	{
		int index = Integer.parseInt(key) * NUM_COLUMNS;
		for (int i = index; i < index + NUM_COLUMNS; i++)
		{
			ColourButton button = colourButtons.get(i);
			if (button.isEnabled())
				button.setSelected(true);
		}
		updateButtons();
	}

	//------------------------------------------------------------------

	private void onSet()
	{
		Color colour = defaultColour;
		for (int i = 0; i < numColours; i++)
		{
			ColourButton button = colourButtons.get(i);
			if (button.selected)
			{
				colour = button.colour;
				break;
			}
		}

		colour = JColorChooser.showDialog(this, SELECTED_COLOURS_STR, colour);
		if (colour != null)
		{
			for (int i = 0; i < numColours; i++)
			{
				ColourButton button = colourButtons.get(i);
				if (button.selected)
					button.setColour(colour);
			}
		}
	}

	//------------------------------------------------------------------

	private void onGraduate()
	{
		HueSaturationRangeDialog dialog = new HueSaturationRangeDialog(SwingUtilities.getWindowAncestor(this),
																	   GRADUATE_TITLE_STR, graduateParams);
		if (dialog.accepted)
		{
			HueSaturationRangePanel.Params params = dialog.hueSaturationRangePanel.getParams();
			graduateParams = params;
			int hue2 = params.hue2;
			if (hue2 < params.hue1)
				hue2 += HueSaturationRangePanel.HUE_RANGE;
			IntegerRange hueRange = new IntegerRange(params.hue1, hue2);
			IntegerRange saturationRange = new IntegerRange(params.saturation1, params.saturation2);
			IntegerRange opacityRange = new IntegerRange(params.opacity1, params.opacity2);

			List<ColourButton> buttons = new ArrayList<>();
			for (int i = 0; i < numColours; i++)
			{
				ColourButton button = colourButtons.get(i);
				if (button.selected)
					buttons.add(button);
			}

			for (int i = 0; i < buttons.size(); i++)
			{
				double fraction = (double)i / (double)(buttons.size() - 1);

				Color colour = HueSaturationRangePanel.hsbToColour(hueRange.getValue(fraction),
																   saturationRange.getValue(fraction),
																   params.brightness, opacityRange.getValue(fraction));
				buttons.get(i).setColour(colour);
			}
		}
	}

	//------------------------------------------------------------------

	private void onRandomise()
	{
		HueSaturationRangeDialog dialog = new HueSaturationRangeDialog(SwingUtilities.getWindowAncestor(this),
																	   RANDOMISE_TITLE_STR, randomiseParams);
		if (dialog.accepted)
		{
			HueSaturationRangePanel.Params params = dialog.hueSaturationRangePanel.getParams();
			randomiseParams = params;
			int hue2 = params.hue2;
			if (hue2 < params.hue1)
				hue2 += HueSaturationRangePanel.HUE_RANGE;
			IntegerRange hueRange = new IntegerRange(params.hue1, hue2);
			IntegerRange saturationRange = new IntegerRange(Math.min(params.saturation1, params.saturation2),
															Math.max(params.saturation1, params.saturation2));
			IntegerRange opacityRange = new IntegerRange(Math.min(params.opacity1, params.opacity2),
														 Math.max(params.opacity1, params.opacity2));
			for (int i = 0; i < numColours; i++)
			{
				ColourButton button = colourButtons.get(i);
				if (button.selected)
				{
					Color colour = HueSaturationRangePanel.hsbToColour(prng.nextInt(hueRange),
																	   prng.nextInt(saturationRange), params.brightness,
																	   prng.nextInt(opacityRange));
					button.setColour(colour);
				}
			}
		}
	}

	//------------------------------------------------------------------

	private void onClearSelection()
	{
		for (int i = 0; i < numColours; i++)
			colourButtons.get(i).setSelected(false);
		updateButtons();
	}

	//------------------------------------------------------------------

	private void onMoveUp()
	{
		int index = findFocusedColourButtonIndex();
		if (index >= NUM_COLUMNS)
			colourButtons.get(index - NUM_COLUMNS).requestFocusInWindow();
	}

	//------------------------------------------------------------------

	private void onMoveDown()
	{
		int index = findFocusedColourButtonIndex();
		if (index >= 0)
		{
			index += NUM_COLUMNS;
			if (index < numColours)
				colourButtons.get(index).requestFocusInWindow();
		}
	}

	//------------------------------------------------------------------

	private void onMoveLeft()
	{
		int index = findFocusedColourButtonIndex();
		if ((index > 0) && (index % NUM_COLUMNS > 0))
			colourButtons.get(--index).requestFocusInWindow();
	}

	//------------------------------------------------------------------

	private void onMoveRight()
	{
		int index = findFocusedColourButtonIndex();
		if ((index >= 0) && (index % NUM_COLUMNS < NUM_COLUMNS - 1))
		{
			if (++index < numColours)
				colourButtons.get(index).requestFocusInWindow();
		}
	}

	//------------------------------------------------------------------

	private void onMoveLeftMax()
	{
		int index = findFocusedColourButtonIndex();
		if (index > 0)
			colourButtons.get(index / NUM_COLUMNS * NUM_COLUMNS).requestFocusInWindow();
	}

	//------------------------------------------------------------------

	private void onMoveRightMax()
	{
		int index = findFocusedColourButtonIndex();
		if (index >= 0)
		{
			index = index / NUM_COLUMNS * NUM_COLUMNS + NUM_COLUMNS - 1;
			colourButtons.get(Math.min(index, numColours - 1)).requestFocusInWindow();
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// ROW SELECTION BUTTON CLASS


	private static class RowSelectionButton
		extends JButton
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	VERTICAL_MARGIN		= 3;
		private static final	int	HORIZONTAL_MARGIN	= 6;

		private static final	String	TOOLTIP_STR	= "Select all the colours in the row";

		private static final	Color	BORDER_COLOUR					= new Color(176, 192, 176);
		private static final	Color	FOCUSED_BORDER_COLOUR			= new Color(128, 128, 160);
		private static final	Color	BACKGROUND_COLOUR				= new Color(208, 224, 208);
		private static final	Color	HIGHLIGHTED_BACKGROUND_COLOUR	= Colours.FOCUSED_SELECTION_BACKGROUND;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private RowSelectionButton()
		{
			// Call superclass constructor
			super(Icons.DOUBLE_ANGLE_LEFT);

			// Set properties
			setBorder(null);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void setEnabled(
			boolean	enabled)
		{
			super.setEnabled(enabled);
			setToolTipText(enabled ? TOOLTIP_STR : null);
		}

		//--------------------------------------------------------------

		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension(2 * HORIZONTAL_MARGIN + getIcon().getIconWidth(),
								 2 * VERTICAL_MARGIN + getIcon().getIconHeight());
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(
			Graphics	gr)
		{
			// Fill background
			Rectangle rect = gr.getClipBounds();
			gr.setColor(isEnabled()
							? (isSelected() != getModel().isArmed())
									? HIGHLIGHTED_BACKGROUND_COLOUR
									: BACKGROUND_COLOUR
							: getBackground());
			gr.fillRect(rect.x, rect.y, rect.width, rect.height);

			// Draw icon
			if (isEnabled())
				getIcon().paintIcon(this, gr, HORIZONTAL_MARGIN, VERTICAL_MARGIN);

			// Draw border
			if (isFocusOwner())
			{
				gr.setColor(FOCUSED_BORDER_COLOUR);
				gr.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
			}
			else
				gr.setColor(BORDER_COLOUR);
			gr.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// HUE AND SATURATION RANGE DIALOG CLASS


	private static class HueSaturationRangeDialog
		extends JDialog
		implements ActionListener
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		// Commands
		private interface Command
		{
			String	ACCEPT	= "accept";
			String	CLOSE	= "close";
		}

	////////////////////////////////////////////////////////////////////
	//  Class variables
	////////////////////////////////////////////////////////////////////

		private static	Point	location;

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	boolean					accepted;
		private	HueSaturationRangePanel	hueSaturationRangePanel;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private HueSaturationRangeDialog(
			Window							owner,
			String							title,
			HueSaturationRangePanel.Params	params)
		{
			// Call superclass constructor
			super(owner, title, ModalityType.APPLICATION_MODAL);


			//----  Hue and saturation range panel

			hueSaturationRangePanel = new HueSaturationRangePanel(params);
			GuiUtils.setPaddedLineBorder(hueSaturationRangePanel, 2, 2, 6, 2);


			//----  Button panel

			JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 8, 0));
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

			// Button: OK
			JButton okButton = new FButton(AppConstants.OK_STR);
			okButton.setActionCommand(Command.ACCEPT);
			okButton.addActionListener(this);
			buttonPanel.add(okButton);

			// Button: cancel
			JButton cancelButton = new FButton(AppConstants.CANCEL_STR);
			cancelButton.setActionCommand(Command.CLOSE);
			cancelButton.addActionListener(this);
			buttonPanel.add(cancelButton);


			//----  Main panel

			GridBagLayout gridBag = new GridBagLayout();
			GridBagConstraints gbc = new GridBagConstraints();
			JPanel mainPanel = new JPanel(gridBag);
			mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

			int gridY = 0;

			gbc.gridx = 0;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 0, 0, 0);
			gridBag.setConstraints(hueSaturationRangePanel, gbc);
			mainPanel.add(hueSaturationRangePanel);

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
			KeyAction.create(mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this, KEY_COMMANDS);


			//----  Window

			// Set content pane
			setContentPane(mainPanel);

			// Dispose of window explicitly
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

			// Handle window events
			addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowOpened(
					WindowEvent	event)
				{
					// WORKAROUND for a bug that has been observed on Linux/GNOME whereby a window is displaced
					// downwards when its location is set.  The error in the y coordinate is the height of the title bar
					// of the window.  The workaround is to set the location of the window again with an adjustment for
					// the error.
					LinuxWorkarounds.fixWindowYCoord(event.getWindow(), location);
				}

				@Override
				public void windowClosing(
					WindowEvent	event)
				{
					onClose();
				}
			});

			// Prevent dialog from being resized
			setResizable(false);

			// Resize dialog to its preferred size
			pack();

			// Set location of dialog
			if (location == null)
				location = GuiUtils.getComponentLocation(this, owner);
			setLocation(location);

			// Set default button
			getRootPane().setDefaultButton(okButton);

			// Show dialog
			setVisible(true);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void actionPerformed(
			ActionEvent	event)
		{
			switch (event.getActionCommand())
			{
				case Command.ACCEPT -> onAccept();
				case Command.CLOSE  -> onClose();
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void onAccept()
		{
			accepted = true;
			onClose();
		}

		//--------------------------------------------------------------

		private void onClose()
		{
			location = getLocation();
			setVisible(false);
			dispose();
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// COLOUR BUTTON CLASS


	private class ColourButton
		extends JComponent
		implements ActionListener, FocusListener, MouseListener, MouseMotionListener
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int		WIDTH	= 32;
		private static final	int		HEIGHT	= 16;

		private static final	int		INNER_BORDER_WIDTH	= 1;
		private static final	int		OUTER_BORDER_WIDTH	= 2;

		private static final	String	COLOUR_STR	= "Colour ";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int			index;
		private	Color		colour;
		private	Color		transparencyColour;
		private	boolean		selected;
		private	ButtonState	buttonState;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ColourButton(
			int		index,
			Color	colour)
		{
			// Initialise instance variables
			this.index = index;
			this.colour = colour;
			buttonState = ButtonState.NOT_PRESSED;

			// Set properties
			setOpaque(true);
			setFocusable(true);

			// Add commands to action map
			KeyAction.create(this, JComponent.WHEN_FOCUSED, this, COLOUR_BUTTON_KEY_COMMANDS);

			// Add listeners
			addFocusListener(this);
			addMouseListener(this);
			addMouseMotionListener(this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void actionPerformed(
			ActionEvent	event)
		{
			switch (event.getActionCommand())
			{
				case Command.CHOOSE_COLOUR           -> onChooseColour();
				case Command.TOGGLE_COLOUR_SELECTION -> onToggleColourSelection();
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : FocusListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void focusGained(
			FocusEvent	event)
		{
			repaint();
		}

		//--------------------------------------------------------------

		@Override
		public void focusLost(
			FocusEvent	event)
		{
			repaint();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : MouseListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void mouseClicked(
			MouseEvent	event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		@Override
		public void mouseEntered(
			MouseEvent	event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		@Override
		public void mouseExited(
			MouseEvent	event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		@Override
		public void mousePressed(
			MouseEvent	event)
		{
			if (isEnabled() && SwingUtilities.isLeftMouseButton(event) && isWithinButton(event))
			{
				requestFocusInWindow();

				if (event.isControlDown())
					onToggleColourSelection();
				else
					setButtonState(ButtonState.ARMED);
			}
		}

		//--------------------------------------------------------------

		@Override
		public void mouseReleased(
			MouseEvent	event)
		{
			if (isEnabled() && SwingUtilities.isLeftMouseButton(event))
			{
				if ((buttonState == ButtonState.ARMED) && isWithinButton(event))
					onChooseColour();
				setButtonState(ButtonState.NOT_PRESSED);
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : MouseMotionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void mouseDragged(
			MouseEvent	event)
		{
			if (buttonState != ButtonState.NOT_PRESSED)
				setButtonState(isWithinButton(event) ? ButtonState.ARMED : ButtonState.PRESSED);
		}

		//--------------------------------------------------------------

		@Override
		public void mouseMoved(
			MouseEvent	event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void setEnabled(
			boolean	enabled)
		{
			super.setEnabled(enabled);
			setToolTipText(enabled ? COLOUR_STR + (index + 1) : null);
		}

		//--------------------------------------------------------------

		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension(2 * (OUTER_BORDER_WIDTH + INNER_BORDER_WIDTH) + WIDTH,
								 2 * (OUTER_BORDER_WIDTH + INNER_BORDER_WIDTH) + HEIGHT);
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(
			Graphics	gr)
		{
			// Create copy of graphics context
			Graphics2D gr2d = GuiUtils.copyGraphicsContext(gr);

			// Get dimensions
			int width = getWidth();
			int height = getHeight();

			// Fill background
			int x = OUTER_BORDER_WIDTH + INNER_BORDER_WIDTH;
			int y = OUTER_BORDER_WIDTH + INNER_BORDER_WIDTH;
			gr2d.setColor(isEnabled()
								? (buttonState == ButtonState.ARMED)
										? ColourUtils.invert(getForeground())
										: getForeground()
								: getBackground());
			gr2d.fillRect(x, y, width - 2 * x, height - 2 * y);

			// Draw cross if button is disabled
			if (!isEnabled())
			{
				// Set rendering hints
				RenderingHints renderingHints = gr2d.getRenderingHints();
				gr2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				gr2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				gr2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

				int x1 = OUTER_BORDER_WIDTH + INNER_BORDER_WIDTH;
				int x2 = width - x1 - 1;
				int y1 = OUTER_BORDER_WIDTH + INNER_BORDER_WIDTH;
				int y2 = height - y1 - 1;
				gr2d.setColor(COLOUR_BUTTON_DISABLED_BORDER_COLOUR);
				gr2d.drawLine(x1, y1, x2, y2);
				gr2d.drawLine(x2, y1, x1, y2);

				gr2d.setRenderingHints(renderingHints);
			}

			// Draw inner border
			int x1 = OUTER_BORDER_WIDTH;
			int x2 = width - 2 * x1 - 1;
			int y1 = OUTER_BORDER_WIDTH;
			int y2 = height - 2 * y1 - 1;
			if (isFocusOwner())
			{
				gr2d.setColor(COLOUR_BUTTON_FOCUSED_BORDER_COLOUR1);
				gr2d.drawRect(x1, y1, x2, y2);

				Stroke stroke = gr2d.getStroke();
				gr2d.setStroke(DASH);
				gr2d.setColor(COLOUR_BUTTON_FOCUSED_BORDER_COLOUR2);
				gr2d.drawRect(x1, y1, x2, y2);
				gr2d.setStroke(stroke);
			}
			else
			{
				gr2d.setColor(isEnabled() ? COLOUR_BUTTON_BORDER_COLOUR : COLOUR_BUTTON_DISABLED_BORDER_COLOUR);
				gr2d.drawRect(x1, y1, x2, y2);
			}

			// Draw outer border
			gr2d.setColor(selected ? COLOUR_BUTTON_SELECTED_BORDER_COLOUR : getBackground());
			gr2d.drawRect(0, 0, width - 1, height - 1);
			gr2d.drawRect(1, 1, width - 3, height - 3);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void setColour(
			Color	colour)
		{
			this.colour = colour;
			updateForeground();
		}

		//--------------------------------------------------------------

		private void setTransparencyColour(
			Color	colour)
		{
			transparencyColour = colour;
			updateForeground();
		}

		//--------------------------------------------------------------

		private void updateForeground()
		{
			setForeground(ColourUtils.blend(colour, transparencyColour));
		}

		//--------------------------------------------------------------

		private boolean isWithinButton(
			MouseEvent	event)
		{
			int x = event.getX();
			int x1 = OUTER_BORDER_WIDTH + INNER_BORDER_WIDTH;
			int x2 = getWidth() - x1;
			int y = event.getY();
			int y1 = OUTER_BORDER_WIDTH + INNER_BORDER_WIDTH;
			int y2 = getHeight() - y1;
			return (x >= x1) && (x < x2) && (y >= y1) && (y < y2);
		}

		//--------------------------------------------------------------

		private void setButtonState(
			ButtonState	state)
		{
			if (buttonState != state)
			{
				buttonState = state;
				repaint();
			}
		}

		//--------------------------------------------------------------

		private void setSelected(
			boolean	selected)
		{
			if (this.selected != selected)
			{
				this.selected = selected;
				repaint();
			}
		}

		//--------------------------------------------------------------

		private void onChooseColour()
		{
			Color colour = JColorChooser.showDialog(this, COLOUR_STR + (index + 1), this.colour);
			if (colour != null)
				setColour(colour);
		}

		//--------------------------------------------------------------

		private void onToggleColourSelection()
		{
			setSelected(!selected);
			updateButtons();
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
