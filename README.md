![Murmux](./murmux.png)

# Murmux

Zero-dependencies HTTP Framework based on ExpressJS

# Getting Started

```java
public class Example {
  public static void main(String[] args) {
    new Murmux()
      .rootHandler(xc -> xc.commitText("Hello world"))
      .listen(8080); // Will listen on port 8080
  }
}
```

## Installation

Available in [Maven Central](https://mvnrepository.com/artifact/io.vacco.murmux/murmux).

```
implementation("io.vacco.murmux:murmux:<LATEST_VERSION>")
```

- [Getting Started](#getting-started)
  - [Installation](#installation)
- [Routing](#routing)
    - [Direct](#direct)
    - [With Router](#with-router)
  - [URL Basics](#url-basics)
    - [URL Parameters](#url-parameters)
    - [URL Query](#url-query)
  - [Cookies](#cookies)
  - [Form data](#form-data)
- [Middleware](#middleware)
  - [Existing Middlewares](#existing-middlewares)
      - [CORS](#cors)
      - [Static Content](#static-content)
      - [Cookie Session](#cookie-session)
  - [Examples](#examples)
      - [File download](#file-download)
- [Development](#development)

# Routing

### Direct

You can add routes (And middlewares) directly to handle requests. Direct also supports methods like `POST` `PATCH` `DELETE` and `PUT`, others need to be created manually:

[DirectRouting](./src/test/java/examples/DirectRouting.java)

### With Router

However, it's better to split your code. You can create routes and add them at runtime:

[DeferredRouting](./src/test/java/examples/PrefixRouting.java)

## URL Basics

You can create handlers for all [request-methods](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods) and contexts. Some examples:

[HttpMethods](./src/test/java/examples/HttpMethods.java)

### URL Parameters

Sometimes you want to create dynamic URLs where some parts are not static. With the `{}` operator you can create variables in the URL which will be saved later in a `HashMap`.

Example request: `GET /posts/john/all`:

[UrlParameters](./src/test/java/examples/UrlParameters.java)

### URL Query

If you make a request which contains queries, you can access the queries over `req.getQuery(NAME)`.

Example request: `GET /posts?page=12&from=john`:

[UrlQuery](./src/test/java/examples/UrlParameters.java)

## Cookies

With `req.cookies.get(NAME)` you can get a cookie by his name, and with `res.withCookie(NAME, VALUE)` you can easily set a cookie.

Example request: `GET /setcookie` and `GET /showcookie`:

[Cookies](./src/test/java/examples/Cookies.java)

## Form data

Use `req.getFormParam(NAME)` to receive values from input elements of an HTML-Form.

[FormData](./src/test/java/examples/FormData.java)

> **Warning: Currently, File-inputs don't work, if there is a File-input the data won't get parsed!**

# Middleware

Middleware allows you to handle a request before it reaches any other request handler.

Middlewares work exactly as request handlers.

You can also filter by [request-methods](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods) and contexts:

## Existing Middlewares

Core middlewares are included in [MxMiddleware](./src/main/java/io/vacco/murmux/middleware).

#### CORS

To realize a CORS api you can use the CORS middleware.

[Cors](./src/test/java/examples/Cors.java)

#### Static Content

Use [MxStatic](./src/main/java/io/vacco/murmux/middleware/MxStatic.java) to serve static content.

Example:

[StaticContent](./src/test/java/examples/StaticContent.java)

#### Cookie Session

A simple in-memory cookie-session middleware is provided:

[CookieSession](./src/test/java/examples/CookieSession.java)

## Examples

#### File download

[FileDownload](./src/test/java/examples/FileDownload.java)

# Development

Requires Gradle 7.1 or later.

Create a file with the following content at `~/.gsOrgConfig.json`:

```
{
  "orgId": "vacco-oss",
  "orgConfigUrl": "https://raw.githubusercontent.com/vaccovecrana/org-config/refs/heads/main/vacco-oss.json"
}
```

Then run:

```
gradle clean build
```
