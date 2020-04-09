package com.comodide.views;


import com.comodide.axiomatization.AxiomManager;
import com.comodide.axiomatization.OWLAxAxiomType;
import com.comodide.editor.model.ClassCell;
import com.comodide.messaging.ComodideMessage;
import com.comodide.messaging.ComodideMessageBus;
import com.comodide.messaging.ComodideMessageHandler;
import com.comodide.patterns.PatternLibrary;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


/**
 * Class to introduce BFO ontology aligned with active ontology if user wish to.
 * @author Abhilekha Dalal
 *
 */

public class BFOView extends AbstractOWLViewComponent implements ComodideMessageHandler {
    // Infrastructure
    private static final long serialVersionUID = 6258186472581035105L;
    private static final Logger log = LoggerFactory.getLogger(BFOView.class);

    // Private members
    private PatternLibrary patternLibrary = PatternLibrary.getInstance();
    public JPanel cellPanel = new JPanel();
    public JPanel edgePanel = new JPanel();
    private JSplitPane splitPane;
    public AxiomManager axiomManager;
    private ClassCell currentSelectedCell;
    private OWLEntity source;
    private OWLObjectProperty property;
    private OWLEntity target;

    @Override
    public void initialiseOWLView() throws Exception {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //Setting up Cells for BFO View
        JLabel cellLabel = new JLabel("CLASSES:");
        Font f = cellLabel.getFont();
        cellLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
        cellPanel.setLayout(new BoxLayout(cellPanel, BoxLayout.Y_AXIS));
        cellPanel.add(cellLabel);

        //Setting up Edges for BFO View
        JLabel edgeName = new JLabel("RELATIONSHIPS:");
        edgeName.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
        edgePanel.setLayout(new BoxLayout(edgePanel, BoxLayout.Y_AXIS));
        edgePanel.add(edgeName);

        //Adding checkboxes for Cells and Edges
        setUpCheckboxes();

        //Adding cellPanel and edgePanel to the View as SplitPanel
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                cellPanel, edgePanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);

        this.axiomManager = AxiomManager.getInstance(getOWLModelManager());
        // This view is interested in Cell Selected Messages sent by Comodide
        ComodideMessageBus.getSingleton().registerHandler(ComodideMessage.CELL_SELECTED, this);
        this.add(splitPane);
        // Finish
        log.info("[CoModIDE:BFOView] Successfully Initialised.");



    }

    public void setUpCheckboxes()
    {
        JLabel continuantCheckbox = new JLabel("Continuant");
        JCheckBox independentContinuant_Checkbox = new JCheckBox("Independent Continuant", false);
        independentContinuant_Checkbox.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent arg0){
                boolean checked = arg0.getStateChange() == 1;
                if(checked){
                    OWLEntity target = axiomManager.findOrAddClass("Independent Continuant");
                    source   = currentSelectedCell.getEntity();
                    property = axiomManager.findObjectProperty("partOf");
                    if (property == null)
                    {
                        property = axiomManager.addNewObjectProperty("partOf");
                    }
                    //owlOntology = modelManager.getActiveOntology();
                    /*if (owlOntology != null) {
                        owlDataFactory = owlOntology.getOWLOntologyManager().getOWLDataFactory();
                        OWLObjectSomeValuesFrom someValuesFrom = owlDataFactory.getOWLObjectSomeValuesFrom(property,
                                source.asOWLClass());
                        log.info("addAxiomChange"+ someValuesFrom);
                        OWLSubClassOfAxiom subClassAxiom = owlDataFactory.getOWLSubClassOfAxiom(someValuesFrom, target.asOWLClass());
                        log.info("addAxiomChange"+ subClassAxiom);
                        AddAxiom addAxiomChange = new AddAxiom(owlOntology, subClassAxiom);
                        log.info("addAxiomChange"+ addAxiomChange);
                        modelManager.applyChange(addAxiomChange);
                    }
                    log.info(" value of owlOntology"+ owlOntology);*/
                    axiomManager.addOWLAxAxiomtoBFO(OWLAxAxiomType.SCOPED_DOMAIN, source, property, target);
                }
                else // unchecked
                {
                    axiomManager.removeOWLAxAxiomtoBFO(OWLAxAxiomType.SCOPED_DOMAIN, source, property, target);
                }
            }
        });
        JCheckBox material_Entity = new JCheckBox("Material Entity", false);
        JCheckBox immaterial_Entity = new JCheckBox("Immaterial Entity", false);
        JCheckBox dependentContinuant_Checkbox = new JCheckBox("Dependent Continuant");
        dependentContinuant_Checkbox.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent arg0){
                boolean checked = arg0.getStateChange() == 1;
                if(checked){
                    target =  axiomManager.findOrAddClass("Dependent Continuant");
                    source   = currentSelectedCell.getEntity();
                    property = axiomManager.findObjectProperty("partOf");
                    if (property == null)
                    {
                        property = axiomManager.addNewObjectProperty("partOf");
                    }
                    axiomManager.addOWLAxAxiomtoBFO(OWLAxAxiomType.SCOPED_DOMAIN, source, property, target);
                }
                else // unchecked
                {
                    axiomManager.removeOWLAxAxiomtoBFO(OWLAxAxiomType.SCOPED_DOMAIN, source, property, target);
                }
            }
        });
        cellPanel.add(continuantCheckbox);
        cellPanel.add(material_Entity);
        cellPanel.add(immaterial_Entity);
        cellPanel.add(independentContinuant_Checkbox);
        cellPanel.add(dependentContinuant_Checkbox);


        JCheckBox is_a = new JCheckBox("is_a");
        JCheckBox part_of = new JCheckBox("part_of");
        edgePanel.add(is_a);
        edgePanel.add(part_of);

    }



    public void changeVisibility(String choice)
    {
        if (choice.equalsIgnoreCase("cell"))
        {
            this.cellPanel.setVisible(true);
            this.edgePanel.setVisible(false);
        }
        else if (choice.equalsIgnoreCase("edge"))
        {
            this.cellPanel.setVisible(false);
            this.edgePanel.setVisible(true);
        }
        else
        {
            log.info("[CoModIDE:BFOView] changeVisibility error.");
        }
    }

    public boolean handleComodideMessage(ComodideMessage message, Object payload)
    {
        boolean result = false;

        // Handler for selecting a cell
        if (message == ComodideMessage.CELL_SELECTED)
        {
            // Make sure that the current selected cell is an edge
            // And that it is named (i.e. we don't want to be 'inspecting'
            // an edge that doesn't yet have a payload
            if (payload instanceof ClassCell) // || payload instanceof SubClassEdgeCell)
            {
                // Track the current selected cell
                this.currentSelectedCell = (ClassCell) payload;
                // Change the title of the view
                //this.edgeLabel.setText(this.currentSelectedCell.getId());
                // Bring up the axioms
                this.changeVisibility("cell");
            }
            else
            {
                this.changeVisibility("edge");
            }

            result = true;
        }

        return result;
    }

    @Override
    protected void disposeOWLView() {
        log.info("BFO view disposed");
    }

}
