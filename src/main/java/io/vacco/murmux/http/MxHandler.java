package io.vacco.murmux.http;

/**
 * Interface to handle HTTP requests.
 */
@FunctionalInterface
public interface MxHandler {

  /**
   * Handle an http-request
   *
   * @param req - The request object
   * @param res - The response object
   */
  void handle(MxRequest req, MxResponse res);
}
