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

import lu.lippmann.cdb.common.gui.LogoHelper;
import lu.lippmann.cdb.common.guice.util.AutoBind;
import lu.lippmann.cdb.common.mvp.Listener;
import lu.lippmann.cdb.models.*;
import lu.lippmann.cdb.models.CVariable.CadralType;

import org.jdesktop.swingx.*;


/**
 * 
 * @author the ACORA/WP team
 */
@AutoBind
public class CVariableEditorViewImpl extends JXFrame implements CVariableEditorView {

	//
	// Static fields
	//

	/** */
	private static final long serialVersionUID=5920498967191L;


	//
	// Instance fields
	//

	/** */
	private Listener<CVariable> listener;

	/** */
	private CVariable cv;

	/** */
	private boolean alreadyInRepository;
	
	/** */
	private boolean saveInRepository;
	
	/** */
	private boolean delInRepository;

	/** */
	private JTextField descField;
	/** */
	private JComboBox typeField;


	//
	// Instance methods
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reInit(final Component parent) 
	{
		saveInRepository	= false;
		delInRepository		= false;
		
		LogoHelper.setLogo(this);
		
		setTitle("Definition of '"+cv.getKey()+"'");
		setPreferredSize(new Dimension(400,150));
		
		getContentPane().removeAll();
		getContentPane().setLayout(new BorderLayout());

		final JButton values=new JButton("Values");

		final JXPanel p=new JXPanel();		
		p.setLayout(new GridLayout(0,2));
		p.add(new JLabel("Name:                                 "));
		p.add(new JLabel(cv.getKey()));
		p.add(new JLabel("Description:                          "));
		descField=new JTextField(cv.getDescription());
		p.add(descField);
		p.add(new JLabel("Type:                                 ")); //FIXME: lol
		typeField=new JComboBox(CVariable.CadralType.values());
		typeField.setSelectedItem(cv.getType());

		values.setVisible(cv.getType().equals(CadralType.ENUMERATION));

		typeField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				values.setVisible(typeField.getSelectedItem().equals(CadralType.ENUMERATION));
			}
		});

		p.add(typeField);
		getContentPane().add(p,BorderLayout.CENTER);

		final JXPanel cpanel=new JXPanel();
		cpanel.setLayout(new FlowLayout());
		final JButton ok=new JButton("Ok");
		ok.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e) 
			{								
				final CadralType type=CadralType.valueOf(typeField.getSelectedItem().toString());
				if (type==CadralType.UNKNOWN)
				{
					JOptionPane.showMessageDialog(asComponent(),"Please provide another type!");
				}
				else
				{
					cv.setDescription(descField.getText());
					cv.setType(type);
					listener.onAction(cv);					
				}
			}
		});
		cpanel.add(ok);

		final JButton cancel=new JButton("Cancel");
		cancel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e) 
			{
				listener.onAction(null);
			}
		});
		cpanel.add(cancel);


		values.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e) 
			{
				showFrameFor(cv);
			}
		});
		cpanel.add(values);
		
		if(!alreadyInRepository){
			final JCheckBox saveToRepo=new JCheckBox("Save to repo.");
			saveToRepo.addActionListener(new ActionListener()
			{	
				@Override
				public void actionPerformed(final ActionEvent e) 
				{
					saveInRepository = saveToRepo.isSelected();
				}
			});
			cpanel.add(saveToRepo);
		}else{
			final JCheckBox delFromRepo=new JCheckBox("Delete from repo.");
			delFromRepo.addActionListener(new ActionListener()
			{	
				@Override
				public void actionPerformed(final ActionEvent e) 
				{
					delInRepository = delFromRepo.isSelected();
				}
			});
			cpanel.add(delFromRepo);
		}
		
		
		getContentPane().add(cpanel,BorderLayout.SOUTH);
		setLocationRelativeTo(null);
		pack();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component asComponent() 
	{
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setOnCVariableUpdateListener(final Listener<CVariable> listener) 
	{
		this.listener=listener;		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCVariable(final CVariable cv) 
	{
		this.cv=new CVariable(cv);		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAlreadyInRepository(boolean alreadyInRepository) {
		this.alreadyInRepository = alreadyInRepository;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasToBeSavedInRepository() {
		return saveInRepository;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasToBeDeletedInRepository() {
		return delInRepository;
	}

	/**
	 * 
	 * @param cv
	 */
	private void showFrameFor(final CVariable cv) {
		final JXFrame f = new JXFrame();
		f.setTitle("Values for '"+cv.getKey()+"'");
		LogoHelper.setLogo(f);
		f.setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		List<String> values = cv.getValues();
		if(values == null) values = Arrays.asList("Sample value");
		final EnumerationTableModel model = new EnumerationTableModel(values);
		final JXTable table = new JXTable(model);
		final JScrollPane scroll = new JScrollPane(table);
		f.setPreferredSize(new Dimension(200,300));
		final JXButton addButton = new JXButton("Add value");
		final JXButton delButton = new JXButton("Delete value");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<String> newValues = new ArrayList<String>(model.getValues());
				newValues.add("New value");
				model.setValues(newValues);
				final int rowIndex = newValues.size()-1;
				model.fireTableRowsInserted(rowIndex, rowIndex);
				table.getSelectionModel().clearSelection();
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
		delButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final int rowIndex = table.getSelectedRow();
				if (rowIndex > -1) {
					final List<String> newValues = new ArrayList<String>(model.getValues());
					newValues.remove(rowIndex);
					model.setValues(newValues);
					model.fireTableRowsDeleted(rowIndex,rowIndex);
				}
			}
		});
		

		f.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				final List<String> listPossibleValues = model.getValues();
				cv.setValues(listPossibleValues);
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
		f.add(addButton,gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		f.add(delButton,gbc);
		//


		f.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		f.setLocationRelativeTo(null);
		f.pack();
		f.setVisible(true);
	}



}
