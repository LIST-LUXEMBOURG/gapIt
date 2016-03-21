/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.panes.editor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import lu.lippmann.cdb.common.gui.*;
import lu.lippmann.cdb.common.guice.util.AutoBind;
import lu.lippmann.cdb.common.mvp.Listener;
import lu.lippmann.cdb.context.ApplicationContext;
import lu.lippmann.cdb.event.*;
import lu.lippmann.cdb.graph.renderer.CShape;
import lu.lippmann.cdb.models.CNode;
import lu.lippmann.cdb.models.history.GraphWithOperations;

import org.jdesktop.swingx.JXTaskPane;
import com.google.inject.Inject;
//import com.jgoodies.forms.layout.*;


/**
 * 
 * @author
 * 
 */
@AutoBind
public class NodeEditorViewImpl extends JPanel implements NodeEditorView {

	private static final long serialVersionUID = 801570745038117290L;

	private CNode node;

	private CNode oldNode;


	/** properties of the node **/
	private JTextField libelleField;
	private ShapeChooser shapeList;  
	private JColorButton nodeColor;


	@Inject
	private ApplicationContext context;
	@Inject 
	private EventPublisher eventPublisher;
	

	/**
	 * 
	 */
	public NodeEditorViewImpl() {
		setOpaque(false);
		initComponents();
		bindComponents();
	}

	/**
	 * 
	 */
	private void bindComponents() {


		libelleField.setText("");
		libelleField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyChar()==KeyEvent.VK_ENTER){
					if(!oldNode.getName().equals(libelleField.getText())){
						final GraphWithOperations graph = (GraphWithOperations)context.getCadralGraph().getInternalGraph();

						node.setName(oldNode.getName()); //ignore temporary change !

						//Historize node update
						final CNode newNode = new CNode(libelleField.getText());
						newNode.setColor(node.getColor());
						newNode.setShape(node.getShape());
						newNode.clearAndAddTags(node.getTags());
						graph.updateNode(node,newNode);

						oldNode.setName(libelleField.getText());

						//node has changed -> repaint graph
						eventPublisher.publish(new GraphRepaintedEvent());
					}
				}
			}
		});




		if(node!=null){
			nodeColor.setColor(node.getColor());
		}
		nodeColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(node!=null){
					final Color color =JColorChooser.showDialog(
							NodeEditorViewImpl.this,
							"Choose vertex background color",
							Color.WHITE);

					if(color!=null){
						if(!oldNode.getColor().equals(color)){
							final GraphWithOperations graph = (GraphWithOperations)context.getCadralGraph().getInternalGraph();
							nodeColor.setColor(color);

							//Historize node shape update
							final CNode newNode = new CNode(node.getName());
							newNode.setColor(color);
							newNode.setShape(node.getShape());
							newNode.clearAndAddTags(node.getTags());
							graph.updateNode(node,newNode);

							oldNode.setColor(color);
							eventPublisher.publish(new GraphRepaintedEvent());
						} 
					}
				}
			}
		});
	}


	/**
	 * 
	 */
	private void initComponents() {
		/*setLayout(new FormLayout("left:100px, fill:100px",
		"center:20px, center:20px, center:20px"));*/

		this.libelleField = new JTextField();
		this.shapeList = new ShapeChooser();
		this.nodeColor = new JColorButton("");
		this.nodeColor.setPreferredSize(new Dimension(50,50));

		//final CellConstraints c = new CellConstraints();

		/*add(new JLabel("Label"), c.xy(1,1));
		add(libelleField,c.xy(2,1));

		add(new JLabel("Shape"),c.xy(1,2));
		add(shapeList,c.xy(2,2));

		add(new JLabel("Color"),c.xy(1,3));
		add(nodeColor,c.xy(2,3));*/

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component asComponent() {
		final JXTaskPane panel = new JXTaskPane();
		panel.add(this);
		panel.setTitle("Node properties");
		return panel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNode(final CNode node) {
		this.node = node;

		//saved to avoid firing the event for nothing
		this.oldNode = node;

		this.libelleField.setText(node.getName());

		//final ActionListener[] save = this.shapeList.getActionListeners();
		//for(ActionListener s : save){this.shapeList.removeActionListener(s);}
		//this.shapeList.setSelectedItem(node.getShape());
		//for(ActionListener s : save){this.shapeList.addActionListener(s);}
		this.nodeColor.setColor(node.getColor());
		
		shapeList.setOnChangeShapeListener(new Listener<CShape>() {
			
			@Override
			public void onAction(CShape shape) {
				if(node!=null){
					if(!oldNode.getShape().equals(shape)){
						final GraphWithOperations graph = (GraphWithOperations)context.getCadralGraph().getInternalGraph();
						//Historize node shape update
						final CNode newNode = new CNode(node.getName());
						newNode.setColor(node.getColor());
						newNode.setShape(shape);
						newNode.clearAndAddTags(node.getTags());
						graph.updateNode(node,newNode);

						oldNode.setShape(shape);
						eventPublisher.publish(new GraphRepaintedEvent());
					}
				}
			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void focusOnField() {
		this.libelleField.requestFocusInWindow();
		this.libelleField.selectAll();
	}

}
