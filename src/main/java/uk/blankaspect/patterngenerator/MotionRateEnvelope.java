/*====================================================================*\

MotionRateEnvelope.java

Motion-rate envelope class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.blankaspect.common.envelope.EnvelopeEvaluator;
import uk.blankaspect.common.envelope.EnvelopeKind;
import uk.blankaspect.common.envelope.SimpleNode;

import uk.blankaspect.common.exception.UnexpectedRuntimeException;

import uk.blankaspect.ui.swing.envelope.SimpleViewNode;

//----------------------------------------------------------------------


// MOTION-RATE ENVELOPE CLASS


class MotionRateEnvelope
	implements Cloneable
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public MotionRateEnvelope(Collection<? extends SimpleNode> nodes,
							  double                           xCoeff,
							  double                           yCoeff)
	{
		this.nodes = new ArrayList<>();
		for (SimpleNode node : nodes)
			this.nodes.add(new SimpleViewNode(node));
		this.xCoeff = xCoeff;
		this.yCoeff = yCoeff;
		envelopeEvaluator = new EnvelopeEvaluator(nodes, EnvelopeKind.LINEAR);
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	public MotionRateEnvelope(String str)
	{
		// Split string into nodes and coefficient pair
		String[] strs = str.trim().split("\\s*\\)\\s*,\\s*", -1);
		if (strs.length < 3)
			throw new IllegalArgumentException();

		// Parse nodes
		nodes = new ArrayList<>();
		for (int i = 0; i < strs.length - 1; i++)
		{
			if (!strs[i].startsWith("("))
				throw new IllegalArgumentException();
			String[] coordStrs = strs[i].substring(1).trim().split("\\s*,\\s*", -1);
			if (coordStrs.length != 2)
				throw new IllegalArgumentException();
			nodes.add(new SimpleViewNode(Double.parseDouble(coordStrs[0]), Double.parseDouble(coordStrs[1]),
										 (i == 0) || (i == strs.length - 2)));
		}

		// Test x coordinates of first and last nodes
		if ((nodes.get(0).x != 0.0) || (nodes.get(nodes.size() - 1).x != 1.0))
			throw new IllegalArgumentException();

		// Split coefficients
		String[] coeffStrs = strs[strs.length - 1].split("\\s*,\\s*", -1);
		if (coeffStrs.length != 2)
			throw new IllegalArgumentException();

		// Parse x coefficient
		xCoeff = Double.parseDouble(coeffStrs[0]);
		if (xCoeff == 0.0)
			throw new IllegalArgumentException();

		// Parse y coefficient
		yCoeff = Double.parseDouble(coeffStrs[1]);
		if (yCoeff == 0.0)
			throw new IllegalArgumentException();

		// Initialise remaining instance variables
		envelopeEvaluator = new EnvelopeEvaluator(nodes, EnvelopeKind.LINEAR);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public MotionRateEnvelope clone()
	{
		try
		{
			MotionRateEnvelope copy = (MotionRateEnvelope)super.clone();
			return copy;
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
		StringBuilder buffer = new StringBuilder(256);
		for (SimpleNode node : nodes)
		{
			buffer.append('(');
			buffer.append(AppConstants.FORMAT_1_8.format(node.x));
			buffer.append(", ");
			buffer.append(AppConstants.FORMAT_1_8.format(node.y));
			buffer.append("), ");
		}
		buffer.append(xCoeff);
		buffer.append(", ");
		buffer.append(yCoeff);
		return buffer.toString();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public List<SimpleViewNode> getNodes()
	{
		return nodes;
	}

	//------------------------------------------------------------------

	public double getXCoefficient()
	{
		return xCoeff;
	}

	//------------------------------------------------------------------

	public double getYCoefficient()
	{
		return yCoeff;
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	public double evaluate(int frameIndex)
	{
		envelopeEvaluator.initEvaluation();
		return (envelopeEvaluator.evaluate(((double)frameIndex % xCoeff) / xCoeff) * yCoeff);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	List<SimpleViewNode>	nodes;
	private	double					xCoeff;
	private	double					yCoeff;
	private	EnvelopeEvaluator		envelopeEvaluator;

}

//----------------------------------------------------------------------
