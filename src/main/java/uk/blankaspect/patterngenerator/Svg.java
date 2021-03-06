/*====================================================================*\

Svg.java

SVG constants interface.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// SVG CONSTANTS INTERFACE


interface Svg
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	interface ElementName
	{
		String	CIRCLE		= "circle";
		String	CLIP_PATH	= "clipPath";
		String	DEFS		= "defs";
		String	DESC		= "desc";
		String	G			= "g";
		String	PATH		= "path";
		String	RECT		= "rect";
		String	STYLE		= "style";
		String	SVG			= "svg";
	}

	interface AttrName
	{
		String	CLIP_PATH		= "clip-path";
		String	CX				= "cx";
		String	CY				= "cy";
		String	D				= "d";
		String	FILL			= "fill";
		String	FILL_OPACITY	= "fill-opacity";
		String	HEIGHT			= "height";
		String	ID				= "id";
		String	R				= "r";
		String	STROKE			= "stroke";
		String	STROKE_OPACITY	= "stroke-opacity";
		String	TYPE			= "type";
		String	VERSION			= "version";
		String	WIDTH			= "width";
		String	XMLNS			= "xmlns";
	}

}

//----------------------------------------------------------------------
