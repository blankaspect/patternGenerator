/*====================================================================*\

App.java

Application class.

\*====================================================================*/


// IMPORTS


import java.awt.Dimension;
import java.awt.Point;

import java.awt.image.BufferedImage;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import javax.swing.filechooser.FileFilter;

import uk.org.blankaspect.exception.AppException;
import uk.org.blankaspect.exception.ExceptionUtilities;

import uk.org.blankaspect.gui.GuiUtilities;
import uk.org.blankaspect.gui.TextRendering;

import uk.org.blankaspect.random.Prng01;

import uk.org.blankaspect.textfield.TextFieldUtilities;

import uk.org.blankaspect.util.CalendarTime;
import uk.org.blankaspect.util.FilenameSuffixFilter;
import uk.org.blankaspect.util.NoYes;
import uk.org.blankaspect.util.PngOutputFile;
import uk.org.blankaspect.util.PropertyString;
import uk.org.blankaspect.util.ResourceProperties;
import uk.org.blankaspect.util.StringUtilities;

//----------------------------------------------------------------------


// APPLICATION CLASS


class App
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    public static final     String  SHORT_NAME  = "PatternGenerator";
    public static final     String  LONG_NAME   = "Pattern generator";
    public static final     String  NAME_KEY    = "patternGenerator";

    public static final     int MAX_NUM_DOCUMENTS   = 64;

    public static final     int     MAX_NUM_SEED_DIGITS = 18;
    public static final     long    MIN_SEED            = 0;
    public static final     long    MAX_SEED            =
                                            Math.round( Math.pow( 10.0, (double)MAX_NUM_SEED_DIGITS ) ) - 1;

    private static final    int FILE_CHECK_TIMER_INTERVAL   = 500;

    private static final    String  DEBUG_PROPERTY_KEY      = "app.debug";
    private static final    String  VERSION_PROPERTY_KEY    = "version";
    private static final    String  BUILD_PROPERTY_KEY      = "build";
    private static final    String  RELEASE_PROPERTY_KEY    = "release";

    private static final    String  BUILD_PROPERTIES_PATHNAME   = "resources/build.properties";

    private static final    String  DEBUG_STR               = " Debug";
    private static final    String  CONFIG_ERROR_STR        = "Configuration error";
    private static final    String  LAF_ERROR1_STR          = "Look-and-feel: ";
    private static final    String  LAF_ERROR2_STR          = "\nThe look-and-feel is not installed.";
    private static final    String  OPEN_FILE_STR           = "Open file";
    private static final    String  REVERT_FILE_STR         = "Revert file";
    private static final    String  SAVE_FILE_STR           = "Save file";
    private static final    String  SAVE_FILE_AS_STR        = "Save file as";
    private static final    String  SAVE_CLOSE_FILE_STR     = "Save file before closing";
    private static final    String  EXPORT_IMAGE_STR        = "Export image";
    private static final    String  EXPORT_IMAGE_SEQ_STR    = "Export image sequence";
    private static final    String  EXPORT_AS_SVG_STR           = "Export SVG image";
    private static final    String  MODIFIED_FILE_STR       = "Modified file";
    private static final    String  READ_FILE_STR           = "Read file";
    private static final    String  WRITE_FILE_STR          = "Write file";
    private static final    String  REVERT_STR              = "Revert";
    private static final    String  SAVE_STR                = "Save";
    private static final    String  DISCARD_STR             = "Discard";
    private static final    String  REVERT_MESSAGE_STR      = "\nDo you want discard the changes to the " +
                                                                "current document and reopen the " +
                                                                "original file?";
    private static final    String  MODIFIED_MESSAGE_STR    = "\nThe file has been modified externally.\n" +
                                                                "Do you want to open the modified file?";
    private static final    String  UNNAMED_FILE_STR        = "The unnamed file";
    private static final    String  CHANGED_MESSAGE1_STR    = "\nThe file";
    private static final    String  CHANGED_MESSAGE2_STR    = " has changed.\nDo you want to save the " +
                                                                "changed file?";

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


    // DOCUMENT-VIEW CLASS


    private static class DocumentView
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private DocumentView( PatternDocument document )
        {
            this.document = document;
            view = new PatternView( document );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private PatternDocument document;
        private PatternView     view;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


    // INITIALISATION CLASS


    /**
     * The run() method of this class creates the main window and performs the remaining initialisation of
     * the application from the event-dispatching thread.
     */

    private class DoInitialisation
        implements Runnable
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private DoInitialisation( String[] arguments )
        {
            this.arguments = arguments;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : Runnable interface
    ////////////////////////////////////////////////////////////////////

        public void run( )
        {
            // Create main window
            mainWindow = new MainWindow( );

            // Start file-check timer
            fileCheckTimer = new Timer( FILE_CHECK_TIMER_INTERVAL, AppCommand.CHECK_MODIFIED_FILE );
            fileCheckTimer.setRepeats( false );
            fileCheckTimer.start( );

            // Command-line arguments: open files
            if ( arguments.length > 0 )
            {
                // Create list of files from command-line arguments
                File[] files = new File[arguments.length];
                for ( int i = 0; i < arguments.length; ++i )
                    files[i] = new File( PropertyString.parsePathname( arguments[i] ) );

                // Open files
                openFiles( files );

                // Update title and menus
                mainWindow.updateTitleAndMenus( );
            }
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private String[]    arguments;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    private App( )
    {
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

    public static void main( String[] args )
    {
        getInstance( ).init( args );
    }

    //------------------------------------------------------------------

    public static App getInstance( )
    {
        if ( instance == null )
            instance = new App( );
        return instance;
    }

    //------------------------------------------------------------------

    public static boolean isDebug( )
    {
        return debug;
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    public MainWindow getMainWindow( )
    {
        return mainWindow;
    }

    //------------------------------------------------------------------

    public int getNumDocuments( )
    {
        return documentsViews.size( );
    }

    //------------------------------------------------------------------

    public boolean hasDocuments( )
    {
        return !documentsViews.isEmpty( );
    }

    //------------------------------------------------------------------

    public boolean isDocumentsFull( )
    {
        return ( documentsViews.size( ) >= MAX_NUM_DOCUMENTS );
    }

    //------------------------------------------------------------------

    public PatternDocument getDocument( )
    {
        return ( (hasDocuments( ) && (mainWindow != null)) ? getDocument( mainWindow.getTabIndex( ) )
                                                           : null );
    }

    //------------------------------------------------------------------

    public PatternDocument getDocument( int index )
    {
        return ( hasDocuments( ) ? documentsViews.get( index ).document : null );
    }

    //------------------------------------------------------------------

    public PatternView getView( )
    {
        return ( (hasDocuments( ) && (mainWindow != null)) ? getView( mainWindow.getTabIndex( ) ) : null );
    }

    //------------------------------------------------------------------

    public PatternView getView( int index )
    {
        return ( hasDocuments( ) ? documentsViews.get( index ).view : null );
    }

    //------------------------------------------------------------------

    public PatternView getView( PatternDocument document )
    {
        for ( DocumentView documentView : documentsViews )
        {
            if ( documentView.document == document )
                return documentView.view;
        }
        return null;
    }

    //------------------------------------------------------------------

    public String getVersionString( )
    {
        StringBuilder buffer = new StringBuilder( 32 );
        String str = buildProperties.get( VERSION_PROPERTY_KEY );
        if ( str != null )
            buffer.append( str );

        str = buildProperties.get( RELEASE_PROPERTY_KEY );
        if ( str == null )
        {
            long time = System.currentTimeMillis( );
            if ( buffer.length( ) > 0 )
                buffer.append( ' ' );
            buffer.append( 'b' );
            buffer.append( CalendarTime.dateToString( time ) );
            buffer.append( '-' );
            buffer.append( CalendarTime.hoursMinsToString( time ) );
        }
        else
        {
            NoYes release = NoYes.forKey( str );
            if ( (release == null) || !release.toBoolean( ) )
            {
                str = buildProperties.get( BUILD_PROPERTY_KEY );
                if ( str != null )
                {
                    if ( buffer.length( ) > 0 )
                        buffer.append( ' ' );
                    buffer.append( str );
                }
            }
        }

        if ( debug )
            buffer.append( DEBUG_STR );

        return buffer.toString( );
    }

    //------------------------------------------------------------------

    public void showInfoMessage( String titleStr,
                                 Object message )
    {
        showMessageDialog( titleStr, message, JOptionPane.INFORMATION_MESSAGE );
    }

    //------------------------------------------------------------------

    public void showWarningMessage( String titleStr,
                                    Object message )
    {
        showMessageDialog( titleStr, message, JOptionPane.WARNING_MESSAGE );
    }

    //------------------------------------------------------------------

    public void showErrorMessage( String titleStr,
                                  Object message )
    {
        showMessageDialog( titleStr, message, JOptionPane.ERROR_MESSAGE );
    }

    //------------------------------------------------------------------

    public void showMessageDialog( String titleStr,
                                   Object message,
                                   int    messageKind )
    {
        JOptionPane.showMessageDialog( mainWindow, message, titleStr, messageKind );
    }

    //------------------------------------------------------------------

    public boolean confirmWriteFile( File   file,
                                     String titleStr )
    {
        String[] optionStrs = Util.getOptionStrings( AppConstants.REPLACE_STR );
        return ( !file.exists( ) ||
                 (JOptionPane.showOptionDialog( mainWindow,
                                                Util.getPathname( file ) + AppConstants.ALREADY_EXISTS_STR,
                                                titleStr, JOptionPane.OK_CANCEL_OPTION,
                                                JOptionPane.WARNING_MESSAGE, null, optionStrs,
                                                optionStrs[1] ) == JOptionPane.OK_OPTION) );
    }

    //------------------------------------------------------------------

    public void updateTabText( PatternDocument document )
    {
        for ( int i = 0; i < getNumDocuments( ); ++i )
        {
            if ( getDocument( i ) == document )
            {
                mainWindow.setTabText( i, document.getTitleString( false ),
                                       document.getTitleString( true ) );
                break;
            }
        }
    }

    //------------------------------------------------------------------

    public void updateCommands( )
    {
        PatternDocument document = getDocument( );
        boolean isDocument = (document != null);
        boolean notFull = !isDocumentsFull( );
        boolean documentChanged = isDocument && document.isChanged( );
        boolean hasImage = isDocument && document.hasImage( );

        AppCommand.CHECK_MODIFIED_FILE.setEnabled( true );
        AppCommand.IMPORT_FILES.setEnabled( true );
        AppCommand.CREATE_PATTERN1.putValue( Action.NAME, PatternKind.PATTERN1.getName( ) );
        AppCommand.CREATE_PATTERN1.setEnabled( notFull );
        AppCommand.CREATE_PATTERN2.putValue( Action.NAME, PatternKind.PATTERN2.getName( ) );
        AppCommand.CREATE_PATTERN2.setEnabled( notFull );
        AppCommand.OPEN_FILE.setEnabled( notFull );
        AppCommand.REVERT_FILE.setEnabled( isDocument && (document.getFile( ) != null) &&
                                           (documentChanged || !hasImage) );
        AppCommand.CLOSE_FILE.setEnabled( isDocument );
        AppCommand.CLOSE_ALL_FILES.setEnabled( isDocument );
        AppCommand.SAVE_FILE.setEnabled( documentChanged );
        AppCommand.SAVE_FILE_AS.setEnabled( isDocument );
        AppCommand.EXPORT_IMAGE.setEnabled( isDocument && hasImage && PngOutputFile.canWrite( ) );
        AppCommand.EXPORT_IMAGE_SEQUENCE.setEnabled( isDocument && document.hasAnimation( ) && hasImage &&
                                                     PngOutputFile.canWrite( ) );
        AppCommand.EXPORT_AS_SVG.setEnabled( isDocument && hasImage && document.canExportAsSvg( ) );
        AppCommand.EXIT.setEnabled( true );
        AppCommand.EDIT_PREFERENCES.setEnabled( true );
        AppCommand.TOGGLE_SHOW_FULL_PATHNAMES.setEnabled( true );
        AppCommand.TOGGLE_SHOW_FULL_PATHNAMES.
                                            setSelected( AppConfig.getInstance( ).isShowFullPathnames( ) );
    }

    //------------------------------------------------------------------

    public void executeCommand( AppCommand command )
    {
        // Execute command
        try
        {
            switch ( command )
            {
                case CHECK_MODIFIED_FILE:
                    onCheckModifiedFile( );
                    break;

                case IMPORT_FILES:
                    onImportFiles( );
                    break;

                case CREATE_PATTERN1:
                    onCreatePattern1( );
                    break;

                case CREATE_PATTERN2:
                    onCreatePattern2( );
                    break;

                case OPEN_FILE:
                    onOpenFile( );
                    break;

                case REVERT_FILE:
                    onRevertFile( );
                    break;

                case CLOSE_FILE:
                    onCloseFile( );
                    break;

                case CLOSE_ALL_FILES:
                    onCloseAllFiles( );
                    break;

                case SAVE_FILE:
                    onSaveFile( );
                    break;

                case SAVE_FILE_AS:
                    onSaveFileAs( );
                    break;

                case EXPORT_IMAGE:
                    onExportImage( );
                    break;

                case EXPORT_IMAGE_SEQUENCE:
                    onExportImageSequence( );
                    break;

                case EXPORT_AS_SVG:
                    onExportAsSvg( );
                    break;

                case EXIT:
                    onExit( );
                    break;

                case EDIT_PREFERENCES:
                    onEditPreferences( );
                    break;

                case TOGGLE_SHOW_FULL_PATHNAMES:
                    onToggleShowFullPathnames( );
                    break;
            }
        }
        catch ( AppException e )
        {
            showErrorMessage( SHORT_NAME, e );
        }

        // Update main window
        if ( command != AppCommand.CHECK_MODIFIED_FILE )
        {
            updateTabText( getDocument( ) );
            mainWindow.updateTitleAndMenus( );
        }
    }

    //------------------------------------------------------------------

    public synchronized long getNextRandomSeed( )
    {
        return ( prng.nextLong( ) % (MAX_SEED + 1) );
    }

    //------------------------------------------------------------------

    public void closeDocument( int index )
    {
        // Stop playing
        stopPlaying( index );

        // Remove document from list
        if ( confirmCloseDocument( index ) )
            removeDocument( index );
    }

    //------------------------------------------------------------------

    public void addDocument( PatternDocument document )
    {
        DocumentView documentView = new DocumentView( document );
        documentsViews.add( documentView );
        mainWindow.addView( document.getTitleString( false ), document.getTitleString( true ),
                            documentView.view );
    }

    //------------------------------------------------------------------

    private void removeDocument( int index )
    {
        documentsViews.remove( index );
        mainWindow.removeView( index );
    }

    //------------------------------------------------------------------

    private PatternDocument readDocument( PatternDocument.FileInfo fileInfo )
        throws AppException
    {
        PatternDocument[] result = new PatternDocument[1];
        TaskProgressDialog.showDialog( mainWindow, READ_FILE_STR,
                                       new Task.ReadDocument( fileInfo, result ) );
        return result[0];
    }

    //------------------------------------------------------------------

    private void writeDocument( PatternDocument          document,
                                PatternDocument.FileInfo fileInfo )
        throws AppException
    {
        TaskProgressDialog.showDialog( mainWindow, WRITE_FILE_STR,
                                       new Task.WriteDocument( document, fileInfo ) );
    }

    //------------------------------------------------------------------

    private void openDocument( PatternDocument.FileInfo fileInfo )
        throws AppException
    {
        // Test whether document is already open
        for ( int i = 0; i < documentsViews.size( ); ++i )
        {
            if ( Util.isSameFile( fileInfo.file, getDocument( i ).getFile( ) ) )
            {
                mainWindow.selectView( i );
                return;
            }
        }

        // Read document and add it to list
        PatternDocument document = readDocument( fileInfo );
        if ( document != null )
            addDocument( document );
    }

    //------------------------------------------------------------------

    private void revertDocument( PatternDocument.FileInfo fileInfo )
        throws AppException
    {
        // Read document
        PatternDocument document = readDocument( fileInfo );

        // Replace document in list
        if ( document != null )
        {
            int index = mainWindow.getTabIndex( );
            documentsViews.set( index, new DocumentView( document ) );
            mainWindow.setTabText( index, document.getTitleString( false ),
                                   document.getTitleString( true ) );
            mainWindow.setView( index, getView( ) );
        }
    }

    //------------------------------------------------------------------

    private boolean confirmCloseDocument( int index )
    {
        // Stop playing
        stopPlaying( index );

        // Test whether document has changed
        PatternDocument document = getDocument( index );
        if ( !document.isChanged( ) )
            return true;

        // Restore window
        GuiUtilities.restoreFrame( mainWindow );

        // Display document
        mainWindow.selectView( index );

        // Display prompt to save changed document
        PatternDocument.FileInfo fileInfo = document.getFileInfo( );
        String messageStr = ((fileInfo.file == null)
                                            ? UNNAMED_FILE_STR
                                            : Util.getPathname( fileInfo.file ) + CHANGED_MESSAGE1_STR) +
                                                                                    CHANGED_MESSAGE2_STR;
        String[] optionStrs = Util.getOptionStrings( SAVE_STR, DISCARD_STR );
        int result = JOptionPane.showOptionDialog( mainWindow, messageStr, SAVE_CLOSE_FILE_STR,
                                                   JOptionPane.YES_NO_CANCEL_OPTION,
                                                   JOptionPane.QUESTION_MESSAGE, null, optionStrs,
                                                   optionStrs[0] );

        // Discard changed document
        if ( result == JOptionPane.NO_OPTION )
            return true;

        // Save changed document
        if ( result == JOptionPane.YES_OPTION )
        {
            // Choose filename
            if ( fileInfo.file == null )
            {
                fileInfo = chooseSave( fileInfo );
                if ( fileInfo == null )
                    return false;
                if ( fileInfo.file.exists( ) )
                {
                    messageStr = Util.getPathname( fileInfo.file ) + AppConstants.ALREADY_EXISTS_STR;
                    result = JOptionPane.showConfirmDialog( mainWindow, messageStr, SAVE_CLOSE_FILE_STR,
                                                            JOptionPane.YES_NO_CANCEL_OPTION,
                                                            JOptionPane.WARNING_MESSAGE );
                    if ( result == JOptionPane.NO_OPTION )
                        return true;
                    if ( result != JOptionPane.YES_OPTION )
                        return false;
                }
            }

            // Write file
            try
            {
                writeDocument( document, fileInfo );
                return true;
            }
            catch ( AppException e )
            {
                showErrorMessage( SAVE_CLOSE_FILE_STR, e );
            }
        }

        return false;
    }

    //------------------------------------------------------------------

    private void stopPlaying( int index )
    {
        PatternDocument document = getDocument( index );
        if ( document.isPlaying( ) )
            document.stopPlaying( );
    }

    //------------------------------------------------------------------

    private void init( String[] arguments )
    {
        // Initialise instance variables
        documentsViews = new ArrayList<>( );
        prng = new Prng01( );

        // Set runtime debug flag
        debug = (System.getProperty( DEBUG_PROPERTY_KEY ) != null);

        // Read build properties
        buildProperties = new ResourceProperties( BUILD_PROPERTIES_PATHNAME, getClass( ) );

        // Read configuration
        AppConfig config = AppConfig.getInstance( );
        config.read( );

        // Set UNIX style for pathnames in file exceptions
        ExceptionUtilities.setUnixStyle( config.isShowUnixPathnames( ) );

        // Set text antialiasing
        TextRendering.setAntialiasing( config.getTextAntialiasing( ) );

        // Set look-and-feel
        String lookAndFeelName = config.getLookAndFeel( );
        for ( UIManager.LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels( ) )
        {
            if ( lookAndFeelInfo.getName( ).equals( lookAndFeelName ) )
            {
                try
                {
                    UIManager.setLookAndFeel( lookAndFeelInfo.getClassName( ) );
                }
                catch ( Exception e )
                {
                    // ignore
                }
                lookAndFeelName = null;
                break;
            }
        }
        if ( lookAndFeelName != null )
            showWarningMessage( SHORT_NAME + " | " + CONFIG_ERROR_STR,
                                LAF_ERROR1_STR + lookAndFeelName + LAF_ERROR2_STR );

        // Select all text when a text field gains focus
        if ( config.isSelectTextOnFocusGained( ) )
            TextFieldUtilities.selectAllOnFocusGained( );

        // Initialise file choosers
        initFileChoosers( );

        // Perform remaining initialisation from event-dispatching thread
        SwingUtilities.invokeLater( new DoInitialisation( arguments ) );
    }

    //------------------------------------------------------------------

    private void initFileChoosers( )
    {
        AppConfig config = AppConfig.getInstance( );

        openFileChooser = new JFileChooser( config.getOpenPatternDirectory( ) );
        openFileChooser.setDialogTitle( OPEN_FILE_STR );
        openFileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
        for ( DocumentKind documentKind : DocumentKind.values( ) )
            openFileChooser.setFileFilter( documentKind.getFilter( ) );
        openFileChooser.setFileFilter( config.getDefaultDocumentKind( ).getFilter( ) );

        saveFileChooser = new JFileChooser( config.getSavePatternDirectory( ) );
        saveFileChooser.setDialogTitle( SAVE_FILE_STR );
        saveFileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );

        exportImageFileChooser = new JFileChooser( config.getExportImageDirectory( ) );
        exportImageFileChooser.setDialogTitle( EXPORT_IMAGE_STR );
        exportImageFileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
        exportImageFileChooser.setFileFilter( new FilenameSuffixFilter( AppConstants.PNG_FILES_STR,
                                                                        AppConstants.PNG_FILE_SUFFIX ) );

        exportSvgFileChooser = new JFileChooser( config.getExportSvgDirectory( ) );
        exportSvgFileChooser.setDialogTitle( EXPORT_AS_SVG_STR );
        exportSvgFileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
        exportSvgFileChooser.setFileFilter( new FilenameSuffixFilter( AppConstants.SVG_FILES_STR,
                                                                      AppConstants.SVG_FILE_SUFFIX ) );
    }

    //------------------------------------------------------------------

    private PatternDocument.FileInfo chooseOpen( )
        throws AppException
    {
        PatternDocument.FileInfo fileInfo = null;
        openFileChooser.setSelectedFile( new File( new String( ) ) );
        openFileChooser.rescanCurrentDirectory( );
        if ( openFileChooser.showOpenDialog( mainWindow ) == JFileChooser.APPROVE_OPTION )
        {
            DocumentKind documentKind =
                        DocumentKind.forDescription( openFileChooser.getFileFilter( ).getDescription( ) );
            fileInfo = new PatternDocument.FileInfo( openFileChooser.getSelectedFile( ), documentKind );
        }
        return fileInfo;
    }

    //------------------------------------------------------------------

    private PatternDocument.FileInfo chooseSave( PatternDocument.FileInfo fileInfo )
    {
        saveFileChooser.resetChoosableFileFilters( );
        FileFilter fileFilter = DocumentKind.DEFINITION.getFilter( );
        saveFileChooser.addChoosableFileFilter( fileFilter );
        if ( fileInfo.hasParameters )
        {
            fileFilter = DocumentKind.PARAMETERS.getFilter( );
            saveFileChooser.addChoosableFileFilter( fileFilter );
        }
        saveFileChooser.setFileFilter( fileFilter );

        if ( fileInfo.file == null )
        {
            saveFileChooser.setCurrentDirectory( AppConfig.getInstance( ).getSavePatternDirectory( ) );
            saveFileChooser.setSelectedFile( new File( new String( ) ) );
        }
        else
            saveFileChooser.setSelectedFile( fileInfo.file.getAbsoluteFile( ) );

        saveFileChooser.rescanCurrentDirectory( );

        if ( saveFileChooser.showSaveDialog( mainWindow ) == JFileChooser.APPROVE_OPTION )
        {
            File file = saveFileChooser.getSelectedFile( );
            DocumentKind documentKind =
                        DocumentKind.forDescription( saveFileChooser.getFileFilter( ).getDescription( ) );
            if ( documentKind != null )
            {
                String suffix = documentKind.getFilter( ).getSuffix( 0 );
                String filename = file.getName( );
                DocumentKind filenameDocumentKind = DocumentKind.forFilename( filename );
                if ( (filenameDocumentKind != null) && (filenameDocumentKind != documentKind) )
                {
                    filename = StringUtilities.removeSuffix( filename,
                                                             filenameDocumentKind.getFilter( ).
                                                                                getSuffix( 0 ) ) + suffix;
                    file = new File( file.getParentFile( ), filename );
                }
                else
                    file = Util.appendSuffix( file, suffix );
            }
            fileInfo = new PatternDocument.FileInfo( file, documentKind );
        }
        else
            fileInfo = null;
        return fileInfo;
    }

    //------------------------------------------------------------------

    private File chooseExportImage( File file )
    {
        if ( file == null )
        {
            exportImageFileChooser.
                                setCurrentDirectory( AppConfig.getInstance( ).getExportImageDirectory( ) );
            exportImageFileChooser.setSelectedFile( new File( new String( ) ) );
        }
        else
            exportImageFileChooser.setSelectedFile( file.getAbsoluteFile( ) );
        exportImageFileChooser.rescanCurrentDirectory( );
        return ( (exportImageFileChooser.showSaveDialog( mainWindow ) == JFileChooser.APPROVE_OPTION)
                                            ? Util.appendSuffix( exportImageFileChooser.getSelectedFile( ),
                                                                 AppConstants.PNG_FILE_SUFFIX )
                                            : null );
    }

    //------------------------------------------------------------------

    private File chooseExportSvg( File file )
    {
        if ( file == null )
        {
            exportSvgFileChooser.setCurrentDirectory( AppConfig.getInstance( ).getExportSvgDirectory( ) );
            exportSvgFileChooser.setSelectedFile( new File( new String( ) ) );
        }
        else
            exportSvgFileChooser.setSelectedFile( file.getAbsoluteFile( ) );
        exportSvgFileChooser.rescanCurrentDirectory( );
        return ( (exportSvgFileChooser.showSaveDialog( mainWindow ) == JFileChooser.APPROVE_OPTION)
                                            ? Util.appendSuffix( exportSvgFileChooser.getSelectedFile( ),
                                                                 AppConstants.SVG_FILE_SUFFIX )
                                            : null );
    }

    //------------------------------------------------------------------

    private void updateConfiguration( )
    {
        // Set location of main window
        AppConfig config = AppConfig.getInstance( );
        if ( config.isMainWindowLocation( ) )
        {
            Point location = GuiUtilities.getFrameLocation( mainWindow );
            if ( location != null )
                config.setMainWindowLocation( location );
        }
        config.setMainWindowSize( GuiUtilities.getFrameSize( mainWindow ) );

        // Set file locations
        config.setOpenPatternPathname( Util.getPathname( openFileChooser.getCurrentDirectory( ) ) );
        config.setSavePatternPathname( Util.getPathname( saveFileChooser.getCurrentDirectory( ) ) );
        config.setExportImagePathname( Util.getPathname( exportImageFileChooser.getCurrentDirectory( ) ) );
        config.setExportImageSequencePathname( Util.getPathname( ExportImageSequenceDialog.
                                                                                getSelectedDirectory( ) ) );
        config.setExportSvgPathname( Util.getPathname( exportSvgFileChooser.getCurrentDirectory( ) ) );

        // Set slide-show parameters
        config.setSlideShowInterval( SlideShowParamsDialog.getInterval( ) );

        // Write configuration file
        config.write( );
    }

    //------------------------------------------------------------------

    private void openFiles( File[] files )
    {
        for ( int i = 0; i < files.length; ++i )
        {
            if ( isDocumentsFull( ) )
                break;
            try
            {
                openDocument( new PatternDocument.FileInfo( files[i], null ) );
            }
            catch ( AppException e )
            {
                if ( i == files.length - 1 )
                    showErrorMessage( OPEN_FILE_STR, e );
                else
                {
                    String[] optionStrs = Util.getOptionStrings( AppConstants.CONTINUE_STR );
                    if ( JOptionPane.showOptionDialog( mainWindow, e, OPEN_FILE_STR,
                                                       JOptionPane.OK_CANCEL_OPTION,
                                                       JOptionPane.ERROR_MESSAGE, null, optionStrs,
                                                       optionStrs[1] ) != JOptionPane.OK_OPTION )
                        break;
                }
            }
        }
    }

    //------------------------------------------------------------------

    private void onCheckModifiedFile( )
        throws AppException
    {
        PatternDocument document = getDocument( );
        if ( (document != null) && !document.isExecutingCommand( ) )
        {
            PatternDocument.FileInfo fileInfo = document.getFileInfo( );
            File file = fileInfo.file;
            long timestamp = document.getTimestamp( );
            if ( (file != null) && (timestamp != 0) )
            {
                long currentTimestamp = file.lastModified( );
                if ( (currentTimestamp != 0) && (currentTimestamp != timestamp) )
                {
                    String messageStr = Util.getPathname( file ) + MODIFIED_MESSAGE_STR;
                    if ( JOptionPane.showConfirmDialog( mainWindow, messageStr, MODIFIED_FILE_STR,
                                                        JOptionPane.YES_NO_OPTION,
                                                        JOptionPane.QUESTION_MESSAGE ) ==
                                                                                    JOptionPane.YES_OPTION )
                    {
                        revertDocument( fileInfo );
                        mainWindow.updateTitleAndMenus( );
                    }
                    else
                        document.setTimestamp( currentTimestamp );
                }
            }
        }
        fileCheckTimer.start( );
    }

    //------------------------------------------------------------------

    private void onImportFiles( )
    {
        openFiles( (File[])AppCommand.IMPORT_FILES.getValue( AppCommand.Property.FILES ) );
    }

    //------------------------------------------------------------------

    private void onCreatePattern1( )
        throws AppException
    {
        if ( !isDocumentsFull( ) )
        {
            Pattern1Params params = new Pattern1Params( );
            Dimension size = AppConfig.getInstance( ).getDefaultPatternSize( PatternKind.PATTERN1 );
            params.setWidth( size.width );
            params.setHeight( size.height );

            params = Pattern1ParamsDialog.showDialog( mainWindow, null, params );
            if ( params != null )
                addDocument( new Pattern1Document( params ) );
        }
    }

    //------------------------------------------------------------------

    private void onCreatePattern2( )
        throws AppException
    {
        if ( !isDocumentsFull( ) )
        {
            Pattern2Params params = new Pattern2Params( );
            Dimension size = AppConfig.getInstance( ).getDefaultPatternSize( PatternKind.PATTERN2 );
            params.setWidth( size.width );
            params.setHeight( size.height );

            params = Pattern2ParamsDialog.showDialog( mainWindow, null, params );
            if ( params != null )
                addDocument( new Pattern2Document( params ) );
        }
    }

    //------------------------------------------------------------------

    private void onOpenFile( )
        throws AppException
    {
        if ( !isDocumentsFull( ) )
        {
            PatternDocument.FileInfo fileInfo = chooseOpen( );
            if ( fileInfo != null )
                openDocument( fileInfo );
        }
    }

    //------------------------------------------------------------------

    private void onRevertFile( )
        throws AppException
    {
        PatternDocument document = getDocument( );
        if ( (document != null) && (document.isChanged( ) || !document.hasImage( )) )
        {
            PatternDocument.FileInfo fileInfo = document.getFileInfo( );
            File file = fileInfo.file;
            if ( file != null )
            {
                String messageStr = Util.getPathname( file ) + REVERT_MESSAGE_STR;
                String[] optionStrs = Util.getOptionStrings( REVERT_STR );
                if ( !document.isChanged( ) ||
                     JOptionPane.showOptionDialog( mainWindow, messageStr, REVERT_FILE_STR,
                                                   JOptionPane.OK_CANCEL_OPTION,
                                                   JOptionPane.QUESTION_MESSAGE, null, optionStrs,
                                                   optionStrs[1] ) == JOptionPane.OK_OPTION )
                    revertDocument( fileInfo );
            }
        }
    }

    //------------------------------------------------------------------

    private void onCloseFile( )
    {
        if ( hasDocuments( ) )
            closeDocument( mainWindow.getTabIndex( ) );
    }

    //------------------------------------------------------------------

    private void onCloseAllFiles( )
    {
        while ( hasDocuments( ) )
        {
            int index = getNumDocuments( ) - 1;
            if ( !confirmCloseDocument( index ) )
                break;
            removeDocument( index );
        }
    }

    //------------------------------------------------------------------

    private void onSaveFile( )
        throws AppException
    {
        PatternDocument document = getDocument( );
        if ( (document != null) && document.isChanged( ) )
        {
            PatternDocument.FileInfo fileInfo = document.getFileInfo( );
            if ( fileInfo.file == null )
                onSaveFileAs( );
            else
                writeDocument( document, fileInfo );
        }
    }

    //------------------------------------------------------------------

    private void onSaveFileAs( )
        throws AppException
    {
        PatternDocument document = getDocument( );
        if ( document != null )
        {
            PatternDocument.FileInfo fileInfo = chooseSave( document.getFileInfo( ) );
            if ( (fileInfo != null) && confirmWriteFile( fileInfo.file, SAVE_FILE_AS_STR ) )
                writeDocument( document, fileInfo );
        }
    }

    //------------------------------------------------------------------

    private void onExportImage( )
        throws AppException
    {
        PatternDocument document = getDocument( );
        if ( document != null )
        {
            // Derive pathname of output file from current document
            File file = document.getExportImageFile( );
            if ( file == null )
            {
                file = document.getFile( );
                if ( file != null )
                    file = new File( file.getParentFile( ),
                                     document.getBaseFilename( ) + AppConstants.PNG_FILE_SUFFIX );
            }

            file = chooseExportImage( file );
            if ( (file != null) && confirmWriteFile( file, EXPORT_IMAGE_STR ) )
                TaskProgressDialog.showDialog( mainWindow, EXPORT_IMAGE_STR,
                                               new Task.ExportImage( document, file ) );
        }
    }

    //------------------------------------------------------------------

    private void onExportImageSequence( )
        throws AppException
    {
        PatternDocument document = getDocument( );
        if ( document != null )
        {
            BufferedImage image = document.getImage( );
            if ( image != null )
            {
                ImageSequenceParams params = document.getImageSequenceParams( );
                params = ExportImageSequenceDialog.showDialog( mainWindow, image.getWidth( ),
                                                               image.getHeight( ), params );
                if ( params != null )
                    TaskProgressDialog.showDialog( mainWindow, EXPORT_IMAGE_SEQ_STR,
                                                   new Task.ExportImageSequence( document, params ) );
            }
        }
    }

    //------------------------------------------------------------------

    private void onExportAsSvg( )
        throws AppException
    {
        PatternDocument document = getDocument( );
        if ( (document != null) && document.canExportAsSvg( ) )
        {
            // Derive pathname of output file from current document
            File file = document.getExportSvgFile( );
            if ( file == null )
            {
                file = document.getFile( );
                if ( file != null )
                    file = new File( file.getParentFile( ),
                                     document.getBaseFilename( ) + AppConstants.SVG_FILE_SUFFIX );
            }

            file = chooseExportSvg( file );
            if ( (file != null) && confirmWriteFile( file, EXPORT_AS_SVG_STR ) )
                TaskProgressDialog.showDialog( mainWindow, EXPORT_AS_SVG_STR,
                                               new Task.ExportSvg( document, file ) );
        }
    }

    //------------------------------------------------------------------

    private void onExit( )
    {
        if ( !exiting )
        {
            try
            {
                // Prevent re-entry to this method
                exiting = true;

                // Close all open documents
                while ( hasDocuments( ) )
                {
                    int index = getNumDocuments( ) - 1;
                    if ( !confirmCloseDocument( index ) )
                        return;
                    removeDocument( index );
                }

                // Update configuration
                updateConfiguration( );

                // Destroy main window
                mainWindow.setVisible( false );
                mainWindow.dispose( );

                // Exit application
                System.exit( 0 );
            }
            finally
            {
                exiting = false;
            }
        }
    }

    //------------------------------------------------------------------

    private void onEditPreferences( )
    {
        if ( PreferencesDialog.showDialog( mainWindow ) )
            ExceptionUtilities.setUnixStyle( AppConfig.getInstance( ).isShowUnixPathnames( ) );
    }

    //------------------------------------------------------------------

    private void onToggleShowFullPathnames( )
    {
        AppConfig.getInstance( ).setShowFullPathnames( !AppConfig.getInstance( ).isShowFullPathnames( ) );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

    private static  App     instance;
    private static  boolean debug;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    private ResourceProperties  buildProperties;
    private MainWindow          mainWindow;
    private Timer               fileCheckTimer;
    private List<DocumentView>  documentsViews;
    private Prng01              prng;
    private JFileChooser        openFileChooser;
    private JFileChooser        saveFileChooser;
    private JFileChooser        exportImageFileChooser;
    private JFileChooser        exportSvgFileChooser;
    private boolean             exiting;

}

//----------------------------------------------------------------------
