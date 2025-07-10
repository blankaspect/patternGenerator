/*====================================================================*\

EnvelopeNodeValueDialog.java

Class: envelope-node value dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.envelope;

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
import java.awt.event.WindowEvent;

import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import uk.blankaspect.common.envelope.AbstractNode;
import uk.blankaspect.common.envelope.CompoundNode;
import uk.blankaspect.common.envelope.NodeId;
import uk.blankaspect.common.envelope.SimpleNode;

import uk.blankaspect.common.geometry.Point2D;

import uk.blankaspect.common.range.DoubleRange;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.spinner.DoubleSpinner;

//----------------------------------------------------------------------


// CLASS: ENVELOPE-NODE VALUE DIALOG


public class EnvelopeNodeValueDialog
	extends JDialog
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	Insets	BUTTON_MARGINS	= new Insets(2, 4, 2, 4);

	private static final	String	X_STR	= "x";
	private static final	String	Y_STR	= "y";

	// Commands
	private interface Command
	{
		String	ACCEPT	= "accept";
		String	CLOSE	= "close";
	}

	private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
	{
		new KeyAction.KeyCommandPair(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), Command.CLOSE)
		};

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	boolean		accepted;
	private	JSpinner	xSpinner;
	private	JSpinner	ySpinner;
	private	DoubleRange	xRange;
	private	DoubleRange	yRange;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private EnvelopeNodeValueDialog(
		Window				owner,
		EnvelopeView<?, ?>	envelopeView,
		NodeId				nodeId)
	{
		// Call superclass constructor
		super(owner, ModalityType.APPLICATION_MODAL);

		// Initialise instance variables
		xRange = envelopeView.getNodeXMinMax(nodeId);
		yRange = envelopeView.getNodeYMinMax(nodeId);


		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);

		int gridY = 0;

		// Label: x
		JLabel xLabel = new FLabel(X_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = Constants.COMPONENT_INSETS;
		gridBag.setConstraints(xLabel, gbc);
		controlPanel.add(xLabel);

		// Spinner: x
		AbstractNode node = envelopeView.getNode(nodeId);

		xSpinner = envelopeView.createXSpinner();
		if (xSpinner instanceof INodeValueEditor xEditor)
		{
			xEditor.setMinimumValue(xRange.lowerBound);
			xEditor.setMaximumValue(xRange.upperBound);
			xEditor.setNodeValue(node.x);
		}
		xSpinner.setEnabled(!node.fixedX);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = Constants.COMPONENT_INSETS;
		gridBag.setConstraints(xSpinner, gbc);
		controlPanel.add(xSpinner);

		// Label: y
		JLabel yLabel = new FLabel(Y_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = Constants.COMPONENT_INSETS;
		gridBag.setConstraints(yLabel, gbc);
		controlPanel.add(yLabel);

		// Spinner: y
		double y = 0.0;
		boolean enabled = false;
		if (node instanceof SimpleNode)
		{
			SimpleNode n = (SimpleNode)node;
			y = n.y;
			enabled = !n.fixedY;
		}
		else if (node instanceof CompoundNode)
		{
			CompoundNode n = (CompoundNode)node;
			y = n.ys[nodeId.bandIndex];
			enabled = !n.isFixedY(nodeId.bandIndex);
		}

		ySpinner = envelopeView.createYSpinner();
		if (ySpinner instanceof INodeValueEditor yEditor)
		{
			yEditor.setMinimumValue(yRange.lowerBound);
			yEditor.setMaximumValue(yRange.upperBound);
			yEditor.setNodeValue(y);
		}
		ySpinner.setEnabled(enabled);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = Constants.COMPONENT_INSETS;
		gridBag.setConstraints(ySpinner, gbc);
		controlPanel.add(ySpinner);


		//----  Button panel

		JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 4, 0));

		// Button: OK
		JButton okButton = new FButton(Constants.OK_STR);
		okButton.setMargin(BUTTON_MARGINS);
		okButton.setActionCommand(Command.ACCEPT);
		okButton.addActionListener(this);
		buttonPanel.add(okButton);

		// Button: cancel
		JButton cancelButton = new FButton(Constants.CANCEL_STR);
		cancelButton.setMargin(BUTTON_MARGINS);
		cancelButton.setActionCommand(Command.CLOSE);
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);


		//----  Main panel

		JPanel mainPanel = new JPanel(gridBag);
		GuiUtils.setPaddedRaisedBevelBorder(mainPanel, 3, 4, 4, 4);

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
		gbc.insets = new Insets(4, 0, 0, 0);
		gridBag.setConstraints(buttonPanel, gbc);
		mainPanel.add(buttonPanel);

		// Add commands to action map
		KeyAction.create(mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this, KEY_COMMANDS);


		//----  Window

		// Set content pane
		setContentPane(mainPanel);

		// Omit frame from dialog
		setUndecorated(true);

		// Dispose of window when it is closed
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// Prevent dialog from being resized
		setResizable(false);

		// Resize dialog to its preferred size
		pack();

		// Set location of dialog
		Point point = envelopeView.nodeToPoint(nodeId);
		Point location = new Point(point.x + 1, point.y + 1);
		SwingUtilities.convertPointToScreen(location, envelopeView);
		setLocation(GuiUtils.getComponentLocation(this, location));

		// Set default button
		getRootPane().setDefaultButton(okButton);

		// Set focus
		if (xSpinner.isEnabled())
			xSpinner.requestFocusInWindow();
		else
			ySpinner.requestFocusInWindow();

		// Show dialog
		setVisible(true);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Point2D showDialog(
		Component			parent,
		EnvelopeView<?, ?>	envelopeView,
		NodeId				nodeId)
	{
		return new EnvelopeNodeValueDialog(GuiUtils.getWindow(parent), envelopeView, nodeId).getNodeLocation();
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

		if (command.equals(Command.ACCEPT))
			onAccept();

		else if (command.equals(Command.CLOSE))
			onClose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private Point2D getNodeLocation()
	{
		return (accepted && (xSpinner instanceof INodeValueEditor xEditor)
				&& (ySpinner instanceof INodeValueEditor yEditor))
						? new Point2D( xEditor.getNodeValue(), yEditor.getNodeValue())
						: null;
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
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member interfaces
////////////////////////////////////////////////////////////////////////


	// INTERFACE: NODE-VALUE EDITOR


	public interface INodeValueEditor
	{

	////////////////////////////////////////////////////////////////////
	//  Methods
	////////////////////////////////////////////////////////////////////

		double getNodeValue();

		//--------------------------------------------------------------

		void setNodeValue(
			double	value);

		//--------------------------------------------------------------

		void setMinimumValue(
			double	value);

		//--------------------------------------------------------------

		void setMaximumValue(
			double	value);

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: COORDINATE SPINNER


	public static class CoordinateSpinner
		extends DoubleSpinner
		implements INodeValueEditor
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public CoordinateSpinner(
			double			stepSize,
			int				maxLength,
			NumberFormat	format)

		{
			super(0.0, 0.0, 1.0, stepSize, maxLength, format);
			FontUtils.setAppFont(Constants.FontKey.TEXT_FIELD, this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : INodeValueEditor interface
	////////////////////////////////////////////////////////////////////

		@Override
		public double getNodeValue()
		{
			return getDoubleValue();
		}

		//--------------------------------------------------------------

		@Override
		public void setNodeValue(
			double	value)
		{
			setDoubleValue(value);
		}

		//--------------------------------------------------------------

		@Override
		public void setMinimumValue(
			double	value)
		{
			setMinimum(value);
		}

		//--------------------------------------------------------------

		@Override
		public void setMaximumValue(
			double	value)
		{
			setMaximum(value);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
