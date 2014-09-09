/*====================================================================*\

Pattern2Document.java

Pattern 2 document class.

\*====================================================================*/


// IMPORTS


import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import uk.org.blankaspect.exception.AppException;
import uk.org.blankaspect.exception.TaskCancelledException;

import uk.org.blankaspect.gui.ProgressView;

import uk.org.blankaspect.xml.Attribute;
import uk.org.blankaspect.xml.XmlParseException;
import uk.org.blankaspect.xml.XmlWriter;

//----------------------------------------------------------------------


// PATTERN 2 DOCUMENT CLASS


class Pattern2Document
    extends PatternDocument
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    private static final    String  INIT_ANIMATION_STR  = "Initialise animation";
    private static final    String  FAST_FORWARDING_STR = "Fast-forwarding to frame ";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    public Pattern2Document( Pattern2Params params )
        throws AppException
    {
        this( null, params );
    }

    //------------------------------------------------------------------

    public Pattern2Document( File           file,
                             Pattern2Params params )
        throws AppException
    {
        // Call superclass constructor
        super( file, DocumentKind.PARAMETERS );

        // Initialise instance variables
        this.params = params;

        // Generate pattern from parameters
        try
        {
            generate( );
        }
        catch ( TaskCancelledException e )
        {
            // ignore
        }
    }

    //------------------------------------------------------------------

    public Pattern2Document( File    file,
                             Element element )
        throws XmlParseException
    {
        // Call superclass constructor
        super( file, DocumentKind.DEFINITION );

        // Create image from pattern element
        try
        {
            patternImage = new Pattern2Image( this, element );
        }
        catch ( XmlParseException e )
        {
            throw new XmlParseException( e, file );
        }

        // Render image
        try
        {
            patternImage.renderImage( );
        }
        catch ( InterruptedException e )
        {
            // ignore
        }
    }

    //------------------------------------------------------------------

    private Pattern2Document( File          file,
                              Pattern2Image patternImage,
                              boolean       temporary )
    {
        // Call superclass constructor
        super( file, DocumentKind.DEFINITION, temporary );

        // Initialise instance variables
        this.patternImage = patternImage;
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

    @Override
    public PatternKind getPatternKind( )
    {
        return PatternKind.PATTERN2;
    }

    //------------------------------------------------------------------

    @Override
    public PatternParams getParameters( )
    {
        return params;
    }

    //------------------------------------------------------------------

    @Override
    public void setParameters( PatternParams params )
        throws AppException
    {
        this.params = (Pattern2Params)params;
        generate( );
    }

    //------------------------------------------------------------------

    @Override
    public void setParametersAndPatternImage( PatternParams params,
                                              PatternImage  patternImage )
    {
        this.params = (Pattern2Params)params;
        this.patternImage = (Pattern2Image)patternImage;
    }

    //------------------------------------------------------------------

    @Override
    public boolean hasImage( )
    {
        return ( getImage( ) != null );
    }

    //------------------------------------------------------------------

    @Override
    public BufferedImage getImage( )
    {
        return ( (patternImage == null) ? null : patternImage.getImage( ) );
    }

    //------------------------------------------------------------------

    @Override
    public Pattern2Image createPatternImage( PatternParams params )
        throws InterruptedException
    {
        return new Pattern2Image( this, (Pattern2Params)params );
    }

    //------------------------------------------------------------------

    @Override
    public void generatePattern( )
        throws TaskCancelledException
    {
        // Initialise progress view
        ProgressView progressView = Task.getProgressView( );
        if ( progressView != null )
        {
            progressView.setInfo( RENDERING_STR );
            progressView.setProgress( 0, -1.0 );
        }

        // Create and render pattern image
        try
        {
            patternImage = createPatternImage( params );
            patternImage.renderImage( );
        }
        catch ( InterruptedException e )
        {
            throw new TaskCancelledException( );
        }
    }

    //------------------------------------------------------------------

    @Override
    public boolean editParameters( )
        throws AppException
    {
        Pattern2Params newParams = Pattern2ParamsDialog.showDialog( getWindow( ),
                                                                    getParameterTitleString( ), params );
        if ( newParams == null )
            return false;
        setParameters( newParams );
        return true;
    }

    //------------------------------------------------------------------

    @Override
    public void setSeed( long seed )
        throws AppException
    {
        params.setSeed( seed );
        generate( );
    }

    //------------------------------------------------------------------

    @Override
    public PatternDocument createDefinitionDocument( boolean temporary )
        throws AppException
    {
        return ( (patternImage == null) ? null
                                        : new Pattern2Document( null, patternImage.clone( ), temporary ) );
    }

    //------------------------------------------------------------------

    @Override
    public void write( XmlWriter writer )
        throws IOException
    {
        List<Attribute> attributes = new ArrayList<>( );
        appendCommonAttributes( attributes );
        writer.writeElementStart( getElementName( ), attributes, 0, true, true );
        patternImage.write( writer, XmlWriter.INDENT_INCREMENT );
        writer.writeElementEnd( getElementName( ), 0 );
    }

    //------------------------------------------------------------------

    @Override
    public boolean canExportAsSvg( )
    {
        return true;
    }

    //------------------------------------------------------------------

    @Override
    public void writeSvgElements( XmlWriter writer,
                                  int       indent )
        throws IOException
    {
        patternImage.writeSvgElements( writer, indent );
    }

    //------------------------------------------------------------------

    @Override
    protected boolean hasParameters( )
    {
        return ( params != null );
    }

    //------------------------------------------------------------------

    @Override
    protected String getDescription( )
    {
        return ( (params != null) ? params.getDescription( )
                                  : (patternImage != null) ? patternImage.getDescription( )
                                                           : null );
    }

    //------------------------------------------------------------------

    @Override
    protected void setDescription( String description )
    {
        if ( params != null )
            params.setDescription( description );
        if ( patternImage != null )
            patternImage.setDescription( description );
    }

    //------------------------------------------------------------------

    @Override
    protected int getNumAnimationKinds( )
    {
        return ( hasParameters( ) ? 1 : 0 );
    }

    //------------------------------------------------------------------

    @Override
    protected AnimationParams selectAnimation( boolean imageSequence )
    {
        return ( hasParameters( ) ? imageSequence ? new AnimationParams( 0 )
                                                  : Pattern2AnimationParamsDialog.showDialog( getWindow( ) )
                                  : null );
    }

    //------------------------------------------------------------------

    @Override
    protected boolean canOptimiseAnimation( )
    {
        return false;
    }

    //------------------------------------------------------------------

    @Override
    protected void optimiseAnimation( )
    {
        // do nothing
    }

    //------------------------------------------------------------------

    @Override
    protected boolean initAnimation( int animationId,
                                     int startFrameIndex )
    {
        try
        {
            if ( startFrameIndex == 0 )
                initAnimation( 0 );
            else
                TaskProgressDialog.showDialog( getWindow( ), INIT_ANIMATION_STR,
                                               new Task.InitAnimation( this, startFrameIndex ) );
            return true;
        }
        catch ( AppException e )
        {
            return false;
        }
    }

    //------------------------------------------------------------------

    @Override
    protected void updateAnimation( int frameIndex )
        throws InterruptedException
    {
        patternImage.updatePaths( frameIndex );
        patternImage.renderImage( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    public void initAnimation( int startFrameIndex )
        throws TaskCancelledException
    {
        // Initialise progress view
        ProgressView progressView = Task.getProgressView( );
        if ( progressView != null )
        {
            progressView.setInfo( FAST_FORWARDING_STR + startFrameIndex + " " + AppConstants.ELLIPSIS_STR );
            progressView.setProgress( 0, 0.0 );
        }

        // Initialise pattern image
        try
        {
            patternImage.initAnimation( startFrameIndex );
        }
        catch ( InterruptedException e )
        {
            throw new TaskCancelledException( );
        }
    }

    //------------------------------------------------------------------

    private void generate( )
        throws AppException
    {
        patternImage = null;
        if ( Task.getNumThreads( ) == 0 )
            TaskProgressDialog.showDialog( getWindow( ), GENERATE_STR + getPatternKind( ).getName( ),
                                           new Task.GeneratePattern( this ) );
        else
        {
            Task.setCancelled( false );
            generatePattern( );
        }
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    private Pattern2Params  params;
    private Pattern2Image   patternImage;

}

//----------------------------------------------------------------------
