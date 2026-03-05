
# Replay Web - Spring Boot Starter

[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.java.net/)
[![Spring Boot 4.0](https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Build](https://github.com/patrickhofmann0/replay-spring-boot-starter/actions/workflows/build.yml/badge.svg)](https://github.com/patrickhofmann0/replay-spring-boot-starter/actions/workflows/build.yml)

Ein Spring Boot Starter, der automatisch HTTP-Requests erfasst, sichbar macht und daraus JUnit-Tests generiert. 
Perfekt für Debugging, Test-Driven Development, API-Dokumentation und Regression Testing.

## 🚀 Features

- **Automatische Request-Erfassung**: Alle eingehenden HTTP-Requests werden automatisch aufgezeichnet
- **Test-Code-Generierung**: Automatische Generierung von MockMvc-Tests aus erfassten Requests
- **Web-Dashboard**: Integriertes Dashboard zur Anzeige und Verwaltung erfasster Requests
- **Spring Security Integration**: Funktioniert nahtlos mit Spring Security (erfasst auch 401/403 Responses)
- **Header-Redaction**: Sensible Header (Authorization, Cookie, etc.) werden automatisch verschleiert
- **Flexible Konfiguration**: Konfigurationsmöglichkeiten über `application.properties`
- **Zero-Configuration**: Funktioniert out-of-the-box mit sinnvollen Standardwerten

## 📋 Voraussetzungen

- Java 21 oder höher
- Spring Boot 4.0 oder höher
- Maven

## 🔧 Installation

### 1. Abhängigkeit zum Projekt hinzufügen

Fügen Sie die Abhängigkeit zu Ihrer `pom.xml` hinzu:

```xml
<dependency>
    <groupId>de.hofmann-hbm.replay</groupId>
    <artifactId>replay-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. Konfiguration (Optional)

Fügen Sie folgende Properties zu Ihrer `application.properties` hinzu:

```properties
# Replay aktivieren/deaktivieren (Standard: true)
replay.enabled=true

# Dashboard aktivieren/deaktivieren (Standard: false)
replay.dashboard-enabled=true

# Maximale Anzahl erfasster Requests (Standard: 100)
replay.max-captured-requests=100

# Pfade, die nicht erfasst werden sollen (Standard: /actuator/**, /favicon.ico)
replay.exclude-paths=/actuator/**,/favicon.ico,/static/**

# Header, die verschleiert werden sollen (Standard: Authorization, Cookie, X-Api-Key)
replay.exclude-headers=Authorization,Cookie,X-Api-Key,Set-Cookie
```

### 3. Anwendung starten

Das war's! Der Starter wird automatisch konfiguriert und aktiviert.

## 📖 Verwendung

### Dashboard aufrufen

Wenn das Dashboard aktiviert ist, können Sie es unter folgender URL aufrufen:

```
http://localhost:8080/replay/dashboard
```

### Request-Erfassung

Alle HTTP-Requests werden automatisch erfasst. Führen Sie einfach Requests gegen Ihre Anwendung aus:

```bash
# GET Request
curl http://localhost:8080/hello?name=Patrick

# POST Request mit JSON Body
curl -X POST http://localhost:8080/echo \
  -H "Content-Type: application/json" \
  -d '{"name":"Patrick"}'
```

### Test-Code generieren

1. Öffnen Sie das Dashboard: `http://localhost:8080/replay/dashboard`
2. Klicken Sie auf einen erfassten Request
3. Klicken Sie auf "Generate Tests"
4. Kopieren Sie den generierten Test-Code

Beispiel eines generierten MockMvc-Tests:

```java
@Test
@DisplayName("Test for GET /hello")
void shouldHandleRequest() throws Exception {
    mockMvc.perform(
        get("/hello")
            .queryParam("name", "Patrick")
    )
    .andExpect(status().is(200))
    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
    .andExpect(content().json("""
        Hello, Patrick!
        """, true))
    ;
}
```

## 🔒 Spring Security Integration

Der Replay-Filter funktioniert vollständig mit Spring Security. Er wird **nach** dem Security-Filter ausgeführt und erfasst daher:

- ✅ Erfolgreiche authentifizierte Requests (200 OK)
- ✅ Abgelehnte unauthentifizierte Requests (401 Unauthorized)
- ✅ Verbotene Requests (403 Forbidden)

### Sensible Header werden automatisch verschleiert

```java
// Request mit Authorization Header
curl -H "Authorization: Bearer secret-token" http://localhost:8080/api/secured

// Im Dashboard wird angezeigt:
// Authorization: [REDACTED]
```

Weitere Details siehe [SECURITY.md](docs/SECURITY.md).

## 🎨 Dashboard-Features

Das integrierte Web-Dashboard bietet:

- **Request-Übersicht**: Tabellarische Ansicht aller erfassten Requests
- **Request-Details**: Detailansicht mit Headers, Query-Parametern, Request-Body und Response
- **Test-Generierung**: Ein-Klick-Generierung von Test-Code
- **Auto-Refresh**: Automatische Aktualisierung der Request-Liste (alle 3 Sekunden)
- **Copy-to-Clipboard**: Einfaches Kopieren des generierten Test-Codes

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

## 📁 Projektstruktur

```
replay-spring-boot-starter/
├── src/main/java/de/hofmannhbm/replay/
│   ├── config/
│   │   ├── ReplayAutoConfiguration.java      # Spring Boot Auto-Configuration
│   │   └── ReplayProperties.java             # Konfigurationsparameter
│   ├── core/
│   │   ├── CapturedRequest.java              # Request-Datenmodell
│   │   ├── InMemoryReplayRequestStorage.java # In-Memory-Speicher
│   │   ├── ReplayCaptureFilter.java          # Servlet-Filter für Request-Erfassung
│   │   └── ReplayRequestRepository.java      # Repository-Interface
│   ├── generator/
│   │   ├── MockMvcGenerator.java             # MockMvc-Test-Generator
│   │   ├── TestCodeGenerator.java            # Generator-Interface
│   │   └── TestGenerationService.java        # Service für Test-Generierung
│   └── ReplayDashboardController.java        # Dashboard-Controller
├── src/main/resources/
│   ├── META-INF/spring/
│   │   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│   ├── static/                               # CSS, JS, etc.
│   └── templates/replay/                     # Thymeleaf-Templates
├── example/                                  # Beispiel-Anwendung
│   ├── src/main/java/...
│   ├── demo-requests.http                    # HTTP-Request-Beispiele
│   └── pom.xml
└── pom.xml
```

## 🧩 Architektur

### Komponenten

1. **ReplayCaptureFilter**: Servlet-Filter, der alle eingehenden Requests erfasst
2. **InMemoryReplayRequestStorage**: In-Memory-Speicher für erfasste Requests (Thread-safe mit CircularBuffer)
3. **ReplayDashboardController**: Spring MVC Controller für das Web-Dashboard
4. **TestGenerationService**: Service zur Generierung von Test-Code aus erfassten Requests
5. **MockMvcGenerator**: Konkrete Implementierung für MockMvc-Test-Generierung

### Filter-Order und Spring Security

Der `ReplayCaptureFilter` wird mit `Ordered.LOWEST_PRECEDENCE - 1` registriert, sodass er **nach** Spring Security läuft:

```java
@Bean
public FilterRegistrationBean<ReplayCaptureFilter> replayCaptureFilterRegistration(
        ReplayCaptureFilter replayCaptureFilter) {
    FilterRegistrationBean<ReplayCaptureFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(replayCaptureFilter);
    registration.setOrder(Ordered.LOWEST_PRECEDENCE - 1);
    registration.addUrlPatterns("/*");
    return registration;
}
```

Dies ermöglicht die Erfassung aller Responses, unabhängig davon, ob sie von Security oder vom Controller kommen.

## 🧪 Testing

Das Projekt enthält umfangreiche Tests:

- **Unit-Tests**: Für alle Komponenten (Filter, Storage, Generator, etc.)
- **Integration-Tests**: Für Spring Security Integration
- **Test-Coverage**: Hohe Test-Abdeckung aller kritischen Komponenten

Beispiele:

```bash
# Alle Tests ausführen
mvn test

# Nur bestimmte Tests ausführen
mvn test -Dtest=SecurityIntegrationTest
```

## 🔐 Sicherheit

### Header-Redaction

Sensible Header werden standardmäßig verschleiert:

- `Authorization`
- `Cookie`
- `X-Api-Key`
- `Set-Cookie` (in Responses)

Diese können über `replay.exclude-headers` angepasst werden.

### Ausgeschlossene Pfade

Folgende Pfade werden standardmäßig **nicht** erfasst:

- `/replay/**` (Dashboard selbst)
- `/actuator/**` (Actuator-Endpunkte)
- `/favicon.ico`

Weitere Pfade können über `replay.exclude-paths` konfiguriert werden.

## 📄 Lizenz

Dieses Projekt ist unter der [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) lizenziert.

## 👤 Autor

**Patrick Hofmann**

- GitHub: [@patrickhofmann0](https://github.com/patrickhofmann0)


## 📞 Support

Bei Fragen oder Problemen:

- Öffnen Sie ein [Issue](https://github.com/patrickhofmann0/replay-spring-boot-starter/issues)
- Siehe auch [SECURITY.md](docs/SECURITY.md) für Security-bezogene Fragen

---
