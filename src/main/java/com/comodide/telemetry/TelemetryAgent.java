package com.comodide.telemetry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TelemetryAgent {

	public static String _sessionId = UUID.randomUUID().toString();
	
	private static final Logger log = LoggerFactory.getLogger(TelemetryAgent.class);
	
	private static List<TelemetryMessage> _loggedMessages = new ArrayList<TelemetryMessage>();
	
	public static String SendTelemetry() {
		
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			
			String telemetryMessagesAsJson = mapper.writeValueAsString(_loggedMessages);
			log.debug("Sending telemetry message: '%s'", telemetryMessagesAsJson);
			
			URL url = new URL("http://localhost:5000/telemetry");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setRequestProperty("Accept", "text/plain");
			con.setDoOutput(true);
			
			try(OutputStream os = con.getOutputStream()) {
			    byte[] input = telemetryMessagesAsJson.getBytes("utf-8");
			    os.write(input, 0, input.length);           
			}
			
			try(BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) 
			{
				StringBuilder response = new StringBuilder();
			    String responseLine = null;
			    while ((responseLine = br.readLine()) != null) {
			    	response.append(responseLine.trim());
			    }
			    
			    return(response.toString());
			}
		} 
		catch (JsonProcessingException e) {
			log.error(String.format("JSON processing failed for telemetry list '%s'; exception message: %s", _loggedMessages, e.getLocalizedMessage()));
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void logLibraryClick(String parameter) {
		TelemetryMessage newMessage = new TelemetryMessage(_sessionId, "Pattern library click", parameter);
		_loggedMessages.add(newMessage);
		log.debug(String.format("Logged library click: %s", parameter));
	}
	
	public static void logPatternDrop(String parameter) {
		TelemetryMessage newMessage = new TelemetryMessage(_sessionId, "Pattern drop", parameter);
		_loggedMessages.add(newMessage);
		log.debug(String.format("Logged pattern drop: %s", parameter));
		// TODO: send all logged telemetry on separate thread; clear logged messages if successful
	}
	
	public static void logTestMessage(String parameter) {
		TelemetryMessage newMessage = new TelemetryMessage(_sessionId, "TelemetryAgent test method", parameter);
		_loggedMessages.add(newMessage);
		log.debug(String.format("Logged test message: %s", parameter));
		// TODO: send all logged telemetry on separate thread; clear logged messages if successful
	}
}
