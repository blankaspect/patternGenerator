/*====================================================================*\

Pattern2Params.java

Pattern 2 parameters class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.misc.CollectionUtils;
import uk.blankaspect.common.misc.ColourUtils;
import uk.blankaspect.common.misc.IntegerRange;
import uk.blankaspect.common.misc.Property;

//----------------------------------------------------------------------


// PATTERN 2 PARAMETERS CLASS


class Pattern2Params
	extends PatternParams
	implements Cloneable
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final	int	MIN_WIDTH		= 1 << 5;   // 32;
	public static final	int	MAX_WIDTH		= 1 << 13;  // 8192
	public static final	int	DEFAULT_WIDTH	= 640;

	public static final	int	MIN_HEIGHT		= 1 << 5;   // 32
	public static final	int	MAX_HEIGHT		= 1 << 13;  // 8192
	public static final	int	DEFAULT_HEIGHT	= 480;

	public static final	int	MIN_END_MARGIN		= 1;
	public static final	int	MAX_END_MARGIN		= 8;
	public static final	int	DEFAULT_END_MARGIN	= 2;

	public static final	int	MIN_SIDE_MARGIN		= 1;
	public static final	int	MAX_SIDE_MARGIN		= 16;
	public static final	int	DEFAULT_SIDE_MARGIN	= 4;

	public static final	double	MIN_GRID_INTERVAL		= 3.0;
	public static final	double	MAX_GRID_INTERVAL		= 128.0;
	public static final	double	DEFAULT_GRID_INTERVAL	= 16.0;

	public static final	double	MIN_PATH_THICKNESS		= 1.0;
	public static final	double	MAX_PATH_THICKNESS		= 64.0;
	public static final	double	DEFAULT_PATH_THICKNESS	= 8.0;

	public static final	double	MIN_TERMINAL_DIAMETER		= 1.0;
	public static final	double	MAX_TERMINAL_DIAMETER		= 96.0;
	public static final	double	DEFAULT_TERMINAL_DIAMETER	= 12.0;

	public static final	int	MIN_EXPECTED_PATH_LENGTH		= 1;
	public static final	int	MAX_EXPECTED_PATH_LENGTH		= 64;
	public static final	int	DEFAULT_EXPECTED_PATH_LENGTH	= 6;

	public static final	int	MIN_DIRECTION_PROBABILITY	= 0;
	public static final	int	MAX_DIRECTION_PROBABILITY	= 100;

	public static final	Pattern2Image.Direction.Mode	DEFAULT_DIRECTION_MODE	= Pattern2Image.Direction.Mode.ABSOLUTE;

	public static final	Pattern2Image.Orientation	DEFAULT_ORIENTATION	= Pattern2Image.Orientation.UP;

	public static final	Pattern2Image.TerminalEmphasis	DEFAULT_TERMINAL_EMPHASIS	=
																				Pattern2Image.TerminalEmphasis.START;

	public static final	boolean	DEFAULT_SHOW_EMPTY_PATHS	= true;

	public static final	Color	DEFAULT_BACKGROUND_COLOUR	= Color.BLACK;
	public static final	Color	DEFAULT_TRANSPARENCY_COLOUR	= Color.WHITE;
	public static final	Color	DEFAULT_PATH_COLOUR			= new Color(208, 208, 208);

	public static final	int	MIN_NUM_PATH_COLOURS	= 1;
	public static final	int	MAX_NUM_PATH_COLOURS	= 40;

	public static final	long	MIN_SEED	= App.MIN_SEED;
	public static final	long	MAX_SEED	= App.MAX_SEED;

	private static final	Map<Pattern2Image.Direction, Integer>	DEFAULT_PROBABILITIES;

	private interface Key
	{
		String	ACTIVE_FRACTION					= "activeFraction";
		String	BACKGROUND_COLOUR				= "backgroundColour";
		String	DESCRIPTION						= "description";
		String	DIRECTION_MODE					= "directionMode";
		String	DIRECTION_PROBABILITY			= "directionProbability";
		String	END_MARGIN						= "endMargin";
		String	EXPECTED_PATH_LENGTH			= "expectedPathLength";
		String	GRID_INTERVAL					= "gridInterval";
		String	HEIGHT							= "height";
		String	ORIENTATION						= "orientation";
		String	PATH_COLOUR						= "pathColour";
		String	PATH_THICKNESS					= "pathThickness";
		String	SEED							= "seed";
		String	SHOW_EMPTY_PATHS				= "showEmptyPaths";
		String	SIDE_MARGIN						= "sideMargin";
		String	TERMINAL_DIAMETER				= "terminalDiameter";
		String	TERMINAL_EMPHASIS				= "terminalEmphasis";
		String	TRANSITION_INTERVAL_RANGE		= "transitionIntervalRange";
		String	TRANSPARENCY_COLOUR				= "transparencyColour";
		String	WIDTH							= "width";
	}

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// PROPERTY CLASS: DESCRIPTION


	private class PPDescription
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPDescription()
		{
			super(Key.DESCRIPTION);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getDescription()
	{
		return ppDescription.getValue();
	}

	//------------------------------------------------------------------

	public void setDescription(String value)
	{
		ppDescription.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPDescription	ppDescription	= new PPDescription();

	//==================================================================


	// PROPERTY CLASS: WIDTH


	private class PPWidth
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPWidth()
		{
			super(Key.WIDTH, MIN_WIDTH, MAX_WIDTH);
			value = DEFAULT_WIDTH;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	@Override
	public int getWidth()
	{
		return ppWidth.getValue();
	}

	//------------------------------------------------------------------

	@Override
	public void setWidth(int value)
	{
		ppWidth.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPWidth	ppWidth	= new PPWidth();

	//==================================================================


	// PROPERTY CLASS: HEIGHT


	private class PPHeight
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPHeight()
		{
			super(Key.HEIGHT, MIN_HEIGHT, MAX_HEIGHT);
			value = DEFAULT_HEIGHT;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	@Override
	public int getHeight()
	{
		return ppHeight.getValue();
	}

	//------------------------------------------------------------------

	@Override
	public void setHeight(int value)
	{
		ppHeight.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPHeight	ppHeight	= new PPHeight();

	//==================================================================


	// PROPERTY CLASS: ORIENTATION


	private class PPOrientation
		extends Property.EnumProperty<Pattern2Image.Orientation>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPOrientation()
		{
			super(Key.ORIENTATION, Pattern2Image.Orientation.class);
			value = DEFAULT_ORIENTATION;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Pattern2Image.Orientation getOrientation()
	{
		return ppOrientation.getValue();
	}

	//------------------------------------------------------------------

	public void setOrientation(Pattern2Image.Orientation value)
	{
		ppOrientation.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPOrientation	ppOrientation	= new PPOrientation();

	//==================================================================


	// PROPERTY CLASS: END MARGIN


	private class PPEndMargin
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPEndMargin()
		{
			super(Key.END_MARGIN, MIN_END_MARGIN, MAX_END_MARGIN);
			value = DEFAULT_END_MARGIN;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getEndMargin()
	{
		return ppEndMargin.getValue();
	}

	//------------------------------------------------------------------

	public void setEndMargin(int value)
	{
		ppEndMargin.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPEndMargin	ppEndMargin	= new PPEndMargin();

	//==================================================================


	// PROPERTY CLASS: SIDE MARGIN


	private class PPSideMargin
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPSideMargin()
		{
			super(Key.SIDE_MARGIN, MIN_SIDE_MARGIN, MAX_SIDE_MARGIN);
			value = DEFAULT_SIDE_MARGIN;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getSideMargin()
	{
		return ppSideMargin.getValue();
	}

	//------------------------------------------------------------------

	public void setSideMargin(int value)
	{
		ppSideMargin.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPSideMargin	ppSideMargin	= new PPSideMargin();

	//==================================================================


	// PROPERTY CLASS: GRID INTERVAL


	private class PPGridInterval
		extends Property.DoubleProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPGridInterval()
		{
			super(Key.GRID_INTERVAL, MIN_GRID_INTERVAL, MAX_GRID_INTERVAL, AppConstants.FORMAT_1_3);
			value = DEFAULT_GRID_INTERVAL;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public double getGridInterval()
	{
		return ppGridInterval.getValue();
	}

	//------------------------------------------------------------------

	public void setGridInterval(double value)
	{
		ppGridInterval.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPGridInterval	ppGridInterval	= new PPGridInterval();

	//==================================================================


	// PROPERTY CLASS: PATH THICKNESS


	private class PPPathThickness
		extends Property.DoubleProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPPathThickness()
		{
			super(Key.PATH_THICKNESS, MIN_PATH_THICKNESS, MAX_PATH_THICKNESS, AppConstants.FORMAT_1_3);
			value = DEFAULT_PATH_THICKNESS;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public double getPathThickness()
	{
		return ppPathThickness.getValue();
	}

	//------------------------------------------------------------------

	public void setPathThickness(double value)
	{
		ppPathThickness.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPPathThickness	ppPathThickness	= new PPPathThickness();

	//==================================================================


	// PROPERTY CLASS: TERMINAL DIAMETER


	private class PPTerminalDiameter
		extends Property.DoubleProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPTerminalDiameter()
		{
			super(Key.TERMINAL_DIAMETER, MIN_TERMINAL_DIAMETER, MAX_TERMINAL_DIAMETER,
				  AppConstants.FORMAT_1_3);
			value = DEFAULT_TERMINAL_DIAMETER;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public double getTerminalDiameter()
	{
		return ppTerminalDiameter.getValue();
	}

	//------------------------------------------------------------------

	public void setTerminalDiameter(double value)
	{
		ppTerminalDiameter.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPTerminalDiameter	ppTerminalDiameter	= new PPTerminalDiameter();

	//==================================================================


	// PROPERTY CLASS: EXPECTED PATH LENGTH


	private class PPExpectedPathLength
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPExpectedPathLength()
		{
			super(Key.EXPECTED_PATH_LENGTH, MIN_EXPECTED_PATH_LENGTH, MAX_EXPECTED_PATH_LENGTH);
			value = DEFAULT_EXPECTED_PATH_LENGTH;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getExpectedPathLength()
	{
		return ppExpectedPathLength.getValue();
	}

	//------------------------------------------------------------------

	public void setExpectedPathLength(int value)
	{
		ppExpectedPathLength.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPExpectedPathLength	ppExpectedPathLength	= new PPExpectedPathLength();

	//==================================================================


	// PROPERTY CLASS: DIRECTION PROBABILITY


	private class PPDirectionProbability
		extends Property.PropertyMap<Pattern2Image.Direction, Integer>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPDirectionProbability()
		{
			super(Key.DIRECTION_PROBABILITY, Pattern2Image.Direction.class);
			values.putAll(DEFAULT_PROBABILITIES);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input                   input,
						  Pattern2Image.Direction direction)
			throws AppException
		{
			int outValue = input.parseInteger(new IntegerRange(MIN_DIRECTION_PROBABILITY,
															   MAX_DIRECTION_PROBABILITY));
			values.put(direction, outValue);
		}

		//--------------------------------------------------------------

		@Override
		public String toString(Pattern2Image.Direction direction)
		{
			Integer value = getValue(direction);
			return ((value == null) ? "0" : value.toString());
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Integer getDirectionProbability(Pattern2Image.Direction key)
	{
		return ppDirectionProbability.getValue(key);
	}

	//------------------------------------------------------------------

	public Map<Pattern2Image.Direction, Integer> getDirectionProbabilities()
	{
		return ppDirectionProbability.getValues();
	}

	//------------------------------------------------------------------

	public void setDirectionProbability(Pattern2Image.Direction key,
										int                     value)
	{
		ppDirectionProbability.setValue(key, value);
	}

	//------------------------------------------------------------------

	public void setDirectionProbabilities(Map<Pattern2Image.Direction, Integer> values)
	{
		ppDirectionProbability.clear();
		for (Pattern2Image.Direction direction : values.keySet())
			ppDirectionProbability.setValue(direction, values.get(direction));
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPDirectionProbability	ppDirectionProbability	= new PPDirectionProbability();

	//==================================================================


	// PROPERTY CLASS: DIRECTION MODE


	private class PPDirectionMode
		extends Property.EnumProperty<Pattern2Image.Direction.Mode>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPDirectionMode()
		{
			super(Key.DIRECTION_MODE, Pattern2Image.Direction.Mode.class);
			value = DEFAULT_DIRECTION_MODE;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Pattern2Image.Direction.Mode getDirectionMode()
	{
		return ppDirectionMode.getValue();
	}

	//------------------------------------------------------------------

	public void setDirectionMode(Pattern2Image.Direction.Mode value)
	{
		ppDirectionMode.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPDirectionMode	ppDirectionMode	= new PPDirectionMode();

	//==================================================================


	// PROPERTY CLASS: TERMINAL EMPHASIS


	private class PPTerminalEmphasis
		extends Property.EnumProperty<Pattern2Image.TerminalEmphasis>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPTerminalEmphasis()
		{
			super(Key.TERMINAL_EMPHASIS, Pattern2Image.TerminalEmphasis.class);
			value = DEFAULT_TERMINAL_EMPHASIS;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Pattern2Image.TerminalEmphasis getTerminalEmphasis()
	{
		return ppTerminalEmphasis.getValue();
	}

	//------------------------------------------------------------------

	public void setTerminalEmphasis(Pattern2Image.TerminalEmphasis value)
	{
		ppTerminalEmphasis.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPTerminalEmphasis	ppTerminalEmphasis	= new PPTerminalEmphasis();

	//==================================================================


	// PROPERTY CLASS: SHOW EMPTY PATHS


	private class PPShowEmptyPaths
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPShowEmptyPaths()
		{
			super(Key.SHOW_EMPTY_PATHS);
			value = DEFAULT_SHOW_EMPTY_PATHS;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isShowEmptyPaths()
	{
		return ppShowEmptyPaths.getValue();
	}

	//------------------------------------------------------------------

	public void setShowEmptyPaths(boolean value)
	{
		ppShowEmptyPaths.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPShowEmptyPaths	ppShowEmptyPaths	= new PPShowEmptyPaths();

	//==================================================================


	// PROPERTY CLASS: TRANSPARENCY COLOUR


	private class PPTransparencyColour
		extends Property.ColourProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPTransparencyColour()
		{
			super(Key.TRANSPARENCY_COLOUR);
			value = DEFAULT_TRANSPARENCY_COLOUR;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Color getTransparencyColour()
	{
		return ppTransparencyColour.getValue();
	}

	//------------------------------------------------------------------

	public void setTransparencyColour(Color value)
	{
		ppTransparencyColour.setValue(ColourUtils.opaque(value));
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPTransparencyColour	ppTransparencyColour	= new PPTransparencyColour();

	//==================================================================


	// PROPERTY CLASS: BACKGROUND COLOUR


	private class PPBackgroundColour
		extends Property.ColourProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPBackgroundColour()
		{
			super(Key.BACKGROUND_COLOUR);
			value = DEFAULT_BACKGROUND_COLOUR;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Color getBackgroundColour()
	{
		return ppBackgroundColour.getValue();
	}

	//------------------------------------------------------------------

	public void setBackgroundColour(Color value)
	{
		ppBackgroundColour.setValue(ColourUtils.copy(value));
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPBackgroundColour	ppBackgroundColour	= new PPBackgroundColour();

	//==================================================================


	// PROPERTY CLASS: PATH COLOURS


	private class PPPathColours
		extends Property.PropertyList<Color>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPPathColours()
		{
			super(Key.PATH_COLOUR, MAX_NUM_PATH_COLOURS);
			values.add(DEFAULT_PATH_COLOUR);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected void parse(Input input,
							 int   index)
		{
			try
			{
				Color colour = input.parseColour();
				if (index < values.size())
					values.set(index, colour);
				else
				{
					for (int i = values.size(); i < index; i++)
						values.add(DEFAULT_PATH_COLOUR);
					values.add(colour);
				}
			}
			catch (AppException e)
			{
				App.INSTANCE.showWarningMessage(App.SHORT_NAME, e);
			}
		}

		//--------------------------------------------------------------

		@Override
		protected String toString(int index)
		{
			return ColourUtils.colourToRgbString(values.get(index));
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Color getPathColour(int index)
	{
		return ppPathColours.getValue(index);
	}

	//------------------------------------------------------------------

	public List<Color> getPathColours()
	{
		return ppPathColours.getValues();
	}

	//------------------------------------------------------------------

	public void setPathColours(List<Color> values)
	{
		ppPathColours.setValues(values);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPPathColours	ppPathColours	= new PPPathColours();

	//==================================================================


	// PROPERTY CLASS: ACTIVE FRACTION


	private class PPActiveFraction
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPActiveFraction()
		{
			super(Key.ACTIVE_FRACTION,
				  Pattern2Image.MIN_ACTIVE_FRACTION, Pattern2Image.MAX_ACTIVE_FRACTION);
			value = Pattern2Image.DEFAULT_ACTIVE_FRACTION;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getActiveFraction()
	{
		return ppActiveFraction.getValue();
	}

	//------------------------------------------------------------------

	public void setActiveFraction(int value)
	{
		ppActiveFraction.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPActiveFraction	ppActiveFraction	= new PPActiveFraction();

	//==================================================================


	// PROPERTY CLASS: TRANSITION INTERVAL RANGE


	private class PPTransitionIntervalRange
		extends Property.SimpleProperty<IntegerRange>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPTransitionIntervalRange()
		{
			super(Key.TRANSITION_INTERVAL_RANGE);
			value = Pattern2Image.DEFAULT_TRANSITION_INTERVAL_RANGE;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input input)
			throws AppException
		{
			IntegerRange range = new IntegerRange(Pattern2Image.MIN_TRANSITION_INTERVAL,
												  Pattern2Image.MAX_TRANSITION_INTERVAL);
			int[] outValues = input.parseIntegers(2, new IntegerRange[]{ range, range },
												  Order.GREATER_THAN_OR_EQUAL_TO);
			value = new IntegerRange(outValues[0], outValues[1]);
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return (value.lowerBound + ", " + value.upperBound);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public IntegerRange getTransitionIntervalRange()
	{
		return ppTransitionIntervalRange.getValue();
	}

	//------------------------------------------------------------------

	public void setTransitionIntervalRange(IntegerRange value)
	{
		ppTransitionIntervalRange.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPTransitionIntervalRange	ppTransitionIntervalRange	= new PPTransitionIntervalRange();

	//==================================================================


	// PROPERTY CLASS: SEED


	private class PPSeed
		extends Property.LongProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPSeed()
		{
			super(Key.SEED, MIN_SEED, MAX_SEED);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input input)
			throws AppException
		{
			if (!input.getValue().isEmpty())
				super.parse(input);
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return ((value == null) ? "" : value.toString());
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	@Override
	public Long getSeed()
	{
		return ppSeed.getValue();
	}

	//------------------------------------------------------------------

	@Override
	public void setSeed(Long value)
	{
		ppSeed.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPSeed	ppSeed	= new PPSeed();

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public Pattern2Params()
	{
	}

	//------------------------------------------------------------------

	public Pattern2Params(int                                   width,
						  int                                   height,
						  Pattern2Image.Orientation             orientation,
						  int                                   endMargin,
						  int                                   sideMargin,
						  double                                gridInterval,
						  double                                pathThickness,
						  double                                terminalDiameter,
						  int                                   expectedPathLength,
						  Map<Pattern2Image.Direction, Integer> directionProbabilities,
						  Pattern2Image.Direction.Mode          directionMode,
						  Pattern2Image.TerminalEmphasis        terminalEmphasis,
						  boolean                               showEmptyPaths,
						  Color                                 transparencyColour,
						  Color                                 backgroundColour,
						  List<Color>                           pathColours,
						  int                                   activeFraction,
						  IntegerRange                          transitionIntervalRange,
						  Long                                  seed)
	{
		// Validate parameters
		if ((width < MIN_WIDTH) || (width > MAX_WIDTH) ||
			 (height < MIN_HEIGHT) || (height > MAX_HEIGHT) ||
			 (orientation == null) ||
			 (endMargin < MIN_END_MARGIN) || (endMargin > MAX_END_MARGIN) ||
			 (sideMargin < MIN_SIDE_MARGIN) || (sideMargin > MAX_SIDE_MARGIN) ||
			 (gridInterval < MIN_GRID_INTERVAL) || (gridInterval > MAX_GRID_INTERVAL) ||
			 (pathThickness < MIN_PATH_THICKNESS) || (pathThickness > MAX_PATH_THICKNESS) ||
			 (terminalDiameter < MIN_TERMINAL_DIAMETER) || (terminalDiameter > MAX_TERMINAL_DIAMETER) ||
			 (expectedPathLength < MIN_EXPECTED_PATH_LENGTH) ||
														(expectedPathLength > MAX_EXPECTED_PATH_LENGTH) ||
			 (directionProbabilities == null) || directionProbabilities.isEmpty() ||
			 (directionMode == null) ||
			 (terminalEmphasis == null) ||
			 (transparencyColour == null) ||
			 (backgroundColour == null) ||
			 CollectionUtils.isNullOrEmpty(pathColours) ||
			 (activeFraction < Pattern2Image.MIN_ACTIVE_FRACTION) ||
													(activeFraction > Pattern2Image.MAX_ACTIVE_FRACTION) ||
			 (transitionIntervalRange.lowerBound < Pattern2Image.MIN_TRANSITION_INTERVAL) ||
			 (transitionIntervalRange.upperBound > Pattern2Image.MAX_TRANSITION_INTERVAL) ||
			 (transitionIntervalRange.lowerBound > transitionIntervalRange.upperBound) ||
			 ((seed != null) && ((seed < MIN_SEED) || (seed > MAX_SEED))))
			throw new IllegalArgumentException();

		// Set paramaters
		setWidth(width);
		setHeight(height);
		setOrientation(orientation);
		setEndMargin(endMargin);
		setSideMargin(sideMargin);
		setGridInterval(gridInterval);
		setPathThickness(pathThickness);
		setTerminalDiameter(terminalDiameter);
		setExpectedPathLength(expectedPathLength);
		setDirectionProbabilities(directionProbabilities);
		setDirectionMode(directionMode);
		setTerminalEmphasis(terminalEmphasis);
		setShowEmptyPaths(showEmptyPaths);
		setTransparencyColour(transparencyColour);
		setBackgroundColour(backgroundColour);
		setPathColours(pathColours);
		setActiveFraction(activeFraction);
		setTransitionIntervalRange(transitionIntervalRange);
		setSeed(seed);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public Pattern2Params clone()
	{
		List<Color> pathColours = new ArrayList<>();
		for (Color colour : getPathColours())
			pathColours.add(ColourUtils.copy(colour));
		Long seed = getSeed();
		if (seed != null)
			seed = seed.longValue();
		return new Pattern2Params(getWidth(), getHeight(), getOrientation(), getEndMargin(), getSideMargin(),
								  getGridInterval(), getPathThickness(), getTerminalDiameter(), getExpectedPathLength(),
								  getDirectionProbabilities(), getDirectionMode(), getTerminalEmphasis(),
								  isShowEmptyPaths(), getTransparencyColour(), getBackgroundColour(), pathColours,
								  getActiveFraction(), getTransitionIntervalRange().clone(), seed);
	}

	//------------------------------------------------------------------

	@Override
	public PatternKind getPatternKind()
	{
		return PatternKind.PATTERN2;
	}

	//------------------------------------------------------------------

	@Override
	protected List<Property> getProperties()
	{
		if (properties == null)
		{
			properties = new ArrayList<>();
			for (Field field : getClass().getDeclaredFields())
			{
				try
				{
					if (field.getName().startsWith(FIELD_PREFIX))
						properties.add((Property)field.get(this));
				}
				catch (IllegalAccessException e)
				{
					e.printStackTrace();
				}
			}
		}
		return properties;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		Map<Pattern2Image.Direction, Integer> probabilities = new EnumMap<>(Pattern2Image.Direction.class);
		probabilities.put(Pattern2Image.Direction.FORE,       34);
		probabilities.put(Pattern2Image.Direction.FORE_RIGHT, 33);
		probabilities.put(Pattern2Image.Direction.FORE_LEFT,  33);
		DEFAULT_PROBABILITIES = Collections.unmodifiableMap(probabilities);
	}

}

//----------------------------------------------------------------------
