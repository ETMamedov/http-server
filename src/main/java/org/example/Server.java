package org.example;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static void serverStart(List<String> validPaths) {
        ExecutorService executorService = Executors.newFixedThreadPool(64);
        try {
            final var serverSocket = new ServerSocket(9999);
            while (true) {
                final var socket = serverSocket.accept();
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream());
                executorService.submit(() -> {
                    try {
                        processing(in, out, validPaths);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void processing(BufferedReader in, BufferedOutputStream out, List<String> validPaths) throws
            IOException {

        while (true) {
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                // just close socket
                break;
            }

            final var path = parts[1];
            if (!validPaths.contains(path)) {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                break;
            }

            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);

            // special case for classic
            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
                break;
            }

            final var length = Files.size(filePath);
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();
        }
    }
}