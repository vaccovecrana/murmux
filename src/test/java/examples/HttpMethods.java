package examples;

import io.vacco.murmux.Murmux;

public class HttpMethods {
  public static void main(String[] args) {
    LoggerInit.apply();
    new Murmux() {
      {
        get(
          "/home",
          (req, res) -> {
            // Will match every request which uses the 'GET' method and matches the '/home' path
          });
        post(
          "/login",
          (req, res) -> {
            // Will match every request which uses the 'POST' method and matches the '/login' path
          });
        // Start server
        listen(8080);
      }
    };
  }
}
