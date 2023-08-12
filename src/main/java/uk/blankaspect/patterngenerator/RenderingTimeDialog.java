/*====================================================================*\

RenderingTimeDialog.java

Image rendering time dialog class.

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
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.colour.Colours;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.text.TextRendering;

//----------------------------------------------------------------------


// IMAGE RENDERING TIME DIALOG CLASS


class RenderingTimeDialog
	extends JDialog
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	TITLE_STR			= "Image rendering time";
	private static final	String	RENDERING_TIME_STR	= "Rendering time";
	private static final	String	NS_PER_PIXEL_STR	= "nanoseconds/pixel";
	private static final	String	RESET_STR			= "Reset";

	private static final	String	TIME_PROTOTYPE_STR	= "0000000.0";

	// Commands
	private interface Command
	{
		String	RESET	= "reset";
		String	CLOSE	= "close";
	}

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// TIME FIELD CLASS


	private static class TimeField
		extends JComponent
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	VERTICAL_MARGIN		= 3;
		private static final	int	HORIZONTAL_MARGIN	= 6;

		private static final	Color	TEXT_COLOUR			= Color.BLACK;
		private static final	Color	BACKGROUND_COLOUR	= new Color(248, 236, 192);
		private static final	Color	BORDER_COLOUR		= Colours.LINE_BORDER;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private TimeField(String prototypeStr)
		{
			// Initialise instance variables
			AppFont.TEXT_FIELD.apply(this);
			FontMetrics fontMetrics = getFontMetrics(getFont());
			preferredWidth = 2 * HORIZONTAL_MARGIN + fontMetrics.stringWidth(prototypeStr);
			preferredHeight = 2 * VERTICAL_MARGIN + fontMetrics.getAscent() + fontMetrics.getDescent();

			// Set attributes
			setEnabled(false);
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
			return new Dimension(preferredWidth, preferredHeight);
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Create copy of graphics context
			gr = gr.create();

			// Get dimensions
			int width = getWidth();
			int height = getHeight();

			// Draw background
			gr.setColor(BACKGROUND_COLOUR);
			gr.fillRect(0, 0, width, height);

			// Draw text
			if (text != null)
			{
				// Set rendering hints for text antialiasing and fractional metrics
				TextRendering.setHints((Graphics2D)gr);

				// Draw text
				FontMetrics fontMetrics = gr.getFontMetrics();
				gr.setColor(TEXT_COLOUR);
				gr.drawString(text, width - HORIZONTAL_MARGIN - fontMetrics.stringWidth(text),
							  VERTICAL_MARGIN + fontMetrics.getAscent());
			}

			// Draw border
			gr.setColor(BORDER_COLOUR);
			gr.drawRect(0, 0, width - 1, height - 1);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void setText(String text)
		{
			if (!Objects.equals(text, this.text))
			{
				this.text = text;
				repaint();
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int		preferredWidth;
		private	int		preferredHeight;
		private	String	text;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private RenderingTimeDialog(Window owner)
	{

		// Call superclass constructor
		super(owner, TITLE_STR);

		// Set icons
		setIconImages(owner.getIconImages());


		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		// Label: rendering time
		JLabel renderingTimeLabel = new FLabel(RENDERING_TIME_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(renderingTimeLabel, gbc);
		controlPanel.add(renderingTimeLabel);

		// Panel: rendering time
		JPanel renderingTimePanel = new JPanel(gridBag);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(renderingTimePanel, gbc);
		controlPanel.add(renderingTimePanel);

		// Field: rendering time
		renderingTimeField = new TimeField(TIME_PROTOTYPE_STR);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(renderingTimeField, gbc);
		renderingTimePanel.add(renderingTimeField);

		// Label: nanoseconds per pixel
		JLabel nsPerPixelLabel = new FLabel(NS_PER_PIXEL_STR);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 4, 0, 0);
		gridBag.setConstraints(nsPerPixelLabel, gbc);
		renderingTimePanel.add(nsPerPixelLabel);


		//----  Button panel

		JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 8, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

		// Button: reset
		JButton resetButton = new FButton(RESET_STR);
		resetButton.setActionCommand(Command.RESET);
		resetButton.addActionListener(this);
		buttonPanel.add(resetButton);

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

	public static RenderingTimeDialog showDialog(Component parent)
	{
		return new RenderingTimeDialog(GuiUtils.getWindow(parent));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.RESET))
			onReset();

		else if (command.equals(Command.CLOSE))
			onClose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public void updateRenderingTime()
	{
		updateComponents();
	}

	//------------------------------------------------------------------

	public void close()
	{
		location = getLocation();
		setVisible(false);
		dispose();
	}

	//------------------------------------------------------------------

	private void updateComponents()
	{
		double time = 0.0;
		PatternDocument document = App.INSTANCE.getDocument();
		if (document != null)
			time = document.getRenderingTime();
		renderingTimeField.setText((time == 0.0) ? null : AppConstants.FORMAT_1_1F.format(time));
	}

	//------------------------------------------------------------------

	private void onReset()
	{
		PatternDocument document = App.INSTANCE.getDocument();
		if (document != null)
			document.resetRenderingTime();
	}

	//------------------------------------------------------------------

	private void onClose()
	{
		PatternDocument.closeRenderingTimeDialog();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Point	location;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	TimeField	renderingTimeField;

}

//----------------------------------------------------------------------
