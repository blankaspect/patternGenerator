/*====================================================================*\

AppCommand.java

Application command enumeration.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.KeyStroke;

import uk.blankaspect.common.swing.action.Command;

//----------------------------------------------------------------------


// APPLICATION COMMAND ENUMERATION


enum AppCommand
	implements Action
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	// Commands

	CHECK_MODIFIED_FILE
	(
		"checkModifiedFile"
	),

	IMPORT_FILES
	(
		"importFiles"
	),

	CREATE_PATTERN1
	(
		"createPattern1",
		"Pattern 1",
		KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK)
	),

	CREATE_PATTERN2
	(
		"createPattern2",
		"Pattern 2",
		KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK)
	),

	OPEN_FILE
	(
		"openFile",
		"Open file" + AppConstants.ELLIPSIS_STR,
		KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK)
	),

	REVERT_FILE
	(
		"revertFile",
		"Revert file"
	),

	CLOSE_FILE
	(
		"closeFile",
		"Close file",
		KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK)
	),

	CLOSE_ALL_FILES
	(
		"closeAllFiles",
		"Close all files"
	),

	SAVE_FILE
	(
		"saveFile",
		"Save file",
		KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK)
	),

	SAVE_FILE_AS
	(
		"saveFileAs",
		"Save file as" + AppConstants.ELLIPSIS_STR
	),

	EXPORT_IMAGE
	(
		"exportImage",
		"Export image" + AppConstants.ELLIPSIS_STR,
		KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK)
	),

	EXPORT_IMAGE_SEQUENCE
	(
		"exportImageSequence",
		"Export image sequence" + AppConstants.ELLIPSIS_STR,
		KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK)
	),

	EXPORT_AS_SVG
	(
		"exportAsSvg",
		"Export as SVG file" + AppConstants.ELLIPSIS_STR
	),

	EXIT
	(
		"exit",
		"Exit"
	),

	EDIT_PREFERENCES
	(
		"editPreferences",
		"Preferences" + AppConstants.ELLIPSIS_STR
	),

	TOGGLE_SHOW_FULL_PATHNAMES
	(
		"toggleShowFullPathnames",
		"Show full pathnames"
	);

	//------------------------------------------------------------------

	// Property keys
	interface Property
	{
		String	FILES	= "files";
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private AppCommand(String key)
	{
		command = new Command(this);
		putValue(Action.ACTION_COMMAND_KEY, key);
	}

	//------------------------------------------------------------------

	private AppCommand(String key,
					   String name)
	{
		this(key);
		putValue(Action.NAME, name);
	}

	//------------------------------------------------------------------

	private AppCommand(String    key,
					   String    name,
					   KeyStroke acceleratorKey)
	{
		this(key, name);
		putValue(Action.ACCELERATOR_KEY, acceleratorKey);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : Action interface
////////////////////////////////////////////////////////////////////////

	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		command.addPropertyChangeListener(listener);
	}

	//------------------------------------------------------------------

	public Object getValue(String key)
	{
		return command.getValue(key);
	}

	//------------------------------------------------------------------

	public boolean isEnabled()
	{
		return command.isEnabled();
	}

	//------------------------------------------------------------------

	public void putValue(String key,
						 Object value)
	{
		command.putValue(key, value);
	}

	//------------------------------------------------------------------

	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		command.removePropertyChangeListener(listener);
	}

	//------------------------------------------------------------------

	public void setEnabled(boolean enabled)
	{
		command.setEnabled(enabled);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		App.INSTANCE.executeCommand(this);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public void setSelected(boolean selected)
	{
		putValue(Action.SELECTED_KEY, selected);
	}

	//------------------------------------------------------------------

	public void execute()
	{
		actionPerformed(null);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Command	command;

}

//----------------------------------------------------------------------
