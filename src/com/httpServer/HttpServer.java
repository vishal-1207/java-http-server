package com.httpServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.List;

import com.httpServer.http.*;
import com.httpServer.route.*;
import com.httpServer.middleware.*;

public class HttpServer {
	private final int port;
	private final Router router = new Router();
	private final ExecutorService threadPool = Executors.newFixedThreadPool(10);
	private final String staticDir = "public";
	private final List<Middleware> middlewares = new ArrayList<>();
	
	public HttpServer(int port) {
		this.port = port;
		registerMiddlewares();
		registerRoutes();	
	}
	
	private void registerMiddlewares() {
		middlewares.add(new LoggingMiddleware());
		middlewares.add(new TimingMiddleware());
	}
	
	private void registerRoutes() {
		router.get("/", (req) -> HttpResponse.ok("Welcome to Java HTTP Server.\r\n"));
		router.get("/hello", (req) -> {
			String name = req.getQueryParams().getOrDefault("name", "World");
			return HttpResponse.ok("Hello " + name + "!\r\n");
		});
		router.post("/echo", (req) -> HttpResponse.ok("You sent: " + req.getBody() + "\r\n"));
		router.post("/json", (req) -> HttpResponse.json(req.getBody() + "\r\n"));
	}
	
	public void start() throws IOException {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("HTTP server started on point: " + port);
			
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println("Shutting down server...");
				threadPool.shutdown();
			}));
			
			while (true) {
				Socket client = serverSocket.accept();
				threadPool.submit(() -> handleClient(client));
			}
		}
	}
	
	private void handleClient(Socket client) {
		HttpRequest request = null;
		String threadName = Thread.currentThread().getName();
		try (
			InputStream in = client.getInputStream();
			OutputStream out = client.getOutputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
		) {
			request = HttpParser.parse(reader);
			
			for (Middleware m: middlewares) {
				m.before(request);
			}

			HttpResponse response;
			if ("GET".equals(request.getMethod()) && request.getPath().startsWith("/static/")) {
				response = serveStatic(request.getPath().substring("/static".length()), request);
			} else {
				response = router.route(request);
			}

			for (Middleware m: middlewares) {
				m.after(request);	
			}

			out.write(response.toBytes());	
			out.flush();


		} catch (IOException e) {
			try {
				if (request != null) {
					HttpResponse err = request.isJson() ? HttpResponse.jsonError(500, "Internal Server Error \r\n") : HttpResponse.serverError("Internal Server Error \r\n");
					client.getOutputStream().write(err.toBytes());
				}
			} catch (IOException ignored) {	}
		} finally {
			try { client.close(); } catch (IOException ignored) {}
		}
	}
	
	private HttpResponse serveStatic(String relativePath, HttpRequest req) {
		try {
			if (relativePath.isEmpty() || relativePath.equals("/")) {
				relativePath = "/index.html";
			}
			
			Path filePath = Path.of(staticDir + relativePath).normalize();
			if (!filePath.startsWith(staticDir) || !Files.exists(filePath) || Files.isDirectory(filePath)) {
				return req.isJson() ? HttpResponse.jsonError(404, "Not Found \r\n") : HttpResponse.notFound();
			}
			
			byte[] data = Files.readAllBytes(filePath);
			String contentType = guessContentType(filePath.toString());
			return HttpResponse.okBytes(data, contentType);
		} catch (IOException e) {
			return req.isJson() ? HttpResponse.jsonError(500, "Failed to read file  \r\n") : HttpResponse.serverError("Failed to read file  \r\n");
		}
	}
	
	private String guessContentType(String name) {
		if (name.endsWith(".html")) return "text/html; charset=utf-8";
		if (name.endsWith(".css")) return "text/css; charset=utf-8";
		if (name.endsWith(".js")) return "application/javascript; charset=utf-8";
		if (name.endsWith(".json")) return "application/json; charset=utf-8";
		if (name.endsWith(".png")) return "image/png";
		if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
		if (name.endsWith(".txt")) return "text/plain; charset=utf-8";
		return "application/octet-stream";
	}
	
	public static void main(String[] args) throws IOException {
		new HttpServer(8080).start();
	}
}

