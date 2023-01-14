package examples;

import io.vacco.murmux.Murmux;
import io.vacco.murmux.http.MxCookie;

public class Cookies {
  public static void main(String[] args) {
    LoggerInit.apply();
    new Murmux() {
      {
        get(
          "/setcookie",
          (req, res) -> {
            var cookie = new MxCookie("username", "john");
            res.setCookie(cookie);
            res.send("Cookie has been set!");
          });

        get(
          "/showcookie",
          (req, res) -> {
            var cookie = req.getCookie("username");
            var username = cookie.getValue();
            res.send("The username is: " + username); // Prints "The username is: john"
          });

        // Start server
        listen(8080);
      }
    };
  }
}
