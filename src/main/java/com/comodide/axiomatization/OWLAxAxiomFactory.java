package com.comodide.axiomatization;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.editor.model.ComodideCell;
import com.comodide.editor.model.PropertyEdgeCell;

public class OWLAxAxiomFactory
{
	/* Bookkeeping */
	private static final Logger log = LoggerFactory.getLogger(OWLAxAxiomFactory.class);

	/* Useful constants */
	private static OWLClass owlThing;

	private OWLModelManager modelManager;
	private OWLDataFactory  owlDataFactory;

	public OWLAxAxiomFactory(OWLModelManager modelManager)
	{
		this.modelManager = modelManager;
		this.owlDataFactory = this.modelManager.getOWLDataFactory();
		owlThing = this.owlDataFactory.getOWLThing();
	}

	public OWLAxiom createAxiomFromEdge(OWLAxAxiomType axiomType, PropertyEdgeCell edgeCell)
	{
		// Get Cells
		ComodideCell sourceCell = (ComodideCell) edgeCell.getSource();
		ComodideCell targetCell = (ComodideCell) edgeCell.getTarget();
		// Extract Entities
		OWLEntity source   = sourceCell.getEntity();
		OWLEntity target   = targetCell.getEntity();
		OWLEntity property = edgeCell.getEntity();
		// Create Axiom For edge
		return createAxiom(axiomType, source, property, target);
	}

