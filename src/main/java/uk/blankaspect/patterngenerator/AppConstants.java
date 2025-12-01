/*====================================================================*\

AppConstants.java

Interface: application constants.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Insets;

import java.text.DecimalFormat;

import uk.blankaspect.common.misc.FilenameSuffixFilter;

//----------------------------------------------------------------------


// INTERFACE: APPLICATION CONSTANTS


interface AppConstants
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	// Component constants
	Insets	COMPONENT_INSETS	= new Insets(2, 3, 2, 3);

	// Decimal formats
	DecimalFormat	FORMAT_1_1F	= new DecimalFormat("0.0");
	DecimalFormat	FORMAT_1_2	= new DecimalFormat("0.0#");
	DecimalFormat	FORMAT_1_2F	= new DecimalFormat("0.00");
	DecimalFormat	FORMAT_1_3	= new DecimalFormat("0.0##");
	DecimalFormat	FORMAT_1_3F	= new DecimalFormat("0.000");
	DecimalFormat	FORMAT_1_8	= new DecimalFormat("0.0#######");

	// Strings
	String	ELLIPSIS_STR		= "...";
	String	FILE_CHANGED_SUFFIX	= " *";
	String	OK_STR				= "OK";
	String	CANCEL_STR			= "Cancel";
	String	CLOSE_STR			= "Close";
	String	CONTINUE_STR		= "Continue";
	String	REPLACE_STR			= "Replace";
	String	CLEAR_STR			= "Clear";
	String	ALREADY_EXISTS_STR	= "\nThe file already exists.\nDo you want to replace it?";
	String	XML_VERSION_STR		= "1.0";

	// Filename extensions
	String	PNG_FILENAME_EXTENSION	= ".png";
	String	SVG_FILENAME_EXTENSION	= ".svg";
	String	XML_FILENAME_EXTENSION	= ".xml";

	// Filename suffixes
	String	PG_DEF_FILENAME_SUFFIX	= ".pgdef.xml";
	String	PG_PAR_FILENAME_SUFFIX	= ".pgpar.xml";

	// Filters for file choosers
	FilenameSuffixFilter PNG_FILE_FILTER	=
			new FilenameSuffixFilter("PNG files", PNG_FILENAME_EXTENSION);
	FilenameSuffixFilter SVG_FILE_FILTER	=
			new FilenameSuffixFilter("SVG files", SVG_FILENAME_EXTENSION);
	FilenameSuffixFilter XML_FILE_FILTER	=
			new FilenameSuffixFilter("XML files", XML_FILENAME_EXTENSION);

}

//----------------------------------------------------------------------
