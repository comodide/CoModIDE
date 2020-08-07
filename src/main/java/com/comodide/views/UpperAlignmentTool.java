package com.comodide.views;

import com.comodide.axiomatization.AxiomManager;
import com.comodide.axiomatization.OWLAxAxiomType;
import com.comodide.editor.model.ClassCell;
import com.comodide.editor.model.PropertyEdgeCell;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.comodide.messaging.ComodideMessage;
import com.comodide.messaging.ComodideMessageBus;
import com.comodide.messaging.ComodideMessageHandler;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.tree.*;
//import org.scijava.swing.checkboxtree.CheckBoxNodeEditor;
/*import org.scijava.swing.checkboxtree.CheckBoxNodeData;
import org.scijava.swing.checkboxtree.CheckBoxNodeEditor;
import org.scijava.swing.checkboxtree.CheckBoxNodeRenderer;*/
//import com.cognizant.cognizantits.datalib.or.common.ORObjectInf;


/**
 * Class to introduce upper ontology in order to align it with active ontology
 * @author Abhilekha Dalal
 *
 */
public class UpperAlignmentTool extends AbstractOWLViewComponent  implements ComodideMessageHandler, OWLModelManagerListener {

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
    String rdf_labels;
    private JTree tree;
    private ArrayList<JCheckBox> checkboxesCell;
    private ArrayList<JCheckBox> checkboxesEdges;
    public AxiomManager axiomManager;
    private ClassCell currentSelectedCell;
    private PropertyEdgeCell currentSelectedEdge;
    private OWLEntity source;
    private OWLObjectProperty property;
    private OWLEntity sourceProperty;
    private OWLEntity targetProperty;
    private OWLEntity target;
    static OWLReasoner reasoner;
    OWLReasonerFactory reasonerFactory = null;
    private Box   cellPanel;
    private Box   edgePanel;
    private OWLModelManager modelManager;
    private OWLOntology       owlOntology;
    private OWLDataFactory    owlDataFactory;
    // Configuration fields
    private final IRI BFO_CLASS_IRI = IRI.create("http://purl.obolibrary.org/obo/bfo.owl");



