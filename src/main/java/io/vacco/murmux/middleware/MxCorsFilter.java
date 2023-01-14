package io.vacco.murmux.middleware;

import io.vacco.murmux.http.MxRequest;

public interface MxCorsFilter {
  boolean shouldBypass(MxRequest req);
}
