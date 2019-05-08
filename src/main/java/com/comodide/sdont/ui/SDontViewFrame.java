package com.comodide.sdont.ui;

import javax.swing.JFrame;

import com.mxgraph.swing.mxGraphComponent;

public class SDontViewFrame extends JFrame
{
	/* Bookkeeping */
	private static final long serialVersionUID = 1L;

	public SDontViewFrame(mxGraphComponent graphComponent)
	{
		super("Example");
		getContentPane().add(graphComponent);
		setSize(1000, 1000);
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
