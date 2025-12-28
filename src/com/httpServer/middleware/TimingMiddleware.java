package com.httpServer.middleware;

import com.httpServer.http.HttpRequest;

public class TimingMiddleware implements Middleware {
	private final String START = "_startTime";

	@Override
	public void before(HttpRequest request) {
		request.getAttributes().put(START, System.nanoTime());
	}

	@Override
	public void after(HttpRequest request) {
		Object v = request.getAttributes().get(START);
		
		if (v instanceof Long) {
			long tookNs = System.nanoTime() - (Long) v;
			double ms = tookNs / 1_000_000.0;
			System.out.printf("Request %s %s took %.3f ms%n", request.getMethod(), request.getRawPath(), ms);
		}
	}
	
}