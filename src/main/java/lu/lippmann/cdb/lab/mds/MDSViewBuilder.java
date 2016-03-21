/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.lab.mds;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import lu.lippmann.cdb.common.*;
import lu.lippmann.cdb.common.gui.LogoHelper;
import lu.lippmann.cdb.common.gui.dataset.InstanceFormatter;
import lu.lippmann.cdb.common.mvp.Listener;
import lu.lippmann.cdb.weka.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.ejml.simple.SimpleMatrix;
import org.jdesktop.swingx.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.*;
import org.jfree.ui.RectangleEdge;

import weka.core.*;

/**
 * Functions used to make a MultiDimensional Scaling. 
 * Use the method getCoordinate(double[][], int) to get the coordinates matrix
 * 
 * @author
 */
public final class MDSViewBuilder
{

	public static final int DEFAULT_MAX_INSTANCES     = 500;

	private static final float MAX_POINT_SIZE  = 30.0f;
	protected static final boolean ENABLE_PIE_SHART = true;

	private MDSViewBuilder() {}

	/**
	 * 
	 */
	public static JXPanel buildMDSViewFromDataSet(final Instances instances
			,final MDSResult mdsResult,final int maxInstances
			,final Listener<Instances> listener
			,final String... attrNameToUseAsPointTitle) throws Exception {

		final XYSeriesCollection dataset = new XYSeriesCollection();

		final JFreeChart chart = ChartFactory.createScatterPlot( 
				"", // title 
				"X", "Y", // axis labels 
				dataset, // dataset 
				PlotOrientation.VERTICAL, 
				attrNameToUseAsPointTitle.length==0, // legend? 
				true, // tooltips? yes 
				false // URLs? no 
				); 		
		
		final XYPlot xyPlot = (XYPlot) chart.getPlot();

		xyPlot.setBackgroundPaint(Color.WHITE);		
		xyPlot.getDomainAxis().setTickLabelsVisible(false);
		xyPlot.getRangeAxis().setTickLabelsVisible(false);
		
		//FIXME : should be different for Shih
		if(!mdsResult.isNormalized()){
			String stress = FormatterUtil.DECIMAL_FORMAT.format(ClassicMDS.getKruskalStressFromMDSResult(mdsResult));
			chart.setTitle(mdsResult.getCInstances().isCollapsed()?
					"Collapsed MDS(Instances="+maxInstances+",Stress="+stress+")":
						"MDS(Stress="+stress+")");
		}else{
			chart.setTitle(mdsResult.getCInstances().isCollapsed()?
					"Collapsed MDS(Instances="+maxInstances+")":
					"MDS");
		}

		final SimpleMatrix coordinates = mdsResult.getCoordinates();
		buildFilteredSeries(mdsResult,xyPlot,attrNameToUseAsPointTitle);

		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setPreferredSize(new Dimension(1200,900));
		chartPanel.setBorder(new TitledBorder("MDS Projection"));
		chartPanel.setBackground(Color.WHITE);


		final JButton selectionButton = new JButton("Select data");
		selectionButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final org.jfree.data.Range XDomainRange = xyPlot.getDomainAxis().getRange();
				final org.jfree.data.Range YDomainRange = xyPlot.getRangeAxis().getRange();
				final Instances cInstances = mdsResult.getCollapsedInstances(); 
				final Instances selectedInstances = new Instances(cInstances,0);
				List<Instances> clusters = null;
				if(mdsResult.getCInstances().isCollapsed()){
					clusters = mdsResult.getCInstances().getCentroidMap().getClusters();
				}
				for(int i=0 ; i < cInstances.numInstances() ; i++){
					final Instance centroid = instances.instance(i);
					if(XDomainRange.contains(coordinates.get(i, 0)) && YDomainRange.contains(coordinates.get(i,1))){						
						if(mdsResult.getCInstances().isCollapsed()){
							if(clusters != null){
								final Instances elementsOfCluster = clusters.get(i);
								final int nbElements= elementsOfCluster.numInstances();
								for(int k = 0 ; k < nbElements ; k++){
									selectedInstances.add(elementsOfCluster.get(k));
								}
							}
						}else{
							selectedInstances.add(centroid);
						}
					}
				}
				if(listener != null){
					listener.onAction(selectedInstances);
				}
			}
		});

		final JXPanel allPanel=new JXPanel();
		allPanel.setLayout(new BorderLayout());
		allPanel.add(chartPanel,BorderLayout.CENTER);
		final JXPanel southPanel = new JXPanel();
		southPanel.add(selectionButton);
		allPanel.add(southPanel,BorderLayout.SOUTH);
		return allPanel;
	}

	/**
	 * 
	 * @param instance
	 * @param instances
	 * @param mapAlias
	 * @return
	 */
	private static Integer getStrongestClass(final Integer centroidIndex,final CollapsedInstances mds){
		final KmeansResult mapCentroid = mds.getCentroidMap();
		final Instances newInstances = mapCentroid.getClusters().get(centroidIndex);
		final int classIndex=newInstances.classIndex();
		final AttributeStats classAttributeStats=newInstances.attributeStats(classIndex);
		int maxIndex = -1;
		int max = -1;
		for (int i=0;i<classAttributeStats.nominalCounts.length;i++){
			final int currentCount = classAttributeStats.nominalCounts[i];
			if(currentCount > max){
				max = currentCount;
				maxIndex = i;
			}
		}

		// Problem with that line :-(
		return maxIndex;
	}

	/**
	 * 
	 */
	private static void buildFilteredSeries(final MDSResult mdsResult,final XYPlot xyPlot,final String... attrNameToUseAsPointTitle) throws Exception  {

		final CollapsedInstances distMdsRes = mdsResult.getCInstances();
		final Instances instances = distMdsRes.getInstances();

		final SimpleMatrix coordinates = mdsResult.getCoordinates();

		final Instances collapsedInstances = mdsResult.getCollapsedInstances();
		int maxSize = 0;
		if(distMdsRes.isCollapsed()){
			final List<Instances> clusters = distMdsRes.getCentroidMap().getClusters();
			final int nbCentroids = clusters.size();
			maxSize = clusters.get(0).size();
			for(int i = 1 ; i < nbCentroids ; i++){
				final int currentSize = clusters.get(i).size();
				if(currentSize > maxSize){
					maxSize = currentSize;
				}
			}
		}

		Attribute clsAttribute = null;
		int nbClass = 1;
		if(instances.classIndex()!=-1){
			clsAttribute = instances.classAttribute();
			nbClass = clsAttribute.numValues();
		}
		final XYSeriesCollection dataset = (XYSeriesCollection)xyPlot.getDataset();
		final int fMaxSize = maxSize; 

		final List<XYSeries> lseries = new ArrayList<XYSeries>();

		//No class : add one dummy serie
		if(nbClass<=1){
			lseries.add(new XYSeries("Serie #1",false));
		}else {
			//Some class : add one serie per class
			for(int i=0 ; i < nbClass ; i++){
				lseries.add(new XYSeries(clsAttribute.value(i),false));
			}
		}
		dataset.removeAllSeries();

		/**
		 * Initialize filtered series
		 */
		final List<Instances> filteredInstances = new ArrayList<Instances>();
		for(int i = 0 ; i < lseries.size() ; i++){
			filteredInstances.add(new Instances(collapsedInstances,0));
		}

		final Map<Tuple<Integer,Integer>,Integer> correspondanceMap = new HashMap<Tuple<Integer,Integer>, Integer>();
		for(int i=0 ; i < collapsedInstances.numInstances() ; i++){
			final Instance oInst   = collapsedInstances.instance(i);
			int indexOfSerie = 0;
			if(oInst.classIndex()!=-1){
				if(distMdsRes.isCollapsed()){
					indexOfSerie = getStrongestClass(i,distMdsRes);
				}else{
					indexOfSerie = (int)oInst.value(oInst.classAttribute());
				}
			}
			lseries.get(indexOfSerie).add(coordinates.get(i, 0),coordinates.get(i, 1)); 

			filteredInstances.get(indexOfSerie).add(oInst);
			if(distMdsRes.isCollapsed()){
				correspondanceMap.put(new Tuple<Integer,Integer>(indexOfSerie,filteredInstances.get(indexOfSerie).numInstances()-1),i);
			}
		}

		final List<Paint> colors = new ArrayList<Paint>();

		for(final XYSeries series : lseries){
			dataset.addSeries(series);
		}


		if(distMdsRes.isCollapsed()){
			final XYLineAndShapeRenderer xyRenderer = new XYLineAndShapeRenderer(false,true){
				private static final long serialVersionUID = -6019883886470934528L;

				@Override
				public void drawItem(Graphics2D g2, 
						XYItemRendererState state, java.awt.geom.Rectangle2D dataArea,
						PlotRenderingInfo info, XYPlot plot, ValueAxis domainAxis,
						ValueAxis rangeAxis, XYDataset dataset, int series, int item, 
						CrosshairState crosshairState, int pass){

					if(distMdsRes.isCollapsed()){

						final Integer centroidIndex = correspondanceMap.get(new Tuple<Integer,Integer>(series,item));
						final Instances cluster = distMdsRes.getCentroidMap().getClusters().get(centroidIndex);
						int size = cluster.size();

						final int shapeSize = (int)(MAX_POINT_SIZE*size/fMaxSize+1) ;

						final double x1 = plot.getDataset().getX(series, item).doubleValue();
						final double y1 = plot.getDataset().getY(series, item).doubleValue();

						Map<Object,Integer> mapRepartition = new HashMap<Object, Integer>();
						mapRepartition.put("No class",size);
						if(cluster.classIndex()!=-1){
							mapRepartition = WekaDataStatsUtil.getClassRepartition(cluster);
						} 

						final RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
						final RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
						final double  fx =   domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
						final double  fy =   rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

						setSeriesShape(series,new Ellipse2D.Double(-shapeSize/2,-shapeSize/2,shapeSize,shapeSize));

						super.drawItem(g2, state, dataArea, info, plot, domainAxis, 
								rangeAxis, dataset, series, item, crosshairState, pass);

						//Draw pie
						if(ENABLE_PIE_SHART){
							createPieChart(g2,(int)(fx-shapeSize/2),(int)(fy-shapeSize/2),shapeSize,mapRepartition,size,colors);
						}

					}else{



						super.drawItem(g2, state, dataArea, info, plot, domainAxis, 
								rangeAxis, dataset, series, item, crosshairState, pass);

					}

				}


			};

			xyPlot.setRenderer(xyRenderer);
		}


		final XYToolTipGenerator gen = new XYToolTipGenerator() {
			@Override
			public String generateToolTip(XYDataset dataset, int series, int item) {
				if(distMdsRes.isCollapsed()){
					final StringBuilder res = new StringBuilder("<html>");
					final Integer centroidIndex = correspondanceMap.get(new Tuple<Integer,Integer>(series,item));
					final Instance centroid = distMdsRes.getCentroidMap().getCentroids().get(centroidIndex);
					final Instances cluster = distMdsRes.getCentroidMap().getClusters().get(centroidIndex);

					//Set same class index for cluster than for original instances
					//System.out.println("Cluster index = "  + cluster.classIndex() + "/" + instances.classIndex());
					cluster.setClassIndex(instances.classIndex());

					Map<Object,Integer> mapRepartition = new HashMap<Object, Integer>();
					mapRepartition.put("No class",cluster.size());
					if(cluster.classIndex()!=-1){
						mapRepartition = WekaDataStatsUtil.getClassRepartition(cluster);
					}
					res.append(InstanceFormatter.htmlFormat(centroid,false)).append("<br/>");
					for(final Map.Entry<Object,Integer> entry : mapRepartition.entrySet()){
						if(entry.getValue()!=0){
							res.append("Class :<b>'" + StringEscapeUtils.escapeHtml(entry.getKey().toString())+"</b>' -> "+entry.getValue()).append("<br/>");
						}
					}
					res.append("</html>");
					return res.toString();
				}else{
					//return InstanceFormatter.htmlFormat(filteredInstances.get(series).instance(item),true);
					return InstanceFormatter.shortHtmlFormat(filteredInstances.get(series).instance(item));
				}
			}
		};

		final Shape shape = new Ellipse2D.Float(0f,0f,MAX_POINT_SIZE,MAX_POINT_SIZE);

		((XYLineAndShapeRenderer)xyPlot.getRenderer()).setUseOutlinePaint(true);

		for(int p=0 ; p < nbClass ; p++){
			xyPlot.getRenderer().setSeriesToolTipGenerator(p,gen);
			((XYLineAndShapeRenderer)xyPlot.getRenderer()).setLegendShape(p, shape);
			xyPlot.getRenderer().setSeriesOutlinePaint(p, Color.BLACK);
		}

		for(int ii = 0 ; ii < nbClass ; ii++){
			colors.add(xyPlot.getRenderer().getItemPaint(ii,0));
		}

		if (attrNameToUseAsPointTitle.length>0)
		{
			final Attribute attrToUseAsPointTitle=instances.attribute(attrNameToUseAsPointTitle[0]);
			if (attrToUseAsPointTitle!=null)
			{
				final XYItemLabelGenerator lg=new XYItemLabelGenerator() 
				{
					@Override
					public String generateLabel(final XYDataset dataset,final int series,final int item) 
					{
						return filteredInstances.get(series).instance(item).stringValue(attrToUseAsPointTitle);
					}
				};
				xyPlot.getRenderer().setBaseItemLabelGenerator(lg);
				xyPlot.getRenderer().setBaseItemLabelsVisible(true);
			}
		}
	}

	/**
	 * 
	 * @param g
	 * @param fx
	 * @param fy
	 * @param size
	 * @param repartition
	 * @param total
	 * @param colors
	 */
	public static void createPieChart(Graphics g,int fx,int fy,int size,
			Map<Object,Integer> repartition,int total,List<Paint> colors)
	{
		if(total > 0){
			int startAngle = 0;
			int k=0;
			double curValue = 0.0D;
			for(final Map.Entry<Object,Integer> entry : repartition.entrySet()){
				startAngle = (int) (curValue * 360 / total);
				int arcAngle = (int) (entry.getValue() * 360 / total);
				g.setColor((Color)colors.get(k));
				g.fillArc( fx, fy, size, size, startAngle, arcAngle );
				curValue += entry.getValue();
				k++;
			}
		}
	}

	/**
	 * 
	 * @param clusters
	 */
	public static void buildKMeansChart(final List<Instances> clusters){
		final XYSeriesCollection dataset = new XYSeriesCollection();

		final JFreeChart chart = ChartFactory.createScatterPlot( 
				"", // title 
				"X", "Y", // axis labels 
				dataset, // dataset 
				PlotOrientation.VERTICAL, 
				true, // legend? yes 
				true, // tooltips? yes 
				false // URLs? no 
				); 


		final XYPlot xyPlot = (XYPlot) chart.getPlot();

		((NumberAxis)xyPlot.getDomainAxis()).setTickUnit(new NumberTickUnit(2.0));
		((NumberAxis)xyPlot.getRangeAxis()).setTickUnit(new NumberTickUnit(2.0));

		Attribute clsAttribute = null;
		int nbClass = 1;
		Instances cluster0 = clusters.get(0);
		if(cluster0.classIndex()!=-1){
			clsAttribute = cluster0.classAttribute();
			nbClass = clsAttribute.numValues();
		}
		if(nbClass<=1){
			dataset.addSeries(new XYSeries("Serie #1",false));
		}else {
			for(int i=0 ; i < nbClass ; i++){
				dataset.addSeries(new XYSeries(clsAttribute.value(i),false));
			}
		}

		final XYToolTipGenerator gen = new XYToolTipGenerator() {
			@Override
			public String generateToolTip(XYDataset dataset, int series, int item) {
				return "TODO";				
			}
		};

		for(int i=0 ; i < nbClass ; i++){
			dataset.getSeries(i).clear();
			xyPlot.getRenderer().setSeriesToolTipGenerator(i,gen);
		}

		final int nbClusters = clusters.size();
		for(int i = 0 ; i < nbClusters ; i++){
			Instances instances = clusters.get(i);
			final int nbInstances = instances.numInstances();
			for(int j = 0 ; j < nbInstances ; j++){
				final Instance oInst = instances.instance(j);
				dataset.getSeries(i).add(oInst.value(0),oInst.value(1));
			}
		}


		final TitledBorder titleBorder = new TitledBorder("Kmeans of projection");
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setPreferredSize(new Dimension(1200,900));
		chartPanel.setBorder(titleBorder);
		chartPanel.setBackground(Color.WHITE);

		JXFrame frame = new JXFrame();
		frame.getContentPane().add(chartPanel);
		frame.setVisible(true);
		frame.pack();

	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static final void main(String[] args) throws Exception {

		//final String fileName = "./samples/csv/salary.csv";
		//final String fileName = "./samples/csv/uci/mushroom.csv";
		final String fileName = "./samples/csv/uci/zoo.csv";
		//final String fileName = "./samples/csv/lines.csv";
		//final String fileName = WekaDataAccessUtil.DEFAULT_SAMPLE_DIR+"csv/zoo.csv";
		//final String fileName = "./samples/csv/speech-revisited.csv";
		//final String fileName = "./samples/arff/UCI/autos.arff";

		Instances instances = WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(new File(fileName));

		//Modify instance using SHIH Algorithm
		//Shih2010 shih = new Shih2010(instances);
		//instances = shih.getModifiedInstances();

		LookAndFeelUtil.init();

		final JFrame mainFrame = new JFrame();
		mainFrame.setBackground(Color.WHITE);
		LogoHelper.setLogo(mainFrame);
		mainFrame.setTitle("MDS for '"+instances.relationName()+"'");


		int maxResult = 1000;
		final MDSResult mdsResult = ClassicMDS.doMDS(instances,MDSDistancesEnum.EUCLIDEAN,2,maxResult,true,false);

		mainFrame.getContentPane().setLayout(new BorderLayout());
		mainFrame.getContentPane().add(buildMDSViewFromDataSet(
				instances,mdsResult,maxResult
				,new Listener<Instances>()
				{
					@Override
					public void onAction(final Instances parameter) 
					{
						//System.out.println(parameter);		
					}
				}));

		mainFrame.setSize(1200,900);
		mainFrame.setVisible(true);	

	}





}
