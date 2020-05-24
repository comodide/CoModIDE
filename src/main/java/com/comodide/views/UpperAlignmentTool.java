package com.comodide.views;

import com.comodide.axiomatization.AxiomManager;
import com.comodide.axiomatization.OWLAxAxiomType;
import com.comodide.editor.model.ClassCell;
import com.comodide.messaging.ComodideMessage;
import com.comodide.messaging.ComodideMessageBus;
import com.comodide.messaging.ComodideMessageHandler;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
//import uk.ac.manchester.cs.jfact.JFactFactory;


/**
 * Class to introduce upper ontology in order to align it with active ontology
 * @author Abhilekha Dalal
 *
 */
public class UpperAlignmentTool extends AbstractOWLViewComponent implements ComodideMessageHandler {

    // Infrastructure
    private static final long serialVersionUID = 6258186472581035105L;
    private static final Logger log = LoggerFactory.getLogger(UpperAlignmentTool.class);
    private JSplitPane splitPane;
    private File fileName;
    private String rdf_labels;
    JPanel alignmentPanel = new JPanel();
    private Box    cellPanel;
    private Box    edgePanel;
   /* JPanel cellPanel = new JPanel();
    JPanel edgePanel = new JPanel();*/
    JPanel loadPanel = new JPanel();
    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    OWLDataFactory factory = manager.getOWLDataFactory();
    OWLOntology index;
    public AxiomManager axiomManager;
    private ClassCell currentSelectedCell;
    private OWLEntity source;
    private OWLObjectProperty property;
    private OWLEntity target;
    static OWLReasoner reasoner;
    OWLReasonerFactory reasonerFactory = null;
    // Configuration fields
    private final IRI BFO_CLASS_IRI = IRI.create("http://purl.obolibrary.org/obo/bfo.owl");


    @Override
    public void initialiseOWLView() throws Exception {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        cellPanel = Box.createVerticalBox();
        //Panel is used to load the specified upper ontology
        JTextField loadTextField = new JTextField(10);
        JButton loadButton = new JButton("Load Button");
        Font f = loadButton.getFont();
        loadButton.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
        loadButton.addActionListener(new ActionListener()
        {   //on click of loadButton loadPanel should get invisible and only alignmentPanel should be visible.
            public void actionPerformed(ActionEvent e)
            {

                JFileChooser fileChooser = new JFileChooser();
                int returnVal = fileChooser.showOpenDialog(fileChooser);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    fileName = fileChooser.getSelectedFile();
                    //log.info("fileName is "+fileName);
                    try {
                        ClassLoader classloader = this.getClass().getClassLoader();
                        InputStream is = classloader.getResourceAsStream("modl/bfo.owl");
                        index = manager.loadOntologyFromOntologyDocument(is);
                        /*reasoner = reasonerFactory.createReasoner(index,
                                config);
                        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);*/
                        Set<OWLClass> entOnt = index.getClassesInSignature();
                        for (OWLClass owlClass : entOnt) {

                            //noinspection PackageAccessibility
                            OWLReasonerFactory reasonerFactory= new Reasoner.ReasonerFactory();
                            //noinspection PackageAccessibility
                            //reasonerFactory = new ReasonerFactory();
                            ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
                            OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
                            OWLReasoner reasoner = reasonerFactory.createReasoner(index, config);
                            reasoner.precomputeInferences();
                            /*NodeSet<OWLClass> subClses = reasoner.getSubClasses(owlClass, true);
                            Set<OWLClass> clses = subClses.getFlattened();
                            log.info("Subclasses of this class: "+owlClass);
                            for (OWLClass cls : clses) {
                                log.info("    " + cls);
                            }*/
                            rdf_labels = getLabels(owlClass, index);
                            if(rdf_labels!=null){
                                JCheckBox jcb = new JCheckBox(rdf_labels, false);
                                jcb.addItemListener(new ItemListener()
                                {
                                    @Override
                                    public void itemStateChanged(ItemEvent arg0)
                                    {
                                        boolean checked = arg0.getStateChange() == 1;
                                        property = axiomManager.findObjectProperty("partOf");
                                        if (property == null)
                                        {
                                            property = axiomManager.addNewObjectProperty("partOf");
                                        }
                                        if(checked)
                                        {
                                            OWLEntity target = axiomManager.findOrAddClass(((JCheckBox) arg0.getItem()).getText());
                                            axiomManager.addOWLAxAxiomtoBFO(OWLAxAxiomType.SCOPED_DOMAIN, source, property, target);
                                        }
                                        else // unchecked
                                        {
                                            OWLEntity target = axiomManager.findOrAddClass(((JCheckBox) arg0.getItem()).getText());
                                            axiomManager.removeOWLAxAxiomtoBFO(OWLAxAxiomType.SCOPED_DOMAIN, source, property, target);
                                        }
                                    }
                                });
                                cellPanel.add(jcb);
                            }
                        }
                    alignmentPanel.add(cellPanel);
                    } catch (OWLOntologyCreationException ex) {
                    ex.printStackTrace();
                    }

                }
                alignmentPanel.setVisible(true);
                loadPanel.setVisible(false);
            }
        });
        loadPanel.add(loadTextField);
        loadPanel.add(loadButton);



        this.alignmentPanel.setVisible(false);



        this.add(loadPanel);
        JScrollPane scrollPane = new JScrollPane(alignmentPanel);
        this.add(scrollPane);
        this.axiomManager = AxiomManager.getInstance(getOWLModelManager());
        // This view is interested in Cell Selected Messages sent by Comodide
        ComodideMessageBus.getSingleton().registerHandler(ComodideMessage.CELL_SELECTED, this);
        //finish
        log.info("[CoModIDE:UpperAlignmentTool] Successfully Initialised.");
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
                source   = currentSelectedCell.getEntity();
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

    private String getLabels(OWLEntity entity, OWLOntology ontology) {
        // retVal = new ArrayList<String>();
        String retVal = null;
        for(OWLAnnotation annotation: EntitySearcher.getAnnotations(entity, ontology, factory.getRDFSLabel())) {
            OWLAnnotationValue value = annotation.getValue();
            if(value instanceof OWLLiteral) {
                retVal = ((OWLLiteral) value).getLiteral();
                //retVal.add(((OWLLiteral) value).getLiteral());
            }
        }
        return retVal;
    }

    @Override
    protected void disposeOWLView() {
        log.info("Upper Alignment Tool disposed");
    }


}
