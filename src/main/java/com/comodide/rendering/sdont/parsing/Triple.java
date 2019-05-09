package com.comodide.rendering.sdont.parsing;

/**
 * 
 * @author Cogs
 *
 * This object is used for passing source to target via role edges from a Parser to a Visualizer
 *
 */
public class Triple
{
	private String fr;
	private String to;
	private String pr;

	private boolean isSubClass;
	
	public Triple(String fr, String to, String pr)
	{
		this.fr = fr;
		this.to = to;
		this.pr = pr;
		this.isSubClass = false;
	}
	
	public Triple(String fr, String to)
	{
		this.fr = fr;
		this.to= to;
		this.pr = "SUBCLASS";
		this.isSubClass = true;
	}
	
	public boolean isSubClass()
	{
		return isSubClass;
	}

	public void setSubClass(boolean isSubClass)
	{
		this.isSubClass = isSubClass;
	}

	public String getFr()
	{
		return fr;
	}
	
	public void setFr(String fr)
	{
		this.fr = fr;
	}
	
	public String getTo()
	{
		return to;
	}
	
	public void setTo(String to)
	{
		this.to = to;
	}
	
	public String getPr()
	{
		return pr;
	}
	
	public void setPr(String pr)
	{
		this.pr = pr;
	}
}