    @Override
    public void initialiseOWLView() throws Exception {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.modelManager = getOWLModelManager();
        //Panel is used to load classes of specified ontology class
        cellPanel = Box.createVerticalBox();
        edgePanel = Box.createVerticalBox();
        // Init array list to store checkboxes for later use
        this.checkboxesCell = new ArrayList<>();
        this.checkboxesEdges = new ArrayList<>();
        JTextField loadTextField = new JTextField(10);
        JButton loadButton = new JButton("Load Button");
        Font f = loadButton.getFont();
        loadButton.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
        loadButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {

                repaintPanel();
                JFileChooser fileChooser = new JFileChooser();
                int returnVal = fileChooser.showOpenDialog(fileChooser);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    fileName = fileChooser.getSelectedFile();
                    try {
                        ClassLoader classloader = this.getClass().getClassLoader();
                        InputStream is = classloader.getResourceAsStream("modl/bfo.owl");
                        index = manager.loadOntologyFromOntologyDocument(fileName);
                        JLabel cellFilename = new JLabel(fileName.getName());
                        Font f1 = cellFilename.getFont();
                        cellFilename.setFont(f1.deriveFont(f1.getStyle() | Font.BOLD));
                        cellPanel.add(cellFilename);
                        JLabel edgeFilename = new JLabel(fileName.getName());
                        edgeFilename.setFont(f1.deriveFont(f1.getStyle() | Font.BOLD));
                        edgePanel.add(edgeFilename);
                        Set<OWLClass> entOnt = index.getClassesInSignature();
                        Set<OWLAnnotationProperty> annotationProperties = index.getAnnotationPropertiesInSignature();
                        Set<OWLObjectProperty> objectProperties = index.getObjectPropertiesInSignature();
                        //DefaultMutableTreeNode root = new DefaultMutableTreeNode("BFO_VIEW");
                        for (OWLClass parent_entity : entOnt) {

                            //noinspection PackageAccessibility
                            /*OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
                            ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
                            OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
                            OWLReasoner reasoner = reasonerFactory.createReasoner(index, config);
                            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);


                            NodeSet<OWLClass> subClses = reasoner.getSubClasses(parent_entity, true);
                            Set<OWLClass> clses = subClses.getFlattened();*/
                            String entityShortName = parent_entity.getIRI().getShortForm();
                            rdf_labels = getLabels(parent_entity, index);
                            JCheckBox jcb;
                            if (rdf_labels != null) {
                                jcb = new JCheckBox(rdf_labels, false);
                            } else{
                                jcb = new JCheckBox(entityShortName , false);
                            }
                            jcb.addItemListener(new ItemListener() {
                                @Override
                                public void itemStateChanged(ItemEvent arg0) {
                                    boolean checked = arg0.getStateChange() == 1;
                                    if (checked) {
                                        target = axiomManager.findOrAddClass(((JCheckBox) arg0.getItem()).getText());
                                        axiomManager.addOWLAxAxiomtoBFO(source, target);
                                        //jcb.setSelected(false);

                                    } else// unchecked
                                    {
                                        target = axiomManager.findOrAddClass(((JCheckBox) arg0.getItem()).getText());
                                        axiomManager.removeOWLAxAxiomtoBFO(OWLAxAxiomType.SCOPED_DOMAIN, source, property, target);
                                    }
                                }
                            });
                            cellPanel.add(jcb);
                            checkboxesCell.add(jcb);

                        }
                        if(annotationProperties!=null){
                            for (OWLAnnotationProperty owlProperty : annotationProperties) {
                                rdf_labels = getLabels(owlProperty, index);
                                getPropertyCheckboxes(rdf_labels);
                            }
                        }
                        if(objectProperties!=null){
                            for (OWLObjectProperty owlProperty : objectProperties) {
                                String entityShortName = owlProperty.getIRI().getShortForm();
                                getPropertyCheckboxes(entityShortName);
                            }
                        }
                        alignmentPanel.add(cellPanel);
                        alignmentPanel.add(edgePanel);

                    } catch (OWLOntologyCreationException ex) {
                        ex.printStackTrace();
                        String infomsg = "There is error in the file, please select file with correct format";
                        JOptionPane.showMessageDialog(null, infomsg, "ErrorBox", JOptionPane.ERROR_MESSAGE);
                        log.info("there is error in the file");
                    }

                }
                alignmentPanel.setVisible(true);
                edgePanel.setVisible(false);
                //loadPanel.setVisible(false);
            }
        });
        loadPanel.add(loadTextField);
        loadPanel.add(loadButton);
        alignmentPanel.setVisible(false);

        this.add(loadPanel);
        JScrollPane scrollPane = new JScrollPane(alignmentPanel);
        this.add(scrollPane);
        this.axiomManager = AxiomManager.getInstance(getOWLModelManager());
        // This view is interested in Cell Selected Messages sent by Comodide
        ComodideMessageBus.getSingleton().registerHandler(ComodideMessage.CELL_SELECTED, this);
        //finish
        if (this.modelManager != null)
        {
            this.owlOntology = this.modelManager.getActiveOntology();
            log.info("ontology is"+owlOntology);
            // Finish and Log
            log.info("[CoModIDE:UpperAlignmentView] Successfully initialized");
        }
        else
        {
            log.error("[CoModIDE:UpperAlignmentView] Manager does not exist.");
        }
    }
    public void repaintPanel(){
        cellPanel.removeAll();
        cellPanel.revalidate();
        cellPanel.repaint();
        edgePanel.removeAll();
        edgePanel.revalidate();
        edgePanel.repaint();
        alignmentPanel.removeAll();
        alignmentPanel.revalidate();
        alignmentPanel.repaint();
    }

    public void getPropertyCheckboxes(String checkBoxText){
        if(checkBoxText!=null){
            JCheckBox jcb = new JCheckBox(checkBoxText, false);
            jcb.addItemListener(new ItemListener()
            {
                @Override
                public void itemStateChanged(ItemEvent arg0)
                {
                    boolean checked = arg0.getStateChange() == 1;
                    if(checked)
                    {
                        String propLabel = ((JCheckBox) arg0.getItem()).getText();
                        targetProperty = axiomManager.findObjectProperty(propLabel);
                        if (targetProperty == null)
                        {
                            targetProperty = axiomManager.addNewObjectProperty(propLabel);
                        }
                        axiomManager.addPropertyOWLAxAxiom(OWLAxAxiomType.SCOPED_DOMAIN, sourceProperty, targetProperty);
                    }
                    else // unchecked
                    {
                        axiomManager.removePropertyOWLAxAxiom(OWLAxAxiomType.SCOPED_DOMAIN, sourceProperty, targetProperty);
                    }
                }
            });
            edgePanel.add(jcb);
            checkboxesEdges.add(jcb);
        }
    }

    private String getLabels(OWLEntity entity, OWLOntology ontology) {
        String retVal = null;
        for(OWLAnnotation annotation: EntitySearcher.getAnnotations(entity, ontology, factory.getRDFSLabel())) {

            OWLAnnotationValue value = annotation.getValue();
            if(value instanceof OWLLiteral) {
                retVal= (((OWLLiteral) value).getLiteral());
            }
        }
        return retVal;
    }

    public void changeVisibility(String choice) {
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

    public boolean handleComodideMessage(ComodideMessage message, Object payload) {
        boolean result = false;

        if (message == ComodideMessage.CELL_SELECTED)
        {
            if (payload instanceof ClassCell)
            {
                this.currentSelectedCell = (ClassCell) payload;
                source   = currentSelectedCell.getEntity();
                this.changeVisibility("cell");
                for(JCheckBox jcb : this.checkboxesCell)
                {
                    OWLClass targetClass =  this.axiomManager.findClass(jcb.getText());
                    if(targetClass!=null){
                        boolean isAxiomPresent = this.axiomManager.matchOWLAxAxiomTypeCell(source, targetClass);
                        // set the checkbox
                        jcb.setSelected(isAxiomPresent);
                    }

                }



            }
            else if(payload instanceof PropertyEdgeCell)
            {
                this.currentSelectedEdge = (PropertyEdgeCell) payload;
                sourceProperty = currentSelectedEdge.getEntity();
                this.changeVisibility("edge");
                for(JCheckBox jcb : this.checkboxesEdges)
                {
                    OWLEntity targetPropertyAxiom = axiomManager.findObjectProperty(jcb.getText());
                    log.info("target property"+targetPropertyAxiom);
                    // Check if the axiom for the current checkbox and current selected cell exists in the ontology
                    if(targetPropertyAxiom!=null){
                        boolean isAxiomPresent = this.axiomManager.matchOWLAxAxiomTypeEdge(OWLAxAxiomType.SCOPED_DOMAIN, sourceProperty, targetPropertyAxiom);
                        // set the checkbox
                        jcb.setSelected(isAxiomPresent);
                    }

                }


            }
            else
            {
               log.info("Select a Named Property Edge to Continue.");
            }

            result = true;
        }

        return result;
    }



    private Object checkExistingNode(String node_name, JTree tree) {
        DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)treeModel.getRoot();

        // DepthFirst means it provides the very last child nodes and work its way up to the root node.
        Enumeration en = rootNode.depthFirstEnumeration();
        Object objectTargetNode = null;
        while ( en.hasMoreElements() ) {
            // Get a reference to a node in the tree to see if it is the target node.
            DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode)en.nextElement();
            // Get the virtual component object of the target node.
            objectTargetNode= targetNode.getUserObject();
            if ( objectTargetNode.toString().equals(node_name) ) {
                // Exit out of the loop.
                break;
            }
        }
        return objectTargetNode;
    }

    private void addNodeToTree(Object newObject, String category, JTree tree) {
        DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)treeModel.getRoot();

        // BreadFirst means it searches by top nodes first. It'll start with the root node,
        // then iterate thru the children of the root node and so on.
        Enumeration en = rootNode.breadthFirstEnumeration();

        while ( en.hasMoreElements() ) {
            DefaultMutableTreeNode categoryNode = (DefaultMutableTreeNode)en.nextElement();
            // Get the user defined object.
            Object categoryObject = categoryNode.getUserObject();

            // Check if node matches the category that the new node belongs to and if it does, then
            // add the new node in this category node.
            if ( categoryObject.toString().equals(category) ) {
                // Create a new node.
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newObject);
                // Use the tree model to insert the new node. This will take care of the UI updates.
                treeModel.insertNodeInto(newNode, categoryNode, categoryNode.getChildCount());
                // Exit out of the loop.
                break;
            }
        }
    }


    @Override
    protected void disposeOWLView() {
        log.info("Upper Alignment Tool disposed");
    }


    @Override
    public void handleChange(OWLModelManagerChangeEvent owlModelManagerChangeEvent) {

    }
}
