package io.vacco.murmux;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import io.vacco.murmux.filter.MxFilterImpl;
import io.vacco.murmux.filter.MxFilterLayerHandler;
import io.vacco.murmux.filter.MxFilterTask;
import io.vacco.murmux.filter.MxWorker;
import io.vacco.murmux.http.MxHandler;
import io.vacco.murmux.util.MxStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Murmux implements MxRouter {

  private static final Logger log = LoggerFactory.getLogger(Murmux.class);

  private final ArrayList<MxWorker> worker = new ArrayList<>();
  private final MxFilterLayerHandler handler = new MxFilterLayerHandler(2);

  private Executor executor = Executors.newCachedThreadPool();
  private String hostname;
  private HttpServer httpServer;
  private HttpsConfigurator httpsConfigurator;

  /**
   * Create an instance and bind the server to a hostname. Default is "Localhost"
   *
   * @param hostname The host name
   */
  public Murmux(String hostname) {
    this.hostname = hostname;
  }

  /**
   * Default, will bind the server to "localhost"
   *
   * @param httpsConfigurator The HttpsConfigurator for https
   */
  public Murmux(HttpsConfigurator httpsConfigurator) {
    this.httpsConfigurator = httpsConfigurator;
  }

  /**
   * Create an instance and bind the server to a hostname. Default is "Localhost"
   *
   * @param hostname          The host name
   * @param httpsConfigurator The HttpsConfigurator for https
   */
  public Murmux(String hostname, HttpsConfigurator httpsConfigurator) {
    this.hostname = hostname;
    this.httpsConfigurator = httpsConfigurator;
  }

  /**
   * Default, will bind the server to "localhost"
   */
  public Murmux() {}

  /**
   * @return True if the server uses https.
   */
  public boolean isSecure() {
    return httpsConfigurator != null;
  }

  /**
   * Set an executor service, default is CachedThreadPool. Can only change if the server isn't
   * already started.
   *
   * @param executor The new executor.
   * @throws IOException If the server is currently running
   */
  public void setExecutor(Executor executor) throws IOException {
    if (httpServer != null) {
      throw new IOException("Cannot set executor after the server has started");
    } else {
      this.executor = executor;
    }
  }

  /**
   * Add a routing object.
   *
   * @param router The router.
   * @return this instance
   */
  public Murmux use(MxLayerRouter router) {
    this.handler.combine(router.getHandler());
    this.worker.addAll(router.getWorker());
    return this;
  }

  /**
   * Add a routing object with a specific root.
   *
   * @param root   The root path for all request to this router.
   * @param router The router.
   * @return this instance
   */
  public Murmux use(String root, MxLayerRouter router) {
    router.getHandler().forEach(fl ->
      fl.getFilter().forEach(layer ->
        ((MxFilterImpl) layer).setRoot(root)
      )
    );
    this.handler.combine(router.getHandler());
    this.worker.addAll(router.getWorker());
    return this;
  }

  public Murmux use(MxHandler middleware) {
    addMiddleware("*", "*", middleware);
    return this;
  }

  public Murmux use(String context, MxHandler middleware) {
    addMiddleware("*", context, middleware);
    return this;
  }

  public Murmux use(String context, String requestMethod, MxHandler middleware) {
    addMiddleware(requestMethod.toUpperCase(), context, middleware);
    return this;
  }

  private void addMiddleware(String requestMethod, String context, MxHandler middleware) {
    if (middleware instanceof MxFilterTask) {
      worker.add(new MxWorker((MxFilterTask) middleware));
    }
    handler.add(0, new MxFilterImpl(requestMethod, context, middleware));
  }

  public Murmux all(MxHandler request) {
    handler.add(1, new MxFilterImpl("*", "*", request));
    return this;
  }

  public Murmux all(String context, MxHandler request) {
    handler.add(1, new MxFilterImpl("*", context, request));
    return this;
  }

  public Murmux all(String context, String requestMethod, MxHandler request) {
    handler.add(1, new MxFilterImpl(requestMethod, context, request));
    return this;
  }

  public Murmux get(String context, MxHandler request) {
    handler.add(1, new MxFilterImpl("GET", context, request));
    return this;
  }

  public Murmux post(String context, MxHandler request) {
    handler.add(1, new MxFilterImpl("POST", context, request));
    return this;
  }

  public Murmux put(String context, MxHandler request) {
    handler.add(1, new MxFilterImpl("PUT", context, request));
    return this;
  }

  public Murmux delete(String context, MxHandler request) {
    handler.add(1, new MxFilterImpl("DELETE", context, request));
    return this;
  }

  public Murmux patch(String context, MxHandler request) {
    handler.add(1, new MxFilterImpl("PATCH", context, request));
    return this;
  }

  /**
   * Start the HTTP-Server on port 80. This method is asynchronous so be sure to add a listener or
   * keep it in mind!
   */
  public void listen() {
    listen(null, 80);
  }

  /**
   * Start the HTTP-Server on a specific port This method is asynchronous so be sure to add an
   * listener or keep it in mind!
   *
   * @param port The port.
   */
  public void listen(int port) {
    listen(null, port);
  }

  /**
   * Start the HTTP-Server on port 80. This method is asynchronous so be sure to add a listener or
   * keep it in mind!
   *
   * @param onStart a listener which will be fired after the server is stardet.
   */
  public void listen(MxListener onStart) {
    listen(onStart, 80);
  }

  /**
   * Start the HTTP-Server on a specific port.
   * This method is asynchronous so be sure to
   * add a listener or keep it in mind.
   *
   * @param onStart a listener which will be fired after the server is started.
   * @param port    The port.
   */
  public void listen(MxListener onStart, int port) {
    new Thread(() -> {
      try {
        worker.forEach(MxWorker::start);

        InetSocketAddress socketAddress =
          this.hostname == null
            ? new InetSocketAddress(port)
            : new InetSocketAddress(this.hostname, port);

        if (httpsConfigurator != null) {
          httpServer = HttpsServer.create(socketAddress, 0);
          ((HttpsServer) httpServer).setHttpsConfigurator(httpsConfigurator);
        } else {
          httpServer = HttpServer.create(socketAddress, 0);
        }

        httpServer.setExecutor(executor);
        httpServer.createContext("/", ex -> {
          var res = handler.handle(ex);
          if (!res.isClosed()) {
            log.warn(
              "Request [{} {}] did not complete. Please provide a fallback handler. Forcing close.",
              ex.getRequestMethod(), ex.getRequestURI()
            );
            res.setStatus(MxStatus._404);
            res.send();
          }
        });
        httpServer.start();

        if (onStart != null) {
          onStart.action();
        }
      } catch (IOException e) {
        throw new IllegalStateException("Server initialization error", e);
      }
    }).start();
  }

  /**
   * Stop instance
   */
  public void stop() {
    if (httpServer != null) {
      httpServer.stop(0);
      worker.forEach(MxWorker::stop);
    }
  }
}
