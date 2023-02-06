package io.vacco.murmux.http;

@FunctionalInterface()
public interface MxHandler {
  void handle(MxExchange xc);
}
