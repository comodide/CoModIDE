package com.comodide.patterns;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class PatternsPanel extends JPanel {

	private static final long serialVersionUID = 3623229347059347252L;
	
	private String[] patternCategories = {
    		"Academy",
    		"Agriculture",
    		"Biology",
    		"Building and Construction",
    		"Business"
	};
	
	private String[][] patternsTablePlaceholderData = {
    		{"Action","http://www.ontologydesignpatterns.org/cp/owl/action.owl"},
    		{"Description","http://www.ontologydesignpatterns.org/cp/owl/description.owl"},
    		{"Nary Relation","http://www.ontologydesignpatterns.org/cp/owl/naryrelation.owl"},
    		{"Participation","http://www.ontologydesignpatterns.org/cp/owl/participation.owl"},
    		{"Object Record","http://www.ontologydesignpatterns.org/cp/owl/objectrecord.owl"},
    		{"Communication Event","http://www.ontologydesignpatterns.org/cp/owl/communicationevent.owl"},
    		{"Event Core","http://www.ontologydesignpatterns.org/cp/owl/eventcore.owl"},
    		{"Information Realization","http://www.ontologydesignpatterns.org/cp/owl/informationrealization.owl"},
    };
	
	private JTable patternsTable;
	private DefaultTableModel patternsTableModel = new DefaultTableModel(patternsTablePlaceholderData, new String[]{"Name","IRI"}) {
		private static final long serialVersionUID = 8811235031396256734L;
		@Override
		public boolean isCellEditable(int row, int column){  
	          return false;  
	      }
	};

	public PatternsPanel() {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JLabel patternLibraryHeading = new JLabel("Pattern Library");
		Font f = patternLibraryHeading.getFont();
		patternLibraryHeading.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		this.add(patternLibraryHeading);
		
		JLabel categorySelectorLabel = new JLabel("Pattern Category Selector:");
        this.add(categorySelectorLabel);
        // This is a hack due to JComboBox misbehaving; see https://stackoverflow.com/questions/7581846/swing-boxlayout-problem-with-jcombobox-without-using-setxxxsize
        JComboBox<String> categoryList = new JComboBox<String>(this.patternCategories) {
			private static final long serialVersionUID = 4795749883863962239L;
			@Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                max.height = getPreferredSize().height;
                return max;
            }
        };
        this.add(categoryList);
		
        JLabel patternsTableHeading = new JLabel("Patterns:");
        this.add(patternsTableHeading);
		patternsTable = new JTable(patternsTableModel);
		JScrollPane patternsTableScrollPane = new JScrollPane(patternsTable);
		
		patternsTable.setFillsViewportHeight(true);
		patternsTable.setColumnSelectionAllowed(false);
		patternsTable.setRowSelectionAllowed(true);
		ListSelectionModel selectionModel = patternsTable.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.add(patternsTableScrollPane);
	}
}
