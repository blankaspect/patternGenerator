/*====================================================================*\

HueSaturationRangePanel.java

Hue and saturation range panel class.

\*====================================================================*/


// IMPORTS


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.awt.image.BufferedImage;

import java.util.EnumMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.org.blankaspect.gui.Colours;
import uk.org.blankaspect.gui.FIntegerSpinner;
import uk.org.blankaspect.gui.FixedWidthRadioButton;
import uk.org.blankaspect.gui.FLabel;
import uk.org.blankaspect.gui.GuiUtilities;
import uk.org.blankaspect.gui.HorizontalSlider;
import uk.org.blankaspect.gui.IntegerSpinnerSliderPanel;

import uk.org.blankaspect.util.MaxValueMap;
import uk.org.blankaspect.util.StringUtilities;

//----------------------------------------------------------------------


// HUE AND SATURATION RANGE PANEL CLASS


class HueSaturationRangePanel
    extends JPanel
    implements ActionListener, ChangeListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    public static final     int HUE_RANGE   = 360;

    private static final    int MIN_HUE         = 0;
    private static final    int MAX_HUE         = HUE_RANGE - 1;
    private static final    int DEFAULT_HUE1    = 30;
    private static final    int DEFAULT_HUE2    = 60;

    private static final    int MIN_SATURATION      = 0;
    private static final    int MAX_SATURATION      = 100;
    private static final    int DEFAULT_SATURATION1 = 100;
    private static final    int DEFAULT_SATURATION2 = 75;

    private static final    int MIN_BRIGHTNESS      = 0;
    private static final    int MAX_BRIGHTNESS      = 100;
    private static final    int DEFAULT_BRIGHTNESS  = MAX_BRIGHTNESS;

    private static final    int HUE_FIELD_LENGTH        = 3;
    private static final    int SATURATION_FIELD_LENGTH = 3;
    private static final    int BRIGHTNESS_FIELD_LENGTH = 3;

    private static final    int SLIDER_KNOB_WIDTH   = 24;
    private static final    int SLIDER_HEIGHT       = 18;

    private static final    String  HUE_STR         = "Hue:";
    private static final    String  SATURATION_STR  = "Sat:";
    private static final    String  BRIGHTNESS_STR  = "Brightness:";

    private static final    String  SPINNER_SLIDER_PANEL_KEY    = HueSaturationRangePanel.class.getName( );

    private static final    Color   COLOUR_PANEL_BORDER_COLOUR          = Colours.LINE_BORDER;
    private static final    Color   COLOUR_PANEL_RANGE_BOX_COLOUR       = Color.BLACK;
    private static final    Color   COLOUR_PANEL_RANGE_BOX_XOR_COLOUR   = Color.WHITE;
    private static final    Color   COLOUR_PANEL_MARKER_COLOUR          = Color.DARK_GRAY;

    // Commands
    private interface Command
    {
        String  SET_MODE    = "setMode.";
    }

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


    // MODE


    private enum Mode
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        LIMIT1  ( "Limit 1" ),
        LIMIT2  ( "Limit 2" );

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private Mode( String text )
        {
            this.text = text;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        public String toString( )
        {
            return text;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods
    ////////////////////////////////////////////////////////////////////

        private Mode getComplement( )
        {
            Mode value = null;
            switch ( this )
            {
                case LIMIT1:
                    value = LIMIT2;
                    break;

                case LIMIT2:
                    value = LIMIT1;
                    break;
            }
            return value;
        }

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


    // PARAMETERS CLASS


    public static class Params
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        public Params( )
        {
            hue1 = DEFAULT_HUE1;
            saturation1 = DEFAULT_SATURATION1;
            hue2 = DEFAULT_HUE2;
            saturation2 = DEFAULT_SATURATION2;
            brightness = DEFAULT_BRIGHTNESS;
        }

        //--------------------------------------------------------------

        public Params( int hue1,
                       int saturation1,
                       int hue2,
                       int saturation2,
                       int brightness )
        {
            this.hue1 = hue1;
            this.saturation1 = saturation1;
            this.hue2 = hue2;
            this.saturation2 = saturation2;
            this.brightness = brightness;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        int hue1;
        int saturation1;
        int hue2;
        int saturation2;
        int brightness;

    }

    //==================================================================


    // RADIO BUTTON CLASS


    private static class RadioButton
        extends FixedWidthRadioButton
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        private static final    String  KEY = RadioButton.class.getCanonicalName( );

        private static final    Color   BACKGROUND_COLOUR   = new Color( 252, 224, 128 );

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private RadioButton( String text )
        {
            super( text );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Class methods
    ////////////////////////////////////////////////////////////////////

        private static void reset( )
        {
            MaxValueMap.removeAll( KEY );
        }

        //--------------------------------------------------------------

        private static void update( )
        {
            MaxValueMap.update( KEY );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        public Color getBackground( )
        {
            return ( isSelected( ) ? BACKGROUND_COLOUR : super.getBackground( ) );
        }

        //--------------------------------------------------------------

        @Override
        protected String getKey( )
        {
            return KEY;
        }

        //--------------------------------------------------------------

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


    // COLOUR PANEL CLASS


    private class ColourPanel
        extends JComponent
        implements MouseListener, MouseMotionListener
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        private static final    int COLOUR_AREA_HALF_WIDTH  = HUE_RANGE / 2;
        private static final    int COLOUR_AREA_WIDTH       = 2 * COLOUR_AREA_HALF_WIDTH;
        private static final    int COLOUR_AREA_HEIGHT      = MAX_SATURATION - MIN_SATURATION + 1;
        private static final    int BORDER_WIDTH            = 1;
        private static final    int MARKER_LENGTH           = 5;
        private static final    int LEFT_MARGIN             = MARKER_LENGTH;
        private static final    int RIGHT_MARGIN            = LEFT_MARGIN;
        private static final    int TOP_MARGIN              = MARKER_LENGTH;
        private static final    int BOTTOM_MARGIN           = TOP_MARGIN;
        private static final    int COLOUR_AREA_X           = LEFT_MARGIN + BORDER_WIDTH;
        private static final    int COLOUR_AREA_Y           = TOP_MARGIN + BORDER_WIDTH;

        private static final    float H_FACTOR  = 2.0f / (float)COLOUR_AREA_WIDTH;
        private static final    float S_FACTOR  = 1.0f / (float)(COLOUR_AREA_HEIGHT - 1);

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private ColourPanel( Params params )
        {
            // Initialise instance variables
            hue1 = params.hue1;
            saturation1 = params.saturation1;
            hue2 = params.hue2;
            saturation2 = params.saturation2;

            // Initialise image for colour area
            colourAreaImage = new BufferedImage( COLOUR_AREA_WIDTH, COLOUR_AREA_HEIGHT,
                                                 BufferedImage.TYPE_INT_RGB );
            updateColourAreaImage( params.brightness );

            // Set component attributes
            setOpaque( true );
            setFocusable( false );

            // Add listeners
            addMouseListener( this );
            addMouseMotionListener( this );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : MouseListener interface
    ////////////////////////////////////////////////////////////////////

        public void mouseClicked( MouseEvent event )
        {
            // do nothing
        }

        //--------------------------------------------------------------

        public void mouseEntered( MouseEvent event )
        {
            // do nothing
        }

        //--------------------------------------------------------------

        public void mouseExited( MouseEvent event )
        {
            // do nothing
        }

        //--------------------------------------------------------------

        public void mousePressed( MouseEvent event )
        {
            setValues( event );
        }

        //--------------------------------------------------------------

        public void mouseReleased( MouseEvent event )
        {
            setValues( event );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : MouseMotionListener interface
    ////////////////////////////////////////////////////////////////////

        public void mouseDragged( MouseEvent event )
        {
            setValues( event );
        }

        //--------------------------------------------------------------

        public void mouseMoved( MouseEvent event )
        {
            // do nothing
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        public Dimension getPreferredSize( )
        {
            return new Dimension( LEFT_MARGIN + RIGHT_MARGIN + 2 * BORDER_WIDTH + COLOUR_AREA_WIDTH,
                                  TOP_MARGIN + BOTTOM_MARGIN + 2 * BORDER_WIDTH + COLOUR_AREA_HEIGHT );
        }

        //--------------------------------------------------------------

        @Override
        protected void paintComponent( Graphics gr )
        {
            // Fill background
            Rectangle rect = gr.getClipBounds( );
            gr.setColor( getBackground( ) );
            gr.fillRect( rect.x, rect.y, rect.width, rect.height );

            // Draw colour-area image
            gr.drawImage( colourAreaImage, COLOUR_AREA_X, COLOUR_AREA_Y, null );

            // Draw border
            gr.setColor( COLOUR_PANEL_BORDER_COLOUR );
            gr.drawRect( LEFT_MARGIN, TOP_MARGIN, 2 * BORDER_WIDTH + COLOUR_AREA_WIDTH - 1,
                         2 * BORDER_WIDTH + COLOUR_AREA_HEIGHT - 1 );

            // Draw range box
            int xh1 = COLOUR_AREA_X + hue1 / 2;
            int xh2 = COLOUR_AREA_X + hue2 / 2;
            if ( xh2 < xh1 )
                xh2 += COLOUR_AREA_HALF_WIDTH;
            int ys1 = COLOUR_AREA_Y + COLOUR_AREA_HEIGHT - 1 - saturation1;
            int ys2 = COLOUR_AREA_Y + COLOUR_AREA_HEIGHT - 1 - saturation2;
            int y1 = Math.min( ys1, ys2 );
            int y2 = Math.max( ys1, ys2 );
            gr.setColor( COLOUR_PANEL_RANGE_BOX_COLOUR );
            gr.setXORMode( COLOUR_PANEL_RANGE_BOX_XOR_COLOUR );
            gr.drawRect( xh1, y1, xh2 - xh1, y2 - y1 );

            // Draw hue marker 1
            gr.setPaintMode( );
            gr.setColor( COLOUR_PANEL_MARKER_COLOUR );
            int x1 = xh1 - MARKER_LENGTH + 1;
            int x2 = xh1 + MARKER_LENGTH - 1;
            int y = COLOUR_AREA_Y - BORDER_WIDTH - MARKER_LENGTH;
            while ( x1 <= x2 )
            {
                gr.drawLine( x1, y, x2, y );
                ++x1;
                --x2;
                ++y;
            }

            // Draw saturation marker 1
            int x = 0;
            y1 = ys1 - MARKER_LENGTH + 1;
            y2 = ys1 + MARKER_LENGTH - 1;
            while ( y1 <= y2 )
            {
                gr.drawLine( x, y1, x, y2 );
                ++x;
                ++y1;
                --y2;
            }

            // Draw hue marker 2
            int yh2 = COLOUR_AREA_Y + COLOUR_AREA_HEIGHT + BORDER_WIDTH + MARKER_LENGTH - 1;
            x1 = xh2 - MARKER_LENGTH + 1;
            x2 = xh2 + MARKER_LENGTH - 1;
            y = yh2;
            while ( x1 <= x2 )
            {
                gr.drawLine( x1, y, x2, y );
                ++x1;
                --x2;
                --y;
            }

            // Draw saturation marker 2
            int xs2 = COLOUR_AREA_X + COLOUR_AREA_WIDTH + BORDER_WIDTH + MARKER_LENGTH - 1;
            x = xs2;
            y1 = ys2 - MARKER_LENGTH + 1;
            y2 = ys2 + MARKER_LENGTH - 1;
            while ( y1 <= y2 )
            {
                gr.drawLine( x, y1, x, y2 );
                --x;
                ++y1;
                --y2;
            }

            // Draw lines on second hue and saturation markers
            gr.setColor( getBackground( ) );
            gr.drawLine( xh2, yh2, xh2, yh2 - 1 );
            gr.drawLine( xs2, ys2, xs2 - 1, ys2 );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods
    ////////////////////////////////////////////////////////////////////

        private void updateBrightness( int brightness )
        {
            updateColourAreaImage( brightness );
            repaint( );
        }

        //--------------------------------------------------------------

        private void setValues( Mode mode,
                                int  hue,
                                int  saturation )
        {
            switch ( mode )
            {
                case LIMIT1:
                    if ( (hue1 != hue) || (saturation1 != saturation) )
                    {
                        hue1 = hue;
                        saturation1 = saturation;
                        repaint( );
                    }
                    break;

                case LIMIT2:
                    if ( (hue2 != hue) || (saturation2 != saturation) )
                    {
                        hue2 = hue;
                        saturation2 = saturation;
                        repaint( );
                    }
                    break;
            }
        }

        //--------------------------------------------------------------

        private void updateColourAreaImage( int brightness )
        {
            for ( int y = 0; y < COLOUR_AREA_HEIGHT; ++y )
            {
                for ( int x = 0; x < COLOUR_AREA_WIDTH; ++x )
                {
                    float h = (float)x * H_FACTOR;
                    float s = (float)(COLOUR_AREA_HEIGHT - 1 - y) * S_FACTOR;
                    float b = (float)brightness / (float)MAX_BRIGHTNESS;
                    colourAreaImage.setRGB( x, y, Color.HSBtoRGB( h, s, b ) );
                }
            }
        }

        //--------------------------------------------------------------

        private void setValues( MouseEvent event )
        {
            int x = event.getX( );
            if ( x >= COLOUR_AREA_HALF_WIDTH )
                x -= COLOUR_AREA_HALF_WIDTH;
            int hue = Math.min( Math.max( MIN_HUE, (x - COLOUR_AREA_X) * 2 ), MAX_HUE );
            int saturation = Math.min( Math.max( MIN_SATURATION,
                                                 COLOUR_AREA_Y + COLOUR_AREA_HEIGHT - 1 - event.getY( ) ),
                                       MAX_SATURATION );
            Mode m = SwingUtilities.isLeftMouseButton( event ) ? mode : mode.getComplement( );
            setValues( m, hue, saturation );
            HueSaturationRangePanel.this.setValues( m, hue, saturation );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private int             hue1;
        private int             saturation1;
        private int             hue2;
        private int             saturation2;
        private BufferedImage   colourAreaImage;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    public HueSaturationRangePanel( Params params )
    {
        // Initialise instance variables
        mode = Mode.LIMIT1;


        //----  Colour panel

        colourPanel = new ColourPanel( params );


        //----  Control panel

        GridBagLayout gridBag = new GridBagLayout( );
        GridBagConstraints gbc = new GridBagConstraints( );
        JPanel controlPanel = new JPanel( gridBag );


        //----  Hue and saturation panel

        // Reset fixed-width radio buttons
        RadioButton.reset( );

        JPanel hueSatPanel = new JPanel( gridBag );
        GuiUtilities.setPaddedLineBorder( hueSatPanel );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( hueSatPanel, gbc );
        controlPanel.add( hueSatPanel );

        int gridY = 0;

        Map<Mode, Integer> hues = new EnumMap<>( Mode.class );
        hues.put( Mode.LIMIT1, params.hue1 );
        hues.put( Mode.LIMIT2, params.hue2 );
        Map<Mode, Integer> saturations = new EnumMap<>( Mode.class );
        saturations.put( Mode.LIMIT1, params.saturation1 );
        saturations.put( Mode.LIMIT2, params.saturation2 );

        hueSpinners = new EnumMap<>( Mode.class );
        saturationSpinners = new EnumMap<>( Mode.class );
        sampleBoxes = new EnumMap<>( Mode.class );
        ButtonGroup buttonGroup = new ButtonGroup( );

        for ( Mode mode : Mode.values( ) )
        {
            // Radio button: mode
            JRadioButton modeRadioButton = new RadioButton( mode.text );
            buttonGroup.add( modeRadioButton );
            modeRadioButton.setSelected( mode == this.mode );
            modeRadioButton.setActionCommand( Command.SET_MODE + mode );
            modeRadioButton.addActionListener( this );

            gbc.gridx = 0;
            gbc.gridy = gridY;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_END;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( modeRadioButton, gbc );
            hueSatPanel.add( modeRadioButton );

            // Panel: mode
            JPanel modePanel = new JPanel( gridBag );

            gbc.gridx = 1;
            gbc.gridy = gridY++;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( modePanel, gbc );
            hueSatPanel.add( modePanel );

            int gridX = 0;

            // Label: hue
            JLabel hueLabel = new FLabel( HUE_STR );

            gbc.gridx = gridX++;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_END;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets( 0, 6, 0, 0 );
            gridBag.setConstraints( hueLabel, gbc );
            modePanel.add( hueLabel );

            // Spinner: hue
            FIntegerSpinner hueSpinner = new FIntegerSpinner( hues.get( mode ), MIN_HUE, MAX_HUE,
                                                              HUE_FIELD_LENGTH );
            hueSpinners.put( mode, hueSpinner );
            hueSpinner.addChangeListener( this );

            gbc.gridx = gridX++;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets( 0, 6, 0, 0 );
            gridBag.setConstraints( hueSpinner, gbc );
            modePanel.add( hueSpinner );

            // Label: saturation
            JLabel saturationLabel = new FLabel( SATURATION_STR );

            gbc.gridx = gridX++;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_END;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets( 0, 12, 0, 0 );
            gridBag.setConstraints( saturationLabel, gbc );
            modePanel.add( saturationLabel );

            // Spinner: saturation
            FIntegerSpinner saturationSpinner = new FIntegerSpinner( saturations.get( mode ),
                                                                     MIN_SATURATION, MAX_SATURATION,
                                                                     SATURATION_FIELD_LENGTH );
            saturationSpinners.put( mode, saturationSpinner );
            saturationSpinner.addChangeListener( this );

            gbc.gridx = gridX++;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets( 0, 6, 0, 0 );
            gridBag.setConstraints( saturationSpinner, gbc );
            modePanel.add( saturationSpinner );

            // Sample box
            ColourSampleBox sampleBox = new ColourSampleBox( hsbToColour( hues.get( mode ),
                                                                          saturations.get( mode ),
                                                                          params.brightness ) );
            sampleBoxes.put( mode, sampleBox );

            gbc.gridx = gridX++;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets( 0, 14, 0, 0 );
            gridBag.setConstraints( sampleBox, gbc );
            modePanel.add( sampleBox );
        }

        // Update widths of radio buttons
        RadioButton.update( );


        //----  Brightness panel

        JPanel brightnessPanel = new JPanel( gridBag );
        GuiUtilities.setPaddedLineBorder( brightnessPanel );

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 3, 0, 0, 0 );
        gridBag.setConstraints( brightnessPanel, gbc );
        controlPanel.add( brightnessPanel );

        // Label: brightness
        JLabel brightnessLabel = new FLabel( BRIGHTNESS_STR );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( brightnessLabel, gbc );
        brightnessPanel.add( brightnessLabel );

        // Spinner-slider panel: brightness
        int sliderExtent = MAX_BRIGHTNESS - MIN_BRIGHTNESS + 1;
        int sliderWidth = HorizontalSlider.extentToWidth( sliderExtent, SLIDER_KNOB_WIDTH );
        brightnessSpinnerSlider = new IntegerSpinnerSliderPanel( params.brightness, MIN_BRIGHTNESS,
                                                                 MAX_BRIGHTNESS, BRIGHTNESS_FIELD_LENGTH,
                                                                 false, sliderWidth, SLIDER_HEIGHT,
                                                                 SLIDER_KNOB_WIDTH, MAX_BRIGHTNESS,
                                                                 SPINNER_SLIDER_PANEL_KEY );
        brightnessSpinnerSlider.getSpinner( ).addChangeListener( this );

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 6, 0, 0 );
        gridBag.setConstraints( brightnessSpinnerSlider, gbc );
        brightnessPanel.add( brightnessSpinnerSlider );


        //----  Outer panel

        setLayout( gridBag );

        gridY = 0;

        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( colourPanel, gbc );
        add( colourPanel );

        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 3, 0, 0, 0 );
        gridBag.setConstraints( controlPanel, gbc );
        add( controlPanel );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

    public static Color hsbToColour( int hue,
                                     int saturation,
                                     int brightness )
    {
        return new Color( Color.HSBtoRGB( (float)hue / (float)HUE_RANGE,
                                          (float)saturation / (float)MAX_SATURATION,
                                          (float)brightness / (float)MAX_BRIGHTNESS ) );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

    public void actionPerformed( ActionEvent event )
    {
        String command = event.getActionCommand( );

        if ( command.startsWith( Command.SET_MODE ) )
            onSetMode( StringUtilities.removePrefix( command, Command.SET_MODE ) );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ChangeListener interface
////////////////////////////////////////////////////////////////////////

    public void stateChanged( ChangeEvent event )
    {
        Object eventSource = event.getSource( );

        Mode mode = null;

        // Test for hue spinner
        for ( Mode m : hueSpinners.keySet( ) )
        {
            if ( eventSource == hueSpinners.get( m ) )
            {
                mode = m;
                break;
            }
        }

        // Test for saturation spinner
        if ( mode == null )
        {
            for ( Mode m : saturationSpinners.keySet( ) )
            {
                if ( eventSource == saturationSpinners.get( m ) )
                {
                    mode = m;
                    break;
                }
            }
        }

        int brightness = getBrightness( );
        if ( mode == null )
        {
            colourPanel.updateBrightness( brightness );
            for ( Mode m : sampleBoxes.keySet( ) )
                sampleBoxes.get( m ).setBackground( hsbToColour( getHue( m ), getSaturation( m ),
                                                                 brightness ) );
        }
        else
        {
            int hue = getHue( mode );
            int saturation = getSaturation( mode );
            colourPanel.setValues( mode, hue, saturation );
            sampleBoxes.get( mode ).setBackground( hsbToColour( hue, saturation, brightness ) );
        }
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    public Params getParams( )
    {
        return new Params( getHue( Mode.LIMIT1 ), getSaturation( Mode.LIMIT1 ), getHue( Mode.LIMIT2 ),
                           getSaturation( Mode.LIMIT2 ), getBrightness( ) );
    }

    //------------------------------------------------------------------

    private int getHue( Mode mode )
    {
        return hueSpinners.get( mode ).getIntValue( );
    }

    //------------------------------------------------------------------

    private int getSaturation( Mode mode )
    {
        return saturationSpinners.get( mode ).getIntValue( );
    }

    //------------------------------------------------------------------

    private int getBrightness( )
    {
        return brightnessSpinnerSlider.getValue( );
    }

    //------------------------------------------------------------------

    private void setValues( Mode mode,
                            int  hue,
                            int  saturation )
    {
        hueSpinners.get( mode ).setIntValue( hue );
        saturationSpinners.get( mode ).setIntValue( saturation );
    }

    //------------------------------------------------------------------

    private void onSetMode( String key )
    {
        for ( Mode m : Mode.values( ) )
        {
            if ( m.toString( ).equals( key ) )
            {
                mode = m;
                break;
            }
        }
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    private Mode                        mode;
    private ColourPanel                 colourPanel;
    private Map<Mode, FIntegerSpinner>  hueSpinners;
    private Map<Mode, FIntegerSpinner>  saturationSpinners;
    private Map<Mode, ColourSampleBox>  sampleBoxes;
    private IntegerSpinnerSliderPanel   brightnessSpinnerSlider;

}

//----------------------------------------------------------------------
