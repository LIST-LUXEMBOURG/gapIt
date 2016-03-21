/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tabs;

import java.awt.Component;
import javax.swing.*;

import lu.lippmann.cdb.common.mvp.Listener;
import weka.core.Instances;


/**
 * TabView.
 * 
 * @author the WP1 team
 */
public interface TabView 
{
	String getName();
	Icon getIcon();
	
	void setLocation(JTabbedPane jtp, int pos);
	
	boolean needsClassAttribute();
	boolean needsDateAttribute();
	boolean isSlow();
	
	Component getComponent();
	Component getErrorComponent();
	Component getBusyComponent();	

	void update(Instances dataSet);
		
	void setDataChangeListener(Listener<DataChange> listener);	
	void pushDataChange(DataChange c);
		
	
	//
	// Inner static classes and enums
	//
	
	static enum TabViewUpdateModeEnum
	{
		LIVE,ON_BACKGROUND,ON_DEMAND;
	}
	
	static enum DataChangeTypeEnum 
	{
		Selection,Update,Deletion;
	}
	
	static class DataChange
	{
		private final DataChangeTypeEnum changeType;
		private final Instances dataSet;
		
		public DataChange(final Instances dataSet,final DataChangeTypeEnum ce)
		{
			this.dataSet=dataSet;
			this.changeType=ce;
		}

		public Instances getDataSet() 
		{
			return dataSet;
		}

		public DataChangeTypeEnum getDataChangeTypeEnum() 
		{
			return changeType;
		}
	}

}
