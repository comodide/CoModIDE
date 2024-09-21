package com.comodide.editor;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.*;

import org.apache.commons.lang3.tuple.Pair;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.axiomatization.MetadataUtils;
import com.comodide.axiomatization.OplaAnnotationManager;
import com.comodide.configuration.ComodideConfiguration;
import com.comodide.patterns.PatternTransferable;
import com.comodide.rendering.PositioningOperations;
import com.comodide.telemetry.TelemetryAgent;
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

	/** For annotation locking */
	private SchemaDiagram schemaDiagram;

	/** Empty Constructor */
	public SDTransferHandler()
	{

	}

	public SDTransferHandler(OWLModelManager modelManager, SchemaDiagram schemaDiagram)
	{
		super();
		this.modelManager = modelManager;
		this.schemaDiagram = schemaDiagram;
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
		if (isLocalDrag())
		{
			// Enables visual feedback on the Mac
			return true;
		}

		if (t.isDataFlavorSupported(PatternTransferable.dataFlavor))
		{
			try
			{
				Pair<Double, Double> dropLocation = getScaledDropLocation((SchemaDiagramComponent) c);
				// Extract from TransferHandler
				PatternTransferable pt = (PatternTransferable) t.getTransferData(PatternTransferable.dataFlavor);
				// Unpack from Transferable
				Set<OWLAxiom> instantiationAxioms            = pt.getInstantiationAxioms();
				Set<OWLAxiom> modularizationAnnotationAxioms = pt.getModularisationAnnotationAxioms();
				// Axioms should be sorted, i.e. declarations, then GCI, etc.
				// This allows nodes to be rendered, then edges
				// Sets are unordered, so create List first.
				ArrayList<OWLAxiom> sortedInstantiationAxioms = new ArrayList<>(instantiationAxioms);
				Collections.sort(sortedInstantiationAxioms); // In place sorting using OWLAPI default comparators

				// Clone pattern axioms into active ontology.
				OWLOntology             activeOntology = modelManager.getActiveOntology();
				List<OWLOntologyChange> newAxioms      = new ArrayList<OWLOntologyChange>();
				for (OWLAxiom instantiationAxiom : sortedInstantiationAxioms)
				{
					// There was originally code here to calculate offsets
					// This is no longer necessary because nodes are wrapped in modules
					// and the positioning is done relative to the module.
					newAxioms.add(new AddAxiom(activeOntology, instantiationAxiom));
				}

				// Depending on user configuration, add modularization axioms either to separate
				// metadata ontology or directly to target ontology
				OWLOntology targetOntology;
				if (ComodideConfiguration.getModuleMetadataExternal())
				{
					OWLOntology metadataOntology  = MetadataUtils.findOrCreateMetadataOntology(modelManager);
					IRI         activeOntologyIRI = activeOntology.getOntologyID().getOntologyIRI().orNull();
					// Add import of active ontology to metadata ontology
					OWLDataFactory        factory    = metadataOntology.getOWLOntologyManager().getOWLDataFactory();
					OWLImportsDeclaration importsDec = factory.getOWLImportsDeclaration(activeOntologyIRI);
					AddImport             ai         = new AddImport(metadataOntology, importsDec);
					newAxioms.add(ai);

					targetOntology = metadataOntology;
				}
				else // (i.e. add directly to active ontology)
				{
					targetOntology = activeOntology;
				}

				List<OWLOntologyChange> newModularizationAxioms = new ArrayList<>();
				// Add modularization axioms to target ontology
				for (OWLAxiom modularizationAnnotationAxiom : modularizationAnnotationAxioms)
				{
					newModularizationAxioms.add(new AddAxiom(targetOntology, modularizationAnnotationAxiom));

					// This clause creates an Positioning Axiom for the newly created module
					// This is done here because the newly created module does not exist in the
					// pattern OWL file and this is the first place that the dropLocation is
					// accessible.
					if (modularizationAnnotationAxiom.isOfType(AxiomType.CLASS_ASSERTION))
					{
						OWLClassAssertionAxiom caa = (OWLClassAssertionAxiom) modularizationAnnotationAxiom;
						if (caa.getClassExpression().equals(OplaAnnotationManager.module))
						{
							OWLNamedIndividual module = caa.getIndividual().asOWLNamedIndividual();

							Double newX = dropLocation.getLeft();
							Double newY = dropLocation.getRight();
							PositioningOperations.updateXYCoordinateAnnotations(module, targetOntology, newX, newY);
						}
					}
				}

				// The annotation lock here prevents OPLa annotations from being removed
				// due to the order in which axioms are added (see corresponding use
				// in the renderActiveOntology method in ComodideEditor
				this.schemaDiagram.setAnnotationLock(true);
				modelManager.applyChanges(newAxioms);
				this.schemaDiagram.setAnnotationLock(false);

				SwingUtilities.invokeLater(() -> {
					this.schemaDiagram.setAnnotationLock(true);
					modelManager.applyChanges(newModularizationAxioms);
					this.schemaDiagram.setAnnotationLock(false);
				});

				TelemetryAgent.logPatternDrop();
				return true;
			}
			catch (Exception ex)
			{
				log.error("[CoModIDE:sdTransferHandler] Failed to import pattern.");
				ex.printStackTrace();
				return false;
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
							return importGraphTransferable(graphComponent, gt);
						}
					}
				}
			}
			catch (Exception ex)
			{
				log.warn("[CoModIDE:sdTransferHandler] Failed to import data", ex);
				return true;
			}
		}

		return false;
	}

	private Pair<Double, Double> getScaledDropLocation(mxGraphComponent graphComponent)
	{
		mxGraph graph = graphComponent.getGraph();
		double  scale = graph.getView().getScale();
		double  dx    = 0, dy = 0;

		mxPoint translate = graph.getView().getTranslate();

		dx = location.getX() - translate.getX() * scale;
		dy = location.getY() - translate.getY() * scale;

		// Keeps the cells aligned to the grid
		dx = graph.snap(dx / scale);
		dy = graph.snap(dy / scale);

		return Pair.of(dx, dy);
	}
}
