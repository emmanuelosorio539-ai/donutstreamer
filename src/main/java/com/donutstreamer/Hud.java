package com.donutstreamer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;

/** Overlay compacto para stream: estado, tiempo de sesión, ganancia y meta. */
public final class Hud {
    private Hud() {}

    public static void render(DrawContext ctx, RenderTickCounter tick) {
        Config c = Config.get();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!c.hudEnabled || mc.options.hudHidden || mc.player == null) return;

        TextRenderer tr = mc.textRenderer;
        long now = System.currentTimeMillis();
        int x = 8, y = 8, w = 158;
        boolean hasGoal = c.goal > 0;
        int h = hasGoal ? 62 : 48;

        // fondo + borde + barra de acento
        ctx.fill(x, y, x + w, y + h, 0xC00E1118);
        ctx.fill(x, y, x + w, y + 1, 0x33FFFFFF);
        ctx.fill(x, y + h - 1, x + w, y + h, 0x33000000);
        ctx.fill(x, y, x + 3, y + h, c.accent);

        // cabecera: punto "en vivo" + temporizador
        long s = Math.max(0, (now - DonutStreamerClient.sessionStart) / 1000);
        String time = String.format("%02d:%02d:%02d", s / 3600, (s / 60) % 60, s % 60);
        if (c.streamMode) {
            int a = 170 + (int) (85 * Math.sin(now / 250.0));
            ctx.fill(x + 10, y + 7, x + 16, y + 13, (a << 24) | 0xFF3B3B);
            ctx.drawText(tr, "EN VIVO", x + 21, y + 6, 0xFFFFFFFF, true);
        } else {
            ctx.drawText(tr, "SESIÓN", x + 10, y + 6, 0xFFB6BDCB, true);
        }
        ctx.drawText(tr, time, x + w - 8 - tr.getWidth(time), y + 6, 0xFFB6BDCB, true);

        // ganancia de la sesión
        double profit = c.profit(DonutStreamerClient.sessionStart);
        String p = (profit >= 0 ? "+" : "") + Money.format(profit);
        ctx.drawText(tr, "Ganancia", x + 10, y + 19, 0xFF8892A6, false);
        ctx.drawText(tr, p, x + w - 8 - tr.getWidth(p), y + 19, profit >= 0 ? 0xFF4ADE80 : 0xFFF87171, true);

        int line = y + 32;
        if (hasGoal) {
            double ratio = Math.max(0, Math.min(1, profit / c.goal));
            int bx = x + 10, bw = w - 20;
            ctx.fill(bx, line, bx + bw, line + 5, 0xFF262B36);
            ctx.fill(bx, line, bx + (int) (bw * ratio), line + 5, c.accent);
            String pct = (int) (ratio * 100) + "% de " + Money.format(c.goal);
            ctx.drawText(tr, pct, bx, line + 8, 0xFF8892A6, false);
            line += 20;
        }

        // FPS y ping
        PlayerListEntry e = mc.getNetworkHandler() == null ? null
                : mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        int ping = e == null ? 0 : e.getLatency();
        ctx.drawText(tr, mc.getCurrentFps() + " FPS  ·  " + ping + " ms", x + 10, line, 0xFF6B7487, false);
    }
}
