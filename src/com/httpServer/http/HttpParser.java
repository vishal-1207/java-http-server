package com.httpServer.http;

import java.io.BufferedReader;
import java.io.IOException;

public class HttpParser {
	
	public static HttpRequest parse(BufferedReader reader) throws IOException {
		HttpRequest request = new HttpRequest();
		
		String requestLine = reader.readLine();
		if (requestLine == null || requestLine.isEmpty()) {
			return request;
		}
		
		String[] parts = requestLine.split(" ");
		request.setMethod(parts[0]);
		request.setRawPath(parts.length > 1 ? parts[1] : "/");
		request.setVersion(parts.length > 2 ? parts[2] : "HTTP/1.1");
		
		parsePathAndQuery(request);
		
		String line;
		while ((line = reader.readLine()) != null && !line.isEmpty()) {
			int index = line.indexOf(':');
			
			if (index > 0) {
				String key = line.substring(0, index).trim();
				String value = line.substring(index + 1).trim();
				request.getHeaders().put(key, value);
			}
		}
		
		if (request.getHeaders().containsKey("Content-Length")) {
			int length = Integer.parseInt(request.getHeaders().get("Content-Length"));
			char[] bodyChars = new char[length];
			int read = reader.read(bodyChars);
			
			if (read > 0) {
				request.setBody(new String(bodyChars, 0, read));
			}
		}
		
		return request;
	}
	
	private static void parsePathAndQuery(HttpRequest request) {
		String raw = request.getRawPath();
		int qIndex = raw.indexOf('?');
		
		if (qIndex > 0) {
			request.setPath(raw.substring(0, qIndex));
			String query = 	raw.substring(qIndex + 1);
			
			for (String pair: query.split("&")) {
				if (pair.isEmpty()) continue;
				
				String[] keyValue = pair.split("=", 2);
				String key = decode(keyValue[0]);
				String value = keyValue.length > 1 ? decode(keyValue[1]) : "";
				request.getQueryParams().put(key, value);
			}
		} else {
			request.setPath(raw);
		}
	}
	
	private static String decode(String s) {
		return s.replace("+", " ");
	}
}