/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.panes.variables;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import lu.lippmann.cdb.common.guice.util.AutoBind;
import lu.lippmann.cdb.common.mvp.Listener;
import lu.lippmann.cdb.models.CVariable;
import lu.lippmann.cdb.models.CVariable.CadralType;

import org.jdesktop.swingx.JXTaskPane;


/**
 * VariablesEditorViewImpl.
 * 
 *@author the ACORA/WP1 team
 */
@AutoBind
public class VariablesEditorViewImpl extends JXTaskPane implements VariablesEditorView {

	//
	// Static fields
	//
	
	/** */
	private static final long serialVersionUID=159204952191L;

	
	//
	// Instance fields
	//
	
	/** */
	private Set<CVariable> definedCadralVariables;
	/** */
	private Set<CVariable> usedButNotDefinedCadralVariables;
	/** */
	private Set<CVariable> definedButNotUsedCadralVariables;
	
	/** */
	private Listener<CVariable> listener;


	
	
	//
	// Constructors.
	//
	
	/**
	 * Constructor.
	 */
	public VariablesEditorViewImpl() 
	{
		setTitle("Variables edition");
		setOpaque(false);
		initComponents();
	}

	
	//
	// Instance methods
	//

	/**
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private GridBagConstraints makeGbc(int x, int y,int wx,int wy) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = wx;
        gbc.weighty = wy;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }
	
	/**
	 * 
	 */
	private void initComponents() 
	{
		removeAll();
		setLayout(new GridLayout(0,1));

		final JTabbedPane varTab = new JTabbedPane();
		final JPanel usedPane = new JPanel();
		usedPane.setLayout(new GridBagLayout());
		final JPanel repoPane = new JPanel();
		repoPane.setLayout(new GridBagLayout());
		GridBagConstraints gbc;
		int used = 0;
		int repo = 0;
		if (definedCadralVariables!=null&&!definedCadralVariables.isEmpty())
		{
			for (final CVariable cv: new TreeSet<CVariable>(definedCadralVariables))
			{
				final String libelle;
				if (cv.hasDescription()) 
				{
					libelle=cv.getKey()+" ("+cv.getDescription()+"): "+cv.getType();
				}
				else
				{
					libelle=cv.getKey()+": "+cv.getType();
				}
				final JButton button=new JButton(libelle);
				button.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(final ActionEvent e) 
					{
						listener.onAction(cv);
					}
				});
				button.setBackground(Color.GREEN);
				button.setToolTipText("'"+cv.getKey()+"' used and defined in repository!");
				gbc = makeGbc(1, used,1,0);
				usedPane.add(button,gbc);
				used++;
			}
		}		

		if (definedButNotUsedCadralVariables!=null&&!definedButNotUsedCadralVariables.isEmpty())
		{
			for (final CVariable cv:new TreeSet<CVariable>(definedButNotUsedCadralVariables))
			{
				final JButton button=new JButton(cv.getKey()+" ("+cv.getDescription()+"): "+cv.getType());
				button.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(final ActionEvent e) 
					{
						listener.onAction(cv);
					}
				});
				button.setBackground(Color.ORANGE);
				button.setToolTipText("'"+cv.getKey()+"' defined in repository but not used!");
				gbc = makeGbc(1, repo,1,0);
				repoPane.add(button,gbc);
				repo++;
			}
		}
		
		if (usedButNotDefinedCadralVariables!=null&&!usedButNotDefinedCadralVariables.isEmpty())
		{
			for (final CVariable cv:new TreeSet<CVariable>(usedButNotDefinedCadralVariables))
			{
				final JButton button=new JButton();
				
				if(!cv.getType().equals(CadralType.UNKNOWN))
				{
					button.setText(cv.getKey()+" ("+cv.getDescription()+"): "+cv.getType());
					button.setBackground(Color.BLUE);
					button.setToolTipText("'"+cv.getKey()+"' used and defined,  but not in repository!");
				}
				else
				{
					button.setText(cv.getKey()+"?");
					button.setBackground(Color.RED);
					button.setToolTipText("'"+cv.getKey()+"' used but no type defined!");
				}
				button.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(final ActionEvent e) 
					{
						listener.onAction(cv);
					}
				});
				gbc = makeGbc(1, used,1,0);
				usedPane.add(button,gbc);	
				used++;
			}
		}
		
		int tbnIdx=0;
		if(used!=0)
		{
			varTab.add(usedPane);
			varTab.setTitleAt(tbnIdx,"Graph variables");
			varTab.setToolTipTextAt(tbnIdx, "Display the variables used in the graph");
			tbnIdx++;
		}
		if(repo!=0)
		{
			varTab.add(repoPane);
			varTab.setTitleAt(tbnIdx,"Unused repository variables");
			varTab.setToolTipTextAt(tbnIdx, "Display the variables defined in the repository");
		}
		
		add(varTab);
		
		updateUI();
		setVisible(true);
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
	public void reInit() 
	{
		initComponents();		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefinedAndUsedCVariables(final Set<CVariable> v) 
	{
		this.definedCadralVariables=v;		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUsedButNotDefinedCVariables(final Set<CVariable> v) 
	{
		this.usedButNotDefinedCadralVariables=v;		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setOnAskCVariableUpdateListener(final Listener<CVariable> listener) 
	{
		this.listener=listener;		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefinedButNotUsedCVariables(final Set<CVariable> v) 
	{
		this.definedButNotUsedCadralVariables=v;		
	}


}
