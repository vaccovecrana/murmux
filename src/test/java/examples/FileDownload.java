package examples;

import io.vacco.murmux.Murmux;

import java.nio.file.Paths;

public class FileDownload {
  public static void main(String[] args) {
    LoggerInit.apply();
    new Murmux() {
      {
        var downloadFile = Paths.get("./", "ISSUE_TEMPLATE.md");
        // Create get-route where the file can be downloaded
        get("/download-me", (req, res) -> res.sendAttachment(downloadFile));
        // Start server
        listen(8080);
      }
    };
  }
}
