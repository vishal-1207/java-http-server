package com.httpServer.middleware;

import com.httpServer.http.HttpRequest;

public class ApiKeyAuthMiddleware implements Middleware {
	
	private final String validApiKey;
	
	public ApiKeyAuthMiddleware(String validApiKey) {
		this.validApiKey = validApiKey;
	}
	
	@Override
	public void before(HttpRequest request) {
		
		if (request.getPath() != null && request.getPath().startsWith("/json")) {
			String apiKey = request.getHeaders().get("X-API-KEY");
			
			if (apiKey == null || !apiKey.equals(validApiKey)) {
				throw new UnauthorizedException();
			}
		}
	}

	@Override
	public void after(HttpRequest request) {}
}