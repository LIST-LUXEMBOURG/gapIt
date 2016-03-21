/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.lab.mds;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

import lu.lippmann.cdb.common.*;
import lu.lippmann.cdb.weka.*;

import org.ejml.data.*;
import org.ejml.factory.*;
import org.ejml.simple.*;
import org.jdesktop.swingx.*;

import weka.clusterers.SimpleKMeans;
import weka.core.*;

/**
 * ClassicMDS.
 * 
 * @author didry
 */
public final class ClassicMDS 
{
	/**
	 * Private constructor.
	 */
	private ClassicMDS() {}

	/**
	 * 
	 */
	private static KmeansResult getSimplifiedInstances(final Instances instances,final DistanceFunction df,final int maxInstances) throws Exception{
		Instances centroids = null; 
		List<Instances> clusters = null;

		final int savedClassIndex = instances.classIndex();
		instances.setClassIndex(-1);
		final SimpleKMeans clusterer = WekaMachineLearningUtil.buildSimpleKMeansClustererWithK(maxInstances,df);
		clusterer.buildClusterer(instances);
		clusters = WekaMachineLearningUtil.computeClusters(clusterer, instances).getClustersList();
		instances.setClassIndex(savedClassIndex);
		final int numClusters = clusters.size();
		//Set class index for each cluster instances
		//System.out.println("Setting class index to each cluster : " + savedClassIndex);
		for(int i = 0 ; i < numClusters; i++){
			clusters.get(i).setClassIndex(savedClassIndex);
		}
		//Save centroids
		centroids      = clusterer.getClusterCentroids();

		return new KmeansResult(centroids,clusters);
	}


	/**
	 * 
	 */
	public static CollapsedInstances distanceBetweenInstances(final Instances instances,final MDSDistancesEnum distEnum,final int maxInstances,final boolean ignoreClassInDistance) throws Exception{
		KmeansResult mapCentroids = null;

		final NormalizableDistance usedDist;
		if (distEnum.equals(MDSDistancesEnum.EUCLIDEAN)){
			usedDist=new EuclideanDistance(instances);
			//usedDist.setDontNormalize(true);
			//usedDist.setAttributeIndices("1");
			//usedDist.setInvertSelection(true);
		}
		else if (distEnum.equals(MDSDistancesEnum.MANHATTAN)) usedDist=new ManhattanDistance(instances);
		else if (distEnum.equals(MDSDistancesEnum.MINKOWSKI)){
			usedDist=new MinkowskiDistance(instances);
			final String[] parameters = MDSDistancesEnum.MINKOWSKI.getParameters();
			//Change order
			double order = Double.valueOf(parameters[0]).doubleValue();
			((MinkowskiDistance)usedDist).setOrder(order);
		}
		else if (distEnum.equals(MDSDistancesEnum.CHEBYSHEV)) usedDist=new ChebyshevDistance(instances);
		//else if (distEnum.equals(MDSDistancesEnum.DT)) usedDist=new DTDistance(instances);
		else throw new IllegalStateException();		

		final int numInstances = instances.numInstances();
		final boolean collapsed = (numInstances > maxInstances)
				&&(distEnum.equals(MDSDistancesEnum.EUCLIDEAN)||distEnum.equals(MDSDistancesEnum.MANHATTAN));

		SimpleMatrix distances;

		//Ignore class in distance
		if(ignoreClassInDistance && instances.classIndex()!=-1){
			usedDist.setAttributeIndices(""+(instances.classIndex()+1));
			usedDist.setInvertSelection(true);
		}

		int numCollapsedInstances = numInstances;
		if(collapsed){
			//Compute distance with centroids using K-means with K=MAX_INSTANCES
			mapCentroids = getSimplifiedInstances(instances,usedDist,maxInstances);

			final List<Instance> centroids = mapCentroids.getCentroids();
			numCollapsedInstances = centroids.size();

			distances = new SimpleMatrix(numCollapsedInstances,numCollapsedInstances);

			for (int i=0; i<numCollapsedInstances; i++){
				for (int j=i+1; j<numCollapsedInstances; j++){
					double dist = usedDist.distance(centroids.get(i), centroids.get(j));
					distances.set(i,j,dist);
					distances.set(j,i,dist);
				}
			}
		}else{
			distances = new SimpleMatrix(numCollapsedInstances,numCollapsedInstances);
			for (int i=0; i<numCollapsedInstances; i++){
				for (int j=i+1; j<numCollapsedInstances; j++){
					double dist = usedDist.distance(instances.get(i), instances.get(j));
					distances.set(i,j,dist);
					distances.set(j,i,dist);
				}
			}
		}
		return new CollapsedInstances(instances,mapCentroids,distances,collapsed);
	}


