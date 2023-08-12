/*====================================================================*\

PatternParams.java

Class: pattern parameters.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.io.File;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.FileException;
import uk.blankaspect.common.exception.UnexpectedRuntimeException;

import uk.blankaspect.common.property.Property;

import uk.blankaspect.common.tuple.StrKVPair;

//----------------------------------------------------------------------


// CLASS: PATTERN PARAMETERS


abstract class PatternParams
	implements Cloneable
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	protected static final	String	FIELD_PREFIX	= "pp";

	private static final	int		VERSION					= 0;
	private static final	int		MIN_SUPPORTED_VERSION	= 0;
	private static final	int		MAX_SUPPORTED_VERSION	= 0;

	private static final	String	NAMESPACE_PREFIX		= "http://ns.blankaspect.uk/patternGenerator-parameters/";
	private static final	String	NAMESPACE_PREFIX_REGEX	= "http://ns\\.[a-z.]+/patternGenerator-parameters/(\\w+)";

	private static final	String	PATTERN_GENERATOR_FILE_STR	= "Pattern-generator file";

	private interface ElementName
	{
		String	PATTERN_GENERATOR_PARAMETERS	= "patternGeneratorParameters";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	protected	List<Property>	properties;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	protected PatternParams()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static PatternParams read(
		File	file)
		throws AppException
	{
		// Read file
		PropertySet propertySet = new PropertySet();
		PatternKind patternKind = propertySet.read(file);

		// Instantiate parameters from property set
		PatternParams params = null;
		switch (patternKind)
		{
			case PATTERN1:
				params = new Pattern1Params();
				break;

			case PATTERN2:
				params = new Pattern2Params();
				break;
		}

		// Set parameters from properties
		params.getProperties(propertySet);

		return params;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Abstract methods
////////////////////////////////////////////////////////////////////////

	public abstract PatternKind getPatternKind();

	//------------------------------------------------------------------

	public abstract int getWidth();

	//------------------------------------------------------------------

	public abstract int getHeight();

	//------------------------------------------------------------------

	public abstract Long getSeed();

	//------------------------------------------------------------------

	public abstract void setWidth(
		int	width);

	//------------------------------------------------------------------

	public abstract void setHeight(
		int	height);

	//------------------------------------------------------------------

	public abstract void setSeed(
		Long	seed);

	//------------------------------------------------------------------

	protected abstract List<Property> getProperties();

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public PatternParams clone()
	{
		try
		{
			return (PatternParams)super.clone();
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
		PropertyList properties = new PropertyList();
		for (Property property : getProperties())
			property.put(properties);
		return properties.toString();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public void write(
		File	file)
		throws AppException
	{
		// Put parameters into property set
		PropertySet propertySet = new PropertySet(getPatternKind());
		for (Property property : getProperties())
			property.put(propertySet);

		// Write file
		propertySet.write(file, null);
	}

	//------------------------------------------------------------------

	protected void getProperties(
		Property.ISource...	propertySources)
	{
		for (Property property : getProperties())
		{
			try
			{
				property.get(propertySources);
			}
			catch (AppException e)
			{
				AppConfig.showWarningMessage(e);
			}
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		FILE_DOES_NOT_EXIST
		("The file does not exist."),

		UNEXPECTED_DOCUMENT_FORMAT
		("The document does not have the expected format."),

		NO_VERSION_NUMBER
		("The document does not have a version number."),

		INVALID_VERSION_NUMBER
		("The version number of the document is invalid."),

		UNSUPPORTED_DOCUMENT_VERSION
		("The version of the document (%1) is not supported by this version of " + App.SHORT_NAME + ".");

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


	// CLASS: PROPERTY SET


	private static class PropertySet
		extends uk.blankaspect.common.property.PropertySet
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PropertySet()
		{
		}

		//--------------------------------------------------------------

		private PropertySet(
			PatternKind	patternKind)
			throws AppException
		{
			super(ElementName.PATTERN_GENERATOR_PARAMETERS, NAMESPACE_PREFIX + patternKind.getKey(),
				  Integer.toString(VERSION));
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String getSourceName()
		{
			return PATTERN_GENERATOR_FILE_STR;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private PatternKind read(
			File	file)
			throws AppException
		{
			// Test for file
			if (!file.isFile())
				throw new FileException(ErrorId.FILE_DOES_NOT_EXIST, file);

			// Read file
			read(file, ElementName.PATTERN_GENERATOR_PARAMETERS);

			// Test namespace name
			PatternKind patternKind = null;
			String namespaceName = getNamespaceName();
			if (namespaceName != null)
			{
				Matcher matcher = Pattern.compile(NAMESPACE_PREFIX_REGEX).matcher(namespaceName);
				if (matcher.matches())
					patternKind = PatternKind.forKey(matcher.group(1));
			}
			if (patternKind == null)
				throw new FileException(ErrorId.UNEXPECTED_DOCUMENT_FORMAT, file);

			// Test version number
			String versionStr = getVersionString();
			if (versionStr == null)
				throw new FileException(ErrorId.NO_VERSION_NUMBER, file);
			try
			{
				int version = Integer.parseInt(versionStr);
				if (version < 0)
					throw new NumberFormatException();
				if ((version < MIN_SUPPORTED_VERSION) || (version > MAX_SUPPORTED_VERSION))
					throw new FileException(ErrorId.UNSUPPORTED_DOCUMENT_VERSION, file, versionStr);
			}
			catch (NumberFormatException e)
			{
				throw new FileException(ErrorId.INVALID_VERSION_NUMBER, file);
			}

			return patternKind;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: PROPERTY LIST


	private static class PropertyList
		implements Property.ITarget
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	List<StrKVPair>	properties;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PropertyList()
		{
			properties = new ArrayList<>();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Property.ITarget interface
	////////////////////////////////////////////////////////////////////

		@Override
		public boolean putProperty(
			String	key,
			String	value)
		{
			if (get(key) != null)
				return false;
			properties.add(StrKVPair.of(key, value));
			return true;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			StringBuilder buffer = new StringBuilder(1024);
			for (StrKVPair property : properties)
			{
				String value = property.value();
				if (!value.isEmpty())
				{
					buffer.append(property.key());
					buffer.append(" = ");
					buffer.append(value);
					buffer.append('\n');
				}
			}
			return buffer.toString();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private String get(
			String	key)
		{
			for (StrKVPair property : properties)
			{
				if (property.key().equals(key))
					return property.value();
			}
			return null;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
