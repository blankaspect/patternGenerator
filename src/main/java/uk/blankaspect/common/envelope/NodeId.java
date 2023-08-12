/*====================================================================*\

NodeId.java

Class: node identifier.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.envelope;

//----------------------------------------------------------------------


// IMPORTS


import uk.blankaspect.common.exception.UnexpectedRuntimeException;

//----------------------------------------------------------------------


// CLASS: NODE IDENTIFIER


public class NodeId
	implements Cloneable
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	public	int	envelopeIndex;
	public	int	bandIndex;
	public	int	nodeIndex;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public NodeId()
	{
	}

	//------------------------------------------------------------------

	public NodeId(int envelopeIndex,
				  int bandIndex,
				  int nodeIndex)
	{
		this.envelopeIndex = envelopeIndex;
		this.bandIndex = bandIndex;
		this.nodeIndex = nodeIndex;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public NodeId clone()
	{
		try
		{
			return (NodeId)super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new UnexpectedRuntimeException(e);
		}
	}

	//------------------------------------------------------------------

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (obj instanceof NodeId)
		{
			NodeId id = (NodeId)obj;
			return (envelopeIndex == id.envelopeIndex) && (bandIndex == id.bandIndex) && (nodeIndex == id.nodeIndex);
		}
		return false;
	}

	//------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		return envelopeIndex << 20 | bandIndex << 10 | nodeIndex;
	}

	//------------------------------------------------------------------

	@Override
	public String toString()
	{
		return new String(envelopeIndex + ", " + bandIndex + ", " + nodeIndex);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