	/**
	 * Get a squared matrix with a given dimension, filled with 1-1/n
	 * @param size The matrix dimension
	 * @return The squared matrix with a given dimension, filled with 1.0
	 */
	private static SimpleMatrix getJMatrix(int size)
	{
		final SimpleMatrix matrix = new SimpleMatrix(size,size);
		for(int i = 0 ; i < size ; i++)
		{
			for(int j = 0 ; j < size ; j++)
			{
				if(i == j ){
					matrix.set(i,j,1-1.0/size);
				}else{
					matrix.set(i,j,-1.0/size);
				}
			}
		}

		return matrix;
	}

	public static SimpleMatrix getHMatrix(int size)
	{
		final SimpleMatrix matrix = new SimpleMatrix(size,size);
		for(int i = 0 ; i < size ; i++)
		{
			for(int j = 0 ; j < size ; j++)
			{
				if(i == j ){
					matrix.set(i,j,1-1.0/size);
				}else{
					matrix.set(i,j,-1.0/size);
				}
			}
		}

		return matrix;
	}

	/**
	 * Get the B matrix in the classical MDS algorithm
	 * @param matrix The distance matrix
	 * @return The B matrix : B = -J/2*[M^2]*J
	 */
	public static SimpleMatrix getBMatrix(final SimpleMatrix matrix)
	{
		final SimpleMatrix matrixSquared = matrix.elementMult(matrix);
		SimpleMatrix bMatrix = getJMatrix(matrix.numRows());
		final SimpleMatrix secondPart = bMatrix;
		bMatrix = bMatrix.scale(-0.5);
		bMatrix = bMatrix.mult(matrixSquared);
		bMatrix = bMatrix.mult(secondPart);
		return bMatrix;
	}

	private static SimpleMatrix getKMatrix(final SimpleMatrix D)
	{
		SimpleMatrix H = getJMatrix(D.numRows());
		return H.mult(D).mult(H).scale(-0.5); 
	}

	public static SimpleMatrix buildKMatrix(final SimpleMatrix D,final int dimension) throws Exception
	{
		final int N = D.numRows();
		final SimpleMatrix H = getJMatrix(N);
		//System.out.println("H done");
		final SimpleMatrix K2 = getKMatrix(D.elementMult(D));
		//System.out.println("K2 done");
		final SimpleMatrix K  = getKMatrix(D);
		//System.out.println("K done");
		final SimpleMatrix M = new SimpleMatrix(2*N,2*N);
		for(int i = 0 ; i < 2*N ; i++ ){
			for(int j = 0 ; j < 2*N ; j++){
				if(i<N && j >= N){
					M.set(i,j,2*K2.get(i,j-N));
				}
				if(i>=N && j < N && i-N==j){
					M.set(i,j,-1);
				}
				if(i>=N && j >=N){
					M.set(i,j,-4*K.get(i-N,j-N));
				}
			}
		}
		//System.out.println("M done");

		//Compute eigenvalue ... don't care about eigenvectors
		EigenDecomposition<DenseMatrix64F> ed = DecompositionFactory.eig(2*N,false);
		ed.decompose(M.getMatrix());
		//---------------------

		//System.out.println("M eig done");
		final int nbEig = ed.getNumberOfEigenvalues();
		double c = 0.0d;
		for(int i = 0 ; i < nbEig ; i++){
			final Complex64F lambda = ed.getEigenvalue(i);
			double re = lambda.real;
			if(re > c){ c = re; }
		}
		//System.out.println("C* found = " + c);
		final SimpleMatrix KChap = K2.plus(K.scale(2*c)).plus(H.scale(c*c/2.0));

		//System.out.println("B done");
		return KChap;
	}

