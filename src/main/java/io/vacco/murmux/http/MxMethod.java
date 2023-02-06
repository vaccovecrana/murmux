package io.vacco.murmux.http;

/**
 * HTTP request methods.
 */
public enum MxMethod {

  GET("GET"),
  POST("POST"),
  PUT("PUT"),
  PATCH("PATCH"),
  DELETE("DELETE"),
  CONNECT("CONNECT"),
  OPTIONS("OPTIONS"),
  TRACE("TRACE"),
  HEAD("HEAD"),

  /**
   * Wildcards for nested routing. Not standard.
   */
  ANY("ANY"), PREFIX("PREFIX");

  public final String method;

  MxMethod(String method) {
    this.method = method;
  }

}
