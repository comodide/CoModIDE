package com.comodide.views;

import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.axiomatization.AxiomManager;
import com.comodide.axiomatization.OWLAxAxiomType;
import com.comodide.editor.model.ComodideCell;
import com.comodide.editor.model.PropertyEdgeCell;
import com.comodide.messaging.ComodideMessage;
import com.comodide.messaging.ComodideMessageBus;
import com.comodide.messaging.ComodideMessageHandler;

public class EdgeInspectorView extends AbstractOWLViewComponent implements ComodideMessageHandler
{
	// Bookkeeping
	private static final long   serialVersionUID = 4279759233076733524L;
	private static final Logger log              = LoggerFactory.getLogger(EdgeInspectorView.class);
	// Swing components
	private Box    edgeBox;
	private JLabel edgeLabel;
	private JLabel cellLabel = new JLabel("Select a Named Property Edge to Continue.");
	//
	private ArrayList<JCheckBox> checkboxes;
	//
	private PropertyEdgeCell currentSelectedCell;
	private AxiomManager     axiomManager;

	@Override
	public void initialiseOWLView()
	{
		// Layout
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// Labels
		Font f = this.cellLabel.getFont();
		this.edgeLabel = new JLabel("");
		this.cellLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		this.edgeLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		this.cellLabel.setVisible(false);
		this.add(cellLabel);
		// Edge Inspector stuff
		setUpCheckboxes();
		// Get an axiom manager
		this.axiomManager = AxiomManager.getInstance(getOWLModelManager());
		// This view is interested in Cell Selected Messages sent by Comodide
		ComodideMessageBus.getSingleton().registerHandler(ComodideMessage.CELL_SELECTED, this);
		// Finish
		log.info("[CoModIDE:EdgeInspectorView] Successfully Initialised.");
	}

	public void setUpCheckboxes()
	{
		// Create box
		this.edgeBox = Box.createVerticalBox();
		// Title
		this.edgeBox.add(edgeLabel);
		// Init array list to store checkboxes for later use
		this.checkboxes = new ArrayList<>();
		// Add all checkboxes for the axiom types
		for (OWLAxAxiomType axiomType : OWLAxAxiomType.values())
		{
			String axiomTypeString = axiomType.getAxiomType();
			/*
			 * TODO dynamically determine if the axioms are present To do this, we will want
			 * to augment the axiom manager with an axiom finder
			 *
			 * Actually, this probably shouldn't be done here but in the handle comodide
			 * message or in the change visibility
			 */
			// Generate the Checkbox
			JCheckBox jcb = new JCheckBox(axiomTypeString, false);
			// Add the item listener to the checkbox
			jcb.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(ItemEvent arg0)
				{
					boolean checked = arg0.getStateChange() == 1;

					if (checked)
					{
						axiomManager.addOWLAxAxiom(axiomType, currentSelectedCell);
					}
					else // unchecked
					{
						axiomManager.removeOWLAxAxiom(axiomType, currentSelectedCell);
					}
				}
			});
			this.checkboxes.add(jcb);
			this.edgeBox.add(jcb);
		}
		this.edgeBox.setVisible(false);
		this.add(edgeBox);
	}

	public void changeVisibility(String choice)
	{
		if (choice.equalsIgnoreCase("cell"))
		{
			this.cellLabel.setVisible(true);
			this.edgeBox.setVisible(false);
		}
		else if (choice.equalsIgnoreCase("edge"))
		{
			this.cellLabel.setVisible(false);
			this.edgeBox.setVisible(true);
		}
		else
		{
			log.info("[CoModIDE:EdgeInspectorView] changeVisibility error.");
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
			if (payload instanceof PropertyEdgeCell && ((ComodideCell) payload).isNamed()) // || payload instanceof
																						   // SubClassEdgeCell)
			{
				// Track the current selected cell
				this.currentSelectedCell = (PropertyEdgeCell) payload;
				OWLEntity property = currentSelectedCell.getEntity();
				// Change the title of the view
				this.edgeLabel.setText((String) this.currentSelectedCell.getValue());
				// Bring up the axioms
				this.changeVisibility("edge");
				// Set dynamic checking for each checkbox
				if(property.isOWLObjectProperty()) {
					for (JCheckBox jcb : this.checkboxes)
					{
						// Get axiom type string
						String axiomTypeString = jcb.getText();
						// Get axiom type from string
						OWLAxAxiomType oaat = OWLAxAxiomType.fromString(axiomTypeString);
						// Check if the axiom for the current checkbox and current selected cell exists
						// in the ontology
						boolean isAxiomPresent = this.axiomManager.matchOWLAxAxiomType(oaat, currentSelectedCell);
						// set the checkbox
						jcb.setSelected(isAxiomPresent);
					}
				}
				else
				{
					for (JCheckBox jcb : this.checkboxes)
					{
						// Get axiom type from string
						OWLAxAxiomType oaat=OWLAxAxiomType.fromString(jcb.getText());
						//check for only valid data property axiomType
						for (OWLAxAxiomType axiomTypeDataProperty : OWLAxAxiomType.getValidDataProperty())
						{
							if(String.valueOf(oaat).equalsIgnoreCase(String.valueOf(axiomTypeDataProperty)))
							{
								// Check if the axiom for the current checkbox and current selected cell exists
								// in the ontology
								boolean isAxiomPresent = this.axiomManager.matchOWLAxAxiomType(oaat, currentSelectedCell);
								// set the checkbox
								jcb.setSelected(isAxiomPresent);
							}
						}

					}

				}
			}
			else
			{
				this.changeVisibility("cell");
			}

			result = true;
		}

		return result;
	}

	@Override
	public void disposeOWLView()
	{
		log.info("[CoModIDE:EdgeInspectorView] Successfully Disposed.");
	}
}