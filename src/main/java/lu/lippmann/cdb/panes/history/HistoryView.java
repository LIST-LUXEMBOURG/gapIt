/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.panes.history;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import lu.lippmann.cdb.common.mvp.*;
import lu.lippmann.cdb.models.CUser;
import lu.lippmann.cdb.models.history.GraphOperation;

import org.jdesktop.swingx.*;


/**
 * HistoryView.
 *  
 * @author The ACORA team.
 */
public class HistoryView extends JPanel implements Display{

	//
	// Static fields
	//
	
	/** Serial version UID */
	private static final long serialVersionUID=17592926L;
	
	
	//
	// Instance fields
	//
	
	private List<GraphOperation> operations;
	private Listener<List<GraphOperation>> onClickListener;
	private JScrollPane scrollPane = null;

	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public HistoryView() {
		this.operations = new LinkedList<GraphOperation>();
	}

	
	//
	// Instance methods
	//
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component asComponent() 
	{
		final JXTaskPane panel=new JXTaskPane();
		panel.setLayout(new GridLayout(1,1));
		if (scrollPane == null) {
			scrollPane = new JScrollPane(this);
		}
		panel.add(scrollPane);
		scrollPane.setPreferredSize(new Dimension(150,200));
		panel.setTitle("History");
		panel.setCollapsed(true);
		return panel;
	}

	public void setOperations(List<GraphOperation> operations) 
	{
		this.operations = operations;
		refresh();
	}

	private void refresh() 
	{
		removeAll();

		if (operations.isEmpty())
		{
			add(new JXLabel("Empty!"));
			return;
		}
		
		setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		final List<GraphOperation> copyOperations = new ArrayList<GraphOperation>(operations);
		CUser lastHistoryLineOwner = null;
		Long groupId = null, nextGroupId = null;
		final List<GraphOperation> tmpList = new ArrayList<GraphOperation>();

		for(int i = 0 ; i < copyOperations.size() ; i++){
			final GraphOperation operation 		= copyOperations.get(i);
			final GraphOperation nextOperation	= (i+1 < copyOperations.size())?copyOperations.get(i+1):null;
			HistorySnapshotPanel panel = null;
			groupId 	  = operation.getIdGroup();
			nextGroupId   = (nextOperation!=null)?nextOperation.getIdGroup():null;

			if(groupId!=null){
				tmpList.add(operation);
				if(nextGroupId==null||!nextGroupId.equals(groupId)){
					panel = new HistorySnapshotPanel(tmpList);
					tmpList.clear();
				}
			}else{
				panel = new HistorySnapshotPanel(operation);
			}

			if(panel != null){
				panel.setOnClick(onClickListener);
				//display modification by user
				CUser userForThisLine = operation.getUser();
				if(userForThisLine==null){userForThisLine=CUser.ANONYMOUS;}
				if (!userForThisLine.equals(lastHistoryLineOwner)) 
				{
					final JXLabel label = new JXLabel("Changes from "+userForThisLine.toString());
					label.setFont(label.getFont().deriveFont(Font.BOLD).deriveFont(13f));
					add(label,gbc);
					gbc.gridy++;
					lastHistoryLineOwner = userForThisLine;
				}

				add(panel,gbc);
				gbc.gridy++;
			}
		}

		gbc.weighty = 1;
		add(new JPanel(), gbc);		
		
		validate();
		updateUI();

		//scroll to bottom
		SwingUtilities.invokeLater(new Runnable() 
		{
			public void run() 
			{
				scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getHeight()); 
			}
		});		
	}

	public void setOnClick(Listener<List<GraphOperation>> listener) {
		this.onClickListener = listener;		
	}

}
