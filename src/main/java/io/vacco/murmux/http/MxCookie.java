package io.vacco.murmux.http;

import java.time.Instant;

/**
 * Simple class which represents an HTTPCookie.
 */
public class MxCookie {

  public String name = "", value = "";
  public String expire = "";
  public String path = "/";
  public String domain = "";
  public String sameSite = "";
  public boolean secure = false;
  public boolean httpOnly = false;
  public long maxAge = -1;

  /**
   * Creates a new http-cookie
   *
   * @param name  Cookie name
   * @param value Cookie value
   */
  public MxCookie(String name, String value) {
    name = name.trim();
    if (name.isEmpty() || name.charAt(0) == '$') {
      throw new IllegalArgumentException("Illegal cookie name");
    }
    this.name = name;
    this.value = value;
  }

  /**
   * Set the cookie expiration (GMT Instant) Default is infinite
   *
   * @param instant expiration time
   */
  public MxCookie withExpire(Instant instant) {
    this.expire = instant.toString();
    return this;
  }

  public MxCookie withMaxAge(long maxAge) {
    this.maxAge = maxAge;
    return this;
  }

  public String serialize() {
    if (name == null || value == null) return null;
    var b = new StringBuilder().append(name).append("=").append(value);
    if (!path.isEmpty()) b.append("; Path=").append(path);
    if (!expire.isEmpty()) b.append("; Expire=").append(expire);
    if (!domain.isEmpty()) b.append("; Domain=").append(domain);
    if (!sameSite.isEmpty()) b.append("; SameSite=").append(sameSite);
    if (maxAge != -1) b.append("; Max-Age=").append(maxAge);
    if (secure) b.append("; Secure");
    if (httpOnly) b.append("; HttpOnly");
    return b.toString();
  }

  @Override public boolean equals(Object obj) {
    if (obj instanceof MxCookie) {
      var other = (MxCookie) obj;
      if (!other.value.equals(this.value)) return false;
      if (!other.name.equals(this.name)) return false;
      if (!other.domain.equals(this.domain)) return false;
      if (!other.expire.equals(this.expire)) return false;
      if (other.maxAge != this.maxAge) return false;
      if (!other.sameSite.equals(this.sameSite)) return false;
      return other.path.equals(this.path);
    }
    return false;
  }

  @Override public int hashCode() {
    return serialize().hashCode();
  }

  @Override public String toString() {
    var ser = serialize();
    return ser == null ? "??" : ser;
  }
}
