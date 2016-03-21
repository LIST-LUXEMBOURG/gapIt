/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.lab.beta.shih;

import java.io.File;
import java.util.*;

import lu.lippmann.cdb.lab.beta.util.WekaUtil2;
import lu.lippmann.cdb.weka.WekaDataAccessUtil;
import weka.core.*;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.Resample;


/**
 * 
 * @author didry
 *
 */
public final class Shih2010 {

	private Instances instances;
	private boolean ignoreClass;
	private int n;
	private int[][] M;

	private double[][] D;
	private double theta;

	private Map<TupleSI,Double> F;

	private int baseIndex;
	private List<TupleSI> base;
	private List<TupleSI> noBase;
	private List<Integer> idxsC;

	private List<Integer> idxsN;
	private double[] maxNum;


	// Map<id attribute (nominal),Map<String (value attribute),Integer(matrix index)>>
	private Map<Integer,Map<String,Integer>> mapIndex;
	private Map<Integer,Set<String>> 		 mapDomain;

	// Max instance taken in for the distance, otherwise we sample
	private boolean resample;
	public static final int MAX_INSTANCES_TAKEN = 2500;

	/**
	 * 
	 * @param instances
	 */
	public Shih2010(Instances instances){
		this(instances,false,false,0.01);
	}

	public Shih2010(Instances instances,boolean ignoreClass,boolean needsToResample,double theta){
		try {
			this.instances = instances;
			this.theta     = theta;
			this.resample = needsToResample;
			if(needsToResample){
				final Resample rs = new Resample();
				if(this.instances.numInstances() > MAX_INSTANCES_TAKEN){
					rs.setInputFormat(instances);
					rs.setSampleSizePercent(MAX_INSTANCES_TAKEN*100.0/this.instances.numInstances());
					this.instances = Filter.useFilter(instances, rs);
				}
			}

			//System.out.println("Size = " + this.instances.numInstances());

			this.mapDomain   = new HashMap<Integer, Set<String>>();

			if(ignoreClass){this.instances.setClassIndex(-1);}

			//Save index of nominal & categorial attributes
			//Build a map i-DOM -> Attribute index
			this.idxsC=new ArrayList<Integer>();
			this.idxsN=new ArrayList<Integer>();
			int nn = 0;

			for(int i = 0 ; i < instances.numAttributes() ; i++){
				if(!instances.attribute(i).isNumeric())
					mapDomain.put(i,new HashSet<String>());
			}

			//Create map index & domain
			this.mapIndex = new HashMap<Integer, Map<String,Integer>>();
			int  mapIdx   = 0;
			for(int i = 0 ; i < instances.numAttributes() ; i++){
				Attribute attribute = instances.attribute(i);
				if(!attribute.isNumeric()){
					idxsC.add(i); //i-th attribute is nominal
					final Map<String,Integer> mapIndexAttribute = new HashMap<String, Integer>();
					mapIndex.put(i,mapIndexAttribute);
					Enumeration<?> en = attribute.enumerateValues();
					while(en.hasMoreElements()){
						String catVal = en.nextElement().toString();
						boolean created = mapDomain.get(i).add(catVal);
						if(created){
							mapIndexAttribute.put(catVal,mapIdx++);
						}
					}
					nn+=mapDomain.get(i).size(); //count total nominal values
				}else{
					idxsN.add(i);
				}
			}

			this.n = nn;
			this.base      = new ArrayList<TupleSI>();
			this.noBase    = new ArrayList<TupleSI>();
			this.M		   = new int[n][n];
			this.D		   = new double[n][n];
			this.F		   = new HashMap<TupleSI, Double>();
			this.computeBase();
			this.computeMatrixMDF();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param filePath
	 * @param ignoreClass
	 * @param theta
	 * @throws Exception
	 */
	public Shih2010(String filePath,boolean ignoreClass,boolean needsToResample,double theta) throws Exception{
		this(WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(new File(filePath)),ignoreClass,needsToResample,theta);
	}


	/**
	 * 
	 * @param dataSet
	 * @return
	 */
	private void computeBase()
	{
		//Save base
		int attributeIndex = -1;
		int max = 0;
		for(int i = 0 ; i < instances.numAttributes() ; i++){
			final Attribute attribute = instances.attribute(i);

			//Ignore class attribute if needed
			if(attribute.index()==instances.classIndex()&&ignoreClass) continue;

			if(!attribute.isNumeric()){
				int size = instances.attributeStats(i).nominalCounts.length;
				if(size > max){attributeIndex = i;max=size;}
			}
		}
		final Attribute maxAttribute = instances.attribute(attributeIndex);
		Enumeration<?> en = maxAttribute.enumerateValues();
		while(en.hasMoreElements()){
			base.add(new TupleSI(en.nextElement().toString(),attributeIndex));
		}
		this.baseIndex = attributeIndex;

		//Save noBase
		for(int i = 0 ; i < instances.numAttributes() ; i++){
			Attribute attribute = instances.attribute(i);
			if(attribute.index()==instances.classIndex()&&ignoreClass) continue;
			if(i != attributeIndex && !instances.attribute(i).isNumeric()){
				Enumeration<?> enb = attribute.enumerateValues();
				while(enb.hasMoreElements()){
					noBase.add(new TupleSI(enb.nextElement().toString(),i));
				}
			}
		}
	}




	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	private void computeMatrixMDF() throws Exception
	{
		//Build filtered instance for each element of the base
		int baseSize = base.size();
		Attribute baseAttribute = instances.attribute(baseIndex);

		//Create baseSize copy of instances for filtering
		List<Instances> filteredInstances = new ArrayList<Instances>();
		for(int i = 0 ; i < baseSize ; i++){filteredInstances.add(new Instances(instances));}

		//Filter
		for(int i = 0 ; i < instances.numInstances() ; i++){
			final Instance instance   = instances.instance(i);
			for(final TupleSI j : base){
				final int wekaAttributeValue = (int)instance.value(baseIndex);
				if(!new TupleSI(baseAttribute.value(wekaAttributeValue),baseIndex).equals(j)){
					WekaUtil2.removeFromInstances(filteredInstances.get(base.indexOf(j)),instance);
				}
			}
		}

		//Compute I vector
		final int idxsNs = idxsN.size();

		if(idxsNs==0){
			throw new Exception("You need at least one numerical attribute !!");
		}

		int minIndexForI = -1;
		double minValueForI = Double.MAX_VALUE;
		final double[][] meanBase = new double[idxsNs][baseSize];
		int p = 0;
		for(final Integer num : idxsN){
			double Ip = 0.0;
			for(int j = 0 ; j < baseSize ; j++){
				final List<Instance> filtredInstance = filteredInstances.get(j);
				final int fs = filtredInstance.size();
				double mean = 0;
				for(int l = 0 ; l < fs ; l++){ mean+=filtredInstance.get(l).value(num);}
				mean=mean/fs;
				meanBase[p][j] = mean;
				for(int l = 0 ; l < fs ; l++){
					Ip+=Math.pow(filtredInstance.get(l).value(num)-mean,2);
				}
			}
			if(Ip < minValueForI){minValueForI = Ip;  minIndexForI = p;}
			p++;
		}

		this.maxNum = new double[idxsNs];
		for(int i = 0 ; i <  instances.numInstances() ; i++){
			final Instance instance = instances.instance(i);
			//Save maximum value for each numerical attribute
			for(Integer n1 : idxsN){
				double val = instance.value(n1);
				int    idx = idxsN.indexOf(n1);
				if(val > maxNum[idx]){maxNum[idx] = val;}
			}
			//Compute matrix M for each categorical attribute
			for(final Integer e1 : idxsC){
				for(final Integer e2 : idxsC){
					final int i1 = getIndexOf(e1,instance.attribute(e1).value((int)instance.value(e1)));
					final int j1 = getIndexOf(e2,instance.attribute(e2).value((int)instance.value(e2)));
					M[i1][j1]=M[i1][j1]+1;
				}
			}
		}

		//Compute D matrix
		for(int i = 0 ; i < n ; i++){
			for(int j = 0 ; j < n ; j++){
				double d = M[i][j]/(M[i][i]+M[j][j]-M[i][j]+0.0);
				if(d >= theta){
					D[i][j]=d;
				}else{
					D[i][j]=0;
				}
			}
		}

		//Compute F matrix for base
		for(final TupleSI baseVal : base){
			F.put(baseVal,meanBase[minIndexForI][base.indexOf(baseVal)]);	
		}

		//Compute F matrix for noBase
		for(final TupleSI noBaseVal : noBase){
			double f = 0.0;
			for(final TupleSI baseVal : base){
				f+=D[getIndexOf(noBaseVal)][getIndexOf(baseVal)]*F.get(baseVal);
			}
			F.put(noBaseVal,f);
		}


	}

	/**
	 * 
	 * @param e1
	 * @param value
	 * @return
	 */
	private int getIndexOf(Integer e1, String value) {
		return mapIndex.get(e1).get(value);
	}

	/**
	 * 
	 * @param t
	 * @return
	 */
	private int getIndexOf(TupleSI t) {
		return getIndexOf(t.getY(),t.getX());
	}

	/**
	 * 
	 * @param o
	 */
	public static void showMatrix1(int[][] o){
		for(int i = 0 ; i < o.length ; i++){
			for(int j = 0 ; j < o[i].length ; j++){
				System.out.print(o[i][j]+"\t");
			}
			System.out.println();
		}
	}

	/**
	 * 
	 * @param o
	 */
	public static void showMatrix1(Object[][] o){
		for(int i = 0 ; i < o.length ; i++){
			for(int j = 0 ; j < o[i].length ; j++){
				System.out.print(o[i][j]+"\t");
			}
			System.out.println();
		}
	}

	/**
	 * 
	 * @param o
	 */
	public static void showMatrix2(double[][] o){
		for(int i = 0 ; i < o.length ; i++){
			for(int j = 0 ; j < o[i].length ; j++){
				System.out.print(o[i][j]+"\t");
			}
			System.out.println();
		}
	}


	/**
	 * 
	 * @return
	 */
	public Instances getModifiedInstances(){

		//Copy attribute list (and change categorical by numerical)
		final ArrayList<Attribute> lAttrs = new ArrayList<Attribute>();
		for(int i = 0 ; i < instances.numAttributes() ; i++){
			Attribute attr = instances.attribute(i);
			if(attr.isNumeric()||attr.index()==instances.classIndex()){
				lAttrs.add(attr);
			}else {
				Attribute newAttr = new Attribute(attr.name());
				lAttrs.add(newAttr);
			}
		}

		//Build new instance
		final Instances newInstances = new Instances("Shih instance",lAttrs,instances.numInstances());
		newInstances.setClassIndex(instances.classIndex());
		for(int i = 0 ; i < instances.numInstances() ; i++){
			final Instance instance = instances.instance(i);
			final Instance cpyInstance = (Instance)instance.copy();
			for(int j = 0 ; j < instance.numAttributes() ; j++){
				Attribute attribute = instance.attribute(j);
				int k = 0;
				if(attribute.index()==instances.classIndex()){
					//The class index is nominal
					cpyInstance.setValue(attribute,instance.stringValue(j));
				}else if(!attribute.isNumeric()){
					String elt  = attribute.value((int)instance.value(j));
					cpyInstance.setValue(attribute,F.get(new TupleSI(elt,j)));
				}else{
					if(maxNum[k] > 1){
						cpyInstance.setValue(attribute,instance.value(j)/maxNum[k]);
					}
					k++;
				}
			}
			newInstances.add(cpyInstance);
		}

		if(ignoreClass && instances.classIndex()!=-1){
			newInstances.deleteAttributeAt(instances.classIndex());
		}
		return newInstances;
	}


	/**
	 * @return the instances
	 */
	public Instances getInstances() {
		return instances;
	}

	/**
	 * @return the n
	 */
	public int getN() {
		return n;
	}


	/**
	 * @return the m
	 */
	public int[][] getM() {
		return M;
	}

	/**
	 * @return the d
	 */
	public double[][] getD() {
		return D;
	}

	/**
	 * @return the f
	 */
	public Map<TupleSI, Double> getF() {
		return F;
	}


	/**
	 * @return the baseIndex
	 */
	public int getBaseIndex() {
		return baseIndex;
	}

	/**
	 * @return the base
	 */
	public List<TupleSI> getBase() {
		return base;
	}


	/**
	 * @return the noBase
	 */
	public List<TupleSI> getNoBase() {
		return noBase;
	}


	/**
	 * @return the idxsC
	 */
	public List<Integer> getIdxsC() {
		return idxsC;
	}

	/**
	 * @return the idxsN
	 */
	public List<Integer> getIdxsN() {
		return idxsN;
	}

	/**
	 * Sorted domain
	 * @return
	 */
	public SortedSet<TupleSI> getDomain(){
		final SortedSet<TupleSI> domain = new TreeSet<TupleSI>();
		domain.addAll(base);
		domain.addAll(noBase);
		return domain;
	}



	/**
	 * @return the mapDomain
	 */
	public Map<Integer, Set<String>> getMapDomain() {
		return mapDomain;
	}

	/**
	 * @return the resample
	 */
	public boolean isResample() {
		return resample;
	}

	/**
	 * @param resample the resample to set
	 */
	public void setResample(boolean resample) {
		this.resample = resample;
	}

	/**
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) {

		//String filePath = "./samples/csv/salary.csv";
		//String filePath = "./samples/csv/shih.csv";
		//String filePath = "./samples/csv/uci/zoo.csv";
		String filePath = "./samples/csv/bank.csv";
		//String filePath = "./samples/csv/uci/mushroom.csv";
		//String filePath = "./samples/csv/uci/house-votes-84.csv";
		//String filePath = "./samples/csv/uci/credit-g.csv";

		try {
			final Shih2010 shih = new Shih2010(filePath,false,true,0.01);

			final Instances oldInstances = shih.getInstances();
			final int clsIndex    = oldInstances.classIndex();


			System.out.println("Base = " + shih.getBase());
			System.out.println("NoBase =" + shih.getNoBase());


			int[][] M = shih.getM();
			showMatrix1(M);

			System.out.println("----------------");

			double[][] D = shih.getD();
			showMatrix2(D);

			System.out.println("----------------");

			Map<TupleSI,Double> F = shih.getF();
			System.out.println(F);

			System.out.println("----------------");


			//Launch k-means for testing
			final Instances newInstances = shih.getModifiedInstances();

			System.out.println(newInstances);

			//final List<String> className = WekaUtil.getClassesValues(oldInstances);
			/*
		//
		//2-step algorithm
		//
		int K = (int)(oldInstances.numInstances()/3.0);
		System.out.println("K="+K);
		List<IndexedInstance> subSets = WekaUtil2.doHAC(newInstances, K);


		final List<MixedCentroid> lCentroids = new ArrayList<MixedCentroid>();
		for(int i = 0 ; i < subSets.size() ;i++){
			//Build original subset from numeric subset to add additionnal info to centroids
			final IndexedInstance subSet = subSets.get(i);
			Map<Integer,Integer> mapIdx = subSet.getMapOrigIndex();
			Instances origSubset = new Instances(oldInstances,0);
			for(Integer rowIndex : mapIdx.keySet()){
				origSubset.add(oldInstances.instance(mapIdx.get(rowIndex)));
			}
			MixedCentroid centroid = WekaUtil2.computeMixedCentroid(false, new EuclideanDistance(subSet.getInstances()), subSet.getInstances(), origSubset,i);
			//System.out.println(centroid);
			lCentroids.add(centroid);
		}



		final SortedSet<TupleSI> domain = shih.getDomain();
		final int lCentroidsSize = lCentroids.size();
		final ArrayList<Attribute> lAttrs = new ArrayList<Attribute>();
		if(lCentroidsSize > 0){
			for(int i = 0 ; i < newInstances.numAttributes() ; i++){
				lAttrs.add(newInstances.attribute(i));
			}
			for(TupleSI d : domain){
				lAttrs.add(new Attribute(d.getY()+"("+d.getX()+")"));
			}
			//Create centroid instances
			final Instances centroidInstances = new Instances("Centroid instance",lAttrs,lCentroidsSize);
			for(int i = 0 ; i < lCentroids.size() ; i++){
				double[] m11 = lCentroids.get(i).getMixedCentroid(domain);
				centroidInstances.add(new DenseInstance(1d,m11));
			}

			K = 5;
			SimpleKMeans mdf1 = new SimpleKMeans();
			mdf1.setOptions(Utils.splitOptions("-N " + K +" -R first-last -I 500 -S 10 -A weka.core.EuclideanDistance"));
			mdf1.buildClusterer(centroidInstances);
			List<IndexedInstance> lInstances = WekaUtil2.computeClusters(mdf1,centroidInstances);
			Instances initialCentroid = new Instances(newInstances,0);
			for(IndexedInstance instances : lInstances){
				Instance centroid = WekaMachineLearningUtil.computeCentroid(false,new EuclideanDistance(instances.getInstances()),instances.getInstances());
				int nbCentroids = centroid.numAttributes();
				for(int j = nbCentroids -1 ; j >= newInstances.numAttributes(); j--){
					centroid.deleteAttributeAt(j);
				}
				initialCentroid.add(centroid);
			}


			//System.out.println(initialCentroid);

			ModifiedKMeans mdf = new ModifiedKMeans(initialCentroid);
			mdf.setOptions(Utils.splitOptions("-N " + K +" -R first-last -I 500 -S 10 -A weka.core.EuclideanDistance"));
			mdf.setInitializeUsingKMeansPlusPlusMethod(true);
			mdf.buildClusterer(newInstances);
			final ClusterEvaluation eval=new ClusterEvaluation();
			eval.setClusterer(mdf);
			eval.evaluateClusterer(newInstances);



			List<IndexedInstance> lis = WekaUtil2.computeClusters(mdf, newInstances);
			List<Instances> result = new ArrayList<Instances>();
			lAttrs.clear();
			for(int i = 0 ; i < oldInstances.numAttributes() ; i++){
				lAttrs.add(oldInstances.attribute(i));
			}
			//lAttrs.add(oldInstances.attribute(clsIndex));
			Map<Integer,Map<String,Integer>> clusterRepartionClass = new HashMap<Integer, Map<String,Integer>>();
			List<String> lValues = new ArrayList<String>();
			for(int i = 0 ; i < K ; i++){
				lValues.add(String.valueOf(i));
			}
			int k=0;
			lAttrs.add(new Attribute("[cluster]",lValues));
			for(IndexedInstance idxInstances : lis){
				Instances instances = idxInstances.getInstances();
				Instances rInstances = new Instances("DTA instance",lAttrs,oldInstances.numInstances());
				rInstances.setClassIndex(instances.numAttributes());
				for(int i = 0 ; i < instances.numInstances() ; i++){
					int mappingIdx = idxInstances.getMapOrigIndex().get(i);
					double[] attValues = new double[instances.numAttributes()+2];
					DenseInstance instance = new DenseInstance(1d, attValues);
					int j = 0;
					for(j = 0 ; j < instances.numAttributes() ; j++){
						attValues[j] = oldInstances.instance(mappingIdx).value(j);
					}
					int clsValue = (int)oldInstances.instance(mappingIdx).value(clsIndex);
					j = instances.numAttributes();
					attValues[j]=clsValue;
					attValues[j+1]=k;
					rInstances.add(instance);
				}
				result.add(rInstances);

				//Build repartition class
				Map<String,Integer> repartionClass = new HashMap<String, Integer>();
				for(int i = 0 ; i < instances.numInstances() ; i++){
					String clsValue = oldInstances.attribute(oldInstances.classIndex()).value((int)oldInstances.instance(idxInstances.getMapOrigIndex().get(i)).value(oldInstances.classIndex()));
					if(!repartionClass.containsKey(clsValue)) repartionClass.put(clsValue,0);
					repartionClass.put(clsValue,repartionClass.get(clsValue)+1);
				}
				clusterRepartionClass.put(k, repartionClass);
				k++;
			}

			//Used result to discretize & apriori
			int ss = shih.getIdxsN().size();
			k = 0;
			Map<Integer,AssociationRules> rulePerCluster = new HashMap<Integer, AssociationRules>();
			for(Instances instances : result){
				int[] arrayToDiscretize = new int[ss+1];
				for(int i = 0 ; i <ss ; i++){
					arrayToDiscretize[i] = shih.getIdxsN().get(i);
				}

				Apriori ap = new Apriori();
				Discretize disc = new Discretize();
				disc.setOptions(Utils.splitOptions("-B 10"));
				disc.setAttributeIndicesArray(arrayToDiscretize);
				disc.setInputFormat(instances);

				//disc.set
				Instances discInstances = Filter.useFilter(instances, disc);

				//System.out.println(discInstances);

				ap.buildAssociations(discInstances);
				AssociationRules rules = ap.getAssociationRules();
				rulePerCluster.put(k,rules);
				k++;
			}

			double confidenceFactor = 0.5d;
			final int frameWidth=1250;//1024;
			final int frameHeight=950;//768;
			try 
			{
				final EventPublisher eventPublisher = new EventPublisherBushImpl();				

				Instances ai = WekaMachineLearningUtil.buildDataSetExplainingClustersAssignment(result,"cluster",true);				
				DecisionTree dt    = new C45DecisionTreeFactory(confidenceFactor).buildDecisionTree(ai);
				GraphWithOperations gwo =  dt.getGraphWithOperations();
				final GraphView myGraph = DecisionTreeToGraphViewHelper.buildGraphView(gwo,ai,eventPublisher,new CommandDispatcherFakeImpl());

				//Compute association rule string for showing
				StringBuilder resume = new StringBuilder("<html>\n");
				for(Integer clusterNum : rulePerCluster.keySet()){
					resume.append("<b><font color='red'>Cluster : ").append(clusterNum+1).append("</font></b><br/>");
					resume.append("Repartition : ").append(clusterRepartionClass.get(clusterNum)).append("<br/><br/>");	
					for(AssociationRule rule : rulePerCluster.get(clusterNum).getRules()){
						if(!rule.getPremise().toString().contains("cluster")
								&&!rule.getConsequence().toString().contains("cluster")
								&&!rule.getPremise().toString().contains("'All'")
								&&!rule.getConsequence().toString().contains("'All'")
								){ //Do not put cluster rules & attribute with all same value
							resume.append(rule).append("<br/>");
						}
					}
					resume.append("<br/>");
				}
				resume.append("<br/><b><font color='red'>Full set rules :</font></b><br/>");
				int[] arrayToDiscretize = new int[ss+1];
				for(int i = 0 ; i <ss ; i++){arrayToDiscretize[i] = shih.getIdxsN().get(i);}
				Apriori ap = new Apriori();
				Discretize disc = new Discretize();
				disc.setOptions(Utils.splitOptions("-B 10"));
				disc.setAttributeIndicesArray(arrayToDiscretize);
				disc.setInputFormat(oldInstances);
				Instances discInstances = Filter.useFilter(oldInstances, disc);
				ap.buildAssociations(discInstances);
				AssociationRules rules = ap.getAssociationRules();
				for(AssociationRule rule : rules.getRules()){
					resume.append(rule).append("<br/>");
				}
				resume.append("</html>");
				//END Compute association rule string for showing

				final JXFrame f2=new JXFrame();
				LogoHelper.setLogo(f2);
				f2.setTitle("Rules for each cluster");
				f2.setLayout(new BorderLayout());
				f2.setPreferredSize(new Dimension(frameWidth,frameHeight));
				JScrollPane scrollPane = new JScrollPane(new JLabel(resume.toString()));
				f2.add(scrollPane);
				f2.pack();
				f2.setResizable(false);
				f2.setVisible(true);


				final JXFrame f3=new JXFrame();
				LogoHelper.setLogo(f3);
				f3.setTitle("Cluster assignement view");
				f3.setLayout(new BorderLayout());
				f3.setPreferredSize(new Dimension(frameWidth,frameHeight));
				f3.add(myGraph.asComponent());
				f3.pack();
				f3.setResizable(false);
				f3.setVisible(true);


				for(int i = 0 ; i < K ; i++){
					List<CNode> fs = gwo.findNodeByName("cluster"+(i+1));
					GraphWithOperations gwo2 = GraphUtil.filterGraphWithFinalState(gwo,fs.get(0));
					final GraphView myGraphFilter = DecisionTreeToGraphViewHelper.buildGraphView(gwo2,null,eventPublisher,new CommandDispatcherFakeImpl());
					final JXFrame f4=new JXFrame();
					LogoHelper.setLogo(f4);
					f4.setTitle("Cluster filtred view for cluster : " + i);
					f4.setLayout(new BorderLayout());
					f4.setPreferredSize(new Dimension(frameWidth,frameHeight));
					f4.add(myGraphFilter.asComponent());
					f4.pack();
					f4.setResizable(false);
					f4.setVisible(true);
				}


			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}


		}	
			 */

		}
		catch(Exception e){
			e.printStackTrace();
		}



	}

}
