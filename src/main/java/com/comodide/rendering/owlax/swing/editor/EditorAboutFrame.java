package com.comodide.rendering.owlax.swing.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import com.mxgraph.util.mxResources;

public class EditorAboutFrame extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3378029138434324390L;

	/**
	 * 
	 */
	public EditorAboutFrame(Frame owner) {
		super(owner);
		setTitle(mxResources.get("aboutGraphEditor"));
		setLayout(new BorderLayout());

		// Creates the gradient panel
		JPanel panel = new JPanel(new BorderLayout()) {

			/**
			 * 
			 */
			private static final long serialVersionUID = -5062895855016210947L;

			/**
			 * 
			 */
			public void paintComponent(Graphics g) {
				super.paintComponent(g);

				// Paint gradient background
				Graphics2D g2d = (Graphics2D) g;
				g2d.setPaint(new GradientPaint(0, 0, Color.WHITE, getWidth(), 0, getBackground()));
				g2d.fillRect(0, 0, getWidth(), getHeight());
			}

		};

		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
				BorderFactory.createEmptyBorder(8, 8, 12, 8)));

		// Adds title
		JLabel titleLabel = new JLabel("OWLAx");
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		titleLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		titleLabel.setOpaque(false);
		panel.add(titleLabel, BorderLayout.NORTH);

		String htmlSubTitle = "<html><br><h3>OWLAx</h3><p>OWL Axiomatizer</p><br><p>For details please visit: <a href=\"http://dase.cs.wright.edu/content/ontology-axiomatization-support\" style=\"text-decoration:none;\">OWLAx Capabilities and How to Use</a></html>";

		// Adds optional subtitle
		JLabel subtitleLabel = new JLabel("OWL Axiomatizer Plugin for Desktop Protege 5.0+");
		subtitleLabel.setBorder(BorderFactory.createEmptyBorder(4, 18, 0, 0));
		subtitleLabel.setOpaque(false);
		panel.add(subtitleLabel, BorderLayout.CENTER);

		getContentPane().add(panel, BorderLayout.NORTH);

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		// content.add(new JLabel("JGraph X - The Swing Portion of mxGraph"));
		content.add(new JLabel(" "));

		content.add(new JLabel("Version " + mxResources.get("owlaxVersion")));
		content.add(new JLabel("Copyright (C) 2016 by Data Semantics Lab."));
		content.add(new JLabel("All rights reserved."));
		content.add(new JLabel(" "));
		content.add(new JLabel(" "));
		content.add(new JLabel(" "));
		
		//AcknowledgeMent
		content.add(new JLabel("Acknowledgement:"));
		content.add(new JLabel("Some graphics part was developed by mxGraph."));
		content.add(new JLabel("This work was supported by the National Science Foundation under"));
		content.add(new JLabel("award 1017225 III: Small: TROn – Tractable Reasoning with Ontolo- gies."));
		
		// try
		// {
		// content.add(new JLabel("Operating System Name: "
		// + System.getProperty("os.name")));
		// content.add(new JLabel("Operating System Version: "
		// + System.getProperty("os.version")));
		// content.add(new JLabel(" "));
		//
		// content.add(new JLabel("Java Vendor: "
		// + System.getProperty("java.vendor", "undefined")));
		// content.add(new JLabel("Java Version: "
		// + System.getProperty("java.version", "undefined")));
		// content.add(new JLabel(" "));
		//
		// content.add(new JLabel("Total Memory: "
		// + Runtime.getRuntime().totalMemory()));
		// content.add(new JLabel("Free Memory: "
		// + Runtime.getRuntime().freeMemory()));
		// }
		// catch (Exception e)
		// {
		// // ignore
		// }

		getContentPane().add(content, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY), BorderFactory.createEmptyBorder(16, 8, 8, 8)));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		// Adds OK button to close window
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});

		buttonPanel.add(closeButton);

		// Sets default button for enter key
		getRootPane().setDefaultButton(closeButton);

		setResizable(false);
		setSize(500, 400);
	}

	/**
	 * Overrides {@link JDialog#createRootPane()} to return a root pane that
	 * hides the window when the user presses the ESCAPE key.O
	 */
	protected JRootPane createRootPane() {
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		JRootPane rootPane = new JRootPane();
		rootPane.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				setVisible(false);
			}
		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		return rootPane;
	}

}
