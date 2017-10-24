/*====================================================================*\

Pattern1Document.java

Pattern 1 document class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.TaskCancelledException;

import uk.blankaspect.common.gui.IProgressView;

import uk.blankaspect.common.misc.DoubleRange;

import uk.blankaspect.common.xml.Attribute;
import uk.blankaspect.common.xml.XmlParseException;
import uk.blankaspect.common.xml.XmlWriter;

//----------------------------------------------------------------------


// PATTERN 1 DOCUMENT CLASS


class Pattern1Document
	extends PatternDocument
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		double	MIN_ANIMATION_RATE		= 0.01;
	public static final		double	MAX_ANIMATION_RATE		= 60.0;
	public static final		double	DEFAULT_ANIMATION_RATE	= 10.0;

	private static final	String	BRIGHTNESS_RANGE_STR	= "Determining minimum and maximum " +
																"brightness " + AppConstants.ELLIPSIS_STR;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public Pattern1Document(Pattern1Params params)
		throws AppException
	{
		this(null, params);
	}

	//------------------------------------------------------------------

	public Pattern1Document(File           file,
							Pattern1Params params)
		throws AppException
	{
		// Call superclass constructor
		super(file, DocumentKind.PARAMETERS);

		// Initialise instance fields
		this.params = params;
		animationKinds = EnumSet.allOf(Pattern1Image.AnimationKind.class);

		// Generate pattern from parameters
		try
		{
			generate();
		}
		catch (TaskCancelledException e)
		{
			// ignore
		}
	}

	//------------------------------------------------------------------

	public Pattern1Document(File    file,
							Element element)
		throws XmlParseException
	{
		// Call superclass constructor
		super(file, DocumentKind.DEFINITION);

		// Initialise instance fields
		animationKinds = EnumSet.allOf(Pattern1Image.AnimationKind.class);

		// Create image from pattern element
		try
		{
			patternImage = new Pattern1Image(this, element);
		}
		catch (XmlParseException e)
		{
			throw new XmlParseException(e, file);
		}

		// Render image
		try
		{
			patternImage.renderImage();
		}
		catch (InterruptedException e)
		{
			// ignore
		}
	}

	//------------------------------------------------------------------

	private Pattern1Document(File          file,
							 Pattern1Image patternImage,
							 boolean       temporary)
	{
		// Call superclass constructor
		super(file, DocumentKind.DEFINITION, temporary);

		// Initialise instance fields
		this.patternImage = patternImage;
		animationKinds = EnumSet.allOf(Pattern1Image.AnimationKind.class);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public PatternKind getPatternKind()
	{
		return PatternKind.PATTERN1;
	}

	//------------------------------------------------------------------

	@Override
	public PatternParams getParameters()
	{
		return params;
	}

	//------------------------------------------------------------------

	@Override
	public void setParameters(PatternParams params)
		throws AppException
	{
		this.params = (Pattern1Params)params;
		generate();
	}

	//------------------------------------------------------------------

	@Override
	public void setParametersAndPatternImage(PatternParams params,
											 PatternImage  patternImage)
	{
		this.params = (Pattern1Params)params;
		this.patternImage = (Pattern1Image)patternImage;
	}

	//------------------------------------------------------------------

	@Override
	public boolean hasImage()
	{
		return (getImage() != null);
	}

	//------------------------------------------------------------------

	@Override
	public BufferedImage getImage()
	{
		return ((patternImage == null) ? null : patternImage.getImage());
	}

	//------------------------------------------------------------------

	@Override
	public BufferedImage getExportImage()
	{
		return ((patternImage == null) ? null : patternImage.getExportImage());
	}

	//------------------------------------------------------------------

	@Override
	public Pattern1Image createPatternImage(PatternParams params)
		throws InterruptedException
	{
		return new Pattern1Image(this, (Pattern1Params)params);
	}

	//------------------------------------------------------------------

	@Override
	public void generatePattern()
		throws TaskCancelledException
	{
		// Initialise progress view
		IProgressView progressView = Task.getProgressView();
		if (progressView != null)
		{
			progressView.setInfo(RENDERING_STR);
			progressView.setProgress(0, -1.0);
		}

		// Create and render pattern image
		try
		{
			patternImage = createPatternImage(params);
			patternImage.renderImage();
		}
		catch (InterruptedException e)
		{
			throw new TaskCancelledException();
		}
	}

	//------------------------------------------------------------------

	@Override
	public boolean editParameters()
		throws AppException
	{
		Pattern1Params newParams = Pattern1ParamsDialog.showDialog(getWindow(),
																   getParameterTitleString(), params);
		if (newParams == null)
			return false;
		setParameters(newParams);
		return true;
	}

	//------------------------------------------------------------------

	@Override
	public void setSeed(long seed)
		throws AppException
	{
		params.setSeed(seed);
		generate();
	}

	//------------------------------------------------------------------

	@Override
	public PatternDocument createDefinitionDocument(boolean temporary)
		throws AppException
	{
		return ((patternImage == null) ? null
									   : new Pattern1Document(null, patternImage.clone(), temporary));
	}

	//------------------------------------------------------------------

	@Override
	public void write(XmlWriter writer)
		throws IOException
	{
		List<Attribute> attributes = new ArrayList<>();
		appendCommonAttributes(attributes);
		writer.writeElementStart(getElementName(), attributes, 0, true, true);
		patternImage.write(writer, XmlWriter.INDENT_INCREMENT);
		writer.writeElementEnd(getElementName(), 0);
	}

	//------------------------------------------------------------------

	@Override
	public boolean canExportAsSvg()
	{
		return false;
	}

	//------------------------------------------------------------------

	@Override
	public void writeSvgElements(XmlWriter writer,
								 int       indent)
		throws IOException
	{
		// do nothing
	}

	//------------------------------------------------------------------

	@Override
	protected boolean hasParameters()
	{
		return (params != null);
	}

	//------------------------------------------------------------------

	@Override
	protected String getDescription()
	{
		return ((params != null) ? params.getDescription()
								 : (patternImage != null) ? patternImage.getDescription()
														  : null);
	}

	//------------------------------------------------------------------

	@Override
	protected void setDescription(String description)
	{
		if (params != null)
			params.setDescription(description);
		if (patternImage != null)
			patternImage.setDescription(description);
	}

	//------------------------------------------------------------------

	@Override
	protected int getNumAnimationKinds()
	{
		int numAnimationKinds = Pattern1Image.AnimationKind.values().length;
		if (!AppConfig.INSTANCE.isPattern1PhaseAnimation())
			--numAnimationKinds;
		return numAnimationKinds;
	}

	//------------------------------------------------------------------

	@Override
	protected AnimationParams selectAnimation(boolean imageSequence)
	{
		AnimationParams animationParams =
				imageSequence
					? Pattern1AnimationKindsDialog.showDialog(getWindow(),
															  patternImage.getSupportedAnimationKinds(),
															  animationKinds)
					: Pattern1AnimationParamsDialog.showDialog(getWindow(),
															   patternImage.getSupportedAnimationKinds(),
															   animationKinds);
		if (animationParams != null)
			animationKinds = Pattern1Image.AnimationKind.bitFieldToSet(animationParams.animationKind);
		return animationParams;
	}

	//------------------------------------------------------------------

	@Override
	protected boolean canOptimiseAnimation()
	{
		return true;
	}

	//------------------------------------------------------------------

	@Override
	protected void optimiseAnimation()
	{
		Pattern1AnimationOptimisationDialog.showDialog(getWindow(), this,
													   patternImage.getSupportedAnimationKinds());
	}

	//------------------------------------------------------------------

	@Override
	protected boolean initAnimation(int animationId,
									int startFrameIndex)
	{
		animationKinds = Pattern1Image.AnimationKind.bitFieldToSet(animationId);
		patternImage.initAnimation(animationKinds, startFrameIndex);
		return true;
	}

	//------------------------------------------------------------------

	@Override
	protected void updateAnimation(int frameIndex)
		throws InterruptedException
	{
		for (Pattern1Image.AnimationKind animationKind : animationKinds)
		{
			switch (animationKind)
			{
				case PHASE:
					patternImage.updateSourcePhases(frameIndex);
					break;

				case POSITION:
					patternImage.updateSourcePositions(frameIndex);
					break;

				case ORIENTATION:
					patternImage.updateSourceOrientations(frameIndex);
					break;
			}
		}
		patternImage.renderImage();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public boolean isBrightnessRange()
	{
		return patternImage.isBrightnessRange();
	}

	//------------------------------------------------------------------

	public boolean isBrightnessRange(Set<Pattern1Image.AnimationKind> animationKinds,
									 int                              startFrameIndex)
	{
		return patternImage.isBrightnessRange(animationKinds, startFrameIndex);
	}

	//------------------------------------------------------------------

	public void removeBrightnessRange(Set<Pattern1Image.AnimationKind> animationKinds,
									  int                              startFrameIndex)
	{
		patternImage.removeBrightnessRange(animationKinds, startFrameIndex);
	}

	//------------------------------------------------------------------

	public void setBrightnessRange(Set<Pattern1Image.AnimationKind> animationKinds,
								   int                              startFrameIndex,
								   int                              numFrames)
		throws AppException
	{
		// Get brightness range by partially rendering a sequence of images on a temporary document
		DoubleRange brightnessRange = ((Pattern1Document)createDefinitionDocument(true)).
										getBrightnessRange(animationKinds, startFrameIndex, numFrames);

		// Add brightness range to map
		if (brightnessRange != null)
			patternImage.putBrightnessRange(animationKinds, startFrameIndex, brightnessRange);
	}

	//------------------------------------------------------------------

	public long[] getBrightnessRangeKeys()
	{
		return patternImage.getBrightnessRangeKeys();
	}

	//------------------------------------------------------------------

	private void generate()
		throws AppException
	{
		patternImage = null;
		if (Task.getNumThreads() == 0)
			TaskProgressDialog.showDialog(getWindow(), GENERATE_STR + getPatternKind().getName(),
										  new Task.GeneratePattern(this));
		else
		{
			Task.setCancelled(false);
			generatePattern();
		}
	}

	//------------------------------------------------------------------

	private DoubleRange getBrightnessRange(Set<Pattern1Image.AnimationKind> animationKinds,
										   int                              startFrameIndex,
										   int                              numFrames)
	{
		// Initialise progress view
		IProgressView progressView = Task.getProgressView();
		progressView.setInfo(BRIGHTNESS_RANGE_STR);
		progressView.setProgress(0, 0.0);

		// Save rendering mode
		Pattern1Image.RenderingMode oldRenderingMode = patternImage.getRenderingMode();

		// Set rendering mode
		patternImage.setRenderingMode(Pattern1Image.RenderingMode.PARTIAL);

		// Initialise animation
		initAnimation(Pattern1Image.AnimationKind.setToBitField(animationKinds), startFrameIndex);

		// Generate sequence of images
		DoubleRange brightnessRange = null;
		try
		{
			int frameIndex = 0;
			while (frameIndex < numFrames)
			{
				// Generate next image
				updateAnimation(startFrameIndex + frameIndex);

				// Increment frame index
				++frameIndex;

				// Update progress in progress view
				progressView.setProgress(0, (double)frameIndex / (double)numFrames);
			}

			// Get brightness range
			brightnessRange = new DoubleRange(patternImage.getMinimumBrightness(),
											  patternImage.getMaximumBrightness());
		}
		catch (InterruptedException e)
		{
			// do nothing
		}

		// Restore rendering mode
		patternImage.setRenderingMode(oldRenderingMode);

		return brightnessRange;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	private	Pattern1Params						params;
	private	Pattern1Image						patternImage;
	private	Set<Pattern1Image.AnimationKind>	animationKinds;

}

//----------------------------------------------------------------------
