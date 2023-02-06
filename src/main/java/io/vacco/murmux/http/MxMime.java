package io.vacco.murmux.http;

import java.util.List;
import java.util.Objects;

public class MxMime {

  public String[] extensions;
  public String   type;

  public static MxMime of(String type, String ... extensions) {
    var m = new MxMime();
    m.type = Objects.requireNonNull(type);
    m.extensions = Objects.requireNonNull(extensions);
    return m;
  }

  public static MxMime
    aac     = of("audio/aac", ".aac"),
    abw     = of("application/x-abiword", ".abw"),
    arc     = of("application/x-freearc", ".arc"),
    avif    = of("image/avif", ".avif"),
    avi     = of("video/x-msvideo", ".avi"),
    azw     = of("application/vnd.amazon.ebook", ".azw"),
    bin     = of("application/octet-stream", ".bin"),
    bmp     = of("image/bmp", ".bmp"),
    bz      = of("application/x-bzip", ".bz"),
    bz2     = of("application/x-bzip2", ".bz2"),
    cda     = of("application/x-cdf", ".cda"),
    csh     = of("application/x-csh", ".csh"),
    css     = of("text/css", ".css"),
    csv     = of("text/csv", ".csv"),
    doc     = of("application/msword", ".doc"),
    docx    = of("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx"),
    eot     = of("application/vnd.ms-fontobject", ".eot"),
    epub    = of("application/epub+zip", ".epub"),
    gz      = of("application/gzip", ".gz"),
    gif     = of("image/gif", ".gif"),
    html    = of("text/html", ".htm", ".html"),
    ico     = of("image/vnd.microsoft.icon", ".ico"),
    ics     = of("text/calendar", ".ics"),
    jar     = of("application/java-archive", ".jar"),
    jpeg    = of("image/jpeg", ".jpeg", ".jpg"),
    js      = of("text/javascript", ".js"),
    json    = of("application/json", ".json"),
    jsonld  = of("application/ld+json", ".jsonld"),
    midi    = of("audio/midi", ".mid", ".midi"),
    xmidi   = of("audio/x-midi", ".mid", ".midi"),
    mjs     = of("text/javascript", ".mjs"),
    mp3     = of("audio/mpeg", ".mp3"),
    mp4     = of("video/mp4", ".mp4"),
    mpeg    = of("video/mpeg", ".mpeg"),
    mpkg    = of("application/vnd.apple.installer+xml", ".mpkg"),
    odp     = of("application/vnd.oasis.opendocument.presentation", ".odp"),
    ods     = of("application/vnd.oasis.opendocument.spreadsheet", ".ods"),
    odt     = of("application/vnd.oasis.opendocument.text", ".odt"),
    oga     = of("audio/ogg", ".oga"),
    ogv     = of("video/ogg", ".ogv"),
    ogx     = of("application/ogg", ".ogx"),
    opus    = of("audio/opus", ".opus"),
    otf     = of("font/otf", ".otf"),
    png     = of("image/png", ".png"),
    pdf     = of("application/pdf", ".pdf"),
    php     = of("application/x-httpd-php", ".php"),
    ppt     = of("application/vnd.ms-powerpoint", ".ppt"),
    pptx    = of("application/vnd.openxmlformats-officedocument.presentationml.presentation", ".pptx"),
    rar     = of("application/vnd.rar", ".rar"),
    rtf     = of("application/rtf", ".rtf"),
    sh      = of("application/x-sh", ".sh"),
    svg     = of("image/svg+xml", ".svg"),
    tar     = of("application/x-tar", ".tar"),
    tiff    = of("image/tiff", ".tif, .tiff"),
    ts      = of("video/mp2t", ".ts"),
    ttf     = of("font/ttf", ".ttf"),
    txt     = of("text/plain", ".txt"),
    vsd     = of("application/vnd.visio", ".vsd"),
    wav     = of("audio/wav", ".wav"),
    weba    = of("audio/webm", ".weba"),
    webm    = of("video/webm", ".webm"),
    webp    = of("image/webp", ".webp"),
    woff    = of("font/woff", ".woff"),
    woff2   = of("font/woff2", ".woff2"),
    xhtml   = of("application/xhtml+xml", ".xhtml"),
    xls     = of("application/vnd.ms-excel", ".xls"),
    xlsx    = of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),
    xml     = of("application/xml", ".xml"),
    xul     = of("application/vnd.mozilla.xul+xml", ".xul"),
    zip     = of("application/zip", ".zip"),
    v3gp    = of("video/3gpp", ".3gp"),
    a3gp    = of("audio/3gpp", ".3gp"),
    v3g2    = of("video/3gpp2", ".3g2"),
    a3g2    = of("audio/3gpp2", ".3g2"),
    x7z     = of("application/x-7z-compressed", ".7z"),

    xForm   = of("application/x-www-form-urlencoded");

  public static List<MxMime> defaults = List.of(
    aac, bin, txt
  );

}
