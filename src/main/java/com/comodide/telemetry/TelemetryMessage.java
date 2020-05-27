package com.comodide.telemetry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TelemetryMessage {
	
	private final String _sessionId;
	private final String _operation;
	private final String _parameter;
	private final SimpleDateFormat _df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	private final Date _clientTimestamp;
	
	public String getSessionId() {
		return _sessionId;
	}
	
	public String getOperation() {
		return _operation;
	}
	
	public String getParameter() {
		return _parameter;
	}
	
	public String getClientTimestamp() {
		return _df.format(_clientTimestamp);
	}
	
	public TelemetryMessage(String sessionId, String operation, String parameter) {
		_sessionId = sessionId;
		_operation = operation;
		_parameter = parameter;
		_df.setTimeZone(TimeZone.getTimeZone("UTC"));
		_clientTimestamp = new Date();
	}
}
