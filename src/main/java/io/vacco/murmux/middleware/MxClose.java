package io.vacco.murmux.middleware;

import io.vacco.murmux.http.MxExchange;
import io.vacco.murmux.http.MxHandler;
import io.vacco.murmux.http.MxStatus;
import org.slf4j.Logger;

public class MxClose implements MxHandler {

  private static final Logger log = org.slf4j.LoggerFactory.getLogger(MxClose.class);

  @Override public void handle(MxExchange xc) {
    if (!xc.isCommitted()) {
      log.warn(
        "Request did not commit: [{} {}]. Forcing close.",
        xc.io.getRequestMethod(), xc.io.getRequestURI()
      );
      xc.withStatus(MxStatus._404).commit();
    }
  }

}
