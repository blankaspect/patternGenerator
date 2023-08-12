/*====================================================================*\

CompoundEnvelope.java

Class: compound envelope.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.envelope;

//----------------------------------------------------------------------


// IMPORTS


import java.util.List;

//----------------------------------------------------------------------


// CLASS: COMPOUND ENVELOPE


public class CompoundEnvelope<T extends CompoundNode>
	extends AbstractEnvelope<T>
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String[]	names;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public CompoundEnvelope(EnvelopeKind kind,
							int          numBands)
	{
		// Call superclass constructor
		super(kind);

		// Initialise instance variables
		names = new String[numBands];

		// Set band mask
		setBandMask((1 << numBands) - 1);
	}

	//------------------------------------------------------------------

	public CompoundEnvelope(EnvelopeKind kind,
							int          numBands,
							String[]     names)
	{
		// Call superclass constructor
		super(kind);

		// Validate arguments
		if (names.length != numBands)
			throw new IllegalArgumentException("Incorrect number of names");

		// Initialise instance variables
		this.names = names.clone();

		// Set band mask
		setBandMask((1 << numBands) - 1);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public int getNumBands()
	{
		return names.length;
	}

	//------------------------------------------------------------------

	@Override
	public String getName(int bandIndex)
	{
		return names[bandIndex];
	}

	//------------------------------------------------------------------

	@Override
	public void setName(int    bandIndex,
						String name)
	{
		names[bandIndex] = name;
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
				node1.ys = node0.ys.clone();
				node1.fixedY = node0.fixedY;
			}
			else if (!node1.ys.equals(node0.ys) || (node1.fixedY != node0.fixedY))
				throw new IllegalArgumentException();
		}

		double prevX = -1.0;
		for (int i = 0; i < numNodes; i++)
		{
			T node = nodes.get(i);
			if ((node.x < AbstractNode.MIN_X) || (node.x > AbstractNode.MAX_X) || (node.x - prevX < getMinDeltaX()))
				throw new IllegalArgumentException();
			prevX = node.x;

			double prevY = 0.0;
			for (int j = 0; j < getNumBands(); j++)
			{
				double y = node.ys[j];
				if ((y < AbstractNode.MIN_Y) || (y > AbstractNode.MAX_Y) || (y < prevY))
					throw new IllegalArgumentException();
				prevY = y;
			}
		}

		getNodes().clear();
		for (T node : nodes)
			getNodes().add(node);

		setLoop(loop);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
