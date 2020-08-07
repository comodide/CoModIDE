package com.comodide.views;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.HashMap;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.axiomatization.AxiomManager;
import com.comodide.editor.model.ClassCell;
import com.comodide.editor.model.PropertyEdgeCell;
import com.comodide.messaging.ComodideMessage;
import com.comodide.messaging.ComodideMessageBus;
import com.comodide.messaging.ComodideMessageHandler;

/**
 * Class to introduce upper ontology in order to align it with active ontology
 * 
 * @author Abhilekha Dalal
 *
 */
public class UpperAlignmentTool extends AbstractOWLViewComponent implements ComodideMessageHandler
{

	// Bookkeeping
	private static final long   serialVersionUID = 6258186472581035105L;
	private static final Logger log              = LoggerFactory.getLogger(UpperAlignmentTool.class);

	private File             fileName;
	JPanel                   alignmentPanel = new JPanel();
	JPanel                   loadPanel      = new JPanel();
	OWLOntologyManager       manager        = OWLManager.createOWLOntologyManager();
	OWLDataFactory           factory        = manager.getOWLDataFactory();
	OWLOntology              index;
	public AxiomManager      axiomManager;
	private ClassCell        currentSelectedCell;
	private PropertyEdgeCell currentSelectedEdge;
	private OWLEntity        source;
	private OWLEntity        sourceProperty;
	private OWLEntity        targetProperty;
	private OWLEntity        target;
	private Box              cellPanel;
	private Box              edgePanel;
	private OWLModelManager  modelManager;
	private OWLOntology      owlOntology;

	// For looping through the relevant checkboxes
	private HashMap<JCheckBox, OWLClass>          checkboxesCell;
	private HashMap<JCheckBox, OWLObjectProperty> checkboxesEdges;

