/**
 * Copyright (c) 2006-2012, JGraph Ltd */
package com.comodide.rendering.owlax;

import java.awt.Color;
import java.awt.Point;
import java.net.URL;
import java.text.NumberFormat;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;

import org.protege.editor.owl.model.OWLModelManager;
import org.w3c.dom.Document;

import com.comodide.rendering.owlax.swing.editor.BasicGraphEditor;
import com.comodide.rendering.owlax.swing.editor.EditorMenuBar;
import com.comodide.rendering.owlax.swing.editor.EditorPalette;
import com.comodide.rendering.owlax.util.CustomEntityType;
import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.swing.util.mxSwingConstants;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

public class GraphEditor extends BasicGraphEditor {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4601740824088314699L;

	/**
	 * Holds the shared number formatter.
	 * 
	 * @see NumberFormat#getInstance()
	 */
	public static final NumberFormat numberFormat = NumberFormat.getInstance();

	/**
	 * Holds the URL for the icon to be used as a handle for creating new
	 * connections. This is currently unused.
	 */
	public static URL url = null;

	private int edgeWidth = 60;
	private int edgeHeight = 80;
	private int vertexWidth = 60;
	private int vertexHeight = 50;
	private int edgeStrokeWidth = 3;
	private int edgEendSize = 8;

	// GraphEditor.class.getResource("/images/connector.gif");

	public GraphEditor(OWLModelManager protegeOWLModelManager) {
		this(protegeOWLModelManager, "OWLAx", new CustomGraphComponent(new CustomGraph()));
	}

	/**
	 * 
	 */
	public GraphEditor(OWLModelManager protegeOWLModelManager, String appTitle, mxGraphComponent component) {
		super(protegeOWLModelManager, appTitle, component);

		final mxGraph graph = graphComponent.getGraph();

		// Creates the shapes palette
		EditorPalette shapesPalette = insertPalette(mxResources.get("shapes"));

		// Sets the edge template to be used for creating new edges if an edge
		// is clicked in the shape palette
		shapesPalette.addListener(mxEvent.SELECT, new mxIEventListener() {
			public void invoke(Object sender, mxEventObject evt) {
				Object tmp = evt.getProperty("transferable");

				if (tmp instanceof mxGraphTransferable) {
					mxGraphTransferable t = (mxGraphTransferable) tmp;
					Object cell = t.getCells()[0];

					if (graph.getModel().isEdge(cell)) {
						((CustomGraph) graph).setEdgeTemplate(cell);
					}
				}
			}

		});

		shapesPalette.addTemplate(CustomEntityType.CLASS.getName(),
				new ImageIcon(GraphEditor.class.getResource("/images/rectangle.png")),
				"rectangle;fillColor=#33CCFF;gradientColor=#33CCFF", vertexWidth, vertexHeight, "");

		shapesPalette.addTemplate(CustomEntityType.NAMED_INDIVIDUAL.getName(),
				new ImageIcon(GraphEditor.class.getResource("/images/ellipse.png")),
				"ellipse;shape=ellipse;fillColor=white;gradientColor=white", vertexWidth, vertexHeight, "");

		shapesPalette.addTemplate(CustomEntityType.DATATYPE.getName(),
				new ImageIcon(GraphEditor.class.getResource("/images/rounded.png")),
				"rounded=1;fillColor=#FFFA01;gradientColor=#FFFA01", vertexWidth, vertexHeight, "");

		shapesPalette.addTemplate(CustomEntityType.LITERAL.getName(),
				new ImageIcon(GraphEditor.class.getResource("/images/doublerectangle.png")),
				"rectangle;shape=doubleRectangle;fillColor=white;gradientColor=white", vertexWidth, vertexHeight, "");

		shapesPalette.addEdgeTemplate(CustomEntityType.OBJECT_PROPERTY.getName(),
				new ImageIcon(GraphEditor.class.getResource("/images/arrowblack.png")),
				"edgeStyle=mxEdgeStyle.OrthConnector;strokeWidth=3;strokeColor=black;endArrow=classic;endSize=8",
				edgeWidth, edgeHeight, "");

		shapesPalette.addEdgeTemplate(CustomEntityType.DATA_PROPERTY.getName(),
				new ImageIcon(GraphEditor.class.getResource("/images/dataproperty .png")),
				"edgeStyle=mxEdgeStyle.OrthConnector;strokeWidth=3;strokeColor=#999999;endArrow=block;endSize=8",
				edgeWidth, edgeHeight, "");

		shapesPalette.addEdgeTemplate(CustomEntityType.RDFTYPE.getName(),
				new ImageIcon(GraphEditor.class.getResource("/images/connect.png")), null, edgeWidth, edgeHeight,
				CustomEntityType.RDFTYPE.getName());

		shapesPalette.addEdgeTemplate(CustomEntityType.RDFSSUBCLASS_OF.getName(),
				new ImageIcon(GraphEditor.class.getResource("/images/connect.png")), null, edgeWidth, edgeHeight,
				CustomEntityType.RDFSSUBCLASS_OF.getName());

	}

	/**
	 * 
	 */
	public static class CustomGraphComponent extends mxGraphComponent {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6833603133512882012L;

