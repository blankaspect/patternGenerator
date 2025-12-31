/*====================================================================*\

ExportImageSequenceDialog.java

Export image sequence dialog class.

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

import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.FileException;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.container.DimensionsSpinnerPanel;
import uk.blankaspect.ui.swing.container.PathnamePanel;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.spinner.FIntegerSpinner;

import uk.blankaspect.ui.swing.textfield.FTextField;

import uk.blankaspect.ui.swing.workaround.LinuxWorkarounds;

//----------------------------------------------------------------------


// EXPORT IMAGE SEQUENCE DIALOG CLASS


class ExportImageSequenceDialog
	extends JDialog
	implements ActionListener, ChangeListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		FILENAME_STEM_FIELD_LENGTH	= 24;
	private static final	int		FRAME_WIDTH_FIELD_LENGTH	= 4;
	private static final	int		FRAME_HEIGHT_FIELD_LENGTH	= 4;
	private static final	int		START_FRAME_FIELD_LENGTH	= 10;
	private static final	int		NUM_FRAMES_FIELD_LENGTH		= 6;
	private static final	int		FADE_IN_FIELD_LENGTH		= 4;
	private static final	int		FADE_OUT_FIELD_LENGTH		= FADE_IN_FIELD_LENGTH;

	private static final	Insets	IMAGE_BUTTON_MARGINS	= new Insets(2, 6, 2, 6);

	private static final	String	TITLE_STR				= "Export image sequence";
	private static final	String	DIRECTORY_STR			= "Directory";
	private static final	String	FILENAME_STEM_STR		= "Filename stem";
	private static final	String	FRAME_SIZE_STR			= "Frame size";
	private static final	String	IMAGE_STR				= "Image";
	private static final	String	FIRST_FRAME_STR			= "First frame";
	private static final	String	NUM_FRAMES_STR			= "Number of frames";
	private static final	String	FADE_IN_STR				= "Fade in";
	private static final	String	FADE_OUT_STR			= "Fade out";
	private static final	String	IMAGE_SEQ_DIRECTORY_STR	= "Image-sequence directory";
	private static final	String	SELECT_STR				= "Select";
	private static final	String	SELECT_DIRECTORY_STR	= "Select directory";
	private static final	String	IMAGE_TOOLTIP_STR		= "Set frame size to image size";

	// Commands
	private interface Command
	{
		String	CHOOSE_DIRECTORY			= "chooseDirectory";
		String	SET_FRAME_SIZE_FROM_IMAGE	= "setFrameSizeFromImage";
		String	ACCEPT						= "accept";
		String	CLOSE						= "close";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Point			location;
	private static	JFileChooser	directoryChooser;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	boolean					accepted;
	private	int						imageWidth;
	private	int						imageHeight;
	private	FPathnameField			directoryField;
	private	FTextField				filenameStemField;
	private	DimensionsSpinnerPanel	frameSizePanel;
	private	FIntegerSpinner			startFrameSpinner;
	private	FIntegerSpinner			numFramesSpinner;
	private	FIntegerSpinner			fadeInSpinner;
	private	FIntegerSpinner			fadeOutSpinner;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		directoryChooser = new JFileChooser();
		directoryChooser.setDialogTitle(IMAGE_SEQ_DIRECTORY_STR);
		directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		directoryChooser.setApproveButtonMnemonic(KeyEvent.VK_S);
		directoryChooser.setApproveButtonToolTipText(SELECT_DIRECTORY_STR);
		directoryChooser.setSelectedFile(AppConfig.INSTANCE.getExportImageSequenceDirectory());
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private ExportImageSequenceDialog(Window              owner,
									  int                 imageWidth,
									  int                 imageHeight,
									  ImageSequenceParams params)
	{
		// Call superclass constructor
		super(owner, TITLE_STR, ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());

		// Initialise instance variables
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;


		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		// Label: directory
		JLabel directoryLabel = new FLabel(DIRECTORY_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(directoryLabel, gbc);
		controlPanel.add(directoryLabel);

		// Panel: directory
		directoryField = new FPathnameField(params.directory);
		JPanel directoryPanel = new PathnamePanel(directoryField, Command.CHOOSE_DIRECTORY, this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(directoryPanel, gbc);
		controlPanel.add(directoryPanel);

		// Label: filename stem
		JLabel filenameStemLabel = new FLabel(FILENAME_STEM_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(filenameStemLabel, gbc);
		controlPanel.add(filenameStemLabel);

		// Field: filename stem
		filenameStemField = new FTextField(params.filenameStem, FILENAME_STEM_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(filenameStemField, gbc);
		controlPanel.add(filenameStemField);

		// Label: frame size
		JLabel frameSizeLabel = new FLabel(FRAME_SIZE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(frameSizeLabel, gbc);
		controlPanel.add(frameSizeLabel);

		// Panel: frame size, outer
		JPanel frameSizeOuterPanel = new JPanel(gridBag);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(frameSizeOuterPanel, gbc);
		controlPanel.add(frameSizeOuterPanel);

		// Panel: frame size
		frameSizePanel = new DimensionsSpinnerPanel(Math.max(imageWidth, params.frameWidth), imageWidth,
													ImageSequenceParams.MAX_FRAME_WIDTH,
													FRAME_WIDTH_FIELD_LENGTH,
													Math.max(imageHeight, params.frameHeight),
													imageHeight, ImageSequenceParams.MAX_FRAME_HEIGHT,
													FRAME_HEIGHT_FIELD_LENGTH, null);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(frameSizePanel, gbc);
		frameSizeOuterPanel.add(frameSizePanel);

		// Button: image
		JButton imageButton = new FButton(IMAGE_STR);
		imageButton.setMargin(IMAGE_BUTTON_MARGINS);
		imageButton.setToolTipText(IMAGE_TOOLTIP_STR);
		imageButton.setActionCommand(Command.SET_FRAME_SIZE_FROM_IMAGE);
		imageButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 8, 0, 0);
		gridBag.setConstraints(imageButton, gbc);
		frameSizeOuterPanel.add(imageButton);

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
		startFrameSpinner = new FIntegerSpinner(params.startFrameIndex,
												ImageSequenceParams.MIN_FRAME_INDEX,
												ImageSequenceParams.MAX_FRAME_INDEX,
												START_FRAME_FIELD_LENGTH);

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
		numFramesSpinner = new FIntegerSpinner(params.numFrames, ImageSequenceParams.MIN_NUM_FRAMES,
											   ImageSequenceParams.MAX_NUM_FRAMES,
											   NUM_FRAMES_FIELD_LENGTH);
		numFramesSpinner.addChangeListener(this);

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

		// Label: fade in
		JLabel fadeInLabel = new FLabel(FADE_IN_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(fadeInLabel, gbc);
		controlPanel.add(fadeInLabel);

		// Panel: fade
		JPanel fadePanel = new JPanel(gridBag);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(fadePanel, gbc);
		controlPanel.add(fadePanel);

		// Spinner: fade in
		fadeInSpinner = new FIntegerSpinner(params.fadeIn, ImageSequenceParams.MIN_FADE_IN,
											params.numFrames, FADE_IN_FIELD_LENGTH);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(fadeInSpinner, gbc);
		fadePanel.add(fadeInSpinner);

		// Label: fade out
		JLabel fadeOutLabel = new FLabel(FADE_OUT_STR);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 16, 0, 0);
		gridBag.setConstraints(fadeOutLabel, gbc);
		fadePanel.add(fadeOutLabel);

		// Spinner: fade out
		fadeOutSpinner = new FIntegerSpinner(params.fadeOut, ImageSequenceParams.MIN_FADE_OUT,
											 params.numFrames, FADE_OUT_FIELD_LENGTH);

		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 6, 0, 0);
		gridBag.setConstraints(fadeOutSpinner, gbc);
		fadePanel.add(fadeOutSpinner);


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
		mainPanel.setTransferHandler(directoryField.getTransferHandler());

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

	public static ImageSequenceParams showDialog(Component           parent,
												 int                 imageWidth,
												 int                 imageHeight,
												 ImageSequenceParams params)
	{
		return new ExportImageSequenceDialog(GuiUtils.getWindow(parent), imageWidth, imageHeight,
											 params).getParameters();
	}

	//------------------------------------------------------------------

	public static File getSelectedDirectory()
	{
		return directoryChooser.getSelectedFile();
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
			case Command.CHOOSE_DIRECTORY          -> onChooseDirectory();
			case Command.SET_FRAME_SIZE_FROM_IMAGE -> onSetFrameSizeFromImage();
			case Command.ACCEPT                    -> onAccept();
			case Command.CLOSE                     -> onClose();
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ChangeListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void stateChanged(ChangeEvent event)
	{
		Object eventSource = event.getSource();

		if (eventSource == numFramesSpinner)
			updateFade();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private ImageSequenceParams getParameters()
	{
		return accepted
				? new ImageSequenceParams(directoryField.getFile(),
										  filenameStemField.getText(),
										  frameSizePanel.getValue1(),
										  frameSizePanel.getValue2(),
										  startFrameSpinner.getIntValue(),
										  numFramesSpinner.getIntValue(),
										  fadeInSpinner.getIntValue(),
										  fadeOutSpinner.getIntValue())
				: null;
	}

	//------------------------------------------------------------------

	private void updateFade()
	{
		int numFrames = numFramesSpinner.getIntValue();
		fadeInSpinner.setMaximum(numFrames);
		fadeOutSpinner.setMaximum(numFrames);
	}

	//------------------------------------------------------------------

	private void validateUserInput()
		throws AppException
	{
		// Directory
		try
		{
			if (directoryField.isEmpty())
				throw new AppException(ErrorId.NO_DIRECTORY);
			File directory = directoryField.getFile();
			if (directory.exists() && !directory.isDirectory())
				throw new FileException(ErrorId.NOT_A_DIRECTORY, directory);
		}
		catch (AppException e)
		{
			GuiUtils.setFocus(directoryField);
			throw e;
		}

		// Filename stem
		try
		{
			if (filenameStemField.isEmpty())
				throw new AppException(ErrorId.NO_FILENAME_STEM);
		}
		catch (AppException e)
		{
			GuiUtils.setFocus(filenameStemField);
			throw e;
		}
	}

	//------------------------------------------------------------------

	private void onChooseDirectory()
	{
		if (!directoryField.isEmpty())
			directoryChooser.setSelectedFile(directoryField.getCanonicalFile());
		directoryChooser.rescanCurrentDirectory();
		if (directoryChooser.showDialog(this, SELECT_STR) == JFileChooser.APPROVE_OPTION)
			directoryField.setFile(directoryChooser.getSelectedFile());
	}

	//------------------------------------------------------------------

	private void onSetFrameSizeFromImage()
	{
		frameSizePanel.setValue1(imageWidth);
		frameSizePanel.setValue2(imageHeight);
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

		NO_DIRECTORY
		("No directory was specified."),

		NOT_A_DIRECTORY
		("The pathname does not denote a directory."),

		NO_FILENAME_STEM
		("No filename stem was specified.");

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

}

//----------------------------------------------------------------------
