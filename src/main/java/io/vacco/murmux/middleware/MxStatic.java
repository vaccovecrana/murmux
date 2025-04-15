package io.vacco.murmux.middleware;

import io.vacco.murmux.http.*;
import java.nio.file.*;
import java.util.Objects;
import java.util.function.BiFunction;

public class MxStatic implements MxHandler {

  public enum Origin { FileSystem, Classpath }

  private final Path contentRoot;
  private final Origin origin;

  private BiFunction<Path, Origin, String> noTypeResolver;

  public MxStatic(Origin origin, Path contentRoot) {
    this.contentRoot = Objects.requireNonNull(contentRoot);
    this.origin = Objects.requireNonNull(origin);
  }

  public MxStatic withNoTypeResolver(BiFunction<Path, Origin, String> noTypeResolver) {
    this.noTypeResolver = Objects.requireNonNull(noTypeResolver);
    return this;
  }

  @Override public void handle(MxExchange xc) {
    var path = xc.getURI().getPath();
    var target = contentRoot.resolve(path.substring(1));
    try {
      switch (origin) {
        case FileSystem:
          var fct = Files.probeContentType(target);
          if (fct == null && noTypeResolver != null) {
            fct = noTypeResolver.apply(target, Origin.FileSystem);
          }
          xc.withStatus(MxStatus._200)
            .withBody(fct, target)
            .commit();
          break;
        case Classpath:
          var u = getClass().getResource(target.toString());
          var conn = Objects.requireNonNull(u).openConnection();
          var rct = conn.getContentType();
          if (rct == null && noTypeResolver != null) {
            rct = noTypeResolver.apply(target, Origin.Classpath);
          }
          xc.withStatus(MxStatus._200)
            .withBody(rct, conn.getInputStream(), conn.getContentLength())
            .commit();
      }
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
