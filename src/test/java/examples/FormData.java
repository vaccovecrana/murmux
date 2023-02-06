package examples;

import io.vacco.murmux.Murmux;
import io.vacco.murmux.middleware.MxRouter;

public class FormData {
  public static void main(String[] args) {
    LoggerInit.apply();
    new Murmux().rootHandler(
      new MxRouter()
        .get("/form", xc -> {
          xc.commitHtml(
            "<!DOCTYPE html>\n" +
              "<html>\n" +
              "<body>\n" +
              "\n" +
              "<h2>HTML Forms</h2>\n" +
              "\n" +
              "<form action=\"/register\" method=\"post\">\n" +
              "  <label for=\"fname\">Username:</label><br>\n" +
              "  <input type=\"text\" name=\"username\" placeholder=\"Your username\"><br>\n" +
              "  <label for=\"lname\">Email:</label><br>\n" +
              "  <input type=\"text\" name=\"email\" placeholder=\"Your E-Mail\"><br>\n" +
              "  <input type=\"submit\" value=\"Submit\">\n" +
              "</form> \n" +
              "</body>\n" +
              "</html>"
          );
        })
        .post("/register", xc -> {
          var email = xc.getFormParam("email");
          var username = xc.getFormParam("username");
          // Process data
          // Prints "E-Mail: john@gmail.com, Username: john"
          xc.commitText("E-Mail: " + email + ", Username: " + username);
        })
    ).listen(8080);
  }
}
