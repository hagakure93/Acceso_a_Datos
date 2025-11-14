package com.example.contratos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

public final class DatabaseLoader implements AutoCloseable {

    private static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS contratos";

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE contratos (
                id INT AUTO_INCREMENT PRIMARY KEY,
                nif VARCHAR(64),
                adjudicatario VARCHAR(255),
                objeto_generico VARCHAR(255),
                objeto TEXT,
                fecha_adjudicacion DATE,
                importe DECIMAL(15,2),
                proveedores_consultados VARCHAR(255),
                tipo_contrato VARCHAR(255)
            )
            """;

    private static final String INSERT_SQL = """
            INSERT INTO contratos (
                nif, adjudicatario, objeto_generico, objeto,
                fecha_adjudicacion, importe, proveedores_consultados, tipo_contrato
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final Connection connection;

    private DatabaseLoader(Connection connection) {
        this.connection = connection;
    }

    public static DatabaseLoader open(AppConfig config) throws SQLException {
        Connection connection = DriverManager.getConnection(
                config.jdbcUrl(),
                config.jdbcUser(),
                config.jdbcPassword()
        );
        connection.setAutoCommit(false);
        return new DatabaseLoader(connection);
    }

    public void initializeSchema() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(DROP_TABLE_SQL);
            statement.executeUpdate(CREATE_TABLE_SQL);
            connection.commit();
        }
    }

    public void insertContracts(List<ContractRecord> records) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL)) {
            for (ContractRecord record : records) {
                ps.setString(1, emptyToNull(record.nif()));
                ps.setString(2, emptyToNull(record.adjudicatario()));
                ps.setString(3, emptyToNull(record.objetoGenerico()));
                ps.setString(4, emptyToNull(record.objeto()));
                LocalDate fecha = record.fechaAdjudicacion();
                if (fecha != null) {
                    ps.setDate(5, java.sql.Date.valueOf(fecha));
                } else {
                    ps.setNull(5, java.sql.Types.DATE);
                }
                if (record.importe() != null) {
                    ps.setBigDecimal(6, record.importe());
                } else {
                    ps.setNull(6, java.sql.Types.DECIMAL);
                }
                ps.setString(7, emptyToNull(record.proveedoresConsultados()));
                ps.setString(8, emptyToNull(record.tipoContrato()));
                ps.addBatch();
            }
            ps.executeBatch();
            connection.commit();
        }
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}