	/**
	 * Get the list of the n biggest eigen values of the given matrix
	 * @param dimension The number of the eigen values to return
	 * @param decomposition The matrix decomposition
	 * @return The list of the n biggest eigen values or null if not enough eigen values
	 */
	public static List<Double> getEigenValues(final SimpleEVD<SimpleMatrix> decomposition,final int dimension)
	{
		List <Double> eigenValues = new ArrayList <Double>();

		int nbEigens = decomposition.getNumberOfEigenvalues();
		// Only takes positive values
		for(int i = 0 ; i < nbEigens ; i++)
		{
			double d = decomposition.getEigenvalue(i).getReal();
			if(d > 0){eigenValues.add(d);}
		}
		Collections.sort(eigenValues, Collections.reverseOrder());
		if(eigenValues.size() >= dimension)
		{
			eigenValues = eigenValues.subList(0, dimension);
			return eigenValues;
		}
		return null;

	}

	/**
	 * Get the eigen vectors list from the eigen values and the eigen decomposition
	 * @param values The eigen values
	 * @param decomposition The eigen decomposition
	 * @return The corresponding eigen vectors list
	 */
	public static SimpleMatrix getEigenVectors(final SimpleEVD<SimpleMatrix> decomposition,final List<Double> values)
	{
		final int nbRows = decomposition.getNumberOfEigenvalues();
		final int nbCols   = values.size();

		final SimpleMatrix matrix = new SimpleMatrix(nbRows, nbCols);

		/** get unsorted eigenvalues **/
		final List <Double> unsortedEigenValues = new ArrayList <Double>();
		for(int i = 0 ; i < nbRows ; i++)
		{
			unsortedEigenValues.add(decomposition.getEigenvalue(i).getReal());
		}


		for(int j = 0 ; j < nbCols ; j++)
		{
			int indexOfValue = unsortedEigenValues.indexOf(values.get(j));
			final SimpleMatrix eigenvector = decomposition.getEigenVector(indexOfValue);
			//Complex eigen value -> 0.0
			if(eigenvector.getMatrix() == null){
				for(int i = 0 ; i < nbRows ; i++){
					matrix.set(i, j, 0.0);
				}
			}else{
				//Real eigenvalue -> eigenvector
				for(int i = 0 ; i < nbRows ; i++){
					matrix.set(i, j, eigenvector.get(i));
				}
			}
		}
		return matrix;
	}

	/**
	 * Get the diagonal matrix from the eigen values list
	 * @param eigenValues The eigen values list
	 * @return The eigenMatrix Diagonal matrix of the eigen values
	 */
	public static SimpleMatrix getEigenMatrix(List<Double> eigenValues)
	{
		int size = eigenValues.size();
		final SimpleMatrix eigenMatrix = new SimpleMatrix(size,size);
		for(int i = 0 ; i < size ; i++)
		{
			for(int j = 0 ; j < size ; j++)
			{
				if(i == j)
				{
					eigenMatrix.set(i,j,Math.sqrt(eigenValues.get(i)));
				}
				else
				{
					eigenMatrix.set(i,j,0);
				}
			}
		}

		return eigenMatrix;
	}

	/**
	 * Get the points's coordinate in a n-dimension space.
	 * @param eigenVectorMatrix Matrix of the eigen vectors
	 * @param eigenMatrix Diagonal matrix of the eigen values
	 * @return The matrix of the coordinates
	 */
	private static SimpleMatrix getCoordinate(SimpleMatrix eigenVectorMatrix, SimpleMatrix eigenMatrix)
	{
		return eigenVectorMatrix.mult(eigenMatrix);
	}

