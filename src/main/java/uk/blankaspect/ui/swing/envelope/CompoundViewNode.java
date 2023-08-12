/*====================================================================*\

CompoundViewNode.java

Class: compound view node.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.envelope;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Point;

import uk.blankaspect.common.envelope.CompoundNode;

//----------------------------------------------------------------------


// CLASS: COMPOUND VIEW NODE


public class CompoundViewNode
	extends CompoundNode
	implements IViewNode
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public CompoundViewNode(double   x,
							double[] ys)
	{
		this(x, ys, false, 0);
	}

	//------------------------------------------------------------------

	public CompoundViewNode(double   x,
							double[] ys,
							boolean  fixedX)
	{
		this(x, ys, fixedX, 0);
	}

	//------------------------------------------------------------------

	public CompoundViewNode(double   x,
							double[] ys,
							boolean  fixedX,
							int      fixedY)
	{
		super(x, ys, fixedX, fixedY);
	}

	//------------------------------------------------------------------

	public CompoundViewNode(CompoundNode node)
	{
		this(node.x, node.ys, node.fixedX, node.fixedY);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IViewNode interface
////////////////////////////////////////////////////////////////////////

	@Override
	public Point toPoint(int width,
						 int height,
						 int bandIndex)
	{
		--width;
		--height;
		return new Point(EnvelopeView.LEFT_MARGIN + (int)Math.round(x * (double)width),
						 EnvelopeView.TOP_MARGIN + height - (int)Math.round(ys[bandIndex] * (double)height));
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
