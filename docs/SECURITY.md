# Spring Security Integration

## Funktionsweise mit Spring Security

Der `ReplayCaptureFilter` funktioniert **vollständig** mit Spring Security abgesicherten Endpunkten.

### Warum funktioniert es?

Der Filter wird mit `FilterRegistrationBean` und der **richtigen Order** registriert:

```java
@Bean
public FilterRegistrationBean<ReplayCaptureFilter> replayCaptureFilterRegistration(
        ReplayCaptureFilter replayCaptureFilter) {
    FilterRegistrationBean<ReplayCaptureFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(replayCaptureFilter);
    // WICHTIG: Order NACH Spring Security (Security Filter hat Order = -100)
    registration.setOrder(Ordered.LOWEST_PRECEDENCE - 1);
    registration.addUrlPatterns("/*");
    return registration;
}
```

**Wichtige Punkte:**

1. **Order = LOWEST_PRECEDENCE - 1**: Der Filter läuft als einer der letzten Filter in der Chain
2. **Nach Spring Security**: Spring Security Filter haben eine höhere Priorität (niedrigere Order-Zahl)
3. **Erfasst alle Responses**: Egal ob vom Security Filter (401, 403) oder vom Controller (200, 201, etc.)

### Was wird erfasst?

Der Filter erfasst **alle** HTTP-Responses, einschließlich:

#### ✅ Erfolgreiche authentifizierte Requests
```
GET /api/users
Authorization: Bearer xyz...
→ 200 OK
```

#### ✅ Abgelehnte unauthentifizierte Requests
```
GET /api/users
(kein Authorization Header)
→ 401 Unauthorized
```

#### ✅ Forbidden Requests (keine Berechtigung)
```
POST /api/admin
Authorization: Bearer user-token
→ 403 Forbidden
```

#### ✅ Alle anderen Security-Responses
- 401 Unauthorized
- 403 Forbidden
- 302 Redirect (bei Form-Login)

### Beispiel-Konfiguration

#### Spring Security Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/replay/**").permitAll() // Replay Dashboard offen lassen
                .requestMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());
        return http.build();
    }
}
```

### Replay Dashboard

Das Replay Dashboard (`/replay/**`) sollte **immer freigeschaltet** werden, damit du die erfassten Requests sehen kannst:

```java
.requestMatchers("/replay/**").permitAll()
```

### Filter-Chain Reihenfolge

Visualisierung der Filter-Chain:

```
Request →
  ├─ Spring Security Filter (Order = -100)
  │   ├─ Authentication Check
  │   ├─ Authorization Check
  │   └─ Response: 401/403 oder weiter
  ├─ ... andere Filter ...
  ├─ Controller (falls autorisiert)
  └─ ReplayCaptureFilter (Order = LOWEST_PRECEDENCE - 1)
      └─ Erfasst Request + Response ✓
→ Response
```

### Tests

Der `SecurityIntegrationTest` demonstriert, dass alle Security-Szenarien korrekt erfasst werden:

```bash
mvn test -Dtest=SecurityIntegrationTest
```

**Test-Szenarien:**
- ✅ Unauthorized Request (401)
- ✅ Authorized Request (200)
- ✅ Forbidden Request (403)
- ✅ Replay Dashboard nicht erfasst

### Vorteile

1. **Vollständige Erfassung**: Auch abgelehnte Requests werden erfasst
2. **Security-Kontext erhalten**: Authorization Header werden mit erfasst
3. **Testgenerierung**: Auch für geschützte Endpunkte können Tests generiert werden
4. **Debugging**: Sieh genau, welche Requests von Security abgelehnt wurden

### Best Practices

#### 1. Replay Dashboard freischalten
```java
.requestMatchers("/replay/**").permitAll()
```

#### 2. In Produktion deaktivieren
```properties
# application-prod.properties
replay.enabled=false
```

#### 3. Sensible Header ausschließen (optional)
```properties
# application.properties
replay.exclude-headers=Authorization,Cookie,X-Api-Key
```

## Fazit

Der Filter funktioniert **nahtlos** mit Spring Security, da er am Ende der Filter-Chain positioniert ist und alle Responses erfasst - egal ob sie von Security oder vom Controller kommen.

