/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.weka;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import lu.lippmann.cdb.common.gui.*;

import org.apache.commons.lang.ArrayUtils;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.*;

import weka.clusterers.SimpleKMeans;
import weka.core.*;

/**
 * 
 * @author didry
 *
 */
public final class SilhouetteUtil {

	/**
	 * 
	 */
	private SilhouetteUtil(){
		throw new IllegalStateException();
	}


	/**
	 * res.get(clusterIdx) = silhouettes values map for instances 
	 * @param result
	 * @return
	 */
	protected static Map<Integer,List<Double>> computeSilhouette(final Instances ds,final WekaClusteringResult result)
	{
		final List<Instances> clusters = result.getClustersList();
		final List<Double> res = new ArrayList<Double>();
		final int cs = clusters.size();

		final double[] ass = result.getAss();
		
		final EuclideanDistance euclidian = new EuclideanDistance(ds);
		//euclidian.setDontNormalize(true);
		ds.setClassIndex(-1);
		
		//Loop through every cluster
		final int dss = ds.numInstances();
		for(int i = 0 ; i < dss ; i++){
			final int clusterIndex = (int)ass[i];
			double distAo=-1;
			double distBo=Double.MAX_VALUE;
			boolean distanceIsZero=false;
			for(int j = 0 ; j < cs ; j++){
				//Compute distCo, for all C
				double distCo = 0;
				Instances cluster = result.getClustersList().get(j);
				final int cls = cluster.numInstances();
				for(int k = 0 ; k < cls ; k++){
					distCo+=euclidian.distance(cluster.instance(k),ds.instance(i));
				}
				distCo/=cls;
				if(j!=clusterIndex && distCo < distBo){
					distBo = distCo;
				}

				if(j==clusterIndex){
					if(distCo==0){
						distanceIsZero=true;
					}
					else{
						distAo = distCo;
					}
				}
			}
			if(distanceIsZero){
				res.add(0d);
			}else{
				res.add((distBo-distAo)/Math.max(distAo,distBo));
			}
		}
		
		final Map<Integer,List<Double>> sils = new HashMap<Integer, List<Double>>();
		for(int i = 0 ; i < dss ; i++){
			final int clusterIndex = (int)ass[i];
			if(!sils.containsKey(clusterIndex)){
				sils.put(clusterIndex, new ArrayList<Double>());
			}
			sils.get(clusterIndex).add(res.get(i));
		}
		for(final List<Double> list : sils.values()){
			Collections.sort(list,Collections.reverseOrder());
		}
		return sils;

	}


