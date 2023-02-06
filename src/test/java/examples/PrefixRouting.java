package examples;

import io.vacco.murmux.Murmux;
import io.vacco.murmux.middleware.MxRouter;

public class PrefixRouting {
  public static void main(String[] args) {
    LoggerInit.apply();
    new Murmux().rootHandler(
      new MxRouter()
        // Define root greeting
        .get("/", xc -> xc.commitText("Hello World!"))
        // Define home routes
        .prefix("/home",
          new MxRouter()
            .get("/home/about", xc -> xc.commitText("About page"))
            .get("/home/impressum", xc -> xc.commitText("Impressum page"))
            .get("/home/sponsors", xc -> xc.commitText("Sponsors page"))
        )
        .prefix("/user",
          new MxRouter()
            .get("/user/login", xc -> xc.commitText("Login page"))
            .get("/user/register", xc -> xc.commitText("Register page"))
            .get("/user/contact", xc -> xc.commitText("Contact page"))
        )
    ).listen(8080);
  }
}
