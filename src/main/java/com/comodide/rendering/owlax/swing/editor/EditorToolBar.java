package com.comodide.rendering.owlax.swing.editor;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.TransferHandler;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import com.comodide.rendering.owlax.IntegrateOntologyWithProtege;
import com.comodide.rendering.owlax.swing.editor.EditorActions.ColorAction;
import com.comodide.rendering.owlax.swing.editor.EditorActions.FontStyleAction;
import com.comodide.rendering.owlax.swing.editor.EditorActions.HistoryAction;
import com.comodide.rendering.owlax.swing.editor.EditorActions.KeyValueAction;
import com.comodide.rendering.owlax.swing.editor.EditorActions.NewAction;
import com.comodide.rendering.owlax.swing.editor.EditorActions.PrintAction;
import com.comodide.rendering.owlax.swing.editor.EditorActions.SaveAction;
import com.comodide.rendering.owlax.util.CustomEntityType;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

public class EditorToolBar extends JToolBar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8015443128436394471L;

	/**
	 * 
	 * @param frame
	 * @param orientation
	 */
	private boolean ignoreZoomChange = false;
	private BasicGraphEditor editor;
	private OWLDataFactory owlDataFactory;
	private OWLModelManager owlModelManager;
	private OWLOntologyManager owlOntologyManager;
	private OWLOntology activeOntology;

	/**
	 * 
	 */
	public EditorToolBar(final BasicGraphEditor editor, int orientation) {
		super(orientation);
		this.editor = editor;

		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3), getBorder()));
		setFloatable(false);

		add(editor.bind("New", new NewAction(), "/images/new.gif"));
		//add(editor.bind("Open", new OpenAction(), "/images/open.gif"));
		add(editor.bind("Export", new SaveAction(false), "/images/save.gif"));

		addSeparator();

		add(editor.bind("Print", new PrintAction(), "/images/print.gif"));

		addSeparator();

		add(editor.bind("Cut", TransferHandler.getCutAction(), "/images/cut.gif"));
		add(editor.bind("Copy", TransferHandler.getCopyAction(), "/images/copy.gif"));
		add(editor.bind("Paste", TransferHandler.getPasteAction(), "/images/paste.gif"));

		addSeparator();

		add(editor.bind("Delete", mxGraphActions.getDeleteAction(), "/images/delete.gif"));

		addSeparator();

		add(editor.bind("Undo", new HistoryAction(true), "/images/undo.gif"));
		add(editor.bind("Redo", new HistoryAction(false), "/images/redo.gif"));

		addSeparator();

		// Gets the list of available fonts from the local graphics environment
		// and adds some frequently used fonts at the beginning of the list
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		List<String> fonts = new ArrayList<String>();
		fonts.addAll(Arrays
				.asList(new String[] { "Helvetica", "Verdana", "Times New Roman", "Garamond", "Courier New", "-" }));
		fonts.addAll(Arrays.asList(env.getAvailableFontFamilyNames()));

		final JComboBox fontCombo = new JComboBox(fonts.toArray());
		fontCombo.setEditable(true);
		fontCombo.setMinimumSize(new Dimension(120, 0));
		fontCombo.setPreferredSize(new Dimension(120, 0));
		fontCombo.setMaximumSize(new Dimension(120, 100));
		add(fontCombo);

		fontCombo.addActionListener(new ActionListener() {
			/**
			 * 
			 */
			public void actionPerformed(ActionEvent e) {
				String font = fontCombo.getSelectedItem().toString();

				if (font != null && !font.equals("-")) {
					mxGraph graph = editor.getGraphComponent().getGraph();
					graph.setCellStyles(mxConstants.STYLE_FONTFAMILY, font);
				}
			}
		});

		final JComboBox sizeCombo = new JComboBox(new Object[] { "6pt", "8pt", "9pt", "10pt", "12pt", "14pt", "18pt",
				"24pt", "30pt", "36pt", "48pt", "60pt" });
		sizeCombo.setEditable(true);
		sizeCombo.setSelectedIndex(3);
		sizeCombo.setMinimumSize(new Dimension(65, 0));
		sizeCombo.setPreferredSize(new Dimension(65, 0));
		sizeCombo.setMaximumSize(new Dimension(65, 100));
		add(sizeCombo);

		sizeCombo.addActionListener(new ActionListener() {
			/**
			 * 
			 */
			public void actionPerformed(ActionEvent e) {
				mxGraph graph = editor.getGraphComponent().getGraph();
				graph.setCellStyles(mxConstants.STYLE_FONTSIZE,
						sizeCombo.getSelectedItem().toString().replace("pt", ""));
			}
		});

		addSeparator();

		add(editor.bind("Bold", new FontStyleAction(true), "/images/bold.gif"));
		add(editor.bind("Italic", new FontStyleAction(false), "/images/italic.gif"));

		addSeparator();

		add(editor.bind("Left", new KeyValueAction(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_LEFT),
				"/images/left.gif"));
		add(editor.bind("Center", new KeyValueAction(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER),
				"/images/center.gif"));
		add(editor.bind("Right", new KeyValueAction(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_RIGHT),
				"/images/right.gif"));

		addSeparator();

		add(editor.bind("Font", new ColorAction("Font", mxConstants.STYLE_FONTCOLOR), "/images/fontcolor.gif"));
		add(editor.bind("Stroke", new ColorAction("Stroke", mxConstants.STYLE_STROKECOLOR), "/images/linecolor.gif"));
		add(editor.bind("Fill", new ColorAction("Fill", mxConstants.STYLE_FILLCOLOR), "/images/fillcolor.gif"));

		addSeparator();

		final mxGraphView view = editor.getGraphComponent().getGraph().getView();
		final JComboBox zoomCombo = new JComboBox(new Object[] { "400%", "200%", "150%", "100%", "75%", "50%",
				mxResources.get("page"), mxResources.get("width"), mxResources.get("actualSize") });
		zoomCombo.setEditable(true);
		zoomCombo.setMinimumSize(new Dimension(75, 0));
		zoomCombo.setPreferredSize(new Dimension(75, 0));
		zoomCombo.setMaximumSize(new Dimension(75, 100));
		zoomCombo.setMaximumRowCount(9);
		add(zoomCombo);

		// Sets the zoom in the zoom combo the current value
		mxIEventListener scaleTracker = new mxIEventListener() {
			/**
			 * 
			 */
			public void invoke(Object sender, mxEventObject evt) {
				ignoreZoomChange = true;

				try {

					zoomCombo.setSelectedItem((int) Math.round(100 * view.getScale()) + "%");
				} finally {
					ignoreZoomChange = false;
				}
			}
		};

		// Installs the scale tracker to update the value in the combo box
		// if the zoom is changed from outside the combo box
		view.getGraph().getView().addListener(mxEvent.SCALE, scaleTracker);
		view.getGraph().getView().addListener(mxEvent.SCALE_AND_TRANSLATE, scaleTracker);

		// Invokes once to sync with the actual zoom value
		scaleTracker.invoke(null, null);

		zoomCombo.addActionListener(new ActionListener() {
			/**
			 * 
			 */
			public void actionPerformed(ActionEvent e) {
				mxGraphComponent graphComponent = editor.getGraphComponent();

				// Zoomcombo is changed when the scale is changed in the diagram
				// but the change is ignored here
				if (!ignoreZoomChange) {
					String zoom = zoomCombo.getSelectedItem().toString();

					if (zoom.equals(mxResources.get("page"))) {
						graphComponent.setPageVisible(true);
						graphComponent.setZoomPolicy(mxGraphComponent.ZOOM_POLICY_PAGE);
					} else if (zoom.equals(mxResources.get("width"))) {
						graphComponent.setPageVisible(true);
						graphComponent.setZoomPolicy(mxGraphComponent.ZOOM_POLICY_WIDTH);
					} else if (zoom.equals(mxResources.get("actualSize"))) {
						graphComponent.zoomActual();
					} else {
						try {
							zoom = zoom.replace("%", "");
							double scale = Math.min(16, Math.max(0.01, Double.parseDouble(zoom) / 100));
							graphComponent.zoomTo(scale, graphComponent.isCenterZoom());
						} catch (Exception ex) {
							JOptionPane.showMessageDialog(editor, ex.getMessage());
						}
					}
				}
			}
		});

		addSeparator();

		JComboBox dataTypeCombo = new JComboBox(getOWLDataTypes());
		dataTypeCombo.setEditable(true);

		dataTypeCombo.setMinimumSize(new Dimension(135, 0));
		dataTypeCombo.setPreferredSize(new Dimension(135, 0));
		// dataTypeCombo.setSelectedIndex(0);
		// adding this value in editor cellDataTypeValue also
		editor.setCellDataTypeValue(dataTypeCombo.getSelectedItem().toString());
		dataTypeCombo.setMaximumSize(new Dimension(135, 100));

		add(dataTypeCombo);

		dataTypeCombo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				mxGraph graph = editor.getGraphComponent().getGraph();
				mxCell selectedCell = (mxCell) graph.getSelectionCell();

				if (dataTypeCombo.getSelectedItem() != null
						&& dataTypeCombo.getSelectedItem().toString().length() > 0) {
					// adding this value in editor cellDataTypeValue also
					editor.setCellDataTypeValue(dataTypeCombo.getSelectedItem().toString());
				}
				if (selectedCell != null && selectedCell.getEntityType().getName().equals( CustomEntityType.DATATYPE.getName())) {

					editor.getGraphComponent().labelChanged(selectedCell, dataTypeCombo.getSelectedItem().toString(),
							e);
				}
				if (selectedCell != null && selectedCell.getEntityType().getName().equals(  CustomEntityType.LITERAL.getName())) {
					selectedCell.setLiteralDataType(dataTypeCombo.getSelectedItem().toString());

					String oldLabelValue = selectedCell.getValue().toString();
					String oldLabelValueOnly = "";
					Pattern pattern = Pattern.compile("\"(.*?)\"");
					Matcher matcher = pattern.matcher(oldLabelValue);
					while (matcher.find()) {
						oldLabelValueOnly = matcher.group(1);
					}

					String value = "\"" + oldLabelValueOnly + "\"" + "^^" + selectedCell.getLiteralDataType();

					editor.getGraphComponent().labelChanged(selectedCell, value, e);
				}
			}
		});

		JButton generateOntologyBtn = new JButton(mxResources.get("generateAxiom"));

		generateOntologyBtn.setMinimumSize(new Dimension(135, 0));
		generateOntologyBtn.setPreferredSize(new Dimension(135, 0));
		generateOntologyBtn.setMaximumSize(new Dimension(135, 100));

		generateOntologyBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				IntegrateOntologyWithProtege ge = new IntegrateOntologyWithProtege(editor);
				ge.generateOntology();
			}
		});
		add(generateOntologyBtn);
	}

	public Set<OWLDatatype> getBuiltinDatatypes(OWLModelManager owlModelManager) {
		Set<OWLDatatype> datatypes = new HashSet<>();
		final OWLDataFactory df = owlModelManager.getOWLDataFactory();

		datatypes.add(df.getTopDatatype());
		for (OWL2Datatype dt : OWL2Datatype.values()) {
			datatypes.add(df.getOWLDatatype(dt.getIRI()));
		}
		return datatypes;
	}

	public Set<OWLDatatype> getKnownDatatypes(OWLModelManager owlModelManager, Set<OWLOntology> onts) {
		Set<OWLDatatype> knownTypes = getBuiltinDatatypes(owlModelManager);
		for (OWLOntology ont : onts) {
			knownTypes.addAll(ont.getDatatypesInSignature());
		}
		return knownTypes;
	}

	private Object[] getOWLDataTypes() {

		// for protege
		owlModelManager = editor.getProtegeOWLModelManager();
		if (owlModelManager != null) {
			owlDataFactory = owlModelManager.getOWLDataFactory();
			owlOntologyManager = owlModelManager.getOWLOntologyManager();

			java.util.List<OWLDatatype> datatypeList = new ArrayList<>(
					getKnownDatatypes(owlModelManager, owlModelManager.getActiveOntologies()));

			// convert 2nd time. it's necessary although it's bad. Martin O
			// Con'r said entity renderer is needed.
			java.util.List<OWLDatatype> datatypeList1 = new ArrayList<>();
			for (OWLDatatype dt : datatypeList) {
				datatypeList1.add(owlDataFactory.getOWLDatatype(dt.getIRI()));
			}

			Collections.sort(datatypeList1, owlModelManager.getOWLObjectComparator());
			OWLDatatype[] dtarray = datatypeList1.toArray(new OWLDatatype[datatypeList.size()]);
			return dtarray;
		} else {
			// to run without protege
			Object[] dtarray = new Object[] { "1", "2" };
			return dtarray;
		}

	}
}
