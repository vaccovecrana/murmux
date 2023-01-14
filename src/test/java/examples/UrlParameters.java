package examples;

import io.vacco.murmux.Murmux;

public class UrlParameters {
  public static void main(String[] args) {
    LoggerInit.apply();
    new Murmux() {
      {
        get(
          "/posts/:user/:description",
          (req, res) -> {
            String user = req.getParam("user"); // Contains 'john'
            String description = req.getParam("description"); // Contains 'all'
            res.send(
              "User: "
                + user
                + ", description: "
                + description); // Send: "User: john, description: all"
          });
        // Start server
        listen(8080);
      }
    };
  }
}
