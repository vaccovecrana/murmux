package io.vacco.murmux.filter;

import io.vacco.murmux.http.MxHandler;
import io.vacco.murmux.http.MxRequest;
import io.vacco.murmux.http.MxResponse;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * http-filter to extract data and check if a context matches the request.
 */
public class MxFilterImpl implements MxHandler {

  private final MxHandler handler;
  private final String req;
  private final String context;
  private final boolean reqAll;
  private final boolean contextAll;

  private String root;
  private String fullContext;

  public MxFilterImpl(String requestMethod, String context, MxHandler handler) {
    this.req = requestMethod;
    this.handler = handler;
    this.context = normalizePath(context);
    this.reqAll = requestMethod.equals("*");
    this.contextAll = context.equals("*");
    this.root = "/";
    this.fullContext = this.context;
  }

  public void setRoot(String root) {
    if (root == null || root.isEmpty()) {
      return;
    }
    if (root.charAt(0) != '/') {
      root = '/' + root;
    }
    if (root.charAt(root.length() - 1) != '/') {
      root += '/';
    }
    this.root = normalizePath(root);
    this.fullContext = normalizePath(this.root + context);
  }

  @Override
  public void handle(MxRequest req, MxResponse res) {
    String requestMethod = req.getMethod();
    String requestPath = req.getURI().getRawPath();

    if (!(reqAll || this.req.equals(requestMethod))) {
      return;
    } else if (contextAll) {
      req.setContext(context);
      handler.handle(req, res);
      return;
    }

    HashMap<String, String> params = matchURL(fullContext, requestPath);
    if (params == null) {
      return;
    }

    req.setParams(params);
    if (res.isClosed()) {
      return;
    }

    req.setContext(context);
    handler.handle(req, res);
  }

  /**
   * Extract and match the parameter from the url with a filter.
   */
  private HashMap<String, String> matchURL(String filter, String url) {
    HashMap<String, String> params = new HashMap<>();
    StringBuilder key = new StringBuilder();
    StringBuilder val = new StringBuilder();
    char[] uc = url.toCharArray();
    char[] fc = filter.toCharArray();
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

        String decVal = URLDecoder.decode(val.toString(), StandardCharsets.UTF_8);
        params.put(key.toString(), decVal);

      } else if (fc[fi] != uc[ui]) {
        // Failed
        return null;
      }
    }

    if (ui < url.length() || fi < filter.length()) {
      return null;
    }

    return params;
  }

  /**
   * Replace all double slashes from a string with a single slash
   */
  private String normalizePath(String context) {
    if (context == null || context.length() == 1) return context;

    StringBuilder sb = new StringBuilder();
    char[] chars = context.toCharArray();

    sb.append(chars[0]);
    for (int i = 1; i < chars.length; i++) {
      if (chars[i] != '/' || chars[i - 1] != '/') {
        sb.append(chars[i]);
      }
    }

    return sb.toString();
  }
}
