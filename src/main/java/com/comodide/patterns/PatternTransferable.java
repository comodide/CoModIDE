package com.comodide.patterns;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Transferable implementation that supports the dragging and dropping of OWLAPI-based ontology design patterns.
 * Provides its own custom DataFlavor implementation based on the {@link Pattern} POJO, which is the only DataFlavor
 * that PatternTransferable supports. 
 * 
 * @author Karl Hammar <karl@karlhammar.com>
 */
public class PatternTransferable implements Transferable {

	private Set<OWLAxiom> instantiationAxioms;
	private Set<OWLAxiom> modularisationAnnotationAxioms;
	public static DataFlavor dataFlavor;
	
	public PatternTransferable(Set<OWLAxiom> instantiationAxioms, Set<OWLAxiom> modularisationAnnotationAxioms) {
		super();
		this.instantiationAxioms = instantiationAxioms;
		this.modularisationAnnotationAxioms = modularisationAnnotationAxioms;
	}

	@Override
	public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
		if (df.equals(dataFlavor)) {
			return this;
		}
		else {
			throw new UnsupportedFlavorException(df);
		}
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] {dataFlavor};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor df) {
		if (df.equals(dataFlavor)) {
			return true;
		}
		else {
			return false;
		}
	}

	public Set<OWLAxiom> getInstantiationAxioms() {
		return instantiationAxioms;
	}

	public Set<OWLAxiom> getModularisationAnnotationAxioms() {
		return modularisationAnnotationAxioms;
	}

	static
	{
		dataFlavor = new DataFlavor(Pattern.class, "Ontology Design Pattern");
	}
}
