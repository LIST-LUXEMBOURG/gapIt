/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.graph;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.*;
import java.util.List;

import javax.swing.*;

import lu.lippmann.cdb.about.AboutPanel;
import lu.lippmann.cdb.command.*;
import lu.lippmann.cdb.common.LookAndFeelUtil;
import lu.lippmann.cdb.common.gui.LogoHelper;
import lu.lippmann.cdb.common.guice.util.AutoBind;
import lu.lippmann.cdb.common.mvp.Listener;
import lu.lippmann.cdb.event.*;
import lu.lippmann.cdb.event.ConnectionTypeChangedEvent.GraphConnectionStatus;
import lu.lippmann.cdb.graph.GenericGraphView.ViewMode;
import lu.lippmann.cdb.graph.mouse.CadralGraphMouse;
import lu.lippmann.cdb.graph.renderer.*;
import lu.lippmann.cdb.models.*;
import lu.lippmann.cdb.models.history.*;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;
import org.bushe.swing.event.annotation.EventSubscriber;
import org.jdesktop.swingx.*;

import com.google.inject.Inject;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.picking.*;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;


/**
 * GraphViewImpl.
 * 
 * @author the ACORA team / WP1
 */
@AutoBind
public class GraphViewImpl extends JLayeredPane implements GraphView {

	//
	// Static fields
	//

	/** Serial version UID. */
	private static final long serialVersionUID=817561106L;


	//
	// Instance fields
	//

	private final EventPublisher eventPublisher;

	private final VisualizationViewer<CNode,CEdge> vv;
	private final SatelliteVisualizationViewer<CNode,CEdge> vv2; 


	private final CadralGraphMouse cadralGraphMouse;

	private final AboutPanel aboutPanel;

	private final JXLabel sharedLabel=new JXLabel();

	private final JXPanel metaInfosPanel=new JXPanel();

	private ViewMode viewMode;

	//private LensSupport lensSupport;

	private boolean isLayoutChanging = false;

	//
	// Constructors
	//

