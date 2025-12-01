/*====================================================================*\

DocumentKind.java

Enumeration: document kind.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.util.Arrays;

import uk.blankaspect.common.misc.FilenameSuffixFilter;
import uk.blankaspect.common.misc.IStringKeyed;

import uk.blankaspect.common.string.StringUtils;

//----------------------------------------------------------------------


// ENUMERATION: DOCUMENT KIND


enum DocumentKind
	implements IStringKeyed
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	DEFINITION
	(
		"definition",
		new FilenameSuffixFilter("Pattern-definition files", AppConstants.PG_DEF_FILENAME_SUFFIX)
	),

	PARAMETERS
	(
		"parameters",
		new FilenameSuffixFilter("Pattern-parameter files", AppConstants.PG_PAR_FILENAME_SUFFIX)
	);

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String					key;
	private	FilenameSuffixFilter	filter;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private DocumentKind(
		String					key,
		FilenameSuffixFilter	filter)
	{
		this.key = key;
		this.filter = filter;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static DocumentKind forKey(
		String	key)
	{
		return Arrays.stream(values()).filter(value -> value.key.equals(key)).findFirst().orElse(null);
	}

	//------------------------------------------------------------------

	public static DocumentKind forDescription(
		String	description)
	{
		return Arrays.stream(values())
				.filter(value -> value.filter.getDescription().equals(description)).findFirst().orElse(null);
	}

	//------------------------------------------------------------------

	public static DocumentKind forFilename(
		String	filename)
	{
		return Arrays.stream(values())
				.filter(value -> filename.endsWith(value.filter.getSuffix(0))).findFirst().orElse(null);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IStringKeyed interface
////////////////////////////////////////////////////////////////////////

	@Override
	public String getKey()
	{
		return key;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public String toString()
	{
		return StringUtils.firstCharToUpperCase(key);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public FilenameSuffixFilter getFilter()
	{
		return filter;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
