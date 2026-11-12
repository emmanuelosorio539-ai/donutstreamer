package com.donutstreamer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DonutStreamerClient implements ClientModInitializer {
    public static volatile long sessionStart = System.currentTimeMillis();

    private static KeyBinding openKey;
    private static KeyBinding streamKey;

    @Override
    public void onInitializeClient() {
        Config.load();

        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.donutstreamer.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.donutstreamer"));
        streamKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.donutstreamer.stream", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F8, "category.donutstreamer"));

        HudRenderCallback.EVENT.register(Hud::render);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> sessionStart = System.currentTimeMillis());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) client.setScreen(new MainScreen());
            while (streamKey.wasPressed()) {
                Config c = Config.get();
                c.streamMode = !c.streamMode;
                c.save();
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("Modo Stream: ")
                            .append(Text.literal(c.streamMode ? "ON" : "OFF")
                                    .formatted(c.streamMode ? Formatting.GREEN : Formatting.RED)), true);
                }
            }
        });

        // Filtro de chat (privacidad) + registro automático de ventas/compras
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (overlay) return true;
            Config c = Config.get();
            String raw = message.getString();

            if (c.autoTrack) track(c, raw);

            if (c.streamMode) {
                if (c.hideWhispers && matches(c.whisperRegex, raw)) return false;
                String lower = raw.toLowerCase(Locale.ROOT);
                for (String w : c.censorWords) {
                    if (w != null && !w.isBlank() && lower.contains(w.toLowerCase(Locale.ROOT))) return false;
                }
            }
            return true;
        });
    }

    private static void track(Config c, String raw) {
        try {
            Matcher m = pattern(c.saleRegex).matcher(raw);
            if (m.find()) {
                Double p = Money.parse(m.group(1).trim());
                if (p != null) c.addEntry(new Config.Entry("(chat)", p, true));
                return;
            }
            m = pattern(c.buyRegex).matcher(raw);
            if (m.find()) {
                Double p = Money.parse(m.group(1).trim());
                if (p != null) c.addEntry(new Config.Entry("(chat)", p, false));
            }
        } catch (Exception ignored) {
        }
    }

    private static boolean matches(String regex, String text) {
        try {
            return pattern(regex).matcher(text).find();
        } catch (Exception e) {
            return false;
        }
    }

    private static final java.util.Map<String, Pattern> CACHE = new java.util.HashMap<>();

    private static Pattern pattern(String src) {
        return CACHE.computeIfAbsent(src, Pattern::compile);
    }
}
