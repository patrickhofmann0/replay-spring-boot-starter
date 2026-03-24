# Replay Web - Spring Boot Starter

[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.java.net/)
[![Spring Boot 4.0](https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Build](https://github.com/patrickhofmann0/replay-spring-boot-starter/actions/workflows/build.yml/badge.svg)](https://github.com/patrickhofmann0/replay-spring-boot-starter/actions/workflows/build.yml)

Ein Spring Boot Starter, der automatisch HTTP-Requests erfasst, sichbar macht und daraus JUnit-Tests generiert. 
Perfekt für Debugging, Test-Driven Development, API-Dokumentation und Regression Testing.

## 🚀 Features

- **Automatische Request-Erfassung**: Alle eingehenden HTTP-Requests werden automatisch aufgezeichnet
- **Web-Dashboard**: Integriertes Dashboard zur Anzeige und Verwaltung erfasster Requests
- **Header-Redaction**: Sensible Header (Authorization, Cookie, etc.) werden automatisch verschleiert

## 📋 Voraussetzungen

- Java 21 oder höher
- Spring Boot 4.0 oder höher
- Maven

## 🔧 Installation

### Abhängigkeit zum Projekt hinzufügen (clone repo)

```bash
mvn install
```

Füge die Abhängigkeit zue `pom.xml` hinzu:

```xml
<dependency>
    <groupId>de.hofmann-hbm.replay</groupId>
    <artifactId>replay-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Abhängigkeit zum Projekt hinzufügen (Maven Central)
tbd.

### Konfiguration (Optional)

Fügen Sie folgende Properties zu Ihrer `application.properties` hinzu:

```properties
# Replay aktivieren/deaktivieren (Standard: true)
replay.enabled=true

# Dashboard aktivieren/deaktivieren (Standard: false)
replay.dashboardEnabled=true

# Maximale Anzahl erfasster Requests (Standard: 100)
replay.maxCapturedRequests=100

# Pfade, die nicht erfasst werden sollen (Standard: /actuator/**, /favicon.ico)
replay.excludePaths=/actuator/**,/favicon.ico,/static/**

# Header, die verschleiert werden sollen (Standard: Authorization, Cookie, X-Api-Key)
replay.excludeHeaders=Authorization,Cookie,X-Api-Key,Set-Cookie
```

### Anwendung starten

Das war's! Der Starter wird automatisch konfiguriert und aktiviert.

## 📖 Verwendung

### Dashboard aufrufen

Wenn das Dashboard aktiviert ist, können Sie es unter folgender URL aufrufen:

```
http://localhost:{PORT_ANWENDUNG}/replay/dashboard
```

### Sensible Header können automatisch verschleiert werden

```java
// Request mit Authorization Header
curl -H "Authorization: Bearer secret-token" http://localhost:8080/api/secured

// Im Dashboard wird angezeigt:
// Authorization: [REDACTED]
```
## 🛠 Entwicklung

### Projekt klonen

```bash
git clone https://github.com/patrickhofmann0/replay-spring-boot-starter.git
cd replay-spring-boot-starter
```

### Projekt bauen

```bash
mvn clean install
```

### Tests ausführen

```bash
mvn test
```

### Beispiel-Anwendung starten

```bash
cd example
mvn spring-boot:run
```

Die Beispiel-Anwendung startet auf Port 8080 und enthält einige Demo-Endpunkte:

- `GET /hello?name=Name` - Gibt eine Begrüßung zurück
- `POST /echo` - Echo-Endpunkt mit JSON-Body

Beispiel-Requests befinden sich in `example/demo-requests.http`.

