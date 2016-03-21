/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.main;

import java.awt.*;

import javax.swing.*;

import lu.lippmann.cdb.common.gui.LogoHelper;


/**
 * MainViewLoadingFrame.
 * 
 * @author the ACORA team 
 */
public class MainViewLoadingFrame extends JFrame
{
	//
	// Static fields
	//
	
	/** Serial version UID. */
	private static final long serialVersionUID=1234L;

	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public MainViewLoadingFrame() 
	{
		super("Loading ...");
		
		LogoHelper.setLogo(this);
		
	    final JProgressBar progressBar = new JProgressBar();
	    progressBar.setIndeterminate(true);
	    final JPanel contentPane = new JPanel();
	    contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	    contentPane.setLayout(new BorderLayout());
	    contentPane.add(new JLabel("Loading ..."), BorderLayout.NORTH);
	    contentPane.add(progressBar, BorderLayout.CENTER);
	    this.setContentPane(contentPane);
	    this.pack();
	    this.setLocationRelativeTo(null);
	    
		contentPane.setPreferredSize(new Dimension(200,50));
	    this.setResizable(false);
	    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}
}
