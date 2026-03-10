package com.demo.spring.batch.processor.totalexpress;

import com.demo.spring.batch.domain.OcorrenciaEntrega;
import com.demo.spring.batch.domain.OcorrenciaEntrega.StatusEntrega;
import com.demo.spring.batch.domain.OcorrenciaEntrega.Transportadora;
import com.demo.spring.batch.reader.totalexpress.TotalExpressOcorrenciaRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
public class TotalExpressOcorrenciaProcessor
        implements ItemProcessor<TotalExpressOcorrenciaRecord, OcorrenciaEntrega> {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private static final Map<String, StatusEntrega> STATUS_MAP = Map.of(
            "ENT", StatusEntrega.ENTREGUE,
            "SAI", StatusEntrega.SAIU_PARA_ENTREGA,
            "TRS", StatusEntrega.EM_TRANSITO,
            "AUS", StatusEntrega.TENTATIVA_FALHA,
            "REC", StatusEntrega.TENTATIVA_FALHA,
            "DEV", StatusEntrega.DEVOLVIDO,
            "EXT", StatusEntrega.EXTRAVIO
    );

    @Override
    public OcorrenciaEntrega process(TotalExpressOcorrenciaRecord record) {
        if (record.getNumeroEncomenda() == null || record.getDataOcorrencia() == null) {
            log.warn("[TOTAL_EXPRESS] Registro inválido ignorado: {}", record.getNumeroEncomenda());
            return null;
        }

        LocalDateTime dataHora = parseDataHora(record.getDataOcorrencia(), record.getHoraOcorrencia());
        StatusEntrega status = STATUS_MAP.getOrDefault(
                record.getCodigoStatus().trim().toUpperCase(),
                StatusEntrega.DESCONHECIDO);

        return OcorrenciaEntrega.builder()
                .codigoRastreio(record.getNumeroEncomenda().trim())
                .transportadora(Transportadora.TOTAL_EXPRESS)
                .status(status)
                .codigoOcorrenciaOriginal(record.getCodigoStatus().trim())
                .dataHoraOcorrencia(dataHora)
                .nomeDestinatario(record.getNomeDestinatario())
                .cepDestino(record.getCepDestino())
                .cidadeDestino(record.getCidadeDestino())
                .ufDestino(record.getUfDestino())
                .valorMercadoria(normalizeValor(record.getValorMercadoria()))
                .nrTentativaEntrega(record.getNumeroDeTentativa())
                .build();
    }

    private LocalDateTime parseDataHora(String data, String hora) {
        try {
            String horaFmt = hora != null && hora.length() == 4 ? hora : "0000";
            return LocalDateTime.parse(data + horaFmt, DT_FMT);
        } catch (Exception e) {
            log.warn("[TOTAL_EXPRESS] Data/hora inválida: {} {}", data, hora);
            return LocalDateTime.now();
        }
    }

    private BigDecimal normalizeValor(BigDecimal valor) {
        if (valor == null) return BigDecimal.ZERO;
        return valor.divide(BigDecimal.valueOf(100), 2, RoundingMode.UNNECESSARY);
    }
}