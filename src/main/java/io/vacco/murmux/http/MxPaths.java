package io.vacco.murmux.http;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class MxPaths {

  /**
   * Match and assign path parameters (if any) specified in a path
   * template against a URL path.
   *
   * @param template a path parameter template. For example <code>/blog/:postId</code>
   * @param url a URL path instance. For example <code>/blog/128</code>
   * @return key/value assignments of matched parameters. For example <code>postId - 128</code>
   */
  public static HashMap<String, String> matchUrl(String template, String url) {
    var params = new HashMap<String, String>();
    var key = new StringBuilder();
    var val = new StringBuilder();
    var uc = url.toCharArray();
    var fc = template.toCharArray();
    int ui = 0, fi = 0;

    for (; fi < fc.length && ui < uc.length; fi++, ui++) {
      if (fc[fi] == ':') {
        key.setLength(0);
        val.setLength(0);
        fi++;
        while (fi < fc.length && fc[fi] != '/') {
          key.append(fc[fi++]);
        }
        while (ui < uc.length && uc[ui] != '/') {
          val.append(uc[ui++]);
        }
        var decVal = URLDecoder.decode(val.toString(), StandardCharsets.UTF_8);
        params.put(key.toString(), decVal);
      } else if (fc[fi] != uc[ui]) {
        return null; // no match
      }
    }
    if (ui < url.length() || fi < template.length()) {
      return null;
    }
    return params;
  }

  /**
   * @param context a context path
   * @return all double slashes from a string replaced with a single slash
   */
  public static String normalizePath(String context) {
    if (context == null || context.length() == 1) return context;
    var sb = new StringBuilder();
    var chars = context.toCharArray();
    sb.append(chars[0]);
    for (int i = 1; i < chars.length; i++) {
      if (chars[i] != '/' || chars[i - 1] != '/') {
        sb.append(chars[i]);
      }
    }
    return sb.toString();
  }

}
