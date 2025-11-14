package com.example.contratos;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

public final class Application {

    private static final String CONFIG_FILE = "application.properties";

    private Application() {
    }

    public static void main(String[] args) {
        try {
            AppConfig config = AppConfig.load(CONFIG_FILE);
            List<ContractRecord> records;
            try (InputStream xmlStream = openXmlStream(config.inputXml())) {
                records = XmlContractParser.parse(xmlStream);
            }
            try (DatabaseLoader loader = DatabaseLoader.open(config)) {
                loader.initializeSchema();
                loader.insertContracts(records);
            }

            Path outputPath = config.outputXml();
            if (!outputPath.isAbsolute()) {
                outputPath = Path.of("").resolve(outputPath);
            }
            FilteredXmlWriter.write(records, outputPath);
            System.out.printf("Procesadas %d filas. XML filtrado en %s%n", records.size(), outputPath.toAbsolutePath());
        } catch (SQLException sqlException) {
            System.err.println("Error al conectar con la base de datos: " + sqlException.getMessage());
            sqlException.printStackTrace(System.err);
            System.exit(2);
        } catch (Exception exception) {
            System.err.println("Fallo al ejecutar la aplicación: " + exception.getMessage());
            exception.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static InputStream openXmlStream(Path resourcePath) throws Exception {
        if (resourcePath.isAbsolute()) {
            return Files.newInputStream(resourcePath);
        }
        Path fromWorkingDir = Path.of("").resolve(resourcePath);
        if (Files.exists(fromWorkingDir)) {
            return Files.newInputStream(fromWorkingDir);
        }
        var resourceUrl = Application.class.getClassLoader().getResource(resourcePath.toString());
        if (resourceUrl != null) {
            return Application.class.getClassLoader().getResourceAsStream(resourcePath.toString());
        }
        throw new IllegalStateException("No se encontró el fichero XML de entrada: " + resourcePath);
    }
}

