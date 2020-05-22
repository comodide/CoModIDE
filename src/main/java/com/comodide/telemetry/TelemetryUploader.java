package com.comodide.telemetry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TelemetryUploader implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(TelemetryUploader.class);

	private static List<TelemetryMessage> _messages;

	public TelemetryUploader(List<TelemetryMessage> messages) {
		_messages = messages;
	}

	@Override
	public void run() {
		ObjectMapper mapper = new ObjectMapper();
		try 
		{
			String telemetryMessagesAsJson = mapper.writeValueAsString(_messages);
			log.debug(String.format("Sending telemetry message: '%s'", telemetryMessagesAsJson));

			URL url = new URL("http://localhost:26003/telemetry");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");

			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setRequestProperty("Accept", "text/plain");
			con.setDoOutput(true);

			try (OutputStream os = con.getOutputStream()) 
			{
				byte[] input = telemetryMessagesAsJson.getBytes("utf-8");
				os.write(input, 0, input.length);
			}

			try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) 
			{
				StringBuilder response = new StringBuilder();
				String responseLine = null;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}

				log.debug(String.format("Submitted telemetry -- got response '%s'", response.toString()));
				if (response.toString() == "Ok!") {
					TelemetryAgent.clearLog();
				}
			}
		} 
		catch (JsonProcessingException e) 
		{
			log.error(String.format("JSON processing failed for telemetry list '%s'; exception message: %s", _messages,
					e.getLocalizedMessage()));
		} 
		catch (ProtocolException e) 
		{
			log.error(String.format("ProtocolException thrown by TelemetryUploader. Inner exception: '%s'", e.getLocalizedMessage()));
		} 
		catch (MalformedURLException e) 
		{
			log.error(String.format("MalformedURLException thrown by TelemetryUploader. Inner exception: '%s'", e.getLocalizedMessage()));
		} 
		catch (IOException e) 
		{
			log.error(String.format("IOException thrown by TelemetryUploader. Inner exception: '%s'", e.getLocalizedMessage()));
		}
	}
}
