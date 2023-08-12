/*====================================================================*\

CompoundNode.java

Class: compound envelope node.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.envelope;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//----------------------------------------------------------------------


// 	CLASS: COMPOUND ENVELOPE NODE


public class CompoundNode
	extends AbstractNode
	implements Cloneable
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final	int	MAX_NUM_BANDS	= Integer.SIZE;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	public	double[]	ys;
	public	int			fixedY;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public CompoundNode(double   x,
						double[] ys)
	{
		this(x, ys, false, 0);
	}

	//------------------------------------------------------------------

	public CompoundNode(double   x,
						double[] ys,
						boolean  fixedX)
	{
		this(x, ys, fixedX, 0);
	}

	//------------------------------------------------------------------

	public CompoundNode(double   x,
						double[] ys,
						boolean  fixedX,
						int      fixedY)
	{
		if ((x < MIN_X) || (x > MAX_X))
			throw new IllegalArgumentException("x coordinate out of bounds: " + x);
		if (ys.length > MAX_NUM_BANDS)
			throw new IllegalArgumentException("Too many y coordinates");
		for (int i = 0; i < ys.length; i++)
		{
			double y = ys[i];
			if ((y < MIN_Y) || (y > MAX_Y))
				throw new IllegalArgumentException("y coordinate out of bounds: " + y);
		}

		this.x = x;
		this.ys = ys.clone();
		this.fixedX = fixedX;
		this.fixedY = fixedY;
	}

	//------------------------------------------------------------------

	public CompoundNode(CompoundNode node)
	{
		this(node.x, node.ys, node.fixedX, node.fixedY);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static List<CompoundNode> toList(Iterable<? extends AbstractNode> nodes)
	{
		List<CompoundNode> outNodes = new ArrayList<>();
		for (AbstractNode node : nodes)
		{
			if (node instanceof CompoundNode)
				outNodes.add((CompoundNode)node);
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
		return fixedX && isFixedY(bandIndex);
	}

	//------------------------------------------------------------------

	@Override
	public boolean isPartiallyFixed(int bandIndex)
	{
		return fixedX || isFixedY(bandIndex);
	}

	//------------------------------------------------------------------

	@Override
	public double getY(int bandIndex)
	{
		return ys[bandIndex];
	}

	//------------------------------------------------------------------

	@Override
	public void setY(int    bandIndex,
					 double y)
	{
		this.ys[bandIndex] = y;
	}

	//------------------------------------------------------------------

	@Override
	public CompoundNode clone()
	{
		CompoundNode copy = (CompoundNode)super.clone();
		copy.ys = ys.clone();
		return copy;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public boolean isFixedY(int bandIndex)
	{
		return ((fixedY & (1 << bandIndex)) != 0);
	}

	//------------------------------------------------------------------

	public boolean equals(CompoundNode node)
	{
		return (node != null) && (x == node.x) && Arrays.equals(ys, node.ys);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
