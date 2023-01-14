package io.vacco.murmux.http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import io.vacco.murmux.util.MxMediaType;
import io.vacco.murmux.util.MxStatus;
import io.vacco.murmux.util.MxIo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class MxResponse {

  private final HttpExchange httpExchange;
  private final OutputStream body;
  private final Headers headers;

  private String contentType = MxMediaType._txt.getMIME();
  private boolean isClose = false;
  private long contentLength = 0;
  private int status = 200;

  public MxResponse(HttpExchange exchange) {
    this.httpExchange = exchange;
    this.headers = exchange.getResponseHeaders();
    this.body = exchange.getResponseBody();
  }

  /**
   * Add a specific value to the reponse header.
   *
   * @param key   The header name.
   * @param value The header value.
   * @return This Response instance.
   */
  public MxResponse setHeader(String key, String value) {
    headers.add(key, value);
    return this;
  }

  /**
   * @param key The header key.
   * @return The values which are associated with this key.
   */
  public List<String> getHeader(String key) {
    return headers.get(key);
  }

  /**
   * Sets the response Location HTTP header to the specified path parameter.
   *
   * @param location The location.
   */
  public void redirect(String location) {
    headers.add("Location", location);
    setStatus(MxStatus._302);
    send();
  }

  /**
   * Set a cookie.
   *
   * @param cookie The cookie.
   * @return This Response instance.
   */
  public MxResponse setCookie(MxCookie cookie) {
    if (isClosed()) return this;
    this.headers.add("Set-Cookie", cookie.toString());
    return this;
  }

  /**
   * @return Current response status.
   */
  public int getStatus() {
    return this.status;
  }

  /**
   * Set the response-status. Default is 200 (ok).
   *
   * @param status The response status.
   * @return This Response instance.
   */
  public MxResponse setStatus(MxStatus status) {
    if (isClosed()) return this;
    this.status = status.getCode();
    return this;
  }

  /**
   * Set the response-status and send the response.
   *
   * @param status The response status.
   */
  public void sendStatus(MxStatus status) {
    if (isClosed()) return;
    this.status = status.getCode();
    send();
  }

  /**
   * @return The current contentType
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * Set the contentType for this response.
   *
   * @param contentType - The contentType
   */
  public void setContentType(MxMediaType contentType) {
    this.contentType = contentType.getMIME();
  }

  /**
   * Set the contentType for this response.
   *
   * @param contentType - The contentType
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  /**
   * Send an empty response (Content-Length = 0)
   */
  public void send() {
    if (isClosed()) return;
    this.contentLength = 0;
    sendHeaders();
    close();
  }

  /**
   * Send a string as response.
   *
   * @param s The string.
   */
  public void send(String s) {
    if (s == null) {
      send();
      return;
    }

    if (isClosed()) return;
    byte[] data = s.getBytes();
    this.contentLength = data.length;

    sendHeaders();

    try {
      this.body.write(s.getBytes());
    } catch (IOException e) {
      throw new IllegalStateException("Failed to write char sequence to client.", e);
    } finally {
      close();
    }
  }

  /**
   * Sets the 'Content-Disposition' header to 'attachment' and his Content-Disposition "filename="
   * parameter to the file name. Normally this triggers a download event client-side.
   *
   * @param file The file to send as attachment.
   */
  public void sendAttachment(Path file) {
    if (isClosed() || !Files.isRegularFile(file)) {
      return;
    }
    String dispo = "attachment; filename=\"" + file.getFileName() + "\"";
    setHeader("Content-Disposition", dispo);
    send(file);
  }

  /**
   * Send an entire file as response The mime type will be automatically detected.
   *
   * @param file The file.
   */
  public void send(Path file) {
    if (isClosed() || !Files.isRegularFile(file)) {
      return;
    }
    try {
      this.contentLength = Files.size(file);
      MxMediaType mediaType = MxIo.getContentType(file);
      this.contentType = mediaType == null ? null : mediaType.getMIME();

      sendHeaders();

      InputStream fis = Files.newInputStream(file, StandardOpenOption.READ);
      byte[] buffer = new byte[1024];
      int n;
      while ((n = fis.read(buffer)) != -1) {
        this.body.write(buffer, 0, n);
      }

      fis.close();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to pipe file to output stream.", e);
    } finally {
      close();
    }
  }

  /**
   * Send a byte array as response. Content type will be set to application/octet-stream
   *
   * @param bytes Byte array
   */
  public void sendBytes(byte[] bytes) {
    if (isClosed() || bytes == null) {
      return;
    }
    try {
      this.contentLength = bytes.length;
      this.contentType = MxMediaType._bin.getMIME();

      sendHeaders();

      this.body.write(bytes);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to pipe file to output stream.", e);
    } finally {
      close();
    }
  }

  /**
   * Streams an input stream to the client. Requires a contentLength as well as a MediaType
   *
   * @param contentLength Total size
   * @param is            Input stream
   * @param mediaType     Stream type
   */
  public void streamFrom(long contentLength, InputStream is, MxMediaType mediaType) {
    if (isClosed() || is == null) {
      return;
    }
    try {
      this.contentLength = contentLength;
      this.contentType = mediaType.getMIME();

      sendHeaders();

      byte[] buffer = new byte[4096];
      int n;
      while ((n = is.read(buffer)) != -1) {
        this.body.write(buffer, 0, n);
      }

      is.close();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to pipe file to output stream.", e);
    } finally {
      close();
    }
  }

  /**
   * @return If the response is already closed (headers have been sent).
   */
  public boolean isClosed() {
    return this.isClose;
  }

  private void sendHeaders() {
    try {
      String contentType = getContentType() == null
        ? MxMediaType._bin.getExtension()
        : getContentType();
      this.headers.set("Content-Type", contentType);
      this.httpExchange.sendResponseHeaders(status, contentLength);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to send headers.", e);
    }
  }

  private void close() {
    try {
      this.body.close();
      this.isClose = true;
    } catch (IOException e) {
      throw new IllegalStateException("Failed to close output stream.", e);
    }
  }
}
