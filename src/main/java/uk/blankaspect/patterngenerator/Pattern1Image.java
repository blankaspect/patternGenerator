/*====================================================================*\

Pattern1Image.java

Class: pattern 1 image.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;

import java.awt.image.BufferedImage;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.blankaspect.common.envelope.SimpleNode;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.ArgumentOutOfBoundsException;

import uk.blankaspect.common.exception2.UnexpectedRuntimeException;

import uk.blankaspect.common.function.IProcedure0;

import uk.blankaspect.common.misc.IStringKeyed;

import uk.blankaspect.common.random.Prng01;

import uk.blankaspect.common.range.DoubleRange;
import uk.blankaspect.common.range.IntegerRange;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.common.xml.Attribute;
import uk.blankaspect.common.xml.AttributeList;
import uk.blankaspect.common.xml.XmlParseException;
import uk.blankaspect.common.xml.XmlUtils;
import uk.blankaspect.common.xml.XmlWriter;

//----------------------------------------------------------------------


// CLASS: PATTERN 1 IMAGE


class Pattern1Image
	extends PatternImage
	implements Cloneable
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int		MIN_NUM_RENDERING_THREADS		= 0;
	public static final		int		MAX_NUM_RENDERING_THREADS		= 32;
	public static final		int		DEFAULT_NUM_RENDERING_THREADS	= 0;

	public static final		int		MAX_PHASE_INCREMENT	= 100;
	public static final		int		MIN_PHASE_INCREMENT	= -MAX_PHASE_INCREMENT;

	public static final		IntegerRange	DEFAULT_PHASE_INCREMENT_RANGE	= new IntegerRange(25, 30);

	public static final		double	MIN_MOTION_RATE		= 0.05;
	public static final		double	MAX_MOTION_RATE		= 4.0;

	public static final		DoubleRange	DEFAULT_MOTION_RATE_RANGE	= new DoubleRange(0.8, 1.2);

	public static final		MotionRateEnvelope	DEFAULT_MOTION_RATE_ENVELOPE	= new MotionRateEnvelope
	(
		List.of
		(
			new SimpleNode(0.0,  0.0, true),
			new SimpleNode(0.25, 0.0),
			new SimpleNode(0.5,  0.0),
			new SimpleNode(0.75, 0.0),
			new SimpleNode(1.0,  0.0, true)
		),
		1.0, 1.0
	);

	private static final	double	TWO_PI	= 2.0 * StrictMath.PI;

	public static final		double	MAX_ROTATION_RATE	= 1.0;
	public static final		double	MIN_ROTATION_RATE	= -MAX_ROTATION_RATE;

	enum RenderingMode
	{
		PARTIAL,
		ONE_PASS,
		TWO_PASSES
	}

	private static final	int		MIN_SUPPORTED_VERSION	= 0;
	private static final	int		MAX_SUPPORTED_VERSION	= 0;
	private static final	int		VERSION					= 0;

	private static final	int		HUE_INDEX			= 0;
	private static final	int		SATURATION_INDEX	= 1;
	private static final	int		BRIGHTNESS_INDEX	= 2;

	private static final	double	MIN_MOTION_ANGLE	= 0.0;
	private static final	double	MAX_MOTION_ANGLE	= 1.0;

	private static final	double	ATTENUATION_FACTOR	= -0.05;

	private static final	double	HUE_FACTOR		= 360.0;
	private static final	double	HUE_FACTOR_INV	= 1.0 / HUE_FACTOR;

	private static final	double	SATURATION_FACTOR		= 100.0;
	private static final	double	SATURATION_FACTOR_INV	= 1.0 / SATURATION_FACTOR;

	private static final	double	PHASE_INCREMENT_FACTOR1	= 0.001;
	private static final	double	PHASE_INCREMENT_FACTOR2	=
			StrictMath.log(0.25 / PHASE_INCREMENT_FACTOR1) / (double)MAX_PHASE_INCREMENT;

	private static final	float	MAX_SATURATION	= 1.0f;

	private static final	int		NUM_RESERVED_SEEDS			= 16;
	private static final	int		NUM_RESERVED_SOURCE_SEEDS	= 4;

	private static final	int		PRNG_SOURCES			= 0;

	private static final	int		PRNG_SOURCE_GENERAL		= 0;
	private static final	int		PRNG_SOURCE_ANIMATION	= 1;

	private interface ElementName
	{
		String	SOURCE	= "source";
	}

	private interface AttrName
	{
		String	DESCRIPTION				= "description";
		String	HEIGHT					= "height";
		String	MOTION_RATE_ENVELOPE	= "motionRateEnvelope";
		String	NUM_SOURCES				= "numSources";
		String	SATURATION_MODE			= "saturationMode";
		String	SYMMETRY				= "symmetry";
		String	VERSION					= "version";
		String	WIDTH					= "width";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	int	threadIndex;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Pattern1Document				document;
	private	Symmetry						symmetry;
	private	SaturationMode					saturationMode;
	private	MotionRateEnvelope				motionRateEnvelope;
	private	List<Source>					sources;
	private	int								numRenderingThreads;
	private	boolean							isPhaseAnimation;
	private	int								animationStartFrameIndex;
	private	float[][][]						hsbValues;
	private	double							minBrightness;
	private	double							maxBrightness;
	private	Map<BrightnessKey, DoubleRange>	brightnessRanges;
	private	int[]							rgbBuffer;
	private	BufferedImage					image;
	private	RenderingMode					renderingMode;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public Pattern1Image(
		Pattern1Document	document,
		Element				element)
		throws XmlParseException
	{
		// Get element path
		String elementPath = XmlUtils.getElementPath(element);

		// Attribute: version
		String attrName = AttrName.VERSION;
		String attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		String attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
		try
		{
			int version = Integer.parseInt(attrValue);
			if ((version < MIN_SUPPORTED_VERSION) || (version > MAX_SUPPORTED_VERSION))
				throw new XmlParseException(ErrorId.UNSUPPORTED_VERSION, attrKey, attrValue,
											PatternKind.PATTERN1.toString());
		}
		catch (NumberFormatException e)
		{
			throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
		}

		// Attribute: description
		attrName = AttrName.DESCRIPTION;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue != null)
			description = attrValue;

		// Attribute: width
		attrName = AttrName.WIDTH;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
		try
		{
			width = Integer.parseInt(attrValue);
			if ((width < Pattern1Params.MIN_WIDTH) || (width > Pattern1Params.MAX_WIDTH))
				throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
		}
		catch (NumberFormatException e)
		{
			throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
		}

		// Attribute: height
		attrName = AttrName.HEIGHT;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
		try
		{
			height = Integer.parseInt(attrValue);
			if ((height < Pattern1Params.MIN_HEIGHT) || (height > Pattern1Params.MAX_HEIGHT))
				throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
		}
		catch (NumberFormatException e)
		{
			throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
		}

		// Attribute: symmetry
		attrName = AttrName.SYMMETRY;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
		symmetry = Symmetry.forKey(attrValue);
		if (symmetry == null)
			throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);

		// Attribute: saturation mode
		attrName = AttrName.SATURATION_MODE;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
		saturationMode = SaturationMode.forKey(attrValue);
		if (saturationMode == null)
			throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);

		// Attribute: motion-rate envelope
		attrName = AttrName.MOTION_RATE_ENVELOPE;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue != null)
		{
			try
			{
				motionRateEnvelope = new MotionRateEnvelope(attrValue);
			}
			catch (IllegalArgumentException e)
			{
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
			}
		}

		// Parse source elements
		sources = new ArrayList<>();
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++)
		{
			Node node = childNodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				Element element1 = (Element)node;
				if (element1.getTagName().equals(ElementName.SOURCE))
					sources.add(Source.parseShape(element1).createSource(element1));
			}
		}

		// Attribute: number of sources
		attrName = AttrName.NUM_SOURCES;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
		try
		{
			int numSources = Integer.parseInt(attrValue);
			if (sources.size() != numSources)
				throw new XmlParseException(ErrorId.INCONSISTENT_NUMBER_OF_SOURCES, attrKey, attrValue);
		}
		catch (NumberFormatException e)
		{
			throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
		}

		// Initialise remaining instance variables
		init(document);

		// Initialise wave table of each source
		initWaveTables();
	}

	//------------------------------------------------------------------

	public Pattern1Image(
		Pattern1Document	document,
		Pattern1Params		params)
	{
		// Initialise instance variables
		width = params.getWidth();
		height = params.getHeight();
		symmetry = params.getSymmetry();
		saturationMode = params.getSaturationMode();
		motionRateEnvelope = params.getMotionRateEnvelope();
		sources = new ArrayList<>();

		// Initialise remaining instance variables
		init(document);

		// Initialise PRNGs
		Prng01 prng = new Prng01(params.getSeed());
		long[] reservedSeeds = new long[NUM_RESERVED_SEEDS];
		for (int i = 0; i < reservedSeeds.length; i++)
			reservedSeeds[i] = prng.nextInt64();
		Prng01 prngSources = new Prng01(reservedSeeds[PRNG_SOURCES]);

		// Initialise mid coordinates and end coordinates
		double x2 = (double)width;
		double xMid = x2 * 0.5;
		double y2 = (double)height;
		double yMid = y2 * 0.5;
		switch (symmetry)
		{
			case NONE:
				// do nothing
				break;

			case REFLECTION_VERTICAL_AXIS:
				x2 = xMid;
				break;

			case REFLECTION_HORIZONTAL_AXIS:
				y2 = yMid;
				break;

			case REFLECTION_VERTICAL_HORIZONTAL_AXES:
			case REFLECTION_DIAGONAL_AXES:
			case REFLECTION_FOUR_AXES:
				x2 = xMid;
				y2 = yMid;
				break;

			case ROTATION_HALF:
				y2 = yMid;
				break;

			case ROTATION_QUARTER:
				x2 = y2 = xMid = yMid = StrictMath.max(xMid, yMid);
				break;
		}

		// Get the pattern's dimension: the length of the diagonal
		double d = 0.0;
		if (symmetry == Symmetry.ROTATION_QUARTER)
		{
			double size = (double)StrictMath.max(width, height);
			d = StrictMath.sqrt(2.0 * size * size);
		}
		else
		{
			double w = (double)width;
			double h = (double)height;
			d = StrictMath.sqrt(w * w + h * h);
		}

		// Generate sources
		List<SourceParams> sourceParams = params.getSources();
		for (SourceParams source : sourceParams)
		{
			// Initialise PRNGs
			long[] reservedSourceSeeds = new long[NUM_RESERVED_SOURCE_SEEDS];
			for (int i = 0; i < reservedSourceSeeds.length; i++)
				reservedSourceSeeds[i] = prngSources.nextInt64();
			Prng01 prng1 = new Prng01(reservedSourceSeeds[PRNG_SOURCE_GENERAL]);
			Prng01 prng2 = new Prng01(reservedSourceSeeds[PRNG_SOURCE_ANIMATION]);

			// Generate frequency
			DoubleRange wavelengthRange = params.getWavelengthRange();
			int minWavelength = (int)StrictMath.round(d * wavelengthRange.lowerBound);
			int maxWavelength = (int)StrictMath.round(d * wavelengthRange.upperBound);
			double frequency = 1.0 / (double)(minWavelength + prng1.nextInt(maxWavelength - minWavelength + 1));

			// Generate phase offset
			double phaseOffset = prng1.nextDouble();

			// Generate orientation
			double orientation = prng1.nextDouble();

			// Generate coordinates
			double x = 0.0;
			double y = 0.0;
			switch (source.constraint)
			{
				case NONE:
					x = prng1.nextDouble() * x2;
					y = prng1.nextDouble() * y2;
					break;

				case VERTICAL_AXIS:
					x = xMid;
					y = prng1.nextDouble() * y2;
					break;

				case HORIZONTAL_AXIS:
					x = prng1.nextDouble() * x2;
					y = yMid;
					break;
			}

			// Generate phase increment
			IntegerRange phaseIncrementRange = params.getPhaseIncrementRange();
			double piLowerBound = (double)phaseIncrementRange.lowerBound;
			double piUpperBound = (double)phaseIncrementRange.upperBound;
			double phaseInc = piLowerBound + prng2.nextDouble() * (piUpperBound - piLowerBound);
			boolean piNegative = false;
			if (phaseInc < 0.0)
			{
				phaseInc = -phaseInc;
				piNegative = true;
			}
			phaseInc = denormalisePhaseIncrement(phaseInc);
			if (!piNegative)
				phaseInc = -phaseInc;

			// Generate motion angle
			double motionAngle = 0.0;
			switch (source.constraint)
			{
				case NONE:
				{
					int interval = Source.MOTION_ANGLE_DIVISIONS_PER_QUADRANT
											- (Source.MOTION_ANGLE_LOWER_MARGIN + Source.MOTION_ANGLE_UPPER_MARGIN);
					int divisions = prng2.nextInt(4) * Source.MOTION_ANGLE_DIVISIONS_PER_QUADRANT
											+ Source.MOTION_ANGLE_LOWER_MARGIN + prng2.nextInt(interval);
					motionAngle = (double)divisions / (double)(4 * Source.MOTION_ANGLE_DIVISIONS_PER_QUADRANT);
					break;
				}

				case VERTICAL_AXIS:
					motionAngle = (prng2.nextInt(1) == 0) ? 0.25 : 0.75;
					break;

				case HORIZONTAL_AXIS:
					motionAngle = (prng2.nextInt(1) == 0) ? 0.0 : 0.5;
					break;
			}

			// Generate motion rate
			DoubleRange motionRateRange = params.getMotionRateRange();
			double motionRate = prng2.nextDouble(motionRateRange);

			// Generate rotation rate
			DoubleRange rotationPeriodRange = params.getRotationPeriodRange();
			double rr1 = 1.0 / rotationPeriodRange.lowerBound;
			double rr2 = 1.0 / rotationPeriodRange.upperBound;
			boolean anticlockwise = prng2.nextBoolean();
			switch (params.getRotationSense())
			{
				case CLOCKWISE:
					anticlockwise = false;
					break;

				case ANTICLOCKWISE:
					anticlockwise = true;
					break;

				case ANY:
					// do nothing
					break;
			}
			if (anticlockwise)
			{
				rr1 = -rr1;
				rr2 = -rr2;
			}
			DoubleRange rotationRateRange = (rr1 < rr2) ? new DoubleRange(rr1, rr2)
														: new DoubleRange(rr2, rr1);
			double rotationRate = prng2.nextDouble(rotationRateRange);

			// Create source and add it to list
			switch (source.shape)
			{
				case CIRCLE:
					sources.add(new Source.Circle(x, y, source.waveform, source.waveCoeff,
												  source.attenuationCoeff, frequency, phaseOffset,
												  source.hue, source.saturation, phaseInc, motionAngle,
												  motionRate));
					break;

				case ELLIPSE:
				{
					int eccentricity = (Integer)source.shapeParams.get(SourceParams.Key.ECCENTRICITY);
					sources.add(new Source.Ellipse(x, y, source.waveform, source.waveCoeff,
												   source.attenuationCoeff, frequency, phaseOffset,
												   orientation, eccentricity, source.hue,
												   source.saturation, phaseInc, motionAngle, motionRate,
												   rotationRate));
					break;
				}

				case POLYGON:
				{
					int numEdges = (Integer)source.shapeParams.get(SourceParams.Key.NUM_EDGES);
					sources.add(new Source.Polygon(x, y, source.waveform, source.waveCoeff,
												   source.attenuationCoeff, frequency, phaseOffset,
												   orientation, numEdges, source.hue, source.saturation,
												   phaseInc, motionAngle, motionRate, rotationRate));
					break;
				}
			}
		}

		// Initialise wave table of each source
		initWaveTables();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Source createWaveTableSource(
		Source.Waveform	waveform,
		int				waveCoeff,
		int				waveTableLength)
	{
		Source source = new Source.Circle(waveform, waveCoeff);
		source.initWaveTable(0, 0, true, waveTableLength);
		return source;
	}

	//------------------------------------------------------------------

	private static double denormalisePhaseIncrement(
		double	value)
	{
		return PHASE_INCREMENT_FACTOR1 * StrictMath.exp(PHASE_INCREMENT_FACTOR2 * value);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public Pattern1Image clone()
	{
		try
		{
			Pattern1Image copy = (Pattern1Image)super.clone();

			copy.sources = new ArrayList<>();
			for (Source source : sources)
				copy.sources.add(source.clone());

			copy.rgbBuffer = null;

			if (image != null)
				copy.image = copyImage(image);

			copy.initWaveTables();

			return copy;
		}
		catch (CloneNotSupportedException e)
		{
			throw new UnexpectedRuntimeException(e);
		}
	}

	//------------------------------------------------------------------

	@Override
	public BufferedImage getImage()
	{
		return image;
	}

	//------------------------------------------------------------------

	@Override
	public BufferedImage getExportImage()
	{
		return image;
	}

	//------------------------------------------------------------------

	@Override
	public void renderImage()
		throws InterruptedException
	{
		// Get time at start of rendering
		long startTime = System.nanoTime();

		// Calculate the dimensions of the principal region of the image
		int regionWidth = width;
		int regionHeight = height;
		switch (symmetry)
		{
			case NONE:
				// do nothing
				break;

			case REFLECTION_VERTICAL_AXIS:
				regionWidth = (width + 1) / 2;
				break;

			case REFLECTION_HORIZONTAL_AXIS:
				regionHeight = (height + 1) / 2;
				break;

			case REFLECTION_VERTICAL_HORIZONTAL_AXES:
			case REFLECTION_FOUR_AXES:
				regionWidth = (width + 1) / 2;
				regionHeight = (height + 1) / 2;
				break;

			case REFLECTION_DIAGONAL_AXES:
			case ROTATION_HALF:
				regionHeight = (height + 1) / 2;
				break;

			case ROTATION_QUARTER:
				regionWidth = (StrictMath.max(width, height) + 1) / 2;
				regionHeight = regionWidth;
				break;
		}

		// Create an array of hue, saturation and brightness values for the principal region
		hsbValues = new float[regionWidth][regionHeight][3];
		if (rgbBuffer == null)
			rgbBuffer = new int[width * height];
		if (renderingMode == RenderingMode.TWO_PASSES)
		{
			minBrightness = Double.MAX_VALUE;
			maxBrightness = Double.MIN_VALUE;
		}

		// Initialise barrier
		CyclicBarrier barrier = new CyclicBarrier(numRenderingThreads);

		// Create image renderer for each thread
		Runnable[] renderers = new Runnable[numRenderingThreads];
		int heightPerThread = (regionHeight + numRenderingThreads - 1) / numRenderingThreads;
		int y = 0;
		List<Source> sources = getSources();
		for (int i = 0; i < numRenderingThreads; i++)
		{
			int width = regionWidth;
			int height = StrictMath.min(heightPerThread, regionHeight - y);
			int startY = y;
			int endY = y + height;
			renderers[i] = new Runnable()
			{
				boolean	secondPass;

				@Override
				public void run()
				{
					try
					{
						switch (renderingMode)
						{
							case PARTIAL:
								renderImageA(startY, endY, width, sources);
								break;

							case ONE_PASS:
								renderImageA(startY, endY, width, sources);
								renderImageB(startY, endY, width);
								break;

							case TWO_PASSES:
								if (secondPass)
									renderImageB(startY, endY, width);
								else
								{
									renderImageA(startY, endY, width, sources);
									secondPass = true;
								}
								break;
						}
						barrier.await();
					}
					catch (InterruptedException | BrokenBarrierException e)
					{
						// ignore
					}
				}
			};
			y = endY;
		}

		// Initialise array of threads
		Thread[] threads = new Thread[numRenderingThreads];

		// Create procedure to create and start threads
		IProcedure0 startThreads = () ->
		{
			for (int i = 0; i < numRenderingThreads; i++)
			{
				Thread thread = new Thread(renderers[i], getClass().getSimpleName() + "-" + ++threadIndex);
				thread.setDaemon(true);
				threads[i] = thread;
				thread.start();
			}
		};

		// Create and start threads
		startThreads.invoke();

		// Wait for threads to terminate
		for (Thread thread : threads)
			thread.join();

		// Perform second pass
		if (renderingMode == RenderingMode.TWO_PASSES)
		{
			// Reset barrier
			barrier.reset();

			// Create and start threads
			startThreads.invoke();

			// Wait for threads to terminate
			for (Thread thread : threads)
				thread.join();
		}

		// Set the RGB values in the image
		if (renderingMode != RenderingMode.PARTIAL)
		{
			if (image == null)
				image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			image.setRGB(0, 0, width, height, rgbBuffer, 0, width);
		}

		// Add rendering time to total
		document.addRenderingTime(width * height, System.nanoTime() - startTime);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public double getMinimumBrightness()
	{
		return minBrightness;
	}

	//------------------------------------------------------------------

	public double getMaximumBrightness()
	{
		return maxBrightness;
	}

	//------------------------------------------------------------------

	public RenderingMode getRenderingMode()
	{
		return renderingMode;
	}

	//------------------------------------------------------------------

	public void setRenderingMode(
		RenderingMode	renderingMode)
	{
		this.renderingMode = renderingMode;
	}

	//------------------------------------------------------------------

	public Set<AnimationKind> getSupportedAnimationKinds()
	{
		Set<AnimationKind> animationKinds = EnumSet.allOf(AnimationKind.class);
		if (!isPhaseAnimation)
			animationKinds.remove(AnimationKind.PHASE);
		return animationKinds;
	}

	//------------------------------------------------------------------

	public boolean isBrightnessRange()
	{
		return !brightnessRanges.isEmpty();
	}

	//------------------------------------------------------------------

	public boolean isBrightnessRange(
		Set<AnimationKind>	animationKinds,
		int					startFrameIndex)
	{
		return brightnessRanges.containsKey(new BrightnessKey(animationKinds, startFrameIndex));
	}

	//------------------------------------------------------------------

	public void removeBrightnessRange(
		Set<AnimationKind>	animationKinds,
		int					startFrameIndex)
	{
		brightnessRanges.remove(new BrightnessKey(animationKinds, startFrameIndex));
	}

	//------------------------------------------------------------------

	public void putBrightnessRange(
		Set<AnimationKind>	animationKinds,
		int					startFrameIndex,
		DoubleRange			brightnessRange)
	{
		brightnessRanges.put(new BrightnessKey(animationKinds, startFrameIndex), brightnessRange);
	}

	//------------------------------------------------------------------

	public long[] getBrightnessRangeKeys()
	{
		long[] keys = new long[brightnessRanges.size()];
		int i = 0;
		for (BrightnessKey key : brightnessRanges.keySet())
			keys[i++] = key.toLong();
		Arrays.sort(keys);
		return keys;
	}

	//------------------------------------------------------------------

	public void initAnimation(
		Set<AnimationKind>	animationKinds,
		int					startFrameIndex)
	{
		animationStartFrameIndex = startFrameIndex;
		if (renderingMode == RenderingMode.PARTIAL)
		{
			minBrightness = Double.MAX_VALUE;
			maxBrightness = Double.MIN_VALUE;
		}
		else
		{
			DoubleRange brightnessRange = brightnessRanges.get(new BrightnessKey(animationKinds, startFrameIndex));
			if (brightnessRange == null)
				renderingMode = RenderingMode.TWO_PASSES;
			else
			{
				renderingMode = RenderingMode.ONE_PASS;
				minBrightness = brightnessRange.lowerBound;
				maxBrightness = brightnessRange.upperBound;
			}
		}
	}

	//------------------------------------------------------------------

	public Element createElement(
		Document	document)
	{
		Element element = document.createElement(PatternKind.PATTERN1.getKey());
		for (Attribute attribute : getAttributes())
			attribute.set(element);

		for (Source source : sources)
			element.appendChild(source.createElement(document));

		return element;
	}

	//------------------------------------------------------------------

	public void write(
		XmlWriter	writer,
		int			indent)
		throws IOException
	{
		String elementName = PatternKind.PATTERN1.getKey();
		writer.writeElementStart(elementName, getAttributes(), indent, true, true);
		for (Source source : sources)
			source.write(writer, indent + XmlWriter.INDENT_INCREMENT);
		writer.writeElementEnd(elementName, indent);
	}

	//------------------------------------------------------------------

	public void updateSourcePhases(
		int	frameIndex)
	{
		for (Source source : sources)
			source.updatePhaseOffset(frameIndex);
	}

	//------------------------------------------------------------------

	public void updateSourcePositions(
		int	frameIndex)
	{
		double motionRateOffset = (motionRateEnvelope == null)
											? 0.0
											: motionRateEnvelope.evaluate(frameIndex - animationStartFrameIndex);
		for (Source source : sources)
			source.updatePosition(frameIndex, width, height, motionRateOffset);
	}

	//------------------------------------------------------------------

	public void updateSourceOrientations(
		int	frameIndex)
	{
		for (Source source : sources)
			source.updateOrientation(frameIndex);
	}

	//------------------------------------------------------------------

	private void init(
		Pattern1Document	document)
	{
		// Initialise instance variables
		this.document = document;
		brightnessRanges = new HashMap<>();
		renderingMode = RenderingMode.TWO_PASSES;

		// Initialise instance variables from configuration
		AppConfig config = AppConfig.INSTANCE;
		numRenderingThreads = config.getPattern1NumRenderingThreads();
		if (numRenderingThreads == 0)
			numRenderingThreads = Runtime.getRuntime().availableProcessors();
		isPhaseAnimation = config.isPattern1PhaseAnimation();
	}

	//------------------------------------------------------------------

	private void initWaveTables()
	{
		int w = width;
		int h = height;
		if (symmetry == Symmetry.ROTATION_QUARTER)
			w = h = StrictMath.max(width, height);
		if (isPhaseAnimation)
		{
			for (int i = 0; i < sources.size(); i++)
			{
				Source source = sources.get(i);
				source.waveTable = null;
				for (int j = 0; j < i; j++)
				{
					Source source1 = sources.get(j);
					if ((source.waveform == source1.waveform) && (source.waveCoeff == source1.waveCoeff))
					{
						source.initWaveTable(source1.waveTable);
						break;
					}
				}
				if (source.waveTable == null)
					source.initWaveTable(w, h, true);
			}
		}
		else
		{
			for (Source source : sources)
				source.initWaveTable(w, h, false);
		}
	}

	//------------------------------------------------------------------

	private List<Source> getSources()
	{
		List<Source> sources = new ArrayList<>(this.sources);
		double xMid = (double)width * 0.5;
		double yMid = (double)height * 0.5;
		if (symmetry == Symmetry.ROTATION_QUARTER)
			xMid = yMid = StrictMath.max(xMid, yMid);
		double wOverH = (double)width / (double)height;
		double hOverW = (double)height / (double)width;
		int numSources = sources.size();
		for (int i = 0; i < numSources; i++)
		{
			Source source = sources.get(i);
			switch (symmetry)
			{
				case NONE:
					// do nothing
					break;

				case REFLECTION_VERTICAL_AXIS:
					if (source.x != xMid)
						sources.add(source.copyModifyX((double)width - source.x));
					break;

				case REFLECTION_HORIZONTAL_AXIS:
					if (source.y != yMid)
						sources.add(source.copyModifyY((double)height - source.y));
					break;

				case REFLECTION_VERTICAL_HORIZONTAL_AXES:
					if (source.x != xMid)
						sources.add(source.copyModifyX((double)width - source.x));
					if (source.y != yMid)
						sources.add(source.copyModifyY((double)height - source.y));
					if ((source.x != xMid) || (source.y != yMid))
						sources.add(source.copyModifyXY1((double)width - source.x, (double)height - source.y));
					break;

				case REFLECTION_DIAGONAL_AXES:
				{
					List<Source> srcs = new ArrayList<>();
					srcs.add(source);
					double x1 = source.x * hOverW;
					double y1 = source.y * wOverH;
					if (x1 != y1)
					{
						Source src = source.copyModifyXY2(y1, x1);
						srcs.add(src);
						sources.add(src);
					}
					for (Source src : srcs)
					{
						if ((src.x != xMid) || (src.y != yMid))
							sources.add(src.copyModifyXY1((double)width - src.x, (double)height - src.y));
					}
					break;
				}

				case REFLECTION_FOUR_AXES:
				{
					List<Source> srcs = new ArrayList<>();
					srcs.add(source);
					double x1 = source.x * hOverW;
					double y1 = source.y * wOverH;
					if (x1 != y1)
					{
						Source src = source.copyModifyXY2(y1, x1);
						srcs.add(src);
						sources.add(src);
					}
					for (Source src : srcs)
					{
						if (src.x != xMid)
							sources.add(src.copyModifyX((double)width - src.x));
						if (src.y != yMid)
							sources.add(src.copyModifyY((double)height - src.y));
						if ((src.x != xMid) || (src.y != yMid))
							sources.add(src.copyModifyXY1((double)width - src.x, (double)height - src.y));
					}
					break;
				}

				case ROTATION_HALF:
					if ((source.x != xMid) || (source.y != yMid))
						sources.add(source.copyModifyXY1((double)width - source.x, (double)height - source.y));
					break;

				case ROTATION_QUARTER:
					if ((source.x != xMid) || (source.y != yMid))
					{
						int size = StrictMath.max(width, height);
						double cx = (double)size - source.x;
						double cy = (double)size - source.y;
						sources.add(source.copyModifyXY(source.y, cx, 0.25));
						sources.add(source.copyModifyXY1(cx, cy));
						sources.add(source.copyModifyXY(cy, source.x, 0.75));
					}
					break;
			}
		}
		return sources;
	}

	//------------------------------------------------------------------

	private void renderImageA(
		int				startY,
		int				endY,
		int				regionWidth,
		List<Source>	sources)
		throws InterruptedException
	{
		// Create an array of hue, saturation and brightness values for the principal region of the image
		double[] rgbValuesA = new double[3];
		double[] rgbValuesB = new double[3];
		double[] hsValues = new double[2];
		double minBrightness = Double.MAX_VALUE;
		double maxBrightness = Double.MIN_VALUE;
		for (int y = startY; y < endY; y++)
		{
			// Test whether task has been cancelled
			if (Thread.interrupted())
				throw new InterruptedException();

			// Calculate the hue, saturation and brightness of each pixel in a row.
			// Hues are not additive, so the hue and saturation are converted to RGB values, the weighted mean of the
			// RGB values is taken, then the result is converted back to a hue and saturation.
			for (int x = 0; x < regionWidth; x++)
			{
				Arrays.fill(rgbValuesA, 0.0);
				double brightness = 0.0;
				for (Source source : sources)
				{
					double value = source.getWaveValue((double)x + 0.5, (double)y + 0.5);
					brightness += value;

					Utils.hsToRgb(source.hue, source.saturation, rgbValuesB);
					for (int j = 0; j < rgbValuesA.length; j++)
						rgbValuesA[j] += rgbValuesB[j] * value;
				}

				Utils.rgbToHs(rgbValuesA, hsValues);
				hsbValues[x][y][HUE_INDEX] = (float)hsValues[0];
				hsbValues[x][y][SATURATION_INDEX] = (float)hsValues[1];
				hsbValues[x][y][BRIGHTNESS_INDEX] = (float)brightness;
				if (minBrightness > brightness)
					minBrightness = brightness;
				if (maxBrightness < brightness)
					maxBrightness = brightness;
			}
		}

		// Update global minimum and maximum brightness values
		if (renderingMode != RenderingMode.ONE_PASS)
		{
			synchronized (this)
			{
				if (this.minBrightness > minBrightness)
					this.minBrightness = minBrightness;
				if (this.maxBrightness < maxBrightness)
					this.maxBrightness = maxBrightness;
			}
		}
	}

	//------------------------------------------------------------------

	private void renderImageB(
		int	startY,
		int	endY,
		int	regionWidth)
		throws InterruptedException
	{
		// Set the RGB values of the image
		double brightnessInterval = maxBrightness - minBrightness;
		float brightnessFactor = (brightnessInterval == 0.0) ? 0.0f : (float)(1.0 / brightnessInterval);
		int xMargin = 0;
		int yMargin = 0;
		int yMax = 0;
		int offset1 = 0;
		int offset2 = 0;
		int offset3 = 0;
		if (symmetry == Symmetry.ROTATION_QUARTER)
		{
			int size = StrictMath.max(width, height);
			xMargin = (size - width) / 2;
			yMargin = (size - height) / 2;
			yMax = size / 2;
			offset3 = (height - 1) * width;
		}
		for (int y1 = startY; y1 < endY; y1++)
		{
			// Test whether task has been cancelled
			if (Thread.interrupted())
				throw new InterruptedException();

			// Set the RGB values for a row of pixels
			int y2 = height - (y1 - yMargin) - 1;
			offset1 = (y1 - yMargin) * width;
			offset2 = y2 * width;
			for (int x1 = 0; x1 < regionWidth; x1++)
			{
				// Set x2 coordinate
				int x2 = width - x1 - 1;

				// Get brightness
				float brightness = (hsbValues[x1][y1][BRIGHTNESS_INDEX] - (float)minBrightness) * brightnessFactor;
				if (brightness < 0.0f)
					brightness = 0.0f;
				if (brightness > 1.0f)
					brightness = 1.0f;

				// Get saturation
				float saturation = 0.0f;
				switch (saturationMode)
				{
					case VARIABLE:
						saturation = hsbValues[x1][y1][SATURATION_INDEX];
						break;

					case MAXIMUM:
						saturation = MAX_SATURATION;
						break;

					case BRIGHTNESS:
						saturation = brightness;
						break;
				}

				// Convert HSB values to RGB
				int rgb = Color.HSBtoRGB(hsbValues[x1][y1][HUE_INDEX], saturation, brightness);

				// Set RGB values of pixels
				switch (symmetry)
				{
					case NONE:
						rgbBuffer[x1 + offset1] = rgb;
						break;

					case REFLECTION_VERTICAL_AXIS:
						rgbBuffer[x1 + offset1] = rgb;
						rgbBuffer[x2 + offset1] = rgb;
						break;

					case REFLECTION_HORIZONTAL_AXIS:
						rgbBuffer[x1 + offset1] = rgb;
						rgbBuffer[x1 + offset2] = rgb;
						break;

					case REFLECTION_VERTICAL_HORIZONTAL_AXES:
					case REFLECTION_FOUR_AXES:
						rgbBuffer[x1 + offset1] = rgb;
						rgbBuffer[x1 + offset2] = rgb;
						rgbBuffer[x2 + offset1] = rgb;
						rgbBuffer[x2 + offset2] = rgb;
						break;

					case REFLECTION_DIAGONAL_AXES:
					case ROTATION_HALF:
						rgbBuffer[x1 + offset1] = rgb;
						rgbBuffer[x2 + offset2] = rgb;
						break;

					case ROTATION_QUARTER:
						if ((x1 >= xMargin) && (y1 >= yMargin) && (y1 < yMax))
						{
							rgbBuffer[x1 - xMargin + offset1] = rgb;
							rgbBuffer[x2 + xMargin + offset2] = rgb;
						}
						if ((x1 >= yMargin) && (y1 >= xMargin))
						{
							int x = y1 - xMargin;
							int offset = (x1 - yMargin) * width;
							rgbBuffer[width - x - 1 + offset] = rgb;
							rgbBuffer[x + offset3 - offset] = rgb;
						}
						break;
				}
			}
		}
	}

	//------------------------------------------------------------------

	private AttributeList getAttributes()
	{
		AttributeList attributes = new AttributeList();
		attributes.add(AttrName.VERSION, VERSION);
		if (description != null)
			attributes.add(AttrName.DESCRIPTION, description, true);
		attributes.add(AttrName.WIDTH, width);
		attributes.add(AttrName.HEIGHT, height);
		attributes.add(AttrName.SYMMETRY, symmetry.key);
		attributes.add(AttrName.SATURATION_MODE, saturationMode.key);
		if (motionRateEnvelope != null)
			attributes.add(AttrName.MOTION_RATE_ENVELOPE, motionRateEnvelope);
		attributes.add(AttrName.NUM_SOURCES, sources.size());
		return attributes;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: SYMMETRY


	enum Symmetry
		implements IStringKeyed
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		NONE
		(
			"none",
			"None"
		),

		REFLECTION_VERTICAL_AXIS
		(
			"reflectionVerticalAxis",
			"Reflection in vertical axis"
		),

		REFLECTION_HORIZONTAL_AXIS
		(
			"reflectionHorizontalAxis",
			"Reflection in horizontal axis"
		),

		REFLECTION_VERTICAL_HORIZONTAL_AXES
		(
			"reflectionVerticalHorizontalAxes",
			"Reflection in V and H axes"
		),

		REFLECTION_DIAGONAL_AXES
		(
			"reflectionDiagonalAxes",
			"Reflection in diagonal axes"
		),

		REFLECTION_FOUR_AXES
		(
			"reflectionFourAxes",
			"Reflection in four axes"
		),

		ROTATION_HALF
		(
			"rotationHalf",
			"Rotation by a half-turn"
		),

		ROTATION_QUARTER
		(
			"rotationQuarter",
			"Rotation by a quarter-turn"
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	key;
		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Symmetry(
			String	key,
			String	text)
		{
			this.key = key;
			this.text = text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static Symmetry forKey(
			String	key)
		{
			for (Symmetry value : values())
			{
				if (value.key.equals(key))
					return value;
			}
			return null;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : IStringKeyed interface
	////////////////////////////////////////////////////////////////////

		public String getKey()
		{
			return key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return text;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// ENUMERATION: SATURATION MODE


	enum SaturationMode
		implements IStringKeyed
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		VARIABLE
		(
			"variable"
		),

		BRIGHTNESS
		(
			"brightness"
		),

		MAXIMUM
		(
			"maximum"
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	key;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private SaturationMode(
			String	key)
		{
			this.key = key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static SaturationMode forKey(
			String	key)
		{
			for (SaturationMode value : values())
			{
				if (value.key.equals(key))
					return value;
			}
			return null;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : IStringKeyed interface
	////////////////////////////////////////////////////////////////////

		@Override
		public String getKey()
		{
			return key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return StringUtils.firstCharToUpperCase(key);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// ENUMERATION: ROTATION SENSE


	enum RotationSense
		implements IStringKeyed
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		CLOCKWISE
		(
			"clockwise"
		),

		ANTICLOCKWISE
		(
			"anticlockwise"
		),

		ANY
		(
			"any"
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	key;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private RotationSense(
			String	key)
		{
			this.key = key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static RotationSense forKey(
			String	key)
		{
			for (RotationSense value : values())
			{
				if (value.key.equals(key))
					return value;
			}
			return null;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : IStringKeyed interface
	////////////////////////////////////////////////////////////////////

		@Override
		public String getKey()
		{
			return key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return StringUtils.firstCharToUpperCase(key);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// ENUMERATION: ANIMATION KIND


	enum AnimationKind
		implements IStringKeyed
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		POSITION
		(
			"position"
		),

		PHASE
		(
			"phase"
		),

		ORIENTATION
		(
			"orientation"
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	key;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private AnimationKind(
			String	key)
		{
			this.key = key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static AnimationKind forKey(
			String	key)
		{
			for (AnimationKind value : values())
			{
				if (value.key.equals(key))
					return value;
			}
			return null;
		}

		//--------------------------------------------------------------

		public static Set<AnimationKind> bitFieldToSet(
			int	bitField)
		{
			Set<AnimationKind> animationKinds = EnumSet.noneOf(AnimationKind.class);
			for (int i = 0; i < values().length; i++)
			{
				if ((bitField & 1 << i) != 0)
					animationKinds.add(values()[i]);
			}
			return animationKinds;
		}

		//--------------------------------------------------------------

		public static int setToBitField(
			Set<AnimationKind>	animationKinds)
		{
			int bitField = 0;
			Iterator<AnimationKind> it = animationKinds.iterator();
			while (it.hasNext())
				bitField |= 1 << it.next().ordinal();
			return bitField;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : IStringKeyed interface
	////////////////////////////////////////////////////////////////////

		@Override
		public String getKey()
		{
			return key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return StringUtils.firstCharToUpperCase(key);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// ENUMERATION: ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		UNSUPPORTED_VERSION
		("The version of %1 is not supported by this version of " + PatternGeneratorApp.SHORT_NAME + "."),

		NO_ATTRIBUTE
		("The required attribute is missing."),

		INVALID_ATTRIBUTE
		("The attribute is invalid."),

		ATTRIBUTE_OUT_OF_BOUNDS
		("The attribute value is out of bounds."),

		INCONSISTENT_NUMBER_OF_SOURCES
		("The attribute value is not consistent with the number of source elements.");

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	message;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ErrorId(
			String	message)
		{
			this.message = message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : AppException.IId interface
	////////////////////////////////////////////////////////////////////

		@Override
		public String getMessage()
		{
			return message;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: SOURCE PARAMETERS


	public static class SourceParams
		implements Cloneable
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		enum Key
		{
			NUM_EDGES,
			ECCENTRICITY
		}

		private static final	String	SEPARATOR	= ";";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Source.Shape		shape;
		private	Map<Key, Object>	shapeParams;
		private	Source.Waveform		waveform;
		private	int					waveCoeff;
		private	int					attenuationCoeff;
		private	Source.Constraint	constraint;
		private	int					hue;
		private	int					saturation;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public SourceParams(
			Source.Shape		shape,
			Map<Key, Object>	shapeParams,
			Source.Waveform		waveform,
			int					waveCoeff,
			int					attenuationCoeff,
			Source.Constraint	constraint,
			int					hue,
			int					saturation)
		{
			this.shape = shape;
			this.shapeParams = new EnumMap<>(Key.class);
			if (shapeParams != null)
				this.shapeParams.putAll(shapeParams);
			this.waveform = waveform;
			this.waveCoeff = waveCoeff;
			this.attenuationCoeff = attenuationCoeff;
			this.constraint = constraint;
			this.hue = hue;
			this.saturation = saturation;
		}

		//--------------------------------------------------------------

		public SourceParams(
			String	str)
		{
			int index = 0;

			// Parse shape
			String[] strs = str.split("\\s*" + SEPARATOR + "\\s*", -1);
			shape = Source.Shape.forKey(strs[index++]);
			if (shape == null)
				throw new IllegalArgumentException();

			// Test number of parameters
			int numParams = 0;
			switch (shape)
			{
				case CIRCLE:
					numParams = 7;
					break;

				case ELLIPSE:
				case POLYGON:
					numParams = 8;
					break;
			}
			if (strs.length != numParams)
				throw new IllegalArgumentException();

			// Parse shape parameters
			shapeParams = new EnumMap<>(Key.class);
			switch (shape)
			{
				case CIRCLE:
					// do nothing
					break;

				case ELLIPSE:
				{
					int eccentricity = Integer.parseInt(strs[index++]);
					if ((eccentricity < Source.Ellipse.MIN_ECCENTRICITY) ||
							(eccentricity > Source.Ellipse.MAX_ECCENTRICITY))
						throw new ArgumentOutOfBoundsException();
					shapeParams.put(Key.ECCENTRICITY, eccentricity);
					break;
				}

				case POLYGON:
				{
					int numEdges = Integer.parseInt(strs[index++]);
					if ((numEdges < Source.Polygon.MIN_NUM_EDGES) || (numEdges > Source.Polygon.MAX_NUM_EDGES))
						throw new ArgumentOutOfBoundsException();
					shapeParams.put(Key.NUM_EDGES, numEdges);
					break;
				}
			}

			// Parse waveform
			waveform = Source.Waveform.forKey(strs[index++]);
			if (waveform == null)
				throw new IllegalArgumentException();

			// Parse wave coefficient
			waveCoeff = Integer.parseInt(strs[index++]);
			if ((waveCoeff < Source.MIN_WAVE_COEFFICIENT) || (waveCoeff > Source.MAX_WAVE_COEFFICIENT))
				throw new ArgumentOutOfBoundsException();

			// Parse attenuation coefficient
			attenuationCoeff = Integer.parseInt(strs[index++]);
			if ((attenuationCoeff < Source.MIN_ATTENUATION_COEFFICIENT) ||
					(attenuationCoeff > Source.MAX_ATTENUATION_COEFFICIENT))
				throw new ArgumentOutOfBoundsException();

			// Parse constraint
			constraint = Source.Constraint.forKey(strs[index++]);
			if (constraint == null)
				throw new IllegalArgumentException();

			// Parse hue
			hue = Integer.parseInt(strs[index++]);
			if ((hue < Source.MIN_HUE) || (hue > Source.MAX_HUE))
				throw new ArgumentOutOfBoundsException();

			// Parse saturation
			saturation = Integer.parseInt(strs[index++]);
			if ((saturation < Source.MIN_SATURATION) || (saturation > Source.MAX_SATURATION))
				throw new ArgumentOutOfBoundsException();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public SourceParams clone()
		{
			try
			{
				return (SourceParams)super.clone();
			}
			catch (CloneNotSupportedException e)
			{
				throw new UnexpectedRuntimeException(e);
			}
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			StringBuilder buffer = new StringBuilder(96);
			buffer.append(shape.key);
			for (Key key : shapeParams.keySet())
			{
				buffer.append(SEPARATOR);
				buffer.append(' ');
				buffer.append(shapeParams.get(key));
			}
			buffer.append(SEPARATOR);
			buffer.append(' ');
			buffer.append(waveform.key);
			buffer.append(SEPARATOR);
			buffer.append(' ');
			buffer.append(waveCoeff);
			buffer.append(SEPARATOR);
			buffer.append(' ');
			buffer.append(attenuationCoeff);
			buffer.append(SEPARATOR);
			buffer.append(' ');
			buffer.append(constraint.key);
			buffer.append(SEPARATOR);
			buffer.append(' ');
			buffer.append(hue);
			buffer.append(SEPARATOR);
			buffer.append(' ');
			buffer.append(saturation);
			return buffer.toString();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public Source.Shape getShape()
		{
			return shape;
		}

		//--------------------------------------------------------------

		public Object getShapeParamValue(
			Key	key)
		{
			return shapeParams.get(key);
		}

		//--------------------------------------------------------------

		public Source.Waveform getWaveform()
		{
			return waveform;
		}

		//--------------------------------------------------------------

		public int getWaveCoefficient()
		{
			return waveCoeff;
		}

		//--------------------------------------------------------------

		public int getAttenuationCoefficient()
		{
			return attenuationCoeff;
		}

		//--------------------------------------------------------------

		public Source.Constraint getConstraint()
		{
			return constraint;
		}

		//--------------------------------------------------------------

		public int getHue()
		{
			return hue;
		}

		//--------------------------------------------------------------

		public int getSaturation()
		{
			return saturation;
		}

		//--------------------------------------------------------------

		public Color getColour()
		{
			return new Color(Utils.hsToRgb((double)hue * HUE_FACTOR_INV, (double)saturation * SATURATION_FACTOR_INV));
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: SOURCE


	public static abstract class Source
		implements Cloneable
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		public static final		int		MIN_WAVE_COEFFICIENT	= 0;
		public static final		int		MAX_WAVE_COEFFICIENT	= 100;

		public static final		int		MIN_ATTENUATION_COEFFICIENT		= 0;
		public static final		int		MAX_ATTENUATION_COEFFICIENT		= 100;
		public static final		int		DEFAULT_ATTENUATION_COEFFICIENT	= 5;

		public static final		int		MIN_HUE		= 0;
		public static final		int		MAX_HUE		= 359;
		public static final		int		DEFAULT_HUE	= 50;

		public static final		int		MIN_SATURATION		= 0;
		public static final		int		MAX_SATURATION		= 100;
		public static final		int		DEFAULT_SATURATION	= 100;

		private static final	double	MIN_X	= 0.0;
		private static final	double	MAX_X	= 10000.0;

		private static final	double	MIN_Y	= 0.0;
		private static final	double	MAX_Y	= 10000.0;

		private static final	double	MIN_FREQUENCY	= 0.0;
		private static final	double	MAX_FREQUENCY	= 10000.0;

		private static final	double	MIN_PHASE_OFFSET	= 0.0;
		private static final	double	MAX_PHASE_OFFSET	= 1.0;

		private static final	double	MIN_ORIENTATION	= 0.0;
		private static final	double	MAX_ORIENTATION	= 1.0;

		private static final	double	WAVE_TABLE_FACTOR_FIXED_PHASE		= 2.0;
		private static final	double	WAVE_TABLE_FACTOR_FIXED_PHASE_INV	= 1.0 / WAVE_TABLE_FACTOR_FIXED_PHASE;

		private static final	int		WAVE_TABLE_LENGTH_VARIABLE_PHASE	= 1 << 14;  // 16384

		private static final	int		MOTION_ANGLE_DIVISIONS_PER_QUADRANT	= 100;
		private static final	int		MOTION_ANGLE_LOWER_MARGIN			= 5;
		private static final	int		MOTION_ANGLE_UPPER_MARGIN			= MOTION_ANGLE_LOWER_MARGIN - 1;

		private interface AttrName
		{
			String	ATTENUATION_COEFFICIENT	= "attenuationCoefficient";
			String	ECCENTRICITY			= "eccentricity";
			String	FREQUENCY				= "frequency";
			String	HUE						= "hue";
			String	ORIENTATION				= "orientation";
			String	MOTION_ANGLE			= "motionAngle";
			String	MOTION_RATE				= "motionRate";
			String	NUM_EDGES				= "numEdges";
			String	PHASE_INCREMENT			= "phaseIncrement";
			String	PHASE_OFFSET			= "phaseOffset";
			String	ROTATION_RATE			= "rotationRate";
			String	SATURATION				= "saturation";
			String	SHAPE					= "shape";
			String	WAVE_COEFFICIENT		= "waveCoefficient";
			String	WAVEFORM				= "waveform";
			String	X						= "x";
			String	Y						= "y";
		}

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		protected	double		x;
		protected	double		y;
		protected	Waveform	waveform;
		protected	int			waveCoeff;
		protected	double		attenuationCoeff;
		protected	double		frequency;
		protected	double		phaseOffset;
		protected	double		orientation;
		protected	double		hue;
		protected	double		saturation;
		protected	double		phaseIncrement;
		protected	double		motionAngle;
		protected	double		motionRate;
		protected	double		rotationRate;
		protected	double[]	waveTable;
		protected	boolean		variablePhase;
		protected	double		startX;
		protected	double		startY;
		protected	double		startPhaseOffset;
		protected	double		startOrientation;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Source(
			Waveform	waveform,
			int			waveCoeff)
		{
			this.waveform = waveform;
			this.waveCoeff = waveCoeff;
		}

		//--------------------------------------------------------------

		private Source(
			double		x,
			double		y,
			Waveform	waveform,
			int			waveCoeff,
			int			attenuationCoeff,
			double		frequency,
			double		phaseOffset,
			double		orientation,
			int			hue,
			int			saturation,
			double		phaseIncrement,
			double		motionAngle,
			double		motionRate,
			double		rotationRate)
		{
			this.x = x;
			this.y = y;
			this.waveform = waveform;
			this.waveCoeff = waveCoeff;
			this.attenuationCoeff = attenuationCoeffIntToDouble(attenuationCoeff);
			this.frequency = frequency;
			this.phaseOffset = phaseOffset;
			this.orientation = orientation * TWO_PI;
			this.hue = (double)hue * HUE_FACTOR_INV;
			this.saturation = (double)saturation * SATURATION_FACTOR_INV;
			this.phaseIncrement = phaseIncrement;
			this.motionAngle = motionAngle * TWO_PI;
			this.motionRate = motionRate;
			this.rotationRate = rotationRate * TWO_PI;
			init();
		}

		//--------------------------------------------------------------

		private Source(
			Element	element)
			throws XmlParseException
		{
			// Get element path
			String elementPath = XmlUtils.getElementPath(element);

			// Attribute: x
			String attrName = AttrName.X;
			String attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			String attrValue = XmlUtils.getAttribute(element, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			try
			{
				x = Double.parseDouble(attrValue);
				if ((x < MIN_X) || (x > MAX_X))
					throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
			}
			catch (NumberFormatException e)
			{
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
			}

			// Attribute: y
			attrName = AttrName.Y;
			attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			attrValue = XmlUtils.getAttribute(element, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			try
			{
				y = Double.parseDouble(attrValue);
				if ((y < MIN_Y) || (y > MAX_Y))
					throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
			}
			catch (NumberFormatException e)
			{
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
			}

			// Attribute: waveform
			attrName = AttrName.WAVEFORM;
			attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			attrValue = XmlUtils.getAttribute(element, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			waveform = Waveform.forKey(attrValue);
			if (waveform == null)
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);

			// Attribute: wave coefficient
			attrName = AttrName.WAVE_COEFFICIENT;
			attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			attrValue = XmlUtils.getAttribute(element, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			try
			{
				waveCoeff = Integer.parseInt(attrValue);
				if ((waveCoeff < MIN_WAVE_COEFFICIENT) || (waveCoeff > MAX_WAVE_COEFFICIENT))
					throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
			}
			catch (NumberFormatException e)
			{
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
			}

			// Attribute: attenuation coefficient
			attrName = AttrName.ATTENUATION_COEFFICIENT;
			attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			attrValue = XmlUtils.getAttribute(element, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			try
			{
				attenuationCoeff = Double.parseDouble(attrValue);
				if ((attenuationCoeff < attenuationCoeffIntToDouble(MAX_ATTENUATION_COEFFICIENT)) ||
						(attenuationCoeff > attenuationCoeffIntToDouble(MIN_ATTENUATION_COEFFICIENT)))
					throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
			}
			catch (NumberFormatException e)
			{
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
			}

			// Attribute: frequency
			attrName = AttrName.FREQUENCY;
			attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			attrValue = XmlUtils.getAttribute(element, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			try
			{
				frequency = Double.parseDouble(attrValue);
				if ((frequency < MIN_FREQUENCY) || (frequency > MAX_FREQUENCY))
					throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
			}
			catch (NumberFormatException e)
			{
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
			}

			// Attribute: phase offset
			attrName = AttrName.PHASE_OFFSET;
			attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			attrValue = XmlUtils.getAttribute(element, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			try
			{
				phaseOffset = Double.parseDouble(attrValue);
				if ((phaseOffset < MIN_PHASE_OFFSET) || (phaseOffset >= MAX_PHASE_OFFSET))
					throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
			}
			catch (NumberFormatException e)
			{
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
			}

			// Attribute: orientation
			if (canRotate())
			{
				attrName = AttrName.ORIENTATION;
				attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
				attrValue = XmlUtils.getAttribute(element, attrName);
				if (attrValue == null)
					throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
				try
				{
					orientation = Double.parseDouble(attrValue);
					if ((orientation < MIN_ORIENTATION) || (orientation > MAX_ORIENTATION))
						throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
					orientation *= TWO_PI;
				}
				catch (NumberFormatException e)
				{
					throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
				}
			}

			// Attribute: hue
			attrName = AttrName.HUE;
			attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			attrValue = XmlUtils.getAttribute(element, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			try
			{
				int hueInt = Integer.parseInt(attrValue);
				if ((hueInt < MIN_HUE) || (hueInt > MAX_HUE))
					throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
				hue = (double)hueInt * HUE_FACTOR_INV;
			}
			catch (NumberFormatException e)
			{
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
			}

			// Attribute: saturation
			attrName = AttrName.SATURATION;
			attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			attrValue = XmlUtils.getAttribute(element, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			try
			{
				int saturationInt = Integer.parseInt(attrValue);
				if ((saturationInt < MIN_SATURATION) || (saturationInt > MAX_SATURATION))
					throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
				saturation = (double)saturationInt * SATURATION_FACTOR_INV;
			}
			catch (NumberFormatException e)
			{
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
			}

			// Attribute: phase increment
			attrName = AttrName.PHASE_INCREMENT;
			attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			attrValue = XmlUtils.getAttribute(element, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			try
			{
				phaseIncrement = Double.parseDouble(attrValue);
				double bound = denormalisePhaseIncrement((double)MAX_PHASE_INCREMENT);
				if ((phaseIncrement < -bound) || (phaseIncrement > bound))
					throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
			}
			catch (NumberFormatException e)
			{
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
			}

			// Attribute: motion angle
			attrName = AttrName.MOTION_ANGLE;
			attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			attrValue = XmlUtils.getAttribute(element, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			try
			{
				motionAngle = Double.parseDouble(attrValue);
				if ((motionAngle < MIN_MOTION_ANGLE) || (motionAngle >= MAX_MOTION_ANGLE))
					throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
				motionAngle *= TWO_PI;
			}
			catch (NumberFormatException e)
			{
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
			}

			// Attribute: motion rate
			attrName = AttrName.MOTION_RATE;
			attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			attrValue = XmlUtils.getAttribute(element, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			try
			{
				motionRate = Double.parseDouble(attrValue);
				if ((motionRate < MIN_MOTION_RATE) || (motionRate > MAX_MOTION_RATE))
					throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
			}
			catch (NumberFormatException e)
			{
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
			}

			// Attribute: rotation rate
			if (canRotate())
			{
				attrName = AttrName.ROTATION_RATE;
				attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
				attrValue = XmlUtils.getAttribute(element, attrName);
				if (attrValue == null)
					throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
				try
				{
					rotationRate = Double.parseDouble(attrValue);
					if ((rotationRate < MIN_ROTATION_RATE) || (rotationRate > MAX_ROTATION_RATE))
						throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
					rotationRate *= TWO_PI;
				}
				catch (NumberFormatException e)
				{
					throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
				}
			}

			// Initialise remaining instance variables
			init();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static double attenuationCoeffIntToDouble(
			int	coeff)
		{
			return (double)coeff / (double)MAX_ATTENUATION_COEFFICIENT * ATTENUATION_FACTOR;
		}

		//--------------------------------------------------------------

		private static Shape parseShape(
			Element	element)
			throws XmlParseException
		{
			// Get element path
			String elementPath = XmlUtils.getElementPath(element);

			// Attribute: shape
			String attrName = AttrName.SHAPE;
			String attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			String attrValue = XmlUtils.getAttribute(element, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			Shape kind = Shape.forKey(attrValue);
			if (kind == null)
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
			return kind;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		protected abstract boolean canRotate();

		//--------------------------------------------------------------

		protected abstract double getDistance(
			double	x,
			double	y);

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Source clone()
		{
			try
			{
				return (Source)super.clone();
			}
			catch (CloneNotSupportedException e)
			{
				throw new UnexpectedRuntimeException(e);
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public double getWaveValue(
			int	index)
		{
			return waveTable[index];
		}

		//--------------------------------------------------------------

		protected AttributeList getAttributes()
		{
			AttributeList attributes = new AttributeList();
			attributes.add(AttrName.X, x, AppConstants.FORMAT_1_8);
			attributes.add(AttrName.Y, y, AppConstants.FORMAT_1_8);
			attributes.add(AttrName.WAVEFORM, waveform.key);
			attributes.add(AttrName.WAVE_COEFFICIENT, waveCoeff);
			attributes.add(AttrName.ATTENUATION_COEFFICIENT, attenuationCoeff, AppConstants.FORMAT_1_8);
			attributes.add(AttrName.FREQUENCY, frequency, AppConstants.FORMAT_1_8);
			attributes.add(AttrName.PHASE_OFFSET, phaseOffset, AppConstants.FORMAT_1_8);
			if (canRotate())
				attributes.add(AttrName.ORIENTATION, orientation / TWO_PI, AppConstants.FORMAT_1_8);
			attributes.add(AttrName.HUE, StrictMath.round(hue * HUE_FACTOR));
			attributes.add(AttrName.SATURATION, StrictMath.round(saturation * SATURATION_FACTOR));
			attributes.add(AttrName.PHASE_INCREMENT, phaseIncrement, AppConstants.FORMAT_1_8);
			attributes.add(AttrName.MOTION_ANGLE, motionAngle / TWO_PI, AppConstants.FORMAT_1_8);
			attributes.add(AttrName.MOTION_RATE, motionRate, AppConstants.FORMAT_1_8);
			if (canRotate())
				attributes.add(AttrName.ROTATION_RATE, rotationRate / TWO_PI, AppConstants.FORMAT_1_8);
			return attributes;
		}

		//--------------------------------------------------------------

		private void init()
		{
			startX = x;
			startY = y;
			startPhaseOffset = phaseOffset;
			startOrientation = orientation;
		}

		//--------------------------------------------------------------

		private Source copyModifyX(
			double	x)
		{
			Source copy = clone();
			copy.x = x;
			copy.orientation = StrictMath.PI - orientation;
			if (copy.orientation < 0.0)
				copy.orientation += TWO_PI;
			copy.rotationRate = -rotationRate;
			copy.init();
			return copy;
		}

		//--------------------------------------------------------------

		private Source copyModifyY(
			double	y)
		{
			Source copy = clone();
			copy.y = y;
			copy.orientation = TWO_PI - orientation;
			if (copy.orientation < 0.0)
				copy.orientation += TWO_PI;
			copy.rotationRate = -rotationRate;
			copy.init();
			return copy;
		}

		//--------------------------------------------------------------

		private Source copyModifyXY(
			double	x,
			double	y,
			double	deltaOrientation)
		{
			Source copy = clone();
			copy.x = x;
			copy.y = y;
			copy.orientation = orientation - deltaOrientation * TWO_PI;
			if (copy.orientation < 0.0)
				copy.orientation += TWO_PI;
			copy.init();
			return copy;
		}

		//--------------------------------------------------------------

		private Source copyModifyXY1(
			double	x,
			double	y)
		{
			return copyModifyXY(x, y, 0.5);
		}

		//--------------------------------------------------------------

		private Source copyModifyXY2(
			double	x,
			double	y)
		{
			Source copy = clone();
			copy.x = x;
			copy.y = y;
			copy.orientation = 0.5 * StrictMath.PI - orientation;
			if (copy.orientation < 0.0)
				copy.orientation += TWO_PI;
			copy.rotationRate = -rotationRate;
			copy.init();
			return copy;
		}

		//--------------------------------------------------------------

		private void updatePhaseOffset(
			int	frameIndex)
		{
			phaseOffset = (startPhaseOffset + (double)frameIndex * phaseIncrement) % MAX_PHASE_OFFSET;
			if (phaseOffset < 0.0)
				phaseOffset += MAX_PHASE_OFFSET;
		}

		//--------------------------------------------------------------

		private void updatePosition(
			int		frameIndex,
			int		width,
			int		height,
			double	motionRateOffset)
		{
			double d = (double)frameIndex * motionRate;
			d += motionRateOffset;

			double w = (double)width;
			double xt = startX + d * StrictMath.cos(motionAngle);
			x = xt % w;
			if (x < 0.0)
				x += w;
			int nx = (int)StrictMath.floor(xt / w);
			if (nx % 2 != 0)
				x = w - x;

			double h = (double)height;
			double yt = startY + d * StrictMath.sin(motionAngle);
			y = yt % h;
			if (y < 0.0)
				y += h;
			int ny = (int)StrictMath.floor(yt / h);
			if (ny % 2 != 0)
				y = h - y;
		}

		//--------------------------------------------------------------

		private void updateOrientation(
			int	frameIndex)
		{
			orientation = (startOrientation + (double)frameIndex * rotationRate) % TWO_PI;
		}

		//--------------------------------------------------------------

		private double getWaveValue(
			double	x,
			double	y)
		{
			double d = getDistance(x, y);
			if (variablePhase)
			{
				double length = (double)(waveTable.length - 1);
				double phaseOffset = (this.phaseOffset + d * frequency) * length % length;
				int index = (int)phaseOffset;
				double value =
						waveTable[index] + (waveTable[index + 1] - waveTable[index]) * (phaseOffset - (double)index);
				return value * StrictMath.exp(attenuationCoeff * d);
			}
			else
			{
				d *= WAVE_TABLE_FACTOR_FIXED_PHASE;
				int index = (int)d;
				return waveTable[index] + (waveTable[index + 1] - waveTable[index]) * (d - (double)index);
			}
		}

		//--------------------------------------------------------------

		private void initWaveTable(
			double[]	waveTable)
		{
			this.waveTable = waveTable;
			variablePhase = true;
		}

		//--------------------------------------------------------------

		private void initWaveTable(
			int		width,
			int		height,
			boolean	variablePhase)
		{
			initWaveTable(width, height, variablePhase, 0);
		}

		//--------------------------------------------------------------

		private void initWaveTable(
			int		width,
			int		height,
			boolean	variablePhase,
			int		waveTableLength)
		{
			this.variablePhase = variablePhase;
			int length = (waveTableLength > 0)
								? waveTableLength
								: variablePhase ? WAVE_TABLE_LENGTH_VARIABLE_PHASE
												: (int)StrictMath.ceil(StrictMath.sqrt(width * width + height * height)
																					* WAVE_TABLE_FACTOR_FIXED_PHASE);
			waveTable = new double[length + 1];
			double waveCoeff = (double)this.waveCoeff / (double)MAX_WAVE_COEFFICIENT;
			switch (waveform)
			{
				case TRIANGLE:
					initWaveTriangle(waveCoeff, false);
					break;

				case TRIANGLE_INVERTED:
					initWaveTriangle(waveCoeff, true);
					break;

				case CUBIC:
					initWaveCubic(waveCoeff, false);
					break;

				case CUBIC_INVERTED:
					initWaveCubic(waveCoeff, true);
					break;

				case COSINE:
					initWaveTableCosine(waveCoeff, false);
					break;

				case COSINE_INVERTED:
					initWaveTableCosine(waveCoeff, true);
					break;

				case COMPRESSED_COSINE:
					initWaveTableCompressedCosine(waveCoeff);
					break;

				case MODIFIED_COSINE:
					initWaveTableModifiedCosine(waveCoeff, false);
					break;

				case MODIFIED_COSINE_INVERTED:
					initWaveTableModifiedCosine(waveCoeff, true);
					break;

				case HYPERBOLIC_COSINE:
					initWaveTableHyperbolicCosine(waveCoeff, false);
					break;

				case HYPERBOLIC_COSINE_INVERTED:
					initWaveTableHyperbolicCosine(waveCoeff, true);
					break;
			}
		}

		//--------------------------------------------------------------

		private void initWaveTriangle(
			double	waveCoeff,
			boolean	inverted)
		{
			double waveTableFactor = variablePhase ? 1.0 / (double)(waveTable.length - 1)
												   : WAVE_TABLE_FACTOR_FIXED_PHASE_INV;
			double x0 = waveCoeff * 0.5;
			double x1 = 0.5;
			double x2r = x0;
			double x2 = 1.0 - x2r;
			double m0 = (x0 == 0.0) ? 1.0 : 1.0 / x0;
			double m1 = (x0 == 0.5) ? 1.0 : 1.0 / (0.5 - x0);
			if (inverted)
			{
				m0 = -m0;
				m1 = -m1;
			}
			for (int i = 0; i < waveTable.length; i++)
			{
				double x = (double)i * waveTableFactor;
				double xo = (variablePhase ? x : (phaseOffset + x * frequency)) % 1.0;
				double y = 0.0;
				if (xo < x0)
					y = xo * m0;
				else if (xo < x1)
					y = (x1 - xo) * m1;
				else if (xo < x2)
					y = (xo - x1) * -m1;
				else
					y = (1.0 - xo) * -m0;
				y *= 0.5;
				y += 0.5;
				if (!variablePhase)
					y *= StrictMath.exp(attenuationCoeff * x);
				waveTable[i] = y;
			}
		}

		//--------------------------------------------------------------

		private void initWaveCubic(
			double	waveCoeff,
			boolean	inverted)
		{
			final	double	FACTOR	= 2.0;

			double waveTableFactor = variablePhase ? 1.0 / (double)(waveTable.length - 1)
												   : WAVE_TABLE_FACTOR_FIXED_PHASE_INV;
			double a = (1.0 - 2.0 * waveCoeff) * FACTOR;
			double minY = Double.MAX_VALUE;
			double maxY = Double.MIN_VALUE;
			for (int i = 0; i < waveTable.length; i++)
			{
				double x = (double)i * waveTableFactor;
				double xo = (variablePhase ? x : (phaseOffset + x * frequency)) % 1.0;
				double y = xo * (xo - 1.0) * (xo - 0.5) * StrictMath.exp(a * xo);
				if (minY > y)
					minY = y;
				if (maxY < y)
					maxY = y;
				waveTable[i] = y;
			}

			double factor = 1.0 / (maxY - minY);
			for (int i = 0; i < waveTable.length; i++)
			{
				double y = (waveTable[i] - minY) * factor;
				if (inverted)
					y = 1.0 - y;
				if (!variablePhase)
				{
					double x = (double)i * waveTableFactor;
					y *= StrictMath.exp(attenuationCoeff * x);
				}
				waveTable[i] = y;
			}
		}

		//--------------------------------------------------------------

		private void initWaveTableCosine(
			double	waveCoeff,
			boolean	inverted)
		{
			final	double	FACTOR	= 2.0;

			double waveTableFactor = variablePhase ? 1.0 / (double)(waveTable.length - 1)
												   : WAVE_TABLE_FACTOR_FIXED_PHASE_INV;
			double a = (1.0 - 2.0 * waveCoeff) * FACTOR;
			double minY = Double.MAX_VALUE;
			double maxY = Double.MIN_VALUE;
			for (int i = 0; i < waveTable.length; i++)
			{
				double x = (double)i * waveTableFactor;
				double xo = (variablePhase ? x : (phaseOffset + x * frequency)) % 1.0;
				double y = StrictMath.cos(TWO_PI * xo) * StrictMath.exp(a * xo);
				if (minY > y)
					minY = y;
				if (maxY < y)
					maxY = y;
				waveTable[i] = y;
			}

			double factor = 1.0 / (maxY - minY);
			for (int i = 0; i < waveTable.length; i++)
			{
				double y = (waveTable[i] - minY) * factor;
				if (inverted)
					y = 1.0 - y;
				if (!variablePhase)
				{
					double x = (double)i * waveTableFactor;
					y *= StrictMath.exp(attenuationCoeff * x);
				}
				waveTable[i] = y;
			}
		}

		//--------------------------------------------------------------

		private void initWaveTableCompressedCosine(
			double	waveCoeff)
		{
			double waveTableFactor = variablePhase ? 1.0 / (double)(waveTable.length - 1)
												   : WAVE_TABLE_FACTOR_FIXED_PHASE_INV;
			double factor = StrictMath.exp(waveCoeff);
			for (int i = 0; i < waveTable.length; i++)
			{
				double x = (double)i * waveTableFactor;
				double y = StrictMath.cos(TWO_PI * (variablePhase ? x : (phaseOffset + x * frequency)));
				if (waveCoeff != 0.0)
					y *= StrictMath.exp(-waveCoeff * StrictMath.abs(y)) * factor;
				y *= 0.5;
				y += 0.5;
				if (!variablePhase)
					y *= StrictMath.exp(attenuationCoeff * x);
				waveTable[i] = y;
			}
		}

		//--------------------------------------------------------------

		private void initWaveTableModifiedCosine(
			double	waveCoeff,
			boolean	inverted)
		{
			double waveTableFactor = variablePhase ? 1.0 / (double)(waveTable.length - 1)
												   : WAVE_TABLE_FACTOR_FIXED_PHASE_INV;
			double a = 0.5 * (waveCoeff + 1.0);
			double maxY = Double.MIN_VALUE;
			for (int i = 0; i < waveTable.length; i++)
			{
				double x = (double)i * waveTableFactor;
				double xo = (variablePhase ? x : (phaseOffset + x * frequency)) % 1.0;
				double x2 = 2.0 * TWO_PI * xo;
				double y = (xo == 0.0) ? 0.0 : (1.0 - StrictMath.cos(a * x2)) / x2;
				if (maxY < y)
					maxY = y;
				waveTable[i] = y;
			}

			double factor = 1.0 / maxY;
			for (int i = 0; i < waveTable.length; i++)
			{
				double y = waveTable[i] * factor;
				if (inverted)
					y = 1.0 - y;
				if (!variablePhase)
				{
					double x = (double)i * waveTableFactor;
					y *= StrictMath.exp(attenuationCoeff * x);
				}
				waveTable[i] = y;
			}
		}

		//--------------------------------------------------------------

		private void initWaveTableHyperbolicCosine(
			double	waveCoeff,
			boolean	inverted)
		{
			final	double	PRECISION_BOUND	= 0.000000005;

			boolean negative = false;
			if (waveCoeff < 0.5)
			{
				waveCoeff = 1.0 - waveCoeff;
				negative = true;
			}
			double a = 1.0 - 11.0 * StrictMath.log(2.0 * waveCoeff) / StrictMath.log(0.5);
			double b = 1.0;
			if (negative)
			{
				double temp = a;
				a = b;
				b = temp;
			}
			double x0 = -1.5;
			while (true)
			{
				double prevX = x0;
				double expax = StrictMath.exp(a * x0);
				double expbx = StrictMath.exp(-b * x0);
				x0 -= (expax + expbx - 4.0) / (a * expax - b * expbx);
				if (StrictMath.abs(x0 - prevX) < PRECISION_BOUND)
					break;
			}
			double x1 = 1.5;
			while (true)
			{
				double prevX = x1;
				double expax = StrictMath.exp(a * x1);
				double expbx = StrictMath.exp(-b * x1);
				x1 -= (expax + expbx - 4.0) / (a * expax - b * expbx);
				if (StrictMath.abs(x1 - prevX) < PRECISION_BOUND)
					break;
			}
			double xInterval = x1 - x0;

			double waveTableFactor = variablePhase ? 1.0 / (double)(waveTable.length - 1)
												   : WAVE_TABLE_FACTOR_FIXED_PHASE_INV;
			double maxY = Double.MIN_VALUE;
			for (int i = 0; i < waveTable.length; i++)
			{
				double x = (double)i * waveTableFactor;
				double xo = (variablePhase ? x : (phaseOffset + x * frequency)) % 1.0;
				double xf = x0 + xo * xInterval;
				double y = 2.0 - 0.5 * (StrictMath.exp(a * xf) + StrictMath.exp(-b * xf));
				if (maxY < y)
					maxY = y;
				waveTable[i] = y;
			}

			double factor = 1.0 / maxY;
			for (int i = 0; i < waveTable.length; i++)
			{
				double y = waveTable[i] * factor;
				if (!inverted)
					y = 1.0 - y;
				if (!variablePhase)
				{
					double x = (double)i * waveTableFactor;
					y *= StrictMath.exp(attenuationCoeff * x);
				}
				waveTable[i] = y;
			}
		}

		//--------------------------------------------------------------

		private Element createElement(
			Document	document)
		{
			Element element = document.createElement(ElementName.SOURCE);
			for (Attribute attribute : getAttributes())
				attribute.set(element);

			return element;
		}

		//--------------------------------------------------------------

		private void write(
			XmlWriter	writer,
			int			indent)
			throws IOException
		{
			writer.writeEmptyElement(Pattern1Image.ElementName.SOURCE, getAttributes(), indent, true);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Enumerated types
	////////////////////////////////////////////////////////////////////


		// ENUMERATION: SHAPE


		enum Shape
			implements IStringKeyed
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			CIRCLE
			(
				"circle"
			)
			{
				@Override
				protected Source createSource(
					Element	element)
					throws XmlParseException
				{
					return new Circle(element);
				}
			},

			ELLIPSE
			(
				"ellipse"
			)
			{
				@Override
				protected Source createSource(
					Element	element)
					throws XmlParseException
				{
					return new Ellipse(element);
				}
			},

			POLYGON
			(
				"polygon"
			)
			{
				@Override
				protected Source createSource(
					Element	element)
					throws XmlParseException
				{
					return new Polygon(element);
				}
			};

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	String	key;

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Shape(
				String	key)
			{
				this.key = key;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Class methods
		////////////////////////////////////////////////////////////////

			public static Shape forKey(
				String	key)
			{
				for (Shape value : values())
				{
					if (value.key.equals(key))
						return value;
				}
				return null;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Abstract methods
		////////////////////////////////////////////////////////////////

			protected abstract Source createSource(
				Element	element)
				throws XmlParseException;

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : IStringKeyed interface
		////////////////////////////////////////////////////////////////

			@Override
			public String getKey()
			{
				return key;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			public String toString()
			{
				return StringUtils.firstCharToUpperCase(key);
			}

			//----------------------------------------------------------

		}

		//==============================================================


		// ENUMERATION: WAVEFORM


		enum Waveform
			implements IStringKeyed
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			TRIANGLE
			(
				"triangle-1",
				"Triangle",
				50
			),

			TRIANGLE_INVERTED
			(
				"triangleInverted-1",
				"Triangle, inverted",
				50
			),

			CUBIC
			(
				"cubic-1",
				"Cubic",
				50
			),

			CUBIC_INVERTED
			(
				"cubicInverted-1",
				"Cubic, inverted",
				50
			),

			COSINE
			(
				"cosine-1",
				"Cosine",
				50
			),

			COSINE_INVERTED
			(
				"cosineInverted-1",
				"Cosine, inverted",
				50
			),

			COMPRESSED_COSINE
			(
				"compressedCosine-1",
				"Compressed cosine",
				0
			),

			MODIFIED_COSINE
			(
				"modifiedCosine-1",
				"Modified cosine",
				0
			),

			MODIFIED_COSINE_INVERTED
			(
				"modifiedCosineInverted-1",
				"Modified cosine, inverted",
				0
			),

			HYPERBOLIC_COSINE
			(
				"hyperbolicCosine-1",
				"Hyperbolic cosine",
				50
			),

			HYPERBOLIC_COSINE_INVERTED
			(
				"hyperbolicCosineInverted-1",
				"Hyperbolic cosine, inverted",
				50
			);

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	String	key;
			private	String	text;
			private	int		defaultCoeff;

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Waveform(
				String	key,
				String	text,
				int		defaultCoeff)
			{
				this.key = key;
				this.text = text;
				this.defaultCoeff = defaultCoeff;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Class methods
		////////////////////////////////////////////////////////////////

			public static Waveform forKey(
				String	key)
			{
				for (Waveform value : values())
				{
					if (value.key.equals(key))
						return value;
				}
				return null;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : IStringKeyed interface
		////////////////////////////////////////////////////////////////

			@Override
			public String getKey()
			{
				return key;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			public String toString()
			{
				return text;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods
		////////////////////////////////////////////////////////////////

			public int getDefaultCoefficient()
			{
				return defaultCoeff;
			}

			//----------------------------------------------------------

		}

		//==============================================================


		// ENUMERATION: CONSTRAINT


		enum Constraint
			implements IStringKeyed
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			NONE
			(
				"none",
				"None"
			),

			VERTICAL_AXIS
			(
				"verticalAxis",
				"Vertical axis"
			),

			HORIZONTAL_AXIS
			(
				"horizontalAxis",
				"Horizontal axis"
			);

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	String	key;
			private	String	text;

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Constraint(
				String	key,
				String	text)
			{
				this.key = key;
				this.text = text;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Class methods
		////////////////////////////////////////////////////////////////

			public static Constraint forKey(
				String	key)
			{
				for (Constraint value : values())
				{
					if (value.key.equals(key))
						return value;
				}
				return null;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : IStringKeyed interface
		////////////////////////////////////////////////////////////////

			@Override
			public String getKey()
			{
				return key;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			public String toString()
			{
				return text;
			}

			//----------------------------------------------------------

		}

		//==============================================================

	////////////////////////////////////////////////////////////////////
	//  Member classes : non-inner classes
	////////////////////////////////////////////////////////////////////


		// CLASS: CIRCLE


		public static class Circle
			extends Source
			implements Cloneable
		{

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Circle(
				Waveform	waveform,
				int			waveCoeff)
			{
				super(waveform, waveCoeff);
			}

			//----------------------------------------------------------

			private Circle(
				double		x,
				double		y,
				Waveform	waveform,
				int			waveCoeff,
				int			attenuationCoeff,
				double		frequency,
				double		phaseOffset,
				int			hue,
				int			saturation,
				double		phaseIncrement,
				double		motionAngle,
				double		motionRate)
			{
				super(x, y, waveform, waveCoeff, attenuationCoeff, frequency, phaseOffset, 0.0, hue,
					  saturation, phaseIncrement, motionAngle, motionRate, 0.0);
			}

			//----------------------------------------------------------

			private Circle(
				Element	element)
				throws XmlParseException
			{
				super(element);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			public Circle clone()
			{
				return (Circle)super.clone();
			}

			//----------------------------------------------------------

			@Override
			protected boolean canRotate()
			{
				return false;
			}

			//----------------------------------------------------------

			@Override
			protected double getDistance(
				double	x,
				double	y)
			{
//XXX
//				double dx = x - this.x;
//				double dy = y - this.y;
//				return StrictMath.sqrt(dx * dx + dy * dy);
				return StrictMath.hypot(x - this.x, y - this.y);
			}

			//----------------------------------------------------------

			@Override
			protected AttributeList getAttributes()
			{
				AttributeList attributes = new AttributeList();
				attributes.add(AttrName.SHAPE, Shape.CIRCLE.key);
				attributes.add(super.getAttributes());
				return attributes;
			}

			//----------------------------------------------------------

		}

		//==============================================================


		// CLASS: ELLIPSE


		public static class Ellipse
			extends Source
			implements Cloneable
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			public static final		int		MIN_ECCENTRICITY	= 1;
			public static final		int		MAX_ECCENTRICITY	= 9;

			private static final	double	ECCENTRICITY_FACTOR		= 10.0;
			private static final	double	ECCENTRICITY_FACTOR_INV	= 1.0 / ECCENTRICITY_FACTOR;

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	int		eccentricity;
			private	double	eccentricityCoeff;

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Ellipse(
				double		x,
				double		y,
				Waveform	waveform,
				int			waveCoeff,
				int			attenuationCoeff,
				double		frequency,
				double		phaseOffset,
				double		orientation,
				int			eccentricity,
				int			hue,
				int			saturation,
				double		phaseIncrement,
				double		motionAngle,
				double		motionRate,
				double		rotationRate)
			{
				// Call superclass constructor
				super(x, y, waveform, waveCoeff, attenuationCoeff, frequency, phaseOffset, orientation,
					  hue, saturation, phaseIncrement, motionAngle, motionRate, rotationRate);

				// Initialise instance variables
				this.eccentricity = eccentricity;
				double e = (double)eccentricity * ECCENTRICITY_FACTOR_INV;
				eccentricityCoeff = StrictMath.sqrt(1.0 - e * e);
			}

			//----------------------------------------------------------

			private Ellipse(
				Element	element)
				throws XmlParseException
			{
				// Call superclass constructor
				super(element);

				// Get element path
				String elementPath = XmlUtils.getElementPath(element);

				// Attribute: eccentricity
				String attrName = AttrName.ECCENTRICITY;
				String attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
				String attrValue = XmlUtils.getAttribute(element, attrName);
				if (attrValue == null)
					throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
				try
				{
					eccentricity = Integer.parseInt(attrValue);
					if ((eccentricity < MIN_ECCENTRICITY) || (eccentricity > MAX_ECCENTRICITY))
						throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
					double e = (double)eccentricity * ECCENTRICITY_FACTOR_INV;
					eccentricityCoeff = StrictMath.sqrt(1.0 - e * e);
				}
				catch (NumberFormatException e)
				{
					throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
				}
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			public Ellipse clone()
			{
				return (Ellipse)super.clone();
			}

			//----------------------------------------------------------

			@Override
			protected boolean canRotate()
			{
				return true;
			}

			//----------------------------------------------------------

			@Override
			protected double getDistance(
				double	x,
				double	y)
			{
				double dx = x - this.x;
				double dy = y - this.y;
				double theta = StrictMath.atan2(dy, dx) - orientation;
				double a = StrictMath.sin(theta);
				double b = eccentricityCoeff * StrictMath.cos(theta);
//XXX
//				return StrictMath.sqrt(dx * dx + dy * dy) * StrictMath.sqrt(a * a + b * b);
				return StrictMath.hypot(dx, dy) * StrictMath.sqrt(a * a + b * b);
			}

			//----------------------------------------------------------

			@Override
			protected AttributeList getAttributes()
			{
				AttributeList attributes = new AttributeList();
				attributes.add(AttrName.SHAPE, Shape.ELLIPSE.key);
				attributes.add(AttrName.ECCENTRICITY, eccentricity);
				attributes.add(super.getAttributes());
				return attributes;
			}

			//----------------------------------------------------------

		}

		//==============================================================


		// CLASS: POLYGON


		public static class Polygon
			extends Source
			implements Cloneable
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			public static final		int	MIN_NUM_EDGES	= 3;
			public static final		int	MAX_NUM_EDGES	= 12;

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	int		numEdges;
			private	double	anglePerEdge;

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Polygon(
				double		x,
				double		y,
				Waveform	waveform,
				int			waveCoeff,
				int			attenuationCoeff,
				double		frequency,
				double		phaseOffset,
				double		orientation,
				int			numEdges,
				int			hue,
				int			saturation,
				double		phaseIncrement,
				double		motionAngle,
				double		motionRate,
				double		rotationRate)
			{
				// Call superclass constructor
				super(x, y, waveform, waveCoeff, attenuationCoeff, frequency, phaseOffset, orientation,
					  hue, saturation, phaseIncrement, motionAngle, motionRate, rotationRate);

				// Initialise instance variables
				this.numEdges = numEdges;
				anglePerEdge = TWO_PI / (double)numEdges;
			}

			//----------------------------------------------------------

			private Polygon(
				Element	element)
				throws XmlParseException
			{
				// Call superclass constructor
				super(element);

				// Get element path
				String elementPath = XmlUtils.getElementPath(element);

				// Attribute: number of edges
				String attrName = AttrName.NUM_EDGES;
				String attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
				String attrValue = XmlUtils.getAttribute(element, attrName);
				if (attrValue == null)
					throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
				try
				{
					numEdges = Integer.parseInt(attrValue);
					if ((numEdges < MIN_NUM_EDGES) || (numEdges > MAX_NUM_EDGES))
						throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
					anglePerEdge = TWO_PI / (double)numEdges;
				}
				catch (NumberFormatException e)
				{
					throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
				}
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			public Polygon clone()
			{
				return (Polygon)super.clone();
			}

			//----------------------------------------------------------

			@Override
			protected boolean canRotate()
			{
				return true;
			}

			//----------------------------------------------------------

			@Override
			protected double getDistance(
				double	x,
				double	y)
			{
				double dx = x - this.x;
				double dy = y - this.y;
				double phi = orientation % anglePerEdge;
				double theta = StrictMath.atan2(dy, dx) - phi;
				double rotAngle = StrictMath.floor(theta / anglePerEdge) * -anglePerEdge;
				rotAngle -= 0.5 * anglePerEdge + phi;
				return StrictMath.abs(dx * StrictMath.cos(rotAngle) - dy * StrictMath.sin(rotAngle));
			}

			//----------------------------------------------------------

			@Override
			protected AttributeList getAttributes()
			{
				AttributeList attributes = new AttributeList();
				attributes.add(AttrName.SHAPE, Shape.POLYGON.key);
				attributes.add(AttrName.NUM_EDGES, numEdges);
				attributes.add(super.getAttributes());
				return attributes;
			}

			//----------------------------------------------------------

		}

		//==============================================================

	}

	//==================================================================


	// CLASS: BRIGHTNESS KEY


	private static class BrightnessKey
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int	animationKindBitField;
		private	int	startFrameIndex;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private BrightnessKey(
			Set<AnimationKind>	animationKinds,
			int					startFrameIndex)
		{
			animationKindBitField = AnimationKind.setToBitField(animationKinds);
			this.startFrameIndex = startFrameIndex;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public boolean equals(
			Object	obj)
		{
			if (this == obj)
				return true;

			return (obj instanceof BrightnessKey bk) && (animationKindBitField == bk.animationKindBitField)
					&& (startFrameIndex == bk.startFrameIndex);
		}

		//--------------------------------------------------------------

		@Override
		public int hashCode()
		{
			return (animationKindBitField << 28) | startFrameIndex;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private long toLong()
		{
			return ((long)animationKindBitField << 32) | startFrameIndex;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
