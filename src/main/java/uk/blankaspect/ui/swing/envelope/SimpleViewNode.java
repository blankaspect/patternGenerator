/*====================================================================*\

SimpleViewNode.java

Class: simple view node.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.envelope;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Point;

import uk.blankaspect.common.envelope.SimpleNode;

//----------------------------------------------------------------------


// CLASS: SIMPLE VIEW NODE


public class SimpleViewNode
	extends SimpleNode
	implements IViewNode
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public SimpleViewNode(double x,
						  double y)
	{
		this(x, y, false, false);
	}

	//------------------------------------------------------------------

	public SimpleViewNode(double  x,
						  double  y,
						  boolean fixedX)
	{
		this(x, y, fixedX, false);
	}

	//------------------------------------------------------------------

	public SimpleViewNode(double  x,
						  double  y,
						  boolean fixedX,
						  boolean fixedY)
	{
		super(x, y, fixedX, fixedY);
	}

	//------------------------------------------------------------------

	public SimpleViewNode(SimpleNode node)
	{
		this(node.x, node.y, node.fixedX, node.fixedY);
	}

	//------------------------------------------------------------------

	public SimpleViewNode(Point point,
						  int   width,
						  int   height)
	{
		this((--width > 0)  ? clampX((double)(point.x - EnvelopeView.LEFT_MARGIN) / (double)width) : 0.0,
			 (--height > 0) ? clampY((double)(height - point.y + EnvelopeView.TOP_MARGIN) / (double)height) : 0.0);
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
						 EnvelopeView.TOP_MARGIN + height - (int)Math.round(y * (double)height));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public SimpleViewNode clone()
	{
		return (SimpleViewNode)super.clone();
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
