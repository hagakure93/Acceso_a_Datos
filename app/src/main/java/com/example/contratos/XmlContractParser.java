package com.example.contratos;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class XmlContractParser {

    private static final DateTimeFormatter[] DATE_FORMATTERS = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ROOT)
    };

    private XmlContractParser() {
    }

    public static List<ContractRecord> parse(Path xmlPath) throws Exception {
        try (InputStream in = Files.newInputStream(xmlPath)) {
            return parse(in);
        }
    }

    public static List<ContractRecord> parse(InputStream inputStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(inputStream);
        NodeList rowNodes = document.getElementsByTagNameNS("*", "Row");
        if (rowNodes.getLength() == 0) {
            return List.of();
        }

        List<String> headers = extractRowValues(rowNodes.item(0));
        List<ContractRecord> records = new ArrayList<>();
        for (int i = 1; i < rowNodes.getLength(); i++) {
            Node rowNode = rowNodes.item(i);
            List<String> values = extractRowValues(rowNode);
            if (values.isEmpty()) {
                continue;
            }
            records.add(mapToContract(headers, values));
        }
        return records;
    }

    private static List<String> extractRowValues(Node rowNode) {
        if (!(rowNode instanceof Element rowElement)) {
            return List.of();
        }
        NodeList cellNodes = rowElement.getElementsByTagNameNS("*", "Cell");
        List<String> values = new ArrayList<>();
        for (int i = 0; i < cellNodes.getLength(); i++) {
            Node cellNode = cellNodes.item(i);
            if (cellNode instanceof Element cellElement) {
                Optional<String> value = extractCellValue(cellElement);
                values.add(value.orElse(""));
            }
        }
        return values;
    }

    private static Optional<String> extractCellValue(Element cellElement) {
        NodeList dataNodes = cellElement.getElementsByTagNameNS("*", "Data");
        if (dataNodes.getLength() == 0) {
            return Optional.empty();
        }
        Node dataNode = dataNodes.item(0);
        return Optional.ofNullable(dataNode.getTextContent()).map(String::trim);
    }

    private static ContractRecord mapToContract(List<String> headers, List<String> values) {
        String nif = valueFor(headers, values, "NIF");
        String adjudicatario = valueFor(headers, values, "ADJUDICATARIO");
        String objetoGenerico = valueFor(headers, values, "OBJETO GENÉRICO");
        String objeto = valueFor(headers, values, "OBJETO");
        String fechaTexto = valueFor(headers, values, "FECHA DE ADJUDICACIÓN");
        String importeTexto = valueFor(headers, values, "IMPORTE");
        String proveedores = valueFor(headers, values, "PROVEEDORES CONSULTADOS");
        String tipoContrato = valueFor(headers, values, "TIPO DE CONTRATO");

        LocalDate fecha = parseDate(fechaTexto);
        BigDecimal importe = parseImporte(importeTexto);

        return new ContractRecord(
                nif,
                adjudicatario,
                objetoGenerico,
                objeto,
                fecha,
                importe,
                proveedores,
                tipoContrato
        );
    }

    private static String valueFor(List<String> headers, List<String> values, String headerName) {
        int index = headers.indexOf(headerName);
        if (index >= 0 && index < values.size()) {
            return values.get(index);
        }
        return "";
    }

    private static LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                if (formatter == DateTimeFormatter.ISO_LOCAL_DATE) {
                    return LocalDate.parse(text, formatter);
                }
                LocalDateTime dateTime = LocalDateTime.parse(text, formatter);
                return dateTime.toLocalDate();
            } catch (DateTimeParseException ignored) {
                // Intentamos con el siguiente formato
            }
        }
        return null;
    }

    private static BigDecimal parseImporte(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String cleaned = text.replace("€", "")
                .replace("EUR", "")
                .replace(".", "")
                .replace(",", ".")
                .replace(" ", "")
                .trim();
        if (cleaned.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

