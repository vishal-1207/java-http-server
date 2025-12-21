package com.httpServer.http;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class HttpResponse {
	
	private int status;
	private String statusText;
	private final Map<String, String> headers = new HashMap<>();
	private byte[] body = new byte[0];
	
	public static HttpResponse ok(String body) {
		return okBytes(body.getBytes(StandardCharsets.UTF_8), "text/plain; charset=utf-8");
	}
	
	public static HttpResponse okBytes(byte[] data, String contentType) {
		HttpResponse res = new HttpResponse();
		res.status = 200;
		res.statusText = "OK";
		res.body = data;
		res.headers.put("Content-Type", contentType);
		res.headers.put("Content-Length", String.valueOf(res.body.length));
		res.headers.put("Connection", "close");
		return res;
	}
	
	public static HttpResponse notFound() {
		HttpResponse res = new HttpResponse();
		res.status = 404;
		res.statusText = "Not Found";
		String body = "404 Not Found\r\n";
		res.body = body.getBytes(StandardCharsets.UTF_8);
		res.headers.put("Content-Type", "text/plain; charset=utf-8");
		res.headers.put("Content-Length", String.valueOf(res.body.length));
		res.headers.put("Connection", "close");
		return res;
	}
	
	public static HttpResponse serverError(String msg) {
		HttpResponse res = new HttpResponse();
		res.status = 500;
		res.statusText = "Internal Server Error";
		res.body = msg.getBytes(StandardCharsets.UTF_8);
		res.headers.put("Content-Type", "text/plain; charset=utf-8");
		res.headers.put("Content-Length", String.valueOf(res.body.length));
		res.headers.put("Connection", "close");
		return res;
	}
	
	public byte[] toBytes() {
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/1.1 ").append(status).append(" ").append(statusText).append("\r\n");
		for (Map.Entry<String, String> e: headers.entrySet()) {
			sb.append(e.getKey()).append(": ").append(e.getValue()).append("\r\n");
		}
		sb.append("\r\n");
		
		byte[] headerBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
		byte[] result = new byte[headerBytes.length + body.length];
		System.arraycopy(headerBytes, 0, result, 0, headerBytes.length);
		System.arraycopy(body, 0, result, headerBytes.length, body.length);
		return result;
	}
}