package com.httpServer.middleware;

import com.httpServer.http.HttpRequest;

public class TimingMiddleware implements Middleware {
	private static final String START_TIME_KEY = "_startTime";

	@Override
	public void before(HttpRequest request) {
		request.getAttributes().put(START_TIME_KEY, System.nanoTime());
	}

	@Override
	public void after(HttpRequest request) {
		Object start = request.getAttributes().get(START_TIME_KEY);
		
		if (start instanceof Long) {
			long tookNs = System.nanoTime() - (Long) start;
			double ms = tookNs / 1_000_000.0;
			System.out.printf("Request %s %s took %.3f ms%n", request.getMethod(), request.getRawPath(), ms);
		}
	}
	
}