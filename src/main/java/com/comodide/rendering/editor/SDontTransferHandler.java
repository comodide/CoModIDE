package com.comodide.rendering.editor;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.patterns.Pattern;
import com.comodide.patterns.PatternTransferable;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxGraphTransferHandler;
import com.mxgraph.swing.util.mxGraphTransferable;

public class SDontTransferHandler extends mxGraphTransferHandler
{
	/** Bookkeeping */
	private static final long   serialVersionUID = 1L;
	private static final Logger log              = LoggerFactory.getLogger(SDontTransferHandler.class);

	/** OWLAPI Integration */
	private OWLModelManager modelManager;

	public SDontTransferHandler(OWLModelManager modelManager)
	{
		super();
		this.modelManager = modelManager;
	}

	/**
	 * Overrides {@link mxGraphTransferHandler#canImport(JComponent, DataFlavor[])},
	 * adding support for {@link PatternTransferable#dataFlavor}.
	 */
	@Override
	public boolean canImport(JComponent comp, DataFlavor[] flavors)
	{
		for (int i = 0; i < flavors.length; i++)
		{
			if (flavors[i] != null && (flavors[i].equals(mxGraphTransferable.dataFlavor))
					|| (flavors[i].equals(PatternTransferable.dataFlavor)))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the mxGraphTransferable data flavour is supported and calls
	 * importGraphTransferable if possible.
	 */
	@Override
	public boolean importData(JComponent c, Transferable t)
	{
		boolean result = false;

		if (isLocalDrag())
		{
			// Enables visual feedback on the Mac
			result = true;
		}
		else if (t.isDataFlavorSupported(PatternTransferable.dataFlavor))
		{
			try
			{
				PatternTransferable pt              = (PatternTransferable) t
						.getTransferData(PatternTransferable.dataFlavor);
				Pattern             pattern         = pt.getPattern();
				OWLOntology         patternOntology = pt.getPatternOntology();
				log.debug(String.format("The pattern '%s' with OWL ontology '%s' was dropped.", pattern.getLabel(),
						patternOntology.getOntologyID().getOntologyIRI().orNull()));

				// Clone pattern axioms into active ontology.
				// This is probably ugly and could be done in a more OWLAPI-ish way
				OWLOntology    activeOntology = modelManager.getActiveOntology();
				List<AddAxiom> newAxioms      = new ArrayList<AddAxiom>();
				for (OWLAxiom patternAxiom : patternOntology.getAxioms())
				{
					newAxioms.add(new AddAxiom(activeOntology, patternAxiom));
				}
				modelManager.applyChanges(newAxioms);
				log.debug(String.format("%s axioms from the pattern '%s' were added to ontology '%s'.",
						newAxioms.size(), patternOntology.getOntologyID().getOntologyIRI().orNull(),
						activeOntology.getOntologyID().getOntologyIRI().orNull()));
			}
			catch (Exception ex)
			{
				log.error("Failed to import pattern.");
				ex.printStackTrace();
			}
		}
		else
		{
			try
			{
				updateImportCount(t);

				if (c instanceof mxGraphComponent)
				{
					mxGraphComponent graphComponent = (mxGraphComponent) c;

					if (graphComponent.isEnabled() && t.isDataFlavorSupported(mxGraphTransferable.dataFlavor))
					{
						mxGraphTransferable gt = (mxGraphTransferable) t
								.getTransferData(mxGraphTransferable.dataFlavor);

						if (gt.getCells() != null)
						{
							result = importGraphTransferable(graphComponent, gt);
						}
					}
				}
			}
			catch (Exception ex)
			{
				log.warn("[CoModIDE:sdTransferHandler] Failed to import data", ex);
			}
		}

		return result;
	}
}
