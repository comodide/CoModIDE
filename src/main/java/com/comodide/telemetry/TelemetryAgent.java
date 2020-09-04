package com.comodide.telemetry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comodide.configuration.ComodideConfiguration;

public class TelemetryAgent {

	public static String _sessionId = UUID.randomUUID().toString();
	
	private static final Logger log = LoggerFactory.getLogger(TelemetryAgent.class);
	
	private static List<TelemetryMessage> _loggedMessages = new ArrayList<TelemetryMessage>();
	
	private static String _lastDraggedPatternName;
	
	public static void SendTelemetry() {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		TelemetryUploader upload = new TelemetryUploader(_loggedMessages);
		executorService.submit(upload);
	    executorService.shutdown();
	}
	
	public static void setLastDraggedPatternName(String patternName) {
		_lastDraggedPatternName = patternName;
	}
	
	public static void clearLog() 
	{
		_loggedMessages.clear();
	}
	
	private static boolean isTelemetryActive() {
		return ComodideConfiguration.getSendTelemetry();
	}

	public static void logLibraryClick(String parameter) {
		if (isTelemetryActive()) {
			TelemetryMessage newMessage = new TelemetryMessage(_sessionId, "Pattern library click", parameter);
			_loggedMessages.add(newMessage);
			log.debug(String.format("Logged library click: %s", parameter));
		}
	}
	
	public static void logPatternDrop() {
		if (isTelemetryActive()) {
			TelemetryMessage newMessage = new TelemetryMessage(_sessionId, "Pattern drop", _lastDraggedPatternName);
			_loggedMessages.add(newMessage);
			log.debug(String.format("Logged pattern drop: %s", _lastDraggedPatternName));
			SendTelemetry();
		}
	}
	
	public static void logTestMessage(String parameter) {
		if (isTelemetryActive()) {
			TelemetryMessage newMessage = new TelemetryMessage(_sessionId, "TelemetryAgent test method", parameter);
			_loggedMessages.add(newMessage);
			log.debug(String.format("Logged test message: %s", parameter));
			SendTelemetry();
		}
	}
}
