/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui.commandlist;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import javax.swing.*;

import lu.lippmann.cdb.common.LookAndFeelUtil;
import lu.lippmann.cdb.common.mvp.Listener;

import org.jdesktop.swingx.JXPanel;
import org.pushingpixels.substance.api.SubstanceConstants;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;


/**
 * A Commandlist is a panel with predefined buttons to control the application.
 * 
 * @author heinesch
 * 
 */
public class CommandListImpl extends JXPanel implements CommandList {
	
	private static final long serialVersionUID = -834057763397421093L;
	
	/** vertical or horizontal alignment */
	private boolean vertical;
	/** suppress default focus */
	private boolean noFocus;
	/** show confirm dialog */
	private boolean confirm;
	/** listeners */
	private final List<Listener<CommandButton>> listeners;
	/** active button list */
	private Set<CommandButton> cbList;
	/** map to retrieve the {@link JButton} */
	private Map<CommandButton, JButton> cbMap;
	/** Selected button */
	private CommandButton selectedButton;
	
	private String cTitle, cText;
	
	/**
	 * Creates a {@link CommandList} using the given buttons.
	 * 
	 * @param buttonList
	 */
	public CommandListImpl(CommandButton[] buttonList) {
		this(false, buttonList);
	}
	
	/**
	 * Creates a {@link CommandList} using the given buttons.
	 * 
	 * @param noFocus
	 *            suppress default focus
	 * @param buttonList
	 */
	public CommandListImpl(boolean noFocus, CommandButton[] buttonList) {
		this.cTitle = null;
		this.cText = null;
		this.noFocus = noFocus;
		this.confirm = false;
		this.listeners = new LinkedList<Listener<CommandButton>>();
		setButtonList(buttonList);
	}
	
	/**
	 * Compute the order and focus of the buttons
	 * 
	 * @param buttonList
	 */
	private void processButtonList(CommandButton[] buttonList) {
		if (buttonList == null || buttonList.length == 0)
			return;

		final SortedSet<CommandButton> selected = new TreeSet<CommandButton>(
				new CommandButton.CommandButtomSelectionComparator());
		
		for (final CommandButton butt : buttonList) {
			cbList.add(butt);
			selected.add(butt);
		}
		this.selectedButton = selected.first();
	}
	
