package com.httpServer.middleware;

import com.httpServer.http.HttpRequest;

import java.time.LocalDateTime;

public class LoggingMiddleware implements Middleware {
	
	@Override
	public void before(HttpRequest request) {
		System.out.println("[" + LocalDateTime.now() + "] " + request.getMethod() + " " + request.getRawPath());
	}

	@Override
	public void after(HttpRequest request) {}
}