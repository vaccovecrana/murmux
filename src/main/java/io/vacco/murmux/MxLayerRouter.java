package io.vacco.murmux;

import io.vacco.murmux.filter.MxFilterImpl;
import io.vacco.murmux.filter.MxFilterLayerHandler;
import io.vacco.murmux.filter.MxFilterTask;
import io.vacco.murmux.filter.MxWorker;
import io.vacco.murmux.http.MxHandler;

import java.util.ArrayList;

/**
 * Basic implementation of a router.
 */
public class MxLayerRouter implements MxRouter {

  private final ArrayList<MxWorker> workers = new ArrayList<>();
  private final MxFilterLayerHandler handler = new MxFilterLayerHandler(2);

  public MxLayerRouter use(MxHandler middleware) {
    addMiddleware("*", "*", middleware);
    return this;
  }

  public MxLayerRouter use(String context, MxHandler middleware) {
    addMiddleware("*", context, middleware);
    return this;
  }

  public MxLayerRouter use(String context, String requestMethod, MxHandler middleware) {
    addMiddleware(requestMethod.toUpperCase(), context, middleware);
    return this;
  }

  private void addMiddleware(String requestMethod, String context, MxHandler middleware) {
    if (middleware instanceof MxFilterTask) {
      workers.add(new MxWorker((MxFilterTask) middleware));
    }
    handler.add(0, new MxFilterImpl(requestMethod, context, middleware));
  }

  public MxLayerRouter all(MxHandler request) {
    handler.add(1, new MxFilterImpl("*", "*", request));
    return this;
  }

  public MxLayerRouter all(String context, MxHandler request) {
    handler.add(1, new MxFilterImpl("*", context, request));
    return this;
  }

  public MxLayerRouter all(String context, String requestMethod, MxHandler request) {
    handler.add(1, new MxFilterImpl(requestMethod, context, request));
    return this;
  }

  public MxLayerRouter get(String context, MxHandler request) {
    handler.add(1, new MxFilterImpl("GET", context, request));
    return this;
  }

  public MxLayerRouter post(String context, MxHandler request) {
    handler.add(1, new MxFilterImpl("POST", context, request));
    return this;
  }

  public MxLayerRouter put(String context, MxHandler request) {
    handler.add(1, new MxFilterImpl("PUT", context, request));
    return this;
  }

  public MxLayerRouter delete(String context, MxHandler request) {
    handler.add(1, new MxFilterImpl("DELETE", context, request));
    return this;
  }

  public MxLayerRouter patch(String context, MxHandler request) {
    handler.add(1, new MxFilterImpl("PATCH", context, request));
    return this;
  }

  ArrayList<MxWorker> getWorker() {
    return workers;
  }

  MxFilterLayerHandler getHandler() {
    return handler;
  }
}
