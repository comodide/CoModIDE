package com.comodide.views;

import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JLabel;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.editor.model.PropertyEdgeCell;
import com.comodide.editor.model.SubClassEdgeCell;
import com.comodide.messaging.ComodideMessage;
import com.comodide.messaging.ComodideMessageBus;
import com.comodide.messaging.ComodideMessageHandler;

public class EdgeInspectorView extends AbstractOWLViewComponent implements ComodideMessageHandler
{
	// Bookkeeping
	private static final long   serialVersionUID = 4279759233076733524L;
	private static final Logger log              = LoggerFactory.getLogger(EdgeInspectorView.class);

	// Labels
	private JLabel edgeLabel = new JLabel("Edge.");
	private JLabel cellLabel = new JLabel("Select a Property Edge to Continue.");
	
	@Override
	public void initialiseOWLView()
	{
		// Layout
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// Font
		Font f = edgeLabel.getFont();
		// Bold them
		edgeLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		cellLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		// Visibility
		edgeLabel.setVisible(false);
		cellLabel.setVisible(false);
		// Add them
		this.add(edgeLabel);
		this.add(cellLabel);
		
		// This view is interested in Cell Selected Messages sent by Comodide
		ComodideMessageBus.getSingleton().registerHandler(ComodideMessage.CELL_SELECTED, this);
		
		// Finish
		log.info("[CoModIDE:EdgeInspectorView] Successfully Initialised.");
	}

	public void changeVisibility(String choice)
	{
		if (choice.equalsIgnoreCase("cell"))
		{
			this.cellLabel.setVisible(true);
			this.edgeLabel.setVisible(false);
		}
		else if (choice.equalsIgnoreCase("edge"))
		{
			this.cellLabel.setVisible(false);
			this.edgeLabel.setVisible(true);
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
		if(message == ComodideMessage.CELL_SELECTED)
		{
			if(payload instanceof PropertyEdgeCell) // || payload instanceof SubClassEdgeCell)
			{
				this.changeVisibility("edge");
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
