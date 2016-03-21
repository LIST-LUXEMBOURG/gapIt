/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.ext.hydviga.data;

import java.awt.*;
import java.util.*;
import javax.swing.border.TitledBorder;
import org.jfree.chart.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.*;
import weka.core.*;
import lu.lippmann.cdb.common.MathsUtil;
import lu.lippmann.cdb.common.gui.dataset.InstanceFormatter;
import lu.lippmann.cdb.models.*;
import lu.lippmann.cdb.weka.WekaDataStatsUtil;


/**
 * StationsDataProvider.
 * 
 * @author the HYDVIGA team
 */
public final class StationsDataProvider 
{
	//
	// Static fields
	//
	
	/** */
	private static final String NOT_USABLE_STATUS="not usable";
	/** */
	private static final String USABLE_STATUS="usable";
	/** */
	private static final String SELECTED_STATUS="selected";
	
	
	//
	// Instance fields
	//
	
	/** */
	private final Map<String,double[]> coordinatesMap;
	/** */
	private final Collection<CGraph> relationshipsGraphs;
	/** */
	private final Image shapeImage;	
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.	 
	 */
	public StationsDataProvider(final Map<String,double[]> coordinatesMap,final Collection<CGraph> relationshipsGraphs,final Image shapeImage)
	{
		this.coordinatesMap=coordinatesMap;
		this.relationshipsGraphs=relationshipsGraphs;
		this.shapeImage=shapeImage;
	}
	
	
	//
	// Instance methods
	//

	public Collection<CGraph> getRelationshipsGraphs() 
	{		
		return this.relationshipsGraphs;
	}
	
	public double[] getCoordinates(final String stationName)
	{
		return this.coordinatesMap.get(stationName);
	}
	
	public String findNearestStation(final String stationName)
	{
		return findNearestStation(stationName,this.coordinatesMap.keySet());
	}

	public String findUpstreamStation(final String stationName) 
	{
		return findUpstreamStation(stationName,this.coordinatesMap.keySet());
	}
	
	public String findDownstreamStation(final String stationName) 
	{
		return findDownstreamStation(stationName,this.coordinatesMap.keySet());
	}
	
	public String findUpstreamStation(final String stationName,final Collection<String> attrNames) 
	{
		for (final CGraph cg:this.relationshipsGraphs)
		{
			for (final CNode cn:cg.getInternalGraph().getVertices())
			{
				//System.out.println(stationName);
				if (cn.getName().equals(stationName.substring(0,stationName.length()-4)))
				{
					CNode tmp=cn;
					while (cg.getInternalGraph().getPredecessors(tmp).size()>0)
					{
						//System.out.println(cg.getInternalGraph().getPredecessors(tmp));
						for (final CNode pred:cg.getInternalGraph().getPredecessors(tmp))
						{
							if (attrNames.contains(pred.getName()+"_val")) return pred.getName()+"_val"; 
						}
						tmp=cg.getInternalGraph().getPredecessors(tmp).iterator().next();
					}
					break;
				}
			}
		}
		return null;
	}
	
	public String findDownstreamStation(final String stationName,final Collection<String> attrNames) 
	{
		for (final CGraph cg:this.relationshipsGraphs)
		{
			for (final CNode cn:cg.getInternalGraph().getVertices())
			{
				//System.out.println(stationName);
				if (cn.getName().equals(stationName.substring(0,stationName.length()-4)))
				{
					CNode tmp=cn;
					while (cg.getInternalGraph().getSuccessors(tmp).size()>0)
					{
						//System.out.println(cg.getInternalGraph().getSuccessors(tmp));
						for (final CNode succ:cg.getInternalGraph().getSuccessors(tmp))
						{
							if (attrNames.contains(succ.getName()+"_val")) return succ.getName()+"_val"; 
						}
						tmp=cg.getInternalGraph().getSuccessors(tmp).iterator().next();
					}
					break;
				}
			}
		}
		return null;
	}
	
