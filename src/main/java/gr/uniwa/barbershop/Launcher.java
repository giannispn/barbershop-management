package gr.uniwa.barbershop;

/**
 * Non-JavaFX main class. When the app is packaged as a shaded/fat jar, the JVM
 * needs an entry point that does NOT extend {@code javafx.application.Application},
 * otherwise it errors with "JavaFX runtime components are missing". This class
 * simply delegates to {@link App#main(String[])}.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
