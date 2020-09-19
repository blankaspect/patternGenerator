/*====================================================================*\

MainWindow.java

Main window class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Dimension;
import java.awt.Frame;

import java.awt.datatransfer.UnsupportedFlavorException;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;
import java.io.IOException;

import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.swing.menu.FCheckBoxMenuItem;
import uk.blankaspect.common.swing.menu.FMenu;
import uk.blankaspect.common.swing.menu.FMenuItem;

import uk.blankaspect.common.swing.misc.GuiUtils;

import uk.blankaspect.common.swing.tabbedpane.TabbedPane;

import uk.blankaspect.common.swing.transfer.DataImporter;

//----------------------------------------------------------------------


// MAIN WINDOW CLASS


class MainWindow
	extends JFrame
	implements ChangeListener, MenuListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int	DEFAULT_WIDTH	= 480;
	public static final		int	DEFAULT_HEIGHT	= 360;

	private static final	String	NEW_STR	= "New";

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// MENUS


	private enum Menu
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		FILE
		(
			"File",
			KeyEvent.VK_F
		)
		{
			protected void update()
			{
				getMenu().getMenuComponent(0).setEnabled(!App.INSTANCE.isDocumentsFull());
				updateAppCommands();
			}
		},

		EDIT
		(
			"Edit",
			KeyEvent.VK_E
		)
		{
			protected void update()
			{
				PatternDocument document = App.INSTANCE.getDocument();
				getMenu().setEnabled((document != null) && !document.isPlaying());
				updateDocumentCommands();
			}
		},

		PATTERN
		(
			"Pattern",
			KeyEvent.VK_P
		)
		{
			protected void update()
			{
				PatternDocument document = App.INSTANCE.getDocument();
				getMenu().setEnabled((document != null) && !document.isPlaying());
				updateDocumentCommands();
			}
		},

		SEQUENCE
		(
			"Sequence",
			KeyEvent.VK_S
		)
		{
			protected void update()
			{
				getMenu().setEnabled(App.INSTANCE.hasDocuments());
				updateDocumentCommands();
			}
		},

		VIEW
		(
			"View",
			KeyEvent.VK_V
		)
		{
			protected void update()
			{
				getMenu().setEnabled(App.INSTANCE.hasDocuments());
				updateDocumentCommands();
			}
		},

		OPTIONS
		(
			"Options",
			KeyEvent.VK_O
		)
		{
			protected void update()
			{
				updateAppCommands();
			}
		};

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Menu(String text,
					 int    keyCode)
		{
			menu = new FMenu(text, keyCode);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static void updateAppCommands()
		{
			App.INSTANCE.updateCommands();
		}

		//--------------------------------------------------------------

		private static void updateDocumentCommands()
		{
			PatternDocument document = App.INSTANCE.getDocument();
			if (document == null)
				PatternDocument.Command.setAllEnabled(false);
			else
				document.updateCommands();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		protected abstract void update();

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		protected JMenu getMenu()
		{
			return menu;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	JMenu	menu;

	}

	//==================================================================


	// ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		FILE_TRANSFER_NOT_SUPPORTED
		("File transfer is not supported."),

		ERROR_TRANSFERRING_DATA
		("An error occurred while transferring data.");

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ErrorId(String message)
		{
			this.message = message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : AppException.IId interface
	////////////////////////////////////////////////////////////////////

		public String getMessage()
		{
			return message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	message;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLOSE ACTION CLASS


	private static class CloseAction
		extends AbstractAction
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CloseAction()
		{
			putValue(Action.ACTION_COMMAND_KEY, "");
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		public void actionPerformed(ActionEvent event)
		{
			App.INSTANCE.closeDocument(Integer.parseInt(event.getActionCommand()));
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// FILE TRANSFER HANDLER CLASS


	private class FileTransferHandler
		extends TransferHandler
		implements Runnable
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public FileTransferHandler()
		{
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Runnable interface
	////////////////////////////////////////////////////////////////////

		public void run()
		{
			AppCommand.IMPORT_FILES.execute();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public boolean canImport(TransferHandler.TransferSupport support)
		{
			boolean supported = !support.isDrop() || ((support.getSourceDropActions() & COPY) == COPY);
			if (supported)
				supported = DataImporter.isFileList(support.getDataFlavors());
			if (support.isDrop() && supported)
				support.setDropAction(COPY);
			return supported;
		}

		//--------------------------------------------------------------

		@Override
		public boolean importData(TransferHandler.TransferSupport support)
		{
			if (canImport(support))
			{
				try
				{
					try
					{
						List<File> files = DataImporter.getFiles(support.getTransferable());
						if (!files.isEmpty())
						{
							toFront();
							AppCommand.IMPORT_FILES.putValue(AppCommand.Property.FILES, files);
							SwingUtilities.invokeLater(this);
							return true;
						}
					}
					catch (UnsupportedFlavorException e)
					{
						throw new AppException(ErrorId.FILE_TRANSFER_NOT_SUPPORTED);
					}
					catch (IOException e)
					{
						throw new AppException(ErrorId.ERROR_TRANSFERRING_DATA);
					}
				}
				catch (AppException e)
				{
					App.INSTANCE.showErrorMessage(App.SHORT_NAME, e);
				}
			}
			return false;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public MainWindow()
	{
		// Set icons
		setIconImages(Images.APP_ICON_IMAGES);


		//----  Menu bar

		JMenuBar menuBar = new JMenuBar();
		menuBar.setBorder(null);

		// File menu
		JMenu menu = Menu.FILE.menu;
		menu.addMenuListener(this);

		JMenu submenu = new FMenu(NEW_STR, KeyEvent.VK_N);

		submenu.add(new FMenuItem(AppCommand.CREATE_PATTERN1, KeyEvent.VK_1));
		submenu.add(new FMenuItem(AppCommand.CREATE_PATTERN2, KeyEvent.VK_2));

		menu.add(submenu);

		menu.add(new FMenuItem(AppCommand.OPEN_FILE, KeyEvent.VK_O));
		menu.add(new FMenuItem(AppCommand.REVERT_FILE, KeyEvent.VK_R));

		menu.addSeparator();

		menu.add(new FMenuItem(AppCommand.CLOSE_FILE, KeyEvent.VK_C));
		menu.add(new FMenuItem(AppCommand.CLOSE_ALL_FILES, KeyEvent.VK_L));

		menu.addSeparator();

		menu.add(new FMenuItem(AppCommand.SAVE_FILE, KeyEvent.VK_S));
		menu.add(new FMenuItem(AppCommand.SAVE_FILE_AS, KeyEvent.VK_A));

		menu.addSeparator();

		menu.add(new FMenuItem(AppCommand.EXPORT_IMAGE, KeyEvent.VK_I));
		menu.add(new FMenuItem(AppCommand.EXPORT_IMAGE_SEQUENCE, KeyEvent.VK_M));
		menu.add(new FMenuItem(AppCommand.EXPORT_AS_SVG, KeyEvent.VK_V));

		menu.addSeparator();

		menu.add(new FMenuItem(AppCommand.EXIT, KeyEvent.VK_X));

		menuBar.add(menu);

		// Edit menu
		menu = Menu.EDIT.menu;
		menu.addMenuListener(this);

		menu.add(new FMenuItem(PatternDocument.Command.UNDO, KeyEvent.VK_U));
		menu.add(new FMenuItem(PatternDocument.Command.REDO, KeyEvent.VK_R));
		menu.add(new FMenuItem(PatternDocument.Command.CLEAR_EDIT_LIST, KeyEvent.VK_L));

		menu.addSeparator();

		menu.add(new FMenuItem(PatternDocument.Command.EDIT_PATTERN_PARAMETERS, KeyEvent.VK_E));
		menu.add(new FMenuItem(PatternDocument.Command.EDIT_DESCRIPTION, KeyEvent.VK_D));

		menuBar.add(menu);

		// Pattern menu
		menu = Menu.PATTERN.menu;
		menu.addMenuListener(this);

		menu.add(new FMenuItem(PatternDocument.Command.REGENERATE_PATTERN_WITH_NEW_SEED, KeyEvent.VK_R));

		menu.addSeparator();

		menu.add(new FMenuItem(PatternDocument.Command.SHOW_IMAGE_RENDERING_TIME, KeyEvent.VK_S));

		menuBar.add(menu);

		// Sequence menu
		menu = Menu.SEQUENCE.menu;
		menu.addMenuListener(this);

		menu.add(new FMenuItem(PatternDocument.Command.START_SLIDE_SHOW, KeyEvent.VK_S));
		menu.add(new FMenuItem(PatternDocument.Command.START_ANIMATION, KeyEvent.VK_A));

		menu.addSeparator();

		menu.add(new FMenuItem(PatternDocument.Command.OPTIMISE_ANIMATION, KeyEvent.VK_O));

		menuBar.add(menu);

		// View menu
		menu = Menu.VIEW.menu;
		menu.addMenuListener(this);

		menu.add(new FMenuItem(PatternDocument.Command.RESIZE_WINDOW_TO_IMAGE, KeyEvent.VK_R));

		menuBar.add(menu);

		// Options menu
		menu = Menu.OPTIONS.menu;
		menu.addMenuListener(this);

		menu.add(new FMenuItem(AppCommand.EDIT_PREFERENCES, KeyEvent.VK_P));
		menu.add(new FCheckBoxMenuItem(AppCommand.TOGGLE_SHOW_FULL_PATHNAMES, KeyEvent.VK_F));

		menuBar.add(menu);

		// Set menu bar
		setJMenuBar(menuBar);


		//----  Tabbed panel

		tabbedPanel = new TabbedPane();
		tabbedPanel.setIgnoreCase(true);
		tabbedPanel.addChangeListener(this);

		// Set transfer handler
		tabbedPanel.setTransferHandler(new FileTransferHandler());


		//----  Window

		// Set content pane
		setContentPane(tabbedPanel);

		// Dispose of window explicitly
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// Handle window closing
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent event)
			{
				AppCommand.EXIT.execute();
			}
		});

		// Resize window to its preferred size
		pack();

		// Set mininum size of window
		setMinimumSize(getPreferredSize());

		// Set location of window
		AppConfig config = AppConfig.INSTANCE;
		if (config.isMainWindowLocation())
			setLocation(GuiUtils.getLocationWithinScreen(this, config.getMainWindowLocation()));

		// Set size of window
		Dimension size = config.getMainWindowSize();
		if ((size != null) && (size.width > 0) && (size.height > 0))
			setSize(size);

		// Update title and menus
		updateTitleAndMenus();

		// Make window visible
		setVisible(true);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ChangeListener interface
////////////////////////////////////////////////////////////////////////

	public void stateChanged(ChangeEvent event)
	{
		if (event.getSource() == tabbedPanel)
		{
			if (isVisible())
				updateTitleAndMenus();
			PatternDocument.updateRenderingTime();
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : MenuListener interface
////////////////////////////////////////////////////////////////////////

	public void menuCanceled(MenuEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	public void menuDeselected(MenuEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	public void menuSelected(MenuEvent event)
	{
		Object eventSource = event.getSource();
		for (Menu menu : Menu.values())
		{
			if (eventSource == menu.menu)
				menu.update();
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int getTabIndex()
	{
		return tabbedPanel.getSelectedIndex();
	}

	//------------------------------------------------------------------

	public void addView(String      title,
						String      tooltipText,
						PatternView view)
	{
		tabbedPanel.addComponent(title, new CloseAction(), view);
		int index = tabbedPanel.getNumTabs() - 1;
		tabbedPanel.setTooltipText(index, tooltipText);
		tabbedPanel.setSelectedIndex(index);
	}

	//------------------------------------------------------------------

	public void removeView(int index)
	{
		tabbedPanel.removeComponent(index);
	}

	//------------------------------------------------------------------

	public void setView(int         index,
						PatternView view)
	{
		tabbedPanel.setComponent(index, view);
	}

	//------------------------------------------------------------------

	public void selectView(int index)
	{
		tabbedPanel.setSelectedIndex(index);
	}

	//------------------------------------------------------------------

	public void setTabText(int    index,
						   String title,
						   String tooltipText)
	{
		tabbedPanel.setTitle(index, title);
		tabbedPanel.setTooltipText(index, tooltipText);
	}

	//------------------------------------------------------------------

	public boolean isMaximised()
	{
		return ((getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH);
	}

	//------------------------------------------------------------------

	public void resize()
	{
		if (!isMaximised())
			pack();
	}

	//------------------------------------------------------------------

	public void updateTitleAndMenus()
	{
		updateTitle();
		updateMenus();
	}

	//------------------------------------------------------------------

	private void updateTitle()
	{
		PatternDocument document = App.INSTANCE.getDocument();
		boolean fullPathname = AppConfig.INSTANCE.isShowFullPathnames();
		setTitle((document == null) ? App.LONG_NAME + " " + App.INSTANCE.getVersionString()
									: App.SHORT_NAME + " - " + document.getTitleString(fullPathname));
	}

	//------------------------------------------------------------------

	private void updateMenus()
	{
		for (Menu menu : Menu.values())
			menu.update();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	TabbedPane	tabbedPanel;

}

//----------------------------------------------------------------------
