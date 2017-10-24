/*====================================================================*\

SeedPanel.java

Random sequence seed panel class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import uk.blankaspect.common.gui.FButton;
import uk.blankaspect.common.gui.GuiUtils;

import uk.blankaspect.common.textfield.LongField;

//----------------------------------------------------------------------


// RANDOM SEQUENCE SEED PANEL CLASS


class SeedPanel
	extends JPanel
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	FIELD_LENGTH	= App.MAX_NUM_SEED_DIGITS;

	private static final	Insets	BUTTON_MARGINS	= new Insets(1, 4, 1, 4);

	private static final	String	RANDOM_STR	= "Random";

	// Commands
	private interface Command
	{
		String	RANDOMISE	= "randomise";
	}

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// SEED FIELD


	private static class SeedField
		extends LongField.Unsigned
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private SeedField()
		{
			super(FIELD_LENGTH);
			AppFont.TEXT_FIELD.apply(this);
			GuiUtils.setTextComponentMargins(this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected int getColumnWidth()
		{
			return (GuiUtils.getCharWidth('0', getFontMetrics(getFont())) + 1);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public SeedPanel(Long seed)
	{
		// Set layout manager
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout(gridBag);

		// Field: seed
		field = new SeedField();
		if (seed != null)
			field.setValue(seed);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(field, gbc);
		add(field);

		// Button: random
		JButton randomButton = new FButton(RANDOM_STR);
		randomButton.setMargin(BUTTON_MARGINS);
		randomButton.setActionCommand(Command.RANDOMISE);
		randomButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 6, 0, 0);
		gridBag.setConstraints(randomButton, gbc);
		add(randomButton);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals(Command.RANDOMISE))
			onRandomise();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public LongField getField()
	{
		return field;
	}

	//------------------------------------------------------------------

	/**
	 * @throws NumberFormatException
	 */

	public Long getSeed()
	{
		return (field.isEmpty() ? null : field.getValue());
	}

	//------------------------------------------------------------------

	public void setSeed(Long seed)
	{
		if (seed == null)
			field.setText(null);
		else
			field.setValue(seed);
	}

	//------------------------------------------------------------------

	private void onRandomise()
	{
		field.setValue(App.INSTANCE.getNextRandomSeed());
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	private	SeedField	field;

}

//----------------------------------------------------------------------
