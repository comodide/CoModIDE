package com.comodide.editor;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
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
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.axiomatization.MetadataUtils;
import com.comodide.configuration.ComodideConfiguration;
import com.comodide.patterns.PatternTransferable;
import com.comodide.rendering.PositioningOperations;
import com.comodide.telemetry.TelemetryAgent;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxGraphTransferHandler;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;

import uk.ac.manchester.cs.owl.owlapi.OWLAnnotationAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplDouble;

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
				Pair<Double, Double> dropLocation = getScaledDropLocation((SchemaDiagramComponent) c);
				// Extract from TransferHandler
				PatternTransferable pt = (PatternTransferable) t.getTransferData(PatternTransferable.dataFlavor);
				// Unpack from Transferable
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

					// The below code replaces positioning annotation assertions from the dropped
					// pattern by new ones that take into account the offset caused by the drop
					// position on the canvas
					if (instantiationAxiom.isOfType(AxiomType.ANNOTATION_ASSERTION))
					{
						OWLAnnotationAssertionAxiom annotationAxiom = (OWLAnnotationAssertionAxiom) instantiationAxiom;
						OWLAnnotationSubject        subject         = annotationAxiom.getSubject();
						OWLAnnotationProperty       property        = annotationAxiom.getProperty();
						OWLAnnotationValue          value           = annotationAxiom.getValue();
						Set<OWLAnnotation>          annotations     = annotationAxiom.getAnnotations();

						if (property.equals(PositioningOperations.entityPositionX)
								|| property.equals(PositioningOperations.entityPositionY))
						{
							double positionOffset;
							if (property.equals(PositioningOperations.entityPositionX))
							{
								positionOffset = dropLocation.getLeft();
							}
							else
							{
								positionOffset = dropLocation.getRight();
							}
							if (value.isLiteral())
							{
								OWLLiteral valueLiteral = value.asLiteral().get();
								if (valueLiteral.isDouble())
								{
									double             sourcePosition = valueLiteral.parseDouble();
									double             newPosition    = sourcePosition + positionOffset;
									OWLAnnotationValue newValue       = new OWLLiteralImplDouble(newPosition,
											valueLiteral.getDatatype());
									instantiationAxiom = new OWLAnnotationAssertionAxiomImpl(subject, property,
											newValue, annotations);
								}
							}
						}
					}
					newAxioms.add(new AddAxiom(activeOntology, instantiationAxiom));
				}

				// Depending on user configuration, add modularization axioms either to separate
				// metadata ontology or directly to target ontology
				if (ComodideConfiguration.getModuleMetadataExternal())
				{
					OWLOntology metadataOntology  = MetadataUtils.findOrCreateMetadataOntology(modelManager);
					IRI         activeOntologyIRI = activeOntology.getOntologyID().getOntologyIRI().orNull();
					// Add import of active ontology to metadata ontology
					OWLDataFactory        factory    = metadataOntology.getOWLOntologyManager().getOWLDataFactory();
					OWLImportsDeclaration importsDec = factory.getOWLImportsDeclaration(activeOntologyIRI);
					AddImport             ai         = new AddImport(metadataOntology, importsDec);
					newAxioms.add(ai);

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
				// TODO all the schema diagram manipulations are a debug
				this.schemaDiagram.setAnnotationLock(true);
				modelManager.applyChanges(newAxioms);
				this.schemaDiagram.setAnnotationLock(false);
				TelemetryAgent.logPatternDrop();
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
