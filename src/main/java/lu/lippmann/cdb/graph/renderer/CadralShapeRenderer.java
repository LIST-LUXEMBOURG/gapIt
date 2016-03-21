/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.graph.renderer;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

import lu.lippmann.cdb.common.gui.ColorHelper;
import lu.lippmann.cdb.graph.GraphUtil;
import lu.lippmann.cdb.lab.mds.MDSViewBuilder;
import lu.lippmann.cdb.models.*;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.renderers.VertexLabelAsShapeRenderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;


/**
 * 
 * @author
 *
 */
public class CadralShapeRenderer extends VertexLabelAsShapeRenderer<CNode, CEdge> {


	private Map<CNode,Map<Object,Integer>> mapRepartition;
	
	private static List<Paint> colors;
	
	private static double scale = 1;
	
	/**
	 * 
	 * @param rc
	 */
	public CadralShapeRenderer(final Map<CNode,Map<Object,Integer>> m,final RenderContext<CNode, CEdge> rc) 
	{
		super(rc);
		this.mapRepartition = m;
		
		if(colors==null){
			colors = new ArrayList<Paint>();
			for (int kk=0;kk<100;kk++) colors.add(ColorHelper.getRandomBrightColor()); // FIXME
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Shape transform(CNode node) {
		return ShapeFactory.createShape(node.getShape(),scale);
	}

	@Override
	public void labelVertex(RenderContext<CNode, CEdge> rc,Layout<CNode, CEdge> layout, CNode v, String label) {

		//Shape height
		final Shape shape = ShapeFactory.createShape(v.getShape(),scale);
		double w = shape.getBounds().getWidth();
		double h = shape.getBounds().getHeight();

		//Font color & compute size
		final GraphicsDecorator g = rc.getGraphicsContext();

		if(mapRepartition!=null && mapRepartition.containsKey(v)){

			Point2D pos = layout.transform(v);
			pos = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, pos);
			
			//FIXME cache for that in ctor
			int total = 0;
			Map<Object,Integer> mapRep =  mapRepartition.get(v);
			for(Integer i : mapRep.values()) total+=i;
			

			
			//TODO colors !
			MDSViewBuilder.createPieChart(
					g.getDelegate(), (int)(pos.getX()+w/2.0),(int)(pos.getY()-h/2.0), 
					(int)h,mapRep,total,colors);
		}

		if(rc.getPickedVertexState().isPicked(v)){
			if (GraphUtil.isDarkNode(v)) {
				g.setColor(new Color(153,204,255));
			} else {
				g.setColor(Color.BLUE);
			}
		}else{
			if (GraphUtil.isDarkNode(v)) {
				g.setColor(Color.WHITE);
			} else {
				g.setColor(Color.BLACK);
			}
		}
		
		Font font = rc.getVertexFontTransformer().transform(v);
		g.setFont(font);
		
		FontMetrics fm   = g.getFontMetrics();
		Rectangle2D rect = fm.getStringBounds(label, g.getDelegate());
		
		int textWidth  = (int)(rect.getWidth());	
		int fontSize  = fm.getFont().getSize();	
		
		//Node position + transform to adapt with zoom/rotation
		Point2D p = layout.transform(v);
		p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
		
		//Magic numbers to center !
		g.drawString(label, (int)(p.getX()-textWidth/2.0+1.5),(int)(p.getY()+fontSize/2.0));
		
		//For debug		
		//int textHeight = (int)(rect.getHeight());
		//g.drawRect((int)(p.getX()-textWidth/2.0),(int)(p.getY()-textHeight/2.0), (int)rect.getWidth(), (int)rect.getHeight());
	}

	/**
	 * 
	 * @param mapRep
	 */
	public void setShapeRepartition(final Map<CNode,Map<Object,Integer>> mapRepartition){
		this.mapRepartition = mapRepartition;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<Paint> getColors() {
		return colors;
	}
	
	public static void setScale(double s) {
		scale = s;
	}
	

}
