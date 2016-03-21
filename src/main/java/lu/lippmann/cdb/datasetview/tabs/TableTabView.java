/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tabs;

import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import lu.lippmann.cdb.common.gui.dataset.InstanceTableModel;
import lu.lippmann.cdb.context.ResourceLoader;
import lu.lippmann.cdb.event.*;
import lu.lippmann.cdb.weka.*;

import org.jdesktop.swingx.JXTable;

import weka.core.Instances;


/**
 * TableTabView.
 *
 * @author the WP1 team
 */
public class TableTabView extends AbstractTabView
{
	//
	// Instance fields
	//
	
	/** */
	private final JXTable instanceTable;

	/** */
	private JScrollPane scrollPane;


	//
	// Constructors
	//

	/**
	 * Constructor.
	 */
	public TableTabView(final EventPublisher eventPublisher)
	{
		super();

		this.instanceTable=new JXTable();

		this.instanceTable.setModel(new InstanceTableModel());

		this.instanceTable.setEditable(true);
		this.instanceTable.setShowHorizontalLines(false);
		this.instanceTable.setShowVerticalLines(false);
		this.instanceTable.setVisibleRowCount(5);
		this.instanceTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		//Render of numbers
		this.instanceTable.setDefaultRenderer(Number.class,new TableTabCellRenderer());


		this.instanceTable.addMouseListener(new MouseAdapter()
		{
			public void mouseReleased(final MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					final InstanceTableModel instanceTableModel = (InstanceTableModel)instanceTable.getModel();
					final Instances dataSet = instanceTableModel.getDataSet();
					final int row=instanceTable.rowAtPoint(e.getPoint());
					final int column=instanceTable.columnAtPoint(e.getPoint());
					final int modelColumn = instanceTable.convertColumnIndexToModel(column);
					final int modelRow    = instanceTable.convertRowIndexToModel(row);

					final JPopupMenu jPopupMenu=new JPopupMenu("feur");

					if (modelColumn>0&&dataSet.classIndex()!=modelColumn-1)
					{
						final JMenuItem removeColumnMenuItem=new JMenuItem("Remove this column ('"+instanceTableModel.getColumnName(modelColumn)+"')");
						removeColumnMenuItem.addActionListener(new ActionListener()
						{
							@Override
							public void actionPerformed(final ActionEvent e) 
							{
								final Instances newdataSet=new Instances(dataSet);
								newdataSet.deleteAttributeAt(modelColumn-1);
								pushDataChange(new DataChange(newdataSet,TabView.DataChangeTypeEnum.Deletion));									
							}
						});
						jPopupMenu.add(removeColumnMenuItem);
					}

					if (modelColumn>0&&dataSet.attribute(modelColumn-1).isNumeric()&&!dataSet.attribute(modelColumn-1).isDate())
					{
						final JMenuItem discrColumnMenuItem=new JMenuItem("Discretize this column ('"+instanceTableModel.getColumnName(modelColumn)+"')");
						discrColumnMenuItem.addActionListener(new ActionListener()
						{
							@Override
							public void actionPerformed(final ActionEvent e) 
							{
								try 
								{
									final Instances newdataSet=WekaDataProcessingUtil.buildDiscretizedDataSetUnsupervisedForOne(dataSet,modelColumn-1);
									pushDataChange(new DataChange(newdataSet,TabView.DataChangeTypeEnum.Update));
								} 
								catch (Exception e1) 
								{
									eventPublisher.publish(new ErrorOccuredEvent("Error during discretization of '"+instanceTableModel.getColumnName(modelColumn)+"'",e1));
								}
							}
						});
						jPopupMenu.add(discrColumnMenuItem);

						for (final int c:new int[]{5,10,20,40,80})
						{
							final JMenuItem discrColumnMenuItemN=new JMenuItem("Discretize this column ('"+instanceTableModel.getColumnName(modelColumn)+"') bins="+c);
							discrColumnMenuItemN.addActionListener(new ActionListener()
							{
								@Override
								public void actionPerformed(final ActionEvent e) 
								{
									try 
									{
										final Instances newdataSet=WekaDataProcessingUtil.buildDiscretizedDataSetUnsupervised(dataSet,modelColumn-1,c);
										pushDataChange(new DataChange(newdataSet,TabView.DataChangeTypeEnum.Update));
									} 
									catch (Exception e1) 
									{
										eventPublisher.publish(new ErrorOccuredEvent("Error during discretization of '"+instanceTableModel.getColumnName(modelColumn)+"'",e1));
									}
								}
							});
							jPopupMenu.add(discrColumnMenuItemN);
						}
						
	                }
	                
	                if (column>0&&dataSet.attribute(column-1).isNumeric()/*WekaDataStatsUtil.isInteger(dataSet,column-1)*/)
	                {
	                	final JMenuItem nominalizeColumnMenuItem=new JMenuItem("Nominalize this column ('"+instanceTableModel.getColumnName(column)+"')");
	                	nominalizeColumnMenuItem.addActionListener(new ActionListener()
	                	{
							@Override
							public void actionPerformed(final ActionEvent e) 
							{
								try 
								{
									final Instances newdataSet=WekaDataProcessingUtil.buildNominalizedDataSet(dataSet,new int[]{modelColumn-1});
									pushDataChange(new DataChange(newdataSet,TabView.DataChangeTypeEnum.Update));
								} 
								catch (Exception e1) 
								{
									eventPublisher.publish(new ErrorOccuredEvent("Error during nominalization of '"+instanceTableModel.getColumnName(modelColumn)+"'",e1));
								}
							}
						});
						jPopupMenu.add(nominalizeColumnMenuItem);
	                }
	                
	                if (column>0&&(dataSet.attribute(column-1).isNominal()||dataSet.attribute(column-1).isString()))
	                {
	                	final JMenuItem numColumnMenuItem=new JMenuItem("Numerize this column ('"+instanceTableModel.getColumnName(column)+"')");
	                	numColumnMenuItem.addActionListener(new ActionListener()
	                	{
							@Override
							public void actionPerformed(final ActionEvent e) 
							{
								try 
								{
									final Instances newdataSet=WekaDataProcessingUtil.buildDataSetWithNumerizedStringAttribute(dataSet,column-1);
									pushDataChange(new DataChange(newdataSet,TabView.DataChangeTypeEnum.Update));
								} 
								catch (Exception e1) 
								{
									eventPublisher.publish(new ErrorOccuredEvent("Error during numerization of '"+instanceTableModel.getColumnName(column)+"'",e1));
								}
							}
						});
						jPopupMenu.add(numColumnMenuItem);
	                }
	                
					final JMenuItem removeRowMenuItem=new JMenuItem("Remove this row (id='"+instanceTableModel.getValueAt(row,0)+"')");
					removeRowMenuItem.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(final ActionEvent e) 
						{			
							final Instances newdataSet=new Instances(dataSet);
							newdataSet.remove(modelRow);
							instanceTableModel.removeRow(modelRow);
							pushDataChange(new DataChange(newdataSet,TabView.DataChangeTypeEnum.Deletion));									
						}
					});					
					jPopupMenu.add(removeRowMenuItem);

					final JMenuItem selectKNNMenuItem=new JMenuItem("Select nearest neighbours of this row (id='"+instanceTableModel.getValueAt(modelRow,0)+"')");
					selectKNNMenuItem.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(final ActionEvent e) 
						{
							try 
							{
								final Instances knnResult=WekaMachineLearningUtil.computeNearestNeighbours(dataSet,instanceTableModel.getRow(modelRow),10);
								pushDataChange(new DataChange(knnResult,TabView.DataChangeTypeEnum.Selection));									
							} 
							catch (Exception e1) 
							{
								eventPublisher.publish(new ErrorOccuredEvent("Error when selecting nearest neighbours of this row!",e1));
							}
						}
					});					
					jPopupMenu.add(selectKNNMenuItem);					

