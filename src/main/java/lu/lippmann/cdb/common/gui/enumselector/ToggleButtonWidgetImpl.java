/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui.enumselector;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import lu.lippmann.cdb.common.gui.formatter.StringFormatter;
import lu.lippmann.cdb.common.mvp.Listener;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;
import org.pushingpixels.substance.api.SubstanceConstants;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;


/**
 *
 * @author
 *
 * @param <T>
 */
public final class ToggleButtonWidgetImpl<T> extends JPanel implements ToggleButtonWidget<T> {

	/** Setial version UID. */	
	private static final long serialVersionUID = 27165252261981118L;

	private int alignement;
	private List<T> choices;
	private Map<T,JButton> buttons;
	private T choice;
	private final List<Listener<T>> selectListeners;
	private Dimension buttonSize;
	private boolean selectable;
	private boolean multichoice;

	/**
	 *
	 */
	public ToggleButtonWidgetImpl() {
		this(null,SwingConstants.HORIZONTAL);
	}

	/**
	 *
	 * @param align SwingConstants.HORIZONTAL or SwingConstants.VERTICAL
	 */
	public ToggleButtonWidgetImpl(int align) {
		this(null,align);
	}

	/**
	 *
	 * @param buttonSize
	 * @param align SwingConstants.HORIZONTAL or SwingConstants.VERTICAL
	 */
	public ToggleButtonWidgetImpl(final Dimension buttonSize, int align) {
		if (!(align==SwingConstants.HORIZONTAL || align==SwingConstants.VERTICAL)) {
			throw new IllegalArgumentException("Align must be SwingConstants.HORIZONTAL or SwingConstants.VERTICAL");
		}
		choices = new ArrayList<T>();
		buttons = new HashMap<T, JButton>();
		this.selectListeners = new ArrayList<Listener<T>>();
		this.buttonSize=buttonSize;
		this.alignement=align;
		selectable = true;
		multichoice = false;
		initComponent();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setChoices(final List<T> choices) {
		this.choices = choices;
		initComponent();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T get() {
		return choice;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(T selected) {
		if (this.selectable) {

			if (!multichoice && choice!=null) {
				buttons.get(choice).setSelected(false);
			}

			// invert choice
			JButton sel = buttons.get(selected);
			if (sel!=null) {
				sel.setSelected(!sel.isSelected());
			}

			this.choice = selected;
			for(Listener<T> listener : this.selectListeners){
				listener.onAction(selected);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(T item, boolean select) {
		if (this.selectable) {

			if (!multichoice && choice!=null && select) {
				buttons.get(choice).setSelected(false);
			}

			// invert choice
			JButton sel = buttons.get(item);
			if (sel!=null) {
				sel.setSelected(select);
			}

			if (select) {
				this.choice = item;
				for(Listener<T> listener : this.selectListeners){
					listener.onAction(item);
				}
			}

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
	public void addOnSelectListener(final Listener<T> listener) {
		this.selectListeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEnabled(final boolean enabled) {
		for (Component c :getComponents()) {
			c.setEnabled(enabled);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMultiChoice(boolean enable) {
		multichoice=enable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<T> getChoices() {
		List<T> ret = new LinkedList<T>();
		for (Entry<T,JButton> e:buttons.entrySet()) {
			if (e.getValue().isSelected()) {
				ret.add(e.getKey());
			}
		}
		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSelectable(final boolean selectable) {
		this.selectable = selectable;
	}

	/**
	 *
	 */
	private void initComponent() {
		if (alignement==SwingConstants.HORIZONTAL) {
			setLayout(new HorizontalLayout(0));
		} else {
			setLayout(new VerticalLayout(0));
		}

		buttons.clear();

		this.removeAll();
		for (final T item : this.choices) {
			final JButton button = createButtonByItem(item);
			addActionListenerToButton(button);
			formatButton(item, button);
			this.add(button);
			buttons.put(item, button);
		}
		validate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T findItemByText(final String text) {
		for (T item : this.choices) {
			if (composeString(item).equals(text)) {
				return item;
			}
		}
		return null;
	}

	/**
	 *
	 * @param item
	 * @return
	 */
	protected String composeString(final T item) {
		return item.toString();
	}

	/**
	 *
	 * @param item
	 * @return
	 */
	private JButton createButtonByItem(final T item) {
		return new JButton(composeString(item));
	}


	/**
	 *
	 * @param button
	 */
	private void addActionListenerToButton(JButton button) {
		button.addActionListener(new ToggleButtonWidgetImplActionListener<T>(
				this, button));
	}

	/**
	 *
	 * @param item
	 * @param button
	 */
	private void formatButton(T item, JButton button) {
		if (buttonSize!=null) {
			button.setMinimumSize(buttonSize);
			button.setPreferredSize(buttonSize);
		}

		if (!multichoice && item.equals(choice)) {
			button.setSelected(true);
		}

		// style effect
		if (choices.size()>1) {
			if (alignement==SwingConstants.HORIZONTAL) {
				if (item.equals(this.choices.get(0))) {
					button.putClientProperty(
							SubstanceLookAndFeel.BUTTON_SIDE_PROPERTY,
							SubstanceConstants.Side.RIGHT);
					button.putClientProperty(
							SubstanceLookAndFeel.BUTTON_OPEN_SIDE_PROPERTY,
							SubstanceConstants.Side.RIGHT);
					button.putClientProperty(SubstanceLookAndFeel.CORNER_RADIUS,
							Float.valueOf(5.0f));
					// if last element
				} else if (item.equals(this.choices.get(this.choices.size() - 1))) {
					button.putClientProperty(
							SubstanceLookAndFeel.BUTTON_SIDE_PROPERTY,
							SubstanceConstants.Side.LEFT);
					button.putClientProperty(SubstanceLookAndFeel.CORNER_RADIUS,
							Float.valueOf(3.0f));
					// all other element
				} else {
					button.putClientProperty(
							SubstanceLookAndFeel.BUTTON_SIDE_PROPERTY,
							SubstanceConstants.Side.RIGHT);
					button.putClientProperty(
							SubstanceLookAndFeel.BUTTON_SIDE_PROPERTY,
							SubstanceConstants.Side.LEFT);
					button.putClientProperty(
							SubstanceLookAndFeel.BUTTON_OPEN_SIDE_PROPERTY,
							SubstanceConstants.Side.RIGHT);
				}
			} else {	// vertical
				if (item.equals(this.choices.get(0))) {
					button.putClientProperty(
							SubstanceLookAndFeel.BUTTON_SIDE_PROPERTY,
							SubstanceConstants.Side.BOTTOM);
					button.putClientProperty(
							SubstanceLookAndFeel.BUTTON_OPEN_SIDE_PROPERTY,
							SubstanceConstants.Side.BOTTOM);
					button.putClientProperty(SubstanceLookAndFeel.CORNER_RADIUS,
							Float.valueOf(5.0f));
					// if last element
				} else if (item.equals(this.choices.get(this.choices.size() - 1))) {
					button.putClientProperty(
							SubstanceLookAndFeel.BUTTON_SIDE_PROPERTY,
							SubstanceConstants.Side.TOP);
					button.putClientProperty(SubstanceLookAndFeel.CORNER_RADIUS,
							Float.valueOf(3.0f));
					// all other element
				} else {
					button.putClientProperty(
							SubstanceLookAndFeel.BUTTON_SIDE_PROPERTY,
							SubstanceConstants.Side.BOTTOM);
					button.putClientProperty(
							SubstanceLookAndFeel.BUTTON_SIDE_PROPERTY,
							SubstanceConstants.Side.TOP);
					button.putClientProperty(
							SubstanceLookAndFeel.BUTTON_OPEN_SIDE_PROPERTY,
							SubstanceConstants.Side.BOTTOM);
				}
			}
		}
	}

	/**
	 *
	 * @author
	 *
	 * @param <T>
	 */
	private static class ToggleButtonWidgetImplActionListener<T> implements ActionListener {

		private ToggleButtonWidget<T> toggleButton;
		private JButton button;

		/**
		 *
		 * @param toggleButton
		 * @param button
		 */
		public ToggleButtonWidgetImplActionListener(
				ToggleButtonWidget<T> toggleButton, JButton button) {
			this.toggleButton = toggleButton;
			this.button = button;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			toggleButton.set(toggleButton.findItemByText(button.getText()));
		}
	}

	@Override
	public void setStringFormatter(StringFormatter<T> stringFormatter) {
		// NOT IMPLEMENTED
	}


}
