package examples;

import io.vacco.murmux.Murmux;

public class DirectRouting {
  public static void main(String[] args) {
    LoggerInit.apply();
    var app = new Murmux();

    // Sample for home routes
    app.get("/", (req, res) -> res.send("Hello index!"));
    app.get("/home", (req, res) -> res.send("Homepage"));
    app.get("/about", (req, res) -> res.send("About"));

    // Sample for user
    app.get("/user/login", (req, res) -> res.send("Please login!"));
    app.get("/user/register", (req, res) -> res.send("Join now!"));

    // Basic methods
    app.get("/user", (req, res) -> res.send("Get an user!"));
    app.patch("/user", (req, res) -> res.send("Modify an user!"));
    app.delete("/user", (req, res) -> res.send("Delete an user!"));
    app.put("/user", (req, res) -> res.send("Add an user!"));

    app.listen(8080);
  }
}
