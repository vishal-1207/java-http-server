package com.httpServer.route;

import java.util.HashMap;
import java.util.Map;

import com.httpServer.http.HttpRequest;
import com.httpServer.http.HttpResponse;
import com.httpServer.route.RouteHandler;

public class Router {
	private final Map<String, RouteHandler> routes = new HashMap<>();
	
	public void get(String path, RouteHandler handler) {
		routes.put("GET " + path, handler);
	}
	
	public void post(String path, RouteHandler handler) {
		routes.put("POST " + path, handler);
	}
	
	public HttpResponse route(HttpRequest request) {
		String key = request.getMethod() + " " + request.getPath();
		RouteHandler handler = routes.get(key);
		if (handler != null) {
			return handler.handle(request);
		}
		
		return HttpResponse.notFound();
	}
}
