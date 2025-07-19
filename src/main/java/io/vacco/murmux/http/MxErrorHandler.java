package io.vacco.murmux.http;

import com.sun.net.httpserver.HttpExchange;

public interface MxErrorHandler extends MxHandler {

  void accept(MxExchange xc, HttpExchange io, Exception e);

}
