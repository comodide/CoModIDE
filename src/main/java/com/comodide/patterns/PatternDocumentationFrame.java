package com.comodide.patterns;

import java.awt.BorderLayout;
import java.awt.HeadlessException;

import javax.swing.JFrame;
import javax.swing.JLabel;

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

		add(new JLabel(pattern.getLabel()), BorderLayout.NORTH);
		add(new JLabel(pattern.getIri().toString()), BorderLayout.CENTER);
		pack();
	}
}
