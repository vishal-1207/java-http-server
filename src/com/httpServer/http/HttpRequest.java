package com.httpServer.http;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
	private String method;
	private String rawPath;
	private String path;
	private String version;
	private final Map<String, String> headers = new HashMap<>();
	private final Map<String, String> queryParams = new HashMap<>();
	String body = "";
	
	public String getMethod() {
		return method;
	}
	
	public String getRawPath() {
		return rawPath;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getVersion() {
		return version;
	}
	
	public Map<String, String> getHeaders() {
		return headers;
	}
	
	public Map<String, String> getQueryParams() {
		return queryParams;
	}
	
	public String getBody() {
		return body;
	}
	
	
	public void setMethod(String method) {
		this.method = method;
	}
	
	public void setRawPath(String rawPath) { 
		this.rawPath = rawPath; 
	}
	
	public void setPath(String path) { 
		this.path = path; 
	}
	
	public void setVersion(String version) { 
		this.version = version; 
	}
	
	public void setBody(String body) { 
		this.body = body; 
	}
}