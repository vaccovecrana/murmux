package io.vacco.murmux;

import com.sun.net.httpserver.*;
import io.vacco.murmux.http.*;
import io.vacco.murmux.middleware.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.*;

import static io.vacco.murmux.http.MxLog.info;

public class Murmux {

  private String          host;
  private HttpServer      httpServer;
  private Executor        executor = Executors.newCachedThreadPool();
  private MxHandler       root;
  private MxErrorHandler  errorHdl = new MxClose();

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
   * Bind to a host name, and use default executor.
   * @param host a host name
   */
  public Murmux(String host) {
    this.host = host;
  }

  /** Bind to "localhost", and use default executor */
  public Murmux() {}

  public Murmux rootHandler(MxHandler handler) {
    this.root = Objects.requireNonNull(handler);
    return this;
  }

  public Murmux errorHandler(MxErrorHandler handler) {
    this.errorHdl = Objects.requireNonNull(handler);
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
              errorHdl.handle(xc);
            }
          } catch (Exception e) {
            errorHdl.accept(null, io, e);
          }
        });
        httpServer.start();
      } catch (IOException e) {
        throw new IllegalStateException("Server initialization error", e);
      }
    }, String.format("%s-IO", Murmux.class.getName())).start();
    return this;
  }

  /** Start the server on port 80. */
  public void listen() {
    listen(80);
  }

  /** Stop the server. */
  public void stop() {
    if (httpServer != null) {
      info("Stopping");
      httpServer.stop(0);
    }
  }

}
