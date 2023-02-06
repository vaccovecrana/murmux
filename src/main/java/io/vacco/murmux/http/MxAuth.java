package io.vacco.murmux.http;

import java.util.stream.Stream;

public class MxAuth {

  public String type;
  public String data;

  public MxAuth(String authHeader) {
    String[] parts =
      Stream.of(authHeader.split(" "))
        .filter(s -> !s.isEmpty())
        .toArray(String[]::new);
    this.type = parts[0];
    this.data = parts[1];
  }

}
