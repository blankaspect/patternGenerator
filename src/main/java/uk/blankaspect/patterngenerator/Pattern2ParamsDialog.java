/*====================================================================*\

Pattern2ParamsDialog.java

Pattern 2 parameters dialog class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.observer.Observable;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.border.TitledBorder;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.checkbox.FCheckBox;

import uk.blankaspect.ui.swing.colour.ColourUtils;

import uk.blankaspect.ui.swing.combobox.FComboBox;

import uk.blankaspect.ui.swing.container.DimensionsSpinnerPanel;
import uk.blankaspect.ui.swing.container.DoubleSpinnerSliderPanel;

import uk.blankaspect.ui.swing.icon.ColourSampleIcon;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.range.IntegerRangeBarPanel;

import uk.blankaspect.ui.swing.slider.HorizontalSlider;

import uk.blankaspect.ui.swing.spinner.FIntegerSpinner;

import uk.blankaspect.ui.swing.text.TextRendering;

//----------------------------------------------------------------------


// PATTERN 2 PARAMETERS DIALOG CLASS


class Pattern2ParamsDialog
	extends JDialog
	implements ActionListener, ChangeListener, DocumentListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		WIDTH_FIELD_LENGTH					= 4;
	private static final	int		HEIGHT_FIELD_LENGTH					= 4;
	private static final	int		END_MARGIN_FIELD_LENGTH				= 2;
	private static final	int		SIDE_MARGIN_FIELD_LENGTH			= 1;
	private static final	int		GRID_INTERVAL_FIELD_LENGTH			= 6;
	private static final	int		PATH_THICKNESS_FIELD_LENGTH			= 5;
	private static final	int		EXPECTED_PATH_LENGTH_FIELD_LENGTH	= 2;
	private static final	int		TERMINAL_DIAMETER_FIELD_LENGTH		= 5;
	private static final	int		NUM_PATH_COLOURS_FIELD_LENGTH		= 2;
	private static final	int		ACTIVE_FRACTION_FIELD_LENGTH		= 3;
	private static final	int		DIRECTION_PROBABILITY_FIELD_LENGTH	= 3;

	private static final	int		SLIDER_KNOB_WIDTH	= 24;
	private static final	int		SLIDER_HEIGHT		= 18;

	private static final	double	DELTA_GRID_INTERVAL		= 0.01;
	private static final	double	DELTA_PATH_THICKNESS	= 0.01;
	private static final	double	DELTA_TERMINAL_DIAMETER	= 0.01;

	private static final	Insets	COLOUR_BUTTON_MARGINS	= new Insets(2, 2, 2, 2);

	private static final	Color	DIRECTION_TEXT_COLOUR	= new Color(192, 64, 0);

	private static final	String	TITLE_PREFIX	= PatternKind.PATTERN2.getName() + " parameters : ";

	private static final	String	NEW_PATTERN_STR					= "New pattern";
	private static final	String	SIZE_STR						= "Size";
	private static final	String	ORIENTATION_STR					= "Orientation";
	private static final	String	END_MARGIN_STR					= "End margin";
	private static final	String	SIDE_MARGIN_STR					= "Side margin";
	private static final	String	GRID_INTERVAL_STR				= "Grid interval";
	private static final	String	PATH_THICKNESS_STR				= "Path thickness";
	private static final	String	EXPECTED_PATH_LENGTH_STR		= "Expected path length";
	private static final	String	SHOW_EMPTY_PATHS_STR			= "Show empty paths";
	private static final	String	TERMINAL_EMPHASIS_STR			= "Terminal emphasis";
	private static final	String	TERMINAL_DIAMETER_STR			= "Terminal diameter";
	private static final	String	SEED_STR						= "Seed";
	private static final	String	DIRECTION_PROBABILITIES_STR		= "Direction probabilities";
	private static final	String	DIRECTION_MODE_STR				= "Direction mode";
	private static final	String	SYMMETRICAL_STR					= "L-R symmetrical";
	private static final	String	COLOURS_STR						= "Colours";
	private static final	String	TRANSPARENCY_COLOUR_STR			= "Transparency colour";
	private static final	String	BACKGROUND_COLOUR_STR			= "Background colour";
	private static final	String	NUM_PATH_COLOURS_STR			= "Number of path colours";
	private static final	String	TRANSPARENCY_COLOUR_TITLE_STR	= "Transparency colour";
	private static final	String	BACKGROUND_COLOUR_TITLE_STR		= "Background colour";
	private static final	String	ANIMATION_STR					= "Animation";
	private static final	String	ACTIVE_FRACTION_STR				= "Active fraction";
	private static final	String	TRANSITION_INTERVAL_STR			= "Transition interval";
	private static final	String	TO_STR							= "to";
	private static final	String	PER_CENT_STR					= "%";

	private static final	String	SPINNER_SLIDER_PANEL_KEY	= Pattern2ParamsDialog.class.getName();
	private static final	String	RANGE_BAR_PANEL_KEY			= SPINNER_SLIDER_PANEL_KEY;

	private static final	Pattern2Image.Direction[]	ORDERED_DIRECTIONS	=
	{
		Pattern2Image.Direction.FORE_LEFT,
		Pattern2Image.Direction.FORE,
		Pattern2Image.Direction.FORE_RIGHT,
		Pattern2Image.Direction.BACK_LEFT,
		Pattern2Image.Direction.BACK,
		Pattern2Image.Direction.BACK_RIGHT
	};

	// Commands
	private interface Command
	{
		String	SELECT_TERMINAL_EMPHASIS	= "selectTerminalEmphasis";
		String	TOGGLE_SYMMETRICAL			= "toggleSymmetrical";
		String	SELECT_DIRECTION_MODE		= "selectDirectionMode";
		String	CHOOSE_TRANSPARENCY_COLOUR	= "chooseTransparencyColour";
		String	CHOOSE_BACKGROUND_COLOUR	= "chooseBackgroundColour";
		String	ACCEPT						= "accept";
		String	CLOSE						= "close";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Point	location;
	private static	boolean	symmetrical;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	boolean											accepted;
	private	DimensionsSpinnerPanel							sizePanel;
	private	FComboBox<Pattern2Image.Orientation>			orientationComboBox;
	private	FIntegerSpinner									endMarginSpinner;
	private	FIntegerSpinner									sideMarginSpinner;
	private	SpinnerSliderPanel								gridIntervalSpinnerSlider;
	private	SpinnerSliderPanel								pathThicknessSpinnerSlider;
	private	FIntegerSpinner									expectedPathLengthSpinner;
	private	FCheckBox										showEmptyPathsCheckBox;
	private	FComboBox<Pattern2Image.TerminalEmphasis>		terminalEmphasisComboBox;
	private	SpinnerSliderPanel								terminalDiameterSpinnerSlider;
	private	SeedPanel										seedPanel;
	private	ColourButton									transparencyColourButton;
	private	ColourButton									backgroundColourButton;
	private	FIntegerSpinner									numPathColoursSpinner;
	private	ColourSetPanel									pathColourPanel;
	private	FIntegerSpinner									activeFractionSpinner;
	private	TransitionIntervalRangePanel					transitionIntervalRangePanel;
	private	FComboBox<Pattern2Image.Direction.Mode>			directionModeComboBox;
	private	DirectionProbabilityPanel						probabilityPanel;
	private	Map<Pattern2Image.Direction, FIntegerSpinner>	directionProbabilitySpinners;
	private	Map<Pattern2Image.Direction, ProbabilityBox>	normalisedProbabilityBoxes;
	private	FCheckBox										symmetricalCheckBox;
	private	FButton											okButton;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private Pattern2ParamsDialog(Window         owner,
								 String         title,
								 Pattern2Params params)
	{
		// Call superclass constructor
		super(owner, TITLE_PREFIX + ((title == null) ? NEW_PATTERN_STR : title),
			  ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());

		// Initialise spinner-slider panels
		DoubleSpinnerSliderPanel.removeInstances(SPINNER_SLIDER_PANEL_KEY);


		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		// Label: size
		JLabel sizeLabel = new FLabel(SIZE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(sizeLabel, gbc);
		controlPanel.add(sizeLabel);

		// Panel: size
		sizePanel = new DimensionsSpinnerPanel(params.getWidth(), Pattern2Params.MIN_WIDTH, Pattern2Params.MAX_WIDTH,
											   WIDTH_FIELD_LENGTH, params.getHeight(), Pattern2Params.MIN_HEIGHT,
											   Pattern2Params.MAX_HEIGHT, HEIGHT_FIELD_LENGTH, null);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(sizePanel, gbc);
		controlPanel.add(sizePanel);

		// Label: orientation
		JLabel orientationLabel = new FLabel(ORIENTATION_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(orientationLabel, gbc);
		controlPanel.add(orientationLabel);

		// Combo box: orientation
		orientationComboBox = new FComboBox<>(Pattern2Image.Orientation.values());
		orientationComboBox.setSelectedValue(params.getOrientation());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(orientationComboBox, gbc);
		controlPanel.add(orientationComboBox);

		// Label: end margin
		JLabel endMarginLabel = new FLabel(END_MARGIN_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(endMarginLabel, gbc);
		controlPanel.add(endMarginLabel);

		// Spinner: end margin
		endMarginSpinner = new FIntegerSpinner(params.getEndMargin(), Pattern2Params.MIN_END_MARGIN,
											   Pattern2Params.MAX_END_MARGIN, END_MARGIN_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(endMarginSpinner, gbc);
		controlPanel.add(endMarginSpinner);

		// Label: side margin
		JLabel sideMarginLabel = new FLabel(SIDE_MARGIN_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(sideMarginLabel, gbc);
		controlPanel.add(sideMarginLabel);

		// Spinner: side margin
		sideMarginSpinner = new FIntegerSpinner(params.getSideMargin(), Pattern2Params.MIN_SIDE_MARGIN,
												Pattern2Params.MAX_SIDE_MARGIN, SIDE_MARGIN_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(sideMarginSpinner, gbc);
		controlPanel.add(sideMarginSpinner);

		// Label: grid interval
		JLabel gridIntervalLabel = new FLabel(GRID_INTERVAL_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(gridIntervalLabel, gbc);
		controlPanel.add(gridIntervalLabel);

		// Spinner-slider panel: grid interval
		int sliderExtent = (int)Math.round(Pattern2Params.MAX_GRID_INTERVAL - Pattern2Params.MIN_GRID_INTERVAL);
		int sliderWidth = HorizontalSlider.extentToWidth(sliderExtent, SLIDER_KNOB_WIDTH);

		gridIntervalSpinnerSlider = new SpinnerSliderPanel(params.getGridInterval(), Pattern2Params.MIN_GRID_INTERVAL,
														   Pattern2Params.MAX_GRID_INTERVAL, DELTA_GRID_INTERVAL,
														   GRID_INTERVAL_FIELD_LENGTH, AppConstants.FORMAT_1_2,
														   sliderWidth, Pattern2Params.DEFAULT_GRID_INTERVAL);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(gridIntervalSpinnerSlider, gbc);
		controlPanel.add(gridIntervalSpinnerSlider);

		// Label: path thickness
		JLabel pathThicknessLabel = new FLabel(PATH_THICKNESS_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(pathThicknessLabel, gbc);
		controlPanel.add(pathThicknessLabel);

		// Spinner-slider panel: path thickness
		sliderExtent = (int)Math.round(Pattern2Params.MAX_PATH_THICKNESS - Pattern2Params.MIN_PATH_THICKNESS);
		sliderWidth = HorizontalSlider.extentToWidth(2 * sliderExtent, SLIDER_KNOB_WIDTH);

		pathThicknessSpinnerSlider = new SpinnerSliderPanel(params.getPathThickness(),
															Pattern2Params.MIN_PATH_THICKNESS,
															Pattern2Params.MAX_PATH_THICKNESS, DELTA_PATH_THICKNESS,
															PATH_THICKNESS_FIELD_LENGTH, AppConstants.FORMAT_1_2,
															sliderWidth, Pattern2Params.DEFAULT_PATH_THICKNESS);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(pathThicknessSpinnerSlider, gbc);
		controlPanel.add(pathThicknessSpinnerSlider);

		// Label: expected path length
		JLabel expectedPathLengthLabel = new FLabel(EXPECTED_PATH_LENGTH_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(expectedPathLengthLabel, gbc);
		controlPanel.add(expectedPathLengthLabel);

		// Spinner: expected path length
		expectedPathLengthSpinner = new FIntegerSpinner(params.getExpectedPathLength(),
														Pattern2Params.MIN_EXPECTED_PATH_LENGTH,
														Pattern2Params.MAX_EXPECTED_PATH_LENGTH,
														EXPECTED_PATH_LENGTH_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(expectedPathLengthSpinner, gbc);
		controlPanel.add(expectedPathLengthSpinner);

		// Check box: show empty paths
		showEmptyPathsCheckBox = new FCheckBox(SHOW_EMPTY_PATHS_STR);
		showEmptyPathsCheckBox.setSelected(params.isShowEmptyPaths());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(showEmptyPathsCheckBox, gbc);
		controlPanel.add(showEmptyPathsCheckBox);

		// Label: terminal emphasis
		JLabel terminalEmphasisLabel = new FLabel(TERMINAL_EMPHASIS_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(terminalEmphasisLabel, gbc);
		controlPanel.add(terminalEmphasisLabel);

		// Combo box: terminal emphasis
		terminalEmphasisComboBox = new FComboBox<>(Pattern2Image.TerminalEmphasis.values());
		terminalEmphasisComboBox.setSelectedValue(params.getTerminalEmphasis());
		terminalEmphasisComboBox.setActionCommand(Command.SELECT_TERMINAL_EMPHASIS);
		terminalEmphasisComboBox.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(terminalEmphasisComboBox, gbc);
		controlPanel.add(terminalEmphasisComboBox);

		// Label: terminal diameter
		JLabel terminalDiameterLabel = new FLabel(TERMINAL_DIAMETER_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(terminalDiameterLabel, gbc);
		controlPanel.add(terminalDiameterLabel);

		// Spinner-slider panel: terminal diameter
		sliderExtent = (int)Math.round(Pattern2Params.MAX_TERMINAL_DIAMETER - Pattern2Params.MIN_TERMINAL_DIAMETER);
		sliderWidth = HorizontalSlider.extentToWidth(sliderExtent, SLIDER_KNOB_WIDTH);

		terminalDiameterSpinnerSlider = new SpinnerSliderPanel(params.getTerminalDiameter(),
															   Pattern2Params.MIN_TERMINAL_DIAMETER,
															   Pattern2Params.MAX_TERMINAL_DIAMETER,
															   DELTA_TERMINAL_DIAMETER, TERMINAL_DIAMETER_FIELD_LENGTH,
															   AppConstants.FORMAT_1_2, sliderWidth,
															   Pattern2Params.DEFAULT_TERMINAL_DIAMETER);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(terminalDiameterSpinnerSlider, gbc);
		controlPanel.add(terminalDiameterSpinnerSlider);

		// Label: seed
		JLabel seedLabel = new FLabel(SEED_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(seedLabel, gbc);
		controlPanel.add(seedLabel);

		// Panel: seed
		seedPanel = new SeedPanel(params.getSeed());
		seedPanel.getField().getDocument().addDocumentListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(seedPanel, gbc);
		controlPanel.add(seedPanel);

		// Align spinner-slider panels
		DoubleSpinnerSliderPanel.align(SPINNER_SLIDER_PANEL_KEY);


		//----  Colours panel

		JPanel coloursPanel = new JPanel(gridBag);
		TitledBorder.setPaddedBorder(coloursPanel, COLOURS_STR);

		// Panel: colours, top
		JPanel coloursTopPanel = new JPanel(gridBag);

		gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(-16, 0, 0, 0);
		gridBag.setConstraints(coloursTopPanel, gbc);
		coloursPanel.add(coloursTopPanel);

		// Label: transparency colour
		JLabel transparencyColourLabel = new FLabel(TRANSPARENCY_COLOUR_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(transparencyColourLabel, gbc);
		coloursTopPanel.add(transparencyColourLabel);

		// Button: transparency colour
		transparencyColourButton = new ColourButton(params.getTransparencyColour(), false);
		transparencyColourButton.setActionCommand(Command.CHOOSE_TRANSPARENCY_COLOUR);
		transparencyColourButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(transparencyColourButton, gbc);
		coloursTopPanel.add(transparencyColourButton);

		// Label: background colour
		JLabel backgroundColourLabel = new FLabel(BACKGROUND_COLOUR_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(backgroundColourLabel, gbc);
		coloursTopPanel.add(backgroundColourLabel);

		// Button: background colour
		backgroundColourButton = new ColourButton(params.getBackgroundColour(), true);
		backgroundColourButton.setActionCommand(Command.CHOOSE_BACKGROUND_COLOUR);
		backgroundColourButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(backgroundColourButton, gbc);
		coloursTopPanel.add(backgroundColourButton);

		// Label: number of path colours
		JLabel numPathColoursLabel = new FLabel(NUM_PATH_COLOURS_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(numPathColoursLabel, gbc);
		coloursTopPanel.add(numPathColoursLabel);

		// Spinner: number of path colours
		List<Color> colours = params.getPathColours();
		numPathColoursSpinner = new FIntegerSpinner(colours.size(), Pattern2Params.MIN_NUM_PATH_COLOURS,
													Pattern2Params.MAX_NUM_PATH_COLOURS, NUM_PATH_COLOURS_FIELD_LENGTH);
		numPathColoursSpinner.addChangeListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(numPathColoursSpinner, gbc);
		coloursTopPanel.add(numPathColoursSpinner);

		// Panel: path colours
		pathColourPanel = new ColourSetPanel(Pattern2Params.MAX_NUM_PATH_COLOURS, colours,
											 Pattern2Params.DEFAULT_PATH_COLOUR, transparencyColourButton.getColour());

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(4, 0, 2, 0);
		gridBag.setConstraints(pathColourPanel, gbc);
		coloursPanel.add(pathColourPanel);

		// Update transparency colour
		transparencyColourButton.colour.addObserver((observable, oldColour, colour) ->
																	backgroundColourButton.updateForeground());
		transparencyColourButton.colour.addObserver((observable, oldColour, colour) ->
																	pathColourPanel.updateTransparencyColour(colour));


		//----  Animation panel

		JPanel animationPanel = new JPanel(gridBag);
		TitledBorder.setPaddedBorder(animationPanel, ANIMATION_STR);

		// Label: active fraction
		JLabel activeFractionLabel = new FLabel(ACTIVE_FRACTION_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(activeFractionLabel, gbc);
		animationPanel.add(activeFractionLabel);

		// Panel: active fraction
		JPanel activeFractionPanel = new JPanel(gridBag);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(activeFractionPanel, gbc);
		animationPanel.add(activeFractionPanel);

		int gridX = 0;

		// Spinner: active fraction
		activeFractionSpinner = new FIntegerSpinner(params.getActiveFraction(), Pattern2Image.MIN_ACTIVE_FRACTION,
													Pattern2Image.MAX_ACTIVE_FRACTION, ACTIVE_FRACTION_FIELD_LENGTH);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(activeFractionSpinner, gbc);
		activeFractionPanel.add(activeFractionSpinner);

		// Label: per cent
		JLabel perCentLabel = new FLabel(PER_CENT_STR);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 4, 0, 0);
		gridBag.setConstraints(perCentLabel, gbc);
		activeFractionPanel.add(perCentLabel);

		// Label: transition-interval range
		JLabel transitionIntervalLabel = new FLabel(TRANSITION_INTERVAL_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(transitionIntervalLabel, gbc);
		animationPanel.add(transitionIntervalLabel);

		// Panel: transition-interval range
		transitionIntervalRangePanel = new TransitionIntervalRangePanel();
		transitionIntervalRangePanel.setRange(params.getTransitionIntervalRange());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(transitionIntervalRangePanel, gbc);
		animationPanel.add(transitionIntervalRangePanel);


		//----  Left panel

		JPanel leftPanel = new JPanel(gridBag);

		gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		leftPanel.add(controlPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(coloursPanel, gbc);
		leftPanel.add(coloursPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(animationPanel, gbc);
		leftPanel.add(animationPanel);


		//----  Direction probabilities panel

		JPanel directionProbabilitiesPanel = new JPanel(gridBag);
		TitledBorder.setPaddedBorder(directionProbabilitiesPanel, DIRECTION_PROBABILITIES_STR);

		gridY = 0;

		// Panel: direction mode
		JPanel directionModePanel = new JPanel(gridBag);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(directionModePanel, gbc);
		directionProbabilitiesPanel.add(directionModePanel);

		// Label: direction mode
		JLabel directionModeLabel = new FLabel(DIRECTION_MODE_STR);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(directionModeLabel, gbc);
		directionModePanel.add(directionModeLabel);

		// Combo box: direction mode
		directionModeComboBox = new FComboBox<>(Pattern2Image.Direction.Mode.values());
		directionModeComboBox.setSelectedValue(params.getDirectionMode());
		directionModeComboBox.setActionCommand(Command.SELECT_DIRECTION_MODE);
		directionModeComboBox.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(directionModeComboBox, gbc);
		directionModePanel.add(directionModeComboBox);

		// Probability panel
		probabilityPanel = new DirectionProbabilityPanel(this, params.getDirectionProbabilities(),
														 params.getDirectionMode(), symmetrical);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(6, 0, 0, 0);
		gridBag.setConstraints(probabilityPanel, gbc);
		directionProbabilitiesPanel.add(probabilityPanel);

		// Panel: probability spinners
		JPanel probabilitySpinnerPanel = new JPanel(new GridLayout(0, 3, 8, 4));

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 0, 0, 0);
		gridBag.setConstraints(probabilitySpinnerPanel, gbc);
		directionProbabilitiesPanel.add(probabilitySpinnerPanel);

		// Spinners: direction probability
		directionProbabilitySpinners = new EnumMap<>(Pattern2Image.Direction.class);
		normalisedProbabilityBoxes = new EnumMap<>(Pattern2Image.Direction.class);
		for (Pattern2Image.Direction direction : ORDERED_DIRECTIONS)
		{
			// Panel: probability
			JPanel panel = new JPanel(gridBag);
			probabilitySpinnerPanel.add(panel);

			int y = 0;

			// Label: direction
			JLabel directionLabel = new FLabel(direction.getShortText());
			directionLabel.setForeground(DIRECTION_TEXT_COLOUR);

			gbc.gridx = 0;
			gbc.gridy = y++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 6, 0, 0);
			gridBag.setConstraints(directionLabel, gbc);
			panel.add(directionLabel);

			// Spinner: probability
			Integer prob = params.getDirectionProbability(direction);
			if (prob == null)
				prob = 0;
			FIntegerSpinner spinner = new FIntegerSpinner(prob, Pattern2Params.MIN_DIRECTION_PROBABILITY,
														  Pattern2Params.MAX_DIRECTION_PROBABILITY,
														  DIRECTION_PROBABILITY_FIELD_LENGTH);
			spinner.addChangeListener(this);
			directionProbabilitySpinners.put(direction, spinner);

			gbc.gridx = 0;
			gbc.gridy = y++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(0, 0, 0, 0);
			gridBag.setConstraints(spinner, gbc);
			panel.add(spinner);

			// Box: normalised probability
			ProbabilityBox probabilityBox = new ProbabilityBox();
			normalisedProbabilityBoxes.put(direction, probabilityBox);

			gbc.gridx = 0;
			gbc.gridy = y++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(0, 0, 0, 0);
			gridBag.setConstraints(probabilityBox, gbc);
			panel.add(probabilityBox);
		}

		// Check box: symmetrical
		symmetricalCheckBox = new FCheckBox(SYMMETRICAL_STR);
		symmetricalCheckBox.setSelected(symmetrical);
		symmetricalCheckBox.setActionCommand(Command.TOGGLE_SYMMETRICAL);
		symmetricalCheckBox.addActionListener(this);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(4, 3, 4, 3);
		gridBag.setConstraints(symmetricalCheckBox, gbc);
		directionProbabilitiesPanel.add(symmetricalCheckBox);


		//----  Button panel

		JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 8, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

		// Button: OK
		okButton = new FButton(AppConstants.OK_STR);
		okButton.setActionCommand(Command.ACCEPT);
		okButton.addActionListener(this);
		buttonPanel.add(okButton);

		// Button: cancel
		FButton cancelButton = new FButton(AppConstants.CANCEL_STR);
		cancelButton.setActionCommand(Command.CLOSE);
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);


		//----  Main panel

		JPanel mainPanel = new JPanel(gridBag);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(leftPanel, gbc);
		mainPanel.add(leftPanel);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.insets = new Insets(0, 3, 0, 0);
		gridBag.setConstraints(directionProbabilitiesPanel, gbc);
		mainPanel.add(directionProbabilitiesPanel);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
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

		// Set location of dialog
		if (location == null)
			location = GuiUtils.getComponentLocation(this, owner);
		setLocation(location);

		// Set default button
		getRootPane().setDefaultButton(okButton);

		// Show dialog
		setVisible(true);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Pattern2Params showDialog(Component      parent,
											String         title,
											Pattern2Params params)
	{
		return new Pattern2ParamsDialog(GuiUtils.getWindow(parent), title, params).getParams();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.SELECT_TERMINAL_EMPHASIS))
			onSelectTerminalEmphasis();

		else if (command.equals(Command.TOGGLE_SYMMETRICAL))
			onToggleSymmetrical();

		else if (command.equals(Command.SELECT_DIRECTION_MODE))
			onSelectDirectionProbabilityMode();

		else if (command.equals(Command.CHOOSE_TRANSPARENCY_COLOUR))
			onChooseTransparencyColour();

		else if (command.equals(Command.CHOOSE_BACKGROUND_COLOUR))
			onChooseBackgroundColour();

		else if (command.equals(Command.ACCEPT))
			onAccept();

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

		if (eventSource == numPathColoursSpinner)
			pathColourPanel.setNumColours(numPathColoursSpinner.getIntValue());

		else
		{
			for (Pattern2Image.Direction direction : directionProbabilitySpinners.keySet())
			{
				FIntegerSpinner spinner = directionProbabilitySpinners.get(direction);
				if (eventSource == spinner)
				{
					int value = spinner.getIntValue();
					updateNormalisedProbabilities();
					probabilityPanel.setProbability(direction, value);
					if (symmetricalCheckBox.isSelected())
					{
						Pattern2Image.Direction dir = direction.getReflection();
						if (dir != null)
						{
							spinner = directionProbabilitySpinners.get(dir);
							if (spinner.isEnabled() && (spinner.getIntValue() != value))
								spinner.setIntValue(value);
						}
					}
					break;
				}
			}
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : DocumentListener interface
////////////////////////////////////////////////////////////////////////

	public void changedUpdate(DocumentEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	public void insertUpdate(DocumentEvent event)
	{
		updateAcceptButton();
	}

	//------------------------------------------------------------------

	public void removeUpdate(DocumentEvent event)
	{
		updateAcceptButton();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private Pattern2Params getParams()
	{
		Pattern2Params params = null;
		if (accepted)
		{
			List<Color> pathColours = new ArrayList<>();
			int numColours = numPathColoursSpinner.getIntValue();
			for (int i = 0; i < numColours; i++)
				pathColours.add(pathColourPanel.getColour(i));

			params = new Pattern2Params(
				sizePanel.getValue1(),
				sizePanel.getValue2(),
				orientationComboBox.getSelectedValue(),
				endMarginSpinner.getIntValue(),
				sideMarginSpinner.getIntValue(),
				gridIntervalSpinnerSlider.getValue(),
				pathThicknessSpinnerSlider.getValue(),
				terminalDiameterSpinnerSlider.getValue(),
				expectedPathLengthSpinner.getIntValue(),
				getDirectionProbabilities(),
				getDirectionMode(),
				terminalEmphasisComboBox.getSelectedValue(),
				showEmptyPathsCheckBox.isSelected(),
				transparencyColourButton.getColour(),
				backgroundColourButton.getColour(),
				pathColours,
				activeFractionSpinner.getIntValue(),
				transitionIntervalRangePanel.getRange(),
				seedPanel.getSeed()
			);
		}
		return params;
	}

	//------------------------------------------------------------------

	public void setProbability(Pattern2Image.Direction direction,
							   int                     value)
	{
		directionProbabilitySpinners.get(direction).setIntValue(value);
	}

	//------------------------------------------------------------------

	private Map<Pattern2Image.Direction, Integer> getDirectionProbabilities()
	{
		Map<Pattern2Image.Direction, Integer> probabilities = new EnumMap<>(Pattern2Image.Direction.class);
		for (Pattern2Image.Direction direction : directionProbabilitySpinners.keySet())
		{
			FIntegerSpinner spinner = directionProbabilitySpinners.get(direction);
			if (spinner.isEnabled())
			{
				int prob = spinner.getIntValue();
				if (prob != 0)
					probabilities.put(direction, prob);
			}
		}
		return probabilities;
	}

	//------------------------------------------------------------------

	private Pattern2Image.Direction.Mode getDirectionMode()
	{
		return directionModeComboBox.getSelectedValue();
	}

	//------------------------------------------------------------------

	private void updateTerminalDiameter()
	{
		GuiUtils.setAllEnabled(terminalDiameterSpinnerSlider,
							   terminalEmphasisComboBox.getSelectedValue() != Pattern2Image.TerminalEmphasis.NONE);
	}

	//------------------------------------------------------------------

	private void updateDirectionProbabilitySpinners()
	{
		directionProbabilitySpinners.get(Pattern2Image.Direction.BACK).
			setEnabled(directionModeComboBox.getSelectedValue() == Pattern2Image.Direction.Mode.ABSOLUTE);
	}

	//------------------------------------------------------------------

	private void updateNormalisedProbabilities()
	{
		int totalProb = 0;
		for (FIntegerSpinner spinner : directionProbabilitySpinners.values())
		{
			if (spinner.isEnabled())
				totalProb += spinner.getIntValue();
		}

		double probFactor = (totalProb == 0) ? 0.0 : 1.0 / (double)totalProb;
		for (Pattern2Image.Direction direction : Pattern2Image.Direction.values())
		{
			FIntegerSpinner spinner = directionProbabilitySpinners.get(direction);
			double prob = spinner.isEnabled() ? (double)spinner.getIntValue() : 0.0;
			prob *= probFactor;
			normalisedProbabilityBoxes.get(direction)
					.setText((prob == 0.0) ? null : AppConstants.FORMAT_1_3.format(prob));
		}
	}

	//------------------------------------------------------------------

	private void updateAcceptButton()
	{
		okButton.setEnabled(!seedPanel.getField().isEmpty());
	}

	//------------------------------------------------------------------

	private void updateComponents()
	{
		updateTerminalDiameter();
		updateDirectionProbabilitySpinners();
		updateNormalisedProbabilities();
		updateAcceptButton();
	}

	//------------------------------------------------------------------

	private void onSelectTerminalEmphasis()
	{
		updateTerminalDiameter();
	}

	//------------------------------------------------------------------

	private void onToggleSymmetrical()
	{
		probabilityPanel.setSymmetrical(symmetricalCheckBox.isSelected());
	}

	//------------------------------------------------------------------

	private void onSelectDirectionProbabilityMode()
	{
		updateDirectionProbabilitySpinners();
		probabilityPanel.setDirectionMode(getDirectionMode());
	}

	//------------------------------------------------------------------

	private void onChooseTransparencyColour()
	{
		Color colour = JColorChooser.showDialog(this, TRANSPARENCY_COLOUR_TITLE_STR,
												transparencyColourButton.getColour());
		if (colour != null)
			transparencyColourButton.setColour(colour);
	}

	//------------------------------------------------------------------

	private void onChooseBackgroundColour()
	{
		Color colour = JColorChooser.showDialog(this, BACKGROUND_COLOUR_TITLE_STR,
												backgroundColourButton.getColour());
		if (colour != null)
			backgroundColourButton.setColour(colour);
	}

	//------------------------------------------------------------------

	private void onAccept()
	{
		try
		{
			if (getDirectionProbabilities().isEmpty())
				throw new AppException(ErrorId.NO_DIRECTION_PROBABILITIES);
			accepted = true;
			symmetrical = symmetricalCheckBox.isSelected();
			onClose();
		}
		catch (AppException e)
		{
			JOptionPane.showMessageDialog(this, e, PatternGeneratorApp.SHORT_NAME, JOptionPane.ERROR_MESSAGE);
		}
	}

	//------------------------------------------------------------------

	private void onClose()
	{
		location = getLocation();
		setVisible(false);
		dispose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		NO_DIRECTION_PROBABILITIES
		("No direction probabilities have been set.");

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	message;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ErrorId(String message)
		{
			this.message = message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : AppException.IId interface
	////////////////////////////////////////////////////////////////////

		public String getMessage()
		{
			return message;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// PROBABILITY BOX CLASS


	private static class ProbabilityBox
		extends JComponent
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	HORIZONTAL_MARGIN	= 5;
		private static final	int	VERTICAL_MARGIN		= 2;

		private static final	String	PROTOTYPE_TEXT	= "0.000";

		private static final	Color	BACKGROUND_COLOUR	= new Color(248, 240, 216);
		private static final	Color	TEXT_COLOUR			= Color.BLACK;
		private static final	Color	BORDER_COLOUR		= new Color(216, 208, 184);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int		width;
		private	int		height;
		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ProbabilityBox()
		{
			AppFont.TEXT_FIELD.apply(this);
			FontMetrics fontMetrics = getFontMetrics(getFont());
			width = 2 * HORIZONTAL_MARGIN + fontMetrics.stringWidth(PROTOTYPE_TEXT);
			height = 2 * VERTICAL_MARGIN + fontMetrics.getHeight() - 1;
			setOpaque(true);
			setFocusable(false);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension(width, height);
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Create copy of graphics context
			gr = gr.create();

			// Fill background
			Rectangle rect = gr.getClipBounds();
			gr.setColor(BACKGROUND_COLOUR);
			gr.fillRect(rect.x, rect.y, rect.width, rect.height);

			// Draw text
			if (text != null)
			{
				// Set rendering hints for text antialiasing and fractional metrics
				TextRendering.setHints((Graphics2D)gr);

				// Draw text
				gr.setColor(TEXT_COLOUR);
				gr.drawString(text, HORIZONTAL_MARGIN,
							  VERTICAL_MARGIN - 1 + gr.getFontMetrics().getAscent());
			}

			// Draw border
			gr.setColor(BORDER_COLOUR);
			int x1 = 0;
			int x2 = getWidth() - 1;
			int y1 = 0;
			int y2 = getHeight() - 1;
			gr.drawLine(x1, y1, x1, y2);
			gr.drawLine(x2, y1, x2, y2);
			gr.drawLine(x1, y2, x2, y2);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void setText(String text)
		{
			if ((text == null) ? (this.text != null) : !text.equals(this.text))
			{
				this.text = text;
				repaint();
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// SPINNER-SLIDER PANEL CLASS


	private static class SpinnerSliderPanel
		extends DoubleSpinnerSliderPanel
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private SpinnerSliderPanel(double       value,
								   double       minValue,
								   double       maxValue,
								   double       deltaValue,
								   int          fieldLength,
								   NumberFormat format,
								   int          sliderWidth,
								   double       defaultValue)
		{
			super(value, minValue, maxValue, deltaValue, fieldLength, format, false, sliderWidth, SLIDER_HEIGHT,
				  SLIDER_KNOB_WIDTH, defaultValue, SPINNER_SLIDER_PANEL_KEY);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// TRANSITION-INTERVAL RANGE PANEL CLASS


	private static class TransitionIntervalRangePanel
		extends IntegerRangeBarPanel.Horizontal
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	OFFSET	= Pattern2Image.MIN_TRANSITION_INTERVAL;
		private static final	int	FACTOR	= Pattern2Image.MAX_TRANSITION_INTERVAL - OFFSET;

		private static final	int	BAR_EXTENT	= 200;

	////////////////////////////////////////////////////////////////////
	//  Member classes : non-inner classes
	////////////////////////////////////////////////////////////////////


		// SPINNER CLASS


		private static class Spinner
			extends FIntegerSpinner
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			private static final	int	FIELD_LENGTH	= 3;

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Spinner()
			{
				super(Pattern2Image.MIN_TRANSITION_INTERVAL, Pattern2Image.MIN_TRANSITION_INTERVAL,
					  Pattern2Image.MAX_TRANSITION_INTERVAL, FIELD_LENGTH, true);
			}

			//----------------------------------------------------------

		}

		//==============================================================

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private TransitionIntervalRangePanel()
		{
			super(TO_STR, new Spinner(), new Spinner(), BAR_EXTENT, RANGE_BAR_PANEL_KEY);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public double normaliseValue(int value)
		{
			return (double)(value - OFFSET) / (double)FACTOR;
		}

		//--------------------------------------------------------------

		@Override
		public int denormaliseValue(double value)
		{
			return (int)Math.round(value * (double)FACTOR) + OFFSET;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// COLOUR BUTTON CLASS


	private class ColourButton
		extends JButton
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	ICON_WIDTH	= 40;
		private static final	int	ICON_HEIGHT	= 16;

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Observable<Color>	colour;
		private	boolean				blend;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ColourButton(Color   colour,
							 boolean blend)
		{
			// Call superclass constructor
			super(new ColourSampleIcon(ICON_WIDTH, ICON_HEIGHT));

			// Initialise instance variables
			this.colour = new Observable.Equality<>();
			this.blend = blend;

			// Set properties
			setMargin(COLOUR_BUTTON_MARGINS);
			setColour(colour);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private Color getColour()
		{
			return blend ? colour.get() : ColourUtils.opaque(colour.get());
		}

		//--------------------------------------------------------------

		private void setColour(Color colour)
		{
			this.colour.set(colour);
			updateForeground();
		}

		//--------------------------------------------------------------

		private void updateForeground()
		{
			Color colour = getColour();
			setForeground(blend ? ColourUtils.blend(colour, transparencyColourButton.getColour())
								: ColourUtils.opaque(colour));
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