	/**
	 * Draw the commandlist
	 */
	private void initializeComponent() {
		this.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		this.setOpaque(false);
		
		if (cbList.size()==0)
			return;

		final JPanel rootPan = new JPanel();
		rootPan.setLayout(new GridBagLayout());
		rootPan.setOpaque(false);
		
		int count = 0;
		// create Buttons
		for (final CommandButton cb : cbList) {
			final CommandButton aButt = cb;
			final JButton butt = new JButton(cb.getLabel());
			//butt.setPreferredSize(ViewConstants.BUTTONSIZE_LARGE);
			cbMap.put(cb, butt);
			butt.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!confirm || showConfirmDialog()) {
						for (final Listener<CommandButton> l : listeners) {
							l.onAction(aButt);
						}
					}
				}
			});
			butt.setSelected(false);
			
			final GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.BASELINE;
			c.gridx = 0;
			c.gridy = 0;
			if (vertical) {
				c.gridy = count;
			} else {
				c.gridx = count + 1;
			}
			
			final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER,
					0, 0));
			panel.setOpaque(false);
			panel.add(butt);
			rootPan.add(panel, c);
			count++;
			
			//add style to the button
			//GUI ENHANCEMENT
			setStyle(butt,cb);
		}
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.BASELINE;
		c.gridx = 0;
		c.gridy = 0;
		if (vertical) {
			c.gridy = count;
		} else {
			// c.gridx = count;
			c.gridx = 0;
		}
		c.weightx=1;
		c.weighty=1;
		JPanel empty = new JPanel();
		empty.setOpaque(false);
		rootPan.add(empty,c);

		if (selectedButton!=null || !noFocus) {
			cbMap.get(selectedButton).requestFocusInWindow();
		}
		this.add(rootPan);
	}
	
	/**
	 * Displays a confimation dialog.
	 * 
	 * @return true if the user confirmed
	 */
	private boolean showConfirmDialog() {
		String title = "Confirmation";
		String text = "Do yo want to confirm?";
		
		if (cTitle != null) {
			title = cTitle;
		}
		if (cText != null) {
			text = cText;
		}
		
		final int n = JOptionPane.showConfirmDialog(this, text, title,
				JOptionPane.YES_NO_OPTION);
		
		return (n == JOptionPane.YES_OPTION);
		
	}
	
	/**
	 * Focuses the given button
	 * 
	 * @param button
	 */
	public void setFocus(CommandButton button) {
		final JButton butt = cbMap.get(button);
		if (butt != null) {
			butt.requestFocusInWindow();
		}
	}
	
	/**
	 * Highlights the given button
	 * 
	 * @param button
	 */
	public void setSelected(CommandButton button) {
		final JButton butt = cbMap.get(button);
		if (butt != null) {
			butt.setSelected(true);
		}
	}
	
	/**
	 * 
	 * @param button
	 * @param enabled
	 */
	public void setEnabled(CommandButton button, boolean enabled) {
		final JButton butt = cbMap.get(button);
		if (butt != null) {
			butt.setEnabled(enabled);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component asComponent() {
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addListener(Listener<CommandButton> listener) {
		listeners.add(listener);
	}
	
	/**
	 * Sets the alignment of this commandlist.
	 * 
	 * @param vertical
	 */
	public void setVerticalAlignement(boolean vertical) {
		this.vertical = vertical;
		this.removeAll();
		initializeComponent();
	}
	
	public void setButtonList(CommandButton[] buttonList) {
		this.removeAll();
		this.cbMap = new HashMap<CommandButton, JButton>();
		this.cbList = new TreeSet<CommandButton>(
				new CommandButton.CommandButtomPositionComparator());
		processButtonList(buttonList);
		initializeComponent();
	}
	
	/**
	 * Enables a confirmation dialog
	 * 
	 * @param required
	 */
	public void setConfirmationRequired(boolean required) {
		this.confirm = required;
	}
	
	/**
	 * Sets the title and text of the confirmation message.
	 * 
	 * @param title
	 * @param text
	 */
	public void setConfirmationText(String title, String text) {
		this.cTitle = title;
		this.cText = text;
	}
	
	/**
	 * Test main
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) 
	{
		SwingUtilities.invokeLater(new Runnable() 
		{
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void run() {
				
				LookAndFeelUtil.init();

				final JFrame frame = new JFrame();
				frame.setLayout(new BorderLayout());
				
				final CommandButton[] l = { CommandButton.OK,
						CommandButton.CANCEL };
				final CommandListImpl c1 = new CommandListImpl(l);
				c1.setConfirmationRequired(true);
				c1.addListener(new Listener<CommandButton>() {
					
					@Override
					public void onAction(CommandButton parameter) {
						System.out.println(parameter);
						
					}
				});
				frame.add(c1, BorderLayout.SOUTH);
				
				final CommandButton[] l2 = { CommandButton.YES,
						CommandButton.NO, CommandButton.CANCEL };
				final CommandListImpl c2 = new CommandListImpl(l2);
				c2.setVerticalAlignement(true);
				
				frame.add(c2, BorderLayout.EAST);
				
				/*
				 * CommandButton[] l3 =
				 * {CommandButton.YES,CommandButton.NO,CommandButton.CANCEL,
				 * CommandButton
				 * .ADD,CommandButton.APPLY,CommandButton.CLOSE,CommandButton
				 * .CONTINUE}; CommandListImpl c3 = new
				 * CommandListImpl(false,l3); frame.add(c3,BorderLayout.NORTH);
				 */

				frame.setSize(400, 300);
				frame.setLocationRelativeTo(null);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
				frame.setVisible(true);
				
			}
		});
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEnabled(boolean state) {
		for (final JButton button : cbMap.values()) {
			button.setEnabled(state);
		}
	}

	/**
	 * add style effect to button
	 * @param butt
	 * @param cb
	 */
	private void setStyle(JButton butt, CommandButton cb) {
		// add some style effect
		// style effect
		// if first element
		if (cbList.size() > 1) {
			if (cb.equals(cbList.toArray()[0])) {
				butt.putClientProperty(
						SubstanceLookAndFeel.BUTTON_SIDE_PROPERTY,
						SubstanceConstants.Side.RIGHT);
				butt.putClientProperty(
						SubstanceLookAndFeel.BUTTON_OPEN_SIDE_PROPERTY,
						SubstanceConstants.Side.RIGHT);
				butt.putClientProperty(SubstanceLookAndFeel.CORNER_RADIUS,
						Float.valueOf(5.0f));
				// if last element
			} else if (cb.equals(cbList.toArray()[cbList.size() - 1])) {
				butt.putClientProperty(
						SubstanceLookAndFeel.BUTTON_SIDE_PROPERTY,
						SubstanceConstants.Side.LEFT);
				butt.putClientProperty(SubstanceLookAndFeel.CORNER_RADIUS,
						Float.valueOf(3.0f));
				// all other element
			} else {
				butt.putClientProperty(
						SubstanceLookAndFeel.BUTTON_SIDE_PROPERTY,
						SubstanceConstants.Side.RIGHT);
				butt.putClientProperty(
						SubstanceLookAndFeel.BUTTON_SIDE_PROPERTY,
						SubstanceConstants.Side.LEFT);
				butt.putClientProperty(
						SubstanceLookAndFeel.BUTTON_OPEN_SIDE_PROPERTY,
						SubstanceConstants.Side.RIGHT);
			}
		}
	}
}
