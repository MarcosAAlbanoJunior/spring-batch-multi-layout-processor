package com.demo.spring.batch.reader.correios;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorreiosOcorrenciaDTO {
    private String codigoRastreio;
    private String codigoOcorrencia;
    private String descricaoOcorrencia;
    private LocalDate dataOcorrencia;
    private String horaOcorrencia;
    private String cpfDestinatario;
    private String nomeDestinatario;
    private String cepDestino;
    private String cidadeDestino;
    private String ufDestino;
    private Integer pesoGramas;
    private BigDecimal valorDeclarado;
}