/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui;

import java.util.*;
import javax.swing.RowFilter;
import javax.swing.table.*;


/**
 * Abstract class designed to be extended by concrete table models. A table
 * model defines how certain data is displayed in an {@link AgimoList}. It acts
 * also as container for the data.
 * 
 * @author heinesch
 * 
 * @param <A>
 *            Type of data to display in a table
 */
public abstract class XTableModel<A> extends DefaultTableModel  {
	
	private static final long serialVersionUID = 248167634905594752L;
	
	/** Data to display */
	protected List<A> data = null;
	
	/**
	 * default constructor
	 */
	public XTableModel() {
		this(null);
	}
	
	/**
	 * Create a new Model containg the given data. <br>
	 * Null is allowed!
	 * 
	 * @param data
	 */
	public XTableModel(List<A> data) {
		this.data = data;
	}
	
	/**
	 * Return a {@link RowFilter} that is used to filter the visible data in the
	 * table. Complex filters can be create using the utility methods of
	 * {@link RowFilter}. Return null to disable custom filtering.
	 * 
	 * @return active filter or null
	 */
	public abstract RowFilter<Object, Object> getRowFilter();
	
	/**
	 * Changes the data contained in this model. Triggers a view update.
	 * 
	 * @param pdata
	 */
	public void setData(final List<A> pdata) {
		// copy to arraylist
		if (pdata!=null) this.data = new ArrayList<A>(pdata);
		this.fireTableDataChanged();
	}
	
	/**
	 * Adds a single row to the data. Triggers a view update.<br>
	 * Do not use this method to add many many entries in a loop
	 * 
	 * @param row
	 */
	public void addRow(A row) {
		data.add(row);
		this.fireTableDataChanged();
	}
	
	/**
	 * Returning true allows to select individual cells instead of only rows
	 * 
	 * @return
	 */
	public boolean getCellSelectionAllowed() {
		return false;
	}
	
	/**
	 * Returns the row with the given index or null if not found.
	 * 
	 * @param row
	 * @return
	 */
	public A getRow(int row) {
		if (row < 0 || row >= data.size())
			return null;
		return data.get(row);
	}
	
	/**
	 * Return the row index of the given element or -1 if
	 * not found.
	 * @param row
	 * @return
	 */
	public int getRowIndex(A row) {
		if (row==null) return -1;
		return data.indexOf(row);
	}
	
	/**
	 * Returns the name of the column with the given index.
	 */
	@Override
	public abstract String getColumnName(int col);
	
	/**
	 * Returns the number of rows to be displayed in the table
	 */
	@Override
	public int getRowCount() {
		if (data != null) {
			return data.size();
		}
		return 0;
	}
	
	/**
	 * Returns the number of columns of this model
	 */
	@Override
	public abstract int getColumnCount();
	
	/**
	 * Should the table show the column with the given number-
	 * 
	 * @param col
	 * @return
	 */
	public boolean isVisible(int col) {
		return true;
	}
	
	/**
	 * Returns custom comparators that are used to sort the data contained in
	 * the columns or null if default sorting should be used.
	 * 
	 * @param col
	 * @return
	 */
	public abstract Comparator<?> getComparator(int col);
	
	/**
	 * Returns the value of a cell
	 */
	@Override
	public abstract Object getValueAt(int row, int col);
	
	/**
	 * Can the user edit a certain cell?
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}
	
	/**
	 * Changes the value of an cell.
	 */
	@Override
	public void setValueAt(Object value, int row, int col) {
		throw new IllegalAccessError();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (getRowCount() != 0) {
			Object o = getValueAt(0, columnIndex);
			if (o!=null) {
				return o.getClass();
			}
		}
		return String.class;
	}
	
}
