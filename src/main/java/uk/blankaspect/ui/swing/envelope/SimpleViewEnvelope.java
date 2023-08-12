/*====================================================================*\

SimpleViewEnvelope.java

Class: simple view envelope.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.envelope;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;

import uk.blankaspect.common.envelope.EnvelopeKind;
import uk.blankaspect.common.envelope.SimpleEnvelope;

//----------------------------------------------------------------------


// CLASS: SIMPLE VIEW ENVELOPE


public class SimpleViewEnvelope
	extends SimpleEnvelope<SimpleViewNode>
	implements IViewEnvelope
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Color	segmentColour;
	private	Color	nodeColour;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public SimpleViewEnvelope(EnvelopeKind kind)
	{
		this(kind, EnvelopeView.DEFAULT_SEGMENT_COLOUR, EnvelopeView.DEFAULT_NODE_COLOUR);
	}

	//------------------------------------------------------------------

	public SimpleViewEnvelope(EnvelopeKind kind,
							  Color        segmentColour,
							  Color        nodeColour)
	{
		super(kind);

		this.segmentColour = segmentColour;
		this.nodeColour = nodeColour;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IViewEnvelope interface
////////////////////////////////////////////////////////////////////////

	@Override
	public Color getSegmentColour(int bandIndex)
	{
		return segmentColour;
	}

	//------------------------------------------------------------------

	@Override
	public void setSegmentColour(int bandIndex, Color colour)
	{
		segmentColour = colour;
	}

	//------------------------------------------------------------------

	@Override
	public Color getNodeColour(int bandIndex)
	{
		return nodeColour;
	}

	//------------------------------------------------------------------

	@Override
	public void setNodeColour(int bandIndex, Color colour)
	{
		nodeColour = colour;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
