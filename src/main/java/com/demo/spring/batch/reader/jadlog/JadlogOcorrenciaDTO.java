package com.demo.spring.batch.reader.jadlog;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JadlogOcorrenciaDTO {
    private String numeroConhecimento;
    private LocalDate dataOcorrencia;
    private String horaOcorrencia;
    private String codigoOcorrencia;
    private String descricaoOcorrencia;
    private String cpfCnpjDestinatario;
    private String nomeDestinatario;
    private String cepDestino;
    private String cidadeDestino;
    private String ufDestino;
    private BigDecimal valorFrete;
    private BigDecimal valorMercadoria;
    private Integer numeroDeTentativa;
}