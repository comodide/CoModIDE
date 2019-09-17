package com.comodide.editor;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

import org.apache.commons.lang3.tuple.Pair;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.ComodideConfiguration;
import com.comodide.patterns.Pattern;
import com.comodide.patterns.PatternTransferable;
import com.comodide.rendering.PositioningOperations;
import com.google.common.base.Optional;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxGraphTransferHandler;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;

public class SDTransferHandler extends mxGraphTransferHandler
{
	/** Bookkeeping */
	private static final long serialVersionUID = 1L;

	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(SDTransferHandler.class);

	/** OWLAPI Integration */
	private OWLModelManager modelManager;

	/** Empty Constructor */
	public SDTransferHandler()
	{

	}

	public SDTransferHandler(OWLModelManager modelManager)
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
				// Extract from TransferHandler
				PatternTransferable pt = (PatternTransferable) t.getTransferData(PatternTransferable.dataFlavor);
				// Unpack from Transferable
				Pattern       pattern                        = pt.getPattern();
				Set<OWLAxiom> instantiationAxioms            = pt.getInstantiationAxioms();
				Set<OWLAxiom> modularizationAnnotationAxioms = pt.getModularisationAnnotationAxioms();
				// Axioms should be sorted, i.e. declarations, then GCI, etc.
				// This allows nodes to be rendered, then edges
				// Sets are unordered, so create List first.
				ArrayList<OWLAxiom> sortedInstantiationAxioms = new ArrayList<OWLAxiom>(instantiationAxioms);
				Collections.sort(sortedInstantiationAxioms); // In place sorting using OWLAPI default comparators

				// Clone pattern axioms into active ontology.
				OWLOntology             activeOntology = modelManager.getActiveOntology();
				List<OWLOntologyChange> newAxioms      = new ArrayList<OWLOntologyChange>();
				for (OWLAxiom instantiationAxiom : sortedInstantiationAxioms)
				{
					newAxioms.add(new AddAxiom(activeOntology, instantiationAxiom));
				}

				for (OWLAxiom instantiationAxiom : sortedInstantiationAxioms)
				{
					if (instantiationAxiom.isOfType(AxiomType.DECLARATION))
					{
						OWLDeclarationAxiom oda    = (OWLDeclarationAxiom) instantiationAxiom;
						OWLEntity           entity = oda.getEntity();

						if (entity.isOWLClass() || entity.isOWLDataProperty())
						{
							PositioningOperations.calculateDropLocationAnnotations(activeOntology, pattern, entity,
									getScaledDropLocation((SchemaDiagramComponent) c));
						}
					}
				}

				// Depending on user configuration, add modularization axioms either to separate
				// metadata ontology or directly to target ontology
				if (ComodideConfiguration.getModuleMetadataExternal())
				{
					IRI activeOntologyIRI = activeOntology.getOntologyID().getOntologyIRI().orNull();
					// The metadata ontology has the same IRI as the main ontology, but with a
					// -metadata ending
					IRI              metadataOntologyIRI = IRI.create(activeOntologyIRI.toString(), "-metadata");
					Set<OWLOntology> allOntologies       = modelManager.getOntologies();

					// Iterate over all loaded ontologies, to find the metadata ontology if it
					// already exists
					OWLOntology metadataOntology = null;
					for (OWLOntology ont : allOntologies)
					{
						if (ont.getOntologyID().getOntologyIRI().orNull().equals(metadataOntologyIRI))
						{
							metadataOntology = ont;
							break;
						}
					}

					// If the metadata ontology was not found
					if (metadataOntology == null)
					{
						// Get and copy existing storage path on disk, injecting "-metadata".
						URI activeOntologyPhysicalURI = modelManager.getOntologyPhysicalURI(activeOntology);

						String scheme    = activeOntologyPhysicalURI.getScheme();
						String authority = activeOntologyPhysicalURI.getAuthority();
						String path      = activeOntologyPhysicalURI.getPath();

						// Fallback solution if no file ending exists
						String newPath = path + "-metadata.rdf";

						// If there is a file ending, inject the -metadata
						if (path.lastIndexOf(".") != -1)
						{
							String pathEnding = path.substring(path.lastIndexOf("."));
							newPath = path.replace(pathEnding, ("-metadata" + pathEnding));
						}

						URI metadataOntologyPhysicalURI = new URI(
								String.format("%s://%s%s", scheme, authority, newPath));

						Optional<IRI> optionalMetadataOntologyIri        = Optional.of(metadataOntologyIRI);
						Optional<IRI> optionalMetadataOntologyVersionIri = Optional.absent();
						OWLOntologyID metadataOid                        = new OWLOntologyID(
								optionalMetadataOntologyIri, optionalMetadataOntologyVersionIri);
						metadataOntology = modelManager.createNewOntology(metadataOid, metadataOntologyPhysicalURI);

						// Add import of active ontology to metadata ontology
						OWLDataFactory        factory    = metadataOntology.getOWLOntologyManager().getOWLDataFactory();
						OWLImportsDeclaration importsDec = factory.getOWLImportsDeclaration(activeOntologyIRI);
						AddImport             ai         = new AddImport(metadataOntology, importsDec);
						newAxioms.add(ai);
					}

					// Add modularization axioms to metadata ontology
					for (OWLAxiom modularizationAnnotationAxiom : modularizationAnnotationAxioms)
					{
						newAxioms.add(new AddAxiom(metadataOntology, modularizationAnnotationAxiom));
					}
				}
				else // (i.e. add directly to active ontology)
				{
					// Add modularization axioms to target ontology
					for (OWLAxiom modularizationAnnotationAxiom : modularizationAnnotationAxioms)
					{
						newAxioms.add(new AddAxiom(activeOntology, modularizationAnnotationAxiom));
					}
				}

				modelManager.applyChanges(newAxioms);

				result = true;
			}
			catch (Exception ex)
			{
				log.error("[CoModIDE:sdTransferHandler] Failed to import pattern.");
				ex.printStackTrace();
			}
		}
		else // (e.g. a cell)
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

	private Pair<Double, Double> getScaledDropLocation(mxGraphComponent graphComponent)
	{
		mxGraph     graph  = graphComponent.getGraph();
		double      scale  = graph.getView().getScale();
		double      dx     = 0, dy = 0;

		mxPoint translate = graph.getView().getTranslate();

		dx = location.getX() - translate.getX() * scale;
		dy = location.getY() - translate.getY() * scale;

		// Keeps the cells aligned to the grid
		dx = graph.snap(dx / scale);
		dy = graph.snap(dy / scale);

		return Pair.of(dx, dy);
	}
}
