package com.comodide.views;

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
public class UpperAlignmentTool extends AbstractOWLViewComponent {

    // Infrastructure
    private static final long serialVersionUID = 6258186472581035105L;
    private static final Logger log = LoggerFactory.getLogger(UpperAlignmentTool.class);
    private JSplitPane splitPane;
    private File fileName;
    JPanel alignmentPanel = new JPanel();
    JPanel loadPanel = new JPanel();
    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    OWLDataFactory factory = manager.getOWLDataFactory();
    OWLOntology index;
    static OWLReasoner reasoner;
    OWLReasonerFactory reasonerFactory = null;
    // Configuration fields
    private final IRI BFO_CLASS_IRI = IRI.create("http://purl.obolibrary.org/obo/bfo.owl");


    @Override
    public void initialiseOWLView() throws Exception {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

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
                   // log.info("fileName is "+fileChooser.getSelectedFile().getPath());
                    try {
                        ClassLoader classloader = this.getClass().getClassLoader();
                        InputStream is = classloader.getResourceAsStream("modl/bfo.owl");
                        index = manager.loadOntologyFromOntologyDocument(is);
                        log.info("using new method to load ontology"+index.getOntologyID().getOntologyIRI());
                        /*reasoner = reasonerFactory.createReasoner(index,
                                config);
                        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);*/
                        Set<OWLClass> entOnt = index.getClassesInSignature();
                        int count =0;
                        for (OWLClass a : entOnt) {

                            /*NodeSet<OWLClass> subClasses = reasoner.getSubClasses(a, true);
                            for (OWLClass subClass : subClasses.getFlattened()) {
                                log.info(subClass.getIRI().getFragment() + "\tsubclass of\t"
                                        + a.getIRI().getFragment());
                            }*/
                            //noinspection PackageAccessibility
                            OWLReasonerFactory reasonerFactory= new Reasoner.ReasonerFactory();
                            //noinspection PackageAccessibility
                            //reasonerFactory = new ReasonerFactory();
                            ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
                            OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
                            OWLReasoner reasoner = reasonerFactory.createReasoner(index, config);
                            reasoner.precomputeInferences();
                            /*boolean consistent = reasoner.isConsistent();
                            System.out.println("Consistent: " + consistent);
                            System.out.println("\n");*/
                            Node<OWLClass> bottomNode = reasoner.getUnsatisfiableClasses();
                            Set<OWLClass> unsatisfiable = bottomNode.getEntitiesMinusBottom();
                            if (!unsatisfiable.isEmpty()) {
                                log.info("The following classes are unsatisfiable: ");
                                for (OWLClass cls : unsatisfiable) {
                                    log.info("    " + cls);
                                }
                            } else {
                                log.info("There are no unsatisfiable classes");
                            }


                            //OWLClass person = factory.getOWLClass(IRI.create("http://localhost/Institute#Person"));
                            NodeSet<OWLClass> subClses = reasoner.getSubClasses(a, true);
                            Set<OWLClass> clses = subClses.getFlattened();
                            log.info("Subclasses of this class: "+a);
                            for (OWLClass cls : clses) {
                                log.info("    " + cls);
                            }
                            //System.out.println("\n");



                          /*  Set<OWLClass> classes = reasoner.getSubClasses(a, true).getFlattened();
                            boolean isEmpty = classes.isEmpty();
                            log.info("isEmpty of "+ isEmpty);
                            for (OWLClass subClass : classes) {
                                log.info(subClass +"subclass of "+ a.getIRI().getFragment());
                            }*/
                            count++;
                        }
                        log.info("Entity from ontology "+count);


                        // Find all the pattern instances in the index
                        /*OWLClass bfoClass = factory.getOWLClass(BFO_CLASS_IRI);
                        List<String> patternLabels = getLabels(bfoClass, index);
                        for (String pattern: patternLabels) {
                            log.info("bfo is inside upperAlignment"+ pattern);

                        }*/
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


        //Panel is used to align ontology with the specified upper ontology
        JLabel alignmentPanelLabel = new JLabel("alignmentPanel:");
        alignmentPanel.add(alignmentPanelLabel);
        this.alignmentPanel.setVisible(false);



        //Adding cellPanel and edgePanel to the View as SplitPanel
        /*splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                loadPanel, alignmentPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);*/
        this.add(loadPanel);
        this.add(alignmentPanel);
        //finish
        log.info("[CoModIDE:UpperAlignmentTool] Successfully Initialised.");
    }

    private List<String> getLabels(OWLEntity entity, OWLOntology ontology) {
        List<String> retVal = new ArrayList<String>();
        for(OWLAnnotation annotation: EntitySearcher.getAnnotations(entity, ontology, factory.getRDFSLabel())) {
            OWLAnnotationValue value = annotation.getValue();
            if(value instanceof OWLLiteral) {
                retVal.add(((OWLLiteral) value).getLiteral());
            }
        }
        return retVal;
    }

    @Override
    protected void disposeOWLView() {
        log.info("Upper Alignment Tool disposed");
    }


}
