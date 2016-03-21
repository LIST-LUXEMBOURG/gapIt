/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tabs;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import lu.lippmann.cdb.common.FormatterUtil;
import lu.lippmann.cdb.common.gui.dataset.InstanceTableModel;
import lu.lippmann.cdb.context.ResourceLoader;
import lu.lippmann.cdb.weka.*;

import org.jdesktop.swingx.*;
import weka.core.Instances;


/**
 * StatsTabView.
 *
 * @author the WP1 team
 */
public final class StatsTabView extends AbstractTabView
{
	//
	// Instance fields
	//

	/** */
	private final JXPanel jxp;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public StatsTabView()
	{
		super();
		this.jxp=new JXPanel();
		this.jxp.setLayout(new BorderLayout());			
	}
	
	
	//
	// Instance methods
	//
	
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
	public boolean isSlow() 
	{		
		return true;
	}	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() 
	{
		return "Stats";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getComponent() 
	{			
		return jxp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update0(final Instances dataSet) throws Exception 
	{		 
		this.jxp.removeAll();
		this.jxp.add(buildPanel(dataSet));
		this.jxp.repaint();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Icon getIcon() 
	{
		return ResourceLoader.getAndCacheIcon("menu/stats.png");
	}
	
	
	//
	// Static methods
	//
	
	public static JXPanel buildPanel(final Instances dataset) throws Exception
	{
		final String FULL=" FULL DATASET ";
		
		final JXPanel internalTextualModelPanel=new JXPanel();
		final Set<Object> classesSet=new HashSet<Object>();
		classesSet.add(FULL);
		if (dataset.classIndex()!=-1) classesSet.addAll(WekaDataStatsUtil.getClassRepartition(dataset).keySet());
		internalTextualModelPanel.setLayout(new GridLayout(classesSet.size(),1));
		for (final Object o:classesSet)
		{
			final Instances part;
			if (o.equals(FULL)) part=dataset;
			else part=WekaDataProcessingUtil.filterDataSetOnNominalValue(dataset,dataset.classIndex(),o.toString());
			
			final JXPanel intjxp=new JXPanel();
			intjxp.setLayout(new BoxLayout(intjxp,BoxLayout.PAGE_AXIS));
			intjxp.add(buildScrollPane(buildStatsForNumericalAttributes(part)));
			
			final JScrollPane comp=new JScrollPane(intjxp);
			comp.setBorder(new TitledBorder("Stats for '"+o.toString()+"'"));
			internalTextualModelPanel.add(comp);
		}
		return internalTextualModelPanel;
	}
	
	public static JScrollPane buildScrollPane(final Instances dataset) throws Exception
	{
		final JXPanel jxp=new JXPanel();
		jxp.setLayout(new BorderLayout());
		
		final JXTable instanceTable=new JXTable();
		instanceTable.setEditable(true);
		instanceTable.setShowHorizontalLines(false);
		instanceTable.setShowVerticalLines(false);
		instanceTable.setVisibleRowCount(5);
		instanceTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);		
		
		final InstanceTableModel instanceTableModel=new InstanceTableModel()
		{
			/** */
			private static final long serialVersionUID=1234L;

			/**
			 * {@inheritDoc}
			 */
			@Override
			public Object getValueAt(final int row,final int col) 
			{
				final Object val=super.getValueAt(row,col);
				if (val.equals("''")) return "";
				else return val;
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
					return data.get(0).attribute(col-1).name();
				}
			}
		};
		instanceTableModel.setDataset(dataset);
		instanceTable.setModel(instanceTableModel);
		instanceTable.packAll();						
		
		final JScrollPane scrollPane=new JScrollPane(instanceTable);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);		
		return scrollPane;
	}
	
	public static Instances buildStatsForNumericalAttributes(final Instances dataset) throws Exception
	{
		final StringBuilder sb=new StringBuilder("@relation blabla\n");
		sb.append("@attribute 'name' string\n");
		sb.append("@attribute 'min' string\n");
		sb.append("@attribute 'max' string\n");
		sb.append("@attribute 'mean' string\n");
		sb.append("@attribute 'stdDev' string\n");
		sb.append("@attribute 'missing values count' string\n");
		sb.append("@attribute 'missing values %' string\n");
		sb.append("@attribute 'values repartition' string\n");
		sb.append("@data\n");
		
		for (int i=0;i<dataset.numAttributes();i++)
		{
			if (dataset.attribute(i).isNumeric()&&!dataset.attribute(i).isDate())
			{
			sb.append(dataset.attribute(i).name())
			  .append(",")
			  .append(FormatterUtil.DECIMAL_FORMAT.format(dataset.attributeStats(i).numericStats.min))
			  .append(",")
			  .append(FormatterUtil.DECIMAL_FORMAT.format(dataset.attributeStats(i).numericStats.max))
			  .append(",")
			  .append(FormatterUtil.DECIMAL_FORMAT.format(dataset.attributeStats(i).numericStats.mean))
			  .append(",")
			  .append(FormatterUtil.DECIMAL_FORMAT.format(dataset.attributeStats(i).numericStats.stdDev))
			  .append(",")
			  .append(dataset.attributeStats(i).missingCount)
			  .append(",")
			  .append(FormatterUtil.DECIMAL_FORMAT.format(100d*dataset.attributeStats(i).missingCount/dataset.numInstances()))
			  .append(",")
			  .append("''")
			  .append("\n");
			}
			else if (dataset.attribute(i).isNominal())
			{
				sb.append(dataset.attribute(i).name())
				  .append(",'','','','','','','");
				  
				final Map<Object,String> nominalRep=WekaDataStatsUtil.getNominalRepartitionForDescription(dataset,i);
				for (Map.Entry<Object,String> e:nominalRep.entrySet())
				{
					sb.append(e.getKey()).append("=").append(e.getValue()).append(" ");
				}			
				
				sb.append("'\n");
			}
		}
		
		final Instances newds=WekaDataAccessUtil.loadInstancesFromARFFString(sb.toString(),false,false);
		
		if (WekaDataStatsUtil.getNominalAttributesIndexes(dataset).length==0)
		{
			newds.deleteAttributeAt(newds.numAttributes()-1);
		}		
		return newds;
	}
	
}
