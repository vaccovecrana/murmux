package examples;

import io.vacco.murmux.http.MxHandler;
import io.vacco.murmux.http.MxRequest;
import io.vacco.murmux.http.MxResponse;

public class PortMiddleware implements MxHandler {
  /**
   * From interface HttpRequest, to handle the request.
   */
  @Override
  public void handle(MxRequest req, MxResponse res) {
    // Get the port
    int port = req.getURI().getPort();
    // Add the port to the request middleware map
    req.addMiddlewareContent(this, port);
  }

}
