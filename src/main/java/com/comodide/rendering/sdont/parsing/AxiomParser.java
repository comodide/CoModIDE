package com.comodide.rendering.sdont.parsing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.HasFiller;
import org.semanticweb.owlapi.model.HasProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.rendering.PositioningOperations;
import com.comodide.rendering.sdont.model.SDEdge;
import com.comodide.rendering.sdont.model.SDEdgeFactory;
import com.comodide.rendering.sdont.model.SDNode;

public class AxiomParser
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(AxiomParser.class);

	/** Used for deriving human readable labels */
	private static final ShortFormProvider shortFormProvider = new SimpleShortFormProvider();

	/** Connects to an OWLOntology and provides some convenience methods. */
	private OWLConnector connector;

	/** Creates new entities/axioms */
	private OWLDataFactory df;

	private Set<SDEdge>   edgeSet = null;
	private SDEdgeFactory edgeFactory;


	public AxiomParser(OWLConnector connector)
	{
		this.connector = connector;
		this.df = connector.getDataFactory();
	}

	public Set<SDEdge> provideEdges(Set<SDNode> nodeSet)
	{
		if (this.edgeSet == null)
		{
			this.edgeSet = new HashSet<>();
			this.edgeFactory = new SDEdgeFactory(nodeSet);

			parseObjectProperties();
			parseDataProperties();
			parseAxioms();
		}

		return this.edgeSet;
	}

	private void parseObjectProperties()
	{
		List<OWLObjectProperty> objectProperties = this.connector.retrieveObjectProperties();

		objectProperties.forEach(objProp -> {
			parseObjectProperty(objProp);
		});
	}

	private void parseObjectProperty(OWLObjectProperty objProp)
	{
		// Get the axioms related to this particular property
		List<OWLObjectPropertyAxiom> objPropAxioms = this.connector.retreiveAxiomsRelatedToObjProp(objProp);
		// Map the type of the axiom to the axiom, so that we
		// can find exactly the domain and range
		HashMap<AxiomType<?>, OWLObjectPropertyAxiom> objPropMap = new HashMap<>();

		// Populate the map
		objPropAxioms.forEach(objPropAx -> {
			objPropMap.put(objPropAx.getAxiomType(), objPropAx);
		});

		// Get the domain and range of the property
		OWLObjectPropertyDomainAxiom domain = (OWLObjectPropertyDomainAxiom) objPropMap
				.get(AxiomType.OBJECT_PROPERTY_DOMAIN);
		OWLObjectPropertyRangeAxiom  range  = (OWLObjectPropertyRangeAxiom) objPropMap
				.get(AxiomType.OBJECT_PROPERTY_RANGE);

		// As long we have both the domain and range (i.e. the objProp is not
		// malformed) parse the axiom
		if (domain != null && range != null)
		{
			OWLClassExpression superClass = df.getOWLObjectSomeValuesFrom(objProp, range.getRange());
			parseAxiom(df.getOWLSubClassOfAxiom(domain.getDomain(), superClass));
		}
	}

	private void parseDataProperties()
	{
		List<OWLDataProperty> dataProperties = this.connector.retrieveDataProperties();
		
		for (OWLDataProperty dataProperty: dataProperties) {
			Set<OWLDataPropertyDomainAxiom> domainAxioms = this.connector.getOntology().getDataPropertyDomainAxioms(dataProperty);
			Set<OWLDataPropertyRangeAxiom> rangeAxioms = this.connector.getOntology().getDataPropertyRangeAxioms(dataProperty);
			
			if (domainAxioms.size() != 1 || rangeAxioms.size() != 1) {
				log.info(String.format("[CoModIDE:AxiomParser] Property '%s' cannot be processed as it does not have exactly 1 domain and range.", dataProperty.toString()));
			}
			else {
				// Get the things we will be linking up
				OWLClass domain = ((OWLDataPropertyDomainAxiom) domainAxioms.toArray()[0]).getDomain().asOWLClass();
				String domainLabel = shortFormProvider.getShortForm(domain);
				OWLDatatype range = ((OWLDataPropertyRangeAxiom) rangeAxioms.toArray()[0]).getRange().asOWLDatatype();
				String rangeLabel = shortFormProvider.getShortForm(range);
				Pair<Double,Double> dataPropertyCoordinates = PositioningOperations.getXYCoordsForEntity(dataProperty, this.connector.getOntology());
				
				SDEdge edge = edgeFactory.makeSDEdgeForDataProperty(domainLabel, rangeLabel, false, dataProperty, dataPropertyCoordinates);
				if (edge != null) {
					this.edgeSet.add(edge);
				}
			}
		}		
	}

	private void parseAxioms()
	{
		List<OWLSubClassOfAxiom> axioms = this.connector.retrieveSubClassAxioms();

		axioms.forEach(axiom -> {
			parseAxiom(axiom);
		});
	}

	public void parseAxiom(OWLSubClassOfAxiom ax)
	{
		// Parse SubClass
		OWLClassExpression  sub  = ax.getSubClass();
		ClassExpressionType subt = sub.getClassExpressionType();

		// Parse SuperClass
		OWLClassExpression  sup  = ax.getSuperClass();
		ClassExpressionType supt = sup.getClassExpressionType();

		try
		{
			// Atomic Subclass Relation
			if (subt.equals(ClassExpressionType.OWL_CLASS) && subt.equals(supt))
			{
				String src = shortFormProvider.getShortForm((OWLEntity) sub);
				String tar = shortFormProvider.getShortForm((OWLEntity) sup);

				Triple t = new Triple(src, tar, ax);
				addEdge(t);
			}
			// Complex \sqsubseteq Class
			else if (sub instanceof HasFiller<?> && isClass(supt))
			{
				Triple t = handleComplexAndClass(sub, sup, ax);
				addEdge(t);
			}
			// Class \sqsubseteq Complex
			else if (isClass(subt) && sup instanceof HasFiller<?>)
			{
				Triple t = handleClassAndComplex(sub, sup, ax);
				addEdge(t);
			}
			else if (sub instanceof HasFiller<?> && sup instanceof OWLObjectUnionOf)
			{
				Set<OWLClassExpression> union = sup.asDisjunctSet();

				for (OWLClassExpression c : union)
				{
					parseAxiom(df.getOWLSubClassOfAxiom(sub, c));
				}
			}
			else if (sub instanceof OWLObjectIntersectionOf && sup instanceof HasFiller<?>)
			{
				Set<OWLClassExpression> intersection = sub.asConjunctSet();

				for (OWLClassExpression c : intersection)
				{
					parseAxiom(df.getOWLSubClassOfAxiom(c, sup));
				}
			}
			else if (sub instanceof OWLObjectUnionOf)
			{
				sub.asDisjunctSet().forEach(u -> parseAxiom(df.getOWLSubClassOfAxiom(u, sup)));
			}
			else if (sup instanceof OWLObjectUnionOf)
			{
				sup.asDisjunctSet().forEach(d -> parseAxiom(df.getOWLSubClassOfAxiom(d, sub)));
			}
			else if (sup instanceof OWLObjectIntersectionOf)
			{
				sup.asConjunctSet().forEach(c -> parseAxiom(df.getOWLSubClassOfAxiom(sub, c)));
			}
			else // weird stuff
			{
				throw new UnparseableAxiomException("Completely unhandled axiom occurred.");
			}
		}
		catch (UnparseableAxiomException e)
		{
//			System.err.println("Unparseable axiom encountered: ");
//			System.err.println(ax.getAxiomWithoutAnnotations().toString());
//			System.err.println("Message: " + e.getMessage() + "\n");
			log.warn("[CoModIDE:SDOnt] Unparseable axiom encountered.");
			log.warn("\tMessage: " + e.getMessage() + "\n");
		}
		catch (ClassCastException e)
		{
//			System.err.println("Problem parsing axiom: ");
//			System.err.println(ax.getAxiomWithoutAnnotations().toString());
//			System.err.println("Message: " + e.getMessage() + "\n");
			log.warn("[CoModIDE:SDOnt] Problem parsing axiom.");
			log.warn("\tMessage: " + e.getMessage() + "\n");
		}
	}

	//////////////////////////////////////////////////////////////////////
	public Triple handleClassAndComplex(OWLClassExpression sub, OWLClassExpression sup, OWLAxiom ax)
			throws UnparseableAxiomException
	{
		String domStr;
		String codStr;
		String rolStr;

		// Get domain shortform
		domStr = shortFormProvider.getShortForm((OWLEntity) sub);

		// Get filler shortform (codomain)
		OWLObject filler = ((HasFiller<?>) sup).getFiller();
		// Get the property
		OWLObjectProperty prop = (OWLObjectProperty) ((HasProperty<?>) sup).getProperty();
		// TODO: this if clause will probably be a problem, later
		if (filler instanceof OWLObjectIntersectionOf)
		{
			OWLObjectIntersectionOf inter = (OWLObjectIntersectionOf) filler;
			Set<?>                  set   = inter.asConjunctSet();
			if (set.size() != 2)
			{
				throw new UnparseableAxiomException();
			}
			else
			{
				OWLClassExpression[] arr = new OWLClassExpression[2];
				set.toArray(arr);
				OWLClass cod;

				if (arr[0] instanceof OWLClass)
				{
					parseAxiom(df.getOWLSubClassOfAxiom(arr[0], arr[1]));
					cod = (OWLClass) arr[0];
				}
				else
				{
					parseAxiom(df.getOWLSubClassOfAxiom(arr[1], arr[0]));
					cod = (OWLClass) arr[1];
				}

				codStr = shortFormProvider.getShortForm((OWLEntity) cod);
			}
		}
		else if (filler instanceof OWLObjectUnionOf)
		{
			// Consider DetectorFinalState \sqsubclasseq \exists hO(A\cup O\cup
			// SC)
			// We need to make an axiom for each of these

			// Get the filler
			Set<OWLClassExpression> set = ((OWLObjectUnionOf) filler).asDisjunctSet();
			for (OWLClassExpression s : set)
			{
				OWLClassExpression newSup;

				if (sup instanceof OWLObjectSomeValuesFrom)
				{
					newSup = df.getOWLObjectSomeValuesFrom(prop, s);
				}
				else
				{
					newSup = df.getOWLObjectAllValuesFrom(prop, s);
				}

				parseAxiom(df.getOWLSubClassOfAxiom(sub, newSup));
			}

			codStr = shortFormProvider.getShortForm((OWLEntity) set.iterator().next());
		}
		else
		{
			codStr = shortFormProvider.getShortForm((OWLEntity) ((HasFiller<?>) sup).getFiller());
		}

		// Get Property shortform
		rolStr = shortFormProvider.getShortForm((OWLEntity) ((HasProperty<?>) sup).getProperty());

		return new Triple(domStr, codStr, rolStr, prop);
	}

	//////////////////////////////////////////////////////////////////////
	public Triple handleComplexAndClass(OWLClassExpression sub, OWLClassExpression sup, OWLAxiom ax)
			throws UnparseableAxiomException
	{
		String domStr;
		String codStr;
		String rolStr;

		// Get domain shortform
		domStr = shortFormProvider.getShortForm((OWLEntity) sup);

		// Get filler shortform (codomain)
		OWLObject filler = ((HasFiller<?>) sub).getFiller();
		// Get the property
		OWLObjectProperty prop = (OWLObjectProperty) ((HasProperty<?>) sub).getProperty();

//		OWLObjectProperty prop = null;
		if (filler instanceof OWLObjectIntersectionOf)
		{
			OWLObjectIntersectionOf inter = (OWLObjectIntersectionOf) filler;
			Set<?>                  set   = inter.asConjunctSet();
			if (set.size() != 2)
			{
				throw new UnparseableAxiomException("Wrong intersection set size");
			}
			else
			{
				OWLClassExpression[] arr = new OWLClassExpression[2];
				set.toArray(arr);
				OWLClass cod;

				if (arr[0] instanceof OWLClass)
				{
					parseAxiom(df.getOWLSubClassOfAxiom(arr[0], arr[1]));
					cod = (OWLClass) arr[0];
				}
				else
				{
					parseAxiom(df.getOWLSubClassOfAxiom(arr[1], arr[0]));
					cod = (OWLClass) arr[1];
				}

				codStr = shortFormProvider.getShortForm((OWLEntity) cod);
			}
		}
		else if (filler instanceof OWLObjectUnionOf)
		{
			// Get the filler
			Set<OWLClassExpression> set = ((OWLObjectUnionOf) filler).asDisjunctSet();
			for (OWLClassExpression s : set)
			{
				if (sub instanceof OWLObjectSomeValuesFrom)
				{
					OWLObjectSomeValuesFrom newSub = df.getOWLObjectSomeValuesFrom(prop, s);
					parseAxiom(df.getOWLSubClassOfAxiom(newSub, sup));
				}
				else
				{
					throw new UnparseableAxiomException("Failure at union in filler in sub");
				}
			}

			codStr = shortFormProvider.getShortForm((OWLEntity) set.iterator().next());
		}
		else
		{
			codStr = shortFormProvider.getShortForm((OWLEntity) ((HasFiller<?>) sub).getFiller());
		}

		// Get Property shortform

		rolStr = shortFormProvider.getShortForm((OWLEntity) ((HasProperty<?>) sub).getProperty());

		return new Triple(domStr, codStr, rolStr, prop);
	}

	/////////////////
	// Helper Functions
	/////////////////
	private boolean isClass(ClassExpressionType cet)
	{
		return cet.equals(ClassExpressionType.OWL_CLASS);
	}

	private SDEdge addEdge(Triple t)
	{
		String from = t.getFr();
		String to = t.getTo();
		boolean isSubClass = t.isSubClass();
		OWLProperty owlProperty = t.wraps();
		SDEdge edge = null;
		if (owlProperty != null && owlProperty.isDataPropertyExpression()) {
			Pair<Double,Double> datatypeCoordinates = PositioningOperations.getXYCoordsForEntity(owlProperty, this.connector.getOntology());
			edge = edgeFactory.makeSDEdgeForDataProperty(from, to, isSubClass, owlProperty, datatypeCoordinates); 
		}
		else {
			edge = edgeFactory.makeSDEdge(from, to, isSubClass, owlProperty);
		}
		if (edge != null) {
			this.edgeSet.add(edge);
		}
		return edge;
	}
}
