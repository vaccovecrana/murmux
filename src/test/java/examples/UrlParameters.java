package examples;

import io.vacco.murmux.Murmux;
import io.vacco.murmux.middleware.MxRouter;

public class UrlParameters {
  public static void main(String[] args) {
    LoggerInit.apply();
    new Murmux().rootHandler(
      new MxRouter()
        .get("/posts/:user/:description", xc -> {
          var user = xc.getPathParam("user"); // Contains 'john'
          var description = xc.getPathParam("description"); // Contains 'all'
          // Send: "User: john, description: all"
          xc.commitText(String.format("User: %s, description: %s", user, description));
        })
    ).listen(8080);
  }
}
