package io.vacco.murmux.middleware;

import com.sun.net.httpserver.HttpExchange;
import io.vacco.murmux.http.*;

import static io.vacco.murmux.http.MxLog.*;

public class MxClose implements MxHandler, MxErrorHandler {

  @Override public void accept(MxExchange xc, HttpExchange io, Exception e) {
    if (io == null) {
      return;
    }
    try {
      if (xc != null && xc.headersSent()) {
        if (e != null) {
          error("Exchange already committed: [{} {}]. Closing.", io.getRequestMethod(), io.getRequestURI());
          debug("Exchange processing error", e);
        }
      } else {
        var err = e != null ? e : (xc != null ? xc.getAttachment(Exception.class) : null);
        if (err != null) {
          error("Request processing error: [{} {}]. Forcing close.", io.getRequestMethod(), io.getRequestURI());
          debug("Exchange processing error", err);
        } else {
          warn("Request did not commit: [{} {}]. Forcing close.", io.getRequestMethod(), io.getRequestURI());
        }
        var status = err != null ? MxStatus._500 : MxStatus._404;
        io.sendResponseHeaders(status.code, -1);
      }
    } catch (Exception e0) {
      error("Exchange closing error: [{} {}]", io.getRequestMethod(), io.getRequestURI(), e0);
    } finally {
      try {
        io.close();
      } catch (Exception e1) {
        error("Exchange close failed: {}", io, e1);
      }
    }
  }

  @Override public void handle(MxExchange xc) {
    try {
      accept(xc, xc.io, xc.getAttachment(Exception.class));
    } catch (Exception e) {
      accept(xc, xc.io, e);
    }
  }

}
