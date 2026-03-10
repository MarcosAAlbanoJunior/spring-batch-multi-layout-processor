package com.demo.spring.batch.reader.totalexpress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotalExpressOcorrenciaRecord {
    private String numeroEncomenda;
    private String nomeDestinatario;
    private String cepDestino;
    private String cidadeDestino;
    private String ufDestino;
    private String dataOcorrencia;
    private String horaOcorrencia;
    private String codigoStatus;
    private Integer pesoGramas;
    private BigDecimal valorMercadoria;
    private Integer numeroDeTentativa;
    private String documentoDestinatario;
}