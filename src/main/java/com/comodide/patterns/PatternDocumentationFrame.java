package com.comodide.patterns;

import java.awt.BorderLayout;
import java.awt.HeadlessException;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class PatternDocumentationFrame extends JFrame {

	private static final long serialVersionUID = -455249794302758088L;

	public PatternDocumentationFrame(Pattern pattern) throws HeadlessException {
		super();
		
		//setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		add(new JLabel(pattern.getLabel()), BorderLayout.NORTH);
		add(new JLabel(pattern.getIri().toString()), BorderLayout.CENTER);
		pack();
	}
}
