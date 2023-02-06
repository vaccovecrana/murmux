package examples;

import io.vacco.murmux.Murmux;
import io.vacco.murmux.middleware.MxRouter;

public class HttpMethods {
  public static void main(String[] args) {
    LoggerInit.apply();
    new Murmux().rootHandler(
      new MxRouter()
        // Will match every request which uses the 'GET' method and matches the '/home' path
        .get("/home", xc -> xc.commitText("Home route"))
        // Will match every request which uses the 'POST' method and matches the '/login' path
        .post("/login", xc -> xc.commitText("Login route"))
    ).listen(8080);
  }
}
