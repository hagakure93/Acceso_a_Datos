package com.example.contratos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContractRecord(
        String nif,
        String adjudicatario,
        String objetoGenerico,
        String objeto,
        LocalDate fechaAdjudicacion,
        BigDecimal importe,
        String proveedoresConsultados,
        String tipoContrato
) {
}