	public String findNearestStation(final String stationName,final Collection<String> attrNames) 
	{
		if (getCoordinates(stationName)==null) 
		{	
			throw new IllegalArgumentException(stationName+" not in coordinates map!");
		}
			
		final double[] coords=this.coordinatesMap.get(stationName);
		
		double mindist=Double.POSITIVE_INFINITY;
		String nearestStation=null;
		for (final String k:attrNames)
		{
			if (k.equals(stationName)) continue;
			if (!this.coordinatesMap.containsKey(k)) throw new IllegalStateException(k+" not in coordinates map!");
			
			final double dist=MathsUtil.distance(coords,this.coordinatesMap.get(k));
			if (dist<0000.1d) continue;
			if (dist<mindist)
			{
				mindist=dist;
				nearestStation=k;
			}			
		}
		return nearestStation;
	}
	
	private Instances getDataSetForMap(final Collection<String> sel,final Collection<String> usable)
	{
		final Instances ds=new Instances("ds",new ArrayList<Attribute>(),0);
		ds.insertAttributeAt(new Attribute("name",new ArrayList<String>(this.coordinatesMap.keySet())),ds.numAttributes());
		ds.insertAttributeAt(new Attribute("x"),ds.numAttributes());
		ds.insertAttributeAt(new Attribute("y"),ds.numAttributes());
		ds.insertAttributeAt(new Attribute("status",Arrays.asList(new String[]{SELECTED_STATUS,USABLE_STATUS,NOT_USABLE_STATUS})),ds.numAttributes());
		ds.setClassIndex(ds.numAttributes()-1);
		
		final Set<String> coordSelected=new HashSet<String>();
		for (final String ssel:sel) 
		{	
			final String coordsKey=coordinatesMap.get(ssel)[0]+"-"+coordinatesMap.get(ssel)[1];
			coordSelected.add(coordsKey);
		}
		final Set<String> coordUsable=new HashSet<String>();
		for (final String uu:usable) 
		{	
			final String coordsKey=coordinatesMap.get(uu)[0]+"-"+coordinatesMap.get(uu)[1];
			coordUsable.add(coordsKey);
		}
		
		final Set<String> coordAlreadyLoaded=new HashSet<String>();
		for (final Map.Entry<String,double[]> entry:this.coordinatesMap.entrySet())
		{
			final String coordsKey=entry.getValue()[0]+"-"+entry.getValue()[1];
			if (coordAlreadyLoaded.contains(coordsKey)) continue;
			final Instance inst=new DenseInstance(1.0d,new double[]{0d,0d,0d,0d});
			inst.setDataset(ds);
			inst.setValue(0,entry.getKey());
			inst.setValue(1,entry.getValue()[0]);
			inst.setValue(2,entry.getValue()[1]);
			//System.out.println(sel+" "+entry.getKey());
			inst.setValue(3,(coordSelected.contains(coordsKey))?SELECTED_STATUS
															   :((coordUsable.contains(coordsKey))?USABLE_STATUS:NOT_USABLE_STATUS));
			ds.add(inst);
			coordAlreadyLoaded.add(coordsKey);
		}
		
		return ds;
	}
	
	public ChartPanel getMapPanel(final Collection<String> sel,final Collection<String> usable,final boolean withLegend)
	{
		final ChartPanel cp=buildMapPanel(getDataSetForMap(sel,usable),1,2,withLegend);
		//cp.setPreferredSize(new Dimension(200,320));
		cp.setPreferredSize(new Dimension(225,300));
		cp.setMinimumSize(new Dimension(225,300));
		//cp.setMinimumSize(new Dimension(250,400));
		cp.setBorder(new TitledBorder(""));				
		return cp;
	}
		
