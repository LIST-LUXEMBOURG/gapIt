/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui.dataset;

import java.awt.Component;
import java.io.File;
import java.util.*;
import java.util.prefs.Preferences;

import javax.swing.*;

import lu.lippmann.cdb.weka.WekaDataAccessUtil;
import weka.core.*;


/**
 * InstancesLoaderDialogFactory.
 *
 * @author Olivier PARISOT
 */
public final class InstancesLoaderDialogFactory 
{
	//
	// Constructors
	//
	
	private static final String REG_KEY = "DEFAULT_PATH";

	/**
	 * Private constructor.
	 */
	private InstancesLoaderDialogFactory() {}
	
	
	//
	// Static methods
	//	
	
	public static Instances showDialogWithClassSelection(final Component parent) throws Exception
	{
		return showDialog(parent,true);
	}	
	
	public static Instances showDialogWithoutClassSelection(final Component parent) throws Exception
	{
		return showDialog(parent,false);
	}
	
	private static Instances showDialog(final Component parent,final boolean setClass) throws Exception
	{
		final Preferences prefs = Preferences.userRoot().node("CadralDecisionBuild");
		final String path = prefs.get(REG_KEY, WekaDataAccessUtil.DEFAULT_SAMPLE_DIR);
		
		final JFileChooser fc=new JFileChooser();
		fc.setCurrentDirectory(new File(path));
		final int returnVal=fc.showOpenDialog(parent);
		if (returnVal==JFileChooser.APPROVE_OPTION) 
		{
			final File file=fc.getSelectedFile();			
			if (file!=null)
			{
				prefs.put(REG_KEY,file.getPath());
				final Instances ds=WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(file);
				final Attribute defaultClassAttr=ds.classIndex()>=0?ds.classAttribute():ds.attribute(0);
				ds.setClassIndex(-1);
				ds.setRelationName(file.getPath());
				final List<String> attributesNames=new ArrayList<String>();
				final Enumeration<?> e=ds.enumerateAttributes();
				while (e.hasMoreElements())
				{
					final Attribute attr=(Attribute)e.nextElement();
					attributesNames.add(attr.name());
				}				
				
				if (setClass)
				{
					final String s=(String)JOptionPane.showInputDialog(
									parent,
				                    "Select the class attribute for '"+file.getName()+"' (default:'"+defaultClassAttr.name()+"'): ",
				                    "Class selection",
				                    JOptionPane.QUESTION_MESSAGE,
				                    null, // icon
				                    attributesNames.toArray(),
				                    attributesNames.get(attributesNames.size()-1));
					if (s!=null)
					{
						ds.setClass(ds.attribute(s));
					}
					else
					{
						//Otherwise no class defined and CACHE attributeClass => No class index defined after cancel + retry
						ds.setClass(defaultClassAttr); 
						return null;
					}
				}
				else
				{
					ds.setClass(defaultClassAttr);
				}
				return ds;
			}
			else throw new Exception();
		}
		else return null;
	}

}
