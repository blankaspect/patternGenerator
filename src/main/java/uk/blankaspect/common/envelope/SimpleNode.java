/*====================================================================*\

SimpleNode.java

Class: simple envelope node.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.envelope;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.List;

//----------------------------------------------------------------------


// 	CLASS: SIMPLE ENVELOPE NODE


public class SimpleNode
	extends AbstractNode
	implements Cloneable
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	public	double	y;
	public	boolean	fixedY;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public SimpleNode(double x,
					  double y)
	{
		this(x, y, false, false);
	}

	//------------------------------------------------------------------

	public SimpleNode(double  x,
					  double  y,
					  boolean fixedX)
	{
		this(x, y, fixedX, false);
	}

	//------------------------------------------------------------------

	public SimpleNode(double  x,
					  double  y,
					  boolean fixedX,
					  boolean fixedY)
	{
		if ((x < MIN_X) || (x > MAX_X))
			throw new IllegalArgumentException("x coordinate out of bounds: " + x);
		if ((y < MIN_Y) || (y > MAX_Y))
			throw new IllegalArgumentException("y coordinate out of bounds: " + y);

		this.x = x;
		this.y = y;
		this.fixedX = fixedX;
		this.fixedY = fixedY;
	}

	//------------------------------------------------------------------

	public SimpleNode(SimpleNode node)
	{
		this(node.x, node.y, node.fixedX, node.fixedY);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static List<SimpleNode> toList(Iterable<? extends AbstractNode> nodes)
	{
		List<SimpleNode> outNodes = new ArrayList<>();
		for (AbstractNode node : nodes)
		{
			if (node instanceof SimpleNode simpleNode)
				outNodes.add(simpleNode);
		}
		return outNodes;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public boolean isFixed(int bandIndex)
	{
		return fixedX && fixedY;
	}

	//------------------------------------------------------------------

	@Override
	public boolean isPartiallyFixed(int bandIndex)
	{
		return fixedX || fixedY;
	}

	//------------------------------------------------------------------

	@Override
	public double getY(int bandIndex)
	{
		return y;
	}

	//------------------------------------------------------------------

	@Override
	public void setY(int    bandIndex,
					 double y)
	{
		this.y = y;
	}

	//------------------------------------------------------------------

	@Override
	public SimpleNode clone()
	{
		return (SimpleNode)super.clone();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public boolean equals(SimpleNode node)
	{
		return (node != null) && (x == node.x) && (y == node.y);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
