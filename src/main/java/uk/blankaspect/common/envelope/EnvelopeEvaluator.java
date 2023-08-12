/*====================================================================*\

EnvelopeEvaluator.java

Class: envelope evaluator.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.envelope;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.blankaspect.common.geometry.Point2D;

import uk.blankaspect.common.misc.IEvaluable;

//----------------------------------------------------------------------


// CLASS: ENVELOPE EVALUATOR


public class EnvelopeEvaluator
	implements IEvaluable
{

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: ENVELOPE NODE


	private static class Node
		extends Point2D
		implements Comparable<Node>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Node(double x,
					 double y)
		{
			super(x, y);
		}

		//--------------------------------------------------------------

		private Node(Point2D node)
		{
			this(node.getX(), node.getY());
		}

		//--------------------------------------------------------------

		private Node(SimpleNode node)
		{
			super(node.x, node.y);
		}

		//--------------------------------------------------------------

		private Node(CompoundNode node,
					 int                  index)
		{
			super(node.x, node.ys[index]);
		}

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Comparable interface
	////////////////////////////////////////////////////////////////////

		@Override
		public int compareTo(Node other)
		{
			return Double.compare(getX(), other.getX());
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	EnvelopeKind	kind;
	private	List<Point2D>	nodes;
	private	int				nodeIndex;
	private	double			evalX;
	private	double			evalY;
	private	double			x0;
	private	double			x1;
	private	double			x2;
	private	double			y0;
	private	double			y1;
	private	double			y2;
	private	double			m0;
	private	double			m1;
	private	double			a;
	private	double			b;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public EnvelopeEvaluator(SimpleEnvelope<?> envelope)
	{
		this(envelope.getNodes(), envelope.getKind());
	}

	//------------------------------------------------------------------

	public EnvelopeEvaluator(CompoundEnvelope<?> envelope,
							 int                 bandIndex)
	{
		this(envelope.getNodes(), bandIndex, envelope.getKind());
	}

	//------------------------------------------------------------------

	public EnvelopeEvaluator(Iterable<? extends SimpleNode> nodes,
							 EnvelopeKind                   kind)
	{
		this.kind = kind;
		this.nodes = new ArrayList<>();
		for (SimpleNode node : nodes)
			this.nodes.add(new Node(node));

		this.nodes.sort(Point2D.X_COMPARATOR);

		initEvaluation();
	}

	//------------------------------------------------------------------

	public EnvelopeEvaluator(Iterable<? extends CompoundNode> nodes,
							 int                              bandIndex,
							 EnvelopeKind                     kind)
	{
		this.kind = kind;
		this.nodes = new ArrayList<>();
		for (CompoundNode node : nodes)
			this.nodes.add(new Node(node, bandIndex));

		this.nodes.sort(Point2D.X_COMPARATOR);

		initEvaluation();
	}

	//------------------------------------------------------------------

	public EnvelopeEvaluator(EnvelopeKind                  kind,
							 Collection<? extends Point2D> nodes)
	{
		this.kind = kind;
		this.nodes = new ArrayList<>(nodes);

		this.nodes.sort(Point2D.X_COMPARATOR);

		initEvaluation();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IEvaluable interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void initEvaluation()
	{
		nodeIndex = 0;
		evalX = -1.0;
		switch (kind)
		{
			case CUBIC_SPLINE_A:
			{
				Point2D node = nodes.get(nodeIndex++);
				x0 = node.getX();
				y0 = node.getY();
				node = nodes.get(nodeIndex++);
				x1 = node.getX();
				y1 = node.getY();
				node = (nodeIndex < nodes.size()) ? nodes.get(nodeIndex++)
												  : new Point2D(x1 + (x1 - x0), y1 + (y1 - y0));
				x2 = node.getX();
				y2 = node.getY();

				double dx01 = x1 - x0;
				double dx02 = x2 - x0;
				double dx12 = x2 - x1;
				double dx01_2 = dx01 * dx01;
				double dx01_3 = dx01_2 * dx01;
				double dy01 = y1 - y0;
				double dy02 = y2 - y0;
				double dy12 = y2 - y1;

				m0 = dy01 / dx01 - (dx01 / dx02) * (dy12 / dx12 - dy01 / dx01);
				m1 = dy02 / dx02;
				a = (m0 + m1) / dx01_2 - 2.0 * dy01 / dx01_3;
				b = 3 * dy01 / dx01_2 - (2.0 * m0 + m1) / dx01;
				break;
			}

			case CUBIC_SPLINE_B:
			{
				Point2D node = nodes.get(nodeIndex++);
				x0 = node.getX();
				y0 = node.getY();
				node = nodes.get(nodeIndex++);
				x1 = node.getX();
				y1 = node.getY();
				node = (nodeIndex < nodes.size()) ? nodes.get(nodeIndex++)
												  : new Point2D(x1 + (x1 - x0), y1 + (y1 - y0));
				x2 = node.getX();
				y2 = node.getY();

				double dx01 = x1 - x0;
				double dx02 = x2 - x0;
				double dx12 = x2 - x1;
				double dx01_2 = dx01 * dx01;
				double dx02_2 = dx02 * dx02;
				double dy01 = y1 - y0;
				double dy12 = y2 - y1;

				m0 = dy01 / dx01 - dx01 / dx02 * (dy12 / dx12 - dy01 / dx01);
				a = dy12 / (dx12 * dx02_2) - dy01 * (dx01 + dx02) / (dx01_2 * dx02_2) + m0 / (dx01 * dx02);
				b = dy01 / dx01_2 - a * dx01 - m0 / dx01;
				break;
			}

			default:
				// do nothing
				break;
		}
	}

	//------------------------------------------------------------------

	@Override
	public double evaluate(double x)
	{
		if ((x < AbstractNode.MIN_X) || (x > AbstractNode.MAX_X))
			throw new IllegalArgumentException();

		if (x != evalX)
		{
			evalX = x;
			switch (kind)
			{
				case LINEAR:
				{
					Point2D node = nodes.get(nodeIndex);
					x0 = node.getX();
					y0 = node.getY();
					node = nodes.get(nodeIndex + 1);
					x1 = node.getX();
					y1 = node.getY();
					while (x > x1)
					{
						x0 = x1;
						y0 = y1;
						++nodeIndex;
						node = nodes.get(nodeIndex + 1);
						x1 = node.getX();
						y1 = node.getY();
					}

					evalY = (x0 == x1) ? 0.5 * (y0 + y1)
									   : y0 + (x - x0) * (y1 - y0) / (x1 - x0);
					break;
				}

				case CUBIC_SEGMENT:
				{
					Point2D node = nodes.get(nodeIndex);
					x0 = node.getX();
					y0 = node.getY();
					node = nodes.get(nodeIndex + 1);
					x1 = node.getX();
					y1 = node.getY();
					while (x > x1)
					{
						x0 = x1;
						y0 = y1;
						++nodeIndex;
						node = nodes.get(nodeIndex + 1);
						x1 = node.getX();
						y1 = node.getY();
					}

					if (x0 == x1)
						evalY = 0.5 * (y0 + y1);
					else
					{
						double a = 2.0 * (y1 - y0) / (x0 * x0 * (x0 - 3.0 * x1) - x1 * x1 * (x1 - 3.0 * x0));
						double b = -1.5 * a * (x0 + x1);
						double c = 3.0 * a * x0 * x1;
						double d = y0 + 0.5 * a * x0 * x0 * (x0 - 3.0 * x1);
						evalY = ((a * x + b) * x + c) * x + d;
					}
					break;
				}

				case CUBIC_SPLINE_A:
				{
					while (x >= x1)
					{
						x0 = x1;
						y0 = y1;

						x1 = x2;
						y1 = y2;

						Point2D node = (nodeIndex < nodes.size()) ? nodes.get(nodeIndex++)
																  : new Point2D(x1 + (x1 - x0), y1 + (y1 - y0));
						x2 = node.getX();
						y2 = node.getY();

						double dx01 = x1 - x0;
						double dx02 = x2 - x0;
						double dx01_2 = dx01 * dx01;
						double dx01_3 = dx01_2 * dx01;
						double dy01 = y1 - y0;
						double dy02 = y2 - y0;

						m0 = Double.isNaN(m1) ? dy01 / dx01 - dx01 / dx02 * ((y2 - y1) / (x2 - x1) - dy01 / dx01)
											  : m1;
						m1 = dy02 / dx02;

						a = (m0 + m1) / dx01_2 - 2.0 * dy01 / dx01_3;
						b = 3 * dy01 / dx01_2 - (2.0 * m0 + m1) / dx01;
					}
					double dx = x - x0;
					evalY = ((a * dx + b) * dx + m0) * dx + y0;
					break;
				}

				case CUBIC_SPLINE_B:
				{
					while ((x >= x1) && (nodeIndex < nodes.size()))
					{
						double prevDx01 = x1 - x0;

						x0 = x1;
						y0 = y1;

						x1 = x2;
						y1 = y2;

						Point2D node = nodes.get(nodeIndex++);
						x2 = node.getX();
						y2 = node.getY();

						double dx01 = x1 - x0;
						double dx02 = x2 - x0;
						double dx12 = x2 - x1;
						double dx01_2 = dx01 * dx01;
						double dx02_2 = dx02 * dx02;
						double dy01 = y1 - y0;
						double dy12 = y2 - y1;

						if (Double.isNaN(m0))
							m0 = dy01 / dx01 - dx01 / dx02 * (dy12 / dx12 - dy01 / dx01);
						else
							m0 += (3.0 * a * prevDx01 + 2.0 * b) * prevDx01;
						a = dy12 / (dx12 * dx02_2) - dy01 * (dx01 + dx02) / (dx01_2 * dx02_2) + m0 / (dx01 * dx02);
						b = dy01 / dx01_2 - a * dx01 - m0 / dx01;
					}
					double dx = x - x0;
					evalY = ((a * dx + b) * dx + m0) * dx + y0;
					break;
				}
			}
		}

		return evalY;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
