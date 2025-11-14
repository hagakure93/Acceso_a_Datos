package com.example.contratos;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public final class AppConfig {

    private final String jdbcUrl;
    private final String jdbcUser;
    private final String jdbcPassword;
    private final Path inputXml;
    private final Path outputXml;

    private AppConfig(String jdbcUrl, String jdbcUser, String jdbcPassword, Path inputXml, Path outputXml) {
        this.jdbcUrl = jdbcUrl;
        this.jdbcUser = jdbcUser;
        this.jdbcPassword = jdbcPassword;
        this.inputXml = inputXml;
        this.outputXml = outputXml;
    }

    public static AppConfig load(String resourceName) throws IOException {
        Objects.requireNonNull(resourceName, "resourceName");
        Properties properties = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IOException("No se encontró el fichero de configuración " + resourceName);
            }
            properties.load(in);
        }
        String jdbcUrl = require(properties, "db.url");
        String jdbcUser = require(properties, "db.user");
        String jdbcPassword = properties.getProperty("db.password", "");
        Path inputXml = Path.of(require(properties, "input.xml"));
        Path outputXml = Path.of(require(properties, "output.xml"));
        return new AppConfig(jdbcUrl, jdbcUser, jdbcPassword, inputXml, outputXml);
    }

    private static String require(Properties properties, String key) throws IOException {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IOException("Falta la propiedad obligatoria: " + key);
        }
        return value.trim();
    }

    public String jdbcUrl() {
        return jdbcUrl;
    }

    public String jdbcUser() {
        return jdbcUser;
    }

    public String jdbcPassword() {
        return jdbcPassword;
    }

    public Path inputXml() {
        return inputXml;
    }

    public Path outputXml() {
        return outputXml;
    }
}

