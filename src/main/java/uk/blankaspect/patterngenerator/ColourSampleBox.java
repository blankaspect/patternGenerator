/*====================================================================*\

ColourSampleBox.java

Colour sample box class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JComponent;

import uk.blankaspect.ui.swing.colour.Colours;

import uk.blankaspect.ui.swing.misc.GuiUtils;

//----------------------------------------------------------------------


// COLOUR SAMPLE BOX CLASS


class ColourSampleBox
	extends JComponent
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		WIDTH	= 40;
	private static final	int		HEIGHT	= 24;

	private static final	Color	BORDER_COLOUR	= Colours.LINE_BORDER;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public ColourSampleBox(Color colour)
	{
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setBackground(colour);
		setOpaque(true);
		setFocusable(false);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	protected void paintComponent(Graphics gr)
	{
		// Create copy of graphics context
		Graphics2D gr2d = GuiUtils.copyGraphicsContext(gr);

		// Fill background
		Rectangle rect = gr2d.getClipBounds();
		gr2d.setColor(getBackground());
		gr2d.fillRect(rect.x, rect.y, rect.width, rect.height);

		// Draw border
		gr2d.setColor(BORDER_COLOUR);
		gr2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
