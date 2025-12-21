package com.httpServer.route;

import com.httpServer.http.HttpRequest;
import com.httpServer.http.HttpResponse;

@FunctionalInterface
public interface RouteHandler {
	HttpResponse handle(HttpRequest request);
}
