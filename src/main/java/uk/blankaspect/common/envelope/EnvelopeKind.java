/*====================================================================*\

EnvelopeKind.java

Enumeration: abstract envelope node.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.envelope;

//----------------------------------------------------------------------


// IMPORTS


import java.util.stream.Stream;

import uk.blankaspect.common.misc.IStringKeyed;

//----------------------------------------------------------------------


// ENUMERATION: ENVELOPE KIND


public enum EnvelopeKind
	implements IStringKeyed
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	LINEAR
	(
		"linear",
		"Linear"
	),

	CUBIC_SEGMENT
	(
		"cubicSegment",
		"Cubic segment"
	),

	CUBIC_SPLINE_A
	(
		"cubicSplineA",
		"Cubic spline A"
	),

	CUBIC_SPLINE_B
	(
		"cubicSplineB",
		"Cubic spline B"
	);

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String	key;
	private	String	text;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private EnvelopeKind(String key,
						 String text)
	{
		this.key = key;
		this.text = text;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static EnvelopeKind get(int index)
	{
		return ((index >= 0) && (index < values().length)) ? values()[index] : null;
	}

	//------------------------------------------------------------------

	public static EnvelopeKind forKey(String key)
	{
		return Stream.of(values())
				.filter(value -> value.key.equals(key))
				.findFirst()
				.orElse(null);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IStringKeyed interface
////////////////////////////////////////////////////////////////////////

	@Override
	public String getKey()
	{
		return key;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public String toString()
	{
		return text;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
