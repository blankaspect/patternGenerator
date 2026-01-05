/*====================================================================*\

Pattern1SourceDialog.java

Pattern 1 source dialog class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
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

import java.util.EnumMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.blankaspect.common.misc.MaxValueMap;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.border.TitledBorder;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.colour.Colours;

import uk.blankaspect.ui.swing.combobox.FComboBox;

import uk.blankaspect.ui.swing.container.IntegerSpinnerSliderPanel;
import uk.blankaspect.ui.swing.container.SpinnerSliderPanel;

import uk.blankaspect.ui.swing.label.FixedWidthLabel;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.slider.FlatHorizontalSlider;

import uk.blankaspect.ui.swing.workaround.LinuxWorkarounds;

//----------------------------------------------------------------------


// PATTERN 1 SOURCE DIALOG CLASS


class Pattern1SourceDialog
	extends JDialog
	implements ActionListener, ChangeListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		WAVE_COEFF_FIELD_LENGTH			= 3;
	private static final	int		ATTENUATION_COEFF_FIELD_LENGTH	= 3;

	private static final	int		SLIDER_HEIGHT	= 18;

	private static final	String	ADD_STR					= "Add";
	private static final	String	EDIT_STR				= "Edit";
	private static final	String	SOURCE_STR				= " source ";
	private static final	String	SHAPE_STR				= "Shape";
	private static final	String	WAVEFORM_STR			= "Waveform";
	private static final	String	WAVE_COEFF_STR			= "Wave coefficient";
	private static final	String	ATTENUATION_COEFF_STR	= "Attenuation coefficient";
	private static final	String	CONSTRAINT_STR			= "Constraint";
	private static final	String	ECCENTRICITY_STR		= "Eccentricity";
	private static final	String	NUM_EDGES_STR			= "Number of edges";
	private static final	String	HUE_SATURATION_STR		= "Hue and saturation";

	private static final	String	SPINNER_SLIDER_PANEL_KEY	= Pattern1SourceDialog.class.getName();

	// Commands
	private interface Command
	{
		String	SELECT_SHAPE			= "selectShape";
		String	SELECT_WAVEFORM			= "selectWaveform";
		String	SET_WAVE_COEFFICIENT	= "setWaveCoefficient";
		String	ACCEPT					= "accept";
		String	CLOSE					= "close";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Point	location;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	boolean										accepted;
	private	FComboBox<Pattern1Image.Source.Shape>		shapeComboBox;
	private	FComboBox<Pattern1Image.Source.Waveform>	waveformComboBox;
	private	IntegerSpinnerSliderPanel					waveCoeffSpinnerSlider;
	private	WavePlotPanel								wavePlotPanel;
	private	IntegerSpinnerSliderPanel					attenuationCoeffSpinnerSlider;
	private	FComboBox<Pattern1Image.Source.Constraint>	constraintComboBox;
	private	JPanel										shapeParamPanel;
	private	HueSaturationPanel							hueSaturationPanel;
	private	FComboBox<Integer>							eccentricityComboBox;
	private	FComboBox<Integer>							numEdgesComboBox;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private Pattern1SourceDialog(Window                     owner,
								 int                        index,
								 Pattern1Image.SourceParams params)
	{
		// Call superclass constructor
		super(owner, (index < 0) ? ADD_STR + SOURCE_STR : EDIT_STR + SOURCE_STR + (index + 1),
			  ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());

		// Reset fixed-width labels
		Label.reset();

		// Initialise spinner-slider panels
		SpinnerSliderPanel.removeInstances(SPINNER_SLIDER_PANEL_KEY);


		//----  Shape panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		// Panel: shape selection
		JPanel shapeSelectionPanel = new JPanel(gridBag);

		int gridY = 0;

		// Label: shape
		JLabel shapeLabel = new Label(SHAPE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(shapeLabel, gbc);
		shapeSelectionPanel.add(shapeLabel);

		// Combo box: shape
		shapeComboBox = new FComboBox<>(Pattern1Image.Source.Shape.values());
		shapeComboBox.setSelectedValue(params.getShape());
		shapeComboBox.setActionCommand(Command.SELECT_SHAPE);
		shapeComboBox.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(shapeComboBox, gbc);
		shapeSelectionPanel.add(shapeComboBox);

		// Panel: shape parameters
		shapeParamPanel = new JPanel(new CardLayout());
		shapeParamPanel.add(createPanelCircle(params), Pattern1Image.Source.Shape.CIRCLE.getKey());
		shapeParamPanel.add(createPanelEllipse(params), Pattern1Image.Source.Shape.ELLIPSE.getKey());
		shapeParamPanel.add(createPanelPolygon(params), Pattern1Image.Source.Shape.POLYGON.getKey());

		// Panel: shape
		JPanel shapePanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(shapePanel);

		gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(shapeSelectionPanel, gbc);
		shapePanel.add(shapeSelectionPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(shapeParamPanel, gbc);
		shapePanel.add(shapeParamPanel);


		//----  Wave panel

		// Panel: wave plot
		wavePlotPanel = new WavePlotPanel(params.getWaveform(), params.getWaveCoefficient());

		// Panel: wave control
		JPanel waveControlPanel = new JPanel(gridBag);

		gridY = 0;

		// Label: waveform
		JLabel waveformLabel = new Label(WAVEFORM_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(waveformLabel, gbc);
		waveControlPanel.add(waveformLabel);

		// Combo box: waveform
		waveformComboBox = new FComboBox<>(Pattern1Image.Source.Waveform.values());
		waveformComboBox.setSelectedValue(params.getWaveform());
		waveformComboBox.setActionCommand(Command.SELECT_WAVEFORM);
		waveformComboBox.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(waveformComboBox, gbc);
		waveControlPanel.add(waveformComboBox);

		// Label: wave coefficient
		JLabel waveCoeffLabel = new Label(WAVE_COEFF_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(waveCoeffLabel, gbc);
		waveControlPanel.add(waveCoeffLabel);

		// Spinner-slider panel: wave coefficient
		int sliderExtent = Pattern1Image.Source.MAX_WAVE_COEFFICIENT - Pattern1Image.Source.MIN_WAVE_COEFFICIENT + 1;
		int sliderWidth = FlatHorizontalSlider.extentToWidth(sliderExtent);
		waveCoeffSpinnerSlider =
				new IntegerSpinnerSliderPanel(params.getWaveCoefficient(), Pattern1Image.Source.MIN_WAVE_COEFFICIENT,
											  Pattern1Image.Source.MAX_WAVE_COEFFICIENT, WAVE_COEFF_FIELD_LENGTH, false,
											  sliderWidth, SLIDER_HEIGHT,  params.getWaveform().getDefaultCoefficient(),
											  SPINNER_SLIDER_PANEL_KEY);
		waveCoeffSpinnerSlider.getSpinner().addChangeListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(waveCoeffSpinnerSlider, gbc);
		waveControlPanel.add(waveCoeffSpinnerSlider);

		// Panel: wave
		JPanel wavePanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(wavePanel);

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
		gridBag.setConstraints(wavePlotPanel, gbc);
		wavePanel.add(wavePlotPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(6, 0, 0, 0);
		gridBag.setConstraints(waveControlPanel, gbc);
		wavePanel.add(waveControlPanel);


		//----  Control panel

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		gridY = 0;

		// Label: attenuation coefficient
		JLabel attenuationCoeffLabel = new Label(ATTENUATION_COEFF_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(attenuationCoeffLabel, gbc);
		controlPanel.add(attenuationCoeffLabel);

		// Spinner-slider panel: attenuation coefficient
		sliderExtent = Pattern1Image.Source.MAX_ATTENUATION_COEFFICIENT
							- Pattern1Image.Source.MIN_ATTENUATION_COEFFICIENT + 1;
		sliderWidth = FlatHorizontalSlider.extentToWidth(sliderExtent);

		attenuationCoeffSpinnerSlider =
				new IntegerSpinnerSliderPanel(params.getAttenuationCoefficient(),
											  Pattern1Image.Source.MIN_ATTENUATION_COEFFICIENT,
											  Pattern1Image.Source.MAX_ATTENUATION_COEFFICIENT,
											  ATTENUATION_COEFF_FIELD_LENGTH, false, sliderWidth, SLIDER_HEIGHT,
											  Pattern1Image.Source.DEFAULT_ATTENUATION_COEFFICIENT,
											  SPINNER_SLIDER_PANEL_KEY);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(attenuationCoeffSpinnerSlider, gbc);
		controlPanel.add(attenuationCoeffSpinnerSlider);

		// Label: constraint
		JLabel constraintLabel = new Label(CONSTRAINT_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(constraintLabel, gbc);
		controlPanel.add(constraintLabel);

		// Combo box: constraint
		constraintComboBox = new FComboBox<>(Pattern1Image.Source.Constraint.values());
		constraintComboBox.setSelectedValue(params.getConstraint());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(constraintComboBox, gbc);
		controlPanel.add(constraintComboBox);

		// Align spinner-slider panels
		SpinnerSliderPanel.align(SPINNER_SLIDER_PANEL_KEY);


		//----  Hue and saturation panel

		hueSaturationPanel = new HueSaturationPanel(params.getHue(), params.getSaturation());
		TitledBorder.setPaddedBorder(hueSaturationPanel, HUE_SATURATION_STR);


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
		gridBag.setConstraints(shapePanel, gbc);
		mainPanel.add(shapePanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(wavePanel, gbc);
		mainPanel.add(wavePanel);

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
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(hueSaturationPanel, gbc);
		mainPanel.add(hueSaturationPanel);

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

		// Select shape panel
		onSelectShape();


		//----  Window

		// Set content pane
		setContentPane(mainPanel);

		// Update widths of labels
		Label.update();

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

	public static Pattern1Image.SourceParams showDialog(Component                  parent,
														int                        index,
														Pattern1Image.SourceParams source)
	{
		return new Pattern1SourceDialog(GuiUtils.getWindow(parent), index, source).getParams();
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
			case Command.SELECT_SHAPE         -> onSelectShape();
			case Command.SELECT_WAVEFORM      -> onSelectWaveform();
			case Command.SET_WAVE_COEFFICIENT -> onSetWaveCoefficient();
			case Command.ACCEPT               -> onAccept();
			case Command.CLOSE                -> onClose();
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ChangeListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void stateChanged(ChangeEvent event)
	{
		updateWavePlot();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private JPanel createPanelCircle(Pattern1Image.SourceParams params)
	{
		return new JPanel();
	}

	//------------------------------------------------------------------

	private JPanel createPanelEllipse(Pattern1Image.SourceParams params)
	{
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel panel = new JPanel(gridBag);

		int gridY = 0;

		// Label: eccentricity
		JLabel eccentricityLabel = new Label(ECCENTRICITY_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(eccentricityLabel, gbc);
		panel.add(eccentricityLabel);

		// Combo box: eccentricity
		eccentricityComboBox = new FComboBox<>();
		for (int i = Pattern1Image.Source.Ellipse.MIN_ECCENTRICITY;
			  i <= Pattern1Image.Source.Ellipse.MAX_ECCENTRICITY; i++)
			eccentricityComboBox.addItem(i);
		if (params.getShape() == Pattern1Image.Source.Shape.ELLIPSE)
			eccentricityComboBox.
				setSelectedItem(params.getShapeParamValue(Pattern1Image.SourceParams.Key.ECCENTRICITY));
		else
			eccentricityComboBox.setSelectedIndex(0);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(eccentricityComboBox, gbc);
		panel.add(eccentricityComboBox);

		return panel;
	}

	//------------------------------------------------------------------

	private JPanel createPanelPolygon(Pattern1Image.SourceParams params)
	{
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel panel = new JPanel(gridBag);

		int gridY = 0;

		// Label: number of edges
		JLabel numEdgesLabel = new Label(NUM_EDGES_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(numEdgesLabel, gbc);
		panel.add(numEdgesLabel);

		// Combo box: number of edges
		numEdgesComboBox = new FComboBox<>();
		for (int i = Pattern1Image.Source.Polygon.MIN_NUM_EDGES;
			  i <= Pattern1Image.Source.Polygon.MAX_NUM_EDGES; i++)
			numEdgesComboBox.addItem(i);
		if (params.getShape() == Pattern1Image.Source.Shape.POLYGON)
			numEdgesComboBox.
				setSelectedItem(params.getShapeParamValue(Pattern1Image.SourceParams.Key.NUM_EDGES));
		else
			numEdgesComboBox.setSelectedIndex(0);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(numEdgesComboBox, gbc);
		panel.add(numEdgesComboBox);

		return panel;
	}

	//------------------------------------------------------------------

	private Pattern1Image.Source.Shape getSourceShape()
	{
		return shapeComboBox.getSelectedValue();
	}

	//------------------------------------------------------------------

	private Pattern1Image.Source.Waveform getWaveform()
	{
		return waveformComboBox.getSelectedValue();
	}

	//------------------------------------------------------------------

	private Pattern1Image.Source.Constraint getConstraint()
	{
		return constraintComboBox.getSelectedValue();
	}

	//------------------------------------------------------------------

	private Pattern1Image.SourceParams getParams()
	{
		Pattern1Image.SourceParams params = null;
		if (accepted)
		{
			Map<Pattern1Image.SourceParams.Key, Object> shapeParams =
													new EnumMap<>(Pattern1Image.SourceParams.Key.class);
			Pattern1Image.Source.Shape shape = getSourceShape();
			switch (shape)
			{
				case CIRCLE:
					// do nothing
					break;

				case ELLIPSE:
					shapeParams.put(Pattern1Image.SourceParams.Key.ECCENTRICITY,
									eccentricityComboBox.getSelectedValue());
					break;

				case POLYGON:
					shapeParams.put(Pattern1Image.SourceParams.Key.NUM_EDGES,
									numEdgesComboBox.getSelectedValue());
					break;
			}
			params = new Pattern1Image.SourceParams(shape, shapeParams, getWaveform(),
													waveCoeffSpinnerSlider.getValue(),
													attenuationCoeffSpinnerSlider.getValue(),
													getConstraint(),
													hueSaturationPanel.getHue(),
													hueSaturationPanel.getSaturation());
		}
		return params;
	}

	//------------------------------------------------------------------

	private void updateWavePlot()
	{
		wavePlotPanel.setValues(getWaveform(), waveCoeffSpinnerSlider.getValue());
	}

	//------------------------------------------------------------------

	private void onSelectShape()
	{
		Pattern1Image.Source.Shape shape = getSourceShape();
		if (shape != null)
			((CardLayout)shapeParamPanel.getLayout()).show(shapeParamPanel, shape.getKey());
	}

	//------------------------------------------------------------------

	private void onSelectWaveform()
	{
		waveCoeffSpinnerSlider.setDefaultValue(getWaveform().getDefaultCoefficient());
		updateWavePlot();
	}

	//------------------------------------------------------------------

	private void onSetWaveCoefficient()
	{
		updateWavePlot();
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
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// LABEL CLASS


	private static class Label
		extends FixedWidthLabel
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	KEY	= Label.class.getCanonicalName();

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Label(String text)
		{
			super(text);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static void reset()
		{
			MaxValueMap.removeAll(KEY);
		}

		//--------------------------------------------------------------

		private static void update()
		{
			MaxValueMap.update(KEY);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected String getKey()
		{
			return KEY;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// WAVE PLOT PANEL CLASS


	private static class WavePlotPanel
		extends JComponent
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	WIDTH			= 360;
		private static final	int	HEIGHT			= 120;
		private static final	int	BORDER_WIDTH	= 1;

		private static final	Color	BACKGROUND_COLOUR	= Colours.BACKGROUND;
		private static final	Color	FOREGROUND_COLOUR	= new Color(0, 0, 192);
		private static final	Color	BORDER_COLOUR		= new Color(208, 208, 192);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Pattern1Image.Source.Waveform	waveform;
		private	int								waveCoeff;
		private	Pattern1Image.Source			source;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private WavePlotPanel(Pattern1Image.Source.Waveform waveform,
							  int                           waveCoeff)
		{
			// Initialise instance variables
			this.waveform = waveform;
			this.waveCoeff = waveCoeff;
			source = Pattern1Image.createWaveTableSource(waveform, waveCoeff, WIDTH);

			// Set properties
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
			return new Dimension(2 * BORDER_WIDTH + WIDTH, 2 * BORDER_WIDTH + HEIGHT);
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Fill background
			Rectangle rect = gr.getClipBounds();
			gr.setColor(BACKGROUND_COLOUR);
			gr.fillRect(rect.x, rect.y, rect.width, rect.height);

			// Draw function
			gr.setColor(FOREGROUND_COLOUR);
			int x1 = Math.max(BORDER_WIDTH, rect.x - 1);
			int x2 = Math.min(rect.x + rect.width - 1, BORDER_WIDTH + WIDTH - 1);
			int prevY = 0;
			for (int x = x1; x <= x2; x++)
			{
				double value = source.getWaveValue(x - BORDER_WIDTH);
				int y = BORDER_WIDTH + HEIGHT - 1 - (int)Math.round(value * (double)(HEIGHT - 1));
				if (x > x1)
					gr.drawLine(x - 1, prevY, x, y);
				prevY = y;
			}

			// Draw border
			gr.setColor(BORDER_COLOUR);
			gr.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void setValues(Pattern1Image.Source.Waveform waveform,
							   int                           waveCoeff)
		{
			if ((waveform != this.waveform) || (waveCoeff != this.waveCoeff))
			{
				this.waveform = waveform;
				this.waveCoeff = waveCoeff;
				source = Pattern1Image.createWaveTableSource(waveform, waveCoeff, WIDTH);
				repaint();
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
