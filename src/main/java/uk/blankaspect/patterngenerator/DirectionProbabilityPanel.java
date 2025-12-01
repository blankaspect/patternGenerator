/*====================================================================*\

DirectionProbabilityPanel.java

Direction probability panel class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import java.util.EnumMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import uk.blankaspect.ui.swing.colour.Colours;

import uk.blankaspect.ui.swing.misc.GuiUtils;

//----------------------------------------------------------------------


// DIRECTION PROBABILITY PANEL CLASS


class DirectionProbabilityPanel
	extends JComponent
	implements MouseListener, MouseMotionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		WIDTH	= 212;
	private static final	int		HEIGHT	= 232;

	private static final	int		CENTRE_X	= WIDTH / 2;
	private static final	int		CENTRE_Y	= HEIGHT / 2;

	private static final	double	HUB_DIAMETER	= 16.0;

	private static final	double	SPOKE_OFFSET	= 12.0;
	private static final	double	SPOKE_WIDTH		= 10.0;
	private static final	double	SPOKE_HEIGHT	= 100.0;

	private static final	double	SPOKE_X1	= (double)CENTRE_X - SPOKE_WIDTH * 0.5;
	private static final	double	SPOKE_X2	= SPOKE_X1 + SPOKE_WIDTH;
	private static final	double	SPOKE_Y2	= (double)CENTRE_Y - SPOKE_OFFSET;
	private static final	double	SPOKE_Y1	= SPOKE_Y2 - SPOKE_HEIGHT;

	private static final	Color	BACKGROUND_COLOUR		= Colours.BACKGROUND;
	private static final	Color	BORDER_COLOUR			= new Color(208, 208, 192);
	private static final	Color	HUB_COLOUR				= new Color(128, 144, 128);
	private static final	Color	SPOKE_BORDER_COLOUR		= new Color(160, 160, 160);
	private static final	Color	SPOKE_FILL_COLOUR		= new Color(240, 160, 64);
	private static final	Color	SPOKE_DISABLED_COLOUR	= new Color(228, 228, 220);

	private static final	Map<Pattern2Image.Direction, Point2D.Double[]>	STROKE_LINE_POINTS;
	private static final	Map<Pattern2Image.Direction, Path2D.Double>		STROKE_PATHS;
	private static final	AffineTransform									ROTATION;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Pattern2ParamsDialog					paramsDialog;
	private	Map<Pattern2Image.Direction, Integer>	probabilities;
	private	Pattern2Image.Direction.Mode			directionMode;
	private	boolean									symmetrical;
	private	Pattern2Image.Direction					activeDirection;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		STROKE_LINE_POINTS = new EnumMap<>(Pattern2Image.Direction.class);
		STROKE_PATHS = new EnumMap<>(Pattern2Image.Direction.class);
		ROTATION = new AffineTransform();
		ROTATION.setToRotation(-Math.PI / 3.0, (double)CENTRE_X, (double)CENTRE_Y);

		AffineTransform transform1 = new AffineTransform();
		transform1.setToRotation(Math.PI / 3.0);
		AffineTransform transform2 = new AffineTransform();
		transform2.setToRotation(-Math.PI / 3.0);

		double x = 0.0;
		double halfWidth = SPOKE_WIDTH * 0.5;
		double y = SPOKE_OFFSET;
		Point2D.Double p1 = new Point2D.Double(x, y);
		Point2D.Double v1 = new Point2D.Double(x - halfWidth, y);
		Point2D.Double v2 = new Point2D.Double(x + halfWidth, y);

		y += SPOKE_HEIGHT;
		Point2D.Double p2 = new Point2D.Double(x, y);
		Point2D.Double v3 = new Point2D.Double(x + halfWidth, y);
		Point2D.Double v4 = new Point2D.Double(x - halfWidth, y);

		Point2D.Double[] inPoints = null;
		Point2D.Double[] outPoints = null;

		Map<Pattern2Image.Direction, Point2D.Double[]> strokeBoxPoints =
															new EnumMap<>(Pattern2Image.Direction.class);

		inPoints = new Point2D.Double[] { p1, p2 };
		STROKE_LINE_POINTS.put(Pattern2Image.Direction.FORE, inPoints);

		outPoints = new Point2D.Double[inPoints.length];
		transform1.transform(inPoints, 0, outPoints, 0, inPoints.length);
		STROKE_LINE_POINTS.put(Pattern2Image.Direction.FORE_LEFT, outPoints);

		outPoints = new Point2D.Double[inPoints.length];
		transform2.transform(inPoints, 0, outPoints, 0, inPoints.length);
		STROKE_LINE_POINTS.put(Pattern2Image.Direction.FORE_RIGHT, outPoints);

		inPoints = new Point2D.Double[] { v1, v2, v3, v4 };
		strokeBoxPoints.put(Pattern2Image.Direction.FORE, inPoints);

		outPoints = new Point2D.Double[inPoints.length];
		transform1.transform(inPoints, 0, outPoints, 0, inPoints.length);
		strokeBoxPoints.put(Pattern2Image.Direction.FORE_LEFT, outPoints);

		outPoints = new Point2D.Double[inPoints.length];
		transform2.transform(inPoints, 0, outPoints, 0, inPoints.length);
		strokeBoxPoints.put(Pattern2Image.Direction.FORE_RIGHT, outPoints);

		y = -SPOKE_OFFSET;
		p1 = new Point2D.Double(x, y);
		v1 = new Point2D.Double(x - halfWidth, y);
		v2 = new Point2D.Double(x + halfWidth, y);

		y -= SPOKE_HEIGHT;
		p2 = new Point2D.Double(x, y);
		v3 = new Point2D.Double(x + halfWidth, y);
		v4 = new Point2D.Double(x - halfWidth, y);

		inPoints = new Point2D.Double[] { p1, p2 };
		STROKE_LINE_POINTS.put(Pattern2Image.Direction.BACK, inPoints);

		outPoints = new Point2D.Double[inPoints.length];
		transform1.transform(inPoints, 0, outPoints, 0, inPoints.length);
		STROKE_LINE_POINTS.put(Pattern2Image.Direction.BACK_RIGHT, outPoints);

		outPoints = new Point2D.Double[inPoints.length];
		transform2.transform(inPoints, 0, outPoints, 0, inPoints.length);
		STROKE_LINE_POINTS.put(Pattern2Image.Direction.BACK_LEFT, outPoints);

		inPoints = new Point2D.Double[] { v1, v2, v3, v4 };
		strokeBoxPoints.put(Pattern2Image.Direction.BACK, inPoints);

		outPoints = new Point2D.Double[inPoints.length];
		transform1.transform(inPoints, 0, outPoints, 0, inPoints.length);
		strokeBoxPoints.put(Pattern2Image.Direction.BACK_RIGHT, outPoints);

		outPoints = new Point2D.Double[inPoints.length];
		transform2.transform(inPoints, 0, outPoints, 0, inPoints.length);
		strokeBoxPoints.put(Pattern2Image.Direction.BACK_LEFT, outPoints);

		halfWidth = (double)(WIDTH / 2);
		double halfHeight = (double)(HEIGHT / 2);
		for (Pattern2Image.Direction direction : STROKE_LINE_POINTS.keySet())
		{
			for (Point2D.Double p : STROKE_LINE_POINTS.get(direction))
				p.setLocation(p.x + halfWidth, halfHeight - p.y);
		}

		for (Pattern2Image.Direction direction : strokeBoxPoints.keySet())
		{
			Point2D.Double[] points = strokeBoxPoints.get(direction);
			for (Point2D.Double p : points)
				p.setLocation(p.x + halfWidth, halfHeight - p.y);
			Path2D.Double path = new Path2D.Double();
			for (int i = 0; i < points.length; i++)
			{
				if (i == 0)
					path.moveTo(points[i].x, points[i].y);
				else
					path.lineTo(points[i].x, points[i].y);
			}
			path.closePath();
			STROKE_PATHS.put(direction, path);
		}
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public DirectionProbabilityPanel(Pattern2ParamsDialog                  paramsDialog,
									 Map<Pattern2Image.Direction, Integer> probabilities,
									 Pattern2Image.Direction.Mode          directionMode,
									 boolean                               symmetrical)
	{
		// Initialise instance variables
		this.paramsDialog = paramsDialog;
		this.probabilities = new EnumMap<>(Pattern2Image.Direction.class);
		for (Pattern2Image.Direction direction : Pattern2Image.Direction.values())
			this.probabilities.put(direction, probabilities.getOrDefault(direction, 0));
		this.directionMode = directionMode;
		this.symmetrical = symmetrical;

		// Set properties
		setOpaque(true);
		setFocusable(false);

		// Add listeners
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : MouseListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void mouseEntered(MouseEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	@Override
	public void mouseExited(MouseEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	@Override
	public void mousePressed(MouseEvent event)
	{
		requestFocusInWindow();

		if (SwingUtilities.isLeftMouseButton(event))
		{
			double x = (double)event.getX();
			double y = (double)event.getY();
			Point2D.Double point = new Point2D.Double(x, y);
			for (Pattern2Image.Direction direction : Pattern2Image.Direction.values())
			{
				if (isDirectionEnabled(direction) &&
					 (x >= SPOKE_X1) && (x < SPOKE_X2) && (y >= SPOKE_Y1) && (y < SPOKE_Y2))
				{
					activeDirection = direction;
					setProbability(y);
					break;
				}
				ROTATION.transform(point, point);
				x = point.x;
				y = point.y;
			}
		}
	}

	//------------------------------------------------------------------

	@Override
	public void mouseReleased(MouseEvent event)
	{
		if (SwingUtilities.isLeftMouseButton(event) && isAdjusting())
		{
			setProbability(event);
			activeDirection = null;
		}
	}

	//------------------------------------------------------------------

	@Override
	public void mouseClicked(MouseEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : MouseMotionListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void mouseDragged(MouseEvent event)
	{
		if (SwingUtilities.isLeftMouseButton(event) && isAdjusting())
			setProbability(event);
	}

	//------------------------------------------------------------------

	@Override
	public void mouseMoved(MouseEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(WIDTH, HEIGHT);
	}

	//------------------------------------------------------------------

	@Override
	protected void paintComponent(Graphics gr)
	{
		// Create copy of graphics context
		Graphics2D gr2d = GuiUtils.copyGraphicsContext(gr);

		// Set rendering hints
		gr2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
		gr2d.setRenderingHint(RenderingHints.KEY_RENDERING,      RenderingHints.VALUE_RENDER_QUALITY);
		gr2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		// Draw background
		Rectangle rect = gr2d.getClipBounds();
		gr2d.setColor(BACKGROUND_COLOUR);
		gr2d.fillRect(rect.x, rect.y, rect.width, rect.height);

		// Draw hub
		gr2d.setColor(HUB_COLOUR);
		gr2d.fill(new Ellipse2D.Double((double)(CENTRE_X - HUB_DIAMETER / 2), (double)(CENTRE_Y - HUB_DIAMETER / 2),
									   HUB_DIAMETER, HUB_DIAMETER));

		// Fill spokes
		Stroke stroke = gr2d.getStroke();
		gr2d.setStroke(new BasicStroke((float)SPOKE_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
		for (Pattern2Image.Direction direction : STROKE_LINE_POINTS.keySet())
		{
			Point2D.Double[] points = STROKE_LINE_POINTS.get(direction);
			if (isDirectionEnabled(direction))
			{
				gr2d.setColor(SPOKE_FILL_COLOUR);
				double prob = (double)probabilities.get(direction) / (double)Pattern2Params.MAX_DIRECTION_PROBABILITY;
				Line2D.Double line = new Line2D.Double(points[0].x, points[0].y,
													   points[0].x + (points[1].x - points[0].x) * prob,
													   points[0].y + (points[1].y - points[0].y) * prob);
				gr2d.draw(line);
			}
			else
			{
				gr2d.setColor(SPOKE_DISABLED_COLOUR);
				gr2d.draw(new Line2D.Double(points[0], points[1]));
			}
		}

		// Draw spoke borders
		gr2d.setStroke(stroke);
		for (Pattern2Image.Direction direction : STROKE_PATHS.keySet())
		{
			gr2d.setColor(isDirectionEnabled(direction) ? SPOKE_BORDER_COLOUR : SPOKE_DISABLED_COLOUR);
			gr2d.draw(STROKE_PATHS.get(direction));
		}

		// Draw border
		gr2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
		gr2d.setColor(BORDER_COLOUR);
		gr2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public void setDirectionMode(Pattern2Image.Direction.Mode directionMode)
	{
		if (this.directionMode != directionMode)
		{
			this.directionMode = directionMode;
			repaint();
		}
	}

	//------------------------------------------------------------------

	public void setProbability(Pattern2Image.Direction direction,
							   int                     value)
	{
		if (isDirectionEnabled(direction) && (value != probabilities.get(direction)))
		{
			probabilities.put(direction, value);
			repaint();
		}
	}

	//------------------------------------------------------------------

	public void setSymmetrical(boolean symmetrical)
	{
		this.symmetrical = symmetrical;
	}

	//------------------------------------------------------------------

	private boolean isDirectionEnabled(Pattern2Image.Direction direction)
	{
		return (directionMode != Pattern2Image.Direction.Mode.RELATIVE) || (direction != Pattern2Image.Direction.BACK);
	}

	//------------------------------------------------------------------

	private boolean isAdjusting()
	{
		return (activeDirection != null);
	}

	//------------------------------------------------------------------

	private void setProbability(MouseEvent event)
	{
		Point2D.Double point = new Point2D.Double((double)event.getX(), (double)event.getY());
		for (Pattern2Image.Direction direction : Pattern2Image.Direction.values())
		{
			if (direction == activeDirection)
				break;
			ROTATION.transform(point, point);
		}
		setProbability(point.y);
	}

	//------------------------------------------------------------------

	private void setProbability(double y)
	{
		int value = Math.min(Math.max(Pattern2Params.MIN_DIRECTION_PROBABILITY,
									  (int)Math.round(SPOKE_Y2 - y)),
							 Pattern2Params.MAX_DIRECTION_PROBABILITY);
		boolean changed = false;
		if (value != probabilities.get(activeDirection))
		{
			probabilities.put(activeDirection, value);
			paramsDialog.setProbability(activeDirection, value);
			changed = true;
		}
		if (symmetrical)
		{
			Pattern2Image.Direction direction = activeDirection.getReflection();
			if ((direction != null) && isDirectionEnabled(direction) &&
				 (value != probabilities.get(direction)))
			{
				probabilities.put(direction, value);
				paramsDialog.setProbability(direction, value);
				changed = true;
			}
		}
		if (changed)
			repaint();
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
