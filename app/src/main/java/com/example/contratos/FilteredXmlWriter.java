package com.example.contratos;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class FilteredXmlWriter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private FilteredXmlWriter() {
    }

    public static void write(List<ContractRecord> records, Path outputPath) throws Exception {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        try (OutputStream out = Files.newOutputStream(outputPath)) {
            XMLStreamWriter writer = factory.createXMLStreamWriter(out, "UTF-8");
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeStartElement("contratos");
            for (ContractRecord record : records) {
                writer.writeStartElement("contrato");
                writeElement(writer, "nif", record.nif());
                writeElement(writer, "adjudicatario", record.adjudicatario());
                writeElement(writer, "objetoGenerico", record.objetoGenerico());
                writeElement(writer, "objeto", record.objeto());
                if (record.fechaAdjudicacion() != null) {
                    writeElement(writer, "fechaAdjudicacion", DATE_FORMATTER.format(record.fechaAdjudicacion()));
                } else {
                    writeElement(writer, "fechaAdjudicacion", null);
                }
                if (record.importe() != null) {
                    writeElement(writer, "importe", record.importe().toPlainString());
                } else {
                    writeElement(writer, "importe", null);
                }
                writeElement(writer, "proveedoresConsultados", record.proveedoresConsultados());
                writer.writeEndElement(); // contrato
            }
            writer.writeEndElement(); // contratos
            writer.writeEndDocument();
            writer.flush();
            writer.close();
        }
    }

    private static void writeElement(XMLStreamWriter writer, String name, String value) throws Exception {
        writer.writeStartElement(name);
        if (value != null && !value.isBlank()) {
            writer.writeCharacters(value);
        }
        writer.writeEndElement();
    }
}

