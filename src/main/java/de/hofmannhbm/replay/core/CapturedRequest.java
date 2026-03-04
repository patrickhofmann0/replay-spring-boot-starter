package de.hofmannhbm.replay.core;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Repräsentiert einen aufgezeichneten HTTP-Request und die zugehörige Response.
 * Nutzt Java 21 Records für maximale Kompaktheit.
 */
public record CapturedRequest(
    String id,                // Eindeutige UUID für die UI
    String method,            // GET, POST, etc.
    String path,              // Die URL (z.B. /api/users)
    String queryParams,       // Optional: ?id=123
    Map<String, String> headers, // Wichtig für Auth oder Content-Type
    String requestBody,       // Der Payload (JSON/XML)
    int responseStatus,       // 200, 201, 400, etc.
    String responseBody,      // Was hat der Server geantwortet?
    LocalDateTime timestamp   // Wann wurde der Request abgefangen?
) {
    // Hilfsmethode: Prüft, ob der Body ein JSON ist
    public boolean hasJsonBody() {
        return requestBody != null && (requestBody.startsWith("{") || requestBody.startsWith("["));
    }

    // Hilfsmethode: Formatiert den Pfad für die Anzeige
    public String getShortPath() {
        if (path == null) return "";
        return path.length() > 30 ? path.substring(0, 27) + "..." : path;
    }
}