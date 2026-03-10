package com.demo.spring.batch.processor.correios;

import com.demo.spring.batch.domain.OcorrenciaEntrega;
import com.demo.spring.batch.reader.correios.CorreiosOcorrenciaDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
public class CorreiosOcorrenciaProcessor
        implements ItemProcessor<CorreiosOcorrenciaDTO, OcorrenciaEntrega> {

    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private static final Map<String, OcorrenciaEntrega.StatusEntrega> STATUS_MAP = Map.of(
            "BDE", OcorrenciaEntrega.StatusEntrega.ENTREGUE,
            "BDI", OcorrenciaEntrega.StatusEntrega.SAIU_PARA_ENTREGA,
            "OEC", OcorrenciaEntrega.StatusEntrega.SAIU_PARA_ENTREGA,
            "IDC", OcorrenciaEntrega.StatusEntrega.DEVOLVIDO,
            "RDE", OcorrenciaEntrega.StatusEntrega.DEVOLVIDO,
            "EXT", OcorrenciaEntrega.StatusEntrega.EXTRAVIO
    );

    @Override
    public OcorrenciaEntrega process(CorreiosOcorrenciaDTO record) {
        if (record.getCodigoRastreio() == null || record.getDataOcorrencia() == null) {
            log.warn("[CORREIOS] Registro inválido ignorado: {}", record.getCodigoRastreio());
            return null;
        }

        LocalTime hora = parseHora(record.getHoraOcorrencia());
        LocalDateTime dataHora = record.getDataOcorrencia().atTime(hora);
        OcorrenciaEntrega.StatusEntrega status = STATUS_MAP.getOrDefault(
                record.getCodigoOcorrencia().toUpperCase(),
                OcorrenciaEntrega.StatusEntrega.DESCONHECIDO);

        return OcorrenciaEntrega.builder()
                .codigoRastreio(record.getCodigoRastreio())
                .transportadora(OcorrenciaEntrega.Transportadora.CORREIOS)
                .status(status)
                .codigoOcorrenciaOriginal(record.getCodigoOcorrencia())
                .descricaoOcorrencia(record.getDescricaoOcorrencia())
                .dataHoraOcorrencia(dataHora)
                .cpfCnpjDestinatario(record.getCpfDestinatario())
                .nomeDestinatario(record.getNomeDestinatario())
                .cepDestino(record.getCepDestino())
                .cidadeDestino(record.getCidadeDestino())
                .ufDestino(record.getUfDestino())
                .valorMercadoria(record.getValorDeclarado())
                .nrTentativaEntrega(1)
                .build();
    }

    private LocalTime parseHora(String hora) {
        try {
            return hora != null ? LocalTime.parse(hora.substring(0, 5), HORA_FMT) : LocalTime.MIDNIGHT;
        } catch (Exception e) {
            return LocalTime.MIDNIGHT;
        }
    }
}