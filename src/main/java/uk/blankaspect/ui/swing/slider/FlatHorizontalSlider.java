/*====================================================================*\

FlatHorizontalSlider.java

Class: flat horizontal slider.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.slider;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import uk.blankaspect.ui.swing.misc.GuiConstants;
import uk.blankaspect.ui.swing.misc.GuiUtils;

//----------------------------------------------------------------------


// CLASS: FLAT HORIZONTAL SLIDER


public class FlatHorizontalSlider
	extends HorizontalSlider
	implements MouseListener, MouseMotionListener
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Color	valueBarColour;
	private	boolean	mouseOverKnob;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public FlatHorizontalSlider(
		int	width,
		int	height,
		int	knobWidth)
	{
		this(width, height, knobWidth, DEFAULT_VALUE);
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	public FlatHorizontalSlider(
		int		width,
		int		height,
		int		knobWidth,
		double	value)
	{
		super(width, height, knobWidth, value);
		valueBarColour = FlatSliderColours.DEFAULT_VALUE_BAR_COLOUR;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public void mouseClicked(
		MouseEvent	event)
	{
		super.mouseClicked(event);
	}

	//------------------------------------------------------------------

	@Override
	public void mouseEntered(
		MouseEvent	event)
	{
		updateMouseOverKnob(event);
		super.mouseEntered(event);
	}

	//------------------------------------------------------------------

	@Override
	public void mouseExited(
		MouseEvent	event)
	{
		updateMouseOverKnob(null);
		super.mouseExited(event);
	}

	//------------------------------------------------------------------

	@Override
	public void mousePressed(
		MouseEvent	event)
	{
		updateMouseOverKnob(event);
		super.mousePressed(event);
	}

	//------------------------------------------------------------------

	@Override
	public void mouseReleased(
		MouseEvent	event)
	{
		updateMouseOverKnob(event);
		super.mouseReleased(event);
	}

	//------------------------------------------------------------------

	@Override
	public void mouseDragged(
		MouseEvent	event)
	{
		updateMouseOverKnob(event);
		super.mouseDragged(event);
	}

	//------------------------------------------------------------------

	@Override
	public void mouseMoved(
		MouseEvent	event)
	{
		updateMouseOverKnob(event);
		super.mouseMoved(event);
	}

	//------------------------------------------------------------------

	@Override
	protected void paintComponent(
		Graphics	gr)
	{
		// Create copy of graphics context
		Graphics2D gr2d = GuiUtils.copyGraphicsContext(gr);

		// Draw background
		Rectangle rect = gr2d.getClipBounds();
		gr2d.setColor(isEnabled() ? FlatSliderColours.BACKGROUND_COLOUR : getBackground());
		gr2d.fillRect(rect.x, rect.y, rect.width, rect.height);

		// Draw end regions
		int endRegionWidth = knobRect.width / 2 - 1;
		gr2d.setColor(FlatSliderColours.END_REGION_COLOUR);
		gr2d.fillRect(1, 1, endRegionWidth + 1, height - 2);
		gr2d.fillRect(width - BORDER_WIDTH - endRegionWidth, 1, endRegionWidth + 1, height - 2);

		if (isEnabled())
		{
			// Draw value bar
			int x1 = BORDER_WIDTH + endRegionWidth;
			gr2d.setColor(valueBarColour);
			gr2d.fillRect(x1, BORDER_WIDTH + 2, knobRect.x - BORDER_WIDTH, height - 2 * BORDER_WIDTH - 4);

			// Draw knob border
			int y1 = knobRect.y - 1;
			int y2 = knobRect.y + knobRect.height + 1;
			if (mouseOverKnob || isDragging())
			{
				gr2d.setColor(isDragging() ? FlatSliderColours.KNOB_ACTIVE_BORDER_COLOUR
											: FlatSliderColours.KNOB_BORDER_COLOUR);
				gr2d.drawRect(knobRect.x, knobRect.y, knobRect.width - 1, knobRect.height - 1);
				gr2d.drawRect(knobRect.x + 1, knobRect.y + 1, knobRect.width - 3, knobRect.height - 3);
				++y1;
				y2 -= 2;
			}

			// Draw knob centre line
			gr2d.setColor(FlatSliderColours.KNOB_LINE_COLOUR);
			int x = knobRect.x + knobRect.width / 2 - 1;
			gr2d.drawLine(x, y1, x, y2);
			++x;
			gr2d.drawLine(x, y1, x, y2);
		}

		// Draw border
		gr2d.setColor(FlatSliderColours.BORDER_COLOUR);
		gr2d.drawRect(0, 0, width - 1, height - 1);
		if (isFocusOwner())
		{
			gr2d.setColor(FlatSliderColours.FOCUSED_BORDER_COLOUR1);
			gr2d.drawRect(1, 1, width - 3, height - 3);

			gr2d.setStroke(GuiConstants.BASIC_DASH);
			gr2d.setColor(FlatSliderColours.FOCUSED_BORDER_COLOUR2);
			gr2d.drawRect(1, 1, width - 3, height - 3);
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public Color getValueBarColour()
	{
		return valueBarColour;
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	public void setValueBarColour(
		Color	colour)
	{
		if (colour == null)
			throw new IllegalArgumentException();

		if (!valueBarColour.equals(colour))
		{
			valueBarColour = colour;
			repaint();
		}
	}

	//------------------------------------------------------------------

	private void updateMouseOverKnob(
		MouseEvent	event)
	{
		boolean isOver = (event != null) && knobRect.contains(event.getPoint());
		if (mouseOverKnob != isOver)
		{
			mouseOverKnob = isOver;
			repaint();
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
