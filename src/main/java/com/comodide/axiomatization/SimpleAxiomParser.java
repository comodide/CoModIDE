package com.comodide.axiomatization;

import java.util.Map;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.rendering.editor.SchemaDiagram;
import com.comodide.rendering.sdont.model.SDEdge;
import com.mxgraph.model.mxGraphModel;

public class SimpleAxiomParser
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(SimpleAxiomParser.class);

	/** Used for deriving human readable labels */
	private static final ShortFormProvider	shortFormProvider	= new SimpleShortFormProvider();
	
	/** Reference to the SchemaDiagram that is being displayed */
	//TODO this might break with multiple ontologies D:
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
	public SDEdge parseSimpleAxiom(OWLSubClassOfAxiom axiom)
	{
		// Edge to return
		SDEdge edge;

		OWLClassExpression left  = axiom.getSubClass();
		OWLClassExpression right = axiom.getSuperClass();

		// Atomic subclass relationship (
		if (left.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS)
				&& right.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS))
		{
			edge = atomicSubclass(left, right);
		}
		else if (left.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS)
				&& !right.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS))
		{
			edge = rightComplex(left, right);
		}
		else if (!left.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS)
				&& right.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS))
		{
			edge = leftComplex(left, right);
		}
		else
		{
			log.warn("[CoModIDE:SimpleAxiomParser] Rendering of the axiom is not supported:"
					+ axiom.getAxiomWithoutAnnotations().toString());
			edge = null;
		}

		return edge;
	}

	private SDEdge atomicSubclass(OWLClassExpression left, OWLClassExpression right)
	{
		SDEdge subclassEdge = null;

		// Extract classes
		OWLClass leftclass = left.asOWLClass();
		OWLClass rightclass = right.asOWLClass();
		// Get the shortforms. In node creation, these are the IDs
		String leftLabel = shortFormProvider.getShortForm(leftclass);
		String rightlabel = shortFormProvider.getShortForm(rightclass);
		// Obtain associated cells from labels (which are ids)
		Map<String, Object> cells = ((mxGraphModel) this.schemaDiagram.getModel()).getCells();
		Object leftcell = cells.get(leftLabel);
		Object rightcell = cells.get(rightlabel);
		
		
		return subclassEdge;
	}

	private SDEdge leftComplex(OWLClassExpression left, OWLClassExpression right)
	{
		return null;
	}

	private SDEdge rightComplex(OWLClassExpression left, OWLClassExpression right)
	{
		return null;
	}
}
