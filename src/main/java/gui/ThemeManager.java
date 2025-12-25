package gui;

import javafx.scene.Scene;

public class ThemeManager {

    public enum Theme {
        LIGHT,
        DARK
    }

    private static Theme currentTheme = Theme.LIGHT;

    // ===== APPLY THEME =====
    public static void applyTheme(Scene scene, Theme theme) {
        scene.getStylesheets().clear();

        if (theme == Theme.DARK) {
            scene.getStylesheets().add(
                    ThemeManager.class.getResource("/dark.css").toExternalForm()
            );
        } else {
            scene.getStylesheets().add(
                    ThemeManager.class.getResource("/light.css").toExternalForm()
            );
        }

        currentTheme = theme;
    }

    // ===== TOGGLE BUTTON =====
    public static void toggle(Scene scene) {
        if (currentTheme == Theme.DARK) {
            applyTheme(scene, Theme.LIGHT);
        } else {
            applyTheme(scene, Theme.DARK);
        }
    }

    // ===== AUTO (SYSTEM THEME) =====
    public static Theme detectSystemTheme() {
        String os = System.getProperty("os.name").toLowerCase();

        // Enkel och stabil default:
        // Alla OS → LIGHT (kan byggas ut senare)
        return Theme.LIGHT;
    }
    public static Theme getCurrentTheme() {
        return currentTheme;
    }

}
