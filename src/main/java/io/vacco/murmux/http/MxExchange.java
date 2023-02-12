package io.vacco.murmux.http;

import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.vacco.murmux.http.MxExchanges.*;

public class MxExchange {

  public final HttpExchange io;

  public String requestContentType;
  public Long   requestContentLength;

  public Map<String, MxCookie>  cookies;
  public Map<String, String>    queries;
  public Map<String, String>    formParams;
  public List<MxAuth>           auth;
  public Map<String, String>    pathParams = new HashMap<>();
  public String                 context;
  public final MxMethod         method;

  private MxStatus    responseStatus;
  private InputStream responseBody;
  private Long        responseContentLength;
  private Boolean     responseCommitted = false;

  public Map<Class<?>, Object> attachments = new ConcurrentHashMap<>();

  public MxExchange(HttpExchange io) {
    this.io = Objects.requireNonNull(io);
    this.method = MxMethod.valueOf(io.getRequestMethod());
    this.auth = getAuthorizations(io);
    getContentLength(io).ifPresent(cl -> this.requestContentLength = cl);
    getContentType(io).ifPresent(ct -> {
      this.requestContentType = ct;
      this.formParams = getFormQueries(io.getRequestBody(), ct);
    });
    this.queries = parseFormKv(io.getRequestURI().getRawQuery());
    this.cookies = parseCookies(io);
  }

  /*
   * ===========================================================
   *                    Request methods
   * ===========================================================
   */

  public String getUserAgent() {
    return io.getRequestHeaders().get(HUserAgent).get(0);
  }

  public String getHost() {
    return io.getRequestHeaders().get(HHost).get(0);
  }

  public String getPath() {
    return io.getRequestURI().getPath();
  }

  public URI getURI() {
    return io.getRequestURI();
  }

  public String getProtocol() {
    return io.getProtocol();
  }

  public String getFormParam(String name) {
    return formParams.get(name);
  }

  public String getPathParam(String name) {
    return pathParams.get(name);
  }

  public String getQueryParam(String name) {
    return queries.get(name);
  }

  /*
   * ===========================================================
   *                    Response methods
   * ===========================================================
   */

  public MxExchange withHeader(String key, String value) {
    io.getResponseHeaders().add(key, value);
    return this;
  }

  public MxExchange withStatus(MxStatus status) {
    this.responseStatus = Objects.requireNonNull(status);
    return this;
  }

  public MxExchange withRedirect(String location) {
    return this
      .withStatus(MxStatus._302)
      .withHeader(HLocation, location);
  }

  public MxExchange withCookie(MxCookie cookie) {
    return this.withHeader(
      HSetCookie, Objects.requireNonNull(cookie).toString()
    );
  }

  public MxExchange withAttachmentFile(String fileName) {
    var dis = String.format("attachment; filename=\"%s\"", fileName);
    return this.withHeader(HContentDisposition, dis);
  }

  public MxExchange withBody(String contentType, InputStream is, long contentLength) {
    this.responseBody = Objects.requireNonNull(is);
    this.responseContentLength = contentLength;
    return this.withHeader(
      HContentType, Objects.requireNonNull(contentType)
    );
  }

  public MxExchange withBody(MxMime contentType, InputStream is, long contentLength) {
    return this.withBody(contentType.type, is, contentLength);
  }

  public MxExchange withBody(MxMime contentType, String body) {
    var bytes = body.getBytes(StandardCharsets.UTF_8);
    return this.withBody(
      contentType,
      new ByteArrayInputStream(bytes),
      bytes.length
    );
  }

  public MxExchange withBody(byte[] body) {
    return this.withBody(
      MxMime.bin,
      new ByteArrayInputStream(body),
      body.length
    );
  }

  public MxExchange withBody(URL src) {
    try {
      var conn = src.openConnection();
      this.responseBody = conn.getInputStream();
      this.responseContentLength = conn.getContentLengthLong();
      return this.withHeader(HContentType, conn.getContentType());
    } catch (Exception e) {
      throw new IllegalStateException(
        "Unable to set URL response body: " + src, e
      );
    }
  }

  public MxExchange withBody(String mimeType, Path file) {
    try {
      this.responseContentLength = Files.size(file);
      this.responseBody = Files.newInputStream(file);
      return this.withHeader(HContentType, mimeType);
    } catch (Exception e) {
      throw new IllegalStateException(
        "Unable to set file response body: " + file, e
      );
    }
  }

  public MxExchange withBody(Path file) {
    try {
      return this.withBody(Files.probeContentType(file), file);
    } catch (Exception e) {
      throw new IllegalStateException(
        "Unable to set file response body: " + file, e
      );
    }
  }

  public MxExchange withBody(MxMime mime, Path file) {
    return this.withBody(mime.type, file);
  }

  public MxExchange commit() {
    try {
      long cl = 0;
      if (responseStatus == MxStatus._204) {
        cl = -1;
      } else if (responseBody != null) {
        cl = responseContentLength;
      }
      this.io.sendResponseHeaders(responseStatus.code, cl);
      if (responseBody != null) {
        responseBody.transferTo(io.getResponseBody());
      }
      this.io.close();
      this.responseCommitted = true;
      return this;
    } catch (Exception e) {
      throw new IllegalStateException("Unable to commit response", e);
    }
  }

  public MxExchange commitText(String txt) {
    return this
      .withStatus(MxStatus._200)
      .withBody(MxMime.txt, txt)
      .commit();
  }

  public MxExchange commitJson(String json) {
    return this
      .withStatus(MxStatus._200)
      .withBody(MxMime.json, json)
      .commit();
  }

  public MxExchange commitHtml(String html) {
    return this
      .withStatus(MxStatus._200)
      .withBody(MxMime.html, html)
      .commit();
  }

  public Boolean isCommitted() {
    return responseCommitted;
  }

  /*
   * ===========================================================
   *                    Middleware methods
   * ===========================================================
   */

  public void putAttachment(Object att) {
    Objects.requireNonNull(att);
    this.attachments.put(att.getClass(), att);
  }

  @SuppressWarnings("unchecked")
  public <T> T getAttachment(Class<T> attClass) {
    return (T) attachments.get(attClass);
  }

}
