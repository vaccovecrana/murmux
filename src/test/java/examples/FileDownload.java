package examples;

import io.vacco.murmux.Murmux;
import io.vacco.murmux.http.MxMime;
import io.vacco.murmux.http.MxStatus;
import io.vacco.murmux.middleware.MxRouter;

import java.nio.file.Paths;

public class FileDownload {
  public static void main(String[] args) {
    LoggerInit.apply();
    new Murmux().rootHandler(
      new MxRouter()
        // Create get-route where the file can be downloaded
        .get("/download", xc ->
          xc.withStatus(MxStatus._200)
            .withAttachmentFile("ISSUE_TEMPLATE.md")
            .withBody(Paths.get("./ISSUE_TEMPLATE.md"))
            .commit()
        )
        .get("/download-mime", xc ->
          xc.withStatus(MxStatus._200)
            .withBody(
              MxMime.of("text/markdown", ".md"),
              Paths.get("./ISSUE_TEMPLATE.md")
            ).commit()
          )
    ).listen(8080);
  }
}
