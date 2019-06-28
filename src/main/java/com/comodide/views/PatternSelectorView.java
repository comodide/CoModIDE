package com.comodide.views;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.patterns.PatternCategory;
import com.comodide.patterns.PatternLibrary;
import com.comodide.patterns.PatternTable;
import com.comodide.patterns.PatternTableModel;

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
		
		JLabel categorySelectorLabel = new JLabel("Pattern category selector:");
		Font f = categorySelectorLabel.getFont();
		categorySelectorLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		categorySelectorLabel.setAlignmentX(LEFT_ALIGNMENT);
        this.add(categorySelectorLabel);
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
        categoryList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				PatternCategory selectedCategory = (PatternCategory)categoryList.getSelectedItem();
				patternsTableModel.update(patternLibrary.getPatternsForCategory(selectedCategory));				
			}
        });
        categoryList.setAlignmentX(LEFT_ALIGNMENT);
        this.add(categoryList);
		
        JLabel patternsTableHeading = new JLabel("Patterns:");
        patternsTableHeading.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
        patternsTableHeading.setAlignmentX(LEFT_ALIGNMENT);
        this.add(patternsTableHeading);
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
}