		/**
		 * 
		 * @param graph
		 */
		public CustomGraphComponent(mxGraph graph) {
			super(graph);

			// set editor to get reference not sure it will work or not
			this.setGraphEditor(getGraphEditor());

			/**
			 * 
			 */

			// Sets switches typically used in an editor
			setCenterPage(true);
			// setPageVisible(true);
			// setGridVisible(true);
			setToolTips(true);

			// creating annonying auto copying target
			// getConnectionHandler().setCreateTarget(true);
			// get rid from annonying auto copying
			getConnectionHandler().setEnabled(false);

			// Loads the defalt stylesheet from an external file
			mxCodec codec = new mxCodec();
			Document doc = mxUtils
					.loadDocument(GraphEditor.class.getResource("/resources/default-style.xml").toString());
			codec.decode(doc.getDocumentElement(), graph.getStylesheet());

			// Sets the background to white
			getViewport().setOpaque(true);
			getViewport().setBackground(Color.WHITE);

		}

		/**
		 * Overrides drop behaviour to set the cell style if the target is not a
		 * valid drop target and the cells are of the same type (eg. both
		 * vertices or both edges).
		 */
		public Object[] importCells(Object[] cells, double dx, double dy, Object target, Point location) {
			if (target == null && cells.length == 1 && location != null) {
				target = getCellAt(location.x, location.y);

				if (target instanceof mxICell && cells[0] instanceof mxICell) {
					mxICell targetCell = (mxICell) target;
					mxICell dropCell = (mxICell) cells[0];

					if (targetCell.isVertex() == dropCell.isVertex() || targetCell.isEdge() == dropCell.isEdge()) {
						// mxIGraphModel model = graph.getModel();
						// model.setStyle(target, model.getStyle(cells[0]));
						graph.setSelectionCell(target);
						return null;
					}
				}
			}
			// show dataTypes as list. will not do this.
			// add dataType
			for (Object cell : cells) {
				mxCell currentCell = (mxCell) cell;
				if (currentCell != null) {
					if (currentCell.getEntityType().getName().equals(CustomEntityType.DATATYPE.getName())) {
						this.labelChanged(currentCell, cellDataTypeValue, null);
					} else if (currentCell.getEntityType().getName().equals(CustomEntityType.LITERAL.getName())) {
						String cellValue = "\"" + "\"" + "^^" + cellDataTypeValue;
						currentCell.setLiteralDataType(cellDataTypeValue);
						this.labelChanged(currentCell, cellValue, null);
					}
				}
			}
			return super.importCells(cells, dx, dy, target, location);
		}

	}

	/**
	 * A graph that creates new edges from a given template edge.
	 */
	public static class CustomGraph extends mxGraph {
		/**
		 * Holds the edge to be used as a template for inserting new edges.
		 */
		protected Object edgeTemplate;
		GraphEditor graphEditor;

		/**
		 * Custom graph that defines the alternate edge style to be used when
		 * the middle control point of edges is double clicked (flipped).
		 */
		public CustomGraph() {
		}

		/**
		 * Sets the edge template to be used to inserting edges.
		 */
		public void setEdgeTemplate(Object template) {
			edgeTemplate = template;
		}

		/**
		 * Sets the status in the status bar.
		 */
		@Override
		public void setStatus(String msg) {
			// need to implement this

		}

		/**
		 * Prints out some useful information about the cell in the tooltip.
		 */
		public String getToolTipForCell(Object cell) {
			// have to change
			String headTip = "<html>";
			mxGeometry geo = getModel().getGeometry(cell);
			mxCellState state = getView().getState(cell);

			mxCell src = (mxCell) getModel().getTerminal(cell, true);
			mxCell trg = (mxCell) getModel().getTerminal(cell, false);
			mxCell thiscell = (mxCell) cell;

			headTip = headTip + "<h4>Entity Type: " + thiscell.getEntityType().getName() + "</h4>" + "<p>";

			String paragraphTip = thiscell.getValue().toString();
			// tip = tip + "<p>" + thiscell.getValue().toString();

			if (getModel().isEdge(thiscell)) {
				if (src != null) {
					if (src.getValue().toString().length() > 0)
						paragraphTip = src.getValue().toString() + " -> " + paragraphTip;
				}
				if (trg != null) {
					if (trg.getValue().toString().length() > 0)
						paragraphTip = paragraphTip + " -> " + trg.getValue().toString();
				}
			} else {

			}
			String tip = headTip + paragraphTip + "</p></html>";

			return tip;
		}

		/**
		 * Overrides the method to use the currently selected edge template for
		 * new edges.
		 * 
		 * @param graph
		 * @param parent
		 * @param id
		 * @param value
		 * @param source
		 * @param target
		 * @param style
		 * @return
		 */
		public Object createEdge(Object parent, String id, Object value, Object source, Object target, String style,
				CustomEntityType entityType) {
			if (edgeTemplate != null) {
				mxCell edge = (mxCell) cloneCells(new Object[] { edgeTemplate })[0];
				edge.setId(id);

				return edge;
			}

			return super.createEdge(parent, id, value, source, target, style, entityType);
		}

	}

	/**
	 * Only for Debug Purpose
	 * 
	 * @param args
	 */
//	public static void main(String[] args) {
//
//		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		} catch (Exception e1) {
//			//e1.printStackTrace();
//		}
//
//		mxSwingConstants.SHADOW_COLOR = Color.LIGHT_GRAY;
//		mxConstants.W3C_SHADOWCOLOR = "#D3D3D3";
//
//		GraphEditor editor = new GraphEditor(null);
//
//		JFrame frame = new JFrame("Dase editor");
//		frame.add(editor);
//		frame.setJMenuBar(new EditorMenuBar(editor));
//		frame.setSize(800, 600);
//		frame.setVisible(true);
//
//	}
}
