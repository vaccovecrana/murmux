package io.vacco.murmux.middleware;

import io.vacco.murmux.http.MxHandler;
import io.vacco.murmux.http.MxMethod;
import io.vacco.murmux.http.MxRequest;
import io.vacco.murmux.http.MxResponse;

public class MxCors implements MxHandler {

  private final MxCorsOptions options;

  public MxCors(MxCorsOptions options) {
    this.options = options;
  }

  @Override
  public void handle(MxRequest req, MxResponse res) {
    MxCorsFilter filter = this.options.getFilter();

    // Check if filter is present
    if (filter != null && !filter.shouldBypass(req)) {
      return;
    }

    // Acquire options
    boolean ac = this.options.isAllowCredentials();
    String origins = this.options.getOrigin();
    String[] headers = this.options.getHeaders();
    MxMethod[] methods = this.options.getMethods();

    // Apply headers
    res.setHeader("Access-Control-Allow-Credentials", Boolean.toString(ac));
    res.setHeader("Access-Control-Allow-Origin", origins != null ? origins : "*");
    res.setHeader("Access-Control-Allow-Methods", methods != null ? join(methods) : "*");
    res.setHeader("Access-Control-Request-Headers", headers != null ? join(headers) : "*");
  }

  private String join(Object[] objects) {
    StringBuilder sb = new StringBuilder();

    for (Object o : objects) {
      sb.append(o.toString()).append(", ");
    }

    String string = sb.toString();
    return string.substring(0, string.length() - 2);
  }
}
