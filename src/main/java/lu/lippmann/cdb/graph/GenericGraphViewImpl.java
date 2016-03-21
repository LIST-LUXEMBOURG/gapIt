/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.graph;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

import javax.swing.*;

import lu.lippmann.cdb.common.guice.util.AutoBind;
import lu.lippmann.cdb.event.*;
import lu.lippmann.cdb.graph.mouse.CadralGraphMouse;
import lu.lippmann.cdb.graph.renderer.CShape;
import lu.lippmann.cdb.models.GenericCGraph;
import lu.lippmann.cdb.models.history.GraphWithOperations;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;
import org.bushe.swing.event.annotation.EventSubscriber;
import org.jdesktop.swingx.JXLabel;

import com.google.inject.Inject;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.picking.*;
import edu.uci.ics.jung.visualization.renderers.*;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;


/**
 * GraphViewImpl.
 * 
 * @author the ACORA team / WP1
 */
@AutoBind
public class GenericGraphViewImpl<V,E> extends JLayeredPane implements GenericGraphView<V,E> {

	//
	// Static fields
	//

	/** Serial version UID. */
	private static final long serialVersionUID=817561106L;


	//
	// Instance fields
	//

	private final VisualizationViewer<V,E> vv;

	private ViewMode viewMode;

	//
	// Constructors
	//

	/**
	 * Constructor.
	 */
	@Inject
	public GenericGraphViewImpl() 
	{
		/** initialize layout **/
		final FRLayout<V,E> layout = new FRLayout<V,E>(new DirectedSparseGraph<V, E>());
		//final FRLayout<V,E> layout = new FRLayout<V,E>(new GraphWithOperations());
		layout.setMaxIterations(500);

		final VisualizationModel<V,E> vm = new DefaultVisualizationModel<V, E>(layout);

		this.vv = new VisualizationViewer<V, E>(vm);

		this.vv.setPickSupport(new ShapePickSupport<V, E>(vv,10));

		this.vv.setGraphMouse(new DefaultModalGraphMouse<V,E>());

		/** Default mode */
		setViewMode(ViewMode.Add);

		/** Gimme the time to show the rules **/
		ToolTipManager.sharedInstance().setDismissDelay(50000);

		if(vv.getGraphMouse() instanceof CadralGraphMouse && vv.getGraphLayout().getGraph() instanceof GraphWithOperations){
			//((CadralGraphMouse)vv.getGraphMouse()).setClickedGraph((GraphWithOperations)layout.getGraph());
			((CadralGraphMouse)vv.getGraphMouse()).setClickedGraph((GraphWithOperations)vv.getGraphLayout().getGraph());
		}
	}