	/**
	 * Get the points's coordinate in a n-dimension space.
	 * @param distanceMatrix The distance matrix
	 * @param dimension Number of dimension of the space
	 * @return A coordinate matrix
	 * @throws Exception 
	 */
	public static MDSResult doMDS(Instances instances,final MDSDistancesEnum distEnum,final int dimension,int maxInstances,final boolean ignoreClassInDistance,final boolean normalize) throws Exception
	{
		//Ignore too small values of maxInstances (or negatives)
		if(maxInstances<2){ maxInstances = 2;}
		final CollapsedInstances distanceMDS = distanceBetweenInstances(instances,distEnum,maxInstances,ignoreClassInDistance);
		return new MDSResult(distanceMDS,doMDSWithMatrixV1(distanceMDS.getDistanceMatrix(),normalize,dimension),normalize);
	}

	/**
	 * Should be call only if the result was not normalized
	 * @param res
	 * @return
	 */
	public static double getKruskalStressFromMDSResult(MDSResult res)
	{
		if(!res.isNormalized()){
			return ProjectionStressUtil.getKruskalStressFromProjection(res.getCInstances().getDistanceMatrix(),res.getCoordinates());
		}
		else{
			throw new IllegalStateException("Can't run kruskal stress on normalized output");
		}
	}
	
	public static MDSResult doMDSV1(Instances ds,SimpleMatrix M) throws Exception{
		//Not collapsed for now
		final CollapsedInstances cds = new CollapsedInstances(ds,null,M,false);
		return new MDSResult(cds,doMDSWithMatrixV1(M,false,2),false);
	}
//
//	private static MDSResult doMDSV2(Instances ds,SimpleMatrix M) throws Exception{
//		//Not collapsed for now
//		final CollapsedInstances cds = new CollapsedInstances(ds,null,M,false);
//		return new MDSResult(cds,doMDSWithMatrixV2(M, 2),false);
//	}
//
//	private static MDSResult doMDSV3(Instances ds,SimpleMatrix M) throws Exception{
//		//Not collapsed for now
//		final CollapsedInstances cds = new CollapsedInstances(ds,null,M,false);
//		final SimpleMatrix D = cds.getDistanceMatrix();
//		return new MDSResult(cds,doMDSWithBMatrix(buildKMatrix(D,2),false,2),false);
//	}

	
	private static SimpleMatrix doMDSWithMatrixV1(SimpleMatrix M,boolean normalize,final int dimension) throws Exception
	{
		SimpleMatrix matrix=getBMatrix(M);
		return doMDSWithBMatrix(matrix,normalize,dimension);
	}

//	private static SimpleMatrix doMDSWithMatrixV2(SimpleMatrix M,final int dimension) throws Exception
//	{
//		SimpleMatrix matrix=getBMatrix(M);
//		return doFastMDSWithBMatrix(matrix, dimension);
//	}

