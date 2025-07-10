/*====================================================================*\

HueSaturationPanel.java

Class: hue and saturation panel.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.blankaspect.ui.swing.colour.Colours;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.spinner.FIntegerSpinner;

//----------------------------------------------------------------------


// CLASS: HUE AND SATURATION PANEL


class HueSaturationPanel
	extends JPanel
	implements ChangeListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int	HUE_RANGE	= 360;

	private static final	int	MIN_HUE	= 0;
	private static final	int	MAX_HUE	= HUE_RANGE - 1;

	private static final	int	MIN_SATURATION	= 0;
	private static final	int	MAX_SATURATION	= 100;

	private static final	int	HUE_FIELD_LENGTH		= 3;
	private static final	int	SATURATION_FIELD_LENGTH	= 3;

	private static final	String	HUE_STR			= "H";
	private static final	String	SATURATION_STR	= "S";

	private static final	Color	COLOUR_PANEL_BORDER_COLOUR			= Colours.LINE_BORDER;
	private static final	Color	COLOUR_PANEL_CROSSHAIR_COLOUR		= Color.BLACK;
	private static final	Color	COLOUR_PANEL_CROSSHAIR_XOR_COLOUR	= Color.WHITE;
	private static final	Color	COLOUR_PANEL_MARKER_COLOUR			= Color.DARK_GRAY;

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: COLOUR PANEL


	private class ColourPanel
		extends JComponent
		implements MouseListener, MouseMotionListener
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	COLOUR_AREA_WIDTH		= HUE_RANGE / 2;
		private static final	int	COLOUR_AREA_HEIGHT		= MAX_SATURATION - MIN_SATURATION + 1;
		private static final	int	BORDER_WIDTH			= 1;
		private static final	int	CROSSHAIR_ARM_LENGTH	= 4;
		private static final	int	CROSSHAIR_LENGTH		= 2 * CROSSHAIR_ARM_LENGTH + 1;
		private static final	int	MARKER_LENGTH			= CROSSHAIR_ARM_LENGTH + 1;
		private static final	int	LEFT_MARGIN				= MARKER_LENGTH;
		private static final	int	RIGHT_MARGIN			= CROSSHAIR_ARM_LENGTH - BORDER_WIDTH;
		private static final	int	TOP_MARGIN				= MARKER_LENGTH;
		private static final	int	BOTTOM_MARGIN			= CROSSHAIR_ARM_LENGTH - BORDER_WIDTH;
		private static final	int	COLOUR_AREA_X			= LEFT_MARGIN + BORDER_WIDTH;
		private static final	int	COLOUR_AREA_Y			= TOP_MARGIN + BORDER_WIDTH;

		private static final	double	H_FACTOR	= 1.0 / (double)COLOUR_AREA_WIDTH;
		private static final	double	S_FACTOR	= 1.0 / (double)(COLOUR_AREA_HEIGHT - 1);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int	hue;
		private	int	saturation;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ColourPanel(int hue,
							int saturation)
		{
			// Initialise instance variables
			this.hue = hue;
			this.saturation = saturation;

			// Initialise image for colour area
			if (colourAreaImage == null)
			{
				colourAreaImage = new BufferedImage(COLOUR_AREA_WIDTH, COLOUR_AREA_HEIGHT, BufferedImage.TYPE_INT_RGB);
				for (int y = 0; y < COLOUR_AREA_HEIGHT; y++)
				{
					for (int x = 0; x < COLOUR_AREA_WIDTH; x++)
					{
						double h = (double)x * H_FACTOR;
						double s = (double)(COLOUR_AREA_HEIGHT - 1 - y) * S_FACTOR;
						colourAreaImage.setRGB(x, y, Utils.hsToRgb(h, s));
					}
				}
			}

			// Set properties
			setOpaque(true);
			setFocusable(false);

			// Add listeners
			addMouseListener(this);
			addMouseMotionListener(this);
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
				setValues(event);
		}

		//--------------------------------------------------------------

		@Override
		public void mouseReleased(MouseEvent event)
		{
			if (SwingUtilities.isLeftMouseButton(event))
				setValues(event);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : MouseMotionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void mouseDragged(MouseEvent event)
		{
			if (SwingUtilities.isLeftMouseButton(event))
				setValues(event);
		}

		//--------------------------------------------------------------

		@Override
		public void mouseMoved(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension(LEFT_MARGIN + RIGHT_MARGIN + 2 * BORDER_WIDTH + COLOUR_AREA_WIDTH,
								 TOP_MARGIN + BOTTOM_MARGIN + 2 * BORDER_WIDTH + COLOUR_AREA_HEIGHT);
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Fill background
			Rectangle rect = gr.getClipBounds();
			gr.setColor(getBackground());
			gr.fillRect(rect.x, rect.y, rect.width, rect.height);

			// Draw colour-area image
			gr.drawImage(colourAreaImage, COLOUR_AREA_X, COLOUR_AREA_Y, null);

			// Draw border
			gr.setColor(COLOUR_PANEL_BORDER_COLOUR);
			gr.drawRect(LEFT_MARGIN, TOP_MARGIN, 2 * BORDER_WIDTH + COLOUR_AREA_WIDTH - 1,
						2 * BORDER_WIDTH + COLOUR_AREA_HEIGHT - 1);

			// Draw crosshair
			int xh = COLOUR_AREA_X + hue / 2;
			int ys = COLOUR_AREA_Y + COLOUR_AREA_HEIGHT - 1 - saturation;
			int x = xh - CROSSHAIR_ARM_LENGTH;
			int y = ys - CROSSHAIR_ARM_LENGTH;
			gr.setColor(COLOUR_PANEL_CROSSHAIR_COLOUR);
			gr.setXORMode(COLOUR_PANEL_CROSSHAIR_XOR_COLOUR);
			gr.drawLine(x, ys, x + CROSSHAIR_LENGTH - 1, ys);
			gr.drawLine(xh, y, xh, y + CROSSHAIR_ARM_LENGTH - 1);
			gr.drawLine(xh, ys + 1, xh, ys + CROSSHAIR_ARM_LENGTH);

			// Draw hue marker
			gr.setPaintMode();
			gr.setColor(COLOUR_PANEL_MARKER_COLOUR);
			int x1 = xh - MARKER_LENGTH + 1;
			int x2 = xh + MARKER_LENGTH - 1;
			y = 0;
			while (x1 <= x2)
			{
				gr.drawLine(x1, y, x2, y);
				++x1;
				--x2;
				++y;
			}

			// Draw saturation marker
			x = 0;
			int y1 = ys - MARKER_LENGTH + 1;
			int y2 = ys + MARKER_LENGTH - 1;
			while (y1 <= y2)
			{
				gr.drawLine(x, y1, x, y2);
				++x;
				++y1;
				--y2;
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void setValues(int hue,
							   int saturation)
		{
			if ((this.hue != hue) || (this.saturation != saturation))
			{
				this.hue = hue;
				this.saturation = saturation;
				repaint();
			}
		}

		//--------------------------------------------------------------

		private void setValues(MouseEvent event)
		{
			int hue = Math.min(Math.max(MIN_HUE, (event.getX() - COLOUR_AREA_X) * 2), MAX_HUE);
			int saturation = Math.min(Math.max(MIN_SATURATION, COLOUR_AREA_Y + COLOUR_AREA_HEIGHT - 1 - event.getY()),
									  MAX_SATURATION);
			setValues(hue, saturation);
			HueSaturationPanel.this.setValues(hue, saturation);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	BufferedImage	colourAreaImage;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	ColourPanel		colourPanel;
	private	FIntegerSpinner	hueSpinner;
	private	FIntegerSpinner	saturationSpinner;
	private	ColourSampleBox	sampleBox;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public HueSaturationPanel(int hue,
							  int saturation)
	{
		//----  Colour panel

		colourPanel = new ColourPanel(hue, saturation);


		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel controlPanel = new JPanel(gridBag);

		int gridY = 0;

		// Label: hue
		JLabel hueLabel = new FLabel(HUE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(hueLabel, gbc);
		controlPanel.add(hueLabel);

		// Spinner: hue
		hueSpinner = new FIntegerSpinner(hue, MIN_HUE, MAX_HUE, HUE_FIELD_LENGTH);
		hueSpinner.addChangeListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(hueSpinner, gbc);
		controlPanel.add(hueSpinner);

		// Label: saturation
		JLabel saturationLabel = new FLabel(SATURATION_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(saturationLabel, gbc);
		controlPanel.add(saturationLabel);

		// Spinner: saturation
		saturationSpinner = new FIntegerSpinner(saturation, MIN_SATURATION, MAX_SATURATION, SATURATION_FIELD_LENGTH);
		saturationSpinner.addChangeListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(saturationSpinner, gbc);
		controlPanel.add(saturationSpinner);

		// Sample box
		sampleBox = new ColourSampleBox(hsToColour(hue, saturation));

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(6, 3, 2, 3);
		gridBag.setConstraints(sampleBox, gbc);
		controlPanel.add(sampleBox);


		//----  Outer panel

		setLayout(gridBag);

		int gridX = 0;

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(colourPanel, gbc);
		add(colourPanel);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 3, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		add(controlPanel);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Color hsToColour(int hue,
								   int saturation)
	{
		return new Color(Utils.hsToRgb((double)hue / (double)HUE_RANGE, (double)saturation / (double)MAX_SATURATION));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ChangeListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void stateChanged(ChangeEvent event)
	{
		updateColour();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int getHue()
	{
		return hueSpinner.getIntValue();
	}

	//------------------------------------------------------------------

	public int getSaturation()
	{
		return saturationSpinner.getIntValue();
	}

	//------------------------------------------------------------------

	private void updateColour()
	{
		int hue = getHue();
		int saturation = getSaturation();
		colourPanel.setValues(hue, saturation);
		sampleBox.setBackground(hsToColour(hue, saturation));
	}

	//------------------------------------------------------------------

	private void setValues(int hue,
						   int saturation)
	{
		hueSpinner.setIntValue(hue);
		saturationSpinner.setIntValue(saturation);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
