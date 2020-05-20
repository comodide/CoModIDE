package com.comodide.telemetry;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TelemetryMessage implements ITelemetryMessage {
	
	private final SimpleDateFormat _df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	private final Date _timestamp;
	private final String _message;
	
	public String getTimestamp() {
		return _df.format(_timestamp);
	}
	
	public String getMessage() {
		return _message;
	}

	public TelemetryMessage(String message) {
		_timestamp = new Date(); 
		_message = message;
	}

}
