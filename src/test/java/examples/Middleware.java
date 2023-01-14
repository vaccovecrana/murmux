package examples;

import io.vacco.murmux.Murmux;

public class Middleware {
  public static void main(String[] args) {
    LoggerInit.apply();
    new Murmux() {
      {
        use(
          "/home1",
          "POST",
          (req, res) -> {
            // Handle request by context '/home1' and method 'POST'
          });

        // Optionally, use `*` to handle every **context** or **request-method**:
        use(
          "/home2",
          "*",
          (req, res) -> {
            // Handle request which matches the context '/home2' and all methods.
          });

        // Start server
        listen(8080);
      }
    };
  }
}
