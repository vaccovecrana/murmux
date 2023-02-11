package io.vacco.murmux.middleware;

import io.vacco.murmux.http.*;
import org.slf4j.*;
import java.util.*;
import java.util.function.Consumer;

public class MxRouter implements MxHandler {

  private static final Logger log = LoggerFactory.getLogger(MxRouter.class);

  private final Map<String, MxRule> methodIdx = new TreeMap<>();
  private final Set<MxRule>         pathSet   = new TreeSet<>();
  private final Set<MxRule>         prefixSet = new TreeSet<>();

  public MxRouter add(MxMethod method, String context, MxHandler handler) {
    if (context == null || !context.startsWith("/")) {
      throw new IllegalArgumentException("Invalid context route: " + context);
    }
    var rule = MxRule.of(method, context, handler);
    if (rule.hasPathParameters()) {
      pathSet.add(rule);
    } else if (method == MxMethod.PREFIX) {
      prefixSet.add(rule);
    } else {
      methodIdx.put(rule.id(), rule);
    }
    return this;
  }

  public MxRouter get(String context, MxHandler handler) {
    return add(MxMethod.GET, context, handler);
  }

  public MxRouter post(String context, MxHandler handler) {
    return add(MxMethod.POST, context, handler);
  }

  public MxRouter put(String context, MxHandler handler) {
    return add(MxMethod.PUT, context, handler);
  }

  public MxRouter patch(String context, MxHandler handler) {
    return add(MxMethod.PATCH, context, handler);
  }

  public MxRouter delete(String context, MxHandler handler) {
    return add(MxMethod.DELETE, context, handler);
  }

  public MxRouter connect(String context, MxHandler handler) {
    return add(MxMethod.CONNECT, context, handler);
  }

  public MxRouter options(String context, MxHandler handler) {
    return add(MxMethod.OPTIONS, context, handler);
  }

  public MxRouter trace(String context, MxHandler handler) {
    return add(MxMethod.TRACE, context, handler);
  }

  public MxRouter head(String context, MxHandler handler) {
    return add(MxMethod.HEAD, context, handler);
  }

  public MxRouter prefix(String contextPrefix, MxHandler handler) {
    if (contextPrefix.contains(MxRule.LCurly) || contextPrefix.contains(MxRule.RCurly)) {
      throw new IllegalArgumentException(String.format(
        "Prefix route [%s] cannot contain path variable declarations.",
        contextPrefix
      ));
    }
    return add(MxMethod.PREFIX, contextPrefix, handler);
  }

  public MxRouter any(String context, MxHandler handler) {
    return add(MxMethod.ANY, context, handler);
  }

  public MxRule handlerFor(String ctx, String method,
                           Consumer<Map<String, String>> onPathParams) {
    var mxm = MxMethod.valueOf(method);
    var test = MxRule.of(mxm, MxPaths.normalizePath(ctx), null);
    // exact match
    var exact = methodIdx.get(test.id());
    if (exact != null) {
      return exact;
    }
    // any exact match
    var anyExact = methodIdx.get(test.withMethod(MxMethod.ANY).id());
    if (anyExact != null) {
      return anyExact;
    }
    // path parameter match
    test.withMethod(mxm);
    for (var r : pathSet) {
      var pp = MxPaths.matchUrl(r.context, ctx);
      if (pp != null && !pp.isEmpty()) {
        if (r.method == test.method || r.method == MxMethod.ANY) {
          onPathParams.accept(pp);
          return r;
        }
      }
    }
    // prefix match
    for (var r : prefixSet) {
      if (ctx.startsWith(r.context)) {
        return r;
      }
    }
    // no match
    return null;
  }

  @Override public void handle(MxExchange xc) {
    var context = xc.io.getRequestURI().getPath();
    var method = xc.io.getRequestMethod();
    var rule = handlerFor(
      context, method,
      pathParams -> xc.pathParams.putAll(pathParams)
    );
    if (rule != null) {
      xc.context = rule.context;
      rule.handler.handle(xc);
    } else {
      log.warn("No router rule matched [{} {}]", method, context);
    }
  }
}