	public OWLAxiom createAxiom(OWLAxAxiomType axiomType, OWLEntity source, OWLEntity property, OWLEntity target)
	{
		// Convert OWLEntities to Expressions (or thereabouts, technically the
		// Expression is the super class of all three
		OWLObjectPropertyExpression propertyExpression = null;
		OWLClassExpression          sourceExpression=null;
		OWLClassExpression          targetExpression=null;
		OWLDataPropertyExpression   dataPropertyExpression=null;
		OWLDatatype owlDatatype=null;

		// To be returned
		OWLAxiom owlaxAxiom=null ;

		if(property.isOWLObjectProperty()) {
			propertyExpression = property.asOWLObjectProperty();
			sourceExpression = source.asOWLClass();
			targetExpression = target.asOWLClass();
			if (axiomType == OWLAxAxiomType.GLOBAL_DOMAIN) {
				// this is just regular domain TODO change this in the enum
				OWLObjectSomeValuesFrom oosvfa = this.owlDataFactory.getOWLObjectSomeValuesFrom(propertyExpression,
						owlThing);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(oosvfa, sourceExpression);
			} else if (axiomType == OWLAxAxiomType.SCOPED_DOMAIN) {
				OWLObjectSomeValuesFrom oosvf = this.owlDataFactory.getOWLObjectSomeValuesFrom(propertyExpression,
						targetExpression);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(oosvf, sourceExpression);
			} else if (axiomType == OWLAxAxiomType.GLOBAL_RANGE) {
				OWLObjectAllValuesFrom ooavf = this.owlDataFactory.getOWLObjectAllValuesFrom(propertyExpression,
						targetExpression);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(owlThing, ooavf);
			} else if (axiomType == OWLAxAxiomType.SCOPED_RANGE) {
				OWLObjectAllValuesFrom ooavf = this.owlDataFactory.getOWLObjectAllValuesFrom(propertyExpression,
						targetExpression);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(sourceExpression, ooavf);
			} else if (axiomType == OWLAxAxiomType.EXISTENTIAL) {
				OWLObjectSomeValuesFrom oosvf = this.owlDataFactory.getOWLObjectSomeValuesFrom(propertyExpression,
						targetExpression);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(sourceExpression, oosvf);
			} else if (axiomType == OWLAxAxiomType.INVERSE_EXISTENTIAL) {
				OWLObjectPropertyExpression ope = this.owlDataFactory.getOWLObjectInverseOf(propertyExpression);
				OWLObjectSomeValuesFrom oosvf = this.owlDataFactory.getOWLObjectSomeValuesFrom(ope, sourceExpression);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(targetExpression, oosvf);
			} else if (axiomType == OWLAxAxiomType.FUNCTIONAL_ROLE) {
				OWLObjectMaxCardinality oomc = this.owlDataFactory.getOWLObjectMaxCardinality(1, propertyExpression,
						owlThing);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(owlThing, oomc);
			} else if (axiomType == OWLAxAxiomType.QUALIFIED_FUNCTIONAL_ROLE) {
				OWLObjectMaxCardinality oomc = this.owlDataFactory.getOWLObjectMaxCardinality(1, propertyExpression,
						targetExpression);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(owlThing, oomc);
			} else if (axiomType == OWLAxAxiomType.SCOPED_FUNCTIONAL_ROLE) {
				OWLObjectMaxCardinality oomc = this.owlDataFactory.getOWLObjectMaxCardinality(1, propertyExpression,
						owlThing);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(sourceExpression, oomc);
			} else if (axiomType == OWLAxAxiomType.QUALIFIED_SCOPED_FUNCTIONAL_ROLE) {
				OWLObjectMaxCardinality oomc = this.owlDataFactory.getOWLObjectMaxCardinality(1, propertyExpression,
						targetExpression);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(sourceExpression, oomc);
			} else if (axiomType == OWLAxAxiomType.INVERSE_FUNCTIONAL_ROLE) {
				OWLObjectPropertyExpression ope = this.owlDataFactory.getOWLObjectInverseOf(propertyExpression);
				OWLObjectMaxCardinality oomc = this.owlDataFactory.getOWLObjectMaxCardinality(1, ope, owlThing);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(owlThing, oomc);
			} else if (axiomType == OWLAxAxiomType.INVERSE_QUALIFIED_FUNCTIONAL_ROLE) {
				OWLObjectPropertyExpression ope = this.owlDataFactory.getOWLObjectInverseOf(propertyExpression);
				OWLObjectMaxCardinality oomc = this.owlDataFactory.getOWLObjectMaxCardinality(1, ope, targetExpression);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(owlThing, oomc);
			} else if (axiomType == OWLAxAxiomType.INVERSE_SCOPED_FUNCTIONAL_ROLE) {
				OWLObjectPropertyExpression ope = this.owlDataFactory.getOWLObjectInverseOf(propertyExpression);
				OWLObjectMaxCardinality oomc = this.owlDataFactory.getOWLObjectMaxCardinality(1, ope, owlThing);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(sourceExpression, oomc);
			} else if (axiomType == OWLAxAxiomType.INVERSE_QUALIFIED_SCOPED_FUNCTIONAL_ROLE) {
				OWLObjectPropertyExpression ope = this.owlDataFactory.getOWLObjectInverseOf(propertyExpression);
				OWLObjectMaxCardinality oomc = this.owlDataFactory.getOWLObjectMaxCardinality(1, ope, targetExpression);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(sourceExpression, oomc);
			} else if (axiomType == OWLAxAxiomType.STRUCTURAL_TAUTOLOGY) {
				// A structural tautology has the form
				// source subclassof min 0 property target
				OWLObjectMinCardinality oomc = this.owlDataFactory.getOWLObjectMinCardinality(0, propertyExpression,
						targetExpression);
				OWLSubClassOfAxiom oscoa = this.owlDataFactory.getOWLSubClassOfAxiom(sourceExpression, oomc);

				owlaxAxiom = oscoa;
			} else {
				log.error("[CoModIDE:OWLAxAxiomFactory] Unknown Axiom Type passed.");
			}

		}

		//Data Property check
		if(property.isOWLDataProperty()) {
			dataPropertyExpression = property.asOWLDataProperty();

			sourceExpression = source.asOWLClass();
			owlDatatype = target.asOWLDatatype();

			log.info("axiomType is "+axiomType);

//			if (axiomType == OWLAxAxiomType.GLOBAL_DOMAIN) {
//				// this is just regular domain TODO change this in the enum
//				OWLDataSomeValuesFrom oosvfa =this.owlDataFactory.getOWLDataSomeValuesFrom(dataPropertyExpression,owlDatatype);
//				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(oosvfa, sourceExpression);
//				log.info("axiom is data property"+owlaxAxiom);
//			} else if (axiomType == OWLAxAxiomType.SCOPED_DOMAIN) {
//				OWLDataSomeValuesFrom oosvfa =this.owlDataFactory.getOWLDataSomeValuesFrom(dataPropertyExpression,owlDatatype);
//				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(oosvfa, sourceExpression);
//				log.info("axiom is data property"+owlaxAxiom);
//			}
		if (axiomType == OWLAxAxiomType.GLOBAL_RANGE) {
				OWLDataAllValuesFrom ooavf = this.owlDataFactory.getOWLDataAllValuesFrom(dataPropertyExpression,
						owlDatatype);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(owlThing, ooavf);
			} else if (axiomType == OWLAxAxiomType.SCOPED_RANGE) {
				OWLDataAllValuesFrom ooavf = this.owlDataFactory.getOWLDataAllValuesFrom(dataPropertyExpression,
						owlDatatype);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(sourceExpression, ooavf);
			} else if (axiomType == OWLAxAxiomType.EXISTENTIAL) {
				OWLDataSomeValuesFrom oosvf = this.owlDataFactory.getOWLDataSomeValuesFrom(dataPropertyExpression,
						owlDatatype);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(sourceExpression, oosvf);
			} else if (axiomType == OWLAxAxiomType.FUNCTIONAL_ROLE) {
				OWLDataMaxCardinality oomc = this.owlDataFactory.getOWLDataMaxCardinality(1, dataPropertyExpression,
						owlDatatype);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(owlThing, oomc);
			} else if (axiomType == OWLAxAxiomType.QUALIFIED_FUNCTIONAL_ROLE) {
				OWLDataMaxCardinality oomc = this.owlDataFactory.getOWLDataMaxCardinality(1, dataPropertyExpression,
						owlDatatype);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(owlThing, oomc);
			} else if (axiomType == OWLAxAxiomType.SCOPED_FUNCTIONAL_ROLE) {
				OWLDataMaxCardinality oomc = this.owlDataFactory.getOWLDataMaxCardinality(1, dataPropertyExpression,
						owlDatatype);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(sourceExpression, oomc);
			} else if (axiomType == OWLAxAxiomType.QUALIFIED_SCOPED_FUNCTIONAL_ROLE) {
				OWLDataMaxCardinality oomc = this.owlDataFactory.getOWLDataMaxCardinality(1, dataPropertyExpression,
						owlDatatype);
				owlaxAxiom = this.owlDataFactory.getOWLSubClassOfAxiom(sourceExpression, oomc);
			}  else if (axiomType == OWLAxAxiomType.STRUCTURAL_TAUTOLOGY) {
				// A structural tautology has the form
				// source subclassof min 0 property target
				OWLDataMinCardinality oomc = this.owlDataFactory.getOWLDataMinCardinality(0, dataPropertyExpression,
						owlDatatype);
				OWLSubClassOfAxiom oscoa = this.owlDataFactory.getOWLSubClassOfAxiom(sourceExpression, oomc);

				owlaxAxiom = oscoa;
			}
			else {
				log.error("[CoModIDE:OWLAxAxiomFactory] Unknown Axiom Type passed,please provide valid dataproperty type.");
			}
		}


		// Finish
		return owlaxAxiom;
	}

