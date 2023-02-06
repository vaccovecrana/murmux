package io.vacco.murmux.middleware;

import java.util.Objects;

public class MxSession<T> {
  public long createdUtcMs;
  public T data;

  public MxSession<T> withCreatedUtcMs(long createdUtcMs) {
    this.createdUtcMs = createdUtcMs;
    return this;
  }

  public MxSession<T> withData(T data) {
    this.data = Objects.requireNonNull(data);
    return this;
  }
}
