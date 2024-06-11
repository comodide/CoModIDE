package com.comodide.views;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.swing.mxGraphComponent;
import com.comodide.helper.Singleton;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class Export extends AbstractOWLViewComponent
{

    //using singleton class instance to get graph component of BasicGraphEditor class
    Singleton instance=Singleton.getInstance();
    mxGraphComponent graphComponent=instance.getComponent();

    //variable timestamp holds current timestamp
    SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMdd_HHmmss");
    String timestamp=dateFormat.format(new Date());

    //method to export graph in jpg format
    public static void exportToJpg(String outputFilepath,mxGraphComponent graphComponent) throws IOException
    {

            BufferedImage image = mxCellRenderer.createBufferedImage
                    (graphComponent.getGraph(), null, 1, Color.WHITE, graphComponent.isAntiAlias(), null, graphComponent.getCanvas());

            File fileName = new File(outputFilepath);
            ImageIO.write(image, "JPG", fileName);

    }

    //method to export graph in png format
    public static void exportToPng(String outputFilepath,mxGraphComponent graphComponent) throws IOException
    {

        BufferedImage image = mxCellRenderer.createBufferedImage
                (graphComponent.getGraph(), null, 1, Color.WHITE, graphComponent.isAntiAlias(), null, graphComponent.getCanvas());

        File fileName = new File(outputFilepath);
        ImageIO.write(image, "PNG", fileName);


    }

    //adding action listener to jpg export button for exporting graph to jpg format
    private  final ActionListener JPGButtonsListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            try
            {
                Export.exportToJpg("OntologyGraph_"+timestamp+".jpg",graphComponent);
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
            JOptionPane.showMessageDialog(null, "File Exported Successfully" ,"Export Status", JOptionPane.INFORMATION_MESSAGE);
        }
    };

    //adding action listener to png export button for exporting graph to png format
    private  final ActionListener PNGButtonsListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            try
            {
                Export.exportToPng("OntologyGraph_"+timestamp+".png",graphComponent);
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
            JOptionPane.showMessageDialog(null, "File Exported Successfully" ,"Export Status", JOptionPane.INFORMATION_MESSAGE);
        }
    };


    @Override
    protected void initialiseOWLView() throws Exception {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel buttonLabel = new JLabel("Select Export Format:");
        Font labelFont = buttonLabel.getFont();
        buttonLabel.setFont(labelFont.deriveFont(labelFont.getStyle() | Font.BOLD));
        this.add(buttonLabel);

        JButton jpgExport = new JButton("JPG");
        JButton PngExport = new JButton("PNG");
        JButton PdfExport = new JButton("PDF");

        jpgExport.addActionListener(JPGButtonsListener);
        PngExport.addActionListener(PNGButtonsListener);

        this.add(jpgExport);
        this.add(PngExport);

    }

    @Override
    protected void disposeOWLView() {

    }


}

