package com.comodide.patterns;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class PatternTransferable implements Transferable {

	// TODO: Actually package a suitable OWLAPI construct, not just our metadata pattern class
	private Pattern pattern;
	private DataFlavor flavor;
	
	public PatternTransferable(Pattern pattern) {
		super();
		this.pattern = pattern;
		this.flavor = new DataFlavor(Pattern.class, "Ontology Design Pattern");
	}

	@Override
	public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
		if (df.equals(this.flavor)) {
			return this.pattern;
		}
		else {
			throw new UnsupportedFlavorException(df);
		}
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] {this.flavor};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor df) {
		if (df.equals(this.flavor)) {
			return true;
		}
		else {
			return false;
		}
	}
}
