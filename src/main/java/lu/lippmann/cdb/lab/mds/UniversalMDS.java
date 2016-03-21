/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.lab.mds;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.util.*;
import java.util.List;

import javax.swing.border.TitledBorder;

import lu.lippmann.cdb.common.gui.dataset.InstanceFormatter;
import lu.lippmann.cdb.weka.WekaDataAccessUtil;

import org.ejml.simple.SimpleMatrix;
import org.jdesktop.swingx.*;
import org.jfree.chart.*;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;

import weka.core.*;

/**
 * 
 * @author didry
 *
 */
public final class UniversalMDS {

	private Instances ds;

	private MDSResult fMds;

	private SimpleMatrix distMatrixND;
	private SimpleMatrix distmatrix2D ;

	/** Original placed with ClassicMDS **/
	private double[][] coordinates;

	//private static final double THETA = 0.1d; 


	private enum MDSTypeEnum { 
		CLASSIC, SPHERICAL, ROBUST;
	}

	/**
	 * 
	 * @param ds
	 */
	public UniversalMDS(final Instances ds,final MDSDistancesEnum distEnum,final MDSTypeEnum type){
		this.ds = ds;
		try {
			this.fMds = ClassicMDS.doMDS(ds, distEnum, 2, 5000, true,false);

			this.distMatrixND   = fMds.getCInstances().getDistanceMatrix();
			//Convert to double[][]
			final SimpleMatrix mdsCoords = fMds.getCoordinates();
			final int N = ds.numInstances();
			this.coordinates = new double[N][2];
			for(int i = 0 ; i < mdsCoords.numRows() ; i++){
				for(int j = 0 ; j < mdsCoords.numCols() ; j++)
					this.coordinates[i][j] = mdsCoords.get(i,j);
			}

			/*
			double epsilon;
			int count = 0;
			do {
				epsilon = computeError(ds,type);
				for(int i = 0 ; i < N ; i++){
					placePoint(i,type,(int)Math.floor(Math.sqrt(count)),ds);
				}
				//System.out.println("Cost(1) : " + (epsilon-computeError(type))+"("+count+")");
				count++;
			}while(epsilon - computeError(ds,type) > THETA);// && count <= 100);
			//}while(Math.abs(epsilon - computeError(ds,type)) > THETA && count <= 100);
			 */

			computeDistance2D();

			for(int iter = 1 ; iter <= 100 ; iter++){
				double res = computeError(ds,type);

				for(int i = 0 ; i < N ; i++){
					for(int it=1;it<=Math.floor(Math.sqrt(iter)) ; it++){
						double[][] xChap = computeIntersections(i,ds);
						switch(type){
						case CLASSIC:
							coordinates[i] =  fRecenter(xChap,ds);
							break;
						case ROBUST:
							coordinates[i] = rRecenter(i,xChap,ds);
							break;
						case SPHERICAL:
							throw new IllegalStateException("Not implemented !");
						default:
							break;
						}
						computeDistance2D();
					}
				}

				double cost = (res-computeError(ds,type));
				System.out.println("Cost(2) : " + cost);
				//if(cost<THETA) break;
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	private void placePoint(int i,MDSTypeEnum type,int countMax,Instances ds) throws Exception {
//		double epsilon;
//		int count = 0;
//		do {
//			epsilon = g(i,type,ds);
//			double[][] xChap = computeIntersections(i,ds);
//			switch(type){
//			case CLASSIC:
//				coordinates[i] =  fRecenter(xChap,ds);
//				break;
//			case ROBUST:
//				coordinates[i] = rRecenter(i,xChap,ds);
//				break;
//			case SPHERICAL:
//				throw new IllegalStateException("Not implemented !");
//			default:
//				break;
//			}
//			computeDistance2D();
//			System.out.println("Cost(1) : " + Math.abs(epsilon- g(i,type,ds))+"("+count+"/"+countMax+")");
//			count++;
//			//}while(Math.abs(epsilon - g(i,type,ds)) > THETA && count < countMax);
//		}while(epsilon - g(i,type,ds) > THETA && count <= countMax);
//	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	private double computeError(final Instances ds,final MDSTypeEnum type){
		computeDistance2D();	//Rebuild distMatrix2D distances
		final int N = ds.numInstances();
		double err  = 0;
		for(int i = 0 ; i < N ; i++){
			for(int j = i+1 ; j < N ; j++){
				switch(type){
				case CLASSIC:
					err+=Math.pow(distMatrixND.get(i,j)-distmatrix2D.get(i,j),2);
					break;
				case ROBUST:
				case SPHERICAL:
					err+=Math.abs(distMatrixND.get(i,j)-distmatrix2D.get(i,j));
					break;
				default:
					break;
				}

			}
		}
		return 2*err;
	}



	/**
	 * Find intersection between Circle of center (x1,y1) of radius R in the direction of (x2,y2)
	 * @param x1
	 * @param y1
	 * @param r
	 * @param x2
	 * @param y2
	 * @return
	 */
	private static double[] findIntersection(double x1,double y1,double R,double x2,double y2){
		final double d=Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
		double	x = x1 - R*(x2-x1)/d;
		double	y = y1 - R*(y2-y1)/d;
		return new double[]{x,y};
	}

	/**
	 * 
	 */
	private void computeDistance2D(){
		final int N = coordinates.length;
		this.distmatrix2D   = new SimpleMatrix(N,N);
		final int M = 2;
		for(int i = 0 ; i < N ; i++){
			for(int j = i+1 ; j < N ; j++){
				double d = 0;
				for(int k = 0 ; k < M ; k++){
					d+=Math.pow(coordinates[i][k]-coordinates[j][k],2);
				}
				d=Math.sqrt(d);
				this.distmatrix2D.set(i,j,d);
				this.distmatrix2D.set(j,i,d);
			}
		}
	}

//	private double g(final int i,final MDSTypeEnum type,Instances ds){
//		double err = 0;
//		final int N = ds.numInstances();
//		for(int j = 0 ; j < N ; j++){
//			switch(type){
//			case CLASSIC:
//				err+=Math.pow(distMatrixND.get(i,j)-distmatrix2D.get(i,j),2);
//				break;
//			case ROBUST:
//			case SPHERICAL:
//				err+=Math.abs(distMatrixND.get(i,j)-distmatrix2D.get(i,j));
//				break;
//			default:
//				break;
//			}
//		}
//		return err;
//	}


	/**
	 * 
	 * @param i
	 * @param N
	 * @return
	 */
	private double[][] computeIntersections(int i,Instances ds) {
		final int N = ds.numInstances();
		double[][] xChap = new double[N][2];
		double xi = coordinates[i][0];
		double yi = coordinates[i][1];
		for(int j = 0 ; j < N ; j++){
			double rj = distMatrixND.get(i,j);
			//x_j = (x2,y2)
			double xj = coordinates[j][0];
			double yj = coordinates[j][1];
			//Case of xj=xi
			if(distmatrix2D.get(i,j)==0){
				xChap[j][0] = 0.0;
				xChap[j][1] = 0.0;
			}else{
				double[] xc = findIntersection(xj, yj,rj, xi, yi);
				xChap[j][0] = xc[0];
				xChap[j][1] = xc[1];	
			}

		}
		return xChap;
	}

	/**
	 * 
	 * @param xChap
	 * @return
	 */
	private double[] fRecenter(double[][] xChap,Instances ds) {
		final double[] center = new double[2];
		final int N = ds.numInstances();
		if(N==1){
			center[0]=xChap[0][0];
			center[1]=xChap[0][1];
		}else{
			for(int i = 0 ; i < N ; i++){
				center[0]+=xChap[i][0];
				center[1]+=xChap[i][1];
			}
			center[0]/=(N-1);
			center[1]/=(N-1);
		}
		return center;
	}

	private double[] rRecenter(int i,double[][] xChap,Instances ds) {
		double[] xi = coordinates[i];
		final double[] center = new double[2];
		final int N = ds.numInstances();
		double sum = 0.0d; 
		for(int j = 0 ; j < N ; j++){
			double d = Math.sqrt(Math.pow(xi[0]-xChap[j][0],2)+Math.pow(xi[1]-xChap[j][1],2));
			//Ignore xi
			if(j!=i){
				center[0]+=xChap[j][0]/d;
				center[1]+=xChap[j][1]/d;
				sum+=1/d;
			}
		}
		center[0]/=sum;
		center[1]/=sum;
		return center;
	}


	/**
	 * @return the ds
	 */
	public Instances getDs() {
		return ds;
	}

	/**
	 * @param ds the ds to set
	 */
	public void setDs(Instances ds) {
		this.ds = ds;
	}

	public  JXPanel buildMDSViewFromDataSet(Instances ds,MDSTypeEnum type) throws Exception {

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

		chart.setTitle(type.name()+" MDS");

		Attribute clsAttribute = null;
		int nbClass = 1;
		if(ds.classIndex()!=-1){
			clsAttribute = ds.classAttribute();
			nbClass = clsAttribute.numValues();
		}

		final List<XYSeries> lseries = new ArrayList<XYSeries>();
		if(nbClass<=1){
			lseries.add(new XYSeries("Serie #1",false));
		}else {
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
			filteredInstances.add(new Instances(ds,0));
		}

		for(int i=0 ; i < ds.numInstances() ; i++){
			final Instance oInst   = ds.instance(i);
			int indexOfSerie = 0;
			if(oInst.classIndex()!=-1){
				indexOfSerie = (int)oInst.value(oInst.classAttribute());
			}
			lseries.get(indexOfSerie).add(coordinates[i][0],coordinates[i][1]); 
			filteredInstances.get(indexOfSerie).add(oInst);
		}

		final List<Paint> colors = new ArrayList<Paint>();

		for(final XYSeries series : lseries){
			dataset.addSeries(series);
		}


		final XYToolTipGenerator gen = new XYToolTipGenerator() {
			@Override
			public String generateToolTip(XYDataset dataset, int series, int item) {
				return InstanceFormatter.htmlFormat(filteredInstances.get(series).instance(item),true);
			}
		};

		final Shape shape = new Ellipse2D.Float(0f,0f,5f,5f);

		((XYLineAndShapeRenderer)xyPlot.getRenderer()).setUseOutlinePaint(true);

		for(int p=0 ; p < nbClass ; p++){
			xyPlot.getRenderer().setSeriesToolTipGenerator(p,gen);
			((XYLineAndShapeRenderer)xyPlot.getRenderer()).setLegendShape(p, shape);
			xyPlot.getRenderer().setSeriesOutlinePaint(p, Color.BLACK);
		}

		for(int ii = 0 ; ii < nbClass ; ii++){
			colors.add(xyPlot.getRenderer().getItemPaint(ii,0));
		}

		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setPreferredSize(new Dimension(1200,900));
		chartPanel.setBorder(new TitledBorder("MDS Projection"));
		chartPanel.setBackground(Color.WHITE);

		final JXPanel allPanel=new JXPanel();
		allPanel.setLayout(new BorderLayout());
		allPanel.add(chartPanel,BorderLayout.CENTER);

		return allPanel;
	}

	public static void main(String[] args) throws Exception {


		final Instances ds = WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(
				//new File("./samples/csv/uci/zoo.csv"));
				//new File("./samples/arff/UCI/cmc.arff"));
				//new File("./samples/csv/direct-marketing-bank-reduced.csv"));
				//new File("./samples/csv/bank.csv"));
				//new File("./samples/csv/preswissroll.csv"));
				//new File("./samples/csv/preswissroll-mod4.csv"));
				new File("./samples/csv/lines.csv"));

		/*
		Resample rs = new Resample();
		rs.setInputFormat(dsOrder);
		rs.setSampleSizePercent(50);
		Instances ds = Filter.useFilter(dsOrder,rs);
		 */
		
		final MDSTypeEnum type = MDSTypeEnum.CLASSIC;

		final UniversalMDS mds = new UniversalMDS(ds, MDSDistancesEnum.EUCLIDEAN,type);
		final JXPanel panel = mds.buildMDSViewFromDataSet(ds,type);


		final JXFrame f = new JXFrame();
		final Container c = f.getContentPane();
		c.add(panel);
		f.setVisible(true);
		f.setDefaultCloseOperation(JXFrame.EXIT_ON_CLOSE);
		f.pack();


	}


}
