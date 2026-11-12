package com.donutstreamer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Configuración + registro de subastas. Se guarda en config/donutstreamer.json */
public class Config {
    public boolean streamMode = false;
    public boolean hideWhispers = true;
    public boolean hudEnabled = true;
    public boolean autoTrack = true;
    public int accent = 0xFFFF8A00;
    public double goal = 0;

    /** Mensajes que contengan estas palabras se ocultan en modo stream. */
    public List<String> censorWords = new ArrayList<>();

    /**
     * Regex editables. El grupo 1 debe capturar el precio.
     * Ajústalos al formato real de los mensajes del servidor.
     */
    public String saleRegex = "(?i)you sold .*? for \\$?([0-9][0-9.,]*\\s?[kmb]?)";
    public String buyRegex = "(?i)you bought .*? for \\$?([0-9][0-9.,]*\\s?[kmb]?)";
    public String whisperRegex = "(?i)(.* -> me\\b.*|.*whispers to you.*|^\\[msg\\].*|.*\\bmessages you\\b.*)";

    public List<Entry> entries = new ArrayList<>();

    public static class Entry {
        public String item;
        public double price;
        public boolean sale;
        public long time;

        public Entry() {}

        public Entry(String item, double price, boolean sale) {
            this.item = item;
            this.price = price;
            this.sale = sale;
            this.time = System.currentTimeMillis();
        }
    }

    public double profit(long since) {
        double total = 0;
        for (Entry e : entries) {
            if (e.time < since) continue;
            total += e.sale ? e.price : -e.price;
        }
        return total;
    }

    public void addEntry(Entry e) {
        entries.add(e);
        while (entries.size() > 500) entries.remove(0);
        save();
    }

    // ---- persistencia ----
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Config INSTANCE;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("donutstreamer.json");
    }

    public static Config get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        Config loaded = null;
        try {
            Path p = path();
            if (Files.exists(p)) loaded = GSON.fromJson(Files.readString(p), Config.class);
        } catch (Exception ignored) {
        }
        INSTANCE = loaded != null ? loaded : new Config();
        if (INSTANCE.entries == null) INSTANCE.entries = new ArrayList<>();
        if (INSTANCE.censorWords == null) INSTANCE.censorWords = new ArrayList<>();
        INSTANCE.save();
    }

    public void save() {
        try {
            Files.writeString(path(), GSON.toJson(this));
        } catch (Exception ignored) {
        }
    }
}
