/*====================================================================*\

FlatHorizontalSlider.java

Class: flat horizontal slider.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.slider;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.util.List;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.workaround.Workarounds01;

//----------------------------------------------------------------------


// CLASS: FLAT HORIZONTAL SLIDER


public class FlatHorizontalSlider
	extends AbstractHorizontalSlider
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	BasicStroke	STROKE_WIDTH1	=
			new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

	private static final	BasicStroke	STROKE_WIDTH2	=
			new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

	private static final	BasicStroke	STROKE_DASHED	=
			new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 1.5f, 1.5f },
							0.0f);

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	boolean	mouseOver;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public FlatHorizontalSlider(
		int	width,
		int	height)
	{
		// Call alternative constructor
		this(width, height, DEFAULT_VALUE);
	}

	//------------------------------------------------------------------

	public FlatHorizontalSlider(
		int		width,
		int		height,
		double	value)
	{
		// Call superclass constructor
		super(width, height);

		// Validate arguments
		if ((value < MIN_VALUE) || (value > MAX_VALUE))
			throw new IllegalArgumentException("Value out of bounds");

		// Redraw slider in response to mouse event
		addMouseListener(new MouseListener()
		{
			@Override
			public void mouseEntered(
				MouseEvent	event)
			{
				updateMouseOver(event);
			}

			@Override
			public void mouseExited(
				MouseEvent	event)
			{
				updateMouseOver(event);
			}

			@Override
			public void mousePressed(
				MouseEvent	event)
			{
				updateMouseOver(event);
			}

			@Override
			public void mouseReleased(
				MouseEvent	event)
			{
				updateMouseOver(event);
			}

			@Override
			public void mouseClicked(
				MouseEvent	event)
			{
				// do nothing
			}
		});
		addMouseMotionListener(new MouseMotionListener()
		{
			@Override
			public void mouseDragged(
				MouseEvent	event)
			{
				updateMouseOver(event);
			}

			@Override
			public void mouseMoved(
				MouseEvent	event)
			{
				// do nothing
			}
		});

		// Set value
		setValue(value);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	protected void paintComponent(
		Graphics	gr)
	{
		// Create copy of graphics context
		Graphics2D gr2d = GuiUtils.copyGraphicsContext(gr);

		// Draw background
		boolean enabled = isEnabled();
		gr2d.setColor(FlatSliderColours.BACKGROUND);
		gr2d.fillRect(0, 0, width, height);

		// Draw value bar
		gr2d.setColor(enabled
						? dragging
								? FlatSliderColours.VALUE_BAR_ACTIVE
								: FlatSliderColours.VALUE_BAR
						: FlatSliderColours.DISABLED);
		gr2d.fillRect(BORDER_WIDTH, BORDER_WIDTH + 2, markerX - BORDER_WIDTH, height - 2 * BORDER_WIDTH - 4);

		// Draw border
		if (mouseOver || dragging)
		{
			gr2d.setStroke(STROKE_WIDTH2);
			gr2d.setColor(enabled ? FlatSliderColours.BORDER_HIGHLIGHTED : FlatSliderColours.DISABLED);
			gr2d.drawRect(1, 1, width - 2, height - 2);
		}
		else
		{
			// Draw outer border
			gr2d.setColor(enabled ? FlatSliderColours.BORDER : FlatSliderColours.DISABLED);
// WORKAROUND : AWT/Swing doesn't scale the stroke width for a high-DPI display with a scale factor of 2
//			gr2d.drawRect(0, 0, width - 1, height - 1);
			Workarounds01.drawRect(gr2d, 0, 0, width, height, 1);

			// If slider has focus, draw focus indicator ...
			if (isFocusOwner())
			{
				gr2d.setColor(FlatSliderColours.BORDER_FOCUSED_0);
				gr2d.drawRect(1, 1, width - 2, height - 2);

				gr2d.setStroke(STROKE_DASHED);
				gr2d.setColor(FlatSliderColours.BORDER_FOCUSED_1);
				gr2d.drawRect(1, 1, width - 2, height - 2);
			}

			// ... otherwise, extend border inwards at ends of slider
			else
			{
				gr2d.setStroke(STROKE_WIDTH1);
				for (int x : List.of(1, width - 2))
					gr2d.fillRect(x, 1, 1, height - 2);
			}
		}

		// Draw marker
		gr2d.setStroke(STROKE_WIDTH2);
		gr2d.setColor(enabled ? FlatSliderColours.MARKER : FlatSliderColours.DISABLED);
		gr2d.drawLine(markerX, 1, markerX, height - 1);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private void updateMouseOver(
		MouseEvent	event)
	{
		boolean over = isEnabled() ? new Rectangle(0, 0, width, height).contains(event.getX(), event.getY()) : false;
		if (mouseOver != over)
		{
			mouseOver = over;
			repaint();
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
