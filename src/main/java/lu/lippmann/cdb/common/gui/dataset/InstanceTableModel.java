/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui.dataset;

import java.util.*;

import javax.swing.RowFilter;

import lu.lippmann.cdb.common.gui.XTableModel;
import weka.core.*;


/**
 * InstanceTableModel.
 * Note: automatically shows a virtual column called 'Id' to identify rows.
 * 
 * @author WP1 Team
 */
public class InstanceTableModel extends XTableModel<Instance>
{
	//
	// Static fields
	//

	/** Serial version UID. */
	private static final long serialVersionUID=1472495L;

	/** Id of the rows **/
	private Instances dataSet;

	/** */
	private List<Integer> rows;
	
	/** */
	private final boolean reduceColumnName;

	
	//
	// Constructors
	//

	/**
	 * Constructor.
	 */
	public InstanceTableModel()
	{
		this(true);
	}
	
	/**
	 * Constructor.
	 */
	public InstanceTableModel(final boolean reduceColumnName)
	{
		super();
		this.reduceColumnName=reduceColumnName;
	}
	
	
	//
	// Instance methods
	//
	
	public void setDataset(final Instances dataSet) {
		this.dataSet = dataSet;
		final int numInstances=dataSet.numInstances();			
		final ArrayList<Instance> pdata=new ArrayList<Instance>(numInstances);

		//Initialize rows Id
		final int dsSize = dataSet.numInstances();
		this.rows = new ArrayList<Integer>(dsSize);
		for(int i = 0 ; i < dsSize ; i++){
			rows.add(i);
		}

		//Initialize data
		for (int i=0;i<numInstances;i++)
		{
			pdata.add(dataSet.instance(i));
		}
		super.setData(pdata);


	}

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
		if(col==0) 
		{	
			return "rowId";
		}
		else 
		{
			final String column=data.get(0).attribute(col-1).name();
			final boolean isClass=data.get(0).classIndex()==col-1;
			String nameToShow;
			if (!reduceColumnName||column.length() < 12) nameToShow=column; 
			else nameToShow=column.substring(0,12)+"...";
			if (isClass) nameToShow="["+nameToShow+"]";
			return nameToShow;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getColumnCount() 
	{
		if (data==null||data.isEmpty()) return 0;
		else return data.get(0).numAttributes()+1;
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
		if(data==null) return null;
		if (col==0) return rows.get(row); 
		else 
		{
			if (data.get(row).attribute(col-1).isNominal()||data.get(row).attribute(col-1).isString())
			{
				return data.get(row).toString(col-1);
			}
			else if (data.get(row).attribute(col-1).isDate())
			{
				return data.get(row).attribute(col-1).formatDate(data.get(row).value(col-1));
			}
			else
			{
				return data.get(row).value(col-1);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValueAt(final Object value,final int row,final int col) 
	{
		final Object oldvalue=data.get(row).toString(col-1);		
		if (!value.toString().equals(oldvalue.toString()))
		{
			if (data.get(row).attribute(col-1).isNominal()||data.get(row).attribute(col-1).isString())
			{
				try
				{
					data.get(row).setValue(col-1,value.toString());
					fireTableCellUpdated(row,col-1);
				}
				catch(Exception e) 
				{
					e.printStackTrace();
				}
			}
			else
			{
				data.get(row).setValue(col-1,Double.valueOf(value.toString()));
				fireTableCellUpdated(row,col-1);
			}			
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCellEditable(final int row,final int col)
	{ 
		return true; 
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getCellSelectionAllowed() 
	{
		return true;
	}

	@Override
	public void removeRow(int row) {
		rows.remove(row);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setData(List<Instance> pdata) {
		throw new IllegalStateException("Please call setDataset()");

	}

	/**
	 * 
	 * @return
	 */
	public Instances getDataSet(){
		return dataSet;
	}


}