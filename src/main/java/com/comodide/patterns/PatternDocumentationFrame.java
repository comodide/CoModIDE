package com.comodide.patterns;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A frame displaying ontology design pattern documentation, as provided by a
 * passed in Pattern object.
 * 
 * @author Karl Hammar <karl@karlhammar.com>
 *
 */
public class PatternDocumentationFrame extends JFrame {

	private static final long serialVersionUID = -455249794302758088L;

	public PatternDocumentationFrame(Pattern pattern) throws HeadlessException {
		super();
		
		// TODO: Implement this this documentation frame in a pretty way.

		add(new JLabel("Pattern label = " + pattern.getLabel()), BorderLayout.NORTH);
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.add(new JLabel("Pattern IRI = " + pattern.getIri().toString()));
		centerPanel.add(new JLabel("Rendered schema diagram = " + pattern.getRenderedSchemaDiagram().orElse(new File("NO/FILE/FOUND")).toString()));
		centerPanel.add(new JLabel("HTML docs = " + pattern.getHtmlDocumentation().orElse("<html><body>No docs found.</body></html>")));
		add(centerPanel, BorderLayout.CENTER);
		pack();
	}
}
