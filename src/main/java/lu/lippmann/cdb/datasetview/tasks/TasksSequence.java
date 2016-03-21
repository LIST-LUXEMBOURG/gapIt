/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tasks;

import java.util.*;

import lu.lippmann.cdb.common.FormatterUtil;


/**
 * TasksSequence.
 * 
 * @author Olivier PARISOT
 */
final class TasksSequence
{
	//
	// Instance fields
	//
	
	/** */
	private final List<Task> tasks;
	/** */
	private double completenessRatio;
	/** */
	int dtSize;
	/** */
	double dtErrorRate;
	/** */
	double jaccardIndex;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public TasksSequence()
	{
		this.tasks=new ArrayList<Task>();
		this.completenessRatio=Double.MAX_VALUE;
		this.dtSize=Integer.MAX_VALUE;
		this.dtErrorRate=Double.MAX_VALUE;
		this.jaccardIndex=Double.POSITIVE_INFINITY;
	}
	
	
	//
	// Instance methods
	//
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		final StringBuilder sb=new StringBuilder();
		sb.append("size=").append(this.dtSize).append(" ");
		sb.append("err=").append(FormatterUtil.DECIMAL_FORMAT.format(this.dtErrorRate)).append(" ");
		sb.append("cr=").append(FormatterUtil.DECIMAL_FORMAT.format(this.completenessRatio)).append(" ");					
		if (!Double.isInfinite(this.jaccardIndex))
		{
			sb.append("jacc=").append(FormatterUtil.DECIMAL_FORMAT.format(this.jaccardIndex)).append(" ");
		}
		//for (final Task tt:this.tasks) sb.append(tt.getClass().getSimpleName()).append(" ");
		for (final Task tt:this.tasks) sb.append("[").append(tt.getName()).append("] ");			

		return sb.toString();			
	}

	public double getCompletenessRatio() 
	{
		return completenessRatio;
	}


	public void setCompletenessRatio(double completenessRatio) 
	{
		this.completenessRatio = completenessRatio;
	}


	public int getDtSize() 
	{
		return dtSize;
	}


	public void setDtSize(int dtSize) 
	{
		this.dtSize = dtSize;
	}


	public List<Task> getTasks()
	{
		return tasks;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(completenessRatio);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(dtErrorRate);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + dtSize;
		temp = Double.doubleToLongBits(jaccardIndex);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		//result = prime * result + ((tasks == null) ? 0 : tasks.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) 
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		TasksSequence other = (TasksSequence) obj;
		if (Double.doubleToLongBits(completenessRatio) != Double.doubleToLongBits(other.completenessRatio)) return false;
		if (Double.doubleToLongBits(dtErrorRate) != Double.doubleToLongBits(other.dtErrorRate)) return false;
		if (dtSize != other.dtSize) return false;
		if (Double.doubleToLongBits(jaccardIndex) != Double.doubleToLongBits(other.jaccardIndex)) return false;
		/*if (tasks == null) 
		{
			if (other.tasks != null) return false;
		} 
		else if (!tasks.equals(other.tasks)) return false;*/
		return true;
	}
}