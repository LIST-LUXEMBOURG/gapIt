/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview;

import java.awt.Component;

import lu.lippmann.cdb.event.EventPublisher;
import lu.lippmann.cdb.weka.CompletenessComputer;
import weka.core.Instances;


/**
 * IDatasetView.
 * 
 * @author the WP1 team
 */
public interface IDatasetView 
{
	Instances getInitialDataSet();
	
	Instances getDataSet();

	boolean isComputingOfDataCompletnessEnabled();

	int getDataCompleteness();

	void setDataCompleteness(int dc);

	void notifyTransformation(Instances changedDataSet, String string);
	
	EventPublisher getEventPublisher();

	Component asComponent();

	int getDataCompletenessRatio();

	CompletenessComputer getInitialCompleteness();

	void reinitDataCompleteness();
}
