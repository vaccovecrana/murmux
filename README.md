Zero-dependencies HTTP Framework based on ExpressJS

# Getting Started

```java
public class Example {
  public static void main(String[] args) {
    Murmux app = new Murmux();
    app
      .get("/", (req, res) -> res.send("Hello World"))
      .listen(); // Will listen on port 80 which is set as default
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
  - [Custom middleware](#custom-middleware)
  - [Existing Middlewares](#existing-middlewares)
      - [CORS](#cors)
      - [Static Content](#static-content)
      - [Cookie Session](#cookie-session)
  - [Examples](#examples)
      - [File download](#file-download)
      - [Send cookies](#send-cookies)
- [Development](#development)

# Routing

### Direct

You can add routes (And middlewares) directly to handle requests. Direct also supports methods like `POST` `PATCH` `DELETE` and `PUT`, others need to be created manually:

[DirectRouting](./src/test/java/examples/DirectRouting.java)

### With Router

However, it's better to split your code. You can create routes and add them at runtime:

[DeferredRouting](./src/test/java/examples/DeferredRouting.java)

## URL Basics

You can create handlers for all [request-methods](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods) and contexts. Some examples:

[HttpMethods](./src/test/java/examples/HttpMethods.java)

### URL Parameters

Sometimes you want to create dynamic URLs where some parts are not static. With the `:` operator you can create variables in the URL which will be saved later in a `HashMap`.

Example request: `GET /posts/john/all`:

[UrlParameters](./src/test/java/examples/UrlParameters.java)

### URL Query

If you make a request which contains queries, you can access the queries over `req.getQuery(NAME)`.

Example request: `GET /posts?page=12&from=john`:

[UrlQuery](./src/test/java/examples/UrlQuery.java)

## Cookies

With `req.getCookie(NAME)` you can get a cookie by his name, and with `res.setCookie(NAME, VALUE)` you can easily set a cookie.

Example request: `GET /setcookie` and `GET /showcookie`:

[Cookies](./src/test/java/examples/Cookies.java)

## Form data

Use `req.getFormQuery(NAME)` to receive values from input elements of an HTML-Form.

[FormData](./src/test/java/examples/FormData.java)

> **Warning: Currently, File-inputs don't work, if there is a File-input the data won't get parsed!**

# Middleware

Middleware allows you to handle a request before it reaches any other request handler. To create middleware you have several interfaces:

* [MxHandler](./src/main/java/io/vacco/murmux/http/MxHandler.java) - handles requests.
* [MxFilter](./src/main/java/io/vacco/murmux/filter/MxFilter.java) - puts data on the request listener.
* [MxFilterTask](./src/main/java/io/vacco/murmux/filter/MxFilterTask.java) - for middleware which needs a background thread.

Middlewares work exactly as request handlers.

You can also filter by [request-methods](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods) and contexts:

[Middleware](./src/test/java/examples/Middleware.java)

## Custom middleware

Create a middleware class:

[PortMiddleware](./src/test/java/examples/PortMiddleware.java)

And use it:

[PortHandler](./src/test/java/examples/PortHandler.java)

## Existing Middlewares

Core middlewares are included in [MxMiddleware](./src/main/java/io/vacco/murmux/middleware/MxMiddleware.java).

#### CORS

To realize a CORS api you can use the CORS middleware.

[CorsMiddleware](./src/test/java/examples/CorsMiddleware.java)

#### Static Content

Use [MxFileProvider](./src/main/java/io/vacco/murmux/middleware/MxFileProvider.java) to serve static content.

Example:

[StaticContent](./src/test/java/examples/StaticContent.java)

#### Cookie Session

A simple cookie-session middleware is provided:

[CookieSession](./src/test/java/examples/CookieSession.java)

## Examples

#### File download

[FileDownload](./src/test/java/examples/FileDownload.java)

#### Send cookies

[SendCookies](./src/test/java/examples/SendCookies.java)

# Development

Requires Gradle 7.1 or later.

Create a file with the following content at `~/.gsOrgConfig`:

```
{
  "orgId": "vacco-oss",
  "orgConfigUrl": "https://vacco-oss.s3.us-east-2.amazonaws.com/vacco-oss.json",
}
```

Then run:

```
gradle clean build
```
