package com.httpServer.middleware;

import com.httpServer.http.HttpRequest;

public interface Middleware {
	
	public void before(HttpRequest request);
	public void after(HttpRequest request);
}