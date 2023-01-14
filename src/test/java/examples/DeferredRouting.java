package examples;

import io.vacco.murmux.MxLayerRouter;
import io.vacco.murmux.Murmux;

public class DeferredRouting {
  public static void main(String[] args) {
    LoggerInit.apply();
    new Murmux() {
      {
        // Define root greeting
        get("/", (req, res) -> res.send("Hello World!"));
        // Define home routes
        use(
          "/home",
          new MxLayerRouter() {
            {
              get("/about", (req, res) -> res.send("About page"));
              get("/impressum", (req, res) -> res.send("Impressum page"));
              get("/sponsors", (req, res) -> res.send("Sponsors page"));
            }
          });
        // Define root routes
        use(
          "/",
          new MxLayerRouter() {
            {
              get("/login", (req, res) -> res.send("Login page"));
              get("/register", (req, res) -> res.send("Register page"));
              get("/contact", (req, res) -> res.send("Contact page"));
            }
          });
        // Start server
        listen(8080);
      }
    };
  }
}
