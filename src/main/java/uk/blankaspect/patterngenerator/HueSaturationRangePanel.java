/*====================================================================*\

HueSaturationRangePanel.java

Hue and saturation range panel class.

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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.awt.image.BufferedImage;

import java.util.EnumMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.swing.colour.Colours;
import uk.blankaspect.ui.swing.colour.ColourUtils;

import uk.blankaspect.ui.swing.container.IntegerSpinnerSliderPanel;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.slider.HorizontalSlider;

import uk.blankaspect.ui.swing.spinner.FIntegerSpinner;

//----------------------------------------------------------------------


// HUE AND SATURATION RANGE PANEL CLASS


class HueSaturationRangePanel
	extends JPanel
	implements ActionListener, ChangeListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int		HUE_RANGE	= 360;

	private static final	int		MIN_HUE			= 0;
	private static final	int		MAX_HUE			= HUE_RANGE - 1;
	private static final	int		DEFAULT_HUE1	= 30;
	private static final	int		DEFAULT_HUE2	= 60;

	private static final	int		MIN_SATURATION		= 0;
	private static final	int		MAX_SATURATION		= 100;
	private static final	int		DEFAULT_SATURATION1	= 100;
	private static final	int		DEFAULT_SATURATION2	= 75;

	private static final	int		MIN_BRIGHTNESS		= 0;
	private static final	int		MAX_BRIGHTNESS		= 100;
	private static final	int		DEFAULT_BRIGHTNESS	= MAX_BRIGHTNESS;

	private static final	int		MIN_OPACITY		= 0;
	private static final	int		MAX_OPACITY		= 100;
	private static final	int		DEFAULT_OPACITY	= MAX_OPACITY;

	private static final	int		HUE_FIELD_LENGTH		= 3;
	private static final	int		SATURATION_FIELD_LENGTH	= 3;
	private static final	int		BRIGHTNESS_FIELD_LENGTH	= 3;
	private static final	int		OPACITY_FIELD_LENGTH	= 3;

	private static final	int		SLIDER_KNOB_WIDTH	= 24;
	private static final	int		SLIDER_HEIGHT		= 18;

	private static final	String	HUE_STR			= "Hue";
	private static final	String	SATURATION_STR	= "Saturation";
	private static final	String	OPACITY_STR		= "Opacity";
	private static final	String	BRIGHTNESS_STR	= "Brightness";

	private static final	String	SPINNER_SLIDER_PANEL_KEY	= HueSaturationRangePanel.class.getName();

	private static final	Color	COLOUR_PANEL_BORDER_COLOUR			= Colours.LINE_BORDER;
	private static final	Color	COLOUR_PANEL_RANGE_BOX_COLOUR		= Color.BLACK;
	private static final	Color	COLOUR_PANEL_RANGE_BOX_XOR_COLOUR	= Color.WHITE;
	private static final	Color	COLOUR_PANEL_MARKER_COLOUR			= Color.DARK_GRAY;

	// Commands
	private interface Command
	{
		String	SET_BOUND	= "setBound.";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Bound						bound;
	private	ColourPanel					colourPanel;
	private	Map<Bound, FIntegerSpinner>	hueSpinners;
	private	Map<Bound, FIntegerSpinner>	saturationSpinners;
	private	Map<Bound, FIntegerSpinner>	opacitySpinners;
	private	Map<Bound, ColourSampleBox>	sampleBoxes;
	private	IntegerSpinnerSliderPanel	brightnessSpinnerSlider;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public HueSaturationRangePanel(
		Params	params)
	{
		// Initialise instance variables
		bound = Bound.BOUND1;


		//----  Colour panel

		colourPanel = new ColourPanel(params);


		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel controlPanel = new JPanel(gridBag);


		//----  Hue and saturation panel

		JPanel hueSatPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(hueSatPanel);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(hueSatPanel, gbc);
		controlPanel.add(hueSatPanel);

		int gridY = 0;

		Map<Bound, Integer> hues = new EnumMap<>(Bound.class);
		hues.put(Bound.BOUND1, params.hue1);
		hues.put(Bound.BOUND2, params.hue2);
		Map<Bound, Integer> saturations = new EnumMap<>(Bound.class);
		saturations.put(Bound.BOUND1, params.saturation1);
		saturations.put(Bound.BOUND2, params.saturation2);
		Map<Bound, Integer> opacities = new EnumMap<>(Bound.class);
		opacities.put(Bound.BOUND1, params.opacity1);
		opacities.put(Bound.BOUND2, params.opacity2);

		hueSpinners = new EnumMap<>(Bound.class);
		saturationSpinners = new EnumMap<>(Bound.class);
		opacitySpinners = new EnumMap<>(Bound.class);
		sampleBoxes = new EnumMap<>(Bound.class);
		ButtonGroup buttonGroup = new ButtonGroup();

		for (Bound bound : Bound.values())
		{
			// Radio button: bound
			JRadioButton boundRadioButton = new RadioButton(bound.text);
			buttonGroup.add(boundRadioButton);
			boundRadioButton.setSelected(bound == this.bound);
			boundRadioButton.setActionCommand(Command.SET_BOUND + bound);
			boundRadioButton.addActionListener(this);

			gbc.gridx = 0;
			gbc.gridy = gridY;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_END;
			gbc.fill = GridBagConstraints.VERTICAL;
			gbc.insets = AppConstants.COMPONENT_INSETS;
			gridBag.setConstraints(boundRadioButton, gbc);
			hueSatPanel.add(boundRadioButton);

			// Panel: bound
			JPanel boundPanel = new JPanel(gridBag);

			gbc.gridx = 1;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = AppConstants.COMPONENT_INSETS;
			gridBag.setConstraints(boundPanel, gbc);
			hueSatPanel.add(boundPanel);

			int gridX = 0;

			// Label: hue
			JLabel hueLabel = new FLabel(HUE_STR);

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_END;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 6, 0, 0);
			gridBag.setConstraints(hueLabel, gbc);
			boundPanel.add(hueLabel);

			// Spinner: hue
			FIntegerSpinner hueSpinner = new FIntegerSpinner(hues.get(bound), MIN_HUE, MAX_HUE, HUE_FIELD_LENGTH);
			hueSpinners.put(bound, hueSpinner);
			hueSpinner.addChangeListener(this);

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 6, 0, 0);
			gridBag.setConstraints(hueSpinner, gbc);
			boundPanel.add(hueSpinner);

			// Label: saturation
			JLabel saturationLabel = new FLabel(SATURATION_STR);

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_END;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 12, 0, 0);
			gridBag.setConstraints(saturationLabel, gbc);
			boundPanel.add(saturationLabel);

			// Spinner: saturation
			FIntegerSpinner saturationSpinner = new FIntegerSpinner(saturations.get(bound), MIN_SATURATION,
																	MAX_SATURATION, SATURATION_FIELD_LENGTH);
			saturationSpinners.put(bound, saturationSpinner);
			saturationSpinner.addChangeListener(this);

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 6, 0, 0);
			gridBag.setConstraints(saturationSpinner, gbc);
			boundPanel.add(saturationSpinner);

			// Label: opacity
			JLabel opacityLabel = new FLabel(OPACITY_STR);

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_END;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 12, 0, 0);
			gridBag.setConstraints(opacityLabel, gbc);
			boundPanel.add(opacityLabel);

			// Spinner: opacity
			FIntegerSpinner opacitySpinner = new FIntegerSpinner(opacities.get(bound), MIN_OPACITY, MAX_OPACITY,
																 OPACITY_FIELD_LENGTH);
			opacitySpinners.put(bound, opacitySpinner);
			opacitySpinner.addChangeListener(this);

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 6, 0, 0);
			gridBag.setConstraints(opacitySpinner, gbc);
			boundPanel.add(opacitySpinner);

			// Sample box
			ColourSampleBox sampleBox = new ColourSampleBox(hsbToColour(hues.get(bound), saturations.get(bound),
																		params.brightness, opacities.get(bound)));
			sampleBoxes.put(bound, sampleBox);

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 14, 0, 0);
			gridBag.setConstraints(sampleBox, gbc);
			boundPanel.add(sampleBox);
		}

		// Update widths of radio buttons
//		RadioButton.update();


		//----  Brightness panel

		JPanel brightnessPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(brightnessPanel);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(brightnessPanel, gbc);
		controlPanel.add(brightnessPanel);

		// Label: brightness
		JLabel brightnessLabel = new FLabel(BRIGHTNESS_STR);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(brightnessLabel, gbc);
		brightnessPanel.add(brightnessLabel);

		// Spinner-slider panel: brightness
		int sliderExtent = 2 * (MAX_BRIGHTNESS - MIN_BRIGHTNESS + 1);
		int sliderWidth = HorizontalSlider.extentToWidth(sliderExtent, SLIDER_KNOB_WIDTH);
		brightnessSpinnerSlider = new IntegerSpinnerSliderPanel(params.brightness, MIN_BRIGHTNESS, MAX_BRIGHTNESS,
																BRIGHTNESS_FIELD_LENGTH, false, sliderWidth,
																SLIDER_HEIGHT, SLIDER_KNOB_WIDTH, MAX_BRIGHTNESS,
																SPINNER_SLIDER_PANEL_KEY);
		brightnessSpinnerSlider.getSpinner().addChangeListener(this);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 6, 0, 0);
		gridBag.setConstraints(brightnessSpinnerSlider, gbc);
		brightnessPanel.add(brightnessSpinnerSlider);


		//----  Outer panel

		setLayout(gridBag);

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
		gridBag.setConstraints(colourPanel, gbc);
		add(colourPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		add(controlPanel);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Color hsbToColour(
		int	hue,
		int	saturation,
		int	brightness,
		int	opacity)
	{
		int rgb = Color.HSBtoRGB((float)hue / (float)HUE_RANGE, (float)saturation / (float)MAX_SATURATION,
								 (float)brightness / (float)MAX_BRIGHTNESS);
		int alpha = (int)((float)opacity / (float)MAX_OPACITY * (float)ColourUtils.MAX_ARGB_COMPONENT_VALUE);
		return new Color(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, alpha);
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

		if (command.startsWith(Command.SET_BOUND))
			onSetBound(StringUtils.removePrefix(command, Command.SET_BOUND));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ChangeListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void stateChanged(
		ChangeEvent	event)
	{
		Object eventSource = event.getSource();

		Bound bound = null;

		// Test for hue spinner
		for (Bound b : hueSpinners.keySet())
		{
			if (eventSource == hueSpinners.get(b))
			{
				bound = b;
				break;
			}
		}

		// Test for saturation spinner
		if (bound == null)
		{
			for (Bound b : saturationSpinners.keySet())
			{
				if (eventSource == saturationSpinners.get(b))
				{
					bound = b;
					break;
				}
			}
		}

		int brightness = getBrightness();
		if (bound == null)
		{
			colourPanel.updateBrightness(brightness);
			for (Bound b : sampleBoxes.keySet())
				sampleBoxes.get(b).setBackground(hsbToColour(getHue(b), getSaturation(b), brightness, MAX_OPACITY));
		}
		else
		{
			int hue = getHue(bound);
			int saturation = getSaturation(bound);
			colourPanel.setValues(bound, hue, saturation);
			sampleBoxes.get(bound).setBackground(hsbToColour(hue, saturation, brightness, MAX_OPACITY));
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public Params getParams()
	{
		return new Params(getHue(Bound.BOUND1), getSaturation(Bound.BOUND1), getHue(Bound.BOUND2),
						  getSaturation(Bound.BOUND2), getBrightness(), getOpacity(Bound.BOUND1),
						  getOpacity(Bound.BOUND2));
	}

	//------------------------------------------------------------------

	private int getHue(
		Bound	bound)
	{
		return hueSpinners.get(bound).getIntValue();
	}

	//------------------------------------------------------------------

	private int getSaturation(
		Bound	bound)
	{
		return saturationSpinners.get(bound).getIntValue();
	}

	//------------------------------------------------------------------

	private int getBrightness()
	{
		return brightnessSpinnerSlider.getValue();
	}

	//------------------------------------------------------------------

	private int getOpacity(
		Bound	bound)
	{
		return opacitySpinners.get(bound).getIntValue();
	}

	//------------------------------------------------------------------

	private void setValues(
		Bound	bound,
		int		hue,
		int		saturation)
	{
		hueSpinners.get(bound).setIntValue(hue);
		saturationSpinners.get(bound).setIntValue(saturation);
	}

	//------------------------------------------------------------------

	private void onSetBound(
		String	key)
	{
		for (Bound value : Bound.values())
		{
			if (value.toString().equals(key))
			{
				bound = value;
				break;
			}
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// BOUND


	private enum Bound
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		BOUND1  ("Bound 1"),
		BOUND2  ("Bound 2");

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Bound(
			String	text)
		{
			this.text = text;
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

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private Bound other()
		{
			return switch (this)
			{
				case BOUND1 -> BOUND2;
				case BOUND2 -> BOUND1;
			};
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// PARAMETERS CLASS


	public static class Params
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		int	hue1;
		int	saturation1;
		int	hue2;
		int	saturation2;
		int	brightness;
		int	opacity1;
		int	opacity2;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public Params()
		{
			hue1 = DEFAULT_HUE1;
			saturation1 = DEFAULT_SATURATION1;
			hue2 = DEFAULT_HUE2;
			saturation2 = DEFAULT_SATURATION2;
			brightness = DEFAULT_BRIGHTNESS;
			opacity1 = DEFAULT_OPACITY;
			opacity2 = DEFAULT_OPACITY;
		}

		//--------------------------------------------------------------

		public Params(
			int	hue1,
			int	saturation1,
			int	hue2,
			int	saturation2,
			int	brightness,
			int	opacity1,
			int	opacity2)
		{
			this.hue1 = hue1;
			this.saturation1 = saturation1;
			this.hue2 = hue2;
			this.saturation2 = saturation2;
			this.brightness = brightness;
			this.opacity1 = opacity1;
			this.opacity2 = opacity2;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// RADIO BUTTON CLASS


	private static class RadioButton
		extends JRadioButton
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	Color	BACKGROUND_COLOUR	= new Color(252, 224, 128);

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private RadioButton(
			String	text)
		{
			super(text);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Color getBackground()
		{
			return isSelected() ? BACKGROUND_COLOUR : super.getBackground();
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// COLOUR PANEL CLASS


	private class ColourPanel
		extends JComponent
		implements MouseListener, MouseMotionListener
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int		COLOUR_AREA_HALF_WIDTH	= HUE_RANGE / 2;
		private static final	int		COLOUR_AREA_WIDTH		= 2 * COLOUR_AREA_HALF_WIDTH;
		private static final	int		COLOUR_AREA_HEIGHT		= MAX_SATURATION - MIN_SATURATION + 1;
		private static final	int		BORDER_WIDTH			= 1;
		private static final	int		MARKER_LENGTH			= 5;
		private static final	int		LEFT_MARGIN				= MARKER_LENGTH;
		private static final	int		RIGHT_MARGIN			= LEFT_MARGIN;
		private static final	int		TOP_MARGIN				= MARKER_LENGTH;
		private static final	int		BOTTOM_MARGIN			= TOP_MARGIN;
		private static final	int		COLOUR_AREA_X			= LEFT_MARGIN + BORDER_WIDTH;
		private static final	int		COLOUR_AREA_Y			= TOP_MARGIN + BORDER_WIDTH;

		private static final	float	H_FACTOR	= 2.0f / (float)COLOUR_AREA_WIDTH;
		private static final	float	S_FACTOR	= 1.0f / (float)(COLOUR_AREA_HEIGHT - 1);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int				hue1;
		private	int				saturation1;
		private	int				hue2;
		private	int				saturation2;
		private	BufferedImage	colourAreaImage;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ColourPanel(
			Params	params)
		{
			// Initialise instance variables
			hue1 = params.hue1;
			saturation1 = params.saturation1;
			hue2 = params.hue2;
			saturation2 = params.saturation2;

			// Initialise image for colour area
			colourAreaImage = new BufferedImage(COLOUR_AREA_WIDTH, COLOUR_AREA_HEIGHT,
												BufferedImage.TYPE_INT_RGB);
			updateColourAreaImage(params.brightness);

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
			setValues(event);
		}

		//--------------------------------------------------------------

		@Override
		public void mouseReleased(
			MouseEvent	event)
		{
			setValues(event);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : MouseMotionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void mouseDragged(
			MouseEvent	event)
		{
			setValues(event);
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
		public Dimension getPreferredSize()
		{
			return new Dimension(LEFT_MARGIN + RIGHT_MARGIN + 2 * BORDER_WIDTH + COLOUR_AREA_WIDTH,
								 TOP_MARGIN + BOTTOM_MARGIN + 2 * BORDER_WIDTH + COLOUR_AREA_HEIGHT);
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(
			Graphics	gr)
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

			// Draw range box
			int xh1 = COLOUR_AREA_X + hue1 / 2;
			int xh2 = COLOUR_AREA_X + hue2 / 2;
			if (xh2 < xh1)
				xh2 += COLOUR_AREA_HALF_WIDTH;
			int ys1 = COLOUR_AREA_Y + COLOUR_AREA_HEIGHT - 1 - saturation1;
			int ys2 = COLOUR_AREA_Y + COLOUR_AREA_HEIGHT - 1 - saturation2;
			int y1 = Math.min(ys1, ys2);
			int y2 = Math.max(ys1, ys2);
			gr.setColor(COLOUR_PANEL_RANGE_BOX_COLOUR);
			gr.setXORMode(COLOUR_PANEL_RANGE_BOX_XOR_COLOUR);
			gr.drawRect(xh1, y1, xh2 - xh1, y2 - y1);

			// Draw hue marker 1
			gr.setPaintMode();
			gr.setColor(COLOUR_PANEL_MARKER_COLOUR);
			int x1 = xh1 - MARKER_LENGTH + 1;
			int x2 = xh1 + MARKER_LENGTH - 1;
			int y = COLOUR_AREA_Y - BORDER_WIDTH - MARKER_LENGTH;
			while (x1 <= x2)
			{
				gr.drawLine(x1, y, x2, y);
				++x1;
				--x2;
				++y;
			}

			// Draw saturation marker 1
			int x = 0;
			y1 = ys1 - MARKER_LENGTH + 1;
			y2 = ys1 + MARKER_LENGTH - 1;
			while (y1 <= y2)
			{
				gr.drawLine(x, y1, x, y2);
				++x;
				++y1;
				--y2;
			}

			// Draw hue marker 2
			int yh2 = COLOUR_AREA_Y + COLOUR_AREA_HEIGHT + BORDER_WIDTH + MARKER_LENGTH - 1;
			x1 = xh2 - MARKER_LENGTH + 1;
			x2 = xh2 + MARKER_LENGTH - 1;
			y = yh2;
			while (x1 <= x2)
			{
				gr.drawLine(x1, y, x2, y);
				++x1;
				--x2;
				--y;
			}

			// Draw saturation marker 2
			int xs2 = COLOUR_AREA_X + COLOUR_AREA_WIDTH + BORDER_WIDTH + MARKER_LENGTH - 1;
			x = xs2;
			y1 = ys2 - MARKER_LENGTH + 1;
			y2 = ys2 + MARKER_LENGTH - 1;
			while (y1 <= y2)
			{
				gr.drawLine(x, y1, x, y2);
				--x;
				++y1;
				--y2;
			}

			// Draw lines on second hue and saturation markers
			gr.setColor(getBackground());
			gr.drawLine(xh2, yh2, xh2, yh2 - 1);
			gr.drawLine(xs2, ys2, xs2 - 1, ys2);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void updateBrightness(
			int	brightness)
		{
			updateColourAreaImage(brightness);
			repaint();
		}

		//--------------------------------------------------------------

		private void setValues(
			Bound	bound,
			int		hue,
			int		saturation)
		{
			switch (bound)
			{
				case BOUND1:
					if ((hue1 != hue) || (saturation1 != saturation))
					{
						hue1 = hue;
						saturation1 = saturation;
						repaint();
					}
					break;

				case BOUND2:
					if ((hue2 != hue) || (saturation2 != saturation))
					{
						hue2 = hue;
						saturation2 = saturation;
						repaint();
					}
					break;
			}
		}

		//--------------------------------------------------------------

		private void updateColourAreaImage(
			int	brightness)
		{
			for (int y = 0; y < COLOUR_AREA_HEIGHT; y++)
			{
				for (int x = 0; x < COLOUR_AREA_WIDTH; x++)
				{
					float h = (float)x * H_FACTOR;
					float s = (float)(COLOUR_AREA_HEIGHT - 1 - y) * S_FACTOR;
					float b = (float)brightness / (float)MAX_BRIGHTNESS;
					colourAreaImage.setRGB(x, y, Color.HSBtoRGB(h, s, b));
				}
			}
		}

		//--------------------------------------------------------------

		private void setValues(
			MouseEvent	event)
		{
			int x = event.getX();
			if (x >= COLOUR_AREA_HALF_WIDTH)
				x -= COLOUR_AREA_HALF_WIDTH;
			int hue = Math.min(Math.max(MIN_HUE, (x - COLOUR_AREA_X) * 2), MAX_HUE);
			int saturation = Math.min(Math.max(MIN_SATURATION, COLOUR_AREA_Y + COLOUR_AREA_HEIGHT - 1 - event.getY()),
									  MAX_SATURATION);
			Bound b = SwingUtilities.isLeftMouseButton(event) ? bound : bound.other();
			setValues(b, hue, saturation);
			HueSaturationRangePanel.this.setValues(b, hue, saturation);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
