/*====================================================================*\

PatternDocument.java

Class: pattern document.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.awt.image.BufferedImage;

import java.beans.PropertyChangeListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.FileException;
import uk.blankaspect.common.exception.TempFileException;

import uk.blankaspect.common.filesystem.FilenameUtils;

import uk.blankaspect.common.misc.FileWritingMode;

import uk.blankaspect.common.number.NumberUtils;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.common.ui.progress.IProgressView;

import uk.blankaspect.common.xml.AttributeList;
import uk.blankaspect.common.xml.XmlConstants;
import uk.blankaspect.common.xml.XmlFile;
import uk.blankaspect.common.xml.XmlParseException;
import uk.blankaspect.common.xml.XmlUtils;
import uk.blankaspect.common.xml.XmlWriter;

import uk.blankaspect.ui.swing.image.PngOutputFile;

//----------------------------------------------------------------------


// CLASS: PATTERN DOCUMENT


abstract class PatternDocument
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int		MIN_MAX_EDIT_LIST_LENGTH		= 1;
	public static final		int		MAX_MAX_EDIT_LIST_LENGTH		= 9999;
	public static final		int		DEFAULT_MAX_EDIT_LIST_LENGTH	= 100;

	public static final		int		MIN_NUM_SLIDE_SHOW_THREADS		= 0;
	public static final		int		MAX_NUM_SLIDE_SHOW_THREADS		= 32;
	public static final		int		DEFAULT_NUM_SLIDE_SHOW_THREADS	= 0;

	public static final		int		MIN_SLIDE_SHOW_INTERVAL		= 500;
	public static final		int		MAX_SLIDE_SHOW_INTERVAL		= 60000;
	public static final		int		DEFAULT_SLIDE_SHOW_INTERVAL	= 3000;

	protected static final	String	GENERATE_STR	= "Generate ";
	protected static final	String	RENDERING_STR	= "Rendering image " + AppConstants.ELLIPSIS_STR;
	protected static final	String	WRITING_STR		= "Writing";

	private static final	int		MIN_SUPPORTED_VERSION	= 0;
	private static final	int		MAX_SUPPORTED_VERSION	= 0;
	private static final	int		VERSION					= 0;

	private static final	String	NAMESPACE_PREFIX		= "http://ns.blankaspect.uk/patternGenerator-definition/";
	private static final	String	NAMESPACE_PREFIX_REGEX	= "http://ns\\.[a-z.]+/patternGenerator-definition/(\\w+)";

	private static final	String	UNNAMED_STR			= "Unnamed";
	private static final	String	CLEAR_EDIT_LIST_STR	= "Do you want to clear all the undo/redo actions?";

	private static final	String	SVG_NAMESPACE_NAME	= "http://www.w3.org/2000/svg";
	private static final	String	SVG_VERSION_STR		= "1.1";

	private interface ElementName
	{
		String	PATTERN_GENERATOR	= "patternGenerator";
	}

	private interface AttrName
	{
		String	VERSION	= "version";
		String	XMLNS	= "xmlns";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	int					newFileIndex;
	private static	RenderingTimeDialog	renderingTimeDialog;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	File				file;
	private	DocumentKind		documentKind;
	private	File				exportImageFile;
	private	File				exportSvgFile;
	private	long				timestamp;
	private	int					unnamedIndex;
	private	boolean				executingCommand;
	private	boolean				playing;
	private	boolean				paused;
	private	ImageSequenceParams	imageSequenceParams;
	private	SequenceDialog		sequenceDialog;
	private	EditList			editList;
	private	RenderingTime		renderingTime;

	private volatile	int		absoluteFrameIndex;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	protected PatternDocument(
		File			file,
		DocumentKind	documentKind)
	{
		this(file, documentKind, false);
	}

	//------------------------------------------------------------------

	protected PatternDocument(
		File			file,
		DocumentKind	documentKind,
		boolean			temporary)
	{
		this.file = file;
		this.documentKind = documentKind;
		if ((file == null) && !temporary)
			unnamedIndex = ++newFileIndex;
		imageSequenceParams = new ImageSequenceParams();
		editList = new EditList();
		renderingTime = new RenderingTime();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static PatternDocument read(
		FileInfo	fileInfo)
		throws AppException
	{
		// Determine document kind
		File file = fileInfo.file;
		DocumentKind documentKind = fileInfo.documentKind;
		if (documentKind == null)
			documentKind = DocumentKind.forFilename(file.getName());
		if (documentKind == null)
			documentKind = isDefinitionDocument(file) ? DocumentKind.DEFINITION : DocumentKind.PARAMETERS;

		// Read document
		PatternDocument patternDocument = null;
		switch (documentKind)
		{
			case DEFINITION:
				patternDocument = readDefinition(file);
				break;

			case PARAMETERS:
				patternDocument = readParameters(file);
				break;
		}

		// Set timestamp
		if (patternDocument != null)
			patternDocument.timestamp = file.lastModified();

		return patternDocument;
	}

	//------------------------------------------------------------------

	public static void updateRenderingTime()
	{
		if (renderingTimeDialog != null)
			renderingTimeDialog.updateRenderingTime();
	}

	//------------------------------------------------------------------

	public static void closeRenderingTimeDialog()
	{
		if (renderingTimeDialog != null)
		{
			renderingTimeDialog.close();
			renderingTimeDialog = null;
		}
	}

	//------------------------------------------------------------------

	protected static String getElementName()
	{
		return ElementName.PATTERN_GENERATOR;
	}

	//------------------------------------------------------------------

	protected static MainWindow getWindow()
	{
		return PatternGeneratorApp.INSTANCE.getMainWindow();
	}

	//------------------------------------------------------------------

	private static boolean isDefinitionDocument(
		File	file)
		throws AppException
	{
		Document document = XmlFile.read(file);
		return document.getDocumentElement().getNodeName().equals(ElementName.PATTERN_GENERATOR);
	}

	//------------------------------------------------------------------

	private static PatternDocument readDefinition(
		File	file)
		throws AppException
	{
		// Read XML file
		Document document = XmlFile.read(file);

		// Test document format
		Element element = document.getDocumentElement();
		if (!element.getNodeName().equals(ElementName.PATTERN_GENERATOR))
			throw new FileException(ErrorId.UNEXPECTED_DOCUMENT_FORMAT, file);
		String elementPath = ElementName.PATTERN_GENERATOR;

		// Attribute: namespace
		String attrName = AttrName.XMLNS;
		String attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		String attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, file, attrKey);
		PatternKind patternKind = null;
		Matcher matcher = Pattern.compile(NAMESPACE_PREFIX_REGEX).matcher(attrValue);
		if (matcher.matches())
			patternKind = PatternKind.forKey(matcher.group(1));
		if (patternKind == null)
			throw new FileException(ErrorId.UNEXPECTED_DOCUMENT_FORMAT, file);

		// Attribute: version
		attrName = AttrName.VERSION;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, file, attrKey);
		try
		{
			int version = Integer.parseInt(attrValue);
			if ((version < MIN_SUPPORTED_VERSION) || (version > MAX_SUPPORTED_VERSION))
				throw new FileException(ErrorId.UNSUPPORTED_DOCUMENT_VERSION, file, attrValue);
		}
		catch (NumberFormatException e)
		{
			throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, file, attrKey, attrValue);
		}

		// Parse pattern node
		NodeList nodes = element.getElementsByTagName(patternKind.getKey());
		if (nodes.getLength() == 0)
			throw new FileException(ErrorId.NO_PATTERN_ELEMENT, file);
		if (nodes.getLength() > 1)
			throw new FileException(ErrorId.MULTIPLE_PATTERN_ELEMENTS, file);

		// Create instance of document for pattern kind
		PatternDocument patternDocument = null;
		switch (patternKind)
		{
			case PATTERN1:
			{
				patternDocument = new Pattern1Document(file, (Element)nodes.item(0));
				break;
			}

			case PATTERN2:
			{
				patternDocument = new Pattern2Document(file, (Element)nodes.item(0));
				break;
			}
		}
		return patternDocument;
	}

	//------------------------------------------------------------------

	private static PatternDocument readParameters(
		File	file)
		throws AppException
	{
		PatternParams params = PatternParams.read(file);
		PatternDocument patternDocument = null;
		switch (params.getPatternKind())
		{
			case PATTERN1:
			{
				patternDocument = new Pattern1Document(file, (Pattern1Params)params);
				break;
			}

			case PATTERN2:
			{
				patternDocument = new Pattern2Document(file, (Pattern2Params)params);
				break;
			}
		}
		return patternDocument;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Abstract methods
////////////////////////////////////////////////////////////////////////

	public abstract PatternKind getPatternKind();

	//------------------------------------------------------------------

	public abstract PatternParams getParameters();

	//------------------------------------------------------------------

	public abstract void setParameters(
		PatternParams	params)
		throws AppException;

	//------------------------------------------------------------------

	public abstract void setParametersAndPatternImage(
		PatternParams	params,
		PatternImage	patternImage);

	//------------------------------------------------------------------

	public abstract boolean hasImage();

	//------------------------------------------------------------------

	public abstract BufferedImage getImage();

	//------------------------------------------------------------------

	public abstract BufferedImage getExportImage()
		throws InterruptedException;

	//------------------------------------------------------------------

	public abstract PatternImage createPatternImage(
		PatternParams	params)
		throws InterruptedException;

	//------------------------------------------------------------------

	public abstract void generatePattern()
		throws AppException;

	//------------------------------------------------------------------

	public abstract boolean editParameters()
		throws AppException;

	//------------------------------------------------------------------

	public abstract void setSeed(long seed)
		throws AppException;

	//------------------------------------------------------------------

	public abstract PatternDocument createDefinitionDocument(
		boolean	temporary)
		throws AppException;

	//------------------------------------------------------------------

	public abstract void write(
		XmlWriter	writer)
		throws IOException;

	//------------------------------------------------------------------

	protected abstract boolean canExportAsSvg();

	//------------------------------------------------------------------

	public abstract void writeSvgElements(
		XmlWriter	writer,
		int			indent)
		throws IOException;

	//------------------------------------------------------------------

	protected abstract boolean hasParameters();

	//------------------------------------------------------------------

	protected abstract String getDescription();

	//------------------------------------------------------------------

	protected abstract void setDescription(
		String	description);

	//------------------------------------------------------------------

	protected abstract int getNumAnimationKinds();

	//------------------------------------------------------------------

	protected abstract AnimationParams selectAnimation(
		boolean	imageSequence);

	//------------------------------------------------------------------

	protected abstract boolean canOptimiseAnimation();

	//------------------------------------------------------------------

	protected abstract void optimiseAnimation();

	//------------------------------------------------------------------

	protected abstract boolean initAnimation(
		int	animationId,
		int	startFrameIndex);

	//------------------------------------------------------------------

	protected abstract void updateAnimation(
		int	frameIndex)
		throws InterruptedException;

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public File getFile()
	{
		return file;
	}

	//------------------------------------------------------------------

	public FileInfo getFileInfo()
	{
		return new FileInfo(file, documentKind, hasParameters());
	}

	//------------------------------------------------------------------

	public File getExportImageFile()
	{
		return exportImageFile;
	}

	//------------------------------------------------------------------

	public File getExportSvgFile()
	{
		return exportSvgFile;
	}

	//------------------------------------------------------------------

	public long getTimestamp()
	{
		return timestamp;
	}

	//------------------------------------------------------------------

	public boolean isExecutingCommand()
	{
		return executingCommand;
	}

	//------------------------------------------------------------------

	public boolean isPlaying()
	{
		return playing;
	}

	//------------------------------------------------------------------

	public boolean isChanged()
	{
		return (file == null) || editList.isChanged();
	}

	//------------------------------------------------------------------

	public boolean hasAnimation()
	{
		return (getNumAnimationKinds() > 0);
	}

	//------------------------------------------------------------------

	public ImageSequenceParams getImageSequenceParams()
	{
		return imageSequenceParams;
	}

	//------------------------------------------------------------------

	public int getAbsoluteFrameIndex()
	{
		return absoluteFrameIndex;
	}

	//------------------------------------------------------------------

	public String getName(
		boolean	fullPathname)
	{
		return (file == null) ? UNNAMED_STR + unnamedIndex : fullPathname ? Utils.getPathname(file) : file.getName();
	}

	//------------------------------------------------------------------

	public String getFilenameStem()
	{
		String filename = null;
		if (file != null)
		{
			filename = file.getName();
			int length = filename.length();
			filename = StringUtils.removeSuffix(filename, AppConstants.PG_DEF_FILENAME_SUFFIX);
			if (filename.length() == length)
				filename = StringUtils.removeSuffix(filename, AppConstants.PG_PAR_FILENAME_SUFFIX);
			if (filename.length() == length)
				filename = StringUtils.getPrefixLast(filename, '.');
		}
		return filename;
	}

	//------------------------------------------------------------------

	public String getTitleString(
		boolean	fullPathname)
	{
		String str = getName(fullPathname);
		if (isChanged())
			str += AppConstants.FILE_CHANGED_SUFFIX;
		return str;
	}

	//------------------------------------------------------------------

	public String getParameterTitleString()
	{
		return (file == null) ? null : StringUtils.removeSuffix(file.getName(), AppConstants.PG_PAR_FILENAME_SUFFIX);
	}

	//------------------------------------------------------------------

	public void setTimestamp(
		long	timestamp)
	{
		this.timestamp = timestamp;
	}

	//------------------------------------------------------------------

	public void setPaused(
		boolean	paused)
	{
		this.paused = paused;
	}

	//------------------------------------------------------------------

	public void stopPlaying()
	{
		Task.setCancelled(true);
		while (playing)
		{
			try
			{
				Thread.sleep(50);
			}
			catch (InterruptedException e)
			{
				// ignore
			}
		}
		Task.setCancelled(false);
	}

	//------------------------------------------------------------------

	public double getRenderingTime()
	{
		return renderingTime.getMeanNanosecondsPerPixel();
	}

	//------------------------------------------------------------------

	public void resetRenderingTime()
	{
		renderingTime.reset();
		updateRenderingTime();
	}

	//------------------------------------------------------------------

	public void addRenderingTime(
		long	numPixels,
		long	nanoseconds)
	{
		renderingTime.add(numPixels, nanoseconds);
		updateRenderingTime();
	}

	//------------------------------------------------------------------

	public void write(
		FileInfo	fileInfo)
		throws AppException
	{
		// Set instance variables
		file = fileInfo.file;
		if (fileInfo.documentKind != null)
			documentKind = fileInfo.documentKind;

		// Write file
		if (documentKind == null)
			documentKind = AppConfig.INSTANCE.getDefaultDocumentKind();
		switch (documentKind)
		{
			case DEFINITION:
				writeDefinition();
				break;

			case PARAMETERS:
				writeParameters();
				break;
		}
	}

	//------------------------------------------------------------------

	public void writeImage(
		File	file)
		throws AppException
	{
		// Set instance variable
		exportImageFile = file;

		// Reset progress view
		TaskProgressDialog progressView = (TaskProgressDialog)Task.getProgressView();
		progressView.setInfo(WRITING_STR, file);
		progressView.setProgress(0, -1.0);

		// Write file
		try
		{
			PngOutputFile imageFile = new PngOutputFile(file, getExportImage());
			imageFile.addProgressListener(progressView);
			imageFile.write(FileWritingMode.USE_TEMP_FILE);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	//------------------------------------------------------------------

	public void writeImageSequence(
		ImageSequenceParams	params)
		throws AppException
	{
		// Set instance variable
		imageSequenceParams = params;

		// Select animation
		AnimationParams animationParams = selectAnimation(true);

		// Write images from running animation on a temporary document
		if (animationParams != null)
			createDefinitionDocument(true).writeImageSequence(animationParams.animationKind,
															  params.directory, params.filenameStem,
															  params.frameWidth, params.frameHeight,
															  params.startFrameIndex, params.numFrames,
															  params.fadeIn, params.fadeOut);
	}

	//------------------------------------------------------------------

	public void writeSvg(
		File	file)
		throws AppException
	{
		// Set instance variable
		exportSvgFile = file;

		// Initialise progress view
		IProgressView progressView = Task.getProgressView();
		progressView.setInfo(WRITING_STR, file);
		progressView.setProgress(0, -1.0);

		// Write file
		File tempFile = null;
		XmlWriter writer = null;
		boolean oldFileDeleted = false;
		try
		{
			// Create parent directory of output file
			File directory = file.getAbsoluteFile().getParentFile();
			if ((directory != null) && !directory.exists())
			{
				try
				{
					if (!directory.mkdirs())
						throw new FileException(ErrorId.FAILED_TO_CREATE_DIRECTORY, directory);
				}
				catch (SecurityException e)
				{
					throw new FileException(ErrorId.FAILED_TO_CREATE_DIRECTORY, directory, e);
				}
			}

			// Create temporary file
			try
			{
				tempFile = FilenameUtils.tempLocation(file);
				tempFile.createNewFile();
			}
			catch (Exception e)
			{
				throw new AppException(ErrorId.FAILED_TO_CREATE_TEMPORARY_FILE, e);
			}

			// Open XML writer on temporary file
			try
			{
				writer = new XmlWriter(tempFile, StandardCharsets.UTF_8);
			}
			catch (FileNotFoundException e)
			{
				throw new FileException(ErrorId.FAILED_TO_OPEN_FILE, tempFile, e);
			}
			catch (SecurityException e)
			{
				throw new FileException(ErrorId.FILE_ACCESS_NOT_PERMITTED, tempFile, e);
			}

			// Write file
			try
			{
				// Write XML declaration
				writer.writeXmlDeclaration(AppConstants.XML_VERSION_STR, XmlConstants.ENCODING_NAME_UTF8,
										   XmlWriter.Standalone.NO);

				// Write root element start tag
				AttributeList attributes = new AttributeList();
				attributes.add(Svg.AttrName.XMLNS, SVG_NAMESPACE_NAME);
				attributes.add(Svg.AttrName.VERSION, SVG_VERSION_STR);
				attributes.add(Svg.AttrName.WIDTH, getImage().getWidth());
				attributes.add(Svg.AttrName.HEIGHT, getImage().getHeight());
				writer.writeElementStart(Svg.ElementName.SVG, attributes, 0, true, true);

				// Write SVG elements
				writeSvgElements(writer, XmlWriter.INDENT_INCREMENT);

				// Write root element end tag
				writer.writeElementEnd(Svg.ElementName.SVG, 0);
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.ERROR_WRITING_FILE, tempFile, e);
			}

			// Close output stream
			try
			{
				writer.close();
				writer = null;
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.FAILED_TO_CLOSE_FILE, tempFile, e);
			}

			// Delete any existing file
			try
			{
				if (file.exists() && !file.delete())
					throw new FileException(ErrorId.FAILED_TO_DELETE_FILE, file);
				oldFileDeleted = true;
			}
			catch (SecurityException e)
			{
				throw new FileException(ErrorId.FAILED_TO_DELETE_FILE, file, e);
			}

			// Rename temporary file
			try
			{
				if (!tempFile.renameTo(file))
					throw new TempFileException(ErrorId.FAILED_TO_RENAME_FILE, file, tempFile);
			}
			catch (SecurityException e)
			{
				throw new TempFileException(ErrorId.FAILED_TO_RENAME_FILE, file, e, tempFile);
			}
		}
		catch (AppException e)
		{
			// Close output stream
			try
			{
				if (writer != null)
					writer.close();
			}
			catch (Exception e1)
			{
				// ignore
			}

			// Delete temporary file
			try
			{
				if (!oldFileDeleted && (tempFile != null) && tempFile.exists())
					tempFile.delete();
			}
			catch (Exception e1)
			{
				// ignore
			}

			// Rethrow exception
			throw e;
		}
	}

	//------------------------------------------------------------------

	public void updateCommands()
	{
		boolean hasParameters = hasParameters();
		boolean hasImage = hasImage();
		boolean hasAnimation = hasAnimation();

		Command.UNDO.setEnabled(editList.canUndo());
		Command.REDO.setEnabled(editList.canRedo());
		Command.CLEAR_EDIT_LIST.setEnabled(!editList.isEmpty());
		Command.EDIT_PATTERN_PARAMETERS.setEnabled(hasParameters);
		Command.EDIT_DESCRIPTION.setEnabled(true);
		Command.REGENERATE_PATTERN_WITH_NEW_SEED.setEnabled(hasParameters);
		Command.SHOW_IMAGE_RENDERING_TIME.setEnabled(renderingTimeDialog == null);
		Command.START_SLIDE_SHOW.setEnabled(hasParameters);
		Command.START_ANIMATION.setEnabled(hasImage && hasAnimation);
		Command.OPTIMISE_ANIMATION.setEnabled(hasAnimation && canOptimiseAnimation());
		Command.RESIZE_WINDOW_TO_IMAGE.setEnabled(hasImage && !getWindow().isMaximised());
	}

	//------------------------------------------------------------------

	public void executeCommand(
		Command	command)
	{
		// Set command execution flag
		executingCommand = true;

		// Perform command
		Edit edit = null;
		try
		{
			try
			{
				edit = switch (command)
				{
					case UNDO                             -> onUndo();
					case REDO                             -> onRedo();
					case CLEAR_EDIT_LIST                  -> onClearEditList();
					case EDIT_PATTERN_PARAMETERS          -> onEditPatternParameters();
					case EDIT_DESCRIPTION                 -> onEditDescription();
					case REGENERATE_PATTERN_WITH_NEW_SEED -> onRegeneratePatternWithNewSeed();
					case SHOW_IMAGE_RENDERING_TIME        -> onShowImageRenderingTime();
					case START_SLIDE_SHOW                 -> onStartSlideShow();
					case START_ANIMATION                  -> onStartAnimation();
					case OPTIMISE_ANIMATION               -> onOptimiseAnimation();
					case RESIZE_WINDOW_TO_IMAGE           -> onResizeWindowToImage();
				};
			}
			catch (OutOfMemoryError e)
			{
				throw new AppException(ErrorId.NOT_ENOUGH_MEMORY_TO_PERFORM_COMMAND);
			}
		}
		catch (AppException e)
		{
			PatternGeneratorApp.INSTANCE.showErrorMessage(PatternGeneratorApp.SHORT_NAME, e);
		}

		// Add edit to undo list
		if (edit != null)
			editList.addEdit(edit);

		// Update tab text and title and menus in main window
		PatternGeneratorApp.INSTANCE.updateTabText(this);
		getWindow().updateTitleAndMenus();

		// Clear command execution flag
		executingCommand = false;
	}

	//------------------------------------------------------------------

	protected void appendCommonAttributes(
		AttributeList	attributes)
	{
		attributes.add(AttrName.XMLNS, NAMESPACE_PREFIX + getPatternKind().getKey());
		attributes.add(AttrName.VERSION, VERSION);
	}

	//------------------------------------------------------------------

	private PatternView getView()
	{
		return PatternGeneratorApp.INSTANCE.getView(this);
	}

	//------------------------------------------------------------------

	private void updateView()
	{
		getView().setImage(getImage());
	}

	//------------------------------------------------------------------

	private void updatePlayView()
	{
		sequenceDialog.setImage(getImage());
	}

	//------------------------------------------------------------------

	private void writeDefinition()
		throws AppException
	{
		// Initialise progress view
		IProgressView progressView = Task.getProgressView();
		progressView.setInfo(WRITING_STR, file);
		progressView.setProgress(0, -1.0);

		// Write file
		File tempFile = null;
		XmlWriter writer = null;
		boolean oldFileDeleted = false;
		long timestamp = this.timestamp;
		this.timestamp = 0;
		try
		{
			// Create parent directory of output file
			File directory = file.getAbsoluteFile().getParentFile();
			if ((directory != null) && !directory.exists())
			{
				try
				{
					if (!directory.mkdirs())
						throw new FileException(ErrorId.FAILED_TO_CREATE_DIRECTORY, directory);
				}
				catch (SecurityException e)
				{
					throw new FileException(ErrorId.FAILED_TO_CREATE_DIRECTORY, directory, e);
				}
			}

			// Create temporary file
			try
			{
				tempFile = FilenameUtils.tempLocation(file);
				tempFile.createNewFile();
			}
			catch (Exception e)
			{
				throw new AppException(ErrorId.FAILED_TO_CREATE_TEMPORARY_FILE, e);
			}

			// Open XML writer on temporary file
			try
			{
				writer = new XmlWriter(tempFile, StandardCharsets.UTF_8);
			}
			catch (FileNotFoundException e)
			{
				throw new FileException(ErrorId.FAILED_TO_OPEN_FILE, tempFile, e);
			}
			catch (SecurityException e)
			{
				throw new FileException(ErrorId.FILE_ACCESS_NOT_PERMITTED, tempFile, e);
			}

			// Write file
			try
			{
				// Write XML declaration
				writer.writeXmlDeclaration(AppConstants.XML_VERSION_STR, XmlConstants.ENCODING_NAME_UTF8,
										   XmlWriter.Standalone.NO);

				// Write pattern definition
				write(writer);
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.ERROR_WRITING_FILE, tempFile, e);
			}

			// Close output stream
			try
			{
				writer.close();
				writer = null;
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.FAILED_TO_CLOSE_FILE, tempFile, e);
			}

			// Delete any existing file
			try
			{
				if (file.exists() && !file.delete())
					throw new FileException(ErrorId.FAILED_TO_DELETE_FILE, file);
				oldFileDeleted = true;
			}
			catch (SecurityException e)
			{
				throw new FileException(ErrorId.FAILED_TO_DELETE_FILE, file, e);
			}

			// Rename temporary file
			try
			{
				if (!tempFile.renameTo(file))
					throw new TempFileException(ErrorId.FAILED_TO_RENAME_FILE, file, tempFile);
			}
			catch (SecurityException e)
			{
				throw new TempFileException(ErrorId.FAILED_TO_RENAME_FILE, file, e, tempFile);
			}

			// Set timestamp
			timestamp = file.lastModified();

			// Reset list of edits
			if (AppConfig.INSTANCE.isClearEditListOnSave())
				editList.clear();
			else
				editList.reset();
		}
		catch (AppException e)
		{
			// Close output stream
			try
			{
				if (writer != null)
					writer.close();
			}
			catch (Exception e1)
			{
				// ignore
			}

			// Delete temporary file
			try
			{
				if (!oldFileDeleted && (tempFile != null) && tempFile.exists())
					tempFile.delete();
			}
			catch (Exception e1)
			{
				// ignore
			}

			// Rethrow exception
			throw e;
		}
		finally
		{
			this.timestamp = timestamp;
		}
	}

	//------------------------------------------------------------------

	private void writeParameters()
		throws AppException
	{
		// Test for parameters
		if (!hasParameters())
			throw new AppException(ErrorId.NO_PARAMETERS);

		// Write file
		long timestamp = this.timestamp;
		this.timestamp = 0;
		try
		{
			// Write file
			getParameters().write(file);

			// Set timestamp
			timestamp = file.lastModified();
		}
		finally
		{
			this.timestamp = timestamp;
		}

		// Reset list of edits
		if (AppConfig.INSTANCE.isClearEditListOnSave())
			editList.clear();
		else
			editList.reset();
	}

	//------------------------------------------------------------------

	private void doAnimation(
		AnimationParams	animationParams)
	{
		// Get index of first frame
		int startFrameIndex = animationParams.startFrameIndex;

		// Initialise animation; if initialisation is successful, start animation task
		absoluteFrameIndex = 0;
		paused = false;
		playing = false;
		Task.setCancelled(false);
		if (initAnimation(animationParams.animationKind, startFrameIndex))
		{
			// Hide main window
			getWindow().setVisible(false);

			// Create dialog
			sequenceDialog = new SequenceDialog(getWindow(), this);

			// Start animation task
			playing = true;
			new Thread(() ->
			{
				long interval = Math.round(1_000_000_000.0 / animationParams.frameRate);
				int frameIndex = 0;
				long endTime = 0;
				while (true)
				{
					// Test whether task has been cancelled
					if (Task.isCancelled())
						break;

					// Get current time
					long currentTime = System.nanoTime();

					// If paused, update end time ...
					if (paused)
						endTime = currentTime;

					// ... otherwise, generate next image; update view
					else if (currentTime >= endTime)
					{
						// Update animation
						try
						{
							absoluteFrameIndex = startFrameIndex + frameIndex;
							updateAnimation(absoluteFrameIndex);
						}
						catch (InterruptedException e)
						{
							break;
						}

						// Update view
						SwingUtilities.invokeLater(this::updatePlayView);

						// Increment end time
						if (endTime == 0)
							endTime = currentTime;

						while (currentTime >= endTime)
						{
							endTime += interval;
							++frameIndex;
						}
					}
				}

				// Enable commands
				playing = false;
			})
			.start();

			// Show dialog
			sequenceDialog.setVisible(true);

			// Show main window
			getWindow().setVisible(true);
		}
	}

	//------------------------------------------------------------------

	private void writeImageSequence(
		int		animationKind,
		File	directory,
		String	filenameStem,
		int		frameWidth,
		int		frameHeight,
		int		startFrameIndex,
		int		numFrames,
		int		fadeIn,
		int		fadeOut)
		throws AppException
	{
		// Initialise animation
		if (!initAnimation(animationKind, startFrameIndex))
			return;

		// Create output directory
		if (!directory.exists() && !directory.mkdirs())
			throw new FileException(ErrorId.FAILED_TO_CREATE_DIRECTORY, directory);

		// Initialise progress view
		IProgressView progressView = Task.getProgressView();
		progressView.setProgress(0, 0.0);

		// Get dimensions of image
		BufferedImage image = getImage();
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		// Create buffer for image's RGB data
		int[] rgbBuffer = null;
		if ((frameWidth > imageWidth) || (frameHeight > imageHeight) || (fadeIn > 0) || (fadeOut > 0))
			rgbBuffer = new int[imageWidth * imageHeight];

		// Get number of digits in filename
		int numDigits = NumberUtils.getNumDecDigitsInt(numFrames - 1);

		// Generate a sequence of images, writing each image to a file
		int frameIndex = 0;
		while (frameIndex < numFrames)
		{
			// Create pathname
			String filename = filenameStem + NumberUtils.uIntToDecString(frameIndex, numDigits, '0') +
																			 AppConstants.PNG_FILENAME_EXTENSION;
			File file = new File(directory, filename);

			// Update info in progress view
			progressView.setInfo(WRITING_STR, file);

			// Generate next image
			try
			{
				updateAnimation(startFrameIndex + frameIndex);
			}
			catch (InterruptedException e)
			{
				break;
			}

			// Wrap image in frame, if necessary
			image = getImage();
			if (rgbBuffer != null)
			{
				// Get array of RGB values for image
				image.getRGB(0, 0, imageWidth, imageHeight, rgbBuffer, 0, imageWidth);

				// Fade in or fade out
				double factor = 1.0;
				if (frameIndex < fadeIn)
					factor = (double)frameIndex / (double)fadeIn;
				else if (frameIndex >= numFrames - fadeOut)
					factor = (double)(numFrames - frameIndex - 1) / (double)fadeOut;
				if (factor < 1.0)
				{
					for (int i = 0; i < rgbBuffer.length; i++)
					{
						int rgb = 0;
						for (int shift = 0; shift < 24; shift += 8)
						{
							int value = (int)Math.round(factor * (double)(rgbBuffer[i] >> shift & 0xFF));
							rgb |= value << shift;
						}
						rgbBuffer[i] = rgb;
					}
				}

				// Create new image from RGB values
				BufferedImage frameImage = new BufferedImage(frameWidth, frameHeight, image.getType());
				frameImage.setRGB((frameWidth - imageWidth) / 2, (frameHeight - imageHeight) / 2,
								  imageWidth, imageHeight, rgbBuffer, 0, imageWidth);
				image = frameImage;
			}

			// Write image file
			PngOutputFile.write(file, image);

			// Increment frame index
			++frameIndex;

			// Update progress in progress view
			progressView.setProgress(0, (double)frameIndex / (double)numFrames);
		}
	}

	//------------------------------------------------------------------

	private Edit onUndo()
	{
		Edit edit = editList.removeUndo();
		if (edit != null)
		{
			edit.undo(this);
			updateView();
		}
		return null;
	}

	//------------------------------------------------------------------

	private Edit onRedo()
	{
		Edit edit = editList.removeRedo();
		if (edit != null)
		{
			edit.redo(this);
			updateView();
		}
		return null;
	}

	//------------------------------------------------------------------

	private Edit onClearEditList()
	{
		String[] optionStrs = Utils.getOptionStrings(AppConstants.CLEAR_STR);
		if (JOptionPane.showOptionDialog(PatternGeneratorApp.INSTANCE.getMainWindow(), CLEAR_EDIT_LIST_STR,
										 PatternGeneratorApp.SHORT_NAME, JOptionPane.OK_CANCEL_OPTION,
										 JOptionPane.QUESTION_MESSAGE, null, optionStrs,
										 optionStrs[1]) == JOptionPane.OK_OPTION)
		{
			editList.clear();
			System.gc();
		}
		return null;
	}

	//------------------------------------------------------------------

	private Edit onEditPatternParameters()
		throws AppException
	{
		Edit edit = null;
		PatternParams oldParams = getParameters();
		if (editParameters())
		{
			updateView();
			edit = new Edit.Parameters(oldParams, getParameters());
		}
		return edit;
	}

	//------------------------------------------------------------------

	private Edit onEditDescription()
	{
		Edit edit = null;
		String oldDescription = getDescription();
		String description = DescriptionDialog.showDialog(getWindow(), getPatternKind().getName(), oldDescription);
		if (description != null)
		{
			setDescription(description);
			edit = new Edit.Description(oldDescription, description);
		}
		return edit;
	}

	//------------------------------------------------------------------

	private Edit onRegeneratePatternWithNewSeed()
		throws AppException
	{
		long oldSeed = getParameters().getSeed();
		setSeed(PatternGeneratorApp.INSTANCE.getNextRandomSeed());
		updateView();
		return new Edit.Seed(oldSeed, getParameters().getSeed());
	}

	//------------------------------------------------------------------

	private Edit onStartSlideShow()
	{
		// Display dialog for slide-show parameters
		if (SlideShowParamsDialog.showDialog(getWindow()))
		{
			// Hide main window
			getWindow().setVisible(false);

			// Create dialog
			sequenceDialog = new SequenceDialog(getWindow(), this);

			// Start slide-show task
			absoluteFrameIndex = 0;
			paused = false;
			playing = true;
			Task.setCancelled(false);
			int numThreads = AppConfig.INSTANCE.getNumSlideShowThreads(getPatternKind());
			if (numThreads == 0)
				numThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
			new Thread(new SlideShow(numThreads, SlideShowParamsDialog.getInterval())).start();

			// Show dialog
			sequenceDialog.setVisible(true);

			// Show main window
			getWindow().setVisible(true);

			// Update view with last image from slide show
			updateView();
		}

		return null;
	}

	//------------------------------------------------------------------

	private Edit onStartAnimation()
		throws AppException
	{
		// Select animation
		AnimationParams animationParams = selectAnimation(false);

		// Run animation on a temporary document
		if (animationParams != null)
			createDefinitionDocument(true).doAnimation(animationParams);

		return null;
	}

	//------------------------------------------------------------------

	private Edit onShowImageRenderingTime()
	{
		if (renderingTimeDialog == null)
			renderingTimeDialog = RenderingTimeDialog.showDialog(getWindow());
		return null;
	}

	//------------------------------------------------------------------

	private Edit onOptimiseAnimation()
		throws AppException
	{
		optimiseAnimation();
		return null;
	}

	//------------------------------------------------------------------

	private Edit onResizeWindowToImage()
	{
		PatternView view = getView();
		if (view != null)
		{
			BufferedImage image = view.getImage();
			if (image != null)
				view.setPreferredViewportSize(new Dimension(image.getWidth(), image.getHeight()));
		}
		return null;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: COMMANDS


	enum Command
		implements Action
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		// Commands

		UNDO
		(
			"undo",
			"Undo",
			KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK)
		),

		REDO
		(
			"redo",
			"Redo",
			KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK)
		),

		CLEAR_EDIT_LIST
		(
			"clearEditList",
			"Clear edit history" + AppConstants.ELLIPSIS_STR
		),

		EDIT_PATTERN_PARAMETERS
		(
			"editPatternParameters",
			"Edit pattern parameters" + AppConstants.ELLIPSIS_STR,
			KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK)
		),

		EDIT_DESCRIPTION
		(
			"editDescription",
			"Edit description" + AppConstants.ELLIPSIS_STR,
			KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK)
		),

		REGENERATE_PATTERN_WITH_NEW_SEED
		(
			"regeneratePatternWithNewSeed",
			"Regenerate pattern with new seed",
			KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0)
		),

		SHOW_IMAGE_RENDERING_TIME
		(
			"showImageRenderingTime",
			"Show image rendering time",
			KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK)
		),

		START_SLIDE_SHOW
		(
			"startSlideShow",
			"Start slide show" + AppConstants.ELLIPSIS_STR,
			KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0)
		),

		START_ANIMATION
		(
			"startAnimation",
			"Start animation" + AppConstants.ELLIPSIS_STR,
			KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0)
		),

		OPTIMISE_ANIMATION
		(
			"optimiseAnimation",
			"Optimise animation" + AppConstants.ELLIPSIS_STR,
			KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.CTRL_DOWN_MASK)
		),

		RESIZE_WINDOW_TO_IMAGE
		(
			"resizeWindowToImage",
			"Resize window to image",
			KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0)
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	uk.blankaspect.ui.swing.action.Command	command;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Command(
			String	key)
		{
			command = new uk.blankaspect.ui.swing.action.Command(this);
			putValue(Action.ACTION_COMMAND_KEY, key);
		}

		//--------------------------------------------------------------

		private Command(
			String	key,
			String	name)
		{
			this(key);
			putValue(Action.NAME, name);
		}

		//--------------------------------------------------------------

		private Command(
			String		key,
			String		name,
			KeyStroke	acceleratorKey)
		{
			this(key, name);
			putValue(Action.ACCELERATOR_KEY, acceleratorKey);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static void setAllEnabled(
			boolean	enabled)
		{
			for (Command command : values())
				command.setEnabled(enabled);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Action interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void addPropertyChangeListener(
			PropertyChangeListener	listener)
		{
			command.addPropertyChangeListener(listener);
		}

		//--------------------------------------------------------------

		@Override
		public Object getValue(
			String	key)
		{
			return command.getValue(key);
		}

		//--------------------------------------------------------------

		@Override
		public boolean isEnabled()
		{
			return command.isEnabled();
		}

		//--------------------------------------------------------------

		@Override
		public void putValue(
			String	key,
			Object	value)
		{
			command.putValue(key, value);
		}

		//--------------------------------------------------------------

		@Override
		public void removePropertyChangeListener(
			PropertyChangeListener	listener)
		{
			command.removePropertyChangeListener(listener);
		}

		//--------------------------------------------------------------

		@Override
		public void setEnabled(
			boolean	enabled)
		{
			command.setEnabled(enabled);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void actionPerformed(
			ActionEvent	event)
		{
			PatternDocument document = PatternGeneratorApp.INSTANCE.getDocument();
			if (document != null)
				document.executeCommand(this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void execute()
		{
			actionPerformed(null);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// ENUMERATION: ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		FAILED_TO_OPEN_FILE
		("Failed to open the file."),

		FAILED_TO_CLOSE_FILE
		("Failed to close the file."),

		ERROR_WRITING_FILE
		("An error occurred when writing the file."),

		FILE_ACCESS_NOT_PERMITTED
		("Access to the file was not permitted."),

		FAILED_TO_CREATE_DIRECTORY
		("Failed to create the directory."),

		FAILED_TO_CREATE_TEMPORARY_FILE
		("Failed to create a temporary file."),

		FAILED_TO_DELETE_FILE
		("Failed to delete the existing file."),

		FAILED_TO_RENAME_FILE
		("Failed to rename the temporary file to the specified filename."),

		UNEXPECTED_DOCUMENT_FORMAT
		("The document does not have the expected format."),

		NO_VERSION_NUMBER
		("The document does not have a version number."),

		INVALID_VERSION_NUMBER
		("The version number of the document is invalid."),

		UNSUPPORTED_DOCUMENT_VERSION
		("The version of the document (%1) is not supported by this version of "
			+ PatternGeneratorApp.SHORT_NAME + "."),

		NO_ATTRIBUTE
		("The required attribute is missing."),

		INVALID_ATTRIBUTE
		("The attribute is invalid."),

		NO_PATTERN_ELEMENT
		("The document has no pattern element."),

		MULTIPLE_PATTERN_ELEMENTS
		("The document has more than one pattern element."),

		NO_PARAMETERS
		("The document has no parameters."),

		NOT_ENOUGH_MEMORY_TO_PERFORM_COMMAND
		("There was not enough memory to perform the command.\n" +
			"Clearing the list of undo/redo actions may make more memory available.");

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
//  Member records
////////////////////////////////////////////////////////////////////////


	// RECORD: FILE INFORMATION


	public record FileInfo(
		File			file,
		DocumentKind	documentKind,
		boolean			hasParameters)
	{

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static FileInfo of(
			File			file,
			DocumentKind	documentKind)
		{
			return new FileInfo(file, documentKind, false);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// RECORD: ANIMATION PARAMETERS


	public record AnimationParams(
		int		animationKind,
		double	frameRate,
		int		startFrameIndex)
	{

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static AnimationParams of(
			int	animationKind)
		{
			return new AnimationParams(animationKind, 0, 0);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: EDIT


	private static abstract class Edit
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Edit()
		{
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		protected abstract void undo(
			PatternDocument	document);

		//--------------------------------------------------------------

		protected abstract void redo(
			PatternDocument	document);

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Member classes : non-inner classes
	////////////////////////////////////////////////////////////////////


		// CLASS: DESCRIPTION EDIT


		private static class Description
			extends Edit
		{

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	String	oldDescription;
			private	String	newDescription;

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Description(
				String	oldDescription,
				String	newDescription)
			{
				this.oldDescription = oldDescription;
				this.newDescription = newDescription;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			protected void undo(
				PatternDocument	document)
			{
				document.setDescription(oldDescription);
			}

			//----------------------------------------------------------

			@Override
			protected void redo(
				PatternDocument	document)
			{
				document.setDescription(newDescription);
			}

			//----------------------------------------------------------

		}

		//==============================================================


		// CLASS: SEED EDIT


		private static class Seed
			extends Edit
		{

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	long	oldSeed;
			private	long	newSeed;

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Seed(
				long	oldSeed,
				long	newSeed)
			{
				this.oldSeed = oldSeed;
				this.newSeed = newSeed;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			protected void undo(
				PatternDocument	document)
			{
				try
				{
					document.setSeed(oldSeed);
				}
				catch (AppException e)
				{
					PatternGeneratorApp.INSTANCE.showErrorMessage(PatternGeneratorApp.SHORT_NAME, e);
				}
			}

			//----------------------------------------------------------

			@Override
			protected void redo(
				PatternDocument	document)
			{
				try
				{
					document.setSeed(newSeed);
				}
				catch (AppException e)
				{
					PatternGeneratorApp.INSTANCE.showErrorMessage(PatternGeneratorApp.SHORT_NAME, e);
				}
			}

			//----------------------------------------------------------

		}

		//==============================================================


		// CLASS: PARAMETERS EDIT


		private static class Parameters
			extends Edit
		{

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	PatternParams	oldParams;
			private	PatternParams	newParams;

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Parameters(
				PatternParams	oldParams,
				PatternParams	newParams)
			{
				this.oldParams = oldParams;
				this.newParams = newParams;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			protected void undo(
				PatternDocument	document)
			{
				try
				{
					document.setParameters(oldParams);
				}
				catch (AppException e)
				{
					PatternGeneratorApp.INSTANCE.showErrorMessage(PatternGeneratorApp.SHORT_NAME, e);
				}
			}

			//----------------------------------------------------------

			@Override
			protected void redo(
				PatternDocument	document)
			{
				try
				{
					document.setParameters(newParams);
				}
				catch (AppException e)
				{
					PatternGeneratorApp.INSTANCE.showErrorMessage(PatternGeneratorApp.SHORT_NAME, e);
				}
			}

			//----------------------------------------------------------

		}

		//==============================================================

	}

	//==================================================================


	// CLASS: EDIT LIST


	private static class EditList
		extends LinkedList<Edit>
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int	maxLength;
		private	int	currentIndex;
		private	int	unchangedIndex;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private EditList()
		{
			maxLength = AppConfig.INSTANCE.getMaxEditListLength();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void clear()
		{
			super.clear();
			unchangedIndex = currentIndex = 0;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private Edit removeUndo()
		{
			return canUndo() ? get(--currentIndex) : null;
		}

		//--------------------------------------------------------------

		private Edit removeRedo()
		{
			return canRedo() ? get(currentIndex++) : null;
		}

		//--------------------------------------------------------------

		private boolean canUndo()
		{
			return (currentIndex > 0);
		}

		//--------------------------------------------------------------

		private boolean canRedo()
		{
			return (currentIndex < size());
		}

		//--------------------------------------------------------------

		private boolean isChanged()
		{
			return (currentIndex != unchangedIndex);
		}

		//--------------------------------------------------------------

		private void addEdit(
			Edit	edit)
		{
			// Remove redos
			while (size() > currentIndex)
				removeLast();

			// Preserve changed status if unchanged state cannot be recovered
			if (unchangedIndex > currentIndex)
				unchangedIndex = -1;

			// Remove oldest edits while list is full
			while (size() >= maxLength)
			{
				removeFirst();
				if (--unchangedIndex < 0)
					unchangedIndex = -1;
				if (--currentIndex < 0)
					currentIndex = 0;
			}

			// Add new edit
			add(edit);
			++currentIndex;
		}

		//--------------------------------------------------------------

		private void reset()
		{
			while (size() > currentIndex)
				removeLast();

			unchangedIndex = currentIndex;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: RENDERING TIME


	private static class RenderingTime
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	long	numPixels;
		private	long	nanoseconds;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private RenderingTime()
		{
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void add(
			long	numPixels,
			long	nanoseconds)
		{
			this.numPixels += numPixels;
			this.nanoseconds += nanoseconds;
		}

		//--------------------------------------------------------------

		public void reset()
		{
			numPixels = 0;
			nanoseconds = 0;
		}

		//--------------------------------------------------------------

		public double getMeanNanosecondsPerPixel()
		{
			return (numPixels == 0) ? 0.0 : (double)nanoseconds / (double)numPixels;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: SLIDE SHOW


	private class SlideShow
		implements Runnable
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int				numThreads;
		private	int				interval;
		private	List<Result>	results;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private SlideShow(
			int	numThreads,
			int	interval)
		{
			this.numThreads = numThreads;
			this.interval = interval;
			results = new ArrayList<>();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Runnable interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void run()
		{
			long inTaskIndex = 0;
			long outTaskIndex = 0;
			ExecutorService executor = Executors.newFixedThreadPool(numThreads);
			long endTime = 0;
			while (true)
			{
				// Test whether task has been cancelled
				if (Task.isCancelled())
					break;

				// Submit tasks
				while (inTaskIndex - outTaskIndex < numThreads)
				{
					try
					{
						long index = inTaskIndex;
						executor.execute(() ->
						{
							PatternParams params = getParameters().clone();
							params.setSeed(PatternGeneratorApp.INSTANCE.getNextRandomSeed());
							try
							{
								PatternImage patternImage = createPatternImage(params);
								patternImage.renderImage();
								addResult(new Result(index, params, patternImage));
							}
							catch (InterruptedException e)
							{
								// ignore
							}
						});
						++inTaskIndex;
					}
					catch (RejectedExecutionException e)
					{
						// ignore
					}
				}

				// Update end time while paused
				if (paused)
					endTime = System.currentTimeMillis();

				// Get next pattern image from output list; update document and view
				else if (System.currentTimeMillis() >= endTime)
				{
					if (!results.isEmpty())
					{
						Result result = findResult(outTaskIndex);
						if (result != null)
						{
							// Increment output task index
							++outTaskIndex;

							// Add change of seed to list of edits
							long oldSeed = getParameters().getSeed();
							editList.addEdit(new Edit.Seed(oldSeed, result.params.getSeed()));

							// Set parameters and pattern image
							setParametersAndPatternImage(result.params, result.patternImage);

							// Increment frame index
							++absoluteFrameIndex;

							// Update view
							SwingUtilities.invokeLater(PatternDocument.this::updatePlayView);

							// Increment end time
							if (endTime == 0)
								endTime = System.currentTimeMillis();
							endTime += interval;
						}
					}
				}
			}

			// Shut down executor
			executor.shutdown();

			// Wait for tasks to terminate
			try
			{
				executor.awaitTermination(30, TimeUnit.SECONDS);
			}
			catch (InterruptedException e)
			{
				// ignore
			}
			finally
			{
				if (!executor.isTerminated())
					executor.shutdownNow();
			}

			// Enable commands
			playing = false;
		}

		//--------------------------------------------------------------

		private synchronized void addResult(
			Result	result)
		{
			results.add(result);
		}

		//--------------------------------------------------------------

		private synchronized Result findResult(
			long	index)
		{
			for (int i = 0; i < results.size(); i++)
			{
				Result result = results.get(i);
				if (result.index == index)
				{
					results.remove(i);
					return result;
				}
			}
			return null;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Member records
	////////////////////////////////////////////////////////////////////


		// RECORD: RESULT


		private record Result(
			long          index,
			PatternParams params,
			PatternImage  patternImage)
		{ }

		//==============================================================

	}

	//==================================================================

}

//----------------------------------------------------------------------