	/**
	 * Constructor.
	 */
	@Inject
	public GraphViewImpl(final EventPublisher eventPublisher,final CommandDispatcher commandDispatcher) 
	{
		this.eventPublisher=eventPublisher;

		eventPublisher.markAsEventListener(this);

		/** initialize layout **/
		//final FRLayout<CNode,CEdge> layout = new FRLayout<CNode,CEdge>(new DirectedSparseGraph<CNode, CEdge>());
		final FRLayout<CNode,CEdge> layout = new FRLayout<CNode,CEdge>(new GraphWithOperations());
		layout.setMaxIterations(500);

		final VisualizationModel<CNode,CEdge> vm = new DefaultVisualizationModel<CNode, CEdge>(layout);

		this.vv = new VisualizationViewer<CNode, CEdge>(vm);

		this.vv.setPickSupport(new ShapePickSupport<CNode, CEdge>(vv,10));

		this.cadralGraphMouse = new CadralGraphMouse(CShape.RECTANGLE,Color.WHITE,commandDispatcher);

		this.vv.setGraphMouse(this.cadralGraphMouse);

		/*
		LensSupport hyperbolicLayoutSupport = 
				 new LayoutLensSupport<CNode,CEdge>(vv, new HyperbolicTransformer(vv, 
		            		vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)),
		                    new ModalLensGraphMouse());

		this.lensSupport  = 
				 new LayoutLensSupport<CNode,CEdge>(vv, new MagnifyTransformer(vv, 
		            		vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)),
		                    new ModalLensGraphMouse(new LensMagnificationGraphMousePlugin(1.f, 6.f, .2f)));

		lensSupport.getLensTransformer().setLensShape(hyperbolicLayoutSupport.getLensTransformer().getLensShape());

        this.cadralGraphMouse.addItemListener(this.lensSupport.getGraphMouse().getModeListener());
		 */

		/** Satelite view config **/
		final Dimension preferredSize2 = new Dimension(300, 300);
		this.vv2 = new SatelliteVisualizationViewer<CNode,CEdge>(vv, preferredSize2);
		vv2.scaleToLayout(new CrossoverScalingControl());

		/** about panel */
		this.aboutPanel=new AboutPanel(true);

		/** Default mode */
		setViewMode(ViewMode.Add);

		/** Gimme the time to show the rules **/
		ToolTipManager.sharedInstance().setDismissDelay(50000);

		/** Local **/
		this.sharedLabel.setText(GraphConnectionStatus.LOCAL.name());

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
	@Override	
	public final void setViewMode(final ViewMode mode) 
	{
		this.viewMode = mode;
		//lensSupport.activate(false);
		if (mode == ViewMode.Add) 
		{
			this.cadralGraphMouse.setMode(Mode.EDITING);
		} 
		else if (mode == ViewMode.Edit) 
		{
			this.cadralGraphMouse.setMode(Mode.PICKING);
		}
		else if (mode == ViewMode.Tag)
		{
			this.cadralGraphMouse.setMode(Mode.ANNOTATING);
		}/*else if(mode == ViewMode.Lens){
	        lensSupport.activate(true);
	     }*/
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


	private String buildTooltipFromTags(final String name,final List<CTag> tags) 
	{
		if (tags.isEmpty()) return null;
		final StringBuilder sb=new StringBuilder("<html>"+name+"<br/><table>");
		final Calendar cal=Calendar.getInstance();
		for (final CTag t:tags)
		{
			cal.setTimeInMillis(t.getTimestamp());
			final URL u = ClassLoader.getSystemClassLoader().getResource(t.getValue().getNote().getIconPath());
			sb.append("<tr><td>")
			.append("<img width=16 height=16 src=\"").append(u).append("\"/>").append("</td><td>")
			.append("<i>").append(t.getUser().getName()).append("</i></td><td>")
			.append(cal.getTime()).append("</td><td>")
			.append("<b>'").append(t.getValue().getDesc()).append("'</b>").append("</td></tr>");
		}
		sb.append("</html>");
		return sb.toString();
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
		metaInfosPanel.add(c);
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
		//this.setBackground(Color.WHITE);

		this.removeAll();

		//normal init
		final CadralShapeRenderer vlasr = new CadralShapeRenderer(null,vv.getRenderContext());

		// -----------------------------
		// customize the render context
		// -----------------------------
		final Transformer<CNode,String> vertexLabelTransformer =  new Transformer<CNode,String>(){
			@Override
			public String transform(final CNode node) 
			{
				final String tagString=(!node.getTags().isEmpty())?" [TAGGED!]":"";
				final FontMetrics fm = vv.getFontMetrics(vv.getFont());
				final int textWidth = fm.stringWidth(node.getName());
				final int tagWidth  = fm.stringWidth(tagString);
				int nodeWidth = (int)ShapeFactory.createShape(node.getShape()).getBounds().getWidth();
				final boolean hasTags = !node.getTags().isEmpty();
				final int realTextWidth       = textWidth + (hasTags?tagWidth:0);
				final String modifiedNodeName = node.getName()+(hasTags?tagString:"");
				if(realTextWidth > nodeWidth - 10){
					int maxSubStringIndex = -1;
					for(int subStringIndex = node.getName().length() ; subStringIndex >= 1 ; subStringIndex--){
						String newString = node.getName().substring(0,subStringIndex)+"..."+tagString;
						if(fm.stringWidth(newString) < nodeWidth - 10){
							maxSubStringIndex = subStringIndex;
							break;
						}
					}
					return node.getName().substring(0,maxSubStringIndex)+"..."+tagString;
				}else{
					return modifiedNodeName;
				}
			}
		};

		vv.getRenderContext().setVertexLabelTransformer(vertexLabelTransformer);
		vv2.getRenderContext().setVertexLabelTransformer(vertexLabelTransformer);

		final Transformer<CNode,String> vertexTooltipTransformer = new Transformer<CNode,String>() {
			@Override
			public String transform(final CNode input) 
			{
				if(!input.getTags().isEmpty()){
					return buildTooltipFromTags(input.getName(),input.getTags());
				}else{
					if(!input.getName().equals(vertexLabelTransformer.transform(input))){
						return input.getName();
					}else{
						return null;
					}
				}
			}
		};
		vv.setVertexToolTipTransformer(vertexTooltipTransformer);
		vv2.setVertexToolTipTransformer(vertexTooltipTransformer);

		final Transformer<CEdge,String> edgeTooltipTransformer = new Transformer<CEdge,String>(){
			@Override
			public String transform(final CEdge input) 
			{
				if(!input.getTags().isEmpty()){
					return buildTooltipFromTags(input.getName(),input.getTags());
				}else{
					return input.getName();
				}
			}
		};
		vv.setEdgeToolTipTransformer(edgeTooltipTransformer);
		vv2.setEdgeToolTipTransformer(edgeTooltipTransformer);

		vv.getRenderContext().setVertexFillPaintTransformer(new CadralVertexColorRenderer());
		vv2.getRenderContext().setVertexFillPaintTransformer(new CadralVertexColorRenderer());

		final CadralFontTransformer cadralFontTransformer = new CadralFontTransformer();
		vv.getRenderContext().setVertexFontTransformer(cadralFontTransformer);
		
		vv.getRenderContext().setVertexShapeTransformer(vlasr);
		vv2.getRenderContext().setVertexShapeTransformer(vlasr);

		//VERTEX LABEL RENDERER
		vv.getRenderer().setVertexLabelRenderer(vlasr);
		vv2.getRenderer().setVertexLabelRenderer(vlasr);

		//FIXME : magic number
		vv.getRenderContext().setLabelOffset(16);
		vv2.getRenderContext().setLabelOffset(16);

		// custom edges
		final Transformer<CEdge,String> edgeLabelTransformer = new Transformer<CEdge, String>(){
			@Override
			public String transform(final CEdge input) 
			{
				return input.getExpression()+(input.getTags().isEmpty()?"":" [TAGGED!]");
			}
		};
		vv.getRenderContext().setEdgeLabelTransformer(edgeLabelTransformer);
		vv2.getRenderContext().setEdgeLabelTransformer(edgeLabelTransformer);

		final Font myFont = new Font("Helvetica", 0, 12);

		//FAST EDGE LABEL RENDERER BUT NOT GOOD ENOUGH
		//sun.font.FontManager.getFont2D(myFont);	
				
		vv.getRenderContext().setEdgeFontTransformer(cadralFontTransformer);
		vv2.getRenderContext().setEdgeFontTransformer(new ConstantTransformer(myFont));

		vv.getRenderContext().setEdgeLabelClosenessTransformer(new ConstantTransformer(0.5));
		vv2.getRenderContext().setEdgeLabelClosenessTransformer(new ConstantTransformer(0.5));

		vv.getRenderContext().setEdgeDrawPaintTransformer(getDefaultEdgeDrawPaintTransformer());
		vv2.getRenderContext().setEdgeDrawPaintTransformer(getDefaultEdgeDrawPaintTransformer());

		vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.BLUE,false));
		vv2.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.BLUE,false));

		vv.getRenderContext().setArrowDrawPaintTransformer(new PickableEdgePaintTransformer<CEdge>(vv.getPickedEdgeState(), Color.BLACK, Color.BLUE));
		vv.getRenderContext().setArrowFillPaintTransformer(new PickableEdgePaintTransformer<CEdge>(vv.getPickedEdgeState(), Color.BLACK, Color.BLUE));

		vv2.getRenderContext().setArrowDrawPaintTransformer(new PickableEdgePaintTransformer<CEdge>(vv.getPickedEdgeState(), Color.BLACK, Color.BLUE));
		vv2.getRenderContext().setArrowFillPaintTransformer(new PickableEdgePaintTransformer<CEdge>(vv.getPickedEdgeState(), Color.BLACK, Color.BLUE));

		
		//Manage Zoom
		vv.addMouseWheelListener(new MouseWheelListener() {
			
			private int zoom = 0;

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {

				if (e.getWheelRotation() < 0) {					
					zoom++;
				} else {					
					zoom--;
				}
				
				if (zoom > 0) {
					if (zoom < 9) {
						double scale = 1 + zoom/6.0;
						CadralShapeRenderer.setScale(scale);
						cadralFontTransformer.setScale(scale);	
					}									
				}
				else {
					CadralShapeRenderer.setScale(1);
					cadralFontTransformer.setScale(1);	
				}				
			}
		});
		
		
		final Container panel = new JPanel(new BorderLayout());
		final Container rightPanel = new JPanel(new GridLayout(2,1));
		panel.add(vv);
		final JPanel top = new JPanel();
		final JButton button = new JButton("Rescale");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				rescaleSatelliteView();
			}
		});
		top.add(button);
		rightPanel.add(top);
		rightPanel.add(vv2);
		panel.add(rightPanel, BorderLayout.EAST);


		setLayout(new BorderLayout());

		add(metaInfosPanel,BorderLayout.NORTH);

		if(!USE_EXPERIMENTAL_SATELLITE_VIEW){
			add(vv,BorderLayout.EAST);
		}else{
			add(panel,BorderLayout.EAST);
		}

		add(sharedLabel,BorderLayout.SOUTH);

		add(aboutPanel,BorderLayout.CENTER);

		validate();
		repaint();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Graph<CNode,CEdge> getGraph()
	{
		return this.vv.getGraphLayout().getGraph();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCGraph(final CGraph cgraph) 
	{				
		if(cgraph!=null){
			Component center = ((BorderLayout)getLayout()).getLayoutComponent(BorderLayout.CENTER);
			if(center == aboutPanel){
				Component east = ((BorderLayout)getLayout()).getLayoutComponent(BorderLayout.EAST);
				remove(aboutPanel);
				remove(east);
				add(east,BorderLayout.CENTER);
			}
		}
		
		this.vv.setVisible(cgraph!=null);
		this.metaInfosPanel.setVisible(cgraph!=null);
		this.sharedLabel.setVisible(cgraph!=null);		

		if (cgraph==null) return;

		this.vv.setGraphLayout(cgraph.getInternalLayout());
		this.vv.updateUI();		

		//Refresh the history view the first time 
		eventPublisher.publish(new GraphStructureChangedEvent());


		/** initialize listeners */
		final GraphWithOperations gwo = ((GraphWithOperations)cgraph.getInternalGraph());

		//node moved
		gwo.addNodeMovedListener(new Listener<CNodePosition>() {
			@Override
			public void onAction(CNodePosition parameter) {
				getVisualisationViewer().getGraphLayout().setLocation(parameter.getNode(),parameter.getPoint());
			}
		});
		//layout changed
		gwo.addLayoutChangedListener(new Listener<CLayoutTransition>() {
			@Override
			public void onAction(CLayoutTransition parameter) {
				eventPublisher.publish(new LayoutChangedEvent(getUniqueId(),parameter));
			}
		});
		//structure changed
		gwo.addStructureChangeListener(new Listener<Operation>() {
			@Override
			public void onAction(Operation parameter) {
				if(parameter == null){
					eventPublisher.publish(new GraphStructureChangedEvent());
				}else{
					eventPublisher.publish(new GraphStructureChangedEvent(parameter));
				}
			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void highlightPath(final List<CEdge> path) 
	{
		vv.getRenderContext().setEdgeDrawPaintTransformer(
				new Transformer<CEdge, Paint>() {
					@Override
					public Paint transform(CEdge input) {
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
	public void updateVertexShapeTransformer(final Map<CNode,Map<Object,Integer>> map)
	{
		final CadralShapeRenderer csr = new CadralShapeRenderer(map,vv.getRenderContext());
		vv.getRenderer().setVertexLabelRenderer(csr);
		vv.getRenderContext().setVertexShapeTransformer(csr);
		vv.updateUI();
	}

	@Override
	public void updateEdgeShapeRenderer(final Map<CEdge,Float> map)
	{
		vv.getRenderContext().setEdgeStrokeTransformer(new CadralEdgeStrokeTransformer(map));
		vv.getRenderContext().setEdgeDrawPaintTransformer(new CadralEdgeColorTransformer(map,Color.GRAY,Color.BLACK,20));
		vv.updateUI();
	}


	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void resetVertexAndEdgeShape()
	{
		//Reset edge
		vv.getRenderContext().setEdgeStrokeTransformer(new ConstantTransformer(new BasicStroke(1.0f)));
		vv.getRenderContext().setEdgeDrawPaintTransformer(getDefaultEdgeDrawPaintTransformer());

		//Reset vertex
		final CadralShapeRenderer csr = new CadralShapeRenderer(null,vv.getRenderContext());
		vv.getRenderer().setVertexLabelRenderer(csr);
		vv.getRenderContext().setVertexShapeTransformer(csr);

		//Update
		vv.updateUI();
	}

	/**
	 * 
	 * @return
	 */
	private Transformer<CEdge,Paint> getDefaultEdgeDrawPaintTransformer(){
		return new PickableEdgePaintTransformer<CEdge>(vv.getPickedEdgeState(), Color.BLACK, Color.BLUE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectAll(final boolean doSelect) 
	{
		final Graph<CNode, CEdge> graph = vv.getGraphLayout().getGraph();

		final PickedState<CNode> pickedState =  vv.getPickedVertexState();

		for (final CNode node:graph.getVertices()) {
			pickedState.pick(node, doSelect);
		}

		final PickedState<CEdge> pickedStateEdge =  vv.getPickedEdgeState();

		for(final CEdge edge:graph.getEdges()) {
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
		isLayoutChanging = true;
		final AggregateLayout<CNode,CEdge> clusteringLayout = new AggregateLayout<CNode,CEdge>(vv.getGraphLayout());

		if (state) {
			GraphUtil.reorganize(vv.getGraphLayout(),vv.getPickedVertexState().getPicked());
		} else {
			clusteringLayout.removeAll();
			vv.setGraphLayout(clusteringLayout);
		}
		vv.updateUI();
	}



	/**	 
	 * {@inheritDoc}
	 */
	@Override
	public void deleteSelected() 
	{
		final GraphWithOperations graph = (GraphWithOperations)vv.getGraphLayout().getGraph();
		final Set<CNode> pickedVertex = vv.getPickedVertexState().getPicked();

		boolean needsGroup = (pickedVertex.size() > 1);
		if(needsGroup){
			graph.startGroupOperation();
		}

		for (final CNode node : pickedVertex) {
			graph.removeVertex(node);
		}
		//important else node(s) are still selected but are not anymore in the graph !
		vv.getPickedVertexState().clear(); 

		final Set<CEdge> pickedEdge = vv.getPickedEdgeState().getPicked();

		for (final CEdge edge : pickedEdge) {
			//the edge could be removed by vertex removes !
			if(graph.containsEdge(edge)){
				graph.removeEdge(edge);
			}
		}

		if(needsGroup){
			graph.stopGroupOperation();
		}

		//important else edge(s) are still selected there but are not anymore in the graph!
		vv.getPickedEdgeState().clear();  

		vv.updateUI();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public VisualizationViewer<CNode, CEdge> getVisualisationViewer() {
		return vv;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBackground(final Color color) 
	{
		this.metaInfosPanel.setBackground(color);
		this.sharedLabel.setBackground(color);
		this.vv.setBackground(color);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void hideSharedLabel()
	{
		this.sharedLabel.setVisible(false);
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
		doFitGraphToSubPanel(panelWidth, panelHeigth, ratioPanel, new Point2D.Double(0, 0));
	}

	/**
	 * FIXME : faulty center method
	 * @param panelWidth
	 * @param panelHeigth
	 * @param ratioPanel
	 * @param center
	 */
	private void doFitGraphToSubPanel(double panelWidth,double panelHeigth,final double ratioPanel,Point2D center){

		while(isLayoutChanging){
			System.out.println("Waiting layout to finish !");
			try { Thread.sleep(100);} catch (InterruptedException e) {}
		}

		vv.setPreferredSize(new Dimension((int)panelWidth,(int)panelHeigth));
		vv.setMinimumSize(new Dimension((int)panelWidth,(int)panelHeigth));
		vv.setMaximumSize(new Dimension((int)panelWidth,(int)panelHeigth));

		
		double minX=Integer.MAX_VALUE,maxX=Integer.MIN_VALUE,minY=Integer.MAX_VALUE,maxY=Integer.MIN_VALUE;

		final MutableTransformer viewTransformer   = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW);
		//final MutableTransformer layoutTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);

		//Reset graph layout (zoom level & translation)
		resetLayout();

		final Graph<CNode,CEdge> graph = vv.getModel().getGraphLayout().getGraph();

		if(graph.getVertices().size()==0){
			return;
		} else if(graph.getVertices().size()==1){
			final CNode n       = graph.getVertices().iterator().next();
			final Point2D point = vv.getModel().getGraphLayout().transform(n);     //center of the node
			minX=point.getX();
			maxX=point.getX();
			minY=point.getY();
			maxY=point.getY();
		}else{
			for(final CNode n : graph.getVertices()){
				final Point2D point = vv.getModel().getGraphLayout().transform(n); //center of the node
				
				double nodeMinX = point.getX() - (n.getShape().getWidth()/2);
				double nodeMaxX = point.getX() + (n.getShape().getWidth()/2);
				double nodeMinY = point.getY() - (n.getShape().getHeight()/2);
				double nodeMaxY = point.getY() + (n.getShape().getHeight()/2);
				
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

		//System.out.println("Ratio used : " + ratio);

		viewTransformer.scale(ratio,ratio,center);
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
		this.sharedLabel.setText(evt.getStatus());
	}

	/**
	 * Fire when somebody ask to change the layout of the graph
	 * @param lc
	 */
	@EventSubscriber(eventClass = LayoutChangedEvent.class)
	public void layoutChanged(final LayoutChangedEvent lc) {
		//System.out.println("[OK]Layout asked for : "+  lc.getIdSource() + " on " + getUniqueId());
		if(lc.getIdSource()==getUniqueId()){
			//System.out.println("Changing " + getUniqueId() + "'s layout !");
			final CLayoutTransition transition = lc.getTransition();
			/**
			 * new layout
			 */
			final StaticLayout<CNode,CEdge> newLayout = new StaticLayout<CNode,CEdge>(vv.getGraphLayout().getGraph());
			for(CNode node : transition.getNewLayout().keySet()){
				final CPoint tmp = transition.getNewLayout().get(node);
				newLayout.setLocation(node,new Point2D.Double(tmp.getX(),tmp.getY()));
				//Update node position
				((GraphWithOperations)vv.getGraphLayout().getGraph()).updateNodeAddedOperation(node,tmp);
			}

			//
			vv.setGraphLayout(newLayout);
			//System.out.println("Changing " + getUniqueId() + "'s layout DONE!");
		}
		isLayoutChanging = false;
	}


	//@EventSubscriber(eventClass=GraphRepaintedEvent.class)
	public void rescaleSatelliteView()
	{
		//MutableTransformer masterViewTransformer   =  vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW);
		//MutableTransformer masterLayoutTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
		MutableTransformer slaveViewTransformer    = vv2.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW);
		MutableTransformer slaveLayoutTransformer  = vv2.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);

		final Layout<CNode,CEdge> layout = vv.getGraphLayout(); 
		double minX=Integer.MAX_VALUE,maxX=Integer.MIN_VALUE,minY=Integer.MAX_VALUE,maxY=Integer.MIN_VALUE;
		final Graph<CNode,CEdge> graph = layout.getGraph();

		double w = 0,h=0;
		for(final CNode n : graph.getVertices()){
			final Point2D point = layout.transform(n); //center of the node
			if(point.getX() < minX) {minX = point.getX();w = ShapeFactory.createShape(n.getShape()).getBounds().getWidth()/2;}
			if(point.getX() > maxX) {maxX = point.getX();}
			if(point.getY() < minY) {minY = point.getY();h = ShapeFactory.createShape(n.getShape()).getBounds().getHeight()/2;}
			if(point.getY() > maxY) {maxY = point.getY();}
		}

		final double graphWidth  = (maxX-minX)+2*w;///slaveViewTransformer.getScale(); 
		final double graphHeight = (maxY-minY)+2*h;///slaveViewTransformer.getScale();
		final double slaveWidth  = vv2.getBounds().getWidth()/slaveViewTransformer.getScale();
		final double slaveHeight = vv2.getBounds().getHeight()/slaveViewTransformer.getScale();

		final double scale1 = (slaveWidth  / graphWidth) ;
		final double scale2 = (slaveHeight / graphHeight) ;
		final float scale  = (float) (Math.min(scale1,scale2)); //to avoid round error 
		if(scale < slaveLayoutTransformer.getScale()){
			slaveLayoutTransformer.setToIdentity();
			final Point2D graphCenter = GraphUtil.getCenter(new HashSet<CNode>(vv.getGraphLayout().getGraph().getVertices()),
					vv.getModel().getGraphLayout());
			//System.out.println("Scaling from center : " + graphCenter +"->" + slaveViewTransformer.transform(graphCenter));
			slaveLayoutTransformer.scale(scale , scale, slaveViewTransformer.transform(graphCenter));
			//System.out.println("Graph center : " + graphCenter);
			//slaveLayoutTransformer.setTranslate(slaveWidth/2-graphCenter.getX()/2,slaveHeight/2-graphCenter.getY()/2);
		}else{
			final double tx = slaveLayoutTransformer.getTranslateX();
			final double ty = slaveLayoutTransformer.getTranslateY();
			if(minX < tx){
				slaveLayoutTransformer.translate(-minX-tx+w,0);
			}else if(slaveWidth < maxX){
				slaveLayoutTransformer.translate(slaveWidth-maxX-tx-w,0);
			}
			if(minY < ty){
				slaveLayoutTransformer.translate(0,-minY-ty+h);
			}else if(slaveHeight < maxY){
				slaveLayoutTransformer.translate(0,slaveHeight-maxY-ty-h);
			}
		}


	}


	//
	// Static methods
	//

	public static final boolean USE_EXPERIMENTAL_SATELLITE_VIEW = false;


	/**
	 * 
	 * @param args
	 * @throws UnsupportedLookAndFeelException
	 */
	public static final void main(String[] args) throws UnsupportedLookAndFeelException {

		LookAndFeelUtil.init();

		final JXFrame f=new JXFrame();
		f.setDefaultCloseOperation(JXFrame.EXIT_ON_CLOSE);

		final GraphView view = new GraphViewImpl(new EventPublisherBushImpl(),new CommandDispatcherBushImpl());

		view.setViewMode(ViewMode.Add);
		view.init();

		final int panelWidth=800;
		final int panelHeigth=800;

		final VisualizationViewer<CNode,CEdge> vv = view.getVisualisationViewer();

		vv.setPreferredSize(new Dimension(panelWidth,panelHeigth));

		f.setSize(new Dimension(panelWidth,panelHeigth));
		LogoHelper.setLogo(f);
		f.setTitle("GraphView test");
		//f.setLayout(new BorderLayout());

		final GraphWithOperations gwo = new GraphWithOperations();
		final CGraph cg = new CGraph();
		final FRLayout<CNode,CEdge> layout = new FRLayout<CNode,CEdge>(gwo);
		cg.setInternalLayout(layout);
		view.setCGraph(cg);

		/*
		final JRadioButton hyperView = new JRadioButton("Hyperbolic View");
        hyperView.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
               ((GraphViewImpl)view).lensSupport.activate(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
		 */

		CadralGraphMouse gm = ((CadralGraphMouse)view.getVisualisationViewer().getGraphMouse());
		gm.setClickedGraph(gwo);

		final GraphWithOperations g	= (GraphWithOperations)view.getVisualisationViewer().getModel().getGraphLayout().getGraph();
		final CNode n1 = new CNode("N1");
		g.addVertex(n1);
		layout.setLocation(n1,new Point2D.Double(400,400));
		/*
		final CNode n2 = new CNode("N2");
		g.addVertex(n2);
		final CNode n3 = new CNode("N3");
		g.addVertex(n3);
		g.addEdge(new CEdge("E1"),n1,n2,EdgeType.DIRECTED);
		layout.setLocation(n1,new Point2D.Double(250,250));
		layout.setLocation(n3,new Point2D.Double(100,250));
		 */
		final ScalingControl scaler = new CrossoverScalingControl();

		JButton plus = new JButton("+");
		plus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scaler.scale(vv, 1.1f, vv.getCenter());
			}
		});
		JButton minus = new JButton("-");
		minus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scaler.scale(vv, 1/1.1f, vv.getCenter());
			}
		});
		JPanel controls = new JPanel();
		JPanel zoomControls = new JPanel(new GridLayout(2,1));
		zoomControls.setBorder(BorderFactory.createTitledBorder("Zoom"));
		JPanel hyperControls = new JPanel(new GridLayout(3,2));
		hyperControls.setBorder(BorderFactory.createTitledBorder("Examiner Lens"));
		zoomControls.add(plus);
		zoomControls.add(minus);
		JPanel modeControls = new JPanel(new BorderLayout());
		modeControls.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
		modeControls.add(gm.getModeComboBox());
		//hyperControls.add(hyperView);

		Container content = f.getContentPane();
		controls.add(zoomControls);
		// controls.add(hyperControls);
		controls.add(modeControls);
		content.add(controls, BorderLayout.SOUTH);

		//GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
		content.add(view.asComponent());

		//f.add(view.asComponent());
		f.setVisible(true);
		f.pack();	
	}

	@Override
	public void setShape(final CShape cs){
		this.cadralGraphMouse.setShape(cs);
	}

	@Override
	public void setColor(final Color co){
		this.cadralGraphMouse.setColor(co);
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
		metaInfosPanel.removeAll();
	}


}
