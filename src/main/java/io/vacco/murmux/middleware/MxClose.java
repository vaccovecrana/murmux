package io.vacco.murmux.middleware;

import com.sun.net.httpserver.HttpExchange;
import io.vacco.murmux.http.*;

import static io.vacco.murmux.http.MxLog.*;

public class MxClose implements MxHandler, MxErrorHandler {

  @Override public void accept(MxExchange xc, HttpExchange io, Exception e) {
    try {
      if (io != null) {
        error("Exchange forcing close: [{} {}]", io.getRequestMethod(), io.getRequestURI());
        if (e != null) {
          debug("Exchange processing error", e);
        }
        if (xc != null && !xc.headersSent()) {
          io.sendResponseHeaders(MxStatus._500.code, 0);
        } else {
          io.sendResponseHeaders(MxStatus._404.code, 0);
        }
        io.close();
      }
    } catch (Exception e0) {
      error("Exchange closing error: [{} {}]", io.getRequestMethod(), io.getRequestURI(), e0);
    } finally {
      if (io != null) {
        try {
          io.close();
        } catch (Exception e1) {
          error("Exchange close failed: {}", io, e1);
        }
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
