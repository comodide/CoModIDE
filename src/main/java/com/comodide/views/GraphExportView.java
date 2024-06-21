package com.comodide.views;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.swing.mxGraphComponent;
import com.comodide.helper.GraphComponentHolder;

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

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class GraphExportView extends AbstractOWLViewComponent
{

    //using singleton class instance to get graph component of BasicGraphEditor class
    GraphComponentHolder obj=GraphComponentHolder.getInstance();
    mxGraphComponent graphComponent=obj.getComponent();

    //method to export graph in jpg/png format
    public static void exportToFile(String outputFilepath,mxGraphComponent graphComponent,String fileType) throws IOException
    {

            BufferedImage image = mxCellRenderer.createBufferedImage
                    (graphComponent.getGraph(), null, 1, Color.WHITE, graphComponent.isAntiAlias(), null, graphComponent.getCanvas());

            File fileName = new File(outputFilepath);
            ImageIO.write(image, fileType, fileName);

    }
    //method to export graph in pdf format
    public static void exportToPdf(String outputFilepath, mxGraphComponent graphComponent) throws IOException
    {

        BufferedImage bufferedImage = mxCellRenderer.createBufferedImage
                (graphComponent.getGraph(), null, 1, Color.WHITE, graphComponent.isAntiAlias(), null, graphComponent.getCanvas());

        String tempFilepath="temp.png";
        File fileName = new File(tempFilepath);
        ImageIO.write(bufferedImage, "PNG", fileName);

        PDDocument pdDocument = new PDDocument();
        PDPage pdPage = new PDPage();
        pdDocument.addPage(pdPage);
        PDImageXObject pdImage = PDImageXObject.createFromFile(tempFilepath, pdDocument);

        PDPageContentStream pdPageContentStream = new PDPageContentStream(pdDocument, pdPage);
        pdPageContentStream.drawImage(pdImage, 50, 50);
        pdPageContentStream.close();

        pdDocument.save(outputFilepath);
        pdDocument.close();

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

    //adding action listener for pdf button
    private  final ActionListener pdfButtonsListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser jfc = new JFileChooser();
            jfc.setDialogTitle("Save as pdf");
            int userSelection = jfc.showSaveDialog(graphComponent);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jfc.getSelectedFile();
                String selectedFileAbsolutePath = (selectedFile.getAbsolutePath()).toLowerCase();

                if (!selectedFileAbsolutePath.endsWith(".pdf")) {
                    selectedFileAbsolutePath += "pdf";
                }
                try {
                    exportToPdf(selectedFileAbsolutePath, graphComponent);
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
        JButton pdfExportButton = new JButton("PDF");

        jpgExportButton.addActionListener(jpgButtonsListener);
        pngExportButton.addActionListener(pngButtonsListener);
        pdfExportButton.addActionListener(pdfButtonsListener);

        this.add(jpgExportButton);
        this.add(pngExportButton);
        this.add(pdfExportButton);

    }

    @Override
    protected void disposeOWLView() {

    }

}

