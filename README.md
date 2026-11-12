# Donut Streamer (Fabric, solo cliente)

Mod pensado para streamers y gente que vende en subastas de DonutSMP. Es **solo visual / de registro**: no automatiza acciones, no compra ni vende por ti y no modifica el juego.

## Funciones

- **Overlay HUD** con punto "EN VIVO", tiempo de sesión, ganancia, barra de meta, FPS y ping.
- **Modo Stream** (F8): oculta susurros/DMs y cualquier mensaje con palabras que tú definas.
- **Pestaña Subastas** (Shift Derecho): registro de ventas y compras, ganancia de sesión y total, y calculadora de *undercut* (-5 %, -10 %, -20 %).
- Acepta precios en formato `1.5m`, `250k`, `2b`, `$1,200`.
- **Registro automático** leyendo el chat con regex editables.
- 5 colores de acento.

## Compilar

Requisitos: JDK 21 y Gradle 8.12+ (o abrir la carpeta en IntelliJ).

```
gradle build
```

El `.jar` queda en `build/libs/donutstreamer-1.0.0.jar`. Cópialo a `.minecraft/mods` junto con **Fabric Loader 0.16+** y **Fabric API** para Minecraft 1.21.4.

> Si DonutSMP usa otra versión (por ejemplo 1.21.5+), cambia las versiones en `gradle.properties` (consulta https://fabricmc.net/develop) y ajusta cualquier método que haya cambiado de nombre.

## Configuración

`.minecraft/config/donutstreamer.json`:

- `censorWords`: lista de palabras a ocultar en modo stream (ej. tu IP, nombres).
- `saleRegex`, `buyRegex`: el grupo 1 debe capturar el precio. **Los valores por defecto son una suposición**: copia un mensaje real de venta de la subasta y adapta la regex.
- `whisperRegex`: formato de los mensajes privados.

## Nota

Revisa las reglas de mods del servidor antes de usarlo. Este mod no da ventajas de juego, pero la política depende del servidor.
