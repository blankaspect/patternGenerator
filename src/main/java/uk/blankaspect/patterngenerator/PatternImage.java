/*====================================================================*\

PatternImage.java

Pattern image base class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.image.BufferedImage;

//----------------------------------------------------------------------


// PATTERN IMAGE BASE CLASS


abstract class PatternImage
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	protected PatternImage()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static BufferedImage copyImage(BufferedImage image)
	{
		int width = image.getWidth();
		int height = image.getHeight();
		BufferedImage copy = new BufferedImage(width, height, image.getType());
		int[] rgbData = image.getRGB(0, 0, width, height, null, 0, width);
		copy.setRGB(0, 0, width, height, rgbData, 0, width);
		return copy;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Abstract methods
////////////////////////////////////////////////////////////////////////

	public abstract BufferedImage getImage();

	//------------------------------------------------------------------

	public abstract BufferedImage getExportImage()
		throws InterruptedException;

	//------------------------------------------------------------------

	public abstract void renderImage()
		throws InterruptedException;

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public String getDescription()
	{
		return description;
	}

	//------------------------------------------------------------------

	public int getWidth()
	{
		return width;
	}

	//------------------------------------------------------------------

	public int getHeight()
	{
		return height;
	}

	//------------------------------------------------------------------

	public void setDescription(String description)
	{
		this.description = description;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	protected	String	description;
	protected	int		width;
	protected	int		height;

}

//----------------------------------------------------------------------