	private ChartPanel buildMapPanel(final Instances dataSet,final int xidx,final int yidx,final boolean withLegend)
	{		
		final XYSeriesCollection data=new XYSeriesCollection();
		final Map<Integer,java.util.List<Instance>> filteredInstances=new HashMap<Integer,java.util.List<Instance>>();
		final int classIndex=dataSet.classIndex();
		if (classIndex<0)
		{
			final XYSeries series=new XYSeries("Serie",false);
			for (int i=0;i<dataSet.numInstances();i++) 
			{
				series.add(dataSet.instance(i).value(xidx),dataSet.instance(i).value(yidx));				
			}
			data.addSeries(series);
		}
		else
		{
			final Set<String> pvs=new TreeSet<String>(WekaDataStatsUtil.getPresentValuesForNominalAttribute(dataSet,classIndex));
			int p=0;
			for (final String pv:pvs)
			{
				final XYSeries series=new XYSeries(pv,false);
				for (int i=0;i<dataSet.numInstances();i++) 
				{
					if (dataSet.instance(i).stringValue(classIndex).equals(pv))
					{
						if(!filteredInstances.containsKey(p)){
							filteredInstances.put(p,new ArrayList<Instance>());
						}
						filteredInstances.get(p).add(dataSet.instance(i));

						series.add(dataSet.instance(i).value(xidx),dataSet.instance(i).value(yidx));
					}
				}
				data.addSeries(series);

				p++;
			}

		}

		final JFreeChart chart=ChartFactory.createScatterPlot(
				null, // chart title
				dataSet.attribute(xidx).name(), // x axis label
				dataSet.attribute(yidx).name(), // y axis label
				data, // data
				PlotOrientation.VERTICAL,
				withLegend, // include legend
				true, // tooltips
				false // urls
				);				

		final XYPlot xyPlot=(XYPlot) chart.getPlot();		
		xyPlot.setBackgroundImage(shapeImage);
		
		final XYItemRenderer renderer=xyPlot.getRenderer();        
		final XYToolTipGenerator gen=new XYToolTipGenerator() 
		{
			@Override
			public String generateToolTip(XYDataset dataset, int series, int item) {
				if(classIndex < 0){
					return InstanceFormatter.htmlFormat(dataSet.instance(item),true);
				}else{
					return InstanceFormatter.htmlFormat(filteredInstances.get(series).get(item),true);
				}
			}
		};

		xyPlot.getRangeAxis().setVisible(false);
		xyPlot.getDomainAxis().setVisible(false);
		
		xyPlot.getRangeAxis().setLowerBound(60000);
		xyPlot.getRangeAxis().setUpperBound(135000);
		xyPlot.getDomainAxis().setLowerBound(45000);
		xyPlot.getDomainAxis().setUpperBound(110000);
		
		xyPlot.setDomainGridlinesVisible(false);
		xyPlot.setRangeGridlinesVisible(false);

		xyPlot.setBackgroundPaint(Color.white);
		
		int nbSeries;
		if(classIndex < 0)
		{
			nbSeries=1;
		}else
		{
			nbSeries=filteredInstances.keySet().size();
		}
		
		for(int i=0 ; i < nbSeries ; i++)
		{
			renderer.setSeriesToolTipGenerator(i,gen);
		}

		final XYItemLabelGenerator lg=new XYItemLabelGenerator() 
		{
			@Override
			public String generateLabel(final XYDataset ds,final int series,final int item) 
			{
				final Instance iii=filteredInstances.get(series).get(item);
				if (iii.stringValue(3).equals(SELECTED_STATUS))
				{
					final String label=iii.stringValue(0);
					return label.substring(0,label.length()-4);
				}
				else return null;
			}
		};
		xyPlot.getRenderer().setBaseItemLabelGenerator(lg);
		xyPlot.getRenderer().setBaseItemLabelsVisible(true);
		xyPlot.getRenderer().setBaseItemLabelFont(new Font("Tahoma",Font.PLAIN,12));
		
		xyPlot.getRenderer().setSeriesPaint(1,Color.BLUE);
		xyPlot.getRenderer().setSeriesPaint(0,new Color(210,210,210));		
		xyPlot.getRenderer().setSeriesPaint(2,Color.DARK_GRAY);
		
		//System.out.println("shape -> "+xyPlot.getRenderer().getSeriesStroke(0));
		
		final ChartPanel cp=new ChartPanel(chart);
		cp.setDomainZoomable(false);
		cp.setRangeZoomable(false);		
		
		return cp;
	}



}
