/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.panes;

import java.awt.Component;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import lu.lippmann.cdb.common.mvp.*;
import lu.lippmann.cdb.graph.GenericGraphView.ViewMode;

import org.jdesktop.swingx.JXTaskPane;


/**
 * ViewModePaneView.
 * 
 * @author the ACORA team.
 */
public class ViewModePaneView extends JXTaskPane implements Display 
{
	//
	// Static fields
	//
	
	/** Serial version UID. */
	private static final long serialVersionUID=12306L;
		
	
	//
	// Instance methods
	//
	
	/** */
	private Listener<ViewMode> onSelectViewListener;

	private List<JRadioButton> radios;
	
	//
	// Constructor
	//
	
	/**
	 * Constructor.
	 */
	public ViewModePaneView() 
	{
		super();
		setTitle("Mode");
		setOpaque(false);
		initComponent();		
	}
	
	
	//
	// Instance methods
	//
	
	private final void initComponent() 
	{			
		final ButtonGroup grp=new ButtonGroup();
		boolean first=true;
		this.radios = new ArrayList<JRadioButton>();
		for (final ViewMode vm:ViewMode.values())
		{
			JRadioButton radio=new JRadioButton(vm.name());
			radios.add(radio);
			grp.add(radio);
			add(radio);
			radio.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					final JRadioButton source=(JRadioButton)e.getSource();
					onSelectViewListener.onAction(ViewMode.valueOf(source.getText()));
				}
			});
			if (first)
			{
				radio.setSelected(true);
				first=false;
			}
		}		
		updateUI();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component asComponent()
	{
		return this;
	}

	public void setOnSelectView(Listener<ViewMode> listener) 
	{
		this.onSelectViewListener=listener;
	}

	/**
	 * 
	 * @param mode
	 */
	public void setViewMode(ViewMode mode){
		radios.get(ViewMode.valuesAsStringsList().indexOf(mode.name())).doClick();
	}

}
