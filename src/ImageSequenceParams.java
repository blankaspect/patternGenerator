/*====================================================================*\

ImageSequenceParams.java

Image-sequence parameters class.

\*====================================================================*/


// IMPORTS


import java.io.File;

//----------------------------------------------------------------------


// IMAGE-SEQUENCE PARAMETERS CLASS


class ImageSequenceParams
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    public static final int MAX_FRAME_WIDTH     = 9999;
    public static final int MAX_FRAME_HEIGHT    = 9999;

    public static final int MIN_NUM_FRAMES  = 1;
    public static final int MAX_NUM_FRAMES  = 999999;

    public static final int MIN_FRAME_INDEX = 0;
    public static final int MAX_FRAME_INDEX = Integer.MAX_VALUE - MAX_NUM_FRAMES;

    public static final int MIN_FADE_IN     = 0;
    public static final int MIN_FADE_OUT    = MIN_FADE_IN;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    public ImageSequenceParams( )
    {
        numFrames = MIN_NUM_FRAMES;
    }

    //------------------------------------------------------------------

    public ImageSequenceParams( File   directory,
                                String filenameBase,
                                int    frameWidth,
                                int    frameHeight,
                                int    startFrameIndex,
                                int    numFrames,
                                int    fadeIn,
                                int    fadeOut )
    {
        this.directory = directory;
        this.filenameBase = filenameBase;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.startFrameIndex = startFrameIndex;
        this.numFrames = numFrames;
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    File    directory;
    String  filenameBase;
    int     frameWidth;
    int     frameHeight;
    int     startFrameIndex;
    int     numFrames;
    int     fadeIn;
    int     fadeOut;

}

//----------------------------------------------------------------------
