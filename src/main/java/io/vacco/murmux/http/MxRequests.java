package io.vacco.murmux.http;

import com.sun.net.httpserver.Headers;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

final class MxRequests {

  private MxRequests() {}

  /**
   * Extract the cookies from the 'Cookie' header.
   *
   * @param headers The Headers
   * @return A HashMap with the cookie name as key and the complete cookie as value.
   */
  static HashMap<String, MxCookie> parseCookies(Headers headers) {
    HashMap<String, MxCookie> cookieList = new HashMap<>();
    List<String> headerCookies = headers.get("Cookie");

    if (headerCookies == null || headerCookies.isEmpty()) {
      return cookieList;
    }

    char[] chars = headerCookies.get(0).toCharArray();
    StringBuilder key = new StringBuilder();
    StringBuilder val = new StringBuilder();
    boolean swap = false;

    for (char c : chars) {
      if (c == '=') {
        swap = true;
      } else if (c == ';') {
        String rkey = key.toString().trim();
        cookieList.put(rkey, new MxCookie(rkey, val.toString()));

        key.setLength(0);
        val.setLength(0);
        swap = false;
      } else if (swap) {
        val.append(c);
      } else {
        key.append(c);
      }
    }

    if (key.length() > 0 && val.length() > 0) {
      String rkey = key.toString().trim();
      cookieList.put(rkey, new MxCookie(rkey, val.toString()));
    }

    return cookieList;
  }

  /**
   * Method to extract the query's from an url.
   *
   * @param rawQuery The raw query
   * @return A list with key-values which are encoded in UTF8.
   */
  static HashMap<String, String> parseRawQuery(String rawQuery) {
    HashMap<String, String> querys = new HashMap<>();

    // Return empty map on null
    if (rawQuery == null) {
      return querys;
    }

    StringBuilder key = new StringBuilder();
    StringBuilder val = new StringBuilder();
    char[] chars = rawQuery.toCharArray();
    boolean keyac = false;
    char c = '=';

    for (char cc : chars) {
      c = cc;

      if (c == '=') {
        keyac = true;
      } else if (c == '&') {
        querys.put(
          URLDecoder.decode(key.toString(), StandardCharsets.UTF_8),
          URLDecoder.decode(val.toString(), StandardCharsets.UTF_8)
        );
        key.setLength(0);
        val.setLength(0);
        keyac = false;
      } else if (keyac) {
        val.append(c);
      } else {
        key.append(c);
      }
    }

    if (c != '=' && c != '&') {
      querys.put(key.toString(), val.toString());
    }

    return querys;
  }
}
