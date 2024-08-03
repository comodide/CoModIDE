package com.comodide.views;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.swing.mxGraphComponent;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphExportView extends AbstractOWLViewComponent
{
    private static final Logger log              = LoggerFactory.getLogger(EdgeInspectorView.class);
    private static mxGraphComponent graphComponent;

    //public method to set value of private static member graphComponent
    public static void setCurrentGraphComponent(mxGraphComponent component)
    {
        graphComponent=component;
    }

    //method to export graph in jpg/png format
    public static void exportToFile(String outputFilepath,mxGraphComponent graphComponent,String fileType) throws IOException
    {
        BufferedImage image = mxCellRenderer.createBufferedImage
                (graphComponent.getGraph(), null, 1, Color.WHITE, graphComponent.isAntiAlias(), null, graphComponent.getCanvas());

        File fileName = new File(outputFilepath);
        ImageIO.write(image, fileType, fileName);

    }

    //adding action listener to jpg export button for exporting graph to jpg format
    private  final ActionListener jpgButtonsListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser jfc = new JFileChooser();
            jfc.setDialogTitle("Save as jpg");
            int userSelection = jfc.showSaveDialog(graphComponent);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jfc.getSelectedFile();
                String selectedFileAbsolutePath = (selectedFile.getAbsolutePath()).toLowerCase();

                if (!selectedFileAbsolutePath.endsWith(".jpg")) {
                    selectedFileAbsolutePath += ".jpg";
                }
                try {
                    exportToFile(selectedFileAbsolutePath, graphComponent,"JPG");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                JOptionPane.showMessageDialog(null, "Graph Exported Successfully", "Export Status", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    };

    //adding action listener to png export button for exporting graph to png format
    private  final ActionListener pngButtonsListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser jf = new JFileChooser();
            jf.setDialogTitle("Save as png");
            int userSelection = jf.showSaveDialog(graphComponent);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jf.getSelectedFile();
                String selectedFileAbsolutePath = (selectedFile.getAbsolutePath()).toLowerCase();


                if (!selectedFileAbsolutePath.endsWith(".png")) {
                    selectedFileAbsolutePath += ".png";
                }
                try {
                    exportToFile(selectedFileAbsolutePath, graphComponent,"PNG");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                JOptionPane.showMessageDialog(null, "Graph Exported Successfully", "Export Status", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    };


    @Override
    protected void initialiseOWLView() throws Exception {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel buttonLabel = new JLabel("Select Export Format:");
        Font labelFont = buttonLabel.getFont();
        buttonLabel.setFont(labelFont.deriveFont(labelFont.getStyle() | Font.BOLD));
        this.add(buttonLabel);

        JButton jpgExportButton = new JButton("JPG");
        JButton pngExportButton = new JButton("PNG");
        JButton pdfExportButton = new JButton("PDF Button Under Construction");

        jpgExportButton.addActionListener(jpgButtonsListener);
        pngExportButton.addActionListener(pngButtonsListener);

        this.add(jpgExportButton);
        this.add(pngExportButton);
        this.add(pdfExportButton);
        log.info("[CoModIDE:GraphExportView] Successfully Initialised.");

    }

    @Override
    protected void disposeOWLView()
    {
        log.info("[CoModIDE:GraphExportView] Successfully Disposed.");
    }

}
