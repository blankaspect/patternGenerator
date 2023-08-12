/*====================================================================*\

AbstractEnvelope.java

Class: abstract envelope.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.envelope;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.List;

import uk.blankaspect.common.range.IntegerRange;

//----------------------------------------------------------------------


// CLASS: ABSTRACT ENVELOPE


public abstract class AbstractEnvelope<T extends AbstractNode>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int	MIN_NUM_NODES	= 0;
	public static final		int	MAX_NUM_NODES	= 1 << 14;  // 16384

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	EnvelopeKind	kind;
	private	IntegerRange	numNodesRange;
	private	List<T>			nodes;
	private	int				bandMask;
	private	boolean			loop;
	private	double			minDeltaX;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	protected AbstractEnvelope(EnvelopeKind kind)
	{
		this.kind = kind;
		numNodesRange = new IntegerRange(MIN_NUM_NODES, MAX_NUM_NODES);
		nodes = new ArrayList<>();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Abstract methods
////////////////////////////////////////////////////////////////////////

	public abstract int getNumBands();

	//------------------------------------------------------------------

	public abstract String getName(int bandIndex);

	//------------------------------------------------------------------

	public abstract void setName(int    bandIndex,
								 String name);

	//------------------------------------------------------------------

	public abstract void setNodes(List<T> nodes,
								  boolean loop,
								  boolean forceLoop);

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public EnvelopeKind getKind()
	{
		return kind;
	}

	//------------------------------------------------------------------

	public void setKind(EnvelopeKind kind)
	{
		this.kind = kind;
	}

	//------------------------------------------------------------------

	public IntegerRange getNumNodesRange()
	{
		return numNodesRange;
	}

	//------------------------------------------------------------------

	public int getNumNodes()
	{
		return nodes.size();
	}

	//------------------------------------------------------------------

	public T getNode(int index)
	{
		return nodes.get(index);
	}

	//------------------------------------------------------------------

	public void setNode(int index,
						T   node)
	{
		nodes.set(index, node);
	}

	//------------------------------------------------------------------

	public List<T> getNodes()
	{
		return nodes;
	}

	//------------------------------------------------------------------

	public void setNodes(List<T> nodes)
	{
		setNodes(nodes, false, false);
	}

	//------------------------------------------------------------------

	public void setNodes(List<T> nodes,
						 boolean loop)
	{
		setNodes(nodes, loop, false);
	}

	//------------------------------------------------------------------

	public int getBandMask()
	{
		return bandMask;
	}

	//------------------------------------------------------------------

	public void setBandMask(int mask)
	{
		bandMask = mask;
	}

	//------------------------------------------------------------------

	public boolean isLoop()
	{
		return loop;
	}

	//------------------------------------------------------------------

	public void setLoop(boolean loop)
	{
		this.loop = loop;
	}

	//------------------------------------------------------------------

	public double getMinDeltaX()
	{
		return minDeltaX;
	}

	//------------------------------------------------------------------

	public void setMinDeltaX(double minDeltaX)
	{
		if ((minDeltaX < 0.0) || (minDeltaX >= 1.0))
			throw new IllegalArgumentException();

		this.minDeltaX = minDeltaX;
	}

	//------------------------------------------------------------------

	public void setNumNodesRange(IntegerRange range)
	{
		if ((range.lowerBound < MIN_NUM_NODES) || (range.lowerBound > MAX_NUM_NODES)
				|| (range.upperBound < MIN_NUM_NODES) || (range.upperBound > MAX_NUM_NODES)
				|| (range.upperBound < range.lowerBound))
			throw new IllegalArgumentException();
		numNodesRange = range.clone();
	}

	//------------------------------------------------------------------

	public boolean isValidNodeX(double x)
	{
		for (AbstractNode node : nodes)
		{
			if (Math.abs(node.x - x) < minDeltaX)
				return false;
		}
		return true;
	}

	//------------------------------------------------------------------

	public boolean hasDiscreteSegments()
	{
		switch (kind)
		{
			case LINEAR:
			case CUBIC_SEGMENT:
				return true;

			default:
				return false;
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
