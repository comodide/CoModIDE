package com.comodide.telemetry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TelemetryAgent {

	private static final Logger log = LoggerFactory.getLogger(TelemetryAgent.class);
	
	public static String SendTelemetry(ITelemetryMessage message) {
		
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			
			String jsonInputString = mapper.writeValueAsString(message);
			
			URL url = new URL("http://localhost:5000/telemetry");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setRequestProperty("Accept", "text/plain");
			con.setDoOutput(true);
			
			try(OutputStream os = con.getOutputStream()) {
			    byte[] input = jsonInputString.getBytes("utf-8");
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
			log.error(String.format("JSON processing failed for object with message %s; exception message: %s", message.getMessage(), e.getLocalizedMessage()));
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
}
