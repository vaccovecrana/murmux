package examples;

import io.vacco.murmux.Murmux;
import io.vacco.murmux.middleware.MxRouter;

public class DirectRouting {
  public static void main(String[] args) {
    LoggerInit.apply();
    new Murmux().rootHandler(
      new MxRouter()
        // Sample for home routes
        .get("/", xc -> xc.commitText("Hello index"))
        .get("/home", xc -> xc.commitText("Homepage"))
        .get("/about", xc -> xc.commitText("About"))
        // Sample for user
        .get("/user/login", xc -> xc.commitText("Please login"))
        .get("/user/register", xc -> xc.commitText("Join now"))
        // Basic methods
        .get("/user", xc -> xc.commitText("Get user"))
        .patch("/user", xc -> xc.commitText("Modify user"))
        .delete("/user", xc -> xc.commitText("Delete user"))
        .put("/user", xc -> xc.commitText("Add user"))
    ).listen(8080);
  }
}
