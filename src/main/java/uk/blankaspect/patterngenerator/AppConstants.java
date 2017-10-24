/*====================================================================*\

AppConstants.java

Application constants interface.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Insets;

import java.text.DecimalFormat;

//----------------------------------------------------------------------


// APPLICATION CONSTANTS INTERFACE


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

	// Temporary-file prefix
	String	TEMP_FILE_PREFIX	= "_$_";

	// Filename suffixes
	String	PG_DEF_FILE_SUFFIX	= ".pgdef.xml";
	String	PG_PAR_FILE_SUFFIX	= ".pgpar.xml";
	String	PNG_FILE_SUFFIX		= ".png";
	String	SVG_FILE_SUFFIX		= ".svg";
	String	XML_FILE_SUFFIX		= ".xml";

	// File-filter descriptions
	String	PG_DEF_FILES_STR	= "Pattern definition files";
	String	PG_PAR_FILES_STR	= "Pattern parameter files";
	String	PNG_FILES_STR		= "PNG files";
	String	SVG_FILES_STR		= "SVG files";
	String	XML_FILES_STR		= "XML files";

}

//----------------------------------------------------------------------
