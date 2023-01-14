package io.vacco.murmux;

import io.vacco.murmux.http.MxHandler;

/**
 * Router interface
 */
public interface MxRouter {

  /**
   * Add a middleware which will be called before each request-type listener will be fired.
   *
   * @param middleware A middleware which will be fired on every request-method and path.
   * @return The router itself to allow method call chaining.
   */
  MxRouter use(MxHandler middleware);

  /**
   * Add a middleware which will be called before each request-type listener will be fired.
   *
   * @param context    The context where the middleware should listen.
   * @param middleware a middleware which will be fired if the context matches the request-path.
   * @return The router itself to allow method call chaining.
   */
  MxRouter use(String context, MxHandler middleware);

  /**
   * Add a middleware which will be called before each request-type listener will be fired.
   *
   * @param context       The context where the middleware should listen for the request handler..
   * @param requestMethod And type of request-method eg. GET, POST etc.
   * @param middleware    a middleware which will be fired if the context matches the request-method-
   *                      and path.
   * @return The router itself to allow method call chaining.
   */
  MxRouter use(String context, String requestMethod, MxHandler middleware);

  /**
   * Add a listener for all request methods and contexts.
   *
   * @param request Will be fired on all requests.
   * @return The router itself to allow method call chaining.
   */
  MxRouter all(MxHandler request);

  /**
   * Adds a handler for a specific context.
   *
   * @param context The context.
   * @param request a listener which will be fired if the context matches the request-path.
   * @return The router itself to allow method call chaining.
   */
  MxRouter all(String context, MxHandler request);

  /**
   * Adds a handler for a specific context and method. You can use a star '*' to match every context
   * / request-method.
   *
   * @param context       The context.
   * @param requestMethod The request method.
   * @param request       a listener which will be fired if the context matches the request-path.
   * @return The router itself to allow method call chaining.
   */
  MxRouter all(String context, String requestMethod, MxHandler request);

  /**
   * Add a listener for GET request's.
   *
   * @param context The context.
   * @param request a listener which will be fired if the context matches the request-path.
   * @return The router itself to allow method call chaining.
   */
  MxRouter get(String context, MxHandler request);

  /**
   * Add a listener for POST request's.
   *
   * @param context The context.
   * @param request a listener which will be fired if the context matches the request-path.
   * @return The router itself to allow method call chaining.
   */
  MxRouter post(String context, MxHandler request);

  /**
   * Add a listener for PUT request's.
   *
   * @param context The context for the request handler..
   * @param request a listener which will be fired if the context matches the request-path.
   * @return The router itself to allow method call chaining.
   */
  MxRouter put(String context, MxHandler request);

  /**
   * Add a listener for DELETE request's.
   *
   * @param context The context.
   * @param request a listener which will be fired if the context matches the request-path.
   * @return The router itself to allow method call chaining.
   */
  MxRouter delete(String context, MxHandler request);

  /**
   * Add a listener for PATCH request's.
   *
   * @param context The context.
   * @param request a listener which will be fired if the context matches the request-path.
   * @return The router itself to allow method call chaining.
   */
  MxRouter patch(String context, MxHandler request);
}
