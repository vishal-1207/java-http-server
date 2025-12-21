package com.httpServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.httpServer.http.*;
import com.httpServer.route.*;

public class HttpServer {
	private final int port;
	private final Router router = new Router();
	private final ExecutorService threadPool = Executors.newFixedThreadPool(10);
	private final String staticDir = "public";
	
	public HttpServer(int port) {
		this.port = port;
		registerRoutes();	
	}
	
	private void registerRoutes() {
		router.get("/", (req) -> HttpResponse.ok("Welcome to Java HTTP Server.\r\n"));
		router.get("/hello", (req) -> {
			String name = req.getQueryParams().getOrDefault("name", "World");
			return HttpResponse.ok("Hello " + name + "!");
		});
		router.post("/echo", (req) -> HttpResponse.ok("You sent: " + req.getBody() + "\r\n"));
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
		String threadName = Thread.currentThread().getName();
		try (
			InputStream in = client.getInputStream();
			OutputStream out = client.getOutputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
		) {
			HttpRequest request = HttpParser.parse(reader);
			System.out.println("[" + threadName + "] " + request.getMethod() + " " + request.getPath());

			HttpResponse response;
			if ("GET".equals(request.getMethod()) && request.getPath().startsWith("/static/")) {
				response = serveStatic(request.getPath().substring("/static".length()));
			} else {
				response = router.route(request);
			}


			out.write(response.toBytes());	
			out.flush();


		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try { client.close(); } catch (IOException ignored) {}
		}
	}
	
	private HttpResponse serveStatic(String relativePath) {
		try {
			if (relativePath.isEmpty() || relativePath.equals("/")) {
				relativePath = "/index.html";
			}
			
			Path filePath = Path.of(staticDir + relativePath).normalize();
			if (!filePath.startsWith(staticDir) || !Files.exists(filePath) || Files.isDirectory(filePath)) {
				return HttpResponse.notFound();
			}
			
			byte[] data = Files.readAllBytes(filePath);
			String contentType = guessContentType(filePath.toString());
			return HttpResponse.okBytes(data, contentType);
		} catch (IOException e) {
			return HttpResponse.serverError("Failed to read file.");
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

