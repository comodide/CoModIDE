package com.comodide.rendering.editor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.swing.handler.mxConnectionHandler;

public class SDConnectionHandler extends mxConnectionHandler
{
	/** Bookkeeping */
	private final Logger log = LoggerFactory.getLogger(SDConnectionHandler.class);
	private final String pf  = "[CoModIDE:SDConnectionHandler] ";
	
	public SDConnectionHandler(SDontComponent sdComponent)
	{
		super(sdComponent);
		log.info(pf + "SDConnectionHandler Initialized.");
	}
}
