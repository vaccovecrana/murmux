package io.vacco.murmux.middleware;

import io.vacco.murmux.http.MxHandler;
import io.vacco.murmux.http.MxRequest;
import io.vacco.murmux.http.MxResponse;
import io.vacco.murmux.util.MxStatus;
import io.vacco.murmux.util.MxIo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * A middleware to provide access to static server-files.
 */
public final class MxFileProvider implements MxHandler {

  private MxFileProviderOptions options;
  private String root;

  MxFileProvider(String root, MxFileProviderOptions options) {
    Path rootDir = Paths.get(root);
    if (!Files.exists(rootDir) || !Files.isDirectory(rootDir)) {
      throw new IllegalArgumentException(rootDir + " does not exists or isn't a directory.");
    }
    this.root = rootDir.toAbsolutePath().toString();
    this.options = options;
  }

  @Override
  public void handle(MxRequest req, MxResponse res) {
    try {
      String path = req.getURI().getPath();
      String context = req.getContext();
      if (path.indexOf(context) == 0) {
        path = path.substring(context.length());
      }

      if (path.length() <= 1) {
        path = "index.html";
      }

      Path reqFile = Paths.get(root + File.separator + path);

      /*
       * If the file wasn't found, it will search in the target-directory for
       * the file by the raw-name without extension.
       */
      if (options.isFallBackSearching() && !Files.exists(reqFile) && !Files.isDirectory(reqFile)) {
        String name = reqFile.getFileName().toString();
        try {
          Path parent = reqFile.getParent();
          if (Files.isReadable(parent)) {
            try (var str = Files.walk(parent)) {
              Optional<Path> founded = str.filter(sub -> getBaseName(sub).equals(name)).findFirst();
              if (founded.isPresent()) {
                reqFile = founded.get();
              }
            }
          }
        } catch (IOException e) {
          throw new IllegalStateException("Cannot walk file tree.", e);
        }
      }

      if (Files.exists(reqFile) && Files.isRegularFile(reqFile)) {
        if (reqFile.getFileName().toString().charAt(0) == '.') {
          switch (options.getDotFiles()) {
            case IGNORE:
              res.setStatus(MxStatus._404);
              return;
            case DENY:
              res.setStatus(MxStatus._403);
              return;
          }
        }

        // Check if extension is present
        if (options.getExtensions() != null) {
          String reqEx = MxIo.getExtension(reqFile);
          if (reqEx == null) {
            return;
          }
          for (String ex : options.getExtensions()) {
            if (reqEx.equals(ex)) {
              finish(reqFile, req, res);
              break;
            }
          }
          res.setStatus(MxStatus._403);
        } else {
          finish(reqFile, req, res);
        }
      }

      res.setStatus(MxStatus._404);
      finish(null, req, res);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to serve file content: " + req.getPath(), e);
    }
  }

  private void finish(Path file, MxRequest req, MxResponse res) throws IOException {
    if (file != null) {
      if (options.getHandler() != null) {
        options.getHandler().handle(req, res);
      }
      if (options.isLastModified()) {
        Instant instant = Instant.ofEpochMilli(Files.getLastModifiedTime(file).toMillis());
        DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC);
        res.setHeader("Last-Modified", formatter.format(instant));
      }
      res.setHeader("Cache-Control", String.valueOf(options.getMaxAge()));
      res.send(file);
    } else {
      res.send();
    }
  }

  private String getBaseName(Path path) {
    String name = path.getFileName().toString();
    int index = name.lastIndexOf('.');
    return index == -1 ? name : name.substring(0, index);
  }
}
