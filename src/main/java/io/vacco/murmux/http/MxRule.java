package io.vacco.murmux.http;

import java.util.Objects;

public class MxRule implements Comparable<MxRule> {

  public static final String Colon = ":";

  public MxMethod method;
  public String context;
  public MxHandler handler;

  public MxRule withMethod(MxMethod m) {
    this.method = Objects.requireNonNull(m);
    return this;
  }

  public boolean hasPathParameters() {
    return context.contains(Colon);
  }

  public String id() {
    return String.format("%s|%s", method, context);
  }

  @Override public String toString() {
    return id();
  }

  @Override public boolean equals(Object obj) {
    if (obj instanceof MxRule) {
      var r0 = (MxRule) obj;
      return this.id().equals(r0.id());
    }
    return false;
  }

  @Override public int hashCode() {
    return id().hashCode();
  }

  @Override public int compareTo(MxRule o) {
    return this.id().compareTo(o.id());
  }

  public static MxRule of(MxMethod method, String context, MxHandler handler) {
    var r = new MxRule();
    r.method = Objects.requireNonNull(method);
    r.context = Objects.requireNonNull(context);
    r.handler = handler;
    return r;
  }

  public static MxRule of(String method, String context, MxHandler handler) {
    var m = MxMethod.valueOf(method);
    return of(m, context, handler);
  }

}
