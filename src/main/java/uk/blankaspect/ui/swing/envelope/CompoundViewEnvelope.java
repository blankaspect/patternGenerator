/*====================================================================*\

CompoundViewEnvelope.java

Class: compound view envelope.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.envelope;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;

import java.util.Arrays;

import uk.blankaspect.common.envelope.CompoundEnvelope;
import uk.blankaspect.common.envelope.EnvelopeKind;

//----------------------------------------------------------------------


// CLASS: COMPOUND VIEW ENVELOPE


public class CompoundViewEnvelope
	extends CompoundEnvelope<CompoundViewNode>
	implements IViewEnvelope
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Color[]	segmentColours;
	private	Color[]	nodeColours;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public CompoundViewEnvelope(EnvelopeKind kind,
								int          numBands)
	{
		super(kind, numBands);

		segmentColours = new Color[numBands];
		Arrays.fill(segmentColours, EnvelopeView.DEFAULT_SEGMENT_COLOUR);
		nodeColours = new Color[numBands];
		Arrays.fill(nodeColours, EnvelopeView.DEFAULT_NODE_COLOUR);
	}

	//------------------------------------------------------------------

	public CompoundViewEnvelope(EnvelopeKind kind,
								int          numBands,
								Color[]      segmentColours,
								Color[]      nodeColours)
	{
		super(kind, numBands);

		this.segmentColours = segmentColours.clone();
		this.nodeColours = nodeColours.clone();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IViewEnvelope interface
////////////////////////////////////////////////////////////////////////

	@Override
	public Color getSegmentColour(int bandIndex)
	{
		return segmentColours[bandIndex];
	}

	//------------------------------------------------------------------

	@Override
	public void setSegmentColour(int bandIndex, Color colour)
	{
		segmentColours[bandIndex] = colour;
	}

	//------------------------------------------------------------------

	@Override
	public Color getNodeColour(int bandIndex)
	{
		return nodeColours[bandIndex];
	}

	//------------------------------------------------------------------

	@Override
	public void setNodeColour(int bandIndex, Color colour)
	{
		nodeColours[bandIndex] = colour;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
