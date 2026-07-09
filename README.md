# RequestExporter

A Burp Suite extension (built on the [Montoya API](https://portswigger.github.io/burp-extensions-montoya-api/)) that
adds a context menu option to the **Proxy** and **Repeater** tabs, letting you export one or more selected HTTP
requests/responses to the clipboard or a Postman collection.

## Features

Select one or more entries in Proxy history or Repeater, right-click, and choose **Request Exporter** for two options:

- **Copy to Clipboard** — exports the selected items in ascending order of their request index, formatting each
  entry as `Request Index: N` followed by the corresponding **Request** and **Response**.
- **Create Postman Collection** — generates a Postman Collection v2.1 JSON file containing all selected requests,
  ready for import into Postman.

## Installation

1. Download `RequestExporter.jar` from the [Releases](../../releases) page or the latest
   [GitHub Actions build artifact](../../actions).
2. In Burp Suite, go to **Extensions** → **Add**.
3. Set *Extension Type* to **Java** and browse to `RequestExporter.jar`.
4. Click **Next** — the extension loads automatically.

## Usage

1. In the **Proxy** → **HTTP history** table, or the **Repeater** tab list, select one or more entries.
2. Right-click the selection.
3. Choose **Request Exporter** → **Copy to Clipboard** or **Create Postman Collection**.

## Building

This project is built via GitHub Actions (Java 17 + Maven Shade Plugin) — see
[`.github/workflows/build.yml`](.github/workflows/build.yml). Push to any branch to trigger a build, or push a
`v*` tag to also cut a GitHub Release with the built JAR attached.

To build locally instead:

```bash
mvn clean package
```

The shaded JAR will be at `target/requestexporter-<version>.jar`.
