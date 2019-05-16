package com.comodide.patterns;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.semanticweb.owlapi.model.OWLOntology;

public class PatternTransferable implements Transferable {

	// TODO: Actually package a suitable OWLAPI construct, not just our metadata pattern class
	private Pattern pattern;
	private OWLOntology patternOntology;
	public static DataFlavor dataFlavor;
	
	public PatternTransferable(Pattern pattern, OWLOntology patternOntology) {
		super();
		this.pattern = pattern;
		this.patternOntology = patternOntology;
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
	
	public Pattern getPattern() {
		return pattern;
	}
	
	public OWLOntology getPatternOntology() {
		return patternOntology;
	}

	static
	{
		dataFlavor = new DataFlavor(Pattern.class, "Ontology Design Pattern");
	}
}
