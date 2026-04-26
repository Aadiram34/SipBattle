import java.awt.Color;
 
/**
 * GameConstants.java
 * Shared colors, character names, colors, and emoji used across all screens.
 */
public class GameConstants {
 
    // ── UI Colors ──────────────────────────────────────────────────
    public static final Color BG_DARK      = new Color(15, 25, 50);
    public static final Color BLUE_VEND    = new Color(30, 70, 170);
    public static final Color GOLD         = new Color(255, 185, 0);
    public static final Color ORANGE_BTN   = new Color(230, 140, 0);
    public static final Color BLUE_BTN     = new Color(50, 100, 200);
    public static final Color BOARD_TAN    = new Color(205, 170, 110);
    public static final Color BOARD_LINE   = new Color(160, 120, 70);
    public static final Color BOARD_DARK   = new Color(140, 100, 55);
    public static final Color GREEN_HEADER = new Color(40, 90, 40);
    public static final Color WOOD_DARK    = new Color(90, 55, 25);
    public static final Color WOOD_MID     = new Color(120, 75, 35);
    public static final Color WOOD_LIGHT   = new Color(160, 105, 55);
    public static final Color P1_BLUE      = new Color(60, 130, 220);
    public static final Color P2_RED       = new Color(200, 50, 50);
 
    // ── Character Data ─────────────────────────────────────────────
    public static final String[] CHAR_NAMES = {
        "Blue Bull", "Moonbucks", "Latorade", "PhD Pepper", "Kelvin"
    };
 
    public static final Color[] CHAR_COLORS = {
        new Color(50, 110, 210),   // Blue Bull
        new Color(30, 130, 60),    // Moonbucks
        new Color(210, 100, 20),   // Latorade
        new Color(180, 30, 30),    // PhD Pepper
        new Color(100, 50, 180)    // Kelvin
    };
 
    public static final String[] CHAR_EMOJI = {
        "\uD83D\uDC02",  // 🐂 Blue Bull
        "\u2615",        // ☕ Moonbucks
        "\u26A1",        // ⚡ Latorade
        "\uD83C\uDF36",  // 🌶 PhD Pepper
        "\u2744"         // ❄ Kelvin
    };
 
    // ── Board Dimensions ──────────────────────────────────────────
    public static final int BOARD_ROWS  = 10;
    public static final int BOARD_COLS  = 8;
    public static final int CELL_SIZE   = 58;
    public static final int BOARD_X     = 210;
    public static final int BOARD_Y     = 155;
}
 