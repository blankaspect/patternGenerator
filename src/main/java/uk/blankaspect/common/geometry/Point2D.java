/*====================================================================*\

Point2D.java

Class: two-dimensional point.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.geometry;

//----------------------------------------------------------------------


// IMPORTS


import java.util.Comparator;

import uk.blankaspect.common.exception2.UnexpectedRuntimeException;

import uk.blankaspect.common.function.IFunction1;

//----------------------------------------------------------------------


// CLASS: TWO-DIMENSIONAL POINT


public class Point2D
	implements Cloneable
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final	Point2D	ORIGIN	= new Point2D(0.0, 0.0);

	public static final	Comparator<Point2D>	X_COMPARATOR	= Comparator.comparingDouble(p -> p.x);
	public static final	Comparator<Point2D>	Y_COMPARATOR	= Comparator.comparingDouble(p -> p.y);

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	double	x;
	private	double	y;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public Point2D(
		double	x,
		double	y)
	{
		// Initialise instance variables
		this.x = x;
		this.y = y;
	}

	//------------------------------------------------------------------

	public Point2D(
		double[]	coords)
	{
		// Initialise instance variables
		x = coords[0];
		y = coords[1];
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public boolean equals(
		Object	obj)
	{
		if (this == obj)
			return true;

		return (obj instanceof Point2D other) && (x == other.x) && (y == other.y);
	}

	//------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		return 31 * Double.hashCode(x) + Double.hashCode(y);
	}

	//------------------------------------------------------------------

	@Override
	public Point2D clone()
	{
		try
		{
			return (Point2D)super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new UnexpectedRuntimeException(e);
		}
	}

	//------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "(" + x + ", " + y + ")";
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public double getX()
	{
		return x;
	}

	//------------------------------------------------------------------

	public double getY()
	{
		return y;
	}

	//------------------------------------------------------------------

	public Point2D transformX(
		IFunction1<Double, Double>	function)
	{
		return new Point2D(function.invoke(x), y);
	}

	//------------------------------------------------------------------

	public Point2D transformY(
		IFunction1<Double, Double>	function)
	{
		return new Point2D(x, function.invoke(y));
	}

	//------------------------------------------------------------------

	public Point2D transform(
		IFunction1<Double, Double>	xFunction,
		IFunction1<Double, Double>	yFunction)
	{
		return new Point2D(xFunction.invoke(x), yFunction.invoke(y));
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
