package io.vacco.murmux;

import com.sun.net.httpserver.HttpServer;
import io.vacco.murmux.http.*;
import org.slf4j.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.*;

public class Murmux {

  private static final Logger log = LoggerFactory.getLogger(Murmux.class);

  private String host;
  private HttpServer httpServer;
  private Executor executor = Executors.newCachedThreadPool();
  private MxHandler root;

  /**
   * Bind to a host name with a custom executor.
   * @param host a host name
   * @param executor a custom executor.
   */
  public Murmux(String host, Executor executor) {
    this(host);
    this.executor = executor;
  }

  /**
   * Bind to a host name, and default executor.
   * @param host a host name
   */
  public Murmux(String host) {
    this.host = host;
  }

  /**
   * Bind to "localhost", and default executor.
   */
  public Murmux() {}

  public Murmux rootHandler(MxHandler handler) {
    this.root = Objects.requireNonNull(handler);
    return this;
  }

  /**
   * Start the HTTP-Server on a specific port.
   * @param port http port
   * @return this instance
   */
  public Murmux listen(int port) {
    if (this.root == null) {
      throw new IllegalStateException("Missing root HTTP handler.");
    }
    new Thread(() -> {
      try {
        var socketAddress = this.host == null
          ? new InetSocketAddress(port)
          : new InetSocketAddress(this.host, port);
        httpServer = HttpServer.create(socketAddress, 0);
        httpServer.setExecutor(executor);
        httpServer.createContext("/", io -> {
          try {
            var xc = new MxExchange(io);
            root.handle(xc);
            if (!xc.isCommitted()) {
              log.warn(
                "Request did not commit: [{} {}]. Forcing close.",
                io.getRequestMethod(), io.getRequestURI()
              );
              xc.withStatus(MxStatus._404).commit();
            }
          } catch (Exception e) {
            log.error(
              "Request processing error: [{} {}]. Forcing close.",
              io.getRequestMethod(), io.getRequestURI(), e
            );
            io.sendResponseHeaders(MxStatus._500.code, 0);
            io.close();
          }
        });
        httpServer.start();
      } catch (IOException e) {
        throw new IllegalStateException("Server initialization error", e);
      }
    }, String.format("%s-IO", Murmux.class.getName())).start();
    return this;
  }

  /**
   * Start the server on port 80.
   */
  public void listen() {
    listen(80);
  }

  /**
   * Stop the server.
   */
  public void stop() {
    if (httpServer != null) {
      log.info("Stopping");
      httpServer.stop(0);
    }
  }

}
