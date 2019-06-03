package com.comodide.rendering.editor;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.patterns.Pattern;
import com.comodide.patterns.PatternInstantiationConfiguration;
import com.comodide.patterns.PatternTransferable;
import com.google.common.base.Optional;
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
				PatternTransferable pt = (PatternTransferable) t.getTransferData(PatternTransferable.dataFlavor);
				Pattern pattern = pt.getPattern();
				Set<OWLAxiom> instantiationAxioms = pt.getInstantiationAxioms();
				Set<OWLAxiom> modularizationAnnotationAxioms = pt.getModularisationAnnotationAxioms();
				
				log.debug(String.format("The pattern '%s' with OWL ontology '%s' was dropped.", pattern.getLabel(), pattern.getIri().toString()));

				// Clone pattern axioms into active ontology.
				OWLOntology activeOntology = modelManager.getActiveOntology();
				List<OWLOntologyChange> newAxioms = new ArrayList<OWLOntologyChange>();
				for (OWLAxiom instantiationAxiom : instantiationAxioms)
				{
					newAxioms.add(new AddAxiom(activeOntology, instantiationAxiom));
				}
				
				// Depending on user configuration, add modularization axioms either to separate metadata ontology or directly
				// to target ontology
				if (PatternInstantiationConfiguration.getModuleMetadataExternal()) {
					IRI activeOntologyIRI = activeOntology.getOntologyID().getOntologyIRI().orNull();
					// The metadata ontology has the same IRI as the main ontology, but with a -metadata ending
					// TODO: this assumption may be dangerous, consider the consequences
					IRI metadataOntologyIRI = IRI.create(activeOntologyIRI.toString(), "-metadata");
					Set<OWLOntology> allOntologies = modelManager.getOntologies();
					
					// Iterate over all loaded ontologies, to find the metadata ontology if it already exists
					OWLOntology metadataOntology = null; 
					for (OWLOntology ont: allOntologies) {
						if (ont.getOntologyID().getOntologyIRI().orNull().equals(metadataOntologyIRI)) {
							metadataOntology = ont;
							log.info("Found existing metadata ontology: '" + metadataOntology.toString() + "'");
							break;
						}
					}
					if (metadataOntology == null) {
						
						// Get and copy existing storage path on disk, injecting "-metadata".
						URI activeOntologyPhysicalURI = modelManager.getOntologyPhysicalURI(activeOntology);

						String scheme = activeOntologyPhysicalURI.getScheme();
						String authority = activeOntologyPhysicalURI.getAuthority();
						String path = activeOntologyPhysicalURI.getPath();
						
						// Fallback solution if no file ending exists
						String newPath = path + "-metadata.rdf";
						
						// If there is a file ending, inject the -metadata
						if (path.lastIndexOf(".") != -1) {
							String pathEnding = path.substring(path.lastIndexOf("."));
							newPath = path.replace(pathEnding, ("-metadata" + pathEnding));
						}
						
						URI metadataOntologyPhysicalURI = new URI(String.format("%s://%s%s", scheme, authority, newPath));
						log.info("Physical URI for new metadata ontology = " + metadataOntologyPhysicalURI.toString());
						
						//String metaDataOntologyPath =  activeOntologyPhysicalPath.replace("", replacement)
						Optional<IRI> optionalMetadataOntologyIri = Optional.of(metadataOntologyIRI);
						Optional<IRI> optionalMetadataOntologyVersionIri = Optional.absent();
						OWLOntologyID metadataOid = new OWLOntologyID(optionalMetadataOntologyIri, optionalMetadataOntologyVersionIri);
						metadataOntology = modelManager.createNewOntology(metadataOid, metadataOntologyPhysicalURI);
						log.info("Created new metadata ontology '" + metadataOntology.toString() + "'");
						
						// Add import of active ontology to metadata ontology
						OWLDataFactory factory = metadataOntology.getOWLOntologyManager().getOWLDataFactory();
						OWLImportsDeclaration importsDec = factory.getOWLImportsDeclaration(activeOntologyIRI);
						AddImport ai = new AddImport(metadataOntology, importsDec);
						newAxioms.add(ai);
					}
					
					
					// Add modularization axioms to metadata ontology
					for (OWLAxiom modularizationAnnotationAxiom : modularizationAnnotationAxioms)
					{
						newAxioms.add(new AddAxiom(metadataOntology, modularizationAnnotationAxiom));
					}
					
				}
				else {
					// Add modularization axioms to target ontology
					for (OWLAxiom modularizationAnnotationAxiom : modularizationAnnotationAxioms)
					{
						newAxioms.add(new AddAxiom(activeOntology, modularizationAnnotationAxiom));
					}
				}
				modelManager.applyChanges(newAxioms);
				
				log.debug(String.format("%s axioms from the pattern '%s' were added to ontology '%s'.",
						newAxioms.size(), pattern.getIri().toString(),
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
