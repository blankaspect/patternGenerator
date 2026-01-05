/*====================================================================*\

AbstractSlider.java

Class: abstract slider base.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.slider;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Dimension;
import java.awt.Graphics;

import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//----------------------------------------------------------------------


// CLASS: ABSTRACT SLIDER BASE


public abstract class AbstractSlider
	extends JComponent
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		double	MIN_VALUE		= 0.0;
	public static final		double	MAX_VALUE		= 1.0;
	public static final		double	DEFAULT_VALUE	= 0.0;

	protected static final	int		BORDER_WIDTH	= 2;

	private static final	String	PROPERTY_KEY_ENABLED	= "enabled";

	// Commands
	protected interface Command
	{
		String	DECREMENT_UNIT	= "decrementUnit";
		String	INCREMENT_UNIT	= "incrementUnit";
		String	DECREMENT_BLOCK	= "decrementBlock";
		String	INCREMENT_BLOCK	= "incrementBlock";
		String	DECREMENT_MAX	= "decrementMax";
		String	INCREMENT_MAX	= "incrementMax";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	protected	int						width;
	protected	int						height;
	protected	double					unitIncrement;
	protected	double					blockIncrement;
	protected	boolean					dragging;
	protected	double					value;
	protected	ActionListener			actionListener;
	protected	List<ChangeListener>	changeListeners;
	protected	ChangeEvent				changeEvent;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	protected AbstractSlider(
		int	width,
		int	height)
	{
		// Initialise instance variables
		this.width = width;
		this.height = height;
		value = DEFAULT_VALUE;
		actionListener = event ->
		{
			if (isEnabled())
			{
				switch (event.getActionCommand())
				{
					case Command.DECREMENT_UNIT  -> incrementValue(-unitIncrement);
					case Command.INCREMENT_UNIT  -> incrementValue(unitIncrement);
					case Command.DECREMENT_BLOCK -> incrementValue(-blockIncrement);
					case Command.INCREMENT_BLOCK -> incrementValue(blockIncrement);
					case Command.DECREMENT_MAX   -> setValue(MIN_VALUE);
					case Command.INCREMENT_MAX   -> setValue(MAX_VALUE);
				}
			}
		};
		changeListeners = new ArrayList<>();

		// Set properties
		setMinimumSize(getPreferredSize());
		setMaximumSize(getPreferredSize());

		// Redraw slider when it is enabled or disabled
		addPropertyChangeListener(PROPERTY_KEY_ENABLED, event -> repaint());

		// Redraw slider view it gains or loses focus
		addFocusListener(new FocusListener()
		{
			@Override
			public void focusGained(
				FocusEvent	event)
			{
				repaint();
			}

			@Override
			public void focusLost(
				FocusEvent	event)
			{
				repaint();
			}
		});

		// Start or stop dragging the slider in response to 'mouse pressed' or 'mouse released' events
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(
				MouseEvent	event)
			{
				if (isEnabled())
				{
					requestFocusInWindow();

					if (SwingUtilities.isLeftMouseButton(event))
					{
						dragging = true;
						setValue(valueFor(event));
					}
				}
			}

			@Override
			public void mouseReleased(
				MouseEvent	event)
			{
				if (isEnabled() && SwingUtilities.isLeftMouseButton(event) && dragging)
				{
					dragging = false;
					setValue(valueFor(event));
				}
			}
		});

		// Update the slider value in response to 'mouse dragged' events
		addMouseMotionListener(new MouseAdapter()
		{
			@Override
			public void mouseDragged(
				MouseEvent	event)
			{
				if (isEnabled() && SwingUtilities.isLeftMouseButton(event) && dragging)
					setValue(valueFor(event));
			}
		});
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static double clampValue(
		double	value)
	{
		return Math.min(Math.max(MIN_VALUE, value), MAX_VALUE);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Abstract methods
////////////////////////////////////////////////////////////////////////

	@Override
	protected abstract void paintComponent(
		Graphics	gr);

	//------------------------------------------------------------------

	protected abstract double valueFor(
		MouseEvent	event);

	//------------------------------------------------------------------

	protected abstract void setValue(
		double	value);

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(width, height);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public double value()
	{
		return value;
	}

	//------------------------------------------------------------------

	public void value(
		double	value)
	{
		setValue(value);
	}

	//------------------------------------------------------------------

	public double unitIncrement()
	{
		return unitIncrement;
	}

	//------------------------------------------------------------------

	public void unitIncrement(
		double	increment)
	{
		unitIncrement = increment;
	}

	//------------------------------------------------------------------

	public double blockIncrement()
	{
		return blockIncrement;
	}

	//------------------------------------------------------------------

	public void blockIncrement(
		double	increment)
	{
		blockIncrement = increment;
	}

	//------------------------------------------------------------------

	public void incrementUnits(
		int	numUnits)
	{
		incrementValue((double)numUnits * unitIncrement);
	}

	//------------------------------------------------------------------

	public void incrementBlocks(
		int	numBlocks)
	{
		incrementValue((double)numBlocks * blockIncrement);
	}

	//------------------------------------------------------------------

	public void addChangeListener(
		ChangeListener	listener)
	{
		changeListeners.add(listener);
	}

	//------------------------------------------------------------------

	public void removeChangeListener(
		ChangeListener	listener)
	{
		changeListeners.remove(listener);
	}

	//------------------------------------------------------------------

	protected void fireStateChanged()
	{
		for (int i = changeListeners.size() - 1; i >= 0; i--)
		{
			if (changeEvent == null)
				changeEvent = new ChangeEvent(this);
			changeListeners.get(i).stateChanged(changeEvent);
		}
	}

	//------------------------------------------------------------------

	private void incrementValue(
		double	increment)
	{
		setValue(value + increment);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