	/**
	 * 
	 * @param sils
	 * @return
	 */
	public static JPanel buildSilhouettePanel(final Instances ds,final WekaClusteringResult result){
		
		final Map<Integer,List<Double>> sils = computeSilhouette(ds,result);
				
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		final JFreeChart chart = ChartFactory.createBarChart("Silhouette", 
				"Category", "Value", dataset, PlotOrientation.HORIZONTAL, true, true, false);


		int nbClass = sils.keySet().size();
		
		int id = 0;
		double minValue = 0;
	
		int counter[][] = new int[nbClass][4]; 
		for(int i=0 ; i < nbClass ; i++){
			
			final double[] tree = ArrayUtils.toPrimitive(sils.get(i).toArray(new Double[0]));
			
			for(double val : tree){
				if(val > 0.75){
					dataset.addValue(val, "Cluster "+i+" ++", ""+id);	
					counter[i][0]++;
				}else if(val > 0.50){
					dataset.addValue(val, "Cluster "+i+" +", ""+id);
					counter[i][1]++;
				}else if(val > 0.25){
					dataset.addValue(val, "Cluster "+i+" =", ""+id);
					counter[i][2]++;
				}else{
					dataset.addValue(val, "Cluster "+i+" -", ""+id);
					counter[i][3]++;
				}
				if (val < minValue){
					minValue = val;
				}
				id++;
			}		
			
		}
		
		
		final CategoryPlot categoryplot = (CategoryPlot)chart.getPlot();
		categoryplot.setBackgroundPaint(Color.WHITE);
		categoryplot.getDomainAxis().setVisible(false);
		categoryplot.setDomainGridlinesVisible(false);
		categoryplot.setRangeGridlinesVisible(false);
		categoryplot.getRangeAxis().setRange(minValue, 1.0);
	
	    //Add line markers
		ValueMarker target = new ValueMarker(0.75);
        target.setPaint(Color.BLACK);
        target.setLabel("  ++");
        target.setLabelAnchor(RectangleAnchor.TOP_LEFT);
        target.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        categoryplot.addRangeMarker(target);
        
	    
        target = new ValueMarker(0.5);
        target.setPaint(Color.BLACK);
        target.setLabel("  +");
        target.setLabelAnchor(RectangleAnchor.TOP_LEFT);
        target.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        categoryplot.addRangeMarker(target);
        
	    
        target = new ValueMarker(0.25);
        target.setPaint(Color.BLACK);
        target.setLabel("  =");
        target.setLabelAnchor(RectangleAnchor.TOP_LEFT);
        target.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        categoryplot.addRangeMarker(target);
        
        target = new ValueMarker(0);
        target.setPaint(Color.BLACK);
        target.setLabel("  -");
        target.setLabelAnchor(RectangleAnchor.TOP_LEFT);
        target.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        categoryplot.addRangeMarker(target);
        
        
		//Remove visual effects on bar
		final BarRenderer barrenderer = (BarRenderer)categoryplot.getRenderer();
		barrenderer.setBarPainter(new StandardBarPainter());
		
		//set bar colors
		int p=0;
		final int max = ColorHelper.COLORBREWER_SEQUENTIAL_PALETTES.size();
		
		for(int i = 0 ; i < nbClass ; i++){
			final Color[] color = new ArrayList<Color[]>(
					ColorHelper.COLORBREWER_SEQUENTIAL_PALETTES.values())
					.get((max-i)%max);			
			final int nbColors=color.length;
			for(int k = 0 ; k < counter[i].length ; k++){
				if (counter[i][k] > 0)
					barrenderer.setSeriesPaint(p++,color[(nbColors-k-3)%nbColors]);	
			}
		}
		
		//remove blank line between bars
		barrenderer.setItemMargin(-dataset.getRowCount());		
		
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setPreferredSize(new Dimension(1200,900));
		chartPanel.setBorder(new TitledBorder("Silhouette plot"));
		chartPanel.setBackground(Color.WHITE);
		chart.setTitle("");
		return chartPanel;
		
	}
	
	public static void main(String[] args) throws Exception {
		final Instances ds = WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(
				//new File("./samples/csv/uci/zoo.csv"));
				//new File("./samples/arff/UCI/cmc.arff"));
				//new File("./samples/csv/direct-marketing-bank-reduced.csv"));
				//new File("./samples/csv/bank.csv"));
				//new File("./samples/csv/preswissroll.csv"));
				new File("./samples/csv/iris.csv"));
				//new File("./samples/csv/lines.csv"));
				//new File("./samples/csv/pima.csv"));
				//new File("./samples/csv/isolet.csv"));
				//new File("./samples/csv/iris.csv"));


		final SimpleKMeans kmeans=WekaMachineLearningUtil.buildSimpleKMeansClustererWithK(3);

		final WekaClusteringResult cr=WekaMachineLearningUtil.computeClusters(kmeans,ds);

		final JFrame mainFrame = new JFrame();
		mainFrame.setTitle("Silhouette plot");
		mainFrame.setBackground(Color.WHITE);
		LogoHelper.setLogo(mainFrame);

		mainFrame.getContentPane().setLayout(new BorderLayout());
		mainFrame.getContentPane().add(buildSilhouettePanel(ds,cr),BorderLayout.CENTER);

		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		mainFrame.setSize(1200,900);
		mainFrame.setVisible(true);	

	}


}
