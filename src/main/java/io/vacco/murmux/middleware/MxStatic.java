package io.vacco.murmux.middleware;

import io.vacco.murmux.http.*;
import org.slf4j.*;
import java.nio.file.Path;
import java.util.Objects;

public class MxStatic implements MxHandler {

  private static final Logger log = LoggerFactory.getLogger(MxStatic.class);

  public enum Origin { FileSystem, Classpath }

  private final Path contentRoot;
  private final Origin origin;

  public MxStatic(Origin origin, Path contentRoot) {
    this.contentRoot = Objects.requireNonNull(contentRoot);
    this.origin = Objects.requireNonNull(origin);
  }

  @Override public void handle(MxExchange xc) {
    var path = xc.getURI().getPath();
    var target = contentRoot.resolve(path.substring(1));
    switch (origin) {
      case FileSystem:
        xc.withStatus(MxStatus._200)
          .withBody(target)
          .commit();
        break;
      case Classpath:
        try {
          var u = getClass().getResource(target.toString());
          var conn = Objects.requireNonNull(u).openConnection();
          xc.withStatus(MxStatus._200)
            .withBody(conn.getContentType(), conn.getInputStream(), conn.getContentLength())
            .commit();
        } catch (Exception e) {
          log.error("Unable to serve content: [{}]", target, e);
        }
    }
  }
}
