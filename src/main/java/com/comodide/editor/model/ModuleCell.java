package com.comodide.editor.model;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;

import com.comodide.configuration.Namespaces;
import com.comodide.editor.SDConstants;
import com.mxgraph.model.mxGeometry;

public class ModuleCell extends ComodideCell
{
	/** Bookkeeping */
	private static final long serialVersionUID = 1L;

	/** Styles for the Module based on whether or not its folded. */
	private static final String unfoldedModuleStyle = SDConstants.unfoldedModuleStyle;
	private static final String foldedModuleStyle   = SDConstants.foldedModuleStyle;

	/** The default IRI for the Module */
	private static IRI                  DEFAULT_IRI  = IRI.create(Namespaces.OPLA_CORE + "Module");
	private static EntityType<OWLClass> DEFAULT_TYPE = EntityType.CLASS;

	private ModuleCell(OWLEntity owlEntity)
	{
		super(owlEntity);
		this.style = unfoldedModuleStyle;
		this.setVertex(true);
		this.setConnectable(false);
	}

	/**
	 * cells is included for informative purposes mxGeometry is required, but
	 * overwritten later in the call-chain
	 * 
	 * @param cells
	 */
	public ModuleCell(Object[] cells)
	{
		this(OWLManager.getOWLDataFactory().getOWLEntity(DEFAULT_TYPE, DEFAULT_IRI));
	}

	public ModuleCell(OWLEntity owlEntity, double positionX, double positionY)
	{
		this(owlEntity);
		// WIDTH and HEIGHT (the two zeroes) need to be set somehow
		this.geometry = new mxGeometry(positionX, positionY, 100, 100);
	}

	public void switchStyle()
	{
		String currentStyle = super.getStyle();

		if (currentStyle.contentEquals(ModuleCell.unfoldedModuleStyle))
		{
			super.setStyle(ModuleCell.foldedModuleStyle);
		}
		else
		{
			super.setStyle(ModuleCell.unfoldedModuleStyle);
		}
	}

	@Override
	public boolean isModule()
	{
		return true;
	}

	@Override
	public IRI defaultIRI()
	{
		return DEFAULT_IRI;
	}
}
