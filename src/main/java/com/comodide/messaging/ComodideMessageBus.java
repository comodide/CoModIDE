package com.comodide.messaging;

import java.util.ArrayList;
import java.util.HashMap;

public class ComodideMessageBus
{
	// The Singleton instance
	private static ComodideMessageBus cmb = null;

	// The message bus
	private HashMap<ComodideMessage, ArrayList<ComodideMessageHandler>> messageBus;
	
	private ComodideMessageBus()
	{
		this.messageBus = new HashMap<>();
		for(ComodideMessage message : ComodideMessage.values())
		{
			this.messageBus.put(message, new ArrayList<ComodideMessageHandler>());
		}
		// Finish
		cmb = this;
	}

	public static ComodideMessageBus getSingleton()
	{
		if (cmb == null)
		{
			new ComodideMessageBus();
		}

		return cmb;
	}
	
	public boolean sendMessage(ComodideMessage message, Object payload)
	{
		boolean result = false;
		
		// Handle the message
		if(this.messageBus.containsKey(message))
		{
			ArrayList<ComodideMessageHandler> handlers = this.messageBus.get(message);
			
			for(ComodideMessageHandler handler : handlers)
			{
				boolean r = handler.handleComodideMessage(message, payload);
				result = r || result;
			}
		}
		
		// return the result, can be ignored
		return result;
	}
	
	public void registerHandler(ComodideMessage message, ComodideMessageHandler handler)
	{
		messageBus.get(message).add(handler);
	}
}
