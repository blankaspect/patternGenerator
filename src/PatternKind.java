/*====================================================================*\

PatternKind.java

Pattern kind enumeration.

\*====================================================================*/


// IMPORTS


import java.awt.Dimension;

import uk.org.blankaspect.util.IntegerRange;
import uk.org.blankaspect.util.StringKeyed;

//----------------------------------------------------------------------


// PATTERN KIND ENUMERATION


enum PatternKind
    implements StringKeyed
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    PATTERN1
    (
        "pattern1",
        "Pattern 1",
        Pattern1Params.MIN_WIDTH, Pattern1Params.MAX_WIDTH, Pattern1Params.DEFAULT_WIDTH,
        Pattern1Params.MIN_HEIGHT, Pattern1Params.MAX_HEIGHT, Pattern1Params.DEFAULT_HEIGHT
    ),

    PATTERN2
    (
        "pattern2",
        "Pattern 2",
        Pattern2Params.MIN_WIDTH, Pattern2Params.MAX_WIDTH, Pattern2Params.DEFAULT_WIDTH,
        Pattern2Params.MIN_HEIGHT, Pattern2Params.MAX_HEIGHT, Pattern2Params.DEFAULT_HEIGHT
    );

    //------------------------------------------------------------------

    public static final int MAX_NAME_LENGTH = 24;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    private PatternKind( String key,
                         String text,
                         int    minWidth,
                         int    maxWidth,
                         int    defaultWidth,
                         int    minHeight,
                         int    maxHeight,
                         int    defaultHeight )
    {
        this.key = key;
        this.text = text;
        widthRange = new IntegerRange( minWidth, maxWidth );
        this.defaultWidth = defaultWidth;
        heightRange = new IntegerRange( minHeight, maxHeight );
        this.defaultHeight = defaultHeight;
        sizeRanges = new IntegerRange[]{ widthRange, heightRange };
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

    public static PatternKind forKey( String key )
    {
        for ( PatternKind value : values( ) )
        {
            if ( value.key.equals( key ) )
                return value;
        }
        return null;
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : StringKeyed interface
////////////////////////////////////////////////////////////////////////

    public String getKey( )
    {
        return key;
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

    @Override
    public String toString( )
    {
        return text;
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    public String getName( )
    {
        return AppConfig.getInstance( ).getPatternName( this );
    }

    //------------------------------------------------------------------

    public IntegerRange getWidthRange( )
    {
        return widthRange;
    }

    //------------------------------------------------------------------

    public IntegerRange getHeightRange( )
    {
        return widthRange;
    }

    //------------------------------------------------------------------

    public IntegerRange[] getSizeRanges( )
    {
        return sizeRanges;
    }

    //------------------------------------------------------------------

    public Dimension getDefaultSize( )
    {
        return new Dimension( defaultWidth, defaultHeight );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    private String          key;
    private String          text;
    private int             defaultWidth;
    private int             defaultHeight;
    private IntegerRange    widthRange;
    private IntegerRange    heightRange;
    private IntegerRange[]  sizeRanges;

}

//----------------------------------------------------------------------