	private static SimpleMatrix doMDSWithBMatrix(SimpleMatrix matrix,final boolean normalize,final int dimension) throws Exception
	{

		@SuppressWarnings("unchecked")
		final SimpleEVD<SimpleMatrix> decomposition =  matrix.eig();

		// Get the eigen values
		final List <Double> list = getEigenValues(decomposition,dimension);

		// Get the coordinates
		if(list != null)
		{
			matrix = getCoordinate(getEigenVectors(decomposition,list), getEigenMatrix(list));
			if(normalize){
				return MatrixUtil.centerReduceMatrix(matrix);
			}else{
				return matrix;
			}
		}
		// Not enough eigen values, then return a default matrix, filled with 0.0
		else
		{
			throw new Exception("Not enough eigenvalues!");
		}
	}

//	@SuppressWarnings("unchecked")
//	private static SimpleMatrix doFastMDSWithBMatrix(SimpleMatrix B,final int dimension) throws Exception
//	{
//		int N = B.numRows();
//		SimpleMatrix Vk = new SimpleMatrix(N,dimension);
//		for(int j = 0 ; j < dimension ; j++){
//			Vk.set(j,j,1);
//		}
//		SimpleMatrix PVk = Vk;
//		final SimpleMatrix Lk = new SimpleMatrix(dimension,dimension);
//		int iter = 0;
//		double EPSILON = 0.001d;
//		double error = EPSILON+1;
//		while(iter++ < 10){
//			final SimpleMatrix Z = B.mult(Vk);
//			final QRDecomposition<DenseMatrix64F> qr = new QRDecompositionBlock64();
//			qr.decompose(Z.getMatrix());
//			final DenseMatrix64F QV = qr.getQ(null,true);
//			final SimpleMatrix Q = new SimpleMatrix(QV);
//			final SimpleMatrix Zstar = Q.transpose().mult(B).mult(Q);
//			final SimpleEVD<SimpleMatrix> evd = Zstar.eig();
//			// Get the coordinates
//			final int noe = evd.getNumberOfEigenvalues();
//			if(noe != 0)
//			{
//				final SimpleMatrix F = new SimpleMatrix(dimension,dimension);
//				for(int i = 0 ; i < dimension ; i++){
//					F.setColumn(i,0,evd.getEigenVector(i).getMatrix().getData());
//					Lk.set(i,i,evd.getEigenvalue(i).real);
//				}
//				Vk = Q.mult(F);
//
//				//Update previous coordinates slighly
//			}else{
//				final SimpleMatrix defaultMatrix = new SimpleMatrix(B.numRows(),dimension);
//				final int size = defaultMatrix.numRows();
//				for(int i = 0 ; i < size ; i++)
//				{
//					for(int j = 0 ; j < dimension ; j++)
//					{
//						defaultMatrix.set(i, j, 0.0);
//					}
//				}
//				return defaultMatrix;
//			}
//			SimpleMatrix errorMatrix = SimpleMatrix.identity(N).minus(Vk.mult(Vk.transpose())).mult(PVk);
//			if(PVk!=Vk){
//				error = errorMatrix.normF();
//			}
//			System.out.println("Error = " + error);
//			PVk = Vk;
//		}
//
//		return Vk.mult(Lk);
//	}


	public static void main(String[] args) throws Exception 
	{
		final Instances ds = WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(
		//new File("./samples/csv/uci/zoo.csv"));
		new File("./samples/csv/test50bis.csv"));
		//new File("./samples/arff/UCI/cmc.arff"));
		//new File("./samples/csv/direct-marketing-bank-reduced.csv"));
		//new File("./samples/csv/bank.csv"));
		//new File("./samples/csv/preswissroll.csv"));
		//new File("./samples/csv/preswissroll-mod4.csv"));
		
		ds.setClassIndex(-1);
		
		final int N = ds.size();
		final int M = ds.numAttributes();
		SimpleMatrix dist = new SimpleMatrix(N,N);
		
		for(int i = 0 ; i < N ; i++){
			for(int j =i+1 ; j < N ; j++){
				Instance xi = ds.instance(i);
				Instance xj = ds.instance(j);
				double d = 0, s=0, a=0,b=0;
				for(int k=1 ; k < M ; k++){
					s+=xi.value(k)*xj.value(k);
					a+=xi.value(k)*xi.value(k);
					b+=xi.value(k)*xi.value(k);
				}
				d = 1-s/(Math.sqrt(a)*Math.sqrt(b));
				dist.set(i,j,d);
				dist.set(j,i,d);
			}
		}
		
		final MDSResult res=ClassicMDS.doMDSV1(ds,dist);
		
		
		

		JXPanel p2 = MDSViewBuilder.buildMDSViewFromDataSet(ds,	res,5000,null);
		p2.setPreferredSize(new Dimension(800,600));

		final JXFrame f = new JXFrame();
		f.setPreferredSize(new Dimension(1024,768));
		final Container c = f.getContentPane();
		c.add(p2);
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JXFrame.EXIT_ON_CLOSE);
		
		System.out.println("Kruskal stress : ="+getKruskalStressFromMDSResult(res));

	}

}
