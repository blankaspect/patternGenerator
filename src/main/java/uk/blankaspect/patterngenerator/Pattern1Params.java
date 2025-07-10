/*====================================================================*\

Pattern1Params.java

Pattern 1 parameters class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.List;

import uk.blankaspect.common.collection.CollectionUtils;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.ArgumentOutOfBoundsException;

import uk.blankaspect.common.property.Property;

import uk.blankaspect.common.range.DoubleRange;
import uk.blankaspect.common.range.IntegerRange;

//----------------------------------------------------------------------


// PATTERN 1 PARAMETERS CLASS


class Pattern1Params
	extends PatternParams
	implements Cloneable
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final	int	MIN_WIDTH		= 1 << 5;   // 32
	public static final	int	MAX_WIDTH		= 1 << 13;  // 8192
	public static final	int	DEFAULT_WIDTH	= 320;

	public static final	int	MIN_HEIGHT		= 1 << 5;   // 32
	public static final	int	MAX_HEIGHT		= 1 << 13;  // 8192
	public static final	int	DEFAULT_HEIGHT	= 240;

	public static final	double		MIN_WAVELENGTH				= 0.01;
	public static final	double		MAX_WAVELENGTH				= 4.0;
	public static final	DoubleRange	DEFAULT_WAVELENGTH_RANGE	= new DoubleRange(0.06, 0.18);

	public static final	Pattern1Image.Symmetry	DEFAULT_SYMMETRY	= Pattern1Image.Symmetry.NONE;

	public static final	int	MIN_NUM_SOURCES		= 1;
	public static final	int	MAX_NUM_SOURCES		= 16;
	public static final	int	DEFAULT_NUM_SOURCES	= 4;

	public static final	Pattern1Image.SourceParams	DEFAULT_SOURCE	=
							new Pattern1Image.SourceParams(Pattern1Image.Source.Shape.CIRCLE, null,
														   Pattern1Image.Source.Waveform.COSINE,
														   Pattern1Image.Source.Waveform.COSINE.getDefaultCoefficient(),
														   Pattern1Image.Source.DEFAULT_ATTENUATION_COEFFICIENT,
														   Pattern1Image.Source.Constraint.NONE,
														   Pattern1Image.Source.DEFAULT_HUE,
														   Pattern1Image.Source.DEFAULT_SATURATION);

	public static final	Pattern1Image.SaturationMode	DEFAULT_SATURATION_MODE	= Pattern1Image.SaturationMode.VARIABLE;

	public static final	long	MIN_SEED	= PatternGeneratorApp.MIN_SEED;
	public static final	long	MAX_SEED	= PatternGeneratorApp.MAX_SEED;

	public static final	double	MIN_ROTATION_PERIOD	= 5.0;
	public static final	double	MAX_ROTATION_PERIOD	= 9000.0;

	public static final	DoubleRange	DEFAULT_ROTATION_PERIOD_RANGE	= new DoubleRange(200.0, 400.0);

	public static final	Pattern1Image.RotationSense	DEFAULT_ROTATION_SENSE	= Pattern1Image.RotationSense.ANY;

	private interface Key
	{
		String	DESCRIPTION				= "description";
		String	HEIGHT					= "height";
		String	MOTION_RATE_ENVELOPE	= "motionRateEnvelope";
		String	MOTION_RATE_RANGE		= "motionRateRange";
		String	PHASE_INCREMENT_RANGE	= "phaseIncrementRange";
		String	ROTATION_PERIOD_RANGE	= "rotationPeriodRange";
		String	ROTATION_SENSE			= "rotationSense";
		String	SATURATION_MODE			= "saturationMode";
		String	SEED					= "seed";
		String	SOURCE					= "source";
		String	SYMMETRY				= "symmetry";
		String	WAVELENGTH_RANGE		= "wavelengthRange";
		String	WIDTH					= "width";
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
//--//  Instance variables : associated variables in enclosing class
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
//--//  Instance variables : associated variables in enclosing class
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
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPHeight	ppHeight	= new PPHeight();

	//==================================================================


	// PROPERTY CLASS: SYMMETRY


	private class PPSymmetry
		extends Property.EnumProperty<Pattern1Image.Symmetry>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPSymmetry()
		{
			super(Key.SYMMETRY, Pattern1Image.Symmetry.class);
			value = DEFAULT_SYMMETRY;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Pattern1Image.Symmetry getSymmetry()
	{
		return ppSymmetry.getValue();
	}

	//------------------------------------------------------------------

	public void setSymmetry(Pattern1Image.Symmetry value)
	{
		ppSymmetry.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPSymmetry	ppSymmetry	= new PPSymmetry();

	//==================================================================


	// PROPERTY CLASS: SATURATION MODE


	private class PPSaturationMode
		extends Property.EnumProperty<Pattern1Image.SaturationMode>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPSaturationMode()
		{
			super(Key.SATURATION_MODE, Pattern1Image.SaturationMode.class);
			value = DEFAULT_SATURATION_MODE;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Pattern1Image.SaturationMode getSaturationMode()
	{
		return ppSaturationMode.getValue();
	}

	//------------------------------------------------------------------

	public void setSaturationMode(Pattern1Image.SaturationMode value)
	{
		ppSaturationMode.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPSaturationMode	ppSaturationMode	= new PPSaturationMode();

	//==================================================================


	// PROPERTY CLASS: WAVELENGTH RANGE


	private class PPWavelengthRange
		extends Property.SimpleProperty<DoubleRange>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPWavelengthRange()
		{
			super(Key.WAVELENGTH_RANGE);
			value = DEFAULT_WAVELENGTH_RANGE;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input input)
			throws AppException
		{
			DoubleRange range = new DoubleRange(MIN_WAVELENGTH, MAX_WAVELENGTH);
			double[] outValues = input.parseDoubles(2, new DoubleRange[] { range, range },
													Order.GREATER_THAN_OR_EQUAL_TO);
			value = new DoubleRange(outValues[0], outValues[1]);
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return (AppConstants.FORMAT_1_8.format(value.lowerBound) + ", " +
													AppConstants.FORMAT_1_8.format(value.upperBound));
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public DoubleRange getWavelengthRange()
	{
		return ppWavelengthRange.getValue();
	}

	//------------------------------------------------------------------

	public void setWavelengthRange(DoubleRange value)
	{
		ppWavelengthRange.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPWavelengthRange	ppWavelengthRange	= new PPWavelengthRange();

	//==================================================================


	// PROPERTY CLASS: SOURCES


	private class PPSources
		extends Property.PropertyList<Pattern1Image.SourceParams>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPSources()
		{
			super(Key.SOURCE, MAX_NUM_SOURCES);
			values.add(DEFAULT_SOURCE);
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
				try
				{
					Pattern1Image.SourceParams source = new Pattern1Image.SourceParams(input.getValue());
					if (index < values.size())
						values.set(index, source);
					else
					{
						for (int i = values.size(); i < index; i++)
							values.add(DEFAULT_SOURCE);
						values.add(source);
					}
				}
				catch (ArgumentOutOfBoundsException e)
				{
					throw new ValueOutOfBoundsException(input);
				}
				catch (IllegalArgumentException e)
				{
					throw new IllegalValueException(input);
				}
			}
			catch (AppException e)
			{
				PatternGeneratorApp.INSTANCE.showWarningMessage(PatternGeneratorApp.SHORT_NAME, e);
			}
		}

		//--------------------------------------------------------------

		@Override
		protected String toString(int index)
		{
			return values.get(index).toString();
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Pattern1Image.SourceParams getSource(int index)
	{
		return ppSources.getValue(index);
	}

	//------------------------------------------------------------------

	public List<Pattern1Image.SourceParams> getSources()
	{
		return ppSources.getValues();
	}

	//------------------------------------------------------------------

	public void setSources(List<Pattern1Image.SourceParams> values)
	{
		ppSources.setValues(values);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPSources	ppSources	= new PPSources();

	//==================================================================


	// PROPERTY CLASS: MOTION RATE RANGE


	private class PPMotionRateRange
		extends Property.SimpleProperty<DoubleRange>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPMotionRateRange()
		{
			super(Key.MOTION_RATE_RANGE);
			value = Pattern1Image.DEFAULT_MOTION_RATE_RANGE;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input input)
			throws AppException
		{
			DoubleRange range = new DoubleRange(Pattern1Image.MIN_MOTION_RATE,
												Pattern1Image.MAX_MOTION_RATE);
			double[] outValues = input.parseDoubles(2, new DoubleRange[] { range, range },
													Order.GREATER_THAN_OR_EQUAL_TO);
			value = new DoubleRange(outValues[0], outValues[1]);
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return (AppConstants.FORMAT_1_8.format(value.lowerBound) + ", " +
													AppConstants.FORMAT_1_8.format(value.upperBound));
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public DoubleRange getMotionRateRange()
	{
		return ppMotionRateRange.getValue();
	}

	//------------------------------------------------------------------

	public void setMotionRateRange(DoubleRange value)
	{
		ppMotionRateRange.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPMotionRateRange	ppMotionRateRange	= new PPMotionRateRange();

	//==================================================================


	// PROPERTY CLASS: MOTION RATE ENVELOPE


	private class PPMotionRateEnvelope
		extends Property.SimpleProperty<MotionRateEnvelope>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPMotionRateEnvelope()
		{
			super(Key.MOTION_RATE_ENVELOPE);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input input)
			throws AppException
		{
			try
			{
				value = new MotionRateEnvelope(input.getValue());
			}
			catch (IllegalArgumentException e)
			{
				throw new IllegalValueException(input);
			}
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return ((value == null) ? null : value.toString());
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isMotionRateEnvelope()
	{
		return (ppMotionRateEnvelope.getValue() != null);
	}

	//------------------------------------------------------------------

	public MotionRateEnvelope getMotionRateEnvelope()
	{
		return ppMotionRateEnvelope.getValue();
	}

	//------------------------------------------------------------------

	public void setMotionRateEnvelope(MotionRateEnvelope value)
	{
		ppMotionRateEnvelope.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPMotionRateEnvelope	ppMotionRateEnvelope	= new PPMotionRateEnvelope();

	//==================================================================


	// PROPERTY CLASS: PHASE INCREMENT RANGE


	private class PPPhaseIncrementRange
		extends Property.SimpleProperty<IntegerRange>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPPhaseIncrementRange()
		{
			super(Key.PHASE_INCREMENT_RANGE);
			value = Pattern1Image.DEFAULT_PHASE_INCREMENT_RANGE;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input input)
			throws AppException
		{
			IntegerRange range = new IntegerRange(Pattern1Image.MIN_PHASE_INCREMENT,
												  Pattern1Image.MAX_PHASE_INCREMENT);
			int[] outValues = input.parseIntegers(2, new IntegerRange[] { range, range },
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

	public IntegerRange getPhaseIncrementRange()
	{
		return ppPhaseIncrementRange.getValue();
	}

	//------------------------------------------------------------------

	public void setPhaseIncrementRange(IntegerRange value)
	{
		ppPhaseIncrementRange.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPPhaseIncrementRange	ppPhaseIncrementRange	= new PPPhaseIncrementRange();

	//==================================================================


	// PROPERTY CLASS: ROTATION PERIOD RANGE


	private class PPRotationPeriodRange
		extends Property.SimpleProperty<DoubleRange>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPRotationPeriodRange()
		{
			super(Key.ROTATION_PERIOD_RANGE);
			value = DEFAULT_ROTATION_PERIOD_RANGE;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input input)
			throws AppException
		{
			DoubleRange range = new DoubleRange(MIN_ROTATION_PERIOD, MAX_ROTATION_PERIOD);
			double[] outValues = input.parseDoubles(2, new DoubleRange[] { range, range },
													Order.GREATER_THAN_OR_EQUAL_TO);
			value = new DoubleRange(outValues[0], outValues[1]);
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return (AppConstants.FORMAT_1_8.format(value.lowerBound) + ", " +
													AppConstants.FORMAT_1_8.format(value.upperBound));
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public DoubleRange getRotationPeriodRange()
	{
		return ppRotationPeriodRange.getValue();
	}

	//------------------------------------------------------------------

	public void setRotationPeriodRange(DoubleRange value)
	{
		ppRotationPeriodRange.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPRotationPeriodRange	ppRotationPeriodRange	= new PPRotationPeriodRange();

	//==================================================================


	// PROPERTY CLASS: ROTATION SENSE


	private class PPRotationSense
		extends Property.EnumProperty<Pattern1Image.RotationSense>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PPRotationSense()
		{
			super(Key.ROTATION_SENSE, Pattern1Image.RotationSense.class);
			value = DEFAULT_ROTATION_SENSE;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Pattern1Image.RotationSense getRotationSense()
	{
		return ppRotationSense.getValue();
	}

	//------------------------------------------------------------------

	public void setRotationSense(Pattern1Image.RotationSense value)
	{
		ppRotationSense.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPRotationSense	ppRotationSense	= new PPRotationSense();

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
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	PPSeed	ppSeed	= new PPSeed();

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public Pattern1Params()
	{
	}

	//------------------------------------------------------------------

	public Pattern1Params(int                              width,
						  int                              height,
						  Pattern1Image.Symmetry           symmetry,
						  Pattern1Image.SaturationMode     saturationMode,
						  DoubleRange                      wavelengthRange,
						  List<Pattern1Image.SourceParams> sources,
						  DoubleRange                      motionRateRange,
						  MotionRateEnvelope               motionRateEnvelope,
						  IntegerRange                     phaseIncrementRange,
						  DoubleRange                      rotationPeriodRange,
						  Pattern1Image.RotationSense      rotationSense,
						  Long                             seed)
	{
		// Validate parameters
		if ((width < MIN_WIDTH) || (width > MAX_WIDTH) ||
			 (height < MIN_HEIGHT) || (height > MAX_HEIGHT) ||
			 (symmetry == null) ||
			 (saturationMode == null) ||
			 (wavelengthRange.lowerBound < MIN_WAVELENGTH) ||
			 (wavelengthRange.upperBound > MAX_WAVELENGTH) ||
			 (wavelengthRange.lowerBound > wavelengthRange.upperBound) ||
			 CollectionUtils.isNullOrEmpty(sources) ||
			 (motionRateRange.lowerBound < Pattern1Image.MIN_MOTION_RATE) ||
			 (motionRateRange.upperBound > Pattern1Image.MAX_MOTION_RATE) ||
			 (motionRateRange.lowerBound > motionRateRange.upperBound) ||
			 (phaseIncrementRange.lowerBound < Pattern1Image.MIN_PHASE_INCREMENT) ||
			 (phaseIncrementRange.upperBound > Pattern1Image.MAX_PHASE_INCREMENT) ||
			 (phaseIncrementRange.lowerBound > phaseIncrementRange.upperBound) ||
			 (rotationPeriodRange.lowerBound < MIN_ROTATION_PERIOD) ||
			 (rotationPeriodRange.upperBound > MAX_ROTATION_PERIOD) ||
			 (rotationPeriodRange.lowerBound > rotationPeriodRange.upperBound) ||
			 (rotationSense == null) ||
			 ((seed != null) && ((seed < MIN_SEED) || (seed > MAX_SEED))))
			throw new IllegalArgumentException();

		// Set parameters
		setWidth(width);
		setHeight(height);
		setSymmetry(symmetry);
		setSaturationMode(saturationMode);
		setWavelengthRange(wavelengthRange);
		setSources(sources);
		setMotionRateRange(motionRateRange);
		setMotionRateEnvelope(motionRateEnvelope);
		setPhaseIncrementRange(phaseIncrementRange);
		setRotationPeriodRange(rotationPeriodRange);
		setRotationSense(rotationSense);
		setSeed(seed);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public Pattern1Params clone()
	{
		List<Pattern1Image.SourceParams> sources = new ArrayList<>();
		for (Pattern1Image.SourceParams source : getSources())
			sources.add(source.clone());
		MotionRateEnvelope motionRateEnvelope = getMotionRateEnvelope();
		if (motionRateEnvelope != null)
			motionRateEnvelope = motionRateEnvelope.clone();
		Long seed = getSeed();
		if (seed != null)
			seed = seed.longValue();
		return new Pattern1Params(getWidth(), getHeight(), getSymmetry(), getSaturationMode(),
								  getWavelengthRange().clone(), sources, getMotionRateRange().clone(),
								  motionRateEnvelope, getPhaseIncrementRange().clone(),
								  getRotationPeriodRange().clone(), getRotationSense(), seed);
	}

	//------------------------------------------------------------------

	@Override
	public PatternKind getPatternKind()
	{
		return PatternKind.PATTERN1;
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

}

//----------------------------------------------------------------------
