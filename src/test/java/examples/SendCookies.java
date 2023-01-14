package examples;

import io.vacco.murmux.Murmux;
import io.vacco.murmux.http.MxCookie;

public class SendCookies {
  public static void main(String[] args) {
    LoggerInit.apply();
    new Murmux() {
      {
        // Define route
        get(
          "/give-me-cookies",
          (req, res) -> {
            // Set a cookie (you can call setCookie how often you want)
            res.setCookie(new MxCookie("my-cookie", "Hello World!"));
            // Send text
            res.send("Your cookie has been set!");
          });
        // Start server
        listen(8080);
      }
    };
  }
}
