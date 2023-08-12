/*====================================================================*\

ArgumentOutOfBoundsException.java

Class: 'argument out of bounds' exception.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.exception;

//----------------------------------------------------------------------


// CLASS: 'ARGUMENT OUT OF BOUNDS' EXCEPTION


public class ArgumentOutOfBoundsException
	extends IllegalArgumentException
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public ArgumentOutOfBoundsException()
	{
	}

	//------------------------------------------------------------------

	public ArgumentOutOfBoundsException(
		String	message)
	{
		// Call superclass constructor
		super(message);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
