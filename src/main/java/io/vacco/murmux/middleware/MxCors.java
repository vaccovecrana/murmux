package io.vacco.murmux.middleware;

import io.vacco.murmux.http.*;
import java.util.Objects;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

/**
 * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS#functional_overview">
 *   CORS
 * </a>
 */
public class MxCors implements MxHandler {

  public static class Options {

    public Boolean    allowCredentials;
    public MxMethod[] methods;
    public String[]   headers;
    public String     origin;
    public Integer    maxAgeSec;

    public Options withHeaders(String ... headers) {
      this.headers = Objects.requireNonNull(headers);
      return this;
    }

    public Options withOrigin(String origin) {
      this.origin = Objects.requireNonNull(origin);
      return this;
    }

    public Options withAllowCredentials(boolean allowCredentials) {
      this.allowCredentials = allowCredentials;
      return this;
    }

    public Options withMethods(MxMethod ... methods) {
      this.methods = Objects.requireNonNull(methods);
      return this;
    }

    public Options withMaxAge(Integer maxAgeSec) {
      this.maxAgeSec = Objects.requireNonNull(maxAgeSec);
      return this;
    }
  }

  private final Options options;
  private final MxHandler next;

  public MxCors(Options options, MxHandler next) {
    this.options = Objects.requireNonNull(options);
    this.next = next;
  }

  @Override public void handle(MxExchange xc) {
    var opt = this.options;
    xc.withHeader("Access-Control-Allow-Origin", opt.origin != null ? opt.origin : "*");
    xc.withHeader("Access-Control-Request-Headers",
      opt.headers != null
        ? String.join(", ", opt.headers)
        : "*"
    );
    xc.withHeader("Access-Control-Allow-Methods",
      opt.methods != null
        ? stream(opt.methods).map(m -> m.method).collect(joining(", "))
        : "*"
    );
    if (opt.allowCredentials != null) {
      xc.withHeader("Access-Control-Allow-Credentials", Boolean.toString(opt.allowCredentials));
    }
    if (opt.maxAgeSec != null) {
      xc.withHeader("Access-Control-Max-Age", Integer.toString(opt.maxAgeSec));
    }
    // TODO what about Access-Control-Expose-Headers ??
    if (xc.method == MxMethod.OPTIONS) {
      xc.withStatus(MxStatus._204).commit();
    } else if (next != null) {
      next.handle(xc);
    }
  }
}
