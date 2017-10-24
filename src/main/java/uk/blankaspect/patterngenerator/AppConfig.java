/*====================================================================*\

AppConfig.java

Application configuration class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.FileException;

import uk.blankaspect.common.gui.FontEx;
import uk.blankaspect.common.gui.IProgressView;
import uk.blankaspect.common.gui.TextRendering;

import uk.blankaspect.common.misc.FilenameSuffixFilter;
import uk.blankaspect.common.misc.IntegerRange;
import uk.blankaspect.common.misc.Property;
import uk.blankaspect.common.misc.PropertySet;
import uk.blankaspect.common.misc.PropertyString;

//----------------------------------------------------------------------


// APPLICATION CONFIGURATION CLASS


class AppConfig
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		AppConfig	INSTANCE;

	private static final	int	VERSION					= 0;
	private static final	int	MIN_SUPPORTED_VERSION	= 0;
	private static final	int	MAX_SUPPORTED_VERSION	= 0;

	private static final	String	CONFIG_ERROR_STR	= "Configuration error";
	private static final	String	CONFIG_DIR_KEY		= Property.APP_PREFIX + "configDir";
	private static final	String	PROPERTIES_FILENAME	= App.NAME_KEY + "-properties" +
																			AppConstants.XML_FILE_SUFFIX;
	private static final	String	FILENAME_BASE		= App.NAME_KEY + "-config";
	private static final	String	CONFIG_FILENAME		= FILENAME_BASE + AppConstants.XML_FILE_SUFFIX;
	private static final	String	CONFIG_OLD_FILENAME	= FILENAME_BASE + "-old" +
																			AppConstants.XML_FILE_SUFFIX;

	private static final	String	SAVE_CONFIGURATION_FILE_STR	= "Save configuration file";
	private static final	String	WRITING_STR					= "Writing";

	private interface Key
	{
		String	ANIMATION						= "animation";
		String	APPEARANCE						= "appearance";
		String	CLEAR_EDIT_LIST_ON_SAVE			= "clearEditListOnSave";
		String	CONFIGURATION					= App.NAME_KEY + "Configuration";
		String	DEFAULT_DOCUMENT_KIND			= "defaultDocumentKind";
		String	DEFAULT_SIZE					= "defaultSize";
		String	EXPORT_IMAGE_DIRECTORY			= "exportImageDirectory";
		String	EXPORT_IMAGE_SEQUENCE_DIRECTORY	= "exportImageSequenceDirectory";
		String	EXPORT_SVG_DIRECTORY			= "exportSvgDirectory";
		String	FONT							= "font";
		String	GENERAL							= "general";
		String	INTERVAL						= "interval";
		String	KEEP_SEQUENCE_WINDOW_ON_TOP		= "keepSequenceWindowOnTop";
		String	LOOK_AND_FEEL					= "lookAndFeel";
		String	MAIN_WINDOW_LOCATION			= "mainWindowLocation";
		String	MAIN_WINDOW_SIZE				= "mainWindowSize";
		String	MAX_EDIT_LIST_LENGTH			= "maxEditListLength";
		String	NAME							= "name";
		String	NUM_RENDERING_THREADS			= "numRenderingThreads";
		String	NUM_SLIDE_SHOW_THREADS			= "numSlideShowThreads";
		String	OPEN_PATTERN_DIRECTORY			= "openPatternDirectory";
		String	PATH							= "path";
		String	PATH_RENDERING					= "pathRendering";
		String	PATTERN							= "pattern";
		String	PHASE_ANIMATION					= "phaseAnimation";
		String	RATE							= "rate";
		String	SAVE_PATTERN_DIRECTORY			= "savePatternDirectory";
		String	SELECT_TEXT_ON_FOCUS_GAINED		= "selectTextOnFocusGained";
		String	SHOW_FULL_PATHNAMES				= "showFullPathnames";
		String	SHOW_UNIX_PATHNAMES				= "showUnixPathnames";
		String	SLIDE_SHOW						= "slideShow";
		String	TEXT_ANTIALIASING				= "textAntialiasing";
	}

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		ERROR_READING_PROPERTIES_FILE
		("An error occurred when reading the properties file."),

		NO_CONFIGURATION_FILE
		("No configuration file was found at the specified location."),

		NO_VERSION_NUMBER
		("The configuration file does not have a version number."),

		INVALID_VERSION_NUMBER
		("The version number of the configuration file is invalid."),

		UNSUPPORTED_CONFIGURATION_FILE
		("The version of the configuration file (%1) is not supported by this version of " +
			App.SHORT_NAME + "."),

		FAILED_TO_CREATE_DIRECTORY
		("Failed to create the directory for the configuration file.");

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ErrorId(String message)
		{
			this.message = message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : AppException.IId interface
	////////////////////////////////////////////////////////////////////

		public String getMessage()
		{
			return message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	String	message;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CONFIGURATION FILE CLASS


	private static class ConfigFile
		extends PropertySet
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	CONFIG_FILE1_STR	= "configuration file";
		private static final	String	CONFIG_FILE2_STR	= "Configuration file";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ConfigFile()
		{
		}

		//--------------------------------------------------------------

		private ConfigFile(String versionStr)
			throws AppException
		{
			super(Key.CONFIGURATION, null, versionStr);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String getSourceName()
		{
			return CONFIG_FILE2_STR;
		}

		//--------------------------------------------------------------

		@Override
		protected String getFileKindString()
		{
			return CONFIG_FILE1_STR;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void read(File file)
			throws AppException
		{
			// Read file
			read(file, Key.CONFIGURATION);

			// Test version number
			String versionStr = getVersionString();
			if (versionStr == null)
				throw new FileException(ErrorId.NO_VERSION_NUMBER, file);
			try
			{
				int version = Integer.parseInt(versionStr);
				if ((version < MIN_SUPPORTED_VERSION) || (version > MAX_SUPPORTED_VERSION))
					throw new FileException(ErrorId.UNSUPPORTED_CONFIGURATION_FILE, file, versionStr);
			}
			catch (NumberFormatException e)
			{
				throw new FileException(ErrorId.INVALID_VERSION_NUMBER, file);
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// PROPERTY CLASS: DEFAULT DOCUMENT KIND


	private class CPDefaultDocumentKind
		extends Property.EnumProperty<DocumentKind>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPDefaultDocumentKind()
		{
			super(concatenateKeys(Key.GENERAL, Key.DEFAULT_DOCUMENT_KIND), DocumentKind.class);
			value = DocumentKind.PARAMETERS;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public DocumentKind getDefaultDocumentKind()
	{
		return cpDefaultDocumentKind.getValue();
	}

	//------------------------------------------------------------------

	public void setDefaultDocumentKind(DocumentKind value)
	{
		cpDefaultDocumentKind.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPDefaultDocumentKind	cpDefaultDocumentKind	= new CPDefaultDocumentKind();

	//==================================================================


	// PROPERTY CLASS: SHOW UNIX PATHNAMES


	private class CPShowUnixPathnames
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPShowUnixPathnames()
		{
			super(concatenateKeys(Key.GENERAL, Key.SHOW_UNIX_PATHNAMES));
			value = Boolean.FALSE;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isShowUnixPathnames()
	{
		return cpShowUnixPathnames.getValue();
	}

	//------------------------------------------------------------------

	public void setShowUnixPathnames(boolean value)
	{
		cpShowUnixPathnames.setValue(value);
	}

	//------------------------------------------------------------------

	public void addShowUnixPathnamesObserver(Property.IObserver observer)
	{
		cpShowUnixPathnames.addObserver(observer);
	}

	//------------------------------------------------------------------

	public void removeShowUnixPathnamesObserver(Property.IObserver observer)
	{
		cpShowUnixPathnames.removeObserver(observer);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPShowUnixPathnames	cpShowUnixPathnames	= new CPShowUnixPathnames();

	//==================================================================


	// PROPERTY CLASS: SELECT TEXT ON FOCUS GAINED


	private class CPSelectTextOnFocusGained
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPSelectTextOnFocusGained()
		{
			super(concatenateKeys(Key.GENERAL, Key.SELECT_TEXT_ON_FOCUS_GAINED));
			value = Boolean.TRUE;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isSelectTextOnFocusGained()
	{
		return cpSelectTextOnFocusGained.getValue();
	}

	//------------------------------------------------------------------

	public void setSelectTextOnFocusGained(boolean value)
	{
		cpSelectTextOnFocusGained.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPSelectTextOnFocusGained	cpSelectTextOnFocusGained	= new CPSelectTextOnFocusGained();

	//==================================================================


	// PROPERTY CLASS: SHOW FULL PATHNAMES


	private class CPShowFullPathnames
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPShowFullPathnames()
		{
			super(concatenateKeys(Key.GENERAL, Key.SHOW_FULL_PATHNAMES));
			value = Boolean.FALSE;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isShowFullPathnames()
	{
		return cpShowFullPathnames.getValue();
	}

	//------------------------------------------------------------------

	public void setShowFullPathnames(boolean value)
	{
		cpShowFullPathnames.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPShowFullPathnames	cpShowFullPathnames	= new CPShowFullPathnames();

	//==================================================================


	// PROPERTY CLASS: MAIN WINDOW LOCATION


	private class CPMainWindowLocation
		extends Property.SimpleProperty<Point>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPMainWindowLocation()
		{
			super(concatenateKeys(Key.GENERAL, Key.MAIN_WINDOW_LOCATION));
			value = new Point();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input input)
			throws AppException
		{
			if (input.getValue().isEmpty())
				value = null;
			else
			{
				int[] outValues = input.parseIntegers(2, null);
				value = new Point(outValues[0], outValues[1]);
			}
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return ((value == null) ? "" : value.x + ", " + value.y);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isMainWindowLocation()
	{
		return (getMainWindowLocation() != null);
	}

	//------------------------------------------------------------------

	public Point getMainWindowLocation()
	{
		return cpMainWindowLocation.getValue();
	}

	//------------------------------------------------------------------

	public void setMainWindowLocation(Point value)
	{
		cpMainWindowLocation.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPMainWindowLocation	cpMainWindowLocation	= new CPMainWindowLocation();

	//==================================================================


	// PROPERTY CLASS: MAIN WINDOW SIZE


	private class CPMainWindowSize
		extends Property.SimpleProperty<Dimension>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPMainWindowSize()
		{
			super(concatenateKeys(Key.GENERAL, Key.MAIN_WINDOW_SIZE));
			value = new Dimension(MainWindow.DEFAULT_WIDTH, MainWindow.DEFAULT_HEIGHT);
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
			{
				int[] outValues = input.parseIntegers(2, null);
				value = new Dimension(outValues[0], outValues[1]);
			}
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return (value.width + ", " + value.height);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Dimension getMainWindowSize()
	{
		return cpMainWindowSize.getValue();
	}

	//------------------------------------------------------------------

	public void setMainWindowSize(Dimension value)
	{
		cpMainWindowSize.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPMainWindowSize	cpMainWindowSize	= new CPMainWindowSize();

	//==================================================================


	// PROPERTY CLASS: MAXIMUM EDIT LIST LENGTH


	private class CPMaxEditListLength
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPMaxEditListLength()
		{
			super(concatenateKeys(Key.GENERAL, Key.MAX_EDIT_LIST_LENGTH),
				  PatternDocument.MIN_MAX_EDIT_LIST_LENGTH, PatternDocument.MAX_MAX_EDIT_LIST_LENGTH);
			value = PatternDocument.DEFAULT_MAX_EDIT_LIST_LENGTH;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getMaxEditListLength()
	{
		return cpMaxEditListLength.getValue();
	}

	//------------------------------------------------------------------

	public void setMaxEditListLength(int value)
	{
		cpMaxEditListLength.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPMaxEditListLength	cpMaxEditListLength	= new CPMaxEditListLength();

	//==================================================================


	// PROPERTY CLASS: CLEAR EDIT LIST ON SAVE


	private class CPClearEditListOnSave
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPClearEditListOnSave()
		{
			super(concatenateKeys(Key.GENERAL, Key.CLEAR_EDIT_LIST_ON_SAVE));
			value = Boolean.FALSE;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isClearEditListOnSave()
	{
		return cpClearEditListOnSave.getValue();
	}

	//------------------------------------------------------------------

	public void setClearEditListOnSave(boolean value)
	{
		cpClearEditListOnSave.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPClearEditListOnSave	cpClearEditListOnSave	= new CPClearEditListOnSave();

	//==================================================================


	// PROPERTY CLASS: KEEP SEQUENCE WINDOW ON TOP


	private class CPKeepSequenceWindowOnTop
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPKeepSequenceWindowOnTop()
		{
			super(concatenateKeys(Key.GENERAL, Key.KEEP_SEQUENCE_WINDOW_ON_TOP));
			value = Boolean.TRUE;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isKeepSequenceWindowOnTop()
	{
		return cpKeepSequenceWindowOnTop.getValue();
	}

	//------------------------------------------------------------------

	public void setKeepSequenceWindowOnTop(boolean value)
	{
		cpKeepSequenceWindowOnTop.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPKeepSequenceWindowOnTop	cpKeepSequenceWindowOnTop	= new CPKeepSequenceWindowOnTop();

	//==================================================================


	// PROPERTY CLASS: LOOK-AND-FEEL


	private class CPLookAndFeel
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPLookAndFeel()
		{
			super(concatenateKeys(Key.APPEARANCE, Key.LOOK_AND_FEEL));
			value = "";
			for (UIManager.LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels())
			{
				if (lookAndFeelInfo.getClassName().
											equals(UIManager.getCrossPlatformLookAndFeelClassName()))
				{
					value = lookAndFeelInfo.getName();
					break;
				}
			}
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getLookAndFeel()
	{
		return cpLookAndFeel.getValue();
	}

	//------------------------------------------------------------------

	public void setLookAndFeel(String value)
	{
		cpLookAndFeel.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPLookAndFeel	cpLookAndFeel	= new CPLookAndFeel();

	//==================================================================


	// PROPERTY CLASS: TEXT ANTIALIASING


	private class CPTextAntialiasing
		extends Property.EnumProperty<TextRendering.Antialiasing>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPTextAntialiasing()
		{
			super(concatenateKeys(Key.APPEARANCE, Key.TEXT_ANTIALIASING),
				  TextRendering.Antialiasing.class);
			value = TextRendering.Antialiasing.DEFAULT;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public TextRendering.Antialiasing getTextAntialiasing()
	{
		return cpTextAntialiasing.getValue();
	}

	//------------------------------------------------------------------

	public void setTextAntialiasing(TextRendering.Antialiasing value)
	{
		cpTextAntialiasing.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPTextAntialiasing	cpTextAntialiasing	= new CPTextAntialiasing();

	//==================================================================


	// PROPERTY CLASS: PATHNAME OF DIRECTORY OF OPEN PATTERN FILE CHOOSER


	private class CPOpenPatternPathname
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPOpenPatternPathname()
		{
			super(concatenateKeys(Key.PATH, Key.OPEN_PATTERN_DIRECTORY));
			value = PropertyString.USER_HOME_PREFIX;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getOpenPatternPathname()
	{
		return cpOpenPatternPathname.getValue();
	}

	//------------------------------------------------------------------

	public File getOpenPatternDirectory()
	{
		return new File(PropertyString.parsePathname(getOpenPatternPathname()));
	}

	//------------------------------------------------------------------

	public void setOpenPatternPathname(String value)
	{
		cpOpenPatternPathname.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPOpenPatternPathname	cpOpenPatternPathname	= new CPOpenPatternPathname();

	//==================================================================


	// PROPERTY CLASS: PATHNAME OF DIRECTORY OF SAVE PATTERN FILE CHOOSER


	private class CPSavePatternPathname
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPSavePatternPathname()
		{
			super(concatenateKeys(Key.PATH, Key.SAVE_PATTERN_DIRECTORY));
			value = PropertyString.USER_HOME_PREFIX;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getSavePatternPathname()
	{
		return cpSavePatternPathname.getValue();
	}

	//------------------------------------------------------------------

	public File getSavePatternDirectory()
	{
		return new File(PropertyString.parsePathname(getSavePatternPathname()));
	}

	//------------------------------------------------------------------

	public void setSavePatternPathname(String value)
	{
		cpSavePatternPathname.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPSavePatternPathname	cpSavePatternPathname	= new CPSavePatternPathname();

	//==================================================================


	// PROPERTY CLASS: PATHNAME OF DIRECTORY OF EXPORT IMAGE FILE CHOOSER


	private class CPExportImagePathname
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPExportImagePathname()
		{
			super(concatenateKeys(Key.PATH, Key.EXPORT_IMAGE_DIRECTORY));
			value = PropertyString.USER_HOME_PREFIX;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getExportImagePathname()
	{
		return cpExportImagePathname.getValue();
	}

	//------------------------------------------------------------------

	public File getExportImageDirectory()
	{
		return new File(PropertyString.parsePathname(getExportImagePathname()));
	}

	//------------------------------------------------------------------

	public void setExportImagePathname(String value)
	{
		cpExportImagePathname.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPExportImagePathname	cpExportImagePathname	= new CPExportImagePathname();

	//==================================================================


	// PROPERTY CLASS: PATHNAME OF DIRECTORY OF EXPORT SVG FILE CHOOSER


	private class CPExportSvgPathname
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPExportSvgPathname()
		{
			super(concatenateKeys(Key.PATH, Key.EXPORT_SVG_DIRECTORY));
			value = PropertyString.USER_HOME_PREFIX;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getExportSvgPathname()
	{
		return cpExportSvgPathname.getValue();
	}

	//------------------------------------------------------------------

	public File getExportSvgDirectory()
	{
		return new File(PropertyString.parsePathname(getExportSvgPathname()));
	}

	//------------------------------------------------------------------

	public void setExportSvgPathname(String value)
	{
		cpExportSvgPathname.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPExportSvgPathname	cpExportSvgPathname	= new CPExportSvgPathname();

	//==================================================================


	// PROPERTY CLASS: PATHNAME OF DIRECTORY OF EXPORT IMAGE SEQUENCE FILE CHOOSER


	private class CPExportImageSequencePathname
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPExportImageSequencePathname()
		{
			super(concatenateKeys(Key.PATH, Key.EXPORT_IMAGE_SEQUENCE_DIRECTORY));
			value = PropertyString.USER_HOME_PREFIX;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getExportImageSequencePathname()
	{
		return cpExportImageSequencePathname.getValue();
	}

	//------------------------------------------------------------------

	public File getExportImageSequenceDirectory()
	{
		return new File(PropertyString.parsePathname(getExportImageSequencePathname()));
	}

	//------------------------------------------------------------------

	public void setExportImageSequencePathname(String value)
	{
		cpExportImageSequencePathname.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPExportImageSequencePathname	cpExportImageSequencePathname	=
																	new CPExportImageSequencePathname();

	//==================================================================


	// PROPERTY CLASS: PATTERN NAME


	private class CPPatternName
		extends Property.PropertyMap<PatternKind, String>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPPatternName()
		{
			super(concatenateKeys(Key.PATTERN, Key.NAME), PatternKind.class);
			for (PatternKind patternKind : PatternKind.values())
				values.put(patternKind, patternKind.toString());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input       input,
						  PatternKind patternKind)
			throws AppException
		{
			values.put(patternKind, input.getValue());
		}

		//--------------------------------------------------------------

		@Override
		public String toString(PatternKind patternKind)
		{
			return getValue(patternKind);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getPatternName(PatternKind key)
	{
		return cpPatternName.getValue(key);
	}

	//------------------------------------------------------------------

	public void setPatternName(PatternKind key,
							   String      value)
	{
		cpPatternName.setValue(key, value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPPatternName	cpPatternName	= new CPPatternName();

	//==================================================================


	// PROPERTY CLASS: DEFAULT SIZE OF PATTERN


	private class CPDefaultPatternSize
		extends Property.PropertyMap<PatternKind, Dimension>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPDefaultPatternSize()
		{
			super(concatenateKeys(Key.PATTERN, Key.DEFAULT_SIZE), PatternKind.class);
			for (PatternKind patternKind : PatternKind.values())
				values.put(patternKind, patternKind.getDefaultSize());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input       input,
						  PatternKind patternKind)
			throws AppException
		{
			int[] outValues = input.parseIntegers(2, patternKind.getSizeRanges());
			values.put(patternKind, new Dimension(outValues[0], outValues[1]));
		}

		//--------------------------------------------------------------

		@Override
		public String toString(PatternKind patternKind)
		{
			Dimension value = getValue(patternKind);
			return (value.width + ", " + value.height);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Dimension getDefaultPatternSize(PatternKind key)
	{
		return cpDefaultPatternSize.getValue(key);
	}

	//------------------------------------------------------------------

	public void setDefaultPatternSize(PatternKind key,
									  Dimension   value)
	{
		cpDefaultPatternSize.setValue(key, value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPDefaultPatternSize	cpDefaultPatternSize	= new CPDefaultPatternSize();

	//==================================================================


	// PROPERTY CLASS: NUMBER OF SLIDE-SHOW THREADS


	private class CPNumSlideShowThreads
		extends Property.PropertyMap<PatternKind, Integer>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPNumSlideShowThreads()
		{
			super(concatenateKeys(Key.PATTERN, Key.NUM_SLIDE_SHOW_THREADS), PatternKind.class);
			for (PatternKind patternKind : PatternKind.values())
				values.put(patternKind, PatternDocument.DEFAULT_NUM_SLIDE_SHOW_THREADS);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input       input,
						  PatternKind patternKind)
			throws AppException
		{
			IntegerRange range = new IntegerRange(PatternDocument.MIN_NUM_SLIDE_SHOW_THREADS,
												  PatternDocument.MAX_NUM_SLIDE_SHOW_THREADS);
			values.put(patternKind, input.parseInteger(range));
		}

		//--------------------------------------------------------------

		@Override
		public String toString(PatternKind patternKind)
		{
			return Integer.toString(getValue(patternKind));
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getNumSlideShowThreads(PatternKind key)
	{
		return cpNumSlideShowThreads.getValue(key);
	}

	//------------------------------------------------------------------

	public void setNumSlideShowThreads(PatternKind key,
									   int         value)
	{
		cpNumSlideShowThreads.setValue(key, value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPNumSlideShowThreads	cpNumSlideShowThreads	= new CPNumSlideShowThreads();

	//==================================================================


	// PROPERTY CLASS: NUMBER OF RENDERING THREADS, PATTERN 1


	private class CPPattern1NumRenderingThreads
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPPattern1NumRenderingThreads()
		{
			super(concatenateKeys(PatternKind.PATTERN1.getKey(), Key.NUM_RENDERING_THREADS),
				  Pattern1Image.MIN_NUM_RENDERING_THREADS, Pattern1Image.MAX_NUM_RENDERING_THREADS);
			value = Pattern1Image.DEFAULT_NUM_RENDERING_THREADS;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getPattern1NumRenderingThreads()
	{
		return cpPattern1NumRenderingThreads.getValue();
	}

	//------------------------------------------------------------------

	public void setPattern1NumRenderingThreads(int value)
	{
		cpPattern1NumRenderingThreads.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPPattern1NumRenderingThreads	cpPattern1NumRenderingThreads	=
																	new CPPattern1NumRenderingThreads();

	//==================================================================


	// PROPERTY CLASS: PATTERN 1 PHASE ANIMATION


	private class CPPattern1PhaseAnimation
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPPattern1PhaseAnimation()
		{
			super(concatenateKeys(PatternKind.PATTERN1.getKey(), Key.PHASE_ANIMATION));
			value = Boolean.TRUE;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isPattern1PhaseAnimation()
	{
		return cpPattern1PhaseAnimation.getValue();
	}

	//------------------------------------------------------------------

	public void setPattern1PhaseAnimation(boolean value)
	{
		cpPattern1PhaseAnimation.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPPattern1PhaseAnimation	cpPattern1PhaseAnimation	= new CPPattern1PhaseAnimation();

	//==================================================================


	// PROPERTY CLASS: PATTERN 2 PATH RENDERING


	private class CPPattern2PathRendering
		extends Property.EnumProperty<Pattern2Image.PathRendering>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPPattern2PathRendering()
		{
			super(concatenateKeys(PatternKind.PATTERN2.getKey(), Key.PATH_RENDERING),
				  Pattern2Image.PathRendering.class);
			value = Pattern2Image.PathRendering.PURE;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Pattern2Image.PathRendering getPattern2PathRendering()
	{
		return cpPattern2PathRendering.getValue();
	}

	//------------------------------------------------------------------

	public void setPattern2PathRendering(Pattern2Image.PathRendering value)
	{
		cpPattern2PathRendering.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPPattern2PathRendering	cpPattern2PathRendering	= new CPPattern2PathRendering();

	//==================================================================


	// PROPERTY CLASS: SLIDE-SHOW INTERVAL


	private class CPSlideShowInterval
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPSlideShowInterval()
		{
			super(concatenateKeys(Key.SLIDE_SHOW, Key.INTERVAL),
				  PatternDocument.MIN_SLIDE_SHOW_INTERVAL, PatternDocument.MAX_SLIDE_SHOW_INTERVAL);
			value = PatternDocument.DEFAULT_SLIDE_SHOW_INTERVAL;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getSlideShowInterval()
	{
		return cpSlideShowInterval.getValue();
	}

	//------------------------------------------------------------------

	public void setSlideShowInterval(int value)
	{
		cpSlideShowInterval.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPSlideShowInterval	cpSlideShowInterval	= new CPSlideShowInterval();

	//==================================================================


	// PROPERTY CLASS: ANIMATION RATE


	private class CPAnimationRate
		extends Property.DoubleProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPAnimationRate()
		{
			super(concatenateKeys(Key.ANIMATION, Key.RATE),
				  Pattern1Document.MIN_ANIMATION_RATE, Pattern1Document.MAX_ANIMATION_RATE);
			value = Pattern1Document.DEFAULT_ANIMATION_RATE;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public double getAnimationRate()
	{
		return cpAnimationRate.getValue();
	}

	//------------------------------------------------------------------

	public void setAnimationRate(double value)
	{
		cpAnimationRate.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPAnimationRate	cpAnimationRate	= new CPAnimationRate();

	//==================================================================


	// PROPERTY CLASS: FONTS


	private class CPFonts
		extends Property.PropertyMap<AppFont, FontEx>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPFonts()
		{
			super(Key.FONT, AppFont.class);
			for (AppFont font : AppFont.values())
				values.put(font, new FontEx());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input   input,
						  AppFont appFont)
		{
			try
			{
				FontEx font = new FontEx(input.getValue());
				appFont.setFontEx(font);
				values.put(appFont, font);
			}
			catch (IllegalArgumentException e)
			{
				showWarningMessage(new IllegalValueException(input));
			}
			catch (uk.blankaspect.common.exception.ValueOutOfBoundsException e)
			{
				showWarningMessage(new ValueOutOfBoundsException(input));
			}
		}

		//--------------------------------------------------------------

		@Override
		public String toString(AppFont appFont)
		{
			return getValue(appFont).toString();
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public FontEx getFont(int index)
	{
		return cpFonts.getValue(AppFont.values()[index]);
	}

	//------------------------------------------------------------------

	public void setFont(int    index,
						FontEx font)
	{
		cpFonts.setValue(AppFont.values()[index], font);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance fields : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPFonts	cpFonts	= new CPFonts();

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private AppConfig()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void showWarningMessage(AppException exception)
	{
		App.INSTANCE.showWarningMessage(App.SHORT_NAME + " | " + CONFIG_ERROR_STR, exception);
	}

	//------------------------------------------------------------------

	public static void showErrorMessage(AppException exception)
	{
		App.INSTANCE.showErrorMessage(App.SHORT_NAME + " | " + CONFIG_ERROR_STR, exception);
	}

	//------------------------------------------------------------------

	private static File getFile()
		throws AppException
	{
		File file = null;

		// Get directory of JAR file
		File jarDirectory = null;
		try
		{
			jarDirectory = new File(AppConfig.class.getProtectionDomain().getCodeSource().getLocation().
																				toURI()).getParentFile();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// Get pathname of configuration directory from properties file
		String pathname = null;
		File propertiesFile = new File(jarDirectory, PROPERTIES_FILENAME);
		if (propertiesFile.isFile())
		{
			try
			{
				Properties properties = new Properties();
				properties.loadFromXML(new FileInputStream(propertiesFile));
				pathname = properties.getProperty(CONFIG_DIR_KEY);
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.ERROR_READING_PROPERTIES_FILE, propertiesFile);
			}
		}

		// Get pathname of configuration directory from system property or set system property to pathname
		try
		{
			if (pathname == null)
				pathname = System.getProperty(CONFIG_DIR_KEY);
			else
				System.setProperty(CONFIG_DIR_KEY, pathname);
		}
		catch (SecurityException e)
		{
			// ignore
		}

		// Look for configuration file in default locations
		if (pathname == null)
		{
			// Look for configuration file in local directory
			file = new File(CONFIG_FILENAME);

			// Look for configuration file in default configuration directory
			if (!file.isFile())
			{
				file = null;
				pathname = Utils.getPropertiesPathname();
				if (pathname != null)
				{
					file = new File(pathname, CONFIG_FILENAME);
					if (!file.isFile())
						file = null;
				}
			}
		}

		// Set configuration file from pathname of configuration directory
		else if (!pathname.isEmpty())
		{
			file = new File(PropertyString.parsePathname(pathname), CONFIG_FILENAME);
			if (!file.isFile())
				throw new FileException(ErrorId.NO_CONFIGURATION_FILE, file);
		}

		return file;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public File chooseFile(Component parent)
	{
		if (fileChooser == null)
		{
			fileChooser = new JFileChooser();
			fileChooser.setDialogTitle(SAVE_CONFIGURATION_FILE_STR);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setFileFilter(new FilenameSuffixFilter(AppConstants.XML_FILES_STR,
															   AppConstants.XML_FILE_SUFFIX));
			selectedFile = file;
		}

		fileChooser.setSelectedFile((selectedFile == null) ? new File(CONFIG_FILENAME).getAbsoluteFile()
														   : selectedFile.getAbsoluteFile());
		fileChooser.rescanCurrentDirectory();
		if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION)
		{
			selectedFile = Utils.appendSuffix(fileChooser.getSelectedFile(),
											  AppConstants.XML_FILE_SUFFIX);
			return selectedFile;
		}
		return null;
	}

	//------------------------------------------------------------------

	public void read()
	{
		// Read configuration file
		fileRead = false;
		ConfigFile configFile = null;
		try
		{
			file = getFile();
			if (file != null)
			{
				configFile = new ConfigFile();
				configFile.read(file);
				fileRead = true;
			}
		}
		catch (AppException e)
		{
			showErrorMessage(e);
		}

		// Get properties
		if (fileRead)
			getProperties(configFile, Property.getSystemSource());
		else
			getProperties(Property.getSystemSource());

		// Reset changed status of properties
		resetChanged();
	}

	//------------------------------------------------------------------

	public void write()
	{
		if (isChanged())
		{
			try
			{
				if (file == null)
				{
					if (System.getProperty(CONFIG_DIR_KEY) == null)
					{
						String pathname = Utils.getPropertiesPathname();
						if (pathname != null)
						{
							File directory = new File(pathname);
							if (!directory.exists() && !directory.mkdirs())
								throw new FileException(ErrorId.FAILED_TO_CREATE_DIRECTORY, directory);
							file = new File(directory, CONFIG_FILENAME);
						}
					}
				}
				else
				{
					if (!fileRead)
						file.renameTo(new File(file.getParentFile(), CONFIG_OLD_FILENAME));
				}
				if (file != null)
				{
					write(file);
					resetChanged();
				}
			}
			catch (AppException e)
			{
				showErrorMessage(e);
			}
		}
	}

	//------------------------------------------------------------------

	public void write(File file)
		throws AppException
	{
		// Initialise progress view
		IProgressView progressView = Task.getProgressView();
		if (progressView != null)
		{
			progressView.setInfo(WRITING_STR, file);
			progressView.setProgress(0, -1.0);
		}

		// Create new DOM document
		ConfigFile configFile = new ConfigFile(Integer.toString(VERSION));

		// Set configuration properties in document
		putProperties(configFile);

		// Write file
		configFile.write(file);
	}

	//------------------------------------------------------------------

	private void getProperties(Property.ISource... propertySources)
	{
		for (Property property : getProperties())
		{
			try
			{
				property.get(propertySources);
			}
			catch (AppException e)
			{
				showWarningMessage(e);
			}
		}
	}

	//------------------------------------------------------------------

	private void putProperties(Property.ITarget propertyTarget)
	{
		for (Property property : getProperties())
			property.put(propertyTarget);
	}

	//------------------------------------------------------------------

	private boolean isChanged()
	{
		for (Property property : getProperties())
		{
			if (property.isChanged())
				return true;
		}
		return false;
	}

	//------------------------------------------------------------------

	private void resetChanged()
	{
		for (Property property : getProperties())
			property.setChanged(false);
	}

	//------------------------------------------------------------------

	private List<Property> getProperties()
	{
		if (properties == null)
		{
			properties = new ArrayList<>();
			for (Field field : getClass().getDeclaredFields())
			{
				try
				{
					if (field.getName().startsWith(Property.FIELD_PREFIX))
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

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		INSTANCE = new AppConfig();
	}

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	private	File			file;
	private	boolean			fileRead;
	private	File			selectedFile;
	private	JFileChooser	fileChooser;
	private	List<Property>	properties;

}

//----------------------------------------------------------------------
