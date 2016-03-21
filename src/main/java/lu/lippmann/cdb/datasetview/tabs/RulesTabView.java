/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tabs;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import lu.lippmann.cdb.context.ResourceLoader;
import lu.lippmann.cdb.weka.WekaMachineLearningUtil;

import org.jdesktop.swingx.*;
import weka.core.Instances;


/**
 * RulesTabView.
 *
 * @author the WP1 team
 */
public final class RulesTabView extends AbstractTabView
{
	//
	// Instance fields
	//

	/** */
	private final JXPanel jxp;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public RulesTabView()
	{
		super();		
		this.jxp=new JXPanel();
		this.jxp.setLayout(new BorderLayout());	
	}
	
	
	//
	// Instance methods
	//
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSlow() 
	{		
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() 
	{
		return "Rules";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getComponent() 
	{			
		return jxp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update0(final Instances dataSet) throws Exception 
	{		 
		this.jxp.removeAll();
		
		final JXPanel internalTextualModelPanel=new JXPanel();

		final List<String> l=WekaMachineLearningUtil.computeJRipRules(dataSet);
		int rowcount=0;
		final StringBuilder sbTM=new StringBuilder();
		for (final String s:l) 
		{	
			sbTM.append(s).append('\n');
			rowcount++;
		}	
		final JXTextArea textualModelLabel=new JXTextArea();
		textualModelLabel.setText(sbTM.toString());
		textualModelLabel.setEditable(false);
		textualModelLabel.setColumns(80);
		textualModelLabel.setRows(rowcount*2<=40?rowcount*2:40);
		textualModelLabel.setWrapStyleWord(true);
		textualModelLabel.setLineWrap(true);
		final JScrollPane comp=new JScrollPane(textualModelLabel);
		comp.setBorder(new TitledBorder("Rules"));
		internalTextualModelPanel.add(comp);
	
		this.jxp.add(internalTextualModelPanel,BorderLayout.CENTER);		
		this.jxp.repaint();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Icon getIcon() 
	{
		return ResourceLoader.getAndCacheIcon("menu/rules.png");
	}
}