	@Override
	public void initialiseOWLView() throws Exception
	{
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.modelManager = getOWLModelManager();
		// Panel is used to load classes of specified ontology class
		cellPanel = Box.createVerticalBox();
		edgePanel = Box.createVerticalBox();
		// Init array list to store checkboxes for later use
		this.checkboxesCell = new HashMap<>();
		this.checkboxesEdges = new HashMap<>();
		JTextField loadTextField = new JTextField(10);
		JButton    loadButton    = new JButton("Load Button");
		Font       f             = loadButton.getFont();
		loadButton.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		loadButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Allow the user to choose the file to load
				JFileChooser fileChooser = new JFileChooser();
				int          returnVal   = fileChooser.showOpenDialog(fileChooser);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					// Get the file
					fileName = fileChooser.getSelectedFile();
					try
					{
						// Load the ontology
						index = manager.loadOntologyFromOntologyDocument(fileName);
						// Create and add a label for the name of the loaded ontology (for the cell
						// panel)
						JLabel cellFilename = new JLabel(fileName.getName());
						Font   f1           = cellFilename.getFont();
						cellFilename.setFont(f1.deriveFont(f1.getStyle() | Font.BOLD));
						cellPanel.add(cellFilename);
						// Create and add a label for the name of the loaded ontology (for the edge
						// panel)
						JLabel edgeFilename = new JLabel(fileName.getName());
						edgeFilename.setFont(f1.deriveFont(f1.getStyle() | Font.BOLD));
						edgePanel.add(edgeFilename);
						// Get all the classes from the ontology
						Set<OWLClass> entOnt = index.getClassesInSignature();
						// Get all the object properties
						Set<OWLObjectProperty> objectProperties = index.getObjectPropertiesInSignature();
						// For each class in the ontology
						for (OWLClass parent_entity : entOnt)
						{
							// Get the shortform
							String entityShortName = parent_entity.getIRI().getShortForm();
							// Get the RDF label for the entity
							String rdf_labels = getLabels(parent_entity, index);
							// Create checkbox
							JCheckBox jcb;
							if (rdf_labels != null)
							{
								// Use the RDF label if they exist
								jcb = new JCheckBox(rdf_labels, false);
							}
							else
							{
								// Use the shortform name
								jcb = new JCheckBox(entityShortName, false);
							}
							// Now, create the item listener for it, this will add the axiom to the ontology
							// when it is clicked, or remove it
							jcb.addItemListener(new ItemListener()
							{
								@Override
								public void itemStateChanged(ItemEvent arg0)
								{
									boolean checked = arg0.getStateChange() == 1;
									if (checked)
									{
										axiomManager.addOWLAxAxiomtoBFO(source, parent_entity);
									}
									else// unchecked
									{
										axiomManager.removeOWLAxAxiomtoBFO(source, parent_entity);
									}
								}
							});
							cellPanel.add(jcb);
							checkboxesCell.put(jcb, parent_entity);
						}

						if (objectProperties != null)
						{
							for (OWLObjectProperty owlProperty : objectProperties)
							{
								getPropertyCheckboxes(owlProperty);
							}
						}
						alignmentPanel.add(cellPanel);
						alignmentPanel.add(edgePanel);

					}
					catch (OWLOntologyCreationException ex)
					{
						ex.printStackTrace();
						String infomsg = "There is error in the file, please select file with correct format";
						JOptionPane.showMessageDialog(null, infomsg, "ErrorBox", JOptionPane.ERROR_MESSAGE);
						log.info("there is error in the file");
					}

				}
				alignmentPanel.setVisible(true);
				edgePanel.setVisible(false);
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
		// finish
		if (this.modelManager != null)
		{
			this.owlOntology = this.modelManager.getActiveOntology();
			log.info("ontology is" + owlOntology);
			// Finish and Log
			log.info("[CoModIDE:UpperAlignmentView] Successfully initialized");
		}
		else
		{
			log.error("[CoModIDE:UpperAlignmentView] Manager does not exist.");
		}
	}

	public void getPropertyCheckboxes(OWLObjectProperty owlProperty)
	{
		String checkBoxText = owlProperty.getIRI().getShortForm();
		if (checkBoxText != null)
		{
			JCheckBox jcb = new JCheckBox(checkBoxText, false);
			jcb.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(ItemEvent arg0)
				{
					boolean checked = arg0.getStateChange() == 1;
					if (checked)
					{
						String propLabel = ((JCheckBox) arg0.getItem()).getText();
						targetProperty = axiomManager.findObjectProperty(propLabel);
						if (targetProperty == null)
						{
							targetProperty = axiomManager.addNewObjectProperty(propLabel);
						}
						axiomManager.addPropertyOWLAxAxiom(sourceProperty, owlProperty);
					}
					else // unchecked
					{
						axiomManager.removePropertyOWLAxAxiom(sourceProperty, owlProperty);
					}
				}
			});
			edgePanel.add(jcb);
			checkboxesEdges.put(jcb, owlProperty);
		}
	}

	private String getLabels(OWLEntity entity, OWLOntology ontology)
	{
		String retVal = null;
		for (OWLAnnotation annotation : EntitySearcher.getAnnotations(entity, ontology, factory.getRDFSLabel()))
		{

			OWLAnnotationValue value = annotation.getValue();
			if (value instanceof OWLLiteral)
			{
				retVal = (((OWLLiteral) value).getLiteral());
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

		if (message == ComodideMessage.CELL_SELECTED)
		{
			if (payload instanceof ClassCell)
			{
				// Track the currently selected cell and extract its payload
				this.currentSelectedCell = (ClassCell) payload;
				source = currentSelectedCell.getEntity();
				OWLClass sourceClass = source.asOWLClass();
				// Bring up the Cell related checkboxes
				this.changeVisibility("cell");
				// Loop through all the checkboxes for cells
				for (JCheckBox jcb : this.checkboxesCell.keySet())
				{
					// Extract target class
					OWLClass targetClass    = this.checkboxesCell.get(jcb);
					// Check if the axiom is present
					boolean  isAxiomPresent = this.axiomManager.matchSubClassAxiom(sourceClass, targetClass);
					// set the checkbox
					jcb.setSelected(isAxiomPresent);
				}
			}
			else if (payload instanceof PropertyEdgeCell)
			{
				this.currentSelectedEdge = (PropertyEdgeCell) payload;
				sourceProperty = currentSelectedEdge.getEntity();
				this.changeVisibility("edge");
				for (JCheckBox jcb : this.checkboxesEdges.keySet())
				{
					// Check if the axiom for the current checkbox and current edge exists
					OWLObjectProperty property = this.checkboxesEdges.get(jcb);
					if (property != null)
					{
						boolean isAxiomPresent = this.axiomManager.matchSubPropertyAxiom(sourceProperty, property);
						// Set the checkbox
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

	@Override
	protected void disposeOWLView()
	{
		log.info("[CoModIDE] Upper Alignment Tool disposed");
	}
}
