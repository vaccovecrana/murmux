package io.vacco.murmux.middleware;

import io.vacco.murmux.http.*;
import java.io.*;
import java.net.URL;
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

  /**
   * Traverses up a file path to find a common ancestor matching the expected target path.
   * This is useful when locating resource directories in a project structure.
   *
   * For example, if you have a file path like <code>/home/user/project/src/main/resources/static/images</code>
   * and you want to find the common path <code>resources/static</code>, this method will return
   * <code>/home/user/project/src/main/resources/static</code>.
   *
   * @param inputFile The source file to start traversing from
   * @param expectedPath The target path to match against
   * @return The matched directory as a File, or <code>null</code> if no match is found
   */
  public static File resolveCommonPath(File inputFile, String expectedPath) {
    var inputPath = inputFile.toPath().toAbsolutePath().normalize();
    var expected = Paths.get(expectedPath).normalize();
    int expectedSegments = expected.getNameCount();
    while (inputPath.getNameCount() > 0 && !inputPath.endsWith(expected)) {
      if (inputPath.getNameCount() < expectedSegments) {
        return null;
      }
      inputPath = inputPath.getParent();
    }
    if (inputPath.endsWith(expected)) {
      return inputPath.toFile();
    } else {
      return null;
    }
  }

  public String contentTypeForFileAt(Path target) throws IOException {
    var fct = Files.probeContentType(target);
    if (fct == null && noTypeResolver != null) {
      fct = noTypeResolver.apply(target, Origin.FileSystem);
    }
    return fct;
  }

  public String contentTypeForResourceAt(Path target, URL u) throws IOException {
    var conn = Objects.requireNonNull(u).openConnection();
    var rct = conn.getContentType();
    if (rct == null && noTypeResolver != null) {
      rct = noTypeResolver.apply(target, Origin.Classpath);
    }
    return rct;
  }

  public void handleWithPath(MxExchange xc, String path) {
    var target = contentRoot.resolve(path.substring(1));
    try {
      switch (origin) {
        case FileSystem:
          xc.withStatus(MxStatus._200)
            .withBody(contentTypeForFileAt(target), target)
            .commit();
          break;
        case Classpath:
          var u = getClass().getResource(target.toString());
          var conn = Objects.requireNonNull(u).openConnection();
          xc.withStatus(MxStatus._200)
            .withBody(contentTypeForResourceAt(target, u), conn.getInputStream(), conn.getContentLength())
            .commit();
      }
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Override public void handle(MxExchange xc) {
    handleWithPath(xc, xc.getURI().getPath());
  }

}
