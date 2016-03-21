/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.panes.variables;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.*;

/**
 * 
 * @author didry
 *
 */
public class EnumerationTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 851966468429814813L;

	private List<String> values;

	public EnumerationTableModel(final List<String> values) 
	{
		this.values=values;
	}
	
	@Override
	public int getRowCount() {
		return values.size();
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return "Value";
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return values.get(rowIndex);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		//Check if it's a valid value
		if (aValue.toString().isEmpty() || values.contains(aValue.toString()))
			return;	
		values.set(rowIndex, aValue.toString());
	}
	
	
	/**
	 * 
	 * @param values
	 */
	public void setValues(List<String> values) {
		this.values = values;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<String> getValues() {
		return values;
	}
	
	public static void main(String[] args) {
		final JXFrame f = new JXFrame();
		f.setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		
		final EnumerationTableModel model = new EnumerationTableModel(Arrays.asList("France","Belgique","Luxembourg"));
		final JXTable table = new JXTable(model);
		final JScrollPane scroll = new JScrollPane(table);
		f.setPreferredSize(new Dimension(200,300));
		final JXButton b = new JXButton("Add value");
		final JXButton s = new JXButton("Save");
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<String> newValues = new ArrayList<String>(model.getValues());
				newValues.add("New value");
				model.setValues(newValues);
				final int rowIndex = newValues.size()-1;
				table.getSelectionModel().clearSelection();
				model.fireTableRowsInserted(rowIndex, rowIndex);
				table.editCellAt(rowIndex, 0);
				final Component cell = table.getEditorComponent();
				cell.requestFocusInWindow();
				if(cell instanceof JTextField) ((JTextField) cell).selectAll();
				SwingUtilities.invokeLater(new Runnable() 
				{
					public void run() 
					{
						scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getHeight()); 
					}
				});	
		    }
		});
		table.setColumnControlVisible(false);
		table.setShowGrid(false,false);
		table.getTableHeader().setReorderingAllowed(false);
		table.setVisibleRowCount(5);
	    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    
	    //Model
	    gbc.weightx = gbc.weighty = 1.0;
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.gridwidth  = 2;
	    gbc.fill = GridBagConstraints.BOTH;
	    f.add(scroll,gbc);
	    
	    //Save & Add buttons
	    gbc.weightx = gbc.weighty = 0.0;
	    gbc.gridwidth = 1;
	    gbc.gridheight = 1;
	    gbc.gridx = 0;
	    gbc.gridy = 1;
	    f.add(b,gbc);
	    
	    gbc.gridx = 1;
	    gbc.gridy = 1;
	    f.add(s,gbc);
	    //
	    
	    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    f.setLocationRelativeTo(null);
	    f.pack();
	    f.setVisible(true);
	}


}
