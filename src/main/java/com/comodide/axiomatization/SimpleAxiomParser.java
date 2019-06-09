package com.comodide.axiomatization;

import java.util.Map;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.HasFiller;
import org.semanticweb.owlapi.model.HasProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.rendering.editor.SchemaDiagram;
import com.mxgraph.model.mxGraphModel;

public class SimpleAxiomParser
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(SimpleAxiomParser.class);

	/** Used for deriving human readable labels */
	private static final ShortFormProvider shortFormProvider = new SimpleShortFormProvider();

	/** Reference to the SchemaDiagram that is being displayed */
	// TODO this might break with multiple ontologies D:
	private SchemaDiagram schemaDiagram;

	/** Empty Constructor */
	public SimpleAxiomParser()
	{

	}

	public SimpleAxiomParser(SchemaDiagram schemaDiagram)
	{
		this.schemaDiagram = schemaDiagram;
	}

	// @formatter:off
	/**
	 * This method is only capable of parsing axioms of the following forms 
	 * 
	 * A \sqsubseteq B 
	 * A \sqsubseteq \forall R.B 
	 * A \sqsubseteq \exists R.B 
	 * \forall R.A \sqsubseteq B 
	 * \exists R.A \sqsubseteq B
	 * 
	 * Using this on complex axioms will throw big errors :(
	 * 
	 * @return
	 */
	// @formatter:on
	public EdgeContainer parseSimpleAxiom(OWLSubClassOfAxiom axiom)
	{
		// Edge to return
		EdgeContainer edge;

		OWLClassExpression left  = axiom.getSubClass();
		OWLClassExpression right = axiom.getSuperClass();

		// Atomic subclass relationship (
		if (left.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS)
				&& right.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS))
		{
			edge = atomicSubclass(axiom, left, right);
		}
		else if (left.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS)
				&& !right.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS))
		{
			edge = rightComplex(axiom, left, right);
		}
		else if (!left.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS)
				&& right.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS))
		{
			edge = leftComplex(axiom, left, right);
		}
		else
		{
			log.warn("[CoModIDE:SimpleAxiomParser] Rendering of the axiom is not supported:"
					+ axiom.getAxiomWithoutAnnotations().toString());
			edge = null;
		}

		return edge;
	}

	private EdgeContainer atomicSubclass(OWLAxiom axiom, OWLClassExpression left, OWLClassExpression right)
	{
		EdgeContainer subclassEdge = null;

		// Extract classes
		OWLClass leftClass  = left.asOWLClass();
		OWLClass rightClass = right.asOWLClass();

		// Get the shortforms. In node creation, these are the IDs
		String leftLabel  = shortFormProvider.getShortForm(leftClass);
		String rightlabel = shortFormProvider.getShortForm(rightClass);

		// Obtain associated cells from labels (which are ids)
		Map<String, Object> cells     = ((mxGraphModel) this.schemaDiagram.getModel()).getCells();
		Object              leftcell  = cells.get(leftLabel);
		Object              rightcell = cells.get(rightlabel);

		// Package
		subclassEdge = new EdgeContainer("subclass", axiom, leftcell, rightcell, "subclassStyle");

		return subclassEdge;
	}

	private EdgeContainer leftComplex(OWLAxiom axiom, OWLClassExpression left, OWLClassExpression right)
	{
		EdgeContainer relationEdge = null;

		/* Parse Left */
		// Extract Property
		OWLEntity property = (OWLEntity) ((HasProperty<?>) left).getProperty();
		// Extract Class
		OWLClass leftClass = (OWLClass) ((HasFiller<?>) left).getFiller();
		// Extract Right Class
		OWLClass rightClass = right.asOWLClass();

		// Get the shortforms
		String propertyLabel = shortFormProvider.getShortForm(property);
		String leftLabel     = shortFormProvider.getShortForm(leftClass);
		String rightLabel    = shortFormProvider.getShortForm(rightClass);

		// Obtain associated cells using the labels
		Map<String, Object> cells     = ((mxGraphModel) this.schemaDiagram.getModel()).getCells();
		Object              leftCell  = cells.get(leftLabel);
		Object              rightCell = cells.get(rightLabel);

		relationEdge = new EdgeContainer(propertyLabel, axiom, leftCell, rightCell, "standardStyle");
		return relationEdge;
	}

	private EdgeContainer rightComplex(OWLAxiom axiom, OWLClassExpression left, OWLClassExpression right)
	{
		EdgeContainer relationEdge = null;

		// Extract left Class
		OWLClass leftClass = left.asOWLClass();
		/* Parse Right */
		// Extract Property
		OWLEntity property = (OWLEntity) ((HasProperty<?>) right).getProperty();
		// Extract Class
		OWLClass rightClass = (OWLClass) ((HasFiller<?>) right).getFiller();

		// Get the shortforms
		String propertyLabel = shortFormProvider.getShortForm(property);
		String leftLabel     = shortFormProvider.getShortForm(leftClass);
		String rightLabel    = shortFormProvider.getShortForm(rightClass);

		// Obtain associated cells using the labels
		Map<String, Object> cells     = ((mxGraphModel) this.schemaDiagram.getModel()).getCells();
		Object              leftCell  = cells.get(leftLabel);
		Object              rightCell = cells.get(rightLabel);

		relationEdge = new EdgeContainer(propertyLabel, axiom, leftCell, rightCell, "standardStyle");
		return relationEdge;
	}
}
