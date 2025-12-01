/*====================================================================*\

MotionRateEnvelopeDialog.java

Motion-rate envelope dialog class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.blankaspect.common.envelope.EnvelopeKind;
import uk.blankaspect.common.envelope.NodeId;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.envelope.EnvelopeNodeValueDialog;
import uk.blankaspect.ui.swing.envelope.EnvelopeScrollPane;
import uk.blankaspect.ui.swing.envelope.SimpleViewEnvelope;
import uk.blankaspect.ui.swing.envelope.SimpleViewNode;

import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.spinner.FDoubleSpinner;

import uk.blankaspect.ui.swing.textfield.DoubleValueField;

import uk.blankaspect.ui.swing.workaround.LinuxWorkarounds;

//----------------------------------------------------------------------


// MOTION-RATE ENVELOPE DIALOG CLASS


class MotionRateEnvelopeDialog
	extends JDialog
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	double	MIN_NODE_X	= 0.0;
	private static final	double	MAX_NODE_X	= 1.0;

	private static final	double	MIN_NODE_Y	= 0.0;
	private static final	double	MAX_NODE_Y	= 1.0;

	private static final	String	TITLE_STR	= "Animation motion-rate envelope";
	private static final	String	X_RANGE_STR	= "x range";
	private static final	String	Y_RANGE_STR	= "y range";
	private static final	String	FRAMES_STR	= "frames";

	// Commands
	private interface Command
	{
		String	ACCEPT	= "accept";
		String	CLOSE	= "close";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Point	location;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	boolean				accepted;
	private	SimpleViewEnvelope	envelope;
	private	CoefficientField	xCoeffField;
	private	CoefficientField	yCoeffField;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private MotionRateEnvelopeDialog(Window             owner,
									 MotionRateEnvelope envelope)
	{
		// Call superclass constructor
		super(owner, TITLE_STR, ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());


		//----  Envelope panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel envelopePanel = new JPanel(gridBag);

		int gridY = 0;

		// Field: node value
		NodeValueField nodeValueField = new NodeValueField();

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(nodeValueField, gbc);
		envelopePanel.add(nodeValueField);

		// Envelope view
		this.envelope = new SimpleViewEnvelope(EnvelopeKind.LINEAR);
		this.envelope.setMinDeltaX(1.0 / (double)(EnvelopeView.NUM_X_DIVS * EnvelopeView.X_DIV_WIDTH));
		this.envelope.setNodes(envelope.getNodes(), false, false);

		EnvelopeView envelopeView = new EnvelopeView();
		envelopeView.addEnvelope(this.envelope);
		envelopeView.setScrollUnitIncrement(2);
		envelopeView.addChangeListener(nodeValueField);
		envelopeView.resetSelectedNodeId();

		// Scroll pane: envelope view
		EnvelopeScrollPane envelopeScrollPane = new EnvelopeScrollPane(envelopeView);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 0, 0, 0);
		gridBag.setConstraints(envelopeScrollPane, gbc);
		envelopePanel.add(envelopeScrollPane);


		//----  Control panel

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		gridY = 0;

		// Label: x range
		JLabel xRangeLabel = new FLabel(X_RANGE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(xRangeLabel, gbc);
		controlPanel.add(xRangeLabel);

		// Panel: x coefficient
		JPanel xRangePanel = new JPanel(gridBag);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(xRangePanel, gbc);
		controlPanel.add(xRangePanel);

		// Field: x coefficient
		xCoeffField = new CoefficientField(envelope.getXCoefficient());

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(xCoeffField, gbc);
		xRangePanel.add(xCoeffField);

		// Label: frames
		JLabel framesLabel = new FLabel(FRAMES_STR);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 4, 0, 0);
		gridBag.setConstraints(framesLabel, gbc);
		xRangePanel.add(framesLabel);

		// Label: y coefficient
		JLabel yRangeLabel = new FLabel(Y_RANGE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(yRangeLabel, gbc);
		controlPanel.add(yRangeLabel);

		// Field: y coefficient
		yCoeffField = new CoefficientField(envelope.getYCoefficient());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(yCoeffField, gbc);
		controlPanel.add(yCoeffField);


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
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(envelopePanel, gbc);
		mainPanel.add(envelopePanel);

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


		//----  Window

		// Set content pane
		setContentPane(mainPanel);

		// Dispose of window when it is closed
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

	public static MotionRateEnvelope showDialog(Component          parent,
												MotionRateEnvelope envelope)
	{
		return new MotionRateEnvelopeDialog(GuiUtils.getWindow(parent), envelope).getEnvelope();
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
			case Command.ACCEPT -> onAccept();
			case Command.CLOSE  -> onClose();
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public MotionRateEnvelope getEnvelope()
	{
		return accepted
				? new MotionRateEnvelope(envelope.getNodes(), xCoeffField.getValue(), yCoeffField.getValue())
				: null;
	}

	//------------------------------------------------------------------

	private void validateUserInput()
		throws AppException
	{
		// x coefficient
		try
		{
			try
			{
				double value = xCoeffField.getValue();
				if (value == 0.0)
					throw new AppException(ErrorId.X_COORDINATE_OUT_OF_BOUNDS);
			}
			catch (NumberFormatException e)
			{
				throw new AppException(ErrorId.INVALID_X_COORDINATE);
			}
		}
		catch (AppException e)
		{
			GuiUtils.setFocus(xCoeffField);
			throw e;
		}

		// y coefficient
		try
		{
			try
			{
				double value = yCoeffField.getValue();
				if (value == 0.0)
					throw new AppException(ErrorId.Y_COORDINATE_OUT_OF_BOUNDS);
			}
			catch (NumberFormatException e)
			{
				throw new AppException(ErrorId.INVALID_Y_COORDINATE);
			}
		}
		catch (AppException e)
		{
			GuiUtils.setFocus(yCoeffField);
			throw e;
		}
	}

	//------------------------------------------------------------------

	private void onAccept()
	{
		try
		{
			validateUserInput();
			accepted = true;
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

		INVALID_X_COORDINATE
		("The x coordinate is invalid."),

		INVALID_Y_COORDINATE
		("The y coordinate is invalid."),

		X_COORDINATE_OUT_OF_BOUNDS
		("The x coordinate must be greater than zero."),

		Y_COORDINATE_OUT_OF_BOUNDS
		("The y coordinate must be greater than zero.");

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

		@Override
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


	// NODE VALUE FIELD CLASS


	private static class NodeValueField
		extends JTextField
		implements ChangeListener
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	NUM_COLUMNS	= 11;

		private static final	int	VERTICAL_MARGIN		= 1;
		private static final	int	HORIZONTAL_MARGIN	= 3;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private NodeValueField()
		{
			// Call superclass constructor
			super(NUM_COLUMNS);

			// Set properties
			AppFont.TEXT_FIELD.apply(this);
			GuiUtils.setTextComponentMargins(this, VERTICAL_MARGIN, HORIZONTAL_MARGIN);
			setEditable(false);
			setFocusable(false);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ChangeListener interface
	////////////////////////////////////////////////////////////////////

		public void stateChanged(ChangeEvent event)
		{
			EnvelopeView envelopeView = (EnvelopeView)event.getSource();
			NodeId id = envelopeView.getSelectedNodeId();
			if (id == null)
				setText(null);
			else
			{
				SimpleViewNode node = envelopeView.getNode(id);
				setText(AppConstants.FORMAT_1_3F.format(node.x) + ", " + AppConstants.FORMAT_1_2F.format(node.y));
			}
			setBackground(envelopeView.getXScale().getBackground());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected int getColumnWidth()
		{
			return FontUtils.getCharWidth('0', getFontMetrics(getFont()));
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// NODE X COORDINATE SPINNER CLASS


	public static class NodeXSpinner
		extends FDoubleSpinner
		implements EnvelopeNodeValueDialog.INodeValueEditor
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	FIELD_LENGTH	= 5;

		private static final	double	DELTA	= 0.001;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public NodeXSpinner()
		{
			super(MIN_NODE_X, MIN_NODE_X, MAX_NODE_X, DELTA, FIELD_LENGTH, AppConstants.FORMAT_1_3);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : INodeValueEditor interface
	////////////////////////////////////////////////////////////////////

		/**
		 * @throws NumberFormatException
		 */

		public double getNodeValue()
		{
			return getDoubleValue();
		}

		//--------------------------------------------------------------

		public void setNodeValue(double value)
		{
			setDoubleValue(value);
		}

		//--------------------------------------------------------------

		public void setMinimumValue(double minValue)
		{
			setMinimum(minValue);
		}

		//--------------------------------------------------------------

		public void setMaximumValue(double maxValue)
		{
			setMaximum(maxValue);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// NODE Y COORDINATE SPINNER CLASS


	public static class NodeYSpinner
		extends FDoubleSpinner
		implements EnvelopeNodeValueDialog.INodeValueEditor
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	FIELD_LENGTH	= 4;

		private static final	double	DELTA	= 0.01;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public NodeYSpinner()
		{
			super(MIN_NODE_Y, MIN_NODE_Y, MAX_NODE_Y, DELTA, FIELD_LENGTH, AppConstants.FORMAT_1_2);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : INodeValueEditor interface
	////////////////////////////////////////////////////////////////////

		/**
		 * @throws NumberFormatException
		 */

		public double getNodeValue()
		{
			return getDoubleValue();
		}

		//--------------------------------------------------------------

		public void setNodeValue(double value)
		{
			setDoubleValue(value);
		}

		//--------------------------------------------------------------

		public void setMinimumValue(double minValue)
		{
			setMinimum(minValue);
		}

		//--------------------------------------------------------------

		public void setMaximumValue(double maxValue)
		{
			setMaximum(maxValue);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// ENVELOPE VIEW CLASS


	private static class EnvelopeView
		extends uk.blankaspect.ui.swing.envelope.EnvelopeView<SimpleViewNode, SimpleViewEnvelope>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	X_DIV_WIDTH	= 50;
		private static final	int	NUM_X_DIVS	= 10;

		private static final	int	Y_DIV_HEIGHT	= 20;
		private static final	int	NUM_Y_DIVS		= 5;

		private static final	int	PLOT_WIDTH	= NUM_X_DIVS * X_DIV_WIDTH + 1;
		private static final	int	PLOT_HEIGHT	= NUM_Y_DIVS * Y_DIV_HEIGHT + 1;

		private static final	int	VIEW_WIDTH	= PLOT_WIDTH + 3;

		private static final	String[]	ENVELOPE_VERTICAL_SCALE_STRS	=
		{
			"1.0", "0.8", "0.6", "0.4", "0.2", "0"
		};

		private static final	NumberFormat	X_SCALE_FORMAT	= new DecimalFormat("0.#");

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private EnvelopeView()
		{
			super(PLOT_WIDTH, PLOT_HEIGHT, VIEW_WIDTH, 0, X_DIV_WIDTH, 0, Y_DIV_HEIGHT, NUM_Y_DIVS,
				  ENVELOPE_VERTICAL_SCALE_STRS, AppFont.SCALE.getFont());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public JSpinner createXSpinner()
		{
			return new NodeXSpinner();
		}

		//--------------------------------------------------------------

		@Override
		public JSpinner createYSpinner()
		{
			return new NodeYSpinner();
		}

		//--------------------------------------------------------------

		@Override
		protected String getXScaleString(int index)
		{
			return (getHorizontalDivOffset() + index * getHorizontalDivWidth() <= getPlotWidth())
					? X_SCALE_FORMAT.format((double)index * 0.1)
					: null;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// COEFFICIENT FIELD CLASS


	private static class CoefficientField
		extends DoubleValueField
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	NUM_COLUMNS	= 12;

		private static final	String	VALID_CHARS	= ".0123456789";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CoefficientField(double value)
		{
			super(0, NUM_COLUMNS);
			AppFont.TEXT_FIELD.apply(this);
			GuiUtils.setTextComponentMargins(this);
			setValue(value);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * @throws NumberFormatException
		 */

		@Override
		public double getValue()
		{
			return Double.parseDouble(getText());
		}

		//--------------------------------------------------------------

		/**
		 * @throws IllegalArgumentException
		 */

		@Override
		public void setValue(double value)
		{
			setText(Double.toString(value));
		}

		//--------------------------------------------------------------

		@Override
		protected boolean acceptCharacter(char ch,
										  int  index)
		{
			return (VALID_CHARS.indexOf(ch) >= 0);
		}

		//--------------------------------------------------------------

		@Override
		protected int getColumnWidth()
		{
			return FontUtils.getCharWidth('0', getFontMetrics(getFont()));
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
