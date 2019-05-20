/**
 * This code is adapted from the Graph Editor example provided by JGraph Ltd
 * https://github.com/jgraph/jgraphx
 * Copyright (c) 2006-2012, JGraph Ltd 
 * */
package com.comodide.rendering.editor;

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.swing.util.mxSwingConstants;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraph;

public class GraphEditor extends BasicGraphEditor
{
    private static final long serialVersionUID = -4601740824088314699L;

    public GraphEditor(SDontComponent sdComponent)
    {
        this("Schema Diagram Editor", sdComponent);
    }
    
    public GraphEditor(String appTitle, SDontComponent component)
    {
        super(appTitle, component);
        final mxGraph graph = graphComponent.getGraph();

        // Creates the shapes palette
        EditorPalette shapesPalette = insertPalette(mxResources.get("shapes"));

        // Sets the edge template to be used for creating new edges if an edge
        // is clicked in the shape palette
        shapesPalette.addListener(mxEvent.SELECT, new mxIEventListener()
        {
            public void invoke(Object sender, mxEventObject evt)
            {
                Object tmp = evt.getProperty("transferable");

                if (tmp instanceof mxGraphTransferable)
                {
                    mxGraphTransferable t = (mxGraphTransferable) tmp;
                    Object cell = t.getCells()[0];

                    if (graph.getModel().isEdge(cell))
                    {
                        ((SchemaDiagram) graph).setEdgeTemplate(cell);
                    }
                }
            }

        });

        /*
         * Add some template cells for dropping into the graph. 
         * rounded rectangle: class
         * ellipse: datatype 
         * arrow: relation
         */
        shapesPalette // This can probably be used for allocating modules/patterns?
                .addTemplate("Container", new ImageIcon(GraphEditor.class.getResource("/images/swimlane.png")),
                        "swimlane", 280, 280, "Container");
        shapesPalette // class
                .addTemplate("Rounded Rectangle", new ImageIcon(GraphEditor.class.getResource("/images/rounded.png")),
                        SDConstants.classShape, 120, 30, "");
        shapesPalette // dataype
                .addTemplate("Ellipse", new ImageIcon(GraphEditor.class.getResource("/images/ellipse.png")), SDConstants.datatypeShape,
                        120, 30, "");
        shapesPalette // relation
                .addEdgeTemplate("Straight", new ImageIcon(GraphEditor.class.getResource("/images/straight.png")),
                        "straight", 120, 120, "");
        shapesPalette // subclass
                .addEdgeTemplate("Arrow", new ImageIcon(GraphEditor.class.getResource("/images/arrow.png")), "arrow",
                        120, 120, "");
    }

    /** For debugging purposes */
    public static void main(String[] args)
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e1)
        {
            e1.printStackTrace();
        }

        mxSwingConstants.SHADOW_COLOR = Color.LIGHT_GRAY;
        mxConstants.W3C_SHADOWCOLOR = "#D3D3D3";

        GraphEditor editor = new GraphEditor(new SDontComponent(null, null));
        editor.createFrame(null).setVisible(true);
    }
}
