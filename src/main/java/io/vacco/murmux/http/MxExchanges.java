package io.vacco.murmux.http;

import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

import static java.util.stream.Collectors.toMap;

public class MxExchanges {

  public static final String HAuthorization       = "Authorization";
  public static final String HCacheControl        = "cache-control";
  public static final String HContentDisposition  = "Content-Disposition";
  public static final String HContentLength       = "Content-Length";
  public static final String HContentType         = "Content-Type";
  public static final String HCookie              = "Cookie";
  public static final String HHost                = "Host";
  public static final String HIfModifiedSince     = "if-modified-since";
  public static final String HIfNoneMatch         = "if-none-match";
  public static final String HLastModified        = "last-modified";
  public static final String HLocation            = "Location";
  public static final String HModified            = "modified";
  public static final String HSetCookie           = "Set-Cookie";
  public static final String HUserAgent           = "User-agent";
  public static final String HXRequestedWith      = "X-Requested-With";

  public static boolean headerMatches(HttpExchange io, String header, String value) {
    var headers = io.getRequestHeaders();
    return headers.containsKey(header)
      && headers.get(header).get(0) != null
      && headers.get(header).get(0).equals(value);
  }

  /**
   * Indicates if the request was made by a client library such as jQuery.
   *
   * @param ex the exchange
   * @return True if the 'X-Requested-With' header field is 'XMLHttpRequest'.
   */
  public static boolean isXhr(MxExchange ex) {
    return headerMatches(ex.io, HXRequestedWith, "XMLHttpRequest");
  }

  /**
   * Checks if the connection is 'fresh'. It is true if the cache-control request header doesn't have
   * a no-cache directive, the if-modified-since request header is specified and last-modified
   * request header is equal to or earlier than the modified response header or if-none-match
   * request header is *.
   *
   * @param ex the exchange
   * @return true if the connection is fresh, false otherwise.
   */
  public static boolean isFresh(MxExchange ex) {
    var io = ex.io;
    var headers = io.getRequestHeaders();
    if (headerMatches(io, HCacheControl, "no-cache")) {
      return true;
    }
    if (headerMatches(io, HIfNoneMatch, "*")) {
      return true;
    }
    if (headers.containsKey(HIfModifiedSince)
      && headers.containsKey(HLastModified)
      && headers.containsKey(HModified)) {
      var lmlist = headers.get(HLastModified);
      var mlist = headers.get(HModified);

      if (lmlist.isEmpty() || mlist.isEmpty()) {
        return false;
      }

      var lm = lmlist.get(0);
      var m = mlist.get(0);
      if (lm != null && m != null) {
        try {
          var lmi = Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(lm));
          var mi = Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(m));
          if (lmi.isBefore(mi) || lmi.equals(mi)) {
            return true;
          }
        } catch (Exception e) {
          return false;
        }
      }
    }
    return false;
  }

  /**
   * Indicates whether the request is "stale" and is the opposite of req.fresh
   *
   * @param ex the exchange
   * @return The opposite of req.fresh;
   */
  public static boolean isStale(MxExchange ex) {
    return !isFresh(ex);
  }

  public static String streamToString(InputStream is) {
    try {
      var os = new ByteArrayOutputStream();
      is.transferTo(os);
      return os.toString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static void copyRequestBodyTo(MxExchange ex, OutputStream os) {
    try {
      ex.io.getRequestBody().transferTo(os);
      os.close();
    } catch (Exception e) {
      throw new IllegalStateException(
        "Unable to copy request body content.", e
      );
    }
  }

  public static void copyRequestBodyTo(MxExchange ex, Path f) {
    try {
      if (Files.exists(f)) {
        return;
      }
      Files.createFile(f);
      copyRequestBodyTo(ex, Files.newOutputStream(f));
    } catch (Exception e) {
      throw new IllegalStateException(
        "Unable to copy request body to file: " + f, e
      );
    }
  }

  public static Map<String, String> parseKv(String raw, String entrySep,
                                            String valSep, boolean urlDecode) {
    if (raw == null) {
      return Collections.emptyMap();
    }
    return Arrays.stream(raw.split(entrySep))
      .map(se -> se.split(valSep))
      .collect(toMap(
        sa -> sa[0],
        sa -> sa.length == 2
          ? (urlDecode ? URLDecoder.decode(sa[1], StandardCharsets.UTF_8) : sa[1])
          : ""
      ));
  }

  public static Map<String, String> parseFormKv(String raw) {
    return parseKv(raw, "&", "=", true);
  }

  public static Map<String, MxCookie> parseCookiesTxt(String raw) {
    return parseKv(raw, "; ", "=", false)
      .entrySet().stream()
      .map(e -> new MxCookie(e.getKey().trim(), e.getValue()))
      .collect(toMap(mxc -> mxc.name, Function.identity()));
  }

  public static Map<String, MxCookie> parseCookies(HttpExchange io) {
    var hCookies = io.getRequestHeaders().get(HCookie);
    if (hCookies == null || hCookies.isEmpty()) {
      return Collections.emptyMap();
    }
    return parseCookiesTxt(hCookies.get(0));
  }

  public static Optional<Long> getContentLength(HttpExchange io) {
    var hcl = io.getRequestHeaders().get(HContentLength);
    var contentLength = hcl != null ? hcl.get(0) : null;
    return contentLength != null
      ? Optional.of(Long.parseLong(contentLength))
      : Optional.empty();
  }

  public static Optional<String> getContentType(HttpExchange io) {
    var hct = io.getRequestHeaders().get(HContentType);
    return hct == null ? Optional.empty() : Optional.of(hct.get(0));
  }

  public static List<MxAuth> getAuthorizations(HttpExchange io) {
    var hValues = io.getRequestHeaders().get(HAuthorization);
    if (hValues != null && !hValues.isEmpty()) {
      var authHeader = hValues.get(0);
      return Stream.of(authHeader.split(","))
        .map(MxAuth::new)
        .collect(Collectors.toUnmodifiableList());
    }
    return Collections.emptyList();
  }

  public static Map<String, String> getFormQueries(InputStream body, String contentType) {
    return contentType.startsWith(MxMime.xForm.type)
      ? parseFormKv(streamToString(body))
      : new HashMap<>();
  }

}