					jPopupMenu.show(instanceTable,e.getX(),e.getY());
				}
			}
		});	    


		this.instanceTable.packAll();
		final int tableWidth=(int)this.instanceTable.getPreferredSize().getWidth()+30;
		this.scrollPane=new JScrollPane(this.instanceTable);
		this.scrollPane.setPreferredSize(new Dimension(Math.min(tableWidth,500), 500));
		this.scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}


	//
	// Instance methods
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() 
	{
		return "Table";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean needsClassAttribute()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getComponent() 
	{			
		return this.scrollPane;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update0(final Instances dataSet) throws Exception 
	{		 		
		final InstanceTableModel instanceTableModel = new InstanceTableModel();
		instanceTableModel.setDataset(dataSet);

		instanceTableModel.addTableModelListener(new TableModelListener()
		{
			@Override
			public void tableChanged(TableModelEvent e) 
			{			
				if(dataSet!=null){
					pushDataChange(new DataChange(dataSet,TabView.DataChangeTypeEnum.Update));
				}
			}
		});
		this.instanceTable.setModel(instanceTableModel);

		final TableCellRenderer defaultRenderer = this.instanceTable.getTableHeader().getDefaultRenderer();
		this.instanceTable.getTableHeader().setDefaultRenderer(new TableTabHeaderRenderer(defaultRenderer,dataSet));

		this.instanceTable.packAll();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Icon getIcon() 
	{
		return ResourceLoader.getAndCacheIcon("menu/table.png");
	}
}



class TableTabCellRenderer extends DefaultTableCellRenderer
{

	private static final long serialVersionUID = -5187469904218673262L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		final Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		final JLabel label = (JLabel)comp;
		Number numberValue = (Number)value;
		NumberFormat formatter = NumberFormat.getNumberInstance();
		formatter.setGroupingUsed(false);
		label.setText(formatter.format(numberValue.doubleValue()));
		return comp;		
	}
}

class TableTabHeaderRenderer extends DefaultTableCellRenderer
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7677106048098175602L;

	private Instances dataset;
	private TableCellRenderer defaultRenderer;

	public TableTabHeaderRenderer(final TableCellRenderer defaultRenderer,final Instances dataSet) {
		super();
		this.defaultRenderer = defaultRenderer;
		this.dataset = dataSet;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		final Component comp = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		final JLabel label = (JLabel)comp;

		int modelColumn = table.convertColumnIndexToModel(column);

		//Removes 1 because of rowId  ...
		if(modelColumn>0&&modelColumn<=dataset.numAttributes())
		{
			label.setToolTipText(dataset.attribute(modelColumn-1).name());
		}
		else
		{
			label.setToolTipText(label.getText());
		}
		return comp;		
	}


}
