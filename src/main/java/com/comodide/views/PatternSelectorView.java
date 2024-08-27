package com.comodide.views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.patterns.PatternCategory;
import com.comodide.patterns.PatternLibrary;
import com.comodide.patterns.PatternTable;
import com.comodide.patterns.PatternTableModel;
import com.comodide.telemetry.TelemetryAgent;

/**
 * CoModIDE Pattern Selector view. Lists and displays indexed ontology patterns, and provides hooks to initiate pattern instantiation into an ontology.
 * @author Karl Hammar <karl@karlhammar.com>
 *
 */
public class PatternSelectorView extends AbstractOWLViewComponent {

	// Infrastructure
	private static final long serialVersionUID = 6258186472581035105L;
	private static final Logger log = LoggerFactory.getLogger(PatternSelectorView.class);
	
	// Private members
	private PatternLibrary patternLibrary = PatternLibrary.getInstance();
	private PatternTable patternsTable;
	private PatternTableModel patternsTableModel = new PatternTableModel(patternLibrary.getPatternsForCategory(patternLibrary.ANY_CATEGORY));
	
    @Override
    protected void initialiseOWLView() throws Exception {
    	this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Pattern Category Selector - START
		JLabel categorySelectorLabel = new JLabel("Pattern category selector:");
		Font categorySelectorLabelFont = categorySelectorLabel.getFont();
		categorySelectorLabel.setFont(categorySelectorLabelFont.deriveFont(categorySelectorLabelFont.getStyle() | Font.BOLD));
		categorySelectorLabel.setAlignmentX(LEFT_ALIGNMENT);
        // This is a hack due to JComboBox misbehaving; see https://stackoverflow.com/questions/7581846/swing-boxlayout-problem-with-jcombobox-without-using-setxxxsize
        JComboBox<PatternCategory> categoryList = new JComboBox<PatternCategory>(patternLibrary.getPatternCategories()) {
			private static final long serialVersionUID = 4795749883863962239L;
			@Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                max.height = getPreferredSize().height;
                return max;
            }
        };

        // Listener for when user selects a new category, redraws the pattern table based on chosen category
        categoryList.addActionListener(event -> {
            PatternCategory selectedCategory = (PatternCategory)categoryList.getSelectedItem();
            TelemetryAgent.logLibraryClick(String.format("Category: %s (%s)", selectedCategory.getLabel(), selectedCategory.getIri().toString()));
            patternsTableModel.update(patternLibrary.getPatternsForCategory(selectedCategory));
        });
        categoryList.setAlignmentX(LEFT_ALIGNMENT);
        // Pattern Category Selector - END

        // Pattern Library Selector - START
        JLabel librarySelectorLabel = new JLabel("Pattern library selector:");
        Font librarySelectorFont = librarySelectorLabel.getFont();
        librarySelectorLabel.setFont(librarySelectorFont.deriveFont(librarySelectorFont.getStyle() | Font.BOLD));
        librarySelectorLabel.setAlignmentX(LEFT_ALIGNMENT);
        // This is a hack due to JComboBox misbehaving; see https://stackoverflow.com/questions/7581846/swing-boxlayout-problem-with-jcombobox-without-using-setxxxsize
        JComboBox<String> libraryList = new JComboBox<String>(
                new String[]
                        {
                                PatternLibrary.DEFAULT_LIBRARY_PATH,
                                "modl/csmodl/csmodl.owl",
                                "modl/easy-ai/schema/easy-ai-index.ttl"
                        }
        ) {
            private static final long serialVersionUID = 3692789082261972438L;
            @Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                max.height = getPreferredSize().height;
                return max;
            }
        };

        // Listener for when user selects a new category, redraws the pattern table based on chosen category
        libraryList.addActionListener(event -> {
            String selectedLibraryPath = (String) libraryList.getSelectedItem();
            PatternCategory selectedCategory = (PatternCategory)categoryList.getSelectedItem();
            TelemetryAgent.logLibraryClick(String.format("Library: %s", selectedLibraryPath));
            if (selectedLibraryPath == null)
                return;
            PatternLibrary.setInstanceByPath(selectedLibraryPath);
            patternLibrary = PatternLibrary.getInstance();
            DefaultComboBoxModel<PatternCategory> updatedCategoryListModel = new DefaultComboBoxModel<>(patternLibrary.getPatternCategories());
            categoryList.setModel(updatedCategoryListModel);
            patternsTableModel.update(patternLibrary.getPatternsForCategory(selectedCategory));
        });
        libraryList.setAlignmentX(LEFT_ALIGNMENT);
        // Pattern Library Selector - END

        // Add library selector and then category selector
        /* These are done after creating both the library selector and category selector
        *  because the library selector needs to be displayed above the category selector
        *  but the library selector refers to the category selector for its functionality */
        this.add(librarySelectorLabel);
        this.add(libraryList);
        this.add(categorySelectorLabel);
        this.add(categoryList);
		
        JPanel patternsTableHeader = new JPanel();
        patternsTableHeader.setLayout(new BoxLayout(patternsTableHeader, BoxLayout.X_AXIS));
        patternsTableHeader.setAlignmentX(LEFT_ALIGNMENT);
        this.add(patternsTableHeader);
        
        JLabel patternsLabel = new JLabel("Patterns:");
        patternsLabel.setFont(categorySelectorLabelFont.deriveFont(categorySelectorLabelFont.getStyle() | Font.BOLD));
        patternsTableHeader.add(patternsLabel);
        
        patternsTableHeader.add(Box.createHorizontalGlue());
        
        InfoIcon infoIcon = new InfoIcon(20, Color.BLACK);
        JLabel patternsInfobox = new JLabel(infoIcon);
        
        patternsInfobox.setToolTipText("<html>Click and drag drag a row in<br/> this table onto the canvas to<br/> instantiate the pattern.</html>");
        final int defaultInitialDelay = ToolTipManager.sharedInstance().getInitialDelay();
        patternsInfobox.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent me) {
        	    ToolTipManager.sharedInstance().setInitialDelay(0);
            }
            public void mouseExited(MouseEvent me) {
        	    ToolTipManager.sharedInstance().setInitialDelay(defaultInitialDelay);
            }
        });
        patternsTableHeader.add(patternsInfobox);
        
		patternsTable = new PatternTable(patternsTableModel, this.getOWLModelManager());
		JScrollPane patternsTableScrollPane = new JScrollPane(patternsTable);
		patternsTable.setFillsViewportHeight(true);
		patternsTableScrollPane.setAlignmentX(LEFT_ALIGNMENT);
        this.add(patternsTableScrollPane);
        
        log.info("Pattern Selector view initialized");
    }

	@Override
	protected void disposeOWLView() {
		log.info("Pattern Selector view disposed");
	}
	
	private static class InfoIcon implements Icon {

        private int size;
        private Color color;

        public InfoIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
        	// Configure drawing settings and draw circle
        	int padding = 2;
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(x + padding, y + padding, size - 2 * padding, size - 2 * padding);
            
            // Calculate position of and draw text
            String text = "i";
            int fontSize = Math.round((float)size / 2);
            Font font = new Font(Font.MONOSPACED, Font.BOLD, fontSize);
            FontMetrics metrics = g.getFontMetrics(font);
            int stringX = x + (size - metrics.stringWidth(text)) / 2;
            int stringY = y + ((size - metrics.getHeight()) / 2) + metrics.getAscent();
            g2d.setFont(font);
            g2d.drawString(text, stringX, stringY);
        }
        
        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

}
