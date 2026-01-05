/*====================================================================*\

AbstractHorizontalSlider.java

Class: abstract horizontal slider.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.slider;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import uk.blankaspect.ui.swing.action.KeyAction;

//----------------------------------------------------------------------


// CLASS: ABSTRACT HORIZONTAL SLIDER


public abstract class AbstractHorizontalSlider
	extends AbstractSlider
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int	MIN_WIDTH	= 32;
	public static final		int	MIN_HEIGHT	= 12;

	private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
	{
		KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
						  Command.DECREMENT_UNIT),
		KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
						  Command.INCREMENT_UNIT),
		KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK),
						  Command.DECREMENT_BLOCK),
		KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK),
						  Command.INCREMENT_BLOCK),
		KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
						  Command.DECREMENT_MAX),
		KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
						  Command.INCREMENT_MAX)
	};

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	protected	int	markerX;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public AbstractHorizontalSlider(
		int	width,
		int	height)
	{
		// Call superclass constructor
		super(Math.max(MIN_WIDTH, width), Math.max(MIN_HEIGHT, height));

		// Initialise instance variables
		unitIncrement = 1.0 / extent();
		blockIncrement = 10.0 * unitIncrement;
		markerX = BORDER_WIDTH;

		// Set properties
		setOpaque(true);
		setFocusable(true);

		// Add commands to action map
		KeyAction.create(this, JComponent.WHEN_FOCUSED, actionListener, KEY_COMMANDS);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static int extentToWidth(
		int	extent)
	{
		return extent + 2 * BORDER_WIDTH;
	}

	//------------------------------------------------------------------

	public static int widthToExtent(
		int	width)
	{
		return width - 2 * BORDER_WIDTH;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	protected double valueFor(
		MouseEvent	event)
	{
		int x = Math.min(Math.max(BORDER_WIDTH, event.getX()), width - BORDER_WIDTH);
		return clampValue((double)(x - BORDER_WIDTH) / extent());
	}

	//------------------------------------------------------------------

	@Override
	protected void setValue(
		double	value)
	{
		value = clampValue(value);
		markerX = BORDER_WIDTH + (int)Math.round(value * extent());
		repaint();
		this.value = value;
		fireStateChanged();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private double extent()
	{
		return (double)widthToExtent(width);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
