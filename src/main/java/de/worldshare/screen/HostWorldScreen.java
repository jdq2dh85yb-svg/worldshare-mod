package de.worldshare.screen;

import de.worldshare.util.PlayitDownloader;
import de.worldshare.util.RelayServerClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Host World Screen
 * Zeigt den generierten Code und die Tunnel-Adresse an
 */
public class HostWorldScreen extends Screen {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final int BUTTON_WIDTH = 150;
	private static final int BUTTON_HEIGHT = 40;

	private String generatedCode = "Laden...";
	private String tunnelAddress = "---";
	private String statusMessage = "Tunnel wird gestartet...";
	private boolean isHosting = false;
	private boolean hasError = false;

	private ButtonWidget copyButton;
	private ButtonWidget stopButton;
	private ButtonWidget backButton;

	private Timer hostingTimer;
	private RelayServerClient relayClient;

	public HostWorldScreen() {
		super(Text.literal("Host World"));
	}

	@Override
	protected void init() {
		super.init();

		int centerX = this.width / 2;
		int centerY = this.height / 2;

		// "Code kopieren" Button
		this.copyButton = ButtonWidget.builder(
			Text.literal("📋 Code kopieren"),
			button -> this.copyCodeToClipboard()
		)
		.dimensions(centerX - BUTTON_WIDTH / 2, centerY + 80, BUTTON_WIDTH, BUTTON_HEIGHT)
		.build();
		this.addDrawableChild(this.copyButton);

		// "Stop" Button
		this.stopButton = ButtonWidget.builder(
			Text.literal("⏹ Hosting beenden"),
			button -> this.stopHosting()
		)
		.dimensions(centerX - BUTTON_WIDTH / 2, centerY + 130, BUTTON_WIDTH, BUTTON_HEIGHT)
		.build();
		this.addDrawableChild(this.stopButton);

		// "Zurück" Button
		this.backButton = ButtonWidget.builder(
			Text.literal("← Zurück"),
			button -> this.onClose()
		)
		.dimensions(centerX - BUTTON_WIDTH / 2, centerY + 180, BUTTON_WIDTH, BUTTON_HEIGHT)
		.build();
		this.addDrawableChild(this.backButton);

		// Starte Hosting
		this.startHosting();
	}

	private void startHosting() {
		// Ausführen in separatem Thread um nicht zu blocken
		new Thread(() -> {
			try {
				// Prüfe ob playit.gg CLI vorhanden ist
				if (!PlayitDownloader.isPlayitAvailable()) {
					this.statusMessage = "Downloade playit.gg CLI...";
					PlayitDownloader.downloadPlayit();
				}

				// Generiere einen kurzen Code
				this.generatedCode = this.generateShareCode();
				
				// Starte LAN-Server mit online-mode=false
				this.startLANServer();

				// Starte playit.gg Tunnel
				this.tunnelAddress = PlayitDownloader.startTunnel();

				// Veröffentliche Code auf Relay-Server
				this.relayClient = new RelayServerClient();
				this.relayClient.publishCode(this.generatedCode, this.tunnelAddress);

				this.statusMessage = "✅ Bereit! Freunde können beitreten.";
				this.isHosting = true;

				LOGGER.info("WorldShare Hosting gestartet. Code: {}, Tunnel: {}", this.generatedCode, this.tunnelAddress);

				// Auto-Update Status nach 30 Sekunden
				if (this.hostingTimer != null) {
					this.hostingTimer.cancel();
				}

			} catch (IOException e) {
				this.statusMessage = "❌ Fehler: " + e.getMessage();
				this.hasError = true;
				LOGGER.error("Fehler beim Starten des Hostings", e);
			}
		}).start();
	}

	/**
	 * Generiert einen kurzen, lesbaren Code (z.B. A3F-92X)
	 */
	private String generateShareCode() {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuilder code = new StringBuilder();
		for (int i = 0; i < 3; i++) {
			code.append(chars.charAt((int) (Math.random() * chars.length())));
		}
		code.append("-");
		for (int i = 0; i < 3; i++) {
			code.append(chars.charAt((int) (Math.random() * chars.length())));
		}
		return code.toString();
	}

	/**
	 * Startet den LAN-Server mit online-mode=false
	 */
	private void startLANServer() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != null && client.player != null) {
			client.getNetworkHandler().sendCommand("publish local 0 false");
			LOGGER.info("LAN-Server gestartet mit online-mode=false");
		} else {
			throw new RuntimeException("Keine Singleplayer-Welt aktiv!");
		}
	}

	/**
	 * Kopiere den Code in die Zwischenablage
	 */
	private void copyCodeToClipboard() {
		MinecraftClient client = MinecraftClient.getInstance();
		String fullText = "WorldShare Code: " + this.generatedCode + " | " + this.tunnelAddress;
		client.keyboard.setClipboard(this.generatedCode);
		this.statusMessage = "✓ Code kopiert!";
		
		// Überflüssig nach 3 Sekunden
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				if (HostWorldScreen.this.statusMessage.contains("Code kopiert")) {
					HostWorldScreen.this.statusMessage = "✅ Bereit! Freunde können beitreten.";
				}
			}
		}, 3000);
	}

	/**
	 * Beendet das Hosting
	 */
	private void stopHosting() {
		if (this.hostingTimer != null) {
			this.hostingTimer.cancel();
		}
		try {
			PlayitDownloader.stopTunnel();
			if (this.relayClient != null) {
				this.relayClient.removeCode(this.generatedCode);
			}
		} catch (Exception e) {
			LOGGER.error("Fehler beim Beenden des Hostings", e);
		}
		this.isHosting = false;
		this.onClose();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context, mouseX, mouseY, delta);

		int centerX = this.width / 2;
		int centerY = this.height / 2;

		// Titel
		context.drawCenteredTextWithShadow(
			this.textRenderer,
			Text.literal("🌐 Host World"),
			centerX,
			30,
			0xFFFFFF
		);

		// Code - GROSS UND GELB
		context.drawCenteredTextWithShadow(
			this.textRenderer,
			Text.literal(this.generatedCode),
			centerX,
			centerY - 40,
			0xFFD700
		);

		// Tunnel-Adresse - Klein und Grau
		context.drawCenteredTextWithShadow(
			this.textRenderer,
			Text.literal("Tunnel: " + this.tunnelAddress),
			centerX,
			centerY + 10,
			0xAAAAAA
		);

		// Status-Message
		int statusColor = this.hasError ? 0xFF5555 : 0x55FF55;
		context.drawCenteredTextWithShadow(
			this.textRenderer,
			Text.literal(this.statusMessage),
			centerX,
			centerY + 40,
			statusColor
		);

		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public void onClose() {
		this.stopHosting();
		this.client.setScreen(new WorldShareMenuScreen());
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}
}
