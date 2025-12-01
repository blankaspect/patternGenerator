/*====================================================================*\

Pattern1ParamsDialog.java

Pattern 1 parameters dialog class.

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

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.border.TitledBorder;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.checkbox.FCheckBox;

import uk.blankaspect.ui.swing.colour.Colours;

import uk.blankaspect.ui.swing.combobox.FComboBox;

import uk.blankaspect.ui.swing.container.DimensionsSpinnerPanel;

import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.range.DoubleRangeBarPanel;
import uk.blankaspect.ui.swing.range.IntegerRangeBarPanel;
import uk.blankaspect.ui.swing.range.RangeBarPanel;

import uk.blankaspect.ui.swing.spinner.FDoubleSpinner;
import uk.blankaspect.ui.swing.spinner.FIntegerSpinner;

import uk.blankaspect.ui.swing.text.TextRendering;

import uk.blankaspect.ui.swing.workaround.LinuxWorkarounds;

//----------------------------------------------------------------------


// PATTERN 1 PARAMETERS DIALOG CLASS


class Pattern1ParamsDialog
	extends JDialog
	implements ActionListener, DocumentListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		WIDTH_FIELD_LENGTH	= 4;
	private static final	int		HEIGHT_FIELD_LENGTH	= 4;

	private static final	int		NUM_VIEWABLE_SOURCE_TABLE_ROWS	= 6;

	private static final	Insets	MOTION_RATE_BUTTON_MARGINS	= new Insets(1, 4, 1, 4);

	private static final	String	TITLE_PREFIX	= PatternKind.PATTERN1.getName() + " parameters : ";

	private static final	String	NEW_PATTERN_STR				= "New pattern";
	private static final	String	SIZE_STR					= "Size";
	private static final	String	SYMMETRY_STR				= "Symmetry";
	private static final	String	SATURATION_MODE_STR			= "Saturation mode";
	private static final	String	WAVELENGTH_STR				= "Wavelength";
	private static final	String	SEED_STR					= "Seed";
	private static final	String	SOURCES_STR					= "Sources";
	private static final	String	ANIMATION_STR				= "Animation";
	private static final	String	MOTION_RATE_STR				= "Motion rate";
	private static final	String	MOTION_RATE_ENVELOPE_STR	= "Motion rate envelope";
	private static final	String	PHASE_INCREMENT_STR			= "Phase increment";
	private static final	String	ROTATION_PERIOD_STR			= "Rotation period";
	private static final	String	ROTATION_SENSE_STR			= "Rotation sense";
	private static final	String	EDIT_STR					= "Edit";
	private static final	String	TO_STR						= "to";

	private static final	String	RANGE_BAR_PANEL_KEY	= Pattern1ParamsDialog.class.getName();

	// Commands
	private interface Command
	{
		String	TOGGLE_MOTION_RATE_ENVELOPE_ENABLED	= "toggleMotionRateEnvelopeEnabled";
		String	EDIT_MOTION_RATE_ENVELOPE			= "editMotionRateEnvelope";
		String	ACCEPT								= "accept";
		String	CLOSE								= "close";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Point	location;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	boolean									accepted;
	private	DimensionsSpinnerPanel					sizePanel;
	private	FComboBox<Pattern1Image.Symmetry>		symmetryComboBox;
	private	FComboBox<Pattern1Image.SaturationMode>	saturationModeComboBox;
	private	WavelengthRangePanel					wavelengthRangePanel;
	private	MotionRateRangePanel					motionRateRangePanel;
	private	JCheckBox								motionRateEnvelopeCheckBox;
	private	JButton									motionRateEnvelopeButton;
	private	MotionRateEnvelope						motionRateEnvelope;
	private	PhaseIncrementRangePanel				phaseIncrementRangePanel;
	private	RotationPeriodRangePanel				rotationPeriodRangePanel;
	private	FComboBox<Pattern1Image.RotationSense>	rotationSenseComboBox;
	private	SeedPanel								seedPanel;
	private	SourcePanel								sourcePanel;
	private	JButton									okButton;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private Pattern1ParamsDialog(Window         owner,
								 String         title,
								 Pattern1Params params)
	{
		// Call superclass constructor
		super(owner, TITLE_PREFIX + ((title == null) ? NEW_PATTERN_STR : title),
			  ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());

		// Initialise instance variables
		if (params.isMotionRateEnvelope())
			motionRateEnvelope = params.getMotionRateEnvelope().clone();

		// Initialise range-bar panels
		RangeBarPanel.removeInstances(RANGE_BAR_PANEL_KEY);


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
		sizePanel = new DimensionsSpinnerPanel(params.getWidth(), Pattern1Params.MIN_WIDTH,
											   Pattern1Params.MAX_WIDTH, WIDTH_FIELD_LENGTH,
											   params.getHeight(), Pattern1Params.MIN_HEIGHT,
											   Pattern1Params.MAX_HEIGHT, HEIGHT_FIELD_LENGTH, null);

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

		// Label: symmetry
		JLabel symmetryLabel = new FLabel(SYMMETRY_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(symmetryLabel, gbc);
		controlPanel.add(symmetryLabel);

		// Combo box: symmetry
		symmetryComboBox = new FComboBox<>(Pattern1Image.Symmetry.values());
		symmetryComboBox.setSelectedValue(params.getSymmetry());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(symmetryComboBox, gbc);
		controlPanel.add(symmetryComboBox);

		// Label: saturation mode
		JLabel saturationModeLabel = new FLabel(SATURATION_MODE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(saturationModeLabel, gbc);
		controlPanel.add(saturationModeLabel);

		// Combo box: saturation mode
		saturationModeComboBox = new FComboBox<>(Pattern1Image.SaturationMode.values());
		saturationModeComboBox.setSelectedValue(params.getSaturationMode());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(saturationModeComboBox, gbc);
		controlPanel.add(saturationModeComboBox);

		// Label: wavelength range
		JLabel wavelengthRangeLabel = new FLabel(WAVELENGTH_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(wavelengthRangeLabel, gbc);
		controlPanel.add(wavelengthRangeLabel);

		// Panel: wavelength range
		wavelengthRangePanel = new WavelengthRangePanel();
		wavelengthRangePanel.setRange(params.getWavelengthRange());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(wavelengthRangePanel, gbc);
		controlPanel.add(wavelengthRangePanel);

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


		//----  Source table panel

		JPanel sourceTablePanel = new JPanel(gridBag);
		TitledBorder.setPaddedBorder(sourceTablePanel, SOURCES_STR);

		// Source panel
		sourcePanel = new SourcePanel(params.getSources());

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(sourcePanel, gbc);
		sourceTablePanel.add(sourcePanel);


		//----  Animation panel

		JPanel animationPanel = new JPanel(gridBag);
		TitledBorder.setPaddedBorder(animationPanel, ANIMATION_STR);

		// Label: motion-rate range
		JLabel motionRateRangeLabel = new FLabel(MOTION_RATE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(motionRateRangeLabel, gbc);
		animationPanel.add(motionRateRangeLabel);

		// Panel: motion-rate range
		motionRateRangePanel = new MotionRateRangePanel();
		motionRateRangePanel.setRange(params.getMotionRateRange());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(motionRateRangePanel, gbc);
		animationPanel.add(motionRateRangePanel);

		// Panel: motion-rate envelope
		JPanel motionRateEnvelopePanel = new JPanel(gridBag);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(motionRateEnvelopePanel, gbc);
		animationPanel.add(motionRateEnvelopePanel);

		// Check box: motion-rate envelope
		motionRateEnvelopeCheckBox = new FCheckBox(MOTION_RATE_ENVELOPE_STR);
		motionRateEnvelopeCheckBox.setActionCommand(Command.TOGGLE_MOTION_RATE_ENVELOPE_ENABLED);
		motionRateEnvelopeCheckBox.addActionListener(this);
		motionRateEnvelopeCheckBox.setSelected(motionRateEnvelope != null);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(motionRateEnvelopeCheckBox, gbc);
		motionRateEnvelopePanel.add(motionRateEnvelopeCheckBox);

		// Button: motion-rate envelope
		motionRateEnvelopeButton = new FButton(EDIT_STR);
		motionRateEnvelopeButton.setMargin(MOTION_RATE_BUTTON_MARGINS);
		motionRateEnvelopeButton.setActionCommand(Command.EDIT_MOTION_RATE_ENVELOPE);
		motionRateEnvelopeButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 8, 0, 0);
		gridBag.setConstraints(motionRateEnvelopeButton, gbc);
		motionRateEnvelopePanel.add(motionRateEnvelopeButton);

		// Label: phase-increment range
		JLabel phaseIncRangeLabel = new FLabel(PHASE_INCREMENT_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(phaseIncRangeLabel, gbc);
		animationPanel.add(phaseIncRangeLabel);

		// Panel: phase-increment range
		phaseIncrementRangePanel = new PhaseIncrementRangePanel();
		phaseIncrementRangePanel.setRange(params.getPhaseIncrementRange());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(phaseIncrementRangePanel, gbc);
		animationPanel.add(phaseIncrementRangePanel);

		// Label: rotation-period range
		JLabel rotationPeriodRangeLabel = new FLabel(ROTATION_PERIOD_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(rotationPeriodRangeLabel, gbc);
		animationPanel.add(rotationPeriodRangeLabel);

		// Panel: rotation-period range
		rotationPeriodRangePanel = new RotationPeriodRangePanel();
		rotationPeriodRangePanel.setRange(params.getRotationPeriodRange());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(rotationPeriodRangePanel, gbc);
		animationPanel.add(rotationPeriodRangePanel);

		// Label: rotation sense
		JLabel rotationSenseLabel = new FLabel(ROTATION_SENSE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(rotationSenseLabel, gbc);
		animationPanel.add(rotationSenseLabel);

		// Combo box: rotation sense
		rotationSenseComboBox = new FComboBox<>(Pattern1Image.RotationSense.values());
		rotationSenseComboBox.setSelectedValue(params.getRotationSense());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(rotationSenseComboBox, gbc);
		animationPanel.add(rotationSenseComboBox);

		// Align range-bar panels
		RangeBarPanel.align(RANGE_BAR_PANEL_KEY);


		//----  Button panel

		JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 8, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

		// Button: OK
		okButton = new FButton(AppConstants.OK_STR);
		okButton.setActionCommand(Command.ACCEPT);
		okButton.addActionListener(this);
		buttonPanel.add(okButton);

		// Button: cancel
		JButton cancelButton = new FButton(AppConstants.CANCEL_STR);
		cancelButton.setActionCommand(Command.CLOSE);
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);


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
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		mainPanel.add(controlPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(sourceTablePanel, gbc);
		mainPanel.add(sourceTablePanel);

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
		mainPanel.add(animationPanel);

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
		sourcePanel.updateButtons();
		updateComponents();


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
				// WORKAROUND for a bug that has been observed on Linux/GNOME whereby a window is displaced downwards
				// when its location is set.  The error in the y coordinate is the height of the title bar of the
				// window.  The workaround is to set the location of the window again with an adjustment for the error.
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

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Pattern1Params showDialog(Component      parent,
											String         title,
											Pattern1Params params)
	{
		return new Pattern1ParamsDialog(GuiUtils.getWindow(parent), title, params).getParams();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void actionPerformed(ActionEvent event)
	{
		switch (event.getActionCommand())
		{
			case Command.TOGGLE_MOTION_RATE_ENVELOPE_ENABLED -> onToggleMotionRateEnvelopeEnabled();
			case Command.EDIT_MOTION_RATE_ENVELOPE           -> onEditMotionRateEnvelope();
			case Command.ACCEPT                              -> onAccept();
			case Command.CLOSE                               -> onClose();
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : DocumentListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void changedUpdate(DocumentEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	@Override
	public void insertUpdate(DocumentEvent event)
	{
		updateAcceptButton();
	}

	//------------------------------------------------------------------

	@Override
	public void removeUpdate(DocumentEvent event)
	{
		updateAcceptButton();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private Pattern1Params getParams()
	{
		Pattern1Params params = null;
		if (accepted)
		{
			params = new Pattern1Params(
				sizePanel.getValue1(),
				sizePanel.getValue2(),
				getSymmetry(),
				getSaturationMode(),
				wavelengthRangePanel.getRange(),
				sourcePanel.tableModel.sources,
				motionRateRangePanel.getRange(),
				motionRateEnvelopeCheckBox.isSelected() ? motionRateEnvelope : null,
				phaseIncrementRangePanel.getRange(),
				rotationPeriodRangePanel.getRange(),
				getRotationSense(),
				seedPanel.getSeed()
			);
		}
		return params;
	}

	//------------------------------------------------------------------

	private Pattern1Image.Symmetry getSymmetry()
	{
		return symmetryComboBox.getSelectedValue();
	}

	//------------------------------------------------------------------

	private Pattern1Image.SaturationMode getSaturationMode()
	{
		return saturationModeComboBox.getSelectedValue();
	}

	//------------------------------------------------------------------

	private Pattern1Image.RotationSense getRotationSense()
	{
		return rotationSenseComboBox.getSelectedValue();
	}

	//------------------------------------------------------------------

	private void updateAcceptButton()
	{
		okButton.setEnabled(!seedPanel.getField().isEmpty());
	}

	//------------------------------------------------------------------

	private void updateMotionRateEnvelopeButton()
	{
		motionRateEnvelopeButton.setEnabled(motionRateEnvelopeCheckBox.isSelected());
	}

	//------------------------------------------------------------------

	private void updateComponents()
	{
		updateMotionRateEnvelopeButton();
		updateAcceptButton();
	}

	//------------------------------------------------------------------

	private void onToggleMotionRateEnvelopeEnabled()
	{
		updateMotionRateEnvelopeButton();
	}

	//------------------------------------------------------------------

	private void onEditMotionRateEnvelope()
	{
		MotionRateEnvelope envelope = motionRateEnvelope;
		if (envelope == null)
			envelope = Pattern1Image.DEFAULT_MOTION_RATE_ENVELOPE;
		envelope = MotionRateEnvelopeDialog.showDialog(this, envelope);
		if (envelope != null)
			motionRateEnvelope = envelope;
	}

	//------------------------------------------------------------------

	private void onAccept()
	{
		accepted = true;
		onClose();
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


	// TABLE COLUMN


	private enum Column
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		INDEX
		(
			"#",
			SwingConstants.TRAILING
		),

		SHAPE
		(
			"Shape",
			SwingConstants.LEADING
		),

		PARAMETER
		(
			"Par",
			SwingConstants.TRAILING
		),

		WAVEFORM
		(
			"Waveform",
			SwingConstants.LEADING
		),

		WAVE_COEFFICIENT
		(
			"Coeff",
			SwingConstants.TRAILING
		),

		ATTENUATION_COEFFICIENT
		(
			"Atten",
			SwingConstants.TRAILING
		),

		CONSTRAINT
		(
			"Constraint",
			SwingConstants.LEADING
		),

		COLOUR
		(
			"Colour",
			SwingConstants.LEADING
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;
		private	int		alignment;

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

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// WAVELENGTH RANGE PANEL CLASS


	private static class WavelengthRangePanel
		extends DoubleRangeBarPanel.Horizontal
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	double	OFFSET	= Math.log(Pattern1Params.MIN_WAVELENGTH);
		private static final	double	FACTOR	= Math.log(Pattern1Params.MAX_WAVELENGTH) - OFFSET;

		private static final	int		BAR_EXTENT	= 200;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private WavelengthRangePanel()
		{
			super(TO_STR, new Spinner(), new Spinner(), BAR_EXTENT);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public double normaliseValue(double value)
		{
			return (Math.log(value) - OFFSET) / FACTOR;
		}

		//--------------------------------------------------------------

		@Override
		public double denormaliseValue(double value)
		{
			return Math.exp(OFFSET + value * FACTOR);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Member classes : non-inner classes
	////////////////////////////////////////////////////////////////////


		// SPINNER CLASS


		private static class Spinner
			extends FDoubleSpinner
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			private static final	double	DELTA_VALUE	= 0.001;

			private static final	int		FIELD_LENGTH	= 5;

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Spinner()
			{
				super(Pattern1Params.MIN_WAVELENGTH, Pattern1Params.MIN_WAVELENGTH,
					  Pattern1Params.MAX_WAVELENGTH, DELTA_VALUE, FIELD_LENGTH, AppConstants.FORMAT_1_3);
			}

			//----------------------------------------------------------

		}

		//==============================================================

	}

	//==================================================================


	// PHASE-INCREMENT RANGE PANEL CLASS


	private static class PhaseIncrementRangePanel
		extends IntegerRangeBarPanel.Horizontal
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	OFFSET	= Pattern1Image.MIN_PHASE_INCREMENT;
		private static final	int	FACTOR	= Pattern1Image.MAX_PHASE_INCREMENT - OFFSET;

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

			private static final	int	FIELD_LENGTH	= 4;

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Spinner()
			{
				super(Pattern1Image.MIN_PHASE_INCREMENT, Pattern1Image.MIN_PHASE_INCREMENT,
					  Pattern1Image.MAX_PHASE_INCREMENT, FIELD_LENGTH, true);
			}

			//----------------------------------------------------------

		}

		//==============================================================

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PhaseIncrementRangePanel()
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


	// MOTION-RATE RANGE PANEL CLASS


	private static class MotionRateRangePanel
		extends DoubleRangeBarPanel.Horizontal
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	double	OFFSET	= Math.log(Pattern1Image.MIN_MOTION_RATE);
		private static final	double	FACTOR	= Math.log(Pattern1Image.MAX_MOTION_RATE) - OFFSET;

		private static final	int		BAR_EXTENT	= 200;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private MotionRateRangePanel()
		{
			super(TO_STR, new Spinner(), new Spinner(), BAR_EXTENT, RANGE_BAR_PANEL_KEY);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public double normaliseValue(double value)
		{
			return (Math.log(value) - OFFSET) / FACTOR;
		}

		//--------------------------------------------------------------

		@Override
		public double denormaliseValue(double value)
		{
			return Math.exp(OFFSET + value * FACTOR);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Member classes : non-inner classes
	////////////////////////////////////////////////////////////////////


		// SPINNER CLASS


		private static class Spinner
			extends FDoubleSpinner
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			private static final	double	DELTA_VALUE	= 0.01;

			private static final	int		FIELD_LENGTH	= 4;

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Spinner()
			{
				super(Pattern1Image.MIN_MOTION_RATE, Pattern1Image.MIN_MOTION_RATE,
					  Pattern1Image.MAX_MOTION_RATE, DELTA_VALUE, FIELD_LENGTH, AppConstants.FORMAT_1_2);
			}

			//----------------------------------------------------------

		}

		//==============================================================

	}

	//==================================================================


	// ROTATION-PERIOD RANGE PANEL CLASS


	private static class RotationPeriodRangePanel
		extends DoubleRangeBarPanel.Horizontal
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	double	OFFSET	= Math.log(Pattern1Params.MIN_ROTATION_PERIOD);
		private static final	double	FACTOR	= Math.log(Pattern1Params.MAX_ROTATION_PERIOD) - OFFSET;

		private static final	int		BAR_EXTENT	= 200;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private RotationPeriodRangePanel()
		{
			super(TO_STR, new Spinner(), new Spinner(), BAR_EXTENT, RANGE_BAR_PANEL_KEY);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public double normaliseValue(double value)
		{
			return (Math.log(value) - OFFSET) / FACTOR;
		}

		//--------------------------------------------------------------

		@Override
		public double denormaliseValue(double value)
		{
			return Math.exp(OFFSET + value * FACTOR);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Member classes : non-inner classes
	////////////////////////////////////////////////////////////////////


		// SPINNER CLASS


		private static class Spinner
			extends FDoubleSpinner
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			private static final	double	DELTA_VALUE	= 1.0;

			private static final	int	FIELD_LENGTH	= 6;

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Spinner()
			{
				super(Pattern1Params.MIN_ROTATION_PERIOD, Pattern1Params.MIN_ROTATION_PERIOD,
					  Pattern1Params.MAX_ROTATION_PERIOD, DELTA_VALUE, FIELD_LENGTH,
					  AppConstants.FORMAT_1_3);
			}

			//----------------------------------------------------------

		}

		//==============================================================

	}

	//==================================================================


	// SOURCE TABLE CLASS


	private static class SourceTable
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
		}

		private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
		{
			KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK),
							  Command.FOCUS_PREVIOUS),
			KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
							  Command.FOCUS_NEXT)
		};

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private SourceTable(AbstractTableModel tableModel)
		{
			// Call superclass constructor
			super(tableModel);

			// Set properties
			setGridColor(Colours.Table.GRID.getColour());
			setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			setIntercellSpacing(new Dimension());
			AppFont.MAIN.apply(getTableHeader());
			AppFont.MAIN.apply(this);
			int rowHeight = 2 * CELL_VERTICAL_MARGIN + GRID_LINE_WIDTH +
																getFontMetrics(getFont()).getHeight();
			setRowHeight(rowHeight);

			// Initialise columns
			TableColumnModel columnModel = getColumnModel();
			CellEditor cellEditor = new CellEditor();
			for (Column id : Column.values())
			{
				TableColumn column = columnModel.getColumn(id.ordinal());
				column.setIdentifier(id);
				String text = null;
				TableCellRenderer headerRenderer = new TextRenderer(id.text, id.alignment);
				TableCellRenderer cellRenderer = null;
				switch (id)
				{
					case INDEX:
						text = Integer.toString(Pattern1Params.MAX_NUM_SOURCES);
						cellRenderer = new TextRenderer(text, id.alignment);
						break;

					case SHAPE:
						text = getWidestString(Pattern1Image.Source.Shape.values());
						cellRenderer = new TextRenderer(text, id.alignment);
						break;

					case PARAMETER:
					{
						int maxValue = 0;
						int value = Pattern1Image.Source.Ellipse.MAX_ECCENTRICITY;
						maxValue = Math.max(value, maxValue);
						value = Pattern1Image.Source.Polygon.MAX_NUM_EDGES;
						maxValue = Math.max(value, maxValue);
						text = Integer.toString(maxValue);
						cellRenderer = new TextRenderer(text, id.alignment);
						break;
					}

					case WAVEFORM:
						text = getWidestString(Pattern1Image.Source.Waveform.values());
						cellRenderer = new TextRenderer(text, id.alignment);
						break;

					case WAVE_COEFFICIENT:
						text = Integer.toString(Pattern1Image.Source.MAX_WAVE_COEFFICIENT);
						cellRenderer = new TextRenderer(text, id.alignment);
						break;

					case ATTENUATION_COEFFICIENT:
						text = Integer.toString(Pattern1Image.Source.MAX_ATTENUATION_COEFFICIENT);
						cellRenderer = new TextRenderer(text, id.alignment);
						break;

					case CONSTRAINT:
						text = getWidestString(Pattern1Image.Source.Constraint.values());
						cellRenderer = new TextRenderer(text, id.alignment);
						break;

					case COLOUR:
						cellRenderer = new ColourRenderer();
						break;
				}
				int width = Math.max(((JComponent)headerRenderer).getPreferredSize().width,
									 ((JComponent)cellRenderer).getPreferredSize().width);
				column.setMinWidth(width);
				column.setMaxWidth(width);
				column.setPreferredWidth(width);
				column.setHeaderValue(id);
				column.setHeaderRenderer(headerRenderer);
				column.setCellRenderer(cellRenderer);
				column.setCellEditor(cellEditor);
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

		@Override
		public void actionPerformed(ActionEvent event)
		{
			switch (event.getActionCommand())
			{
				case Command.FOCUS_PREVIOUS -> onFocusPrevious();
				case Command.FOCUS_NEXT     -> onFocusNext();
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : FocusListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void focusGained(FocusEvent event)
		{
			getTableHeader().repaint();
			getParent().repaint();
		}

		//--------------------------------------------------------------

		@Override
		public void focusLost(FocusEvent event)
		{
			getTableHeader().repaint();
			getParent().repaint();
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
			// do nothing
		}

		//--------------------------------------------------------------

		@Override
		public void mouseExited(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		@Override
		public void mousePressed(MouseEvent event)
		{
			if (SwingUtilities.isLeftMouseButton(event))
				requestFocusInWindow();
		}

		//--------------------------------------------------------------

		@Override
		public void mouseReleased(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private String getWidestString(Object[] values)
		{
			String widestStr = null;
			FontMetrics fontMetrics = getFontMetrics(getFont());
			int maxWidth = 0;
			for (Object value : values)
			{
				String str = value.toString();
				int width = fontMetrics.stringWidth(str);
				if (maxWidth < width)
				{
					maxWidth = width;
					widestStr = str;
				}
			}
			return widestStr;
		}

		//--------------------------------------------------------------

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

	}

	//==================================================================


	// SOURCE TABLE MODEL CLASS


	private static class SourceTableModel
		extends AbstractTableModel
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	List<Pattern1Image.SourceParams>	sources;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private SourceTableModel(List<Pattern1Image.SourceParams> sources)
		{
			this.sources = new ArrayList<>(sources);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public int getRowCount()
		{
			return sources.size();
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
			Pattern1Image.SourceParams source = sources.get(row);
			if ((column >= 0) && (column < Column.values().length))
			{
				switch (Column.values()[column])
				{
					case INDEX:
						return Integer.toString(row + 1);

					case SHAPE:
						return source.getShape();

					case PARAMETER:
					{
						Pattern1Image.SourceParams.Key key = null;
						switch (source.getShape())
						{
							case CIRCLE:
								// do nothing
								break;

							case ELLIPSE:
								key = Pattern1Image.SourceParams.Key.ECCENTRICITY;
								break;

							case POLYGON:
								key = Pattern1Image.SourceParams.Key.NUM_EDGES;
								break;
						}
						return (key == null) ? "" : source.getShapeParamValue(key);
					}

					case WAVEFORM:
						return source.getWaveform();

					case WAVE_COEFFICIENT:
						return source.getWaveCoefficient();

					case ATTENUATION_COEFFICIENT:
						return source.getAttenuationCoefficient();

					case CONSTRAINT:
						return source.getConstraint();

					case COLOUR:
						return source.getColour();
				}
			}
			return null;
		}

		//--------------------------------------------------------------

		@Override
		public boolean isCellEditable(int row,
									  int column)
		{
			return (row >= 0);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void addSource(Pattern1Image.SourceParams source)
		{
			int numSources = sources.size();
			if (numSources < Pattern1Params.MAX_NUM_SOURCES)
			{
				sources.add(source);
				fireTableRowsInserted(numSources, numSources);
			}
		}

		//--------------------------------------------------------------

		private void deleteSource(int index)
		{
			int numSources = sources.size();
			if ((numSources > Pattern1Params.MIN_NUM_SOURCES) && (index < numSources))
			{
				sources.remove(index);
				fireTableRowsDeleted(index, index);
			}
		}

		//--------------------------------------------------------------

		private void setSource(int                        index,
							   Pattern1Image.SourceParams source)
		{
			sources.set(index, source);
			fireTableRowsUpdated(index, index);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// TEXT RENDERER CLASS


	private static class TextRenderer
		extends JComponent
		implements TableCellRenderer
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;
		private	int		alignment;
		private	boolean	isHeader;
		private	Color	borderColour;

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
			int width = 2 * SourceTable.CELL_HORIZONTAL_MARGIN + SourceTable.GRID_LINE_WIDTH +
																			fontMetrics.stringWidth(text);
			int height = 2 * SourceTable.CELL_VERTICAL_MARGIN + SourceTable.GRID_LINE_WIDTH +
													fontMetrics.getAscent() + fontMetrics.getDescent();
			return new Dimension(width, height);
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Create copy of graphics context
			Graphics2D gr2d = GuiUtils.copyGraphicsContext(gr);

			// Fill background
			int width = getWidth();
			int height = getHeight();
			gr2d.setColor(getBackground());
			gr2d.fillRect(0, 0, width, height);

			// Set rendering hints for text antialiasing and fractional metrics
			TextRendering.setHints(gr2d);

			// Draw text
			gr2d.setColor(getForeground());
			FontMetrics fontMetrics = gr2d.getFontMetrics();
			int x = (alignment == SwingConstants.LEADING)
							? SourceTable.CELL_HORIZONTAL_MARGIN
							: width - (fontMetrics.stringWidth(text) + SourceTable.CELL_HORIZONTAL_MARGIN);
			gr2d.drawString(text, x, FontUtils.getBaselineOffset(height, fontMetrics));

			// Draw cell border
			--width;
			--height;
			gr2d.setColor(isHeader ? borderColour : Colours.Table.GRID.getColour());
			gr2d.drawLine(width, 0, width, height);
			gr2d.drawLine(0, height, width, height);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// COLOUR RENDERER CLASS


	private static class ColourRenderer
		extends JComponent
		implements TableCellRenderer
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int		SAMPLE_WIDTH	= 32;
		private static final	int		SAMPLE_HEIGHT	= 12;

		private static final	Color	BORDER_COLOUR	= Colours.LINE_BORDER;

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Color	colour;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ColourRenderer()
		{
			setOpaque(true);
			setFocusable(false);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : TableCellRenderer interface
	////////////////////////////////////////////////////////////////////

		@Override
		public Component getTableCellRendererComponent(JTable  table,
													   Object  value,
													   boolean isSelected,
													   boolean hasFocus,
													   int     row,
													   int     column)
		{
			colour = (Color)value;
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
			int width = 2 * SourceTable.CELL_HORIZONTAL_MARGIN + SourceTable.GRID_LINE_WIDTH + SAMPLE_WIDTH;
			int height = 2 * SourceTable.CELL_VERTICAL_MARGIN + SourceTable.GRID_LINE_WIDTH + SAMPLE_HEIGHT;
			return new Dimension(width, height);
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Fill background
			int width = getWidth();
			int height = getHeight();
			gr.setColor(getBackground());
			gr.fillRect(0, 0, width, height);

			// Draw colour sample
			int x = SourceTable.CELL_HORIZONTAL_MARGIN;
			int y = (height - SAMPLE_HEIGHT) / 2;
			gr.setColor(colour);
			gr.fillRect(x + 1, y + 1, SAMPLE_WIDTH - 2, SAMPLE_HEIGHT - 2);

			// Draw border of sample
			gr.setColor(BORDER_COLOUR);
			gr.drawRect(x, y, SAMPLE_WIDTH - 1, SAMPLE_HEIGHT - 1);

			// Draw cell border
			--width;
			--height;
			gr.setColor(Colours.Table.GRID.getColour());
			gr.drawLine(width, 0, width, height);
			gr.drawLine(0, height, width, height);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CELL EDITOR CLASS


	private static class CellEditor
		extends AbstractCellEditor
		implements TableCellEditor
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CellEditor()
		{
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : TableCellEditor interface
	////////////////////////////////////////////////////////////////////

		@Override
		public Object getCellEditorValue()
		{
			return null;
		}

		//--------------------------------------------------------------

		@Override
		public Component getTableCellEditorComponent(JTable  table,
													 Object  value,
													 boolean selected,
													 int     row,
													 int     column)
		{
			SourceTableModel tableModel = (SourceTableModel)table.getModel();
			Pattern1Image.SourceParams source = tableModel.sources.get(row);
			source = Pattern1SourceDialog.showDialog(table, row, source);
			if (source != null)
				tableModel.setSource(row, source);
			return null;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public boolean isCellEditable(EventObject event)
		{
			// Test for a mouse event
			if (event instanceof MouseEvent mouseEvent)
				return SwingUtilities.isLeftMouseButton(mouseEvent) && (mouseEvent.getClickCount() > 1);

			// Test for a key event
			if (event instanceof KeyEvent keyEvent)
				return (keyEvent.getKeyCode() == KeyEvent.VK_SPACE) && (keyEvent.getModifiersEx() == 0);

			return false;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// SOURCE PANEL CLASS


	private static class SourcePanel
		extends JPanel
		implements ActionListener, ListSelectionListener
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	ADD_STR			= "Add";
		private static final	String	DUPLICATE_STR	= "Duplicate";
		private static final	String	EDIT_STR		= "Edit";
		private static final	String	DELETE_STR		= "Delete";

		private interface Command
		{
			String	ADD			= "add";
			String	DUPLICATE	= "duplicate";
			String	EDIT		= "edit";
			String	DELETE		= "delete";
		}

		private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
		{
			KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.SHIFT_DOWN_MASK),
							  Command.DELETE)
		};

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	SourceTableModel	tableModel;
		private	SourceTable			table;
		private	JButton				addButton;
		private	JButton				duplicateButton;
		private	JButton				editButton;
		private	JButton				deleteButton;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private SourcePanel(List<Pattern1Image.SourceParams> sources)
		{
			// Set layout manager
			GridBagLayout gridBag = new GridBagLayout();
			GridBagConstraints gbc = new GridBagConstraints();

			setLayout(gridBag);

			int gridX = 0;


			//----  Source table scroll pane

			tableModel = new SourceTableModel(sources);

			table = new SourceTable(tableModel);
			table.getSelectionModel().addListSelectionListener(this);
			KeyAction.create(table, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this, KEY_COMMANDS);

			JScrollPane tableScrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
														  JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			Dimension viewSize = new Dimension(table.getPreferredScrollableViewportSize().width,
											   NUM_VIEWABLE_SOURCE_TABLE_ROWS * table.getRowHeight());
			tableScrollPane.getViewport().setPreferredSize(viewSize);
			tableScrollPane.getViewport().setFocusable(false);
			tableScrollPane.getVerticalScrollBar().setFocusable(false);
			tableScrollPane.getHorizontalScrollBar().setFocusable(false);

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.5;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 0, 0, 4);
			gridBag.setConstraints(tableScrollPane, gbc);
			add(tableScrollPane);


			//----  Button panel

			JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 8));

			// Button: add
			addButton = new FButton(ADD_STR + AppConstants.ELLIPSIS_STR);
			addButton.setMnemonic(KeyEvent.VK_A);
			addButton.setActionCommand(Command.ADD);
			addButton.addActionListener(this);
			buttonPanel.add(addButton);

			// Button: duplicate
			duplicateButton = new FButton(DUPLICATE_STR);
			duplicateButton.setMnemonic(KeyEvent.VK_D);
			duplicateButton.setActionCommand(Command.DUPLICATE);
			duplicateButton.addActionListener(this);
			buttonPanel.add(duplicateButton);

			// Button: edit
			editButton = new FButton(EDIT_STR + AppConstants.ELLIPSIS_STR);
			editButton.setMnemonic(KeyEvent.VK_E);
			editButton.setActionCommand(Command.EDIT);
			editButton.addActionListener(this);
			buttonPanel.add(editButton);

			// Button: delete
			deleteButton = new FButton(DELETE_STR);
			deleteButton.setMnemonic(KeyEvent.VK_L);
			deleteButton.setActionCommand(Command.DELETE);
			deleteButton.addActionListener(this);
			buttonPanel.add(deleteButton);

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.5;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 4, 0, 0);
			gridBag.setConstraints(buttonPanel, gbc);
			add(buttonPanel);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void actionPerformed(ActionEvent event)
		{
			switch (event.getActionCommand())
			{
				case Command.ADD       -> onAdd();
				case Command.DUPLICATE -> onDuplicate();
				case Command.EDIT      -> onEdit();
				case Command.DELETE    -> onDelete();
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ListSelectionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void valueChanged(ListSelectionEvent event)
		{
			updateButtons();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void updateButtons()
		{
			boolean isSelection = (table.getSelectedRowCount() > 0);
			int numRows = table.getRowCount();

			addButton.setEnabled(numRows < Pattern1Params.MAX_NUM_SOURCES);
			duplicateButton.setEnabled(isSelection && (numRows < Pattern1Params.MAX_NUM_SOURCES));
			editButton.setEnabled(isSelection);
			deleteButton.setEnabled((numRows > Pattern1Params.MIN_NUM_SOURCES) && isSelection);
		}

		//--------------------------------------------------------------

		private void onAdd()
		{
			Pattern1Image.SourceParams source =
								Pattern1SourceDialog.showDialog(table, -1, Pattern1Params.DEFAULT_SOURCE);
			if (source != null)
			{
				tableModel.addSource(source);
				updateButtons();
			}
		}

		//--------------------------------------------------------------

		private void onDuplicate()
		{
			tableModel.addSource(tableModel.sources.get(table.getSelectedRow()).clone());
			updateButtons();
		}

		//--------------------------------------------------------------

		private void onEdit()
		{
			int row = table.getSelectedRow();
			Pattern1Image.SourceParams source = tableModel.sources.get(row);
			source = Pattern1SourceDialog.showDialog(table, row, source);
			if (source != null)
				tableModel.setSource(row, source);
		}

		//--------------------------------------------------------------

		private void onDelete()
		{
			tableModel.deleteSource(table.getSelectedRow());
			updateButtons();
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
