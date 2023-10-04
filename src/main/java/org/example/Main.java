package org.example;

import java.util.List;

import static org.example.Server.serverStart;

public class Main {
  public static void main(String[] args) {
    final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    serverStart(validPaths);
  }
}


