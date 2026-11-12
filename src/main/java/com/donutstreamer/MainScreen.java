package com.donutstreamer;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** Panel principal: pestañas "Stream" y "Subastas". */
public class MainScreen extends Screen {
    private static final int W = 380, H = 240;
    private static final int[] SWATCHES = {0xFFFF8A00, 0xFF22D3EE, 0xFFA78BFA, 0xFF34D399, 0xFFF472B6};
    private static int tab = 0;

    private int px, py;
    private TextFieldWidget itemField, priceField;
    private ButtonWidget streamBtn, whisperBtn, hudBtn, trackBtn;

    public MainScreen() {
        super(Text.literal("Donut Streamer"));
    }

    private static Text onOff(String label, boolean v) {
        return Text.literal(label + ": ").append(
                Text.literal(v ? "ON" : "OFF").formatted(v ? Formatting.GREEN : Formatting.RED));
    }

    private ButtonWidget tabBtn(String name, int idx, int x) {
        return ButtonWidget.builder(Text.literal(name), b -> {
            tab = idx;
            clearAndInit();
        }).dimensions(x, py + 32, 84, 18).build();
    }

    @Override
    protected void init() {
        px = (width - W) / 2;
        py = (height - H) / 2;
        Config c = Config.get();

        addDrawableChild(tabBtn("Stream", 0, px + 16));
        addDrawableChild(tabBtn("Subastas", 1, px + 104));

        if (tab == 0) {
            streamBtn = addDrawableChild(ButtonWidget.builder(onOff("Modo Stream", c.streamMode), b -> {
                c.streamMode = !c.streamMode;
                b.setMessage(onOff("Modo Stream", c.streamMode));
            }).dimensions(px + 16, py + 62, 170, 20).build());

            whisperBtn = addDrawableChild(ButtonWidget.builder(onOff("Ocultar susurros", c.hideWhispers), b -> {
                c.hideWhispers = !c.hideWhispers;
                b.setMessage(onOff("Ocultar susurros", c.hideWhispers));
            }).dimensions(px + 194, py + 62, 170, 20).build());

            hudBtn = addDrawableChild(ButtonWidget.builder(onOff("Overlay HUD", c.hudEnabled), b -> {
                c.hudEnabled = !c.hudEnabled;
                b.setMessage(onOff("Overlay HUD", c.hudEnabled));
            }).dimensions(px + 16, py + 88, 170, 20).build());

            trackBtn = addDrawableChild(ButtonWidget.builder(onOff("Registro auto", c.autoTrack), b -> {
                c.autoTrack = !c.autoTrack;
                b.setMessage(onOff("Registro auto", c.autoTrack));
            }).dimensions(px + 194, py + 88, 170, 20).build());

            TextFieldWidget goal = new TextFieldWidget(textRenderer, px + 16, py + 130, 170, 20, Text.literal("Meta"));
            goal.setPlaceholder(Text.literal("Meta de ganancia (ej. 10m)"));
            goal.setText(c.goal > 0 ? Money.format(c.goal).replace("$", "") : "");
            goal.setChangedListener(s -> {
                Double v = Money.parse(s);
                c.goal = v == null ? 0 : v;
            });
            addDrawableChild(goal);
        } else {
            itemField = new TextFieldWidget(textRenderer, px + 16, py + 62, 140, 20, Text.literal("Item"));
            itemField.setPlaceholder(Text.literal("Item"));
            addDrawableChild(itemField);

            priceField = new TextFieldWidget(textRenderer, px + 162, py + 62, 80, 20, Text.literal("Precio"));
            priceField.setPlaceholder(Text.literal("Precio 1.5m"));
            addDrawableChild(priceField);

            addDrawableChild(ButtonWidget.builder(Text.literal("+ Venta").formatted(Formatting.GREEN),
                    b -> addEntry(true)).dimensions(px + 248, py + 62, 58, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("+ Compra").formatted(Formatting.RED),
                    b -> addEntry(false)).dimensions(px + 310, py + 62, 58, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Borrar último"), b -> {
                if (!c.entries.isEmpty()) c.entries.remove(c.entries.size() - 1);
                c.save();
            }).dimensions(px + 16, py + H - 28, 90, 18).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Limpiar todo"), b -> {
                c.entries.clear();
                c.save();
            }).dimensions(px + 112, py + H - 28, 90, 18).build());
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Cerrar"), b -> close())
                .dimensions(px + W - 76, py + H - 28, 60, 18).build());
    }

    private void addEntry(boolean sale) {
        Double p = Money.parse(priceField.getText());
        if (p == null || p <= 0) return;
        String name = itemField.getText().isBlank() ? "Item" : itemField.getText().trim();
        Config.get().addEntry(new Config.Entry(name, p, sale));
        itemField.setText("");
        priceField.setText("");
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        Config c = Config.get();
        ctx.fill(0, 0, width, height, 0xAA05070B);

        // panel
        ctx.fill(px, py, px + W, py + H, 0xF0141720);
        ctx.fill(px, py, px + W, py + 1, 0x44FFFFFF);
        ctx.fill(px, py + H - 1, px + W, py + H, 0x44000000);
        ctx.fill(px, py, px + 1, py + H, 0x22FFFFFF);
        ctx.fill(px + W - 1, py, px + W, py + H, 0x22FFFFFF);

        // cabecera con degradado de acento
        ctx.fillGradient(px + 1, py + 1, px + W - 1, py + 28, (c.accent & 0x00FFFFFF) | 0x55000000, 0x00000000);
        ctx.fill(px + 1, py + 27, px + W - 1, py + 28, c.accent);

        // pestaña activa
        int tx = px + 16 + tab * 88;
        ctx.fill(tx, py + 50, tx + 84, py + 52, c.accent);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        Config c = Config.get();

        ctx.drawText(textRenderer, "DONUT STREAMER", px + 14, py + 10, c.accent, true);
        String hint = "Shift Der: panel · F8: modo stream";
        ctx.drawText(textRenderer, hint, px + W - 12 - textRenderer.getWidth(hint), py + 10, 0xFF6B7487, false);

        if (tab == 0) renderStreamTab(ctx, c);
        else renderAuctionTab(ctx, c);
    }

    private void renderStreamTab(DrawContext ctx, Config c) {
        ctx.drawText(textRenderer, "META DE LA SESIÓN", px + 16, py + 118, 0xFF8892A6, false);
        ctx.drawText(textRenderer, "COLOR DE ACENTO", px + 16, py + 160, 0xFF8892A6, false);
        for (int i = 0; i < SWATCHES.length; i++) {
            int sx = px + 16 + i * 28, sy = py + 172;
            ctx.fill(sx - 1, sy - 1, sx + 21, sy + 21, SWATCHES[i] == c.accent ? 0xFFFFFFFF : 0x33FFFFFF);
            ctx.fill(sx, sy, sx + 20, sy + 20, SWATCHES[i]);
        }
        ctx.drawText(textRenderer, "Palabras a censurar y regex de chat: config/donutstreamer.json",
                px + 16, py + H - 52, 0xFF6B7487, false);
    }

    private void renderAuctionTab(DrawContext ctx, Config c) {
        // calculadora de undercut
        Double p = Money.parse(priceField == null ? "" : priceField.getText());
        String calc = p == null ? "Escribe un precio para ver undercuts"
                : "-5%: " + Money.format(p * 0.95) + "   -10%: " + Money.format(p * 0.90)
                + "   -20%: " + Money.format(p * 0.80);
        ctx.drawText(textRenderer, calc, px + 16, py + 88, p == null ? 0xFF6B7487 : 0xFFFBBF24, false);

        // lista de movimientos
        ctx.fill(px + 16, py + 102, px + W - 16, py + 104, 0x22FFFFFF);
        List<Config.Entry> list = c.entries;
        int rows = 6;
        for (int i = 0; i < rows && i < list.size(); i++) {
            Config.Entry e = list.get(list.size() - 1 - i);
            int ry = py + 108 + i * 14;
            if (i % 2 == 0) ctx.fill(px + 16, ry - 2, px + W - 16, ry + 12, 0x14FFFFFF);
            ctx.drawText(textRenderer, e.sale ? "▲" : "▼", px + 22, ry, e.sale ? 0xFF4ADE80 : 0xFFF87171, false);
            ctx.drawText(textRenderer, textRenderer.trimToWidth(e.item, 190), px + 36, ry, 0xFFE5E9F0, false);
            String v = (e.sale ? "+" : "-") + Money.format(e.price);
            ctx.drawText(textRenderer, v, px + W - 22 - textRenderer.getWidth(v), ry,
                    e.sale ? 0xFF4ADE80 : 0xFFF87171, false);
        }
        if (list.isEmpty()) {
            ctx.drawText(textRenderer, "Sin movimientos todavía.", px + 22, py + 112, 0xFF6B7487, false);
        }

        // totales
        double session = c.profit(DonutStreamerClient.sessionStart);
        double all = c.profit(0);
        String t = "Sesión: " + (session >= 0 ? "+" : "") + Money.format(session)
                + "   Total: " + (all >= 0 ? "+" : "") + Money.format(all);
        ctx.drawText(textRenderer, t, px + 16, py + H - 48, all >= 0 ? 0xFF4ADE80 : 0xFFF87171, true);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (tab == 0 && button == 0) {
            for (int i = 0; i < SWATCHES.length; i++) {
                int sx = px + 16 + i * 28, sy = py + 172;
                if (mx >= sx && mx <= sx + 20 && my >= sy && my <= sy + 20) {
                    Config.get().accent = SWATCHES[i];
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void removed() {
        Config.get().save();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
