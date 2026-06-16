package de.worldshare.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * RelayServerClient
 * Kommuniziert mit dem Relay-Server um Codes zu veröffentlichen und aufzulösen
 */
public class RelayServerClient {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final String RELAY_SERVER_URL = "https://worldshare-relay.render.com"; // Ändere diese URL nach dem Deployment
	private static final Gson GSON = new Gson();

	/**
	 * Veröffentliche einen Code mit der dazugehörigen Tunnel-Adresse
	 */
	public void publishCode(String code, String tunnelAddress) throws IOException {
		String endpoint = RELAY_SERVER_URL + "/publish";

		JsonObject payload = new JsonObject();
		payload.addProperty("code", code);
		payload.addProperty("address", tunnelAddress);

		String jsonPayload = GSON.toJson(payload);

		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Content-Length", String.valueOf(jsonPayload.length()));
			conn.setDoOutput(true);

			// Schreibe Request Body
			try (var os = conn.getOutputStream()) {
				os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
				os.flush();
			}

			int responseCode = conn.getResponseCode();
			if (responseCode != 200) {
				throw new IOException("Relay-Server antwortet mit Code: " + responseCode);
			}

			LOGGER.info("Code {} erfolgreich auf Relay-Server veröffentlicht", code);

		} catch (IOException e) {
			LOGGER.error("Fehler beim Veröffentlichen des Codes", e);
			throw e;
		}
	}

	/**
	 * Löse einen Code auf und gib die Tunnel-Adresse zurück
	 */
	public String resolveCode(String code) throws IOException {
		String endpoint = RELAY_SERVER_URL + "/resolve?code=" + code;

		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			int responseCode = conn.getResponseCode();
			if (responseCode == 404) {
				LOGGER.warn("Code nicht gefunden auf Relay-Server: {}", code);
				return null;
			}

			if (responseCode != 200) {
				throw new IOException("Relay-Server antwortet mit Code: " + responseCode);
			}

			// Lese Response
			String response;
			try (var is = conn.getInputStream()) {
				response = new String(is.readAllBytes(), StandardCharsets.UTF_8);
			}

			JsonObject jsonResponse = GSON.fromJson(response, JsonObject.class);
			String address = jsonResponse.get("address").getAsString();

			LOGGER.info("Code {} aufgelöst zu Adresse: {}", code, address);

			return address;

		} catch (IOException e) {
			LOGGER.error("Fehler beim Auflösen des Codes", e);
			throw e;
		}
	}

	/**
	 * Entferne einen Code vom Relay-Server
	 */
	public void removeCode(String code) throws IOException {
		String endpoint = RELAY_SERVER_URL + "/remove?code=" + code;

		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
			conn.setRequestMethod("DELETE");

			int responseCode = conn.getResponseCode();
			if (responseCode != 200) {
				LOGGER.warn("Fehler beim Löschen des Codes: {}", responseCode);
			}

			LOGGER.info("Code {} wurde gelöscht", code);

		} catch (IOException e) {
			LOGGER.error("Fehler beim Löschen des Codes", e);
		}
	}
}
