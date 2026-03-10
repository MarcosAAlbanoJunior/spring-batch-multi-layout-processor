package com.demo.spring.batch.writer;

import com.demo.spring.batch.domain.OcorrenciaEntrega;
import com.demo.spring.batch.repository.OcorrenciaEntregaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OcorrenciaEntregaWriter implements ItemWriter<OcorrenciaEntrega> {

    private final OcorrenciaEntregaRepository repository;

    @Override
    public void write(Chunk<? extends OcorrenciaEntrega> chunk) {
        log.info("[WRITER] Salvando chunk com {} registros", chunk.size());

        chunk.getItems().stream()
                .collect(Collectors.groupingBy(
                        o -> o.getTransportadora().name(),
                        Collectors.counting()))
                .forEach((transportadora, qtd) ->
                        log.info("[WRITER] {}: {} registros", transportadora, qtd));

        repository.saveAll(chunk.getItems());

        log.info("[WRITER] Chunk salvo com sucesso.");
    }
}