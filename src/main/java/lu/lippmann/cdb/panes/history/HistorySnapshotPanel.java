/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.panes.history;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.tree.*;

import lu.lippmann.cdb.common.mvp.Listener;
import lu.lippmann.cdb.models.history.GraphOperation;

/**
 * 
 * @author 
 *
 */
public class HistorySnapshotPanel extends JPanel {

	private static final long serialVersionUID = 8732165628619048389L;

	private Listener<GraphOperation> onOverListener;
	private Listener<List<GraphOperation>> onClickListener;

	private Color backgroundColor; 

	/**
	 * 
	 * @param operation
	 */
	public HistorySnapshotPanel(GraphOperation operation) {
		initComponentsForSingleOperation(operation);
	}

	/**
	 * 
	 * @param tmpList
	 */
	public HistorySnapshotPanel(List<GraphOperation> tmpList) {
		initComponentsForGroupOperation(tmpList);
	}

	/**
	 * Single operation
	 */
	private void initComponentsForSingleOperation(final GraphOperation operation) {
		setLayout(new GridLayout(1,3));

		add(new JLabel(operation.toString()));


		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getButton()==MouseEvent.BUTTON1) {
					onClickListener.onAction(Arrays.asList(operation));
				}
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				if (onOverListener!=null) {
					onOverListener.onAction(operation);
				}
				backgroundColor = getBackground();
				setBackground(backgroundColor.brighter().brighter());
			}
			@Override
			public void mouseExited(MouseEvent e) {
				setBackground(backgroundColor);
			}
		});
	}

	/**
	 * 
	 */
	private void initComponentsForGroupOperation(final List<GraphOperation> groupOperation) {
		setLayout(new GridLayout(1,3));
		final List<GraphOperation> cpy = new ArrayList<GraphOperation>(groupOperation);
		if(!groupOperation.isEmpty()){

			String rootLabel ;
			if(groupOperation.get(0).getIdGroup() >= 0){
				rootLabel = "Group operation";
			}else{
				rootLabel = "History revert";
			}

			final DefaultMutableTreeNode tree = new DefaultMutableTreeNode(rootLabel);
			//List of operation needs to be reversed to be applied in the reverse order !
			Collections.reverse(groupOperation);

			for(final GraphOperation operation : groupOperation){
				tree.add(new DefaultMutableTreeNode(operation));
			}
			final JTree groupTree = new JTree(tree);
			final Color historyBackgroundColor = getBackground();
			final  DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)(groupTree.getCellRenderer());
			groupTree.collapseRow(0);
			renderer.setBackgroundNonSelectionColor(historyBackgroundColor);

			groupTree.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					super.mouseEntered(e);
					setBackground(historyBackgroundColor.brighter());
					renderer.setBackgroundNonSelectionColor(historyBackgroundColor.brighter());
				}
				@Override
				public void mouseClicked(MouseEvent e) {
					final TreePath path = groupTree.getSelectionPath();
					final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
					if(!selectedNode.isRoot()){
						if(e.getButton()==MouseEvent.BUTTON1) {
							onClickListener.onAction(cpy);
						}
					}
				}
				@Override
				public void mouseExited(MouseEvent e) {
					setBackground(historyBackgroundColor);
					renderer.setBackgroundNonSelectionColor(historyBackgroundColor);
					groupTree.setSelectionPath(null);
				}
			});
			groupTree.setBackground(backgroundColor);
			add(groupTree);
			repaint();
		}
	}

	/**
	 * 
	 * @param listener
	 */
	public void setOnOver(Listener<GraphOperation> listener) {
		this.onOverListener = listener;
	}

	/**
	 * 
	 * @param listener
	 */
	public void setOnClick(Listener<List<GraphOperation>> listener) {
		this.onClickListener = listener;		
	}

	/**
	 * 
	 * @param b
	 */
	public void setSelected(boolean b) {
		this.setBackground(Color.ORANGE.brighter().brighter());
	}
}
