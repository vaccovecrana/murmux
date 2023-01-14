package io.vacco.murmux.http;

/**
 * Enum with basic request methods.
 */
public enum MxMethod {

  // Real request methods
  GET("GET"),
  POST("POST"),
  PUT("PUT"),
  PATCH("PATCH"),
  DELETE("DELETE"),
  CONNECT("CONNECT"),
  OPTIONS("OPTIONS"),
  TRACE("TRACE"),
  HEAD("HEAD"),

  // Catch every method
  ALL("*");

  private final String method;

  MxMethod(String method) {
    this.method = method;
  }

  public String getMethod() {
    return method;
  }
}
