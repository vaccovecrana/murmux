package examples;

import io.vacco.murmux.Murmux;
import io.vacco.murmux.http.MxCookie;
import io.vacco.murmux.middleware.MxRouter;

public class Cookies {
  public static void main(String[] args) {
    LoggerInit.apply();
    new Murmux().rootHandler(
      new MxRouter()
        .get("/setcookie", xc -> {
          var cookie = new MxCookie("username", "john");
          xc.withCookie(cookie)
            .commitText("Cookie has been set!");
        })
        .get("/showcookie", xc -> {
          var cookie = xc.cookies.get("username");
          var username = cookie.value;
          xc.commitText("The username is: " + username); // Prints "The username is: john"
        })
    ).listen(8080);
  }
}
