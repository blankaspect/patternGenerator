/*====================================================================*\

Pattern1AnimationParamsDialog.java

Pattern1 animation parameters dialog box class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
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

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.gui.FButton;
import uk.blankaspect.common.gui.FDoubleSpinner;
import uk.blankaspect.common.gui.FLabel;
import uk.blankaspect.common.gui.GuiUtils;
import uk.blankaspect.common.gui.TitledBorder;
import uk.blankaspect.common.gui.UnsignedIntegerComboBox;

import uk.blankaspect.common.misc.KeyAction;

//----------------------------------------------------------------------


// PATTERN 1 ANIMATION PARAMETERS DIALOG BOX CLASS


class Pattern1AnimationParamsDialog
	extends JDialog
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	MAX_NUM_START_FRAMES	= 32;

	private static final	int	RATE_FIELD_LENGTH			= 6;
	private static final	int	START_FRAME_FIELD_LENGTH	= 10;

	private static final	double	DELTA_RATE	= 0.01;

	private static final	String	TITLE_STR			= "Animation parameters";
	private static final	String	ANIMATION_KINDS_STR	= "Animation kinds";
	private static final	String	RATE_STR			= "Rate";
	private static final	String	FIRST_FRAME_STR		= "First frame";
	private static final	String	FPS_STR				= "fps";

	// Commands
	private interface Command
	{
		String	ACCEPT	= "accept";
		String	CLOSE	= "close";
	}

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

		NO_START_FRAME
		("No first frame was specified.");

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

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	String	message;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// ANIMATION KINDS PANEL


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
			updateAcceptButton();
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private Pattern1AnimationParamsDialog(Window                           owner,
										  Set<Pattern1Image.AnimationKind> enabledAnimationKinds,
										  Set<Pattern1Image.AnimationKind> selectedAnimationKinds)
	{

		// Call superclass constructor
		super(owner, TITLE_STR, Dialog.ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());


		//----  Animation kinds panel

		JPanel animationKindsOuterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		TitledBorder.setPaddedBorder(animationKindsOuterPanel, ANIMATION_KINDS_STR);

		animationKindsPanel = new AnimationKindsPanel(enabledAnimationKinds, selectedAnimationKinds);
		animationKindsOuterPanel.add(animationKindsPanel);


		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		// Label: rate
		JLabel rateLabel = new FLabel(RATE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(rateLabel, gbc);
		controlPanel.add(rateLabel);

		// Panel: rate
		JPanel ratePanel = new JPanel(gridBag);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(ratePanel, gbc);
		controlPanel.add(ratePanel);

		// Spinner: rate
		rateSpinner = new FDoubleSpinner(rate, Pattern1Document.MIN_ANIMATION_RATE,
										 Pattern1Document.MAX_ANIMATION_RATE, DELTA_RATE,
										 RATE_FIELD_LENGTH, AppConstants.FORMAT_1_2);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(rateSpinner, gbc);
		ratePanel.add(rateSpinner);

		// Label: fps
		JLabel fpsLabel = new FLabel(FPS_STR);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 4, 0, 0);
		gridBag.setConstraints(fpsLabel, gbc);
		ratePanel.add(fpsLabel);

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

		// Combo box: start frame
		startFrameComboBox = new UnsignedIntegerComboBox(START_FRAME_FIELD_LENGTH, MAX_NUM_START_FRAMES,
														 startFrameIndices, startFrameIndex);
		startFrameComboBox.setDefaultComparator();

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(startFrameComboBox, gbc);
		controlPanel.add(startFrameComboBox);


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
		gridBag.setConstraints(animationKindsOuterPanel, gbc);
		mainPanel.add(animationKindsOuterPanel);

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
		updateAcceptButton();


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

		// Set default button
		getRootPane().setDefaultButton(okButton);

		// Show dialog
		setVisible(true);

	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static PatternDocument.AnimationParams
									showDialog(Component                        parent,
											   Set<Pattern1Image.AnimationKind> enabledAnimationKinds,
											   Set<Pattern1Image.AnimationKind> selectedAnimationKinds)
	{
		return new Pattern1AnimationParamsDialog(GuiUtils.getWindow(parent), enabledAnimationKinds,
												 selectedAnimationKinds).getAnimationParams();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
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

	public PatternDocument.AnimationParams getAnimationParams()
	{
		return (accepted ? new PatternDocument.AnimationParams(Pattern1Image.AnimationKind.
																			setToBitField(animationKinds),
															   rate, startFrameIndex)
						 : null);
	}

	//------------------------------------------------------------------

	private void updateAcceptButton()
	{
		okButton.setEnabled(!animationKindsPanel.getAnimationKinds().isEmpty());
	}

	//------------------------------------------------------------------

	private void validateUserInput()
		throws AppException
	{
		// Start frame
		if (startFrameComboBox.isEmpty())
		{
			GuiUtils.setFocus(startFrameComboBox);
			throw new AppException(ErrorId.NO_START_FRAME);
		}
	}

	//------------------------------------------------------------------

	private void onAccept()
	{
		try
		{
			// Validate user input
			validateUserInput();

			// Update class fields
			animationKinds = animationKindsPanel.getAnimationKinds();
			rate = rateSpinner.getDoubleValue();
			AppConfig.INSTANCE.setAnimationRate(rate);
			startFrameIndices = startFrameComboBox.getItems(true);
			startFrameIndex = startFrameComboBox.getValue();

			// Close dialog
			accepted = true;
			onClose();
		}
		catch (AppException e)
		{
			JOptionPane.showMessageDialog(this, e, App.SHORT_NAME, JOptionPane.ERROR_MESSAGE);
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
//  Class fields
////////////////////////////////////////////////////////////////////////

	private static	Point								location;
	private static	Set<Pattern1Image.AnimationKind>	animationKinds		=
													EnumSet.noneOf(Pattern1Image.AnimationKind.class);
	private static	double								rate				=
													AppConfig.INSTANCE.getAnimationRate();
	private static	List<Integer>						startFrameIndices	=
													Collections.singletonList(0);
	private static	int									startFrameIndex;

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	private	boolean					accepted;
	private	AnimationKindsPanel		animationKindsPanel;
	private	FDoubleSpinner			rateSpinner;
	private	UnsignedIntegerComboBox	startFrameComboBox;
	private	JButton					okButton;

}

//----------------------------------------------------------------------
