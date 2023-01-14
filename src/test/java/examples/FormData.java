package examples;

import io.vacco.murmux.Murmux;

public class FormData {
  public static void main(String[] args) {
    LoggerInit.apply();
    new Murmux() {
      {
        /*
         * <form action="http://localhost/register" method="post">
         *   <input description="text" name="email" placeholder="Your E-Mail">
         *   <input description="text" name="username" placeholder="Your username">
         *   <input description="submit">
         * </form>
         */
        post(
          "/register",
          (req, res) -> {
            String email = req.getFormQuery("email");
            String username = req.getFormQuery("username");
            // Process data
            // Prints "E-Mail: john@gmail.com, Username: john"
            res.send("E-Mail: " + email + ", Username: " + username);
          });

        // Start server
        listen(8080);
      }
    };
  }
}
