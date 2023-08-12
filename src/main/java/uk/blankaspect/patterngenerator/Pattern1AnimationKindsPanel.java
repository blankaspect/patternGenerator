/*====================================================================*\

Pattern1AnimationKindsPanel.java

Pattern 1 animation kinds panel class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.patterngenerator;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.colour.Colours;

import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.misc.GuiConstants;
import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.text.TextRendering;

//----------------------------------------------------------------------


// PATTERN 1 ANIMATION KINDS PANEL CLASS


class Pattern1AnimationKindsPanel
	extends JPanel
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		ICON_MARGIN	= 2;
	private static final	int		ICON_WIDTH	= 2 * ICON_MARGIN + Icons.CROSS_10_10.getIconWidth();
	private static final	int		ICON_HEIGHT	= 2 * ICON_MARGIN + Icons.CROSS_10_10.getIconHeight();

	private static final	int		VERTICAL_MARGIN	= 3;
	private static final	int		LEFT_MARGIN		= 4;
	private static final	int		RIGHT_MARGIN	= 8;
	private static final	int		HORIZONTAL_GAP	= 6;

	private static final	Color	BORDER_COLOUR					= new Color(160, 176, 160);
	private static final	Color	SELECTED_BORDER_COLOUR			= new Color(224, 144, 96);
	private static final	Color	FOCUSED_BORDER_COLOUR1			= Color.WHITE;
	private static final	Color	FOCUSED_BORDER_COLOUR2			= Color.BLACK;
	private static final	Color	DISABLED_BORDER_COLOUR			= Colours.LINE_BORDER;
	private static final	Color	BACKGROUND_COLOUR				= new Color(224, 232, 224);
	private static final	Color	SELECTED_BACKGROUND_COLOUR		= Colours.FOCUSED_SELECTION_BACKGROUND;
	private static final	Color	HIGHLIGHTED_BACKGROUND_COLOUR	= new Color(255, 248, 192);
	private static final	Color	FOREGROUND_COLOUR				= Color.BLACK;
	private static final	Color	DISABLED_FOREGROUND_COLOUR		= Colours.LINE_BORDER;

	// Commands
	private interface Command
	{
		String	TOGGLE_ANIMATION_KIND	= "toggleAnimationKind.";
	}

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CHECK BOX CLASS


	private class CheckBox
		extends JToggleButton
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CheckBox(String text)
		{
			// Initialise instance variables
			AppFont.MAIN.apply(this);
			FontMetrics fontMetrics = getFontMetrics(getFont());
			width = LEFT_MARGIN + ICON_WIDTH + HORIZONTAL_GAP + fontMetrics.stringWidth(text) + RIGHT_MARGIN;
			height = 2 * VERTICAL_MARGIN + Math.max(ICON_HEIGHT, fontMetrics.getAscent() + fontMetrics.getDescent());
			if ((height - ICON_HEIGHT) % 2 != 0)
				++height;

			// Set attributes
			setText(text);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension(width, height);
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Create copy of graphics context
			Graphics2D gr2d = GuiUtils.copyGraphicsContext(gr);

			// Draw background
			int width = getWidth();
			int height = getHeight();
			gr2d.setColor(isEnabled()
								? isSelected()
										? SELECTED_BACKGROUND_COLOUR
										: BACKGROUND_COLOUR
								: getBackground());
			gr2d.fillRect(0, 0, width, height);

			// Draw button background
			int x = LEFT_MARGIN;
			int y = (height - ICON_HEIGHT) / 2;
			if (getModel().isArmed())
			{
				gr2d.setColor(HIGHLIGHTED_BACKGROUND_COLOUR);
				gr2d.fillRect(x + 1, y + 1, ICON_WIDTH - 2, ICON_HEIGHT - 2);
			}

			// Draw button border
			gr2d.setColor(isEnabled()
								? isSelected()
										? SELECTED_BORDER_COLOUR
										: BORDER_COLOUR
								: DISABLED_BORDER_COLOUR);
			gr2d.drawRect(x, y, ICON_WIDTH - 1, ICON_HEIGHT - 1);

			// Draw cross
			if (isSelected())
				gr2d.drawImage(Icons.CROSS_10_10.getImage(), LEFT_MARGIN + ICON_MARGIN, y + ICON_MARGIN, null);

			// Set rendering hints for text antialiasing and fractional metrics
			TextRendering.setHints(gr2d);

			// Draw text
			gr2d.setColor(isEnabled() ? FOREGROUND_COLOUR : DISABLED_FOREGROUND_COLOUR);
			gr2d.drawString(getText(), LEFT_MARGIN + ICON_WIDTH + HORIZONTAL_GAP,
							FontUtils.getBaselineOffset(height, gr2d.getFontMetrics()));

			// Draw border
			gr2d.setColor(isEnabled()
								? isSelected()
										? SELECTED_BORDER_COLOUR
										: BORDER_COLOUR
								: DISABLED_BORDER_COLOUR);
			gr2d.drawRect(0, 0, width - 1, height - 1);
			if (isFocusOwner())
			{
				gr2d.setColor(FOCUSED_BORDER_COLOUR1);
				gr2d.drawRect(1, 1, width - 3, height - 3);

				gr2d.setStroke(GuiConstants.BASIC_DASH);
				gr2d.setColor(FOCUSED_BORDER_COLOUR2);
				gr2d.drawRect(1, 1, width - 3, height - 3);
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int	width;
		private	int	height;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public Pattern1AnimationKindsPanel(Set<Pattern1Image.AnimationKind> enabledKinds,
									   Set<Pattern1Image.AnimationKind> selectedKinds)
	{
		// Set layout manager
		setLayout(new GridLayout(0, 1, 0, 6));

		// Check boxes
		checkBoxes = new EnumMap<>(Pattern1Image.AnimationKind.class);

		List<KeyAction.KeyCommandPair> keyCommands = new ArrayList<>();
		for (Pattern1Image.AnimationKind animationKind : Pattern1Image.AnimationKind.values())
		{
			String command = Command.TOGGLE_ANIMATION_KIND + animationKind.getKey();

			CheckBox checkBox = new CheckBox(animationKind.toString());
			checkBox.setEnabled(enabledKinds.contains(animationKind));
			checkBox.setSelected(checkBox.isEnabled() && selectedKinds.contains(animationKind));
			checkBox.setActionCommand(command);
			checkBox.addActionListener(this);
			checkBoxes.put(animationKind, checkBox);
			add(checkBox);

			if (checkBox.isEnabled())
			{
				Character ch = (char)('1' + animationKind.ordinal());
				keyCommands.add(new KeyAction.KeyCommandPair(KeyStroke.getKeyStroke(ch, 0), command));
			}
		}

		// Add commands to action map
		KeyAction.create(this, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this, keyCommands);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.startsWith(Command.TOGGLE_ANIMATION_KIND))
			onToggleAnimationKind(StringUtils.removePrefix(command, Command.TOGGLE_ANIMATION_KIND),
								  event.getSource() == null);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public Set<Pattern1Image.AnimationKind> getAnimationKinds()
	{
		Set<Pattern1Image.AnimationKind> animationKinds =
														EnumSet.noneOf(Pattern1Image.AnimationKind.class);
		for (Pattern1Image.AnimationKind animationKind : checkBoxes.keySet())
		{
			if (checkBoxes.get(animationKind).isSelected())
				animationKinds.add(animationKind);
		}
		return animationKinds;
	}

	//------------------------------------------------------------------

	protected void animationKindsChanged()
	{
		// may be overridden in subclass
	}

	//------------------------------------------------------------------

	private void onToggleAnimationKind(String  key,
									   boolean keyPress)
	{
		if (keyPress)
		{
			CheckBox checkBox = checkBoxes.get(Pattern1Image.AnimationKind.forKey(key));
			checkBox.setSelected(!checkBox.isSelected());
		}
		animationKindsChanged();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Map<Pattern1Image.AnimationKind, CheckBox>	checkBoxes;

}

//----------------------------------------------------------------------
