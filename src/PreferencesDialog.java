/*====================================================================*\

PreferencesDialog.java

Preferences dialog box class.

\*====================================================================*/


// IMPORTS


import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;

import java.util.EnumMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import uk.org.blankaspect.exception.AppException;

import uk.org.blankaspect.gui.BooleanComboBox;
import uk.org.blankaspect.gui.DimensionsSpinnerPanel;
import uk.org.blankaspect.gui.FButton;
import uk.org.blankaspect.gui.FComboBox;
import uk.org.blankaspect.gui.FIntegerSpinner;
import uk.org.blankaspect.gui.FLabel;
import uk.org.blankaspect.gui.FontEx;
import uk.org.blankaspect.gui.FontStyle;
import uk.org.blankaspect.gui.FTabbedPane;
import uk.org.blankaspect.gui.GuiUtilities;
import uk.org.blankaspect.gui.IntegerSpinner;
import uk.org.blankaspect.gui.TextRendering;
import uk.org.blankaspect.gui.TitledBorder;

import uk.org.blankaspect.textfield.ConstrainedTextField;
import uk.org.blankaspect.textfield.IntegerValueField;

import uk.org.blankaspect.util.IntegerRange;
import uk.org.blankaspect.util.KeyAction;

//----------------------------------------------------------------------


// PREFERENCES DIALOG BOX CLASS


