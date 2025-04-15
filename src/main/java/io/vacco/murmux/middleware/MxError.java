package io.vacco.murmux.middleware;

import com.sun.net.httpserver.HttpExchange;
import io.vacco.murmux.http.MxStatus;
import org.slf4j.Logger;
import java.util.function.BiConsumer;

public class MxError implements BiConsumer<HttpExchange, Exception> {

  private static final Logger log = org.slf4j.LoggerFactory.getLogger(MxError.class);

  @Override public void accept(HttpExchange io, Exception e) {
    try {
      if (io != null) {
        log.error(
          "Request processing error: [{} {}]. Forcing close.",
          io.getRequestMethod(), io.getRequestURI(), e
        );
        io.sendResponseHeaders(MxStatus._500.code, 0);
      }
    } catch (Exception e0) {
      log.error("Error closing exchange: [{} {}].", io.getRequestMethod(), io.getRequestURI(), e0);
    } finally {
      if (io != null) {
        try {
          io.close();
        } catch (Exception e1) {
          log.error("Error closing exchange: [{} {}].", io.getRequestMethod(), io.getRequestURI(), e1);
        }
      }
    }
  }

}
