package org.fmazmz.springbootai.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class DotenvLoader {
    private DotenvLoader() {
    }

    public static void loadFromProjectRoot() {
        Path dotenvPath = Path.of(".env").toAbsolutePath().normalize();
        if (!Files.exists(dotenvPath)) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(dotenvPath);
            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int equalsIndex = line.indexOf('=');
                if (equalsIndex <= 0) {
                    continue;
                }

                String key = line.substring(0, equalsIndex).trim();
                String value = line.substring(equalsIndex + 1).trim();
                if (key.isEmpty()) {
                    continue;
                }

                String normalizedValue = unquote(value);
                boolean missingFromSystemProperties = System.getProperty(key) == null;
                boolean missingFromEnvironment = System.getenv(key) == null;
                if (missingFromSystemProperties && missingFromEnvironment) {
                    System.setProperty(key, normalizedValue);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read .env file from project root", e);
        }
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            boolean doubleQuoted = value.startsWith("\"") && value.endsWith("\"");
            boolean singleQuoted = value.startsWith("'") && value.endsWith("'");
            if (doubleQuoted || singleQuoted) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
