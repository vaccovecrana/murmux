package io.vacco.murmux.middleware;

import io.vacco.murmux.http.MxMethod;

public class MxCorsOptions {

  private boolean allowCredentials;
  private MxMethod[] methods;
  private String[] headers;
  private String origin;
  private MxCorsFilter filter;

  public MxCorsOptions(
    boolean allowCredentials,
    String origin,
    String[] headers,
    MxMethod[] methods,
    MxCorsFilter filter) {
    this.allowCredentials = allowCredentials;
    this.origin = origin;
    this.filter = filter;
    this.methods = methods;
    this.headers = headers;
  }

  public MxCorsOptions() {
    this(false, null, null, null, null);
  }

  public String[] getHeaders() {
    return headers;
  }

  public void setHeaders(String[] headers) {
    this.headers = headers;
  }

  public String getOrigin() {
    return origin;
  }

  public boolean isAllowCredentials() {
    return allowCredentials;
  }

  public void setAllowCredentials(boolean allowCredentials) {
    this.allowCredentials = allowCredentials;
  }

  public void setOrigin(String origin) {
    this.origin = origin;
  }

  public MxMethod[] getMethods() {
    return methods;
  }

  public void setMethods(MxMethod[] methods) {
    this.methods = methods;
  }

  public MxCorsFilter getFilter() {
    return filter;
  }

  public void setFilter(MxCorsFilter filter) {
    this.filter = filter;
  }
}
