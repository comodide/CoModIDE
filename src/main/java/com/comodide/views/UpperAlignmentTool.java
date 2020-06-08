package com.comodide.views;

import com.comodide.axiomatization.AxiomManager;
import com.comodide.axiomatization.OWLAxAxiomType;
import com.comodide.editor.model.ClassCell;
import com.comodide.editor.model.PropertyEdgeCell;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.HermiT.Reasoner;
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
import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;
import javax.swing.JTree;
import javax.swing.tree.*;
//import org.scijava.swing.checkboxtree.CheckBoxNodeEditor;
/*import org.scijava.swing.checkboxtree.CheckBoxNodeData;
import org.scijava.swing.checkboxtree.CheckBoxNodeEditor;
import org.scijava.swing.checkboxtree.CheckBoxNodeRenderer;*/
//import com.cognizant.cognizantits.datalib.or.common.ORObjectInf;
import javax.swing.text.Position;



/**
 * Class to introduce upper ontology in order to align it with active ontology
 * @author Abhilekha Dalal
 *
 */
public class UpperAlignmentTool extends AbstractOWLViewComponent  implements ComodideMessageHandler{

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
    private  JCheckBox jcb;
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
    // Configuration fields
    private final IRI BFO_CLASS_IRI = IRI.create("http://purl.obolibrary.org/obo/bfo.owl");


    @Override
    public void initialiseOWLView() throws Exception {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //Panel is used to load classes of specified ontology class
        cellPanel = Box.createVerticalBox();
        JLabel cellLabel = new JLabel("Add Subclass:");
        Font f1 = cellLabel.getFont();
        cellLabel.setFont(f1.deriveFont(f1.getStyle() | Font.BOLD));
        cellPanel.add(cellLabel);
        edgePanel = Box.createVerticalBox();
        JLabel edgeName = new JLabel("Add Superproperty:");
        edgeName.setFont(f1.deriveFont(f1.getStyle() | Font.BOLD));
        edgePanel.add(edgeName);
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
                    try {
                        ClassLoader classloader = this.getClass().getClassLoader();
                        InputStream is = classloader.getResourceAsStream("modl/bfo.owl");
                        index = manager.loadOntologyFromOntologyDocument(fileName);
                        Set<OWLClass> entOnt = index.getClassesInSignature();
                        Set<OWLAnnotationProperty> entProperties = index.getAnnotationPropertiesInSignature();
                        //DefaultMutableTreeNode root = new DefaultMutableTreeNode("BFO_VIEW");
                        for (OWLClass parent_entity : entOnt) {

                            //noinspection PackageAccessibility
                            OWLReasonerFactory reasonerFactory= new Reasoner.ReasonerFactory();
                            ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
                            OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
                            OWLReasoner reasoner = reasonerFactory.createReasoner(index, config);
                            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);


                            NodeSet<OWLClass> subClses = reasoner.getSubClasses(parent_entity, true);
                            Set<OWLClass> clses = subClses.getFlattened();
                            rdf_labels= getLabels(parent_entity, index);
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
                                            target = axiomManager.findOrAddClass(((JCheckBox) arg0.getItem()).getText());
                                            axiomManager.addOWLAxAxiomtoBFO(OWLAxAxiomType.SCOPED_DOMAIN, source, property, target);
                                        }
                                        else // unchecked
                                        {
                                            target = axiomManager.findOrAddClass(((JCheckBox) arg0.getItem()).getText());
                                            axiomManager.removeOWLAxAxiomtoBFO(OWLAxAxiomType.SCOPED_DOMAIN, source, property, target);
                                        }
                                    }
                                });
                                cellPanel.add(jcb);
                            }
                            //create parent nodes for the tree
                            /*Parent_root = new DefaultMutableTreeNode(rdf_labels);
                            root.add(Parent_root);*/

                            //to have subclasses for the parent class and add children node to it
                            /*for (OWLClass cls : clses) {

                                rdf_labels= getLabels(cls, index);
                                Parent_root.add(new DefaultMutableTreeNode(rdf_labels));
                                log.info("  " + rdf_labels);
                            }*/
                        }
                        //creating checkbox for the tree nodes
                        /*tree = new JTree(root);
                        CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
                        tree.setCellRenderer(renderer);

                        tree.setCellEditor(new CheckBoxNodeEditor(tree));
                        tree.setEditable(true);
                        tree = new JTree(root);
                        alignmentPanel.add(tree);*/
                        for (OWLAnnotationProperty owlProperty : entProperties) {
                            rdf_labels = getLabels(owlProperty, index);
                            if(rdf_labels!=null){
                                JCheckBox jcb = new JCheckBox(rdf_labels, false);
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
                                            log.info("source property is:"+sourceProperty+""+"target property is:"+targetProperty);
                                            axiomManager.addPropertyOWLAxAxiom(OWLAxAxiomType.SCOPED_DOMAIN, sourceProperty, targetProperty);
                                        }
                                        else // unchecked
                                        {
                                            axiomManager.removePropertyOWLAxAxiom(OWLAxAxiomType.SCOPED_DOMAIN, sourceProperty, targetProperty);
                                        }
                                    }
                                });
                                edgePanel.add(jcb);
                            }

                        }
                        alignmentPanel.add(cellPanel);
                        alignmentPanel.add(edgePanel);

                    } catch (OWLOntologyCreationException ex) {
                        ex.printStackTrace();
                    }

                }
                alignmentPanel.setVisible(true);
                edgePanel.setVisible(false);
                loadPanel.setVisible(false);
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
        log.info("[CoModIDE:UpperAlignmentTool] Successfully Initialised.");
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
                // Bring up the axioms
                this.changeVisibility("cell");
            }
            else if(payload instanceof PropertyEdgeCell)
            {
                // Track the current selected cell
                this.currentSelectedEdge = (PropertyEdgeCell) payload;
                sourceProperty = currentSelectedEdge.getEntity();
                this.changeVisibility("edge");
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


}
