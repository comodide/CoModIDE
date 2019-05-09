package com.comodide.rendering.sdont.ui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.comodide.rendering.sdont.model.SDGraph;
import com.comodide.rendering.sdont.parsing.OntologyParser;
import com.comodide.rendering.sdont.viz.SDMaker;

public class SDontGUI
{
	public void run()
	{
		// create a top-level window and add the graph component.
		JFrame frame = new JFrame("SDOnt");

		frame.setLayout(new FlowLayout());

		JButton open = new JButton("OPEN");
		JButton save = new JButton("SAVE");

		open.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				// Get File from user
				JFileChooser jfc = new JFileChooser();
				int returnVal = jfc.showOpenDialog(frame);
				
				if(returnVal == JFileChooser.APPROVE_OPTION)
				{
					File file = jfc.getSelectedFile();
					// Visualize
					try
					{
						OntologyParser ontologyParser = new OntologyParser(file);
						SDGraph graph = ontologyParser.parseOntology();
						SDMaker maker = new SDMaker(graph);
						maker.visualize();
					}
					catch(OWLOntologyCreationException e)
					{
						System.out.println("Could not create ontology from file: " + file.getName());
					}
				}
			}
		});

		save.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				// TODO finish actionlistener
			}
		});

		frame.add(open);
		frame.add(save);

		frame.setSize(400, 200);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
