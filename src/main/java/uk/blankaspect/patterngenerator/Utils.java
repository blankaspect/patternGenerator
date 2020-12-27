/*====================================================================*\

Utils.java

Utility methods class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.io.File;
import java.io.IOException;

import uk.blankaspect.common.config.PropertiesPathname;

import uk.blankaspect.common.exception2.ExceptionUtils;

import uk.blankaspect.common.filesystem.PathnameUtils;

//----------------------------------------------------------------------


// UTILITY METHODS CLASS


class Utils
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	FAILED_TO_GET_PATHNAME_STR	= "Failed to get the canonical pathname for ";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private Utils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static int indexOf(Object   target,
							  Object[] values)
	{
		for (int i = 0; i < values.length; i++)
		{
			if (values[i].equals(target))
				return i;
		}
		return -1;
	}

	//------------------------------------------------------------------

	public static char getFileSeparatorChar()
	{
		return (AppConfig.INSTANCE.isShowUnixPathnames() ? '/' : File.separatorChar);
	}

	//------------------------------------------------------------------

	public static String getPathname(File file)
	{
		return getPathname(file, AppConfig.INSTANCE.isShowUnixPathnames());
	}

	//------------------------------------------------------------------

	public static String getPathname(File    file,
									 boolean unixStyle)
	{
		String pathname = null;
		if (file != null)
		{
			try
			{
				pathname = file.getCanonicalPath();
			}
			catch (Exception e)
			{
				ExceptionUtils.printStderrLocated(FAILED_TO_GET_PATHNAME_STR + file.getPath());
				System.err.println("- " + e);
				pathname = file.getAbsolutePath();
			}

			if (unixStyle)
				pathname = PathnameUtils.toUnixStyle(pathname, true);
		}
		return pathname;
	}

	//------------------------------------------------------------------

	public static String getPropertiesPathname()
	{
		String pathname = PropertiesPathname.getPathname();
		if (pathname != null)
			pathname += App.NAME_KEY;
		return pathname;
	}

	//------------------------------------------------------------------

	public static boolean isSameFile(File file1,
									 File file2)
	{
		try
		{
			if (file1 == null)
				return (file2 == null);
			return ((file2 != null) && file1.getCanonicalPath().equals(file2.getCanonicalPath()));
		}
		catch (IOException e)
		{
			return false;
		}
	}

	//------------------------------------------------------------------

	public static File appendSuffix(File   file,
									String suffix)
	{
		String filename = file.getName();
		if (!filename.isEmpty() && (filename.indexOf('.') < 0))
			file = new File(file.getParentFile(), filename + suffix);
		return file;
	}

	//------------------------------------------------------------------

	public static String[] getOptionStrings(String... optionStrs)
	{
		String[] strs = new String[optionStrs.length + 1];
		System.arraycopy(optionStrs, 0, strs, 0, optionStrs.length);
		strs[optionStrs.length] = AppConstants.CANCEL_STR;
		return strs;
	}

	//------------------------------------------------------------------

	public static void hsToRgb(double   hue,
							   double   saturation,
							   double[] rgbValues)
	{
		if (saturation == 0.0)
		{
			rgbValues[0] = 1.0;
			rgbValues[1] = 1.0;
			rgbValues[2] = 1.0;
		}
		else
		{
			double h = (hue - Math.floor(hue)) * 6.0;
			double f = h - Math.floor(h);
			double a = 1.0 - saturation;
			double b = 1.0 - saturation * f;
			double c = 1.0 - (saturation * (1.0 - f));
			switch ((int)h)
			{
				case 0:
					rgbValues[0] = 1.0;
					rgbValues[1] = c;
					rgbValues[2] = a;
					break;

				case 1:
					rgbValues[0] = b;
					rgbValues[1] = 1.0;
					rgbValues[2] = a;
					break;

				case 2:
					rgbValues[0] = a;
					rgbValues[1] = 1.0;
					rgbValues[2] = c;
					break;

				case 3:
					rgbValues[0] = a;
					rgbValues[1] = b;
					rgbValues[2] = 1.0;
					break;

				case 4:
					rgbValues[0] = c;
					rgbValues[1] = a;
					rgbValues[2] = 1.0;
					break;

				case 5:
					rgbValues[0] = 1.0;
					rgbValues[1] = a;
					rgbValues[2] = b;
					break;
			}
		}
	}

	//------------------------------------------------------------------

	public static int hsToRgb(double hue,
							  double saturation)
	{
		double[] rgbValues = new double[3];
		hsToRgb(hue, saturation, rgbValues);
		int rgb = 0;
		for (int i = 0; i < rgbValues.length; i++)
		{
			rgb <<= 8;
			rgb |= (int)(rgbValues[i] * 255.0 + 0.5);
		}
		return rgb;
	}

	//------------------------------------------------------------------

	public static void rgbToHs(double[] rgbValues,
							   double[] hsValues)
	{
		double cMin = (rgbValues[0] < rgbValues[1]) ? rgbValues[0] : rgbValues[1];
		if (cMin > rgbValues[2])
			cMin = rgbValues[2];

		double cMax = (rgbValues[0] > rgbValues[1]) ? rgbValues[0] : rgbValues[1];
		if (cMax < rgbValues[2])
			cMax = rgbValues[2];

		double hue = 0.0;
		double saturation = 0.0;
		double range = cMax - cMin;
		if (cMax != 0.0)
			saturation = range / cMax;
		if (saturation != 0.0)
		{
			double factor = 1.0 / range;
			double red = (cMax - rgbValues[0]) * factor;
			double green = (cMax - rgbValues[1]) * factor;
			double blue = (cMax - rgbValues[2]) * factor;
			if (rgbValues[0] == cMax)
				hue = blue - green;
			else if (rgbValues[1] == cMax)
				hue = 2.0 + red - blue;
			else
				hue = 4.0 + green - red;
			hue /= 6.0;
			if (hue < 0.0)
				hue += 1.0;
		}
		hsValues[0] = hue;
		hsValues[1] = saturation;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
