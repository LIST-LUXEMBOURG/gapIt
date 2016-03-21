/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.graph;

import java.awt.*;
import java.awt.Dialog.ModalityType;

import javax.swing.*;

import lu.lippmann.cdb.common.gui.LogoHelper;

import org.jdesktop.swingx.*;


/**
 * GraphViewJXFrameFactory.
 *
 * @author Olivier PARISOT
 */
public class GraphViewJXFrameFactory 
{
	//
	// Constructors
	//
	
	/**
	 * Private constructor.
	 */
	private GraphViewJXFrameFactory() {}
	
	
	//
	// Static methods
	//
	
	public static void showJXFrame(final GraphView myGraph,final String title,final int width,final int height, boolean exitOnClose)
	{
		final JXFrame f=new JXFrame();
		f.setSize(new Dimension(width,height));
		LogoHelper.setLogo(f);
		f.setTitle(title);
		f.setLayout(new BorderLayout());												
		myGraph.fitGraphToSubPanel(width,height,100);
		f.add(myGraph.asComponent());
		f.pack();
		if(exitOnClose){
			f.setDefaultCloseOperation(JXFrame.EXIT_ON_CLOSE);
		}
		f.setVisible(true);					
	}
	
	public static <V,E> void showGenericJXFrame(final GenericGraphView<V,E> myGraph,final String title,final int width,final int height, boolean exitOnClose)
	{
		final JXFrame f=new JXFrame();
		f.setSize(new Dimension(width,height));
		LogoHelper.setLogo(f);
		f.setTitle(title);
		f.setLayout(new BorderLayout());												
		myGraph.fitGraphToSubPanel(width,height,100);
		f.add(myGraph.asComponent());
		f.pack();
		if(exitOnClose){
			f.setDefaultCloseOperation(JXFrame.EXIT_ON_CLOSE);
		}
		f.setVisible(true);					
	}
	
	public static void showJXFrame(final GraphView myGraph,final String title,final int width,final int height){
		showJXFrame(myGraph,title,width,height,false);
	}
	
	public static void showJDialog(final GraphView myGraph,final String title,final int width,final int height,final Frame parent)
	{
		final JDialog f=new JDialog(parent,ModalityType.APPLICATION_MODAL);
		f.setSize(new Dimension(width,height));
		LogoHelper.setLogo(f);
		f.setTitle(title);
		f.setLayout(new BorderLayout());												
		myGraph.fitGraphToSubPanel(width,height,100);
		f.add(myGraph.asComponent());
		f.pack();
		f.setVisible(true);		
	}
	
}