	//
	// Instance methods
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component asComponent() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEnabled(boolean enabled) { }

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override	
	public final void setViewMode(final ViewMode mode) 
	{
		this.viewMode = mode;

		if (mode == ViewMode.Add) 
		{
			((DefaultModalGraphMouse<V,E>)vv.getGraphMouse()).setMode(Mode.EDITING);
		} 
		else if (mode == ViewMode.Edit) 
		{
			((DefaultModalGraphMouse<V,E>)vv.getGraphMouse()).setMode(Mode.PICKING);
		}
		else if (mode == ViewMode.Tag)
		{
			((DefaultModalGraphMouse<V,E>)vv.getGraphMouse()).setMode(Mode.ANNOTATING);
		}
		else throw new IllegalStateException("viewmode not managed -> "+viewMode);					
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ViewMode getViewMode()
	{
		return viewMode;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addMetaInfo(final String title,final String content)
	{
		final JXLabel metaInfoLabel=new JXLabel("["+title+"]");
		metaInfoLabel.setToolTipText(content);
		addMetaInfoComponent(metaInfoLabel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addMetaInfoComponent(final Component c)
	{
		c.setBackground(this.getBackground()); //same bg
		validate();
		repaint();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void init() 
	{
		this.setBackground(new Color(235,240,245));

		this.removeAll();

		//normal init
		final VertexLabelAsShapeRenderer vlasr = new VertexLabelAsShapeRenderer<V, E>(vv.getRenderContext());

		// -----------------------------
		// customize the render context
		// -----------------------------
		final Transformer<V,String> vertexLabelTransformer =  new Transformer<V,String>(){
			@Override
			public String transform(final V node) 
			{
				return node.toString();
			}
		};

		vv.getRenderContext().setVertexLabelTransformer(vertexLabelTransformer);

		final Transformer<V,String> vertexTooltipTransformer = new Transformer<V,String>() {
			@Override
			public String transform(final V node) 
			{
				return node.toString();
			}
		};
		vv.setVertexToolTipTransformer(vertexTooltipTransformer);

		final Transformer<E,String> edgeTooltipTransformer = new Transformer<E,String>(){
			@Override
			public String transform(final E edge) 
			{
				return edge.toString();
			}
		};
		vv.setEdgeToolTipTransformer(edgeTooltipTransformer);

		vv.getRenderContext().setVertexShapeTransformer(vlasr);

		//VERTEX LABEL RENDERER
		vv.getRenderer().setVertexLabelRenderer(vlasr);

		//FIXME : magic number
		vv.getRenderContext().setLabelOffset(16);

		// custom edges
		final Transformer<E,String> edgeLabelTransformer = new Transformer<E, String>(){
			@Override
			public String transform(final E e) 
			{
				return e.toString();
			}
		};
		vv.getRenderContext().setEdgeLabelTransformer(edgeLabelTransformer);

		final Font myVertexFont = new Font("Helvetica", 0, 8);
		final Font myEdgeFont   = new Font("Helvetica", 0, 8);

		//FAST EDGE LABEL RENDERER BUT NOT GOOD ENOUGH
		//sun.font.FontManager.getFont2D(myFont);

		vv.getRenderContext().setEdgeFontTransformer(new ConstantTransformer(myEdgeFont));
		vv.getRenderContext().setVertexFontTransformer(new ConstantTransformer(myVertexFont));
		
		vv.getRenderContext().setEdgeLabelClosenessTransformer(new ConstantTransformer(0.5));
		vv.getRenderContext().setEdgeDrawPaintTransformer(getDefaultEdgeDrawPaintTransformer());
		vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.BLUE,false));
		vv.getRenderContext().setArrowDrawPaintTransformer(new PickableEdgePaintTransformer<E>(vv.getPickedEdgeState(), Color.BLACK, Color.BLUE));
		vv.getRenderContext().setArrowFillPaintTransformer(new PickableEdgePaintTransformer<E>(vv.getPickedEdgeState(), Color.BLACK, Color.BLUE));

		setLayout(new BorderLayout());

		add(vv,BorderLayout.CENTER);

		validate();
		repaint();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Graph<V,E> getGraph()
	{
		return this.vv.getGraphLayout().getGraph();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCGraph(final GenericCGraph<V,E> cgraph) 
	{				
		this.vv.setVisible(cgraph!=null);
		if (cgraph==null) return;
		this.vv.setGraphLayout(cgraph.getInternalLayout());
		this.vv.updateUI();		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void highlightPath(final List<E> path) 
	{
		vv.getRenderContext().setEdgeDrawPaintTransformer(
				new Transformer<E, Paint>() {
					@Override
					public Paint transform(E input) {
						if(path.contains(input)){
							return Color.RED;
						}else{
							return Color.BLACK;
						}
					}
				});
		vv.updateUI();		
	}


	@Override
	public void updateVertexShapeTransformer(final Map<V,Map<Object,Integer>> map)
	{
		
	}

	@Override
	public void updateEdgeShapeRenderer(final Map<E,Float> map)
	{
		
	}


	@Override
	public void resetVertexAndEdgeShape()
	{

	}

	/**
	 * 
	 * @return
	 */
	private Transformer<E,Paint> getDefaultEdgeDrawPaintTransformer(){
		return new PickableEdgePaintTransformer<E>(vv.getPickedEdgeState(), Color.BLACK, Color.BLUE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectAll(final boolean doSelect) 
	{
		final Graph<V, E> graph = vv.getGraphLayout().getGraph();

		final PickedState<V> pickedState =  vv.getPickedVertexState();

		for (final V node:graph.getVertices()) {
			pickedState.pick(node, doSelect);
		}

		final PickedState<E> pickedStateEdge =  vv.getPickedEdgeState();

		for(final E edge:graph.getEdges()) {
			pickedStateEdge.pick(edge, doSelect);
		}
		vv.updateUI();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reorganize() 
	{
		reorganize(true);
	}


	/**
	 * Reorganize selected graph using Minimum Spanning Tree algorithm
	 */
	private void reorganize(final boolean state) 
	{
	
	}



	/**	 
	 * {@inheritDoc}
	 */
	@Override
	public void deleteSelected() 
	{
		
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public VisualizationViewer<V, E> getVisualisationViewer() {
		return vv;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBackground(final Color color) 
	{
		this.vv.setBackground(color);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getUniqueId() {
		return this.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fitGraphToSubPanel(double panelWidth,double panelHeigth,final double ratioPanel) 
	{
		int w = 10;
		int h = 10;
		
		vv.setPreferredSize(new Dimension((int)panelWidth,(int)panelHeigth));
		vv.setMinimumSize(new Dimension((int)panelWidth,(int)panelHeigth));
		vv.setMaximumSize(new Dimension((int)panelWidth,(int)panelHeigth));

		
		double minX=Integer.MAX_VALUE,maxX=Integer.MIN_VALUE,minY=Integer.MAX_VALUE,maxY=Integer.MIN_VALUE;

		final MutableTransformer viewTransformer   = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW);
		//final MutableTransformer layoutTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);

		//Reset graph layout (zoom level & translation)
		resetLayout();

		final Graph<V,E> graph = vv.getModel().getGraphLayout().getGraph();

		if(graph.getVertices().size()==0){
			return;
		} else if(graph.getVertices().size()==1){
			final V n       = graph.getVertices().iterator().next();
			final Point2D point = vv.getModel().getGraphLayout().transform(n);     //center of the node
			minX=point.getX();
			maxX=point.getX();
			minY=point.getY();
			maxY=point.getY();
		}else{
			for(final V n : graph.getVertices()){
				final Point2D point = vv.getModel().getGraphLayout().transform(n); //center of the node
				
				double nodeMinX = point.getX() - w/2;
				double nodeMaxX = point.getX() + w/2;
				double nodeMinY = point.getY() - h/2;
				double nodeMaxY = point.getY() + h/2;
				
				if(nodeMinX < minX) {minX = nodeMinX;}
				if(nodeMaxX > maxX) {maxX = nodeMaxX;}
				if(nodeMinY < minY) {minY = nodeMinY;}
				if(nodeMaxY > maxY) {maxY = nodeMaxY;}
			}
		}
		
		final int graphWidth  = (int)(maxX-minX); 
		final int graphHeigth = (int)(maxY-minY);

		//System.out.println("Graph heigth : " + graphHeigth);
		//System.out.println("Graph graphWidth : " + graphWidth);

		final double scale1   =  ratioPanel/100.0d * panelWidth / graphWidth;
		final double scale2   =  panelHeigth / graphHeigth;

		final double ratio = Math.min(Math.min(scale1,scale2),1.0f); //offset margin
		//final double ratio = Math.min(scale1,scale2); //offset margin

		//System.out.println("Ratio used : " + ratio);

		viewTransformer.scale(ratio,ratio,new Point2D.Double());
		viewTransformer.translate(((panelWidth*ratioPanel/100.0f-graphWidth*ratio)/2.0f)/ratio-minX,((panelHeigth-graphHeigth*ratio)/2.0f)/ratio-minY);
	}

		/**
	 * {@inheritDoc}
	 */
	@Override
	public void resetLayout() {
		//Revert previous transformation
		vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
	}


	/**
	 * 
	 * @param evt
	 */
	@EventSubscriber(eventClass=ConnectionTypeChangedEvent.class)
	public void setViewConnectionType(ConnectionTypeChangedEvent evt) 
	{
		
	}

	/**
	 * Fire when somebody ask to change the layout of the graph
	 * @param lc
	 */
	@EventSubscriber(eventClass = LayoutChangedEvent.class)
	public void layoutChanged(final LayoutChangedEvent lc) {
		
	}

	//
	// Static methods
	//

	@Override
	public void setShape(final CShape cs){
		
	}

	@Override
	public void setColor(final Color co){
		
	}


	@Override
	public void autoFit() {
		final Rectangle visible = vv.getVisibleRect();
		//Add margin
		int panelWidth  = (int)visible.getWidth() - 10 ; 
		int panelHeight = (int)visible.getHeight()- 10 ;
		fitGraphToSubPanel(panelWidth, panelHeight, 100.0d);
	}


	@Override
	public void clearMetaInfo() {
		
	}


}
