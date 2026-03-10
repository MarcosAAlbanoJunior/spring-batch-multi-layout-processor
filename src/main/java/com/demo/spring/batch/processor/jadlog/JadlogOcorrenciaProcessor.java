package com.demo.spring.batch.processor.jadlog;

import com.demo.spring.batch.domain.OcorrenciaEntrega;
import com.demo.spring.batch.domain.OcorrenciaEntrega.StatusEntrega;
import com.demo.spring.batch.reader.jadlog.JadlogOcorrenciaDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
public class JadlogOcorrenciaProcessor
        implements ItemProcessor<JadlogOcorrenciaDTO, OcorrenciaEntrega> {

    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final Map<String, StatusEntrega> STATUS_MAP = Map.of(
            "D", StatusEntrega.ENTREGUE,
            "S", StatusEntrega.SAIU_PARA_ENTREGA,
            "V", StatusEntrega.TENTATIVA_FALHA,
            "T", StatusEntrega.EM_TRANSITO,
            "E", StatusEntrega.EXTRAVIO,
            "R", StatusEntrega.DEVOLVIDO
    );

    @Override
    public OcorrenciaEntrega process(JadlogOcorrenciaDTO record) {
        if (record.getNumeroConhecimento() == null || record.getDataOcorrencia() == null) {
            log.warn("[JADLOG] Registro inválido ignorado: {}", record.getNumeroConhecimento());
            return null;
        }

        LocalTime hora = parseHora(record.getHoraOcorrencia());
        LocalDateTime dataHora = record.getDataOcorrencia().atTime(hora);
        StatusEntrega status = STATUS_MAP.getOrDefault(
                record.getCodigoOcorrencia().toUpperCase(), StatusEntrega.DESCONHECIDO);

        return OcorrenciaEntrega.builder()
                .codigoRastreio(record.getNumeroConhecimento())
                .transportadora(OcorrenciaEntrega.Transportadora.JADLOG)
                .status(status)
                .codigoOcorrenciaOriginal(record.getCodigoOcorrencia())
                .descricaoOcorrencia(record.getDescricaoOcorrencia())
                .dataHoraOcorrencia(dataHora)
                .cpfCnpjDestinatario(record.getCpfCnpjDestinatario())
                .nomeDestinatario(record.getNomeDestinatario())
                .cepDestino(record.getCepDestino())
                .cidadeDestino(record.getCidadeDestino())
                .ufDestino(record.getUfDestino())
                .valorMercadoria(record.getValorMercadoria())
                .nrTentativaEntrega(record.getNumeroDeTentativa())
                .build();
    }

    private LocalTime parseHora(String hora) {
        try {
            return hora != null ? LocalTime.parse(hora, HORA_FMT) : LocalTime.MIDNIGHT;
        } catch (Exception e) {
            return LocalTime.MIDNIGHT;
        }
    }
}