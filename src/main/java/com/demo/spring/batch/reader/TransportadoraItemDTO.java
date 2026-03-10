package com.demo.spring.batch.reader;

import lombok.extern.slf4j.Slf4j;
import org.beanio.BeanReader;
import org.beanio.StreamFactory;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamException;
import org.springframework.batch.infrastructure.item.ItemStreamReader;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
public class TransportadoraItemDTO<T> implements ItemStreamReader<T> {

    private static final String POSITION_KEY = "beanio.current.position";

    private final Resource resource;
    private final String streamName;
    private BeanReader beanReader;
    private long currentPosition = 0;

    public TransportadoraItemDTO(Resource resource, String streamName) {
        this.resource = resource;
        this.streamName = streamName;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        long savedPosition = executionContext.getLong(POSITION_KEY, 0L);
        try {
            StreamFactory factory = StreamFactory.newInstance();
            factory.load(getClass().getResourceAsStream("/mapping/transportadoras-mapping.xml"));

            beanReader = factory.createReader(
                    streamName,
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
            );

            if (savedPosition > 0) {
                log.info("[{}] Restart: pulando {} registros", streamName, savedPosition);
                for (long i = 0; i < savedPosition; i++) beanReader.read();
            }
            currentPosition = savedPosition;

        } catch (Exception e) {
            throw new ItemStreamException("Erro ao abrir reader: " + streamName, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T read() throws Exception {
        Object record = beanReader.read();
        if (record == null) {
            log.info("[{}] Leitura concluída. Total: {}", streamName, currentPosition);
            return null;
        }
        currentPosition++;
        return (T) record;
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putLong(POSITION_KEY, currentPosition);
    }

    @Override
    public void close() throws ItemStreamException {
        if (beanReader != null) beanReader.close();
    }
}