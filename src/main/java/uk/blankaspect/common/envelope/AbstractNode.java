/*====================================================================*\

AbstractNode.java

Class: abstract envelope node.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.envelope;

import uk.blankaspect.common.exception2.UnexpectedRuntimeException;

//----------------------------------------------------------------------


// 	CLASS: ABSTRACT ENVELOPE NODE


public abstract class AbstractNode
	implements Cloneable, Comparable<AbstractNode>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		double	MIN_X	= 0.0;
	public static final		double	MAX_X	= 1.0;

	public static final		double	MIN_Y	= 0.0;
	public static final		double	MAX_Y	= 1.0;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	public	double	x;
	public	boolean	fixedX;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	protected AbstractNode()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Abstract methods
////////////////////////////////////////////////////////////////////////

	public abstract boolean isFixed(int bandIndex);

	//------------------------------------------------------------------

	public abstract boolean isPartiallyFixed(int bandIndex);

	//------------------------------------------------------------------

	public abstract double getY(int bandIndex);

	//------------------------------------------------------------------

	public abstract void setY(int    bandIndex,
							  double y);

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static double clampX(double value)
	{
		return Math.min(Math.max(MIN_X, value), MAX_X);
	}

	//------------------------------------------------------------------

	public static double clampY(double value)
	{
		return Math.min(Math.max(MIN_Y, value), MAX_Y);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : Comparable interface
////////////////////////////////////////////////////////////////////////

	@Override
	public int compareTo(AbstractNode other)
	{
		return Double.compare(x, other.x);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public AbstractNode clone()
	{
		try
		{
			return (AbstractNode)super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new UnexpectedRuntimeException(e);
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
