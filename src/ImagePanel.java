/*====================================================================*\

ImagePanel.java

Image panel class.

\*====================================================================*/


// IMPORTS


import java.awt.Dimension;
import java.awt.Graphics;

import java.awt.image.BufferedImage;

import javax.swing.JComponent;

//----------------------------------------------------------------------


// IMAGE PANEL CLASS


class ImagePanel
    extends JComponent
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    public ImagePanel( BufferedImage image )
    {
        // Initialise instance variables
        this.image = image;

        // Set component attributes
        setOpaque( true );
        setFocusable( false );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

    @Override
    public Dimension getPreferredSize( )
    {
        return new Dimension( image.getWidth( ), image.getHeight( ) );
    }

    //------------------------------------------------------------------

    @Override
    protected void paintComponent( Graphics gr )
    {
        gr.drawImage( image, 0, 0, null );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    public BufferedImage getImage( )
    {
        return image;
    }

    //------------------------------------------------------------------

    public void setImage( BufferedImage image )
    {
        Dimension oldSize = getPreferredSize( );
        this.image = image;
        if ( !getPreferredSize( ).equals( oldSize ) )
            revalidate( );
        repaint( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    private BufferedImage   image;

}

//----------------------------------------------------------------------
