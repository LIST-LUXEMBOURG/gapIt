/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.panes.editor;

import java.awt.*;
import javax.swing.JPanel;

import lu.lippmann.cdb.common.mvp.Display;

import org.jdesktop.swingx.JXTaskPane;



/**
 * ActionsView.
 * 
 * @author the ACORA team
 */
@Deprecated
public class ActionsView extends JPanel implements Display 
{
	//
	// Static fields
	//
	
	/** Serial version UID. */
	private static final long serialVersionUID=14372L;

	
	//
	// Instance fields
	//
	
	/*@Inject
	private CommandDispatcher commandDispatcher;*/
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor. 
	 */
	public ActionsView() 
	{
		initComponents();
	}
	
	
	//
	// Instance methods
	//
	
	private void initComponents() 
	{
		setLayout(new GridLayout(0,1));
		
		/*final JButton clusterBtn = new JButton("Reorganize all");
		clusterBtn.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				commandDispatcher.dispatch(new SelectAllCommand());
				commandDispatcher.dispatch(new ClusterGraphCommand());
				commandDispatcher.dispatch(new DeselectAllCommand());
			}
		});
		
		add(clusterBtn);
		
		final JButton deleteBtn = new JButton("Delete");
		deleteBtn.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				commandDispatcher.dispatch(new DeleteCommand());
			}
		});
		add(deleteBtn);*/
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component asComponent() 
	{
		final JXTaskPane panel = new JXTaskPane();
		panel.add(this);
		panel.setTitle("Actions");
		return panel;
	}


}
