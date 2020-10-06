package com.comodide.patterns;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A frame displaying ontology design pattern documentation, as provided by a
 * passed in Pattern object.
 * 
 * @author Karl Hammar <karl@karlhammar.com>
 *
 */
public class PatternDocumentationFrame extends JFrame
{

	private static final long serialVersionUID = -455249794302758088L;

	private static final Logger log = LoggerFactory.getLogger(PatternDocumentationFrame.class);

	public PatternDocumentationFrame(Pattern pattern) throws HeadlessException
	{
		super();

		setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().setBackground(Color.WHITE);

		JLabel patternNameLabel = new JLabel(pattern.getLabel());
		Font   orginalLabelFont = patternNameLabel.getFont();
		Font   largeBoldFont    = new Font(orginalLabelFont.getFontName(), Font.BOLD, orginalLabelFont.getSize() * 2);
		patternNameLabel.setFont(largeBoldFont);
		patternNameLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		patternNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(patternNameLabel);

		JLabel patternIriLabel = new JLabel(pattern.getIri().toString());
		patternIriLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		Font courierBoldFont = new Font(Font.MONOSPACED, Font.BOLD, orginalLabelFont.getSize());
		patternIriLabel.setFont(courierBoldFont);
		patternIriLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		add(patternIriLabel);

		try
		{
			if (pattern.getSchemaDiagramPath().isPresent())
			{
				ClassLoader   classloader   = this.getClass().getClassLoader();
				InputStream   is            = classloader.getResourceAsStream(pattern.getSchemaDiagramPath().get());
				BufferedImage schemaDiagram = ImageIO.read(is);
				JLabel        sdLabel       = new JLabel(new ImageIcon(schemaDiagram));
				sdLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
				sdLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
				add(sdLabel);
			}
		}
		catch (Exception ex)
		{
			log.error(ex.getMessage());
			ex.printStackTrace();
		}

		JEditorPane documentationPane = new JEditorPane();
		documentationPane.setContentType("text/html");
		documentationPane.setEditable(false);
		documentationPane.setText(pattern.getHtmlDocumentation().orElse(
				"<html><body>No documentation predicates for this pattern were found in the index.</body></html>"));
		documentationPane.setCaretPosition(0);
		documentationPane.setPreferredSize(new Dimension(800, 600));
		JScrollPane documentationScrollPane = new JScrollPane(documentationPane);
		documentationScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		documentationScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		add(documentationScrollPane);

		pack();
	}
}
