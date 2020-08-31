package com.comodide.messaging;

public interface ComodideMessageHandler
{
	public boolean handleComodideMessage(ComodideMessage message, Object payload);
}
