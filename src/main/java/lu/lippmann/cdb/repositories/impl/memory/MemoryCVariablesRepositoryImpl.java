/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.repositories.impl.memory;

import java.util.*;

import lu.lippmann.cdb.models.*;
import lu.lippmann.cdb.models.CVariable.CadralType;
import lu.lippmann.cdb.repositories.CVariablesRepository;


/**
 * 
 *
 *
 * @author Acora team
 */
public final class MemoryCVariablesRepositoryImpl implements CVariablesRepository
{
	//
	// Instance fields
	//
	
	/** */
	private Set<CVariable> variables;
	
	
	//
	// Constructor
	//
	
	/**
	 * Constructor.
	 */
	public MemoryCVariablesRepositoryImpl()
	{
		this.variables=new HashSet<CVariable>();
		final CVariable cv1=new CVariable();
		cv1.setKey("age");
		cv1.setType(CadralType.NUMERIC);
		cv1.setDescription("age de la personne");
		variables.add(cv1);
		
		final CVariable cv2=new CVariable();
		cv2.setKey("sexe");
		cv2.setType(CadralType.NUMERIC);
		cv2.setDescription("sexe de la personne");
		variables.add(cv2);
		
		final CVariable cv3=new CVariable();
		cv3.setKey("pays");
		cv3.setType(CadralType.ENUMERATION);
		cv3.setDescription("Pays de la personne");
		cv3.setValues(Arrays.asList("Luxembourg","France","Allemagne","Belgique"));
		variables.add(cv3);
	}
	
	
	
	//
	// Instance methods
	//
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<CVariable> getCadralVariables() 
	{
		return variables;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addOrUpdateCadralVariable(final CVariable cv) 
	{		
		if(variables.contains(cv)){
			variables.remove(cv);
		}
		variables.add(cv);	
	}
	


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeCadralVariable(CVariable cv) {
		variables.remove(cv);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(CVariable cv) {
		return variables.contains(cv);
	}

}
