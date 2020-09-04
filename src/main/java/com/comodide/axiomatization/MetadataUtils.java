package com.comodide.axiomatization;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class MetadataUtils
{
	/** Bookkeeping */
	private static final Logger log = LoggerFactory.getLogger(MetadataUtils.class);
	private static final String pf = "[CoModIDE:MetadataUtils] ";

	public static OWLOntology findOrCreateMetadataOntology(OWLModelManager modelManager)
	{
		OWLOntology activeOntology    = modelManager.getActiveOntology();
		IRI         activeOntologyIRI = activeOntology.getOntologyID().getOntologyIRI().orNull();
		// The metadata ontology has the same IRI as the main ontology, but with a
		// -metadata ending
		IRI              metadataOntologyIRI = IRI.create(activeOntologyIRI.toString(), "-comodide-metadata");
		Set<OWLOntology> allOntologies       = modelManager.getOntologies();

		// Iterate over all loaded ontologies, to find the metadata ontology if it
		// already exists
		OWLOntology metadataOntology = null;
		for (OWLOntology ont : allOntologies)
		{
			if (ont.getOntologyID().getOntologyIRI().isPresent())
			{
				String ontIri = ont.getOntologyID().getOntologyIRI().get().toString();
				if (ontIri.endsWith("-comodide-metadata"))
				{
					metadataOntology = ont;
					break;
				}
			}
		}

		// If the metadata ontology was not found
		if (metadataOntology == null)
		{
			// Get and copy existing storage path on disk, injecting "-comodide-metadata".
			URI activeOntologyPhysicalURI = modelManager.getOntologyPhysicalURI(activeOntology);

			String scheme    = activeOntologyPhysicalURI.getScheme();
			String authority = activeOntologyPhysicalURI.getAuthority();
			String path      = activeOntologyPhysicalURI.getPath();

			// If there is a file ending, inject the -comodide-metadata
			String fileComponent      = path;
			String directoryComponent = "";
			// If the path contains slashes, only treat the last component
			// (to avoid false positives due to dots in directory names)
			if (path.lastIndexOf("/") != -1)
			{
				int lastSlash = path.lastIndexOf("/");
				fileComponent = path.substring(lastSlash);
				directoryComponent = path.substring(0, lastSlash);
			}
			if (fileComponent.lastIndexOf(".") != -1)
			{
				// If file ending exists, inject before it
				String fileEnding = fileComponent.substring(fileComponent.lastIndexOf("."));
				fileComponent = fileComponent.replace(fileEnding, ("-comodide-metadata" + fileEnding));
			}
			else
			{
				// Fallback solution if no file ending exists
				fileComponent = fileComponent + "-comodide-metadata.rdf";
			}
			String newPath = directoryComponent + fileComponent;

			URI           metadataOntologyPhysicalURI        = null;
			try
			{
				metadataOntologyPhysicalURI        = new URI(
						String.format("%s://%s%s", scheme, authority, newPath));
			}
			catch (URISyntaxException e1)
			{
				log.warn(pf + "Failed to create metadata ontology URI");
			}
			Optional<IRI> optionalMetadataOntologyIri        = Optional.of(metadataOntologyIRI);
			Optional<IRI> optionalMetadataOntologyVersionIri = Optional.absent();
			OWLOntologyID metadataOid                        = new OWLOntologyID(optionalMetadataOntologyIri,
					optionalMetadataOntologyVersionIri);

			try
			{
				metadataOntology = MetadataUtils.createNewOntology(metadataOid, metadataOntologyPhysicalURI, modelManager,
						activeOntology.getOWLOntologyManager());
			}
			catch (OWLOntologyCreationException e)
			{
				log.warn(pf + "Failed to create metadata ontology.");
			}
		}
		return metadataOntology;
	}

	/***
	 * This method is a copy of createNewOntology in OWLModelManager that removes
	 * the call to setActiveOntology() and the firing of the ONTOLOGY_CREATED event,
	 * such that when a new metadata ontology is added by drag and dropping a
	 * pattern with ComodideConfiguration.getModuleMetadataExternal() set to true,
	 * that new metadata ontology is just added silently, in the background.
	 * 
	 * To the best of my knowledge no important functionality depends on the
	 * ONTOLOGY_CREATED event: OWLWorkspace activates tab 0 when catching it, and
	 * OWLOntologyHierarchyProvider (an internal component in OntologyImportsWalker,
	 * instances of which would typically be transient anyway) uses it to rebuild
	 * the imports hierarchy. Neither of these features matter to CoModIDE.
	 * 
	 * @param ontologyID
	 * @param physicalURI
	 * @param modelManager
	 * @param manager
	 * @return
	 * @throws OWLOntologyCreationException
	 */
	private static OWLOntology createNewOntology(OWLOntologyID ontologyID, URI physicalURI, OWLModelManager modelManager,
			OWLOntologyManager manager) throws OWLOntologyCreationException
	{
		if (physicalURI != null && ontologyID.getDefaultDocumentIRI().isPresent())
		{
			manager.getIRIMappers()
					.add(new SimpleIRIMapper(ontologyID.getDefaultDocumentIRI().get(), IRI.create(physicalURI)));
		}
		OWLOntology ont = manager.createOntology(ontologyID);
		if (physicalURI != null)
		{
			try
			{
				File containingDirectory = new File(physicalURI).getParentFile();
				if (containingDirectory.exists())
				{
					modelManager.getOntologyCatalogManager().addFolder(containingDirectory);
				}
			}
			catch (IllegalArgumentException iae)
			{
				log.info("Cannot generate ontology catalog for ontology at {}. {}", physicalURI, iae.getMessage());
			}
		}
		return ont;
	}
}
