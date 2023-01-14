package examples;

import io.vacco.murmux.Murmux;
import io.vacco.murmux.middleware.MxDotFiles;
import io.vacco.murmux.middleware.MxFileProviderOptions;
import io.vacco.murmux.middleware.MxMiddleware;

import java.io.IOException;

public class StaticContent {
  public static void main(String[] args) throws IOException {
    LoggerInit.apply();

    var options = new MxFileProviderOptions();
    options.setExtensions("html", "css", "js", "properties"); // By default, all are allowed.

    /*
     * Activate fallback search.
     * E.g. if a request to <code>/js/code.js</code> was made but the
     * requested resource cannot be found, try searching under the <code>code</code> directory.
     */
    options.setFallBackSearching(true);
    options.setHandler(
      (req, res) -> {
      }); // Can be used to handle the request before the file will be returned.
    options.setLastModified(true); // Send the Last-Modified header, by default true.
    options.setMaxAge(10000); // Send the Cache-Control header, by default 0.
    options.setDotFiles(MxDotFiles.DENY); // Deny access to dot-files. Default is IGNORE.

    new Murmux() {
      {
        // Try GET /gradle.properties
        use(MxMiddleware.statics(".", options));
        // Start server
        listen(8080);
      }
    };
  }
}
