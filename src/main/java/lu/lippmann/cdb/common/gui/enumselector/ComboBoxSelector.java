/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui.enumselector;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.JComboBox;

import lu.lippmann.cdb.common.gui.formatter.*;
import lu.lippmann.cdb.common.mvp.Listener;

/**
 *
 * @author
 *
 * @param <T>
 */
class ComboBoxSelector<T> extends JComboBox implements SelectorWidget<T> {

	private static final long serialVersionUID = -400505467011572080L;
	
	private StringFormatter<T> stringFormatter;

	//map correlation beetween formatted string and object
	private Map<String, T> mapping;

	public ComboBoxSelector() {
		super();
		mapping = new TreeMap<String, T>();
		stringFormatter = new DefaultStringFormatter<T>();
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
	public void setChoices(List<T> choices) {
		removeAllItems();
		mapping.clear();

		for (T choice : choices) {
			//make a correlation map beetween combox box label and real object
			mapping.put(stringFormatter.getStringFor(choice), choice);
			addItem(stringFormatter.getStringFor(choice));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(T obj) {
		setSelectedItem(stringFormatter.getStringFor(obj));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T get() {
		return mapping.get(getSelectedItem());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStringFormatter(StringFormatter<T> stringFormatter) {
		this.stringFormatter = stringFormatter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addOnSelectListener(final Listener<T> listener) {
		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listener.onAction(ComboBoxSelector.this.get());
			}
		});
	}



}