class PreferencesDialog
    extends JDialog
    implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    // Main panel
    private static final    String  TITLE_STR               = "Preferences";
    private static final    String  SAVE_CONFIGURATION_STR  = "Save configuration";
    private static final    String  SAVE_CONFIG_FILE_STR    = "Save configuration file";
    private static final    String  WRITE_CONFIG_FILE_STR   = "Write configuration file";

    // General panel
    private static final    int     MAX_EDIT_LIST_LENGTH_FIELD_LENGTH   = 4;

    private static final    String  DEFAULT_DOCUMENT_KIND_STR       = "Default document kind:";
    private static final    String  SHOW_UNIX_PATHNAMES_STR         = "Display UNIX-style pathnames:";
    private static final    String  SELECT_TEXT_ON_FOCUS_GAINED_STR = "Select text when focus is gained:";
    private static final    String  SAVE_MAIN_WINDOW_LOCATION_STR   = "Save location of main window:";
    private static final    String  MAX_EDIT_HISTORY_SIZE_STR       = "Maximum size of edit history:";
    private static final    String  CLEAR_EDIT_HISTORY_ON_SAVE_STR  = "Clear edit history on save:";
    private static final    String  KEEP_SEQUENCE_WINDOW_ON_TOP_STR = "Keep sequence window on top:";

    // Appearance panel
    private static final    String  LOOK_AND_FEEL_STR       = "Look-and-feel:";
    private static final    String  TEXT_ANTIALIASING_STR   = "Text antialiasing:";
    private static final    String  NO_LOOK_AND_FEELS_STR   = "<no look-and-feels>";

    // Pattern panel
    private static final    int     DEFAULT_PATTERN_WIDTH_FIELD_LENGTH  = 4;
    private static final    int     DEFAULT_PATTERN_HEIGHT_FIELD_LENGTH = 4;
    private static final    int     NUM_SLIDE_SHOW_THREADS_FIELD_LENGTH = 2;
    private static final    int     NUM_RENDERING_THREADS_FIELD_LENGTH  = 2;

    private static final    String  NAME_STR                    = "Name:";
    private static final    String  DEFAULT_SIZE_STR            = "Default size:";
    private static final    String  NUM_SLIDE_SHOW_THREADS_STR  = "Number of slide-show threads:";
    private static final    String  NUM_RENDERING_THREADS_STR   = "Number of rendering threads:";
    private static final    String  ENABLE_PHASE_ANIMATION_STR  = "Enable phase animation:";
    private static final    String  PATH_RENDERING_STR          = "Path rendering:";

    // Fonts panel
    private static final    String  PT_STR  = "pt";

    // Commands
    private interface Command
    {
        String  SAVE_CONFIGURATION  = "saveConfiguration";
        String  ACCEPT              = "accept";
        String  CLOSE               = "close";
    }

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


    // TABS


    private enum Tab
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        GENERAL
        (
            "General"
        )
        {
            @Override
            protected JPanel createPanel( PreferencesDialog dialog )
            {
                return dialog.createPanelGeneral( );
            }

            //----------------------------------------------------------

            @Override
            protected void validatePreferences( PreferencesDialog dialog )
                throws AppException
            {
                dialog.validatePreferencesGeneral( );
            }

            //----------------------------------------------------------

            @Override
            protected void setPreferences( PreferencesDialog dialog )
            {
                dialog.setPreferencesGeneral( );
            }

            //----------------------------------------------------------
        },

        APPEARANCE
        (
            "Appearance"
        )
        {
            @Override
            protected JPanel createPanel( PreferencesDialog dialog )
            {
                return dialog.createPanelAppearance( );
            }

            //----------------------------------------------------------

            @Override
            protected void validatePreferences( PreferencesDialog dialog )
                throws AppException
            {
                dialog.validatePreferencesAppearance( );
            }

            //----------------------------------------------------------

            @Override
            protected void setPreferences( PreferencesDialog dialog )
            {
                dialog.setPreferencesAppearance( );
            }

            //----------------------------------------------------------
        },

        PATTERN
        (
            "Pattern"
        )
        {
            @Override
            protected JPanel createPanel( PreferencesDialog dialog )
            {
                return dialog.createPanelPattern( );
            }

            //----------------------------------------------------------

            @Override
            protected void validatePreferences( PreferencesDialog dialog )
                throws AppException
            {
                dialog.validatePreferencesPattern( );
            }

            //----------------------------------------------------------

            @Override
            protected void setPreferences( PreferencesDialog dialog )
            {
                dialog.setPreferencesPattern( );
            }

            //----------------------------------------------------------
        },

        FONTS
        (
            "Fonts"
        )
        {
            @Override
            protected JPanel createPanel( PreferencesDialog dialog )
            {
                return dialog.createPanelFonts( );
            }

            //----------------------------------------------------------

            @Override
            protected void validatePreferences( PreferencesDialog dialog )
                throws AppException
            {
                dialog.validatePreferencesFonts( );
            }

            //----------------------------------------------------------

            @Override
            protected void setPreferences( PreferencesDialog dialog )
            {
                dialog.setPreferencesFonts( );
            }

            //----------------------------------------------------------
        };

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private Tab( String text )
        {
            this.text = text;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Abstract methods
    ////////////////////////////////////////////////////////////////////

        protected abstract JPanel createPanel( PreferencesDialog dialog );

        //--------------------------------------------------------------

        protected abstract void validatePreferences( PreferencesDialog dialog )
            throws AppException;

        //--------------------------------------------------------------

        protected abstract void setPreferences( PreferencesDialog dialog );

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private String  text;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


    // PATTERN NAME FIELD


    private static class PatternNameField
        extends ConstrainedTextField
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private PatternNameField( String text )
        {
            super( PatternKind.MAX_NAME_LENGTH, text );
            AppFont.TEXT_FIELD.apply( this );
            GuiUtilities.setTextComponentMargins( this );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        protected boolean acceptCharacter( char ch,
                                           int  index )
        {
            return !Character.isISOControl( ch );
        }

        //--------------------------------------------------------------

    }

    //==================================================================


    // FONT PANEL CLASS


    private static class FontPanel
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        private static final    int MIN_SIZE    = 0;
        private static final    int MAX_SIZE    = 99;

        private static final    int SIZE_FIELD_LENGTH   = 2;

        private static final    String  DEFAULT_FONT_STR    = "<default font>";

    ////////////////////////////////////////////////////////////////////
    //  Member classes : non-inner classes
    ////////////////////////////////////////////////////////////////////


        // SIZE SPINNER CLASS


        private static class SizeSpinner
            extends IntegerSpinner
        {

        ////////////////////////////////////////////////////////////////
        //  Constructors
        ////////////////////////////////////////////////////////////////

            private SizeSpinner( int value )
            {
                super( value, MIN_SIZE, MAX_SIZE, SIZE_FIELD_LENGTH );
                AppFont.TEXT_FIELD.apply( this );
            }

            //----------------------------------------------------------

        ////////////////////////////////////////////////////////////////
        //  Instance methods : overriding methods
        ////////////////////////////////////////////////////////////////

            /**
             * @throws NumberFormatException
             */

            @Override
            protected int getEditorValue( )
            {
                IntegerValueField field = (IntegerValueField)getEditor( );
                return ( field.isEmpty( ) ? 0 : field.getValue( ) );
            }

            //----------------------------------------------------------

            @Override
            protected void setEditorValue( int value )
            {
                IntegerValueField field = (IntegerValueField)getEditor( );
                if ( value == 0 )
                    field.setText( null );
                else
                    field.setValue( value );
            }

            //----------------------------------------------------------

        }

        //==============================================================

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private FontPanel( FontEx   font,
                           String[] fontNames )
        {
            nameComboBox = new FComboBox<>( );
            nameComboBox.addItem( DEFAULT_FONT_STR );
            for ( String fontName : fontNames )
                nameComboBox.addItem( fontName );
            nameComboBox.setSelectedIndex( Util.indexOf( font.getName( ), fontNames ) + 1 );

            styleComboBox = new FComboBox<>( FontStyle.values( ) );
            styleComboBox.setSelectedValue( font.getStyle( ) );

            sizeSpinner = new SizeSpinner( font.getSize( ) );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods
    ////////////////////////////////////////////////////////////////////

        public FontEx getFont( )
        {
            String name = (nameComboBox.getSelectedIndex( ) <= 0) ? null : nameComboBox.getSelectedValue( );
            return new FontEx( name, styleComboBox.getSelectedValue( ), sizeSpinner.getIntValue( ) );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private FComboBox<String>       nameComboBox;
        private FComboBox<FontStyle>    styleComboBox;
        private SizeSpinner             sizeSpinner;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


    // WINDOW EVENT HANDLER CLASS


    private class WindowEventHandler
        extends WindowAdapter
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private WindowEventHandler( )
        {
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        public void windowClosing( WindowEvent event )
        {
            onClose( );
        }

        //--------------------------------------------------------------

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    private PreferencesDialog( Window owner )
    {

        // Call superclass constructor
        super( owner, TITLE_STR, Dialog.ModalityType.APPLICATION_MODAL );

        // Set icons
        setIconImages( owner.getIconImages( ) );


        //----  Tabbed panel

        tabbedPanel = new FTabbedPane( );
        for ( Tab tab : Tab.values( ) )
            tabbedPanel.addTab( tab.text, tab.createPanel( this ) );
        tabbedPanel.setSelectedIndex( tabIndex );


        //----  Button panel: save configuration

        JPanel saveButtonPanel = new JPanel( new GridLayout( 1, 0, 8, 0 ) );

        // Button: save configuration
        JButton saveButton = new FButton( SAVE_CONFIGURATION_STR + AppConstants.ELLIPSIS_STR );
        saveButton.setActionCommand( Command.SAVE_CONFIGURATION );
        saveButton.addActionListener( this );
        saveButtonPanel.add( saveButton );


        //----  Button panel: OK, cancel

        JPanel okCancelButtonPanel = new JPanel( new GridLayout( 1, 0, 8, 0 ) );

        // Button: OK
        JButton okButton = new FButton( AppConstants.OK_STR );
        okButton.setActionCommand( Command.ACCEPT );
        okButton.addActionListener( this );
        okCancelButtonPanel.add( okButton );

        // Button: cancel
        JButton cancelButton = new FButton( AppConstants.CANCEL_STR );
        cancelButton.setActionCommand( Command.CLOSE );
        cancelButton.addActionListener( this );
        okCancelButtonPanel.add( cancelButton );


        //----  Button panel

        GridBagLayout gridBag = new GridBagLayout( );
        GridBagConstraints gbc = new GridBagConstraints( );

        JPanel buttonPanel = new JPanel( gridBag );
        buttonPanel.setBorder( BorderFactory.createEmptyBorder( 3, 24, 3, 24 ) );

        int gridX = 0;

        gbc.gridx = gridX++;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 12 );
        gridBag.setConstraints( saveButtonPanel, gbc );
        buttonPanel.add( saveButtonPanel );

        gbc.gridx = gridX++;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 12, 0, 0 );
        gridBag.setConstraints( okCancelButtonPanel, gbc );
        buttonPanel.add( okCancelButtonPanel );


        //----  Main panel

        JPanel mainPanel = new JPanel( gridBag );
        mainPanel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );

        int gridY = 0;

        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( tabbedPanel, gbc );
        mainPanel.add( tabbedPanel );

        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 3, 0, 0, 0 );
        gridBag.setConstraints( buttonPanel, gbc );
        mainPanel.add( buttonPanel );

        // Add commands to action map
        KeyAction.create( mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                          KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), Command.CLOSE, this );


        //----  Window

        // Set content pane
        setContentPane( mainPanel );

        // Dispose of window explicitly
        setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );

        // Handle window events
        addWindowListener( new WindowEventHandler( ) );

        // Prevent dialog from being resized
        setResizable( false );

        // Resize dialog to its preferred size
        pack( );

        // Set location of dialog box
        if ( location == null )
            location = GuiUtilities.getComponentLocation( this, owner );
        setLocation( location );

        // Set default button
        getRootPane( ).setDefaultButton( okButton );

        // Show dialog
        setVisible( true );

    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

    public static boolean showDialog( Component parent )
    {
        return new PreferencesDialog( GuiUtilities.getWindow( parent ) ).accepted;
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

    public void actionPerformed( ActionEvent event )
    {
        String command = event.getActionCommand( );

        if ( command.equals( Command.SAVE_CONFIGURATION ) )
            onSaveConfiguration( );

        else if ( command.equals( Command.ACCEPT ) )
            onAccept( );

        else if ( command.equals( Command.CLOSE ) )
            onClose( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    private void validatePreferences( )
        throws AppException
    {
        for ( Tab tab : Tab.values( ) )
            tab.validatePreferences( this );
    }

    //------------------------------------------------------------------

    private void setPreferences( )
    {
        for ( Tab tab : Tab.values( ) )
            tab.setPreferences( this );
    }

    //------------------------------------------------------------------

    private void onSaveConfiguration( )
    {
        try
        {
            validatePreferences( );

            File file = AppConfig.getInstance( ).chooseFile( this );
            if ( file != null )
            {
                String[] optionStrs = Util.getOptionStrings( AppConstants.REPLACE_STR );
                if ( !file.exists( ) ||
                     (JOptionPane.showOptionDialog( this, Util.getPathname( file ) +
                                                                            AppConstants.ALREADY_EXISTS_STR,
                                                    SAVE_CONFIG_FILE_STR, JOptionPane.OK_CANCEL_OPTION,
                                                    JOptionPane.WARNING_MESSAGE, null, optionStrs,
                                                    optionStrs[1] ) == JOptionPane.OK_OPTION) )
                {
                    setPreferences( );
                    accepted = true;
                    TaskProgressDialog.showDialog( this, WRITE_CONFIG_FILE_STR,
                                                   new Task.WriteConfig( file ) );
                }
            }
        }
        catch ( AppException e )
        {
            JOptionPane.showMessageDialog( this, e, App.SHORT_NAME, JOptionPane.ERROR_MESSAGE );
        }
        if ( accepted )
            onClose( );
    }

    //------------------------------------------------------------------

    private void onAccept( )
    {
        try
        {
            validatePreferences( );
            setPreferences( );
            accepted = true;
            onClose( );
        }
        catch ( AppException e )
        {
            JOptionPane.showMessageDialog( this, e, App.SHORT_NAME, JOptionPane.ERROR_MESSAGE );
        }
    }

    //------------------------------------------------------------------

    private void onClose( )
    {
        location = getLocation( );
        tabIndex = tabbedPanel.getSelectedIndex( );
        setVisible( false );
        dispose( );
    }

    //------------------------------------------------------------------

    private JPanel createPanelGeneral( )
    {

        //----  Control panel

        GridBagLayout gridBag = new GridBagLayout( );
        GridBagConstraints gbc = new GridBagConstraints( );

        JPanel controlPanel = new JPanel( gridBag );
        GuiUtilities.setPaddedLineBorder( controlPanel );

        int gridY = 0;

        AppConfig config = AppConfig.getInstance( );

        // Label: default document kind
        JLabel defaultDocumentKindLabel = new FLabel( DEFAULT_DOCUMENT_KIND_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( defaultDocumentKindLabel, gbc );
        controlPanel.add( defaultDocumentKindLabel );

        // Combo box: default document kind
        defaultDocumentKindComboBox = new FComboBox<>( DocumentKind.values( ) );
        defaultDocumentKindComboBox.setSelectedValue( config.getDefaultDocumentKind( ) );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( defaultDocumentKindComboBox, gbc );
        controlPanel.add( defaultDocumentKindComboBox );

        // Label: show UNIX pathnames
        JLabel showUnixPathnamesLabel = new FLabel( SHOW_UNIX_PATHNAMES_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( showUnixPathnamesLabel, gbc );
        controlPanel.add( showUnixPathnamesLabel );

        // Combo box: show UNIX pathnames
        showUnixPathnamesComboBox = new BooleanComboBox( config.isShowUnixPathnames( ) );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( showUnixPathnamesComboBox, gbc );
        controlPanel.add( showUnixPathnamesComboBox );

        // Label: select text on focus gained
        JLabel selectTextOnFocusGainedLabel = new FLabel( SELECT_TEXT_ON_FOCUS_GAINED_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( selectTextOnFocusGainedLabel, gbc );
        controlPanel.add( selectTextOnFocusGainedLabel );

        // Combo box: select text on focus gained
        selectTextOnFocusGainedComboBox = new BooleanComboBox( config.isSelectTextOnFocusGained( ) );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( selectTextOnFocusGainedComboBox, gbc );
        controlPanel.add( selectTextOnFocusGainedComboBox );

        // Label: save main window location
        JLabel saveMainWindowLocationLabel = new FLabel( SAVE_MAIN_WINDOW_LOCATION_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( saveMainWindowLocationLabel, gbc );
        controlPanel.add( saveMainWindowLocationLabel );

        // Combo box: save main window location
        saveMainWindowLocationComboBox = new BooleanComboBox( config.isMainWindowLocation( ) );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( saveMainWindowLocationComboBox, gbc );
        controlPanel.add( saveMainWindowLocationComboBox );

        // Label: maximum edit list length
        JLabel maxEditListLengthLabel = new FLabel( MAX_EDIT_HISTORY_SIZE_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( maxEditListLengthLabel, gbc );
        controlPanel.add( maxEditListLengthLabel );

        // Spinner: maximum edit list length
        maxEditListLengthSpinner = new FIntegerSpinner( config.getMaxEditListLength( ),
                                                        PatternDocument.MIN_MAX_EDIT_LIST_LENGTH,
                                                        PatternDocument.MAX_MAX_EDIT_LIST_LENGTH,
                                                        MAX_EDIT_LIST_LENGTH_FIELD_LENGTH );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( maxEditListLengthSpinner, gbc );
        controlPanel.add( maxEditListLengthSpinner );

        // Label: clear edit list on save
        JLabel clearEditListOnSaveLabel = new FLabel( CLEAR_EDIT_HISTORY_ON_SAVE_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( clearEditListOnSaveLabel, gbc );
        controlPanel.add( clearEditListOnSaveLabel );

        // Combo box: clear edit list on save
        clearEditListOnSaveComboBox = new BooleanComboBox( config.isClearEditListOnSave( ) );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( clearEditListOnSaveComboBox, gbc );
        controlPanel.add( clearEditListOnSaveComboBox );

        // Label: keep sequence window on top
        JLabel keepSequenceWindowOnTopLabel = new FLabel( KEEP_SEQUENCE_WINDOW_ON_TOP_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( keepSequenceWindowOnTopLabel, gbc );
        controlPanel.add( keepSequenceWindowOnTopLabel );

        // Combo box: keep sequence window on top
        keepSequenceWindowOnTopComboBox = new BooleanComboBox( config.isKeepSequenceWindowOnTop( ) );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( keepSequenceWindowOnTopComboBox, gbc );
        controlPanel.add( keepSequenceWindowOnTopComboBox );


        //----  Outer panel

        JPanel outerPanel = new JPanel( gridBag );
        outerPanel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( controlPanel, gbc );
        outerPanel.add( controlPanel );

        return outerPanel;

    }

    //------------------------------------------------------------------

    private JPanel createPanelAppearance( )
    {

        //----  Control panel

        GridBagLayout gridBag = new GridBagLayout( );
        GridBagConstraints gbc = new GridBagConstraints( );

        JPanel controlPanel = new JPanel( gridBag );
        GuiUtilities.setPaddedLineBorder( controlPanel );

        int gridY = 0;

        AppConfig config = AppConfig.getInstance( );

        // Label: look-and-feel
        JLabel lookAndFeelLabel = new FLabel( LOOK_AND_FEEL_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( lookAndFeelLabel, gbc );
        controlPanel.add( lookAndFeelLabel );

        // Combo box: look-and-feel
        lookAndFeelComboBox = new FComboBox<>( );

        UIManager.LookAndFeelInfo[] lookAndFeelInfos = UIManager.getInstalledLookAndFeels( );
        if ( lookAndFeelInfos.length == 0 )
        {
            lookAndFeelComboBox.addItem( NO_LOOK_AND_FEELS_STR );
            lookAndFeelComboBox.setSelectedIndex( 0 );
            lookAndFeelComboBox.setEnabled( false );
        }
        else
        {
            String[] lookAndFeelNames = new String[lookAndFeelInfos.length];
            for ( int i = 0; i < lookAndFeelInfos.length; ++i )
            {
                lookAndFeelNames[i] = lookAndFeelInfos[i].getName( );
                lookAndFeelComboBox.addItem( lookAndFeelNames[i] );
            }
            lookAndFeelComboBox.setSelectedValue( config.getLookAndFeel( ) );
        }

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( lookAndFeelComboBox, gbc );
        controlPanel.add( lookAndFeelComboBox );

        // Label: text antialiasing
        JLabel textAntialiasingLabel = new FLabel( TEXT_ANTIALIASING_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( textAntialiasingLabel, gbc );
        controlPanel.add( textAntialiasingLabel );

        // Combo box: text antialiasing
        textAntialiasingComboBox = new FComboBox<>( TextRendering.Antialiasing.values( ) );
        textAntialiasingComboBox.setSelectedValue( config.getTextAntialiasing( ) );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( textAntialiasingComboBox, gbc );
        controlPanel.add( textAntialiasingComboBox );


        //----  Outer panel

        JPanel outerPanel = new JPanel( gridBag );
        outerPanel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( controlPanel, gbc );
        outerPanel.add( controlPanel );

        return outerPanel;

    }

    //------------------------------------------------------------------

    private JPanel createPanelPattern( )
    {

        //----  Pattern-specific panels

        GridBagLayout gridBag = new GridBagLayout( );
        GridBagConstraints gbc = new GridBagConstraints( );

        AppConfig config = AppConfig.getInstance( );

        Map<PatternKind, JPanel> patternPanels = new EnumMap<>( PatternKind.class );
        nameFields = new EnumMap<>( PatternKind.class );
        defaultSizePanels = new EnumMap<>( PatternKind.class );
        numSlideShowThreadsSpinners = new EnumMap<>( PatternKind.class );
        for ( PatternKind patternKind : PatternKind.values( ) )
        {
            // Pattern-specific panel
            JPanel patternPanel = new JPanel( gridBag );
            TitledBorder.setPaddedBorder( patternPanel, patternKind.toString( ) );
            patternPanels.put( patternKind, patternPanel );

            int gridY = 0;

            // Label: name
            JLabel nameLabel = new FLabel( NAME_STR );

            gbc.gridx = 0;
            gbc.gridy = gridY;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_END;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( nameLabel, gbc );
            patternPanel.add( nameLabel );

            // Field: name
            PatternNameField nameField = new PatternNameField( config.getPatternName( patternKind ) );
            nameFields.put( patternKind, nameField );

            gbc.gridx = 1;
            gbc.gridy = gridY++;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( nameField, gbc );
            patternPanel.add( nameField );

            // Label: default size
            JLabel defaultSizeLabel = new FLabel( DEFAULT_SIZE_STR );

            gbc.gridx = 0;
            gbc.gridy = gridY;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_END;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( defaultSizeLabel, gbc );
            patternPanel.add( defaultSizeLabel );

            // Panel: default size
            Dimension size = config.getDefaultPatternSize( patternKind );
            IntegerRange widthRange = patternKind.getWidthRange( );
            IntegerRange heightRange = patternKind.getHeightRange( );
            DimensionsSpinnerPanel defaultSizePanel =
                                            new DimensionsSpinnerPanel( size.width, widthRange.lowerLimit,
                                                                        widthRange.upperLimit,
                                                                        DEFAULT_PATTERN_WIDTH_FIELD_LENGTH,
                                                                        size.height, heightRange.lowerLimit,
                                                                        heightRange.upperLimit,
                                                                        DEFAULT_PATTERN_HEIGHT_FIELD_LENGTH,
                                                                        null );
            defaultSizePanels.put( patternKind, defaultSizePanel );

            gbc.gridx = 1;
            gbc.gridy = gridY++;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( defaultSizePanel, gbc );
            patternPanel.add( defaultSizePanel );

            // Label: number of slide-show threads
            JLabel numSlideShowThreadsLabel = new FLabel( NUM_SLIDE_SHOW_THREADS_STR );

            gbc.gridx = 0;
            gbc.gridy = gridY;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_END;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( numSlideShowThreadsLabel, gbc );
            patternPanel.add( numSlideShowThreadsLabel );

            // Slider: number of slide-show threads
            FIntegerSpinner numSlideShowThreadsSpinner =
                                        new FIntegerSpinner( config.getNumSlideShowThreads( patternKind ),
                                                             PatternDocument.MIN_NUM_SLIDE_SHOW_THREADS,
                                                             PatternDocument.MAX_NUM_SLIDE_SHOW_THREADS,
                                                             NUM_SLIDE_SHOW_THREADS_FIELD_LENGTH );
            numSlideShowThreadsSpinners.put( patternKind, numSlideShowThreadsSpinner );

            gbc.gridx = 1;
            gbc.gridy = gridY++;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( numSlideShowThreadsSpinner, gbc );
            patternPanel.add( numSlideShowThreadsSpinner );

            // Add components for pattern kind
            switch ( patternKind )
            {
                case PATTERN1:
                    addPanelPattern1( patternPanel, gridBag, gbc, gridY );
                    break;

                case PATTERN2:
                    addPanelPattern2( patternPanel, gridBag, gbc, gridY );
                    break;
            }
        }


        //----  Outer panel

        JPanel outerPanel = new JPanel( gridBag );
        outerPanel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );

        int gridY = 0;
        for ( PatternKind patternKind : patternPanels.keySet( ) )
        {
            JPanel patternPanel = patternPanels.get( patternKind );

            gbc.gridx = 0;
            gbc.gridy = gridY++;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.NORTH;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets( (gridY == 0) ? 0 : 3, 0, 0, 0 );
            gridBag.setConstraints( patternPanel, gbc );
            outerPanel.add( patternPanel );
        }

        return outerPanel;

    }

    //------------------------------------------------------------------

    private void addPanelPattern1( JPanel             panel,
                                   GridBagLayout      gridBag,
                                   GridBagConstraints gbc,
                                   int                gridY )
    {
        AppConfig config = AppConfig.getInstance( );

        // Label: number of rendering threads
        JLabel numRenderingThreadsLabel = new FLabel( NUM_RENDERING_THREADS_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( numRenderingThreadsLabel, gbc );
        panel.add( numRenderingThreadsLabel );

        // Slider: number of rendering threads
        pattern1NumRenderingThreadsSpinner = new FIntegerSpinner( config.getPattern1NumRenderingThreads( ),
                                                                  Pattern1Image.MIN_NUM_RENDERING_THREADS,
                                                                  Pattern1Image.MAX_NUM_RENDERING_THREADS,
                                                                  NUM_RENDERING_THREADS_FIELD_LENGTH );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( pattern1NumRenderingThreadsSpinner, gbc );
        panel.add( pattern1NumRenderingThreadsSpinner );

        // Label: enable phase animation
        JLabel phaseAnimationLabel = new FLabel( ENABLE_PHASE_ANIMATION_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( phaseAnimationLabel, gbc );
        panel.add( phaseAnimationLabel );

        // Combo box: enable phase animation
        pattern1PhaseAnimationComboBox = new BooleanComboBox( config.isPattern1PhaseAnimation( ) );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( pattern1PhaseAnimationComboBox, gbc );
        panel.add( pattern1PhaseAnimationComboBox );
    }

    //------------------------------------------------------------------

    private void addPanelPattern2( JPanel             panel,
                                   GridBagLayout      gridBag,
                                   GridBagConstraints gbc,
                                   int                gridY )
    {
        AppConfig config = AppConfig.getInstance( );

        // Label: path rendering
        JLabel pathRenderingLabel = new FLabel( PATH_RENDERING_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( pathRenderingLabel, gbc );
        panel.add( pathRenderingLabel );


        // Combo box: path rendering
        pattern2PathRenderingComboBox = new FComboBox<>( Pattern2Image.PathRendering.values( ) );
        pattern2PathRenderingComboBox.setSelectedValue( config.getPattern2PathRendering( ) );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( pattern2PathRenderingComboBox, gbc );
        panel.add( pattern2PathRenderingComboBox );
    }

    //------------------------------------------------------------------

    private JPanel createPanelFonts( )
    {

        //----  Control panel

        GridBagLayout gridBag = new GridBagLayout( );
        GridBagConstraints gbc = new GridBagConstraints( );

        JPanel controlPanel = new JPanel( gridBag );
        GuiUtilities.setPaddedLineBorder( controlPanel );

        String[] fontNames =
                        GraphicsEnvironment.getLocalGraphicsEnvironment( ).getAvailableFontFamilyNames( );
        fontPanels = new FontPanel[AppFont.getNumFonts( )];
        for ( int i = 0; i < fontPanels.length; ++i )
        {
            FontEx fontEx = AppConfig.getInstance( ).getFont( i );
            fontPanels[i] = new FontPanel( fontEx, fontNames );

            int gridX = 0;

            // Label: font
            JLabel fontLabel = new FLabel( AppFont.values( )[i].toString( ) + ":" );

            gbc.gridx = gridX++;
            gbc.gridy = i;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_END;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( fontLabel, gbc );
            controlPanel.add( fontLabel );

            // Combo box: font name
            gbc.gridx = gridX++;
            gbc.gridy = i;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( fontPanels[i].nameComboBox, gbc );
            controlPanel.add( fontPanels[i].nameComboBox );

            // Combo box: font style
            gbc.gridx = gridX++;
            gbc.gridy = i;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( fontPanels[i].styleComboBox, gbc );
            controlPanel.add( fontPanels[i].styleComboBox );

            // Panel: font size
            JPanel sizePanel = new JPanel( gridBag );

            gbc.gridx = gridX++;
            gbc.gridy = i;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( sizePanel, gbc );
            controlPanel.add( sizePanel );

            // Spinner: font size
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets( 0, 0, 0, 0 );
            gridBag.setConstraints( fontPanels[i].sizeSpinner, gbc );
            sizePanel.add( fontPanels[i].sizeSpinner );

            // Label: "pt"
            JLabel ptLabel = new FLabel( PT_STR );

            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets( 0, 4, 0, 0 );
            gridBag.setConstraints( ptLabel, gbc );
            sizePanel.add( ptLabel );
        }


        //----  Outer panel

        JPanel outerPanel = new JPanel( gridBag );
        outerPanel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( controlPanel, gbc );
        outerPanel.add( controlPanel );

        return outerPanel;

    }

    //------------------------------------------------------------------

    private void validatePreferencesGeneral( )
    {
        // do nothing
    }

    //------------------------------------------------------------------

    private void validatePreferencesAppearance( )
    {
        // do nothing
    }

    //------------------------------------------------------------------

    private void validatePreferencesPattern( )
    {
        // do nothing
    }

    //------------------------------------------------------------------

    private void validatePreferencesFonts( )
    {
        // do nothing
    }

    //------------------------------------------------------------------

    private void setPreferencesGeneral( )
    {
        AppConfig config = AppConfig.getInstance( );
        config.setDefaultDocumentKind( defaultDocumentKindComboBox.getSelectedValue( ) );
        config.setShowUnixPathnames( showUnixPathnamesComboBox.getSelectedValue( ) );
        config.setSelectTextOnFocusGained( selectTextOnFocusGainedComboBox.getSelectedValue( ) );
        if ( saveMainWindowLocationComboBox.getSelectedValue( ) != config.isMainWindowLocation( ) )
            config.setMainWindowLocation( saveMainWindowLocationComboBox.getSelectedValue( ) ? new Point( )
                                                                                             : null );
        config.setMaxEditListLength( maxEditListLengthSpinner.getIntValue( ) );
        config.setClearEditListOnSave( clearEditListOnSaveComboBox.getSelectedValue( ) );
        config.setKeepSequenceWindowOnTop( keepSequenceWindowOnTopComboBox.getSelectedValue( ) );
    }

    //------------------------------------------------------------------

    private void setPreferencesAppearance( )
    {
        AppConfig config = AppConfig.getInstance( );
        if ( lookAndFeelComboBox.isEnabled( ) && (lookAndFeelComboBox.getSelectedIndex( ) >= 0) )
            config.setLookAndFeel( lookAndFeelComboBox.getSelectedValue( ) );
        config.setTextAntialiasing( textAntialiasingComboBox.getSelectedValue( ) );
    }

    //------------------------------------------------------------------

    private void setPreferencesPattern( )
    {
        AppConfig config = AppConfig.getInstance( );
        for ( PatternKind patternKind : nameFields.keySet( ) )
        {
            String name = nameFields.get( patternKind ).getText( );
            if ( !name.isEmpty( ) )
                config.setPatternName( patternKind, name );
        }
        for ( PatternKind patternKind : defaultSizePanels.keySet( ) )
            config.setDefaultPatternSize( patternKind,
                                          defaultSizePanels.get( patternKind ).getDimensions( ) );
        for ( PatternKind patternKind : numSlideShowThreadsSpinners.keySet( ) )
            config.setNumSlideShowThreads( patternKind,
                                           numSlideShowThreadsSpinners.get( patternKind ).getIntValue( ) );
        config.setPattern1NumRenderingThreads( pattern1NumRenderingThreadsSpinner.getIntValue( ) );
        config.setPattern1PhaseAnimation( pattern1PhaseAnimationComboBox.getSelectedValue( ) );
        config.setPattern2PathRendering( pattern2PathRenderingComboBox.getSelectedValue( ) );
    }

    //------------------------------------------------------------------

    private void setPreferencesFonts( )
    {
        for ( int i = 0; i < fontPanels.length; ++i )
        {
            if ( fontPanels[i].nameComboBox.getSelectedIndex( ) >= 0 )
                AppConfig.getInstance( ).setFont( i, fontPanels[i].getFont( ) );
        }
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

    private static  Point   location;
    private static  int     tabIndex;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    // Main panel
    private boolean                                     accepted;
    private JTabbedPane                                 tabbedPanel;

    // General panel
    private FComboBox<DocumentKind>                     defaultDocumentKindComboBox;
    private BooleanComboBox                             showUnixPathnamesComboBox;
    private BooleanComboBox                             selectTextOnFocusGainedComboBox;
    private BooleanComboBox                             saveMainWindowLocationComboBox;
    private FIntegerSpinner                             maxEditListLengthSpinner;
    private BooleanComboBox                             clearEditListOnSaveComboBox;
    private BooleanComboBox                             keepSequenceWindowOnTopComboBox;

    // Appearance panel
    private FComboBox<String>                           lookAndFeelComboBox;
    private FComboBox<TextRendering.Antialiasing>       textAntialiasingComboBox;

    // Pattern panel
    private Map<PatternKind, PatternNameField>          nameFields;
    private Map<PatternKind, DimensionsSpinnerPanel>    defaultSizePanels;
    private Map<PatternKind, FIntegerSpinner>           numSlideShowThreadsSpinners;
    private FIntegerSpinner                             pattern1NumRenderingThreadsSpinner;
    private BooleanComboBox                             pattern1PhaseAnimationComboBox;
    private FComboBox<Pattern2Image.PathRendering>      pattern2PathRenderingComboBox;

    // Fonts panel
    private FontPanel[]                                 fontPanels;

}

//----------------------------------------------------------------------
