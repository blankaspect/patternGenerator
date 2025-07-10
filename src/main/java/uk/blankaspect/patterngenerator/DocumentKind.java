/*====================================================================*\

DocumentKind.java

Document kind enumeration.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import uk.blankaspect.common.misc.FilenameSuffixFilter;
import uk.blankaspect.common.misc.IStringKeyed;

import uk.blankaspect.common.string.StringUtils;

//----------------------------------------------------------------------


// DOCUMENT KIND ENUMERATION


enum DocumentKind
	implements IStringKeyed
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	DEFINITION
	(
		"definition",
		AppConstants.PG_DEF_FILENAME_SUFFIX,
		AppConstants.PG_DEF_FILES_STR
	),

	PARAMETERS
	(
		"parameters",
		AppConstants.PG_PAR_FILENAME_SUFFIX,
		AppConstants.PG_PAR_FILES_STR
	);

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private DocumentKind(String key,
						 String suffix,
						 String description)
	{
		this.key = key;
		filter = new FilenameSuffixFilter(description, suffix);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static DocumentKind forKey(String key)
	{
		for (DocumentKind value : values())
		{
			if (value.key.equals(key))
				return value;
		}
		return null;
	}

	//------------------------------------------------------------------

	public static DocumentKind forDescription(String description)
	{
		for (DocumentKind value : values())
		{
			if (value.filter.getDescription().equals(description))
				return value;
		}
		return null;
	}

	//------------------------------------------------------------------

	public static DocumentKind forFilename(String filename)
	{
		if (filename != null)
		{
			for (DocumentKind value : values())
			{
				if (filename.endsWith(value.filter.getSuffix(0)))
					return value;
			}
		}
		return null;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IStringKeyed interface
////////////////////////////////////////////////////////////////////////

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

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String					key;
	private	FilenameSuffixFilter	filter;

}

//----------------------------------------------------------------------
