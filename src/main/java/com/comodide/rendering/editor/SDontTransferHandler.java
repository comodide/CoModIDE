package com.comodide.rendering.editor;

import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;

import org.protege.editor.owl.model.OWLModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxGraphTransferHandler;
import com.mxgraph.swing.util.mxGraphTransferable;

public class SDontTransferHandler extends mxGraphTransferHandler
{
    /** Bookkeeping */
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(SDontTransferHandler.class);

    /** OWLAPI Integration */
    private OWLModelManager modelManager;
    
    public SDontTransferHandler(OWLModelManager modelManager)
    {
        super();
        this.modelManager = modelManager;
    }

    /**
     * Checks if the mxGraphTransferable data flavour is supported and calls
     * importGraphTransferable if possible.
     */
    @Override
    public boolean importData(JComponent c, Transferable t)
    {
        boolean result = false;

        if (isLocalDrag())
        {
            // Enables visual feedback on the Mac
            result = true;
        } else
        {
            try
            {
                updateImportCount(t);

                if (c instanceof mxGraphComponent)
                {
                    mxGraphComponent graphComponent = (mxGraphComponent) c;

                    if (graphComponent.isEnabled() && t.isDataFlavorSupported(mxGraphTransferable.dataFlavor))
                    {
                        mxGraphTransferable gt = (mxGraphTransferable) t
                                .getTransferData(mxGraphTransferable.dataFlavor);

                        if (gt.getCells() != null)
                        {
                            result = importGraphTransferable(graphComponent, gt);
                            
                            System.out.println(t.getClass() + " was just successfully dropped.");
                        }

                    }
                }
            } catch (Exception ex)
            {
                log.warn("[CoModIDE:sdTransferHandler] Failed to import data", ex);
            }
        }

        return result;
    }
}
