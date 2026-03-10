package com.demo.spring.batch.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ocorrencia_entrega", indexes = {
    @Index(name = "idx_rastreio",       columnList = "codigo_rastreio"),
    @Index(name = "idx_transportadora", columnList = "transportadora"),
    @Index(name = "idx_status",         columnList = "status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OcorrenciaEntrega {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ocorrencia_seq")
    @SequenceGenerator(name = "ocorrencia_seq", sequenceName = "ocorrencia_seq", allocationSize = 500)
    private Long id;

    @Column(name = "codigo_rastreio", nullable = false, length = 30)
    private String codigoRastreio;

    @Enumerated(EnumType.STRING)
    @Column(name = "transportadora", nullable = false, length = 30)
    private Transportadora transportadora;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusEntrega status;

    @Column(name = "codigo_ocorrencia_original", length = 10)
    private String codigoOcorrenciaOriginal;

    @Column(name = "descricao_ocorrencia", length = 200)
    private String descricaoOcorrencia;

    @Column(name = "data_hora_ocorrencia", nullable = false)
    private LocalDateTime dataHoraOcorrencia;

    @Column(name = "cpf_cnpj_destinatario", length = 14)
    private String cpfCnpjDestinatario;

    @Column(name = "nome_destinatario", length = 100)
    private String nomeDestinatario;

    @Column(name = "cep_destino", length = 9)
    private String cepDestino;

    @Column(name = "cidade_destino", length = 60)
    private String cidadeDestino;

    @Column(name = "uf_destino", length = 2)
    private String ufDestino;

    @Column(name = "valor_mercadoria", precision = 12, scale = 2)
    private BigDecimal valorMercadoria;

    @Column(name = "nr_tentativa_entrega")
    private Integer nrTentativaEntrega;

    @Column(name = "processado_em", nullable = false)
    private LocalDateTime processadoEm;

    @PrePersist
    void prePersist() {
        this.processadoEm = LocalDateTime.now();
    }

    public enum Transportadora {
        CORREIOS, JADLOG, TOTAL_EXPRESS
    }

    public enum StatusEntrega {
        ENTREGUE, SAIU_PARA_ENTREGA, EM_ROTA,
        EM_TRANSITO, TENTATIVA_FALHA, DEVOLVIDO,
        EXTRAVIO, DESCONHECIDO
    }
}