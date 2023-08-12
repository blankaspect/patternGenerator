/*====================================================================*\

SimpleEnvelope.java

Class: simple envelope.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.envelope;

//----------------------------------------------------------------------


// IMPORTS


import java.util.List;

//----------------------------------------------------------------------


// CLASS: SIMPLE ENVELOPE


public class SimpleEnvelope<T extends SimpleNode>
	extends AbstractEnvelope<T>
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String	name;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public SimpleEnvelope(EnvelopeKind kind)
	{
		// Call alternative constructor
		this(kind, null);
	}

	//------------------------------------------------------------------

	public SimpleEnvelope(EnvelopeKind kind,
						  String       name)
	{
		// Call superclass constructor
		super(kind);

		// Set name
		setName(0, name);

		// Set band mask
		setBandMask(0b1);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public int getNumBands()
	{
		return 1;
	}

	//------------------------------------------------------------------

	@Override
	public String getName(int bandIndex)
	{
		return name;
	}

	//------------------------------------------------------------------

	@Override
	public void setName(int    bandIndex,
						String name)
	{
		this.name = name;
	}

	//------------------------------------------------------------------

	@Override
	public void setNodes(List<T> nodes,
						 boolean loop,
						 boolean forceLoop)
	{
		int numNodes = nodes.size();
		if (!getNumNodesRange().contains(numNodes))
			throw new IllegalArgumentException();

		if (loop)
		{
			if (numNodes < 2)
				throw new IllegalArgumentException("Too few nodes");

			T node0 = nodes.get(0);
			T node1 = nodes.get(numNodes - 1);
			if ((node0.x != AbstractNode.MIN_X) || !node0.fixedX || (node1.x != AbstractNode.MAX_X) || !node1.fixedX)
				throw new IllegalArgumentException();
			if (forceLoop)
			{
				node1.y = node0.y;
				node1.fixedY = node0.fixedY;
			}
			else if ((node1.y != node0.y) || (node1.fixedY != node0.fixedY))
				throw new IllegalArgumentException();
		}

		double prevX = -1.0;
		for (int i = 0; i < numNodes; i++)
		{
			T node = nodes.get(i);
			if ((node.x < AbstractNode.MIN_X) || (node.x > AbstractNode.MAX_X) || (node.x - prevX < getMinDeltaX()))
				throw new IllegalArgumentException();
			prevX = node.x;

			if ((node.y < AbstractNode.MIN_Y) || (node.y > AbstractNode.MAX_Y))
				throw new IllegalArgumentException();
		}

		getNodes().clear();
		for (T node : nodes)
			getNodes().add(node);

		setLoop(loop);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
