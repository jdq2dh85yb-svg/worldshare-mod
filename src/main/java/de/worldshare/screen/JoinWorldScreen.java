package de.worldshare.screen;

import de.worldshare.util.RelayServerClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Join World Screen
 * Ermöglicht es, einen Code einzugeben und sich mit einer gehosteten Welt zu verbinden
 */
public class JoinWorldScreen extends Screen {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final int INPUT_WIDTH = 200;
	private static final int INPUT_HEIGHT = 20;
	private static final int BUTTON_WIDTH = 150;
	private static final int BUTTON_HEIGHT = 40;

	private TextFieldWidget codeInputField;
	private ButtonWidget joinButton;
	private ButtonWidget backButton;

	private String statusMessage = "";
	private int statusMessageColor = 0xFFFFFF;
	private boolean isConnecting = false;

	public JoinWorldScreen() {
		super(Text.literal("Join World"));
	}

	@Override
	protected void init() {
		super.init();

		int centerX = this.width / 2;
		int centerY = this.height / 2;

		// Input-Feld für Code
		this.codeInputField = new TextFieldWidget(
			this.textRenderer,
			centerX - INPUT_WIDTH / 2,
			centerY - 40,
			INPUT_WIDTH,
			INPUT_HEIGHT,
			Text.literal("Code eingeben")
		);
		this.codeInputField.setMaxLength(10);
		this.codeInputField.setPlaceholder(Text.literal("z.B. A3F-92X"));
		this.addRenderableWidget(this.codeInputField);
		this.setInitialFocus(this.codeInputField);

		// "Beitreten" Button
		this.joinButton = ButtonWidget.builder(
			Text.literal("🔗 Beitreten"),
			button -> this.joinWorld()
		)
		.dimensions(centerX - BUTTON_WIDTH / 2, centerY + 20, BUTTON_WIDTH, BUTTON_HEIGHT)
		.build();
		this.addDrawableChild(this.joinButton);

		// "Zurück" Button
		this.backButton = ButtonWidget.builder(
			Text.literal("← Zurück"),
			button -> this.onClose()
		)
		.dimensions(centerX - BUTTON_WIDTH / 2, centerY + 80, BUTTON_WIDTH, BUTTON_HEIGHT)
		.build();
		this.addDrawableChild(this.backButton);

		this.joinButton.active = !this.isConnecting;
	}

	/**
	 * Versucht, sich mit der gehosteten Welt zu verbinden
	 */
	private void joinWorld() {
		String code = this.codeInputField.getText().trim().toUpperCase();

		if (code.isEmpty()) {
			this.statusMessage = "❌ Bitte gib einen Code ein!";
			this.statusMessageColor = 0xFF5555;
			return;
		}

		if (!code.matches("[A-Z0-9]{3}-[A-Z0-9]{3}")) {
			this.statusMessage = "❌ Ungültiges Format! (z.B. A3F-92X)";
			this.statusMessageColor = 0xFF5555;
			return;
		}

		this.isConnecting = true;
		this.statusMessage = "⏳ Verbindung wird hergestellt...";
		this.statusMessageColor = 0xFFFF00;
		this.joinButton.active = false;

		// Führe in separatem Thread aus
		new Thread(() -> {
			try {
				// Frage Relay-Server nach der Tunnel-Adresse
				RelayServerClient client = new RelayServerClient();
				String tunnelAddress = client.resolveCode(code);

				if (tunnelAddress == null || tunnelAddress.isEmpty()) {
					this.statusMessage = "❌ Code nicht gefunden. Bitte überprüfe den Code.";
					this.statusMessageColor = 0xFF5555;
					this.isConnecting = false;
					this.joinButton.active = true;
					LOGGER.warn("Code nicht auf Relay-Server gefunden: {}", code);
					return;
				}

				// Verbinde mit der Adresse
				this.statusMessage = "✅ Verbindung hergestellt!";
				this.statusMessageColor = 0x55FF55;

				// Minecraft mit der Adresse verbinden
				MinecraftClient mcClient = MinecraftClient.getInstance();
				mcClient.getNetworkHandler().connection.disconnect(Text.literal("Verbindung wird hergestellt..."));
				
				// Nutze DirectConnect Screen
				String[] addressParts = tunnelAddress.split(":");
				String host = addressParts[0];
				int port = addressParts.length > 1 ? Integer.parseInt(addressParts[1]) : 25565;

				mcClient.setScreen(new net.minecraft.client.gui.screen.multiplayer.ConnectScreen(
					null,
					mcClient,
					net.minecraft.server.ServerMetadata.createUnknown(host, port),
					null,
					false,
					null
				));

				LOGGER.info("Verbindung zu {} auf Port {} hergestellt", host, port);

			} catch (Exception e) {
				this.statusMessage = "❌ Fehler: " + e.getMessage();
				this.statusMessageColor = 0xFF5555;
				this.isConnecting = false;
				this.joinButton.active = true;
				LOGGER.error("Fehler beim Beitreten zur Welt", e);
			}
		}).start();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context, mouseX, mouseY, delta);

		int centerX = this.width / 2;
		int centerY = this.height / 2;

		// Titel
		context.drawCenteredTextWithShadow(
			this.textRenderer,
			Text.literal("🔗 Join World"),
			centerX,
			30,
			0xFFFFFF
		);

		// Beschreibung
		context.drawCenteredTextWithShadow(
			this.textRenderer,
			Text.literal("Gib den Code ein, den dir dein Freund gegeben hat"),
			centerX,
			centerY - 80,
			0xAAAAAA
		);

		// Status-Message
		if (!this.statusMessage.isEmpty()) {
			context.drawCenteredTextWithShadow(
				this.textRenderer,
				Text.literal(this.statusMessage),
				centerX,
				centerY + 60,
				this.statusMessageColor
			);
		}

		this.codeInputField.render(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public void onClose() {
		this.client.setScreen(new WorldShareMenuScreen());
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}
}
