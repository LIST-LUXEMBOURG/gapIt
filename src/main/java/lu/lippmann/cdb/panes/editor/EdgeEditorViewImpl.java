/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.panes.editor;

import java.awt.Component;
import java.awt.event.*;

import javax.swing.*;

import lu.lippmann.cdb.common.guice.util.AutoBind;
import lu.lippmann.cdb.context.ApplicationContext;
import lu.lippmann.cdb.event.*;
import lu.lippmann.cdb.models.CEdge;
import lu.lippmann.cdb.models.history.*;

import org.jdesktop.swingx.JXTaskPane;

import com.google.inject.Inject;
//import com.jgoodies.forms.layout.*;


/**
 * 
 * @author
 *
 */
@AutoBind
public class EdgeEditorViewImpl extends JPanel implements EdgeEditorView {

	private static final long serialVersionUID = -7592049896799652191L;

	private CEdge initialEdge;
	private CEdge edge;

	private JTextField libelleField;
	private JTextField expressionField;

	// FIXME: move this code in presenter! -> appcontext should not be accessible in view
	@Inject
	private ApplicationContext context;
	
	@Inject 
	private EventPublisher eventPublisher;
	
	//@Inject
	//private CommandDispatcher commandDispatcher;


	/**
	 * 
	 */
	public EdgeEditorViewImpl() {
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
					if(!initialEdge.getName().equals(libelleField.getText())){
						// FIXME: move this code in presenter! -> appcontext should not be accessible in view
						final GraphWithOperations graph = (GraphWithOperations)context.getCadralGraph().getInternalGraph();
						edge.setName(initialEdge.getName()); //ignore temporary change !
						final CEdge newEdge=new CEdge(libelleField.getText(),expressionField.getText());
						newEdge.clearAndAddTags(edge.getTags());
						graph.updateEdge(edge,newEdge);
						initialEdge = edge;
						//edge has changed -> repaint graph
						eventPublisher.publish(new GraphRepaintedEvent());
					}
				}
			}
		});

		expressionField.setText("");
		expressionField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyChar()==KeyEvent.VK_ENTER){
					if(!initialEdge.getExpression().equals(expressionField.getText())){
						// FIXME: move this code in presenter! -> appcontext should not be accessible in view
						final GraphWithOperations graph = (GraphWithOperations)context.getCadralGraph().getInternalGraph();
						edge.setExpression(initialEdge.getExpression()); //ignore temporary change !
						final CEdge newEdge=new CEdge(libelleField.getText(),expressionField.getText());
						newEdge.clearAndAddTags(edge.getTags());
						graph.updateEdge(edge,newEdge);
						initialEdge = edge;
						
						//edge has changed -> repaint graph
						eventPublisher.publish(new GraphRepaintedEvent());
					}
				}
			}
		});

		//Annoying, please use ? -> syntax ...
		//expressionField.setToolTipText(EvalUtil.getEvalHelpInHTML());

	}


	/**
	 * 
	 */
	private void initComponents() {
		/*setLayout(new FormLayout("right:100px, fill:100px",
		"center:20px, center:20px, center:20px"));*/

		//final CellConstraints c = new CellConstraints();

		/*add(new JLabel("Libelle"), c.xy(1, 1));
		libelleField = new JTextField();
		add(libelleField, c.xy(2, 1));

		add(new JLabel("Expression"), c.xy(1, 2));
		expressionField = new JTextField();
		add(expressionField, c.xy(2, 2));*/
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component asComponent() {
		final JXTaskPane panel = new JXTaskPane();
		panel.add(this);
		panel.setTitle("Edge properties");
		return panel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEdge(CEdge edge) {
		this.edge = edge;
		this.initialEdge = new CEdge(edge.getName(),edge.getExpression());
		this.libelleField.setText(edge.getName());
		this.expressionField.setText(edge.getExpression());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void focusOnField() {
		expressionField.requestFocus();
		expressionField.selectAll();
	}

}
