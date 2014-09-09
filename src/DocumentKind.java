/*====================================================================*\

DocumentKind.java

Document kind enumeration.

\*====================================================================*/


// IMPORTS


import uk.org.blankaspect.util.FilenameSuffixFilter;
import uk.org.blankaspect.util.StringKeyed;
import uk.org.blankaspect.util.StringUtilities;

//----------------------------------------------------------------------


// DOCUMENT KIND ENUMERATION


enum DocumentKind
    implements StringKeyed
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    DEFINITION
    (
        "definition",
        AppConstants.PG_DEF_FILE_SUFFIX,
        AppConstants.PG_DEF_FILES_STR
    ),

    PARAMETERS
    (
        "parameters",
        AppConstants.PG_PAR_FILE_SUFFIX,
        AppConstants.PG_PAR_FILES_STR
    );

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    private DocumentKind( String key,
                          String suffix,
                          String description )
    {
        this.key = key;
        filter = new FilenameSuffixFilter( description, suffix );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

    public static DocumentKind forKey( String key )
    {
        for ( DocumentKind value : values( ) )
        {
            if ( value.key.equals( key ) )
                return value;
        }
        return null;
    }

    //------------------------------------------------------------------

    public static DocumentKind forDescription( String description )
    {
        for ( DocumentKind value : values( ) )
        {
            if ( value.filter.getDescription( ).equals( description ) )
                return value;
        }
        return null;
    }

    //------------------------------------------------------------------

    public static DocumentKind forFilename( String filename )
    {
        if ( filename != null )
        {
            for ( DocumentKind value : values( ) )
            {
                if ( filename.endsWith( value.filter.getSuffix( 0 ) ) )
                    return value;
            }
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
        return StringUtilities.firstCharToUpperCase( key );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    public FilenameSuffixFilter getFilter( )
    {
        return filter;
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    private String                  key;
    private FilenameSuffixFilter    filter;

}

//----------------------------------------------------------------------
