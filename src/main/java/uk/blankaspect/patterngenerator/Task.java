/*====================================================================*\

Task.java

Task class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.io.File;

import java.util.Set;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.TaskCancelledException;

//----------------------------------------------------------------------


// TASK CLASS


abstract class Task
	extends uk.blankaspect.common.misc.Task
{

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// READ DOCUMENT TASK CLASS


	public static class ReadDocument
		extends Task
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public ReadDocument(PatternDocument.FileInfo fileInfo,
							PatternDocument[]        result)
		{
			this.fileInfo = fileInfo;
			this.result = result;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Runnable interface
	////////////////////////////////////////////////////////////////////

		public void run()
		{
			// Perform task
			try
			{
				result[0] = PatternDocument.read(fileInfo);
			}
			catch (TaskCancelledException e)
			{
				// ignore
			}
			catch (AppException e)
			{
				setException(e, false);
			}

			// Remove thread
			removeThread();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	PatternDocument.FileInfo	fileInfo;
		private	PatternDocument[]			result;

	}

	//==================================================================


	// WRITE DOCUMENT TASK CLASS


	public static class WriteDocument
		extends Task
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public WriteDocument(PatternDocument          document,
							 PatternDocument.FileInfo fileInfo)
		{
			this.document = document;
			this.fileInfo = fileInfo;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Runnable interface
	////////////////////////////////////////////////////////////////////

		public void run()
		{
			// Perform task
			try
			{
				document.write(fileInfo);
			}
			catch (AppException e)
			{
				setException(e, false);
			}

			// Remove thread
			removeThread();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	PatternDocument				document;
		private	PatternDocument.FileInfo	fileInfo;

	}

	//==================================================================


	// EXPORT IMAGE TASK CLASS


	public static class ExportImage
		extends Task
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public ExportImage(PatternDocument document,
						   File            file)
		{
			this.document = document;
			this.file = file;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Runnable interface
	////////////////////////////////////////////////////////////////////

		public void run()
		{
			// Perform task
			try
			{
				document.writeImage(file);
			}
			catch (AppException e)
			{
				setException(e, false);
			}

			// Remove thread
			removeThread();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	PatternDocument	document;
		private	File			file;

	}

	//==================================================================


	// EXPORT IMAGE SEQUENCE TASK CLASS


	public static class ExportImageSequence
		extends Task
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public ExportImageSequence(PatternDocument     document,
								   ImageSequenceParams params)
		{
			this.document = document;
			this.params = params;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Runnable interface
	////////////////////////////////////////////////////////////////////

		public void run()
		{
			// Perform task
			try
			{
				document.writeImageSequence(params);
			}
			catch (AppException e)
			{
				setException(e, false);
			}

			// Remove thread
			removeThread();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	PatternDocument		document;
		private	ImageSequenceParams	params;

	}

	//==================================================================


	// EXPORT SVG TASK CLASS


	public static class ExportSvg
		extends Task
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public ExportSvg(PatternDocument document,
						 File            file)
		{
			this.document = document;
			this.file = file;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Runnable interface
	////////////////////////////////////////////////////////////////////

		public void run()
		{
			// Perform task
			try
			{
				document.writeSvg(file);
			}
			catch (AppException e)
			{
				setException(e, false);
			}

			// Remove thread
			removeThread();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	PatternDocument	document;
		private	File			file;

	}

	//==================================================================


	// GENERATE PATTERN TASK CLASS


	public static class GeneratePattern
		extends Task
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public GeneratePattern(PatternDocument document)
		{
			this.document = document;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Runnable interface
	////////////////////////////////////////////////////////////////////

		public void run()
		{
			// Perform task
			try
			{
				document.generatePattern();
			}
			catch (TaskCancelledException e)
			{
				// ignore
			}
			catch (AppException e)
			{
				setException(e, false);
			}

			// Remove thread
			removeThread();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	PatternDocument	document;

	}

	//==================================================================


	// SET BRIGHTNESS RANGE TASK CLASS


	public static class SetBrightnessRange
		extends Task
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public SetBrightnessRange(Pattern1Document                 document,
								  Set<Pattern1Image.AnimationKind> animationKinds,
								  int                              startFrameIndex,
								  int                              numFrames)
		{
			this.document = document;
			this.animationKinds = animationKinds;
			this.startFrameIndex = startFrameIndex;
			this.numFrames = numFrames;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Runnable interface
	////////////////////////////////////////////////////////////////////

		public void run()
		{
			// Perform task
			try
			{
				document.setBrightnessRange(animationKinds, startFrameIndex, numFrames);
			}
			catch (TaskCancelledException e)
			{
				// ignore
			}
			catch (AppException e)
			{
				setException(e, false);
			}

			// Remove thread
			removeThread();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Pattern1Document					document;
		private	Set<Pattern1Image.AnimationKind>	animationKinds;
		private	int									startFrameIndex;
		private	int									numFrames;

	}

	//==================================================================


	// INITIALISE ANIMATION TASK CLASS


	public static class InitAnimation
		extends Task
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public InitAnimation(Pattern2Document document,
							 int              startFrameIndex)
		{
			this.document = document;
			this.startFrameIndex = startFrameIndex;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Runnable interface
	////////////////////////////////////////////////////////////////////

		public void run()
		{
			// Perform task
			try
			{
				document.initAnimation(startFrameIndex);
			}
			catch (TaskCancelledException e)
			{
				setException(e, false);
			}

			// Remove thread
			removeThread();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Pattern2Document	document;
		private	int					startFrameIndex;

	}

	//==================================================================


	// WRITE CONFIGURATION TASK CLASS


	public static class WriteConfig
		extends Task
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public WriteConfig(File file)
		{
			this.file = file;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Runnable interface
	////////////////////////////////////////////////////////////////////

		public void run()
		{
			// Perform task
			try
			{
				AppConfig.INSTANCE.write(file);
			}
			catch (AppException e)
			{
				setException(e, false);
			}

			// Remove thread
			removeThread();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	File	file;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private Task()
	{
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
