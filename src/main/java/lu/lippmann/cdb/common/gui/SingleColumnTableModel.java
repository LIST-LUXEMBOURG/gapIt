/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui;

import java.util.Comparator;
import javax.swing.RowFilter;


/**
 * SingleColumnTableModel.
 * 
 * @author Olivier PARISOT
 */
public final class SingleColumnTableModel extends XTableModel<String>
{
	//
	// Static fields
	//
	
	/** Serial version UID. */
	private static final long serialVersionUID=14729L;
	
	
	//
	// Instance fields
	//
	
	/** */
	private final String title;
	
	
	//
	// Constructor
	//
	
	/**
	 * Constructor.
	 */
	public SingleColumnTableModel(final String title)
	{
		this.title=title;
	}
	
	
	//
	// Instance methods
	//
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isVisible(final int col) 
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getColumnName(final int col) 
	{
		return title;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getColumnCount() 
	{
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Comparator<String> getComparator(final int col) 
	{				
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RowFilter<Object,Object> getRowFilter() 
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValueAt(final int row,final int col) 
	{
		return this.data.get(row);
	}

}