	/*
	 * public OWLAxiom createAxiomOfProperty(OWLAxAxiomType axiomType, OWLEntity
	 * source, OWLEntity target) { // Convert OWLEntities to Expressions (or
	 * thereabouts, technically the // Expression is the super class of all three
	 * OWLObjectPropertyExpression sourcePropertyExpression =
	 * source.asOWLObjectProperty(); OWLObjectPropertyExpression
	 * targePropertyExpression = target.asOWLObjectProperty();
	 * 
	 * 
	 * // To be returned OWLAxiom owlaxAxiom = null;
	 * 
	 * if (axiomType == OWLAxAxiomType.GLOBAL_DOMAIN) { // this is just regular
	 * domain //OWLObjectSomeValuesFrom oosvfa =
	 * this.owlDataFactory.getOWLObjectSomeValuesFrom(propertyExpression, owlThing);
	 * owlaxAxiom =
	 * this.owlDataFactory.getOWLSubObjectPropertyOfAxiom(sourcePropertyExpression,
	 * targePropertyExpression); } else if (axiomType ==
	 * OWLAxAxiomType.SCOPED_DOMAIN) { //OWLObjectSomeValuesFrom oosvf =
	 * this.owlDataFactory.getOWLObjectSomeValuesFrom(propertyExpression,
	 * targetExpression); owlaxAxiom =
	 * this.owlDataFactory.getOWLSubObjectPropertyOfAxiom(sourcePropertyExpression,
	 * targePropertyExpression); } return owlaxAxiom; }
	 */
}
