package examples;

import io.vacco.murmux.Murmux;
import io.vacco.murmux.middleware.*;
import java.nio.file.Paths;

public class StaticContent {
  public static void main(String[] args) {
    LoggerInit.apply();
    new Murmux().rootHandler(
      new MxRouter()
        // This serves content from a Filesystem location.
        // try http://localhost:8080/src/test/resources/murmux.png
        .prefix("/src", new MxStatic(MxStatic.Origin.FileSystem, Paths.get(".")))
        // This serves content from the root of the classpath (i.e. any static resources embedded inside a jar file).
        // try http://localhost:8080/glossary.json
        .prefix("/glossary.json", new MxStatic(MxStatic.Origin.Classpath, Paths.get("/")))
    ).listen(8080);
  }
}
