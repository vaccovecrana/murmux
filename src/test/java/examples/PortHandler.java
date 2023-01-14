package examples;

import io.vacco.murmux.Murmux;

public class PortHandler {
  public static void main(String[] args) {
    LoggerInit.apply();
    new Murmux() {
      {
        get(
          "/port-test",
          (req, res) -> {
            // Get the content from the PortParser which we create above
            int port = req.getMiddlewareContent(PortMiddleware.class);
            // Return it to the client:
            res.send("Port: " + port);
          });
        use(new PortMiddleware());
        // Start server
        listen(8080);
      }
    };
  }
}
