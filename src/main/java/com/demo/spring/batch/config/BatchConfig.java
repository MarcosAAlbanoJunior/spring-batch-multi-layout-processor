package com.demo.spring.batch.config;

import com.demo.spring.batch.domain.OcorrenciaEntrega;
import com.demo.spring.batch.listener.TransportadoraSkipListener;
import com.demo.spring.batch.processor.correios.CorreiosOcorrenciaProcessor;

import com.demo.spring.batch.processor.jadlog.JadlogOcorrenciaProcessor;
import com.demo.spring.batch.processor.totalexpress.TotalExpressOcorrenciaProcessor;
import com.demo.spring.batch.reader.TransportadoraItemDTO;
import com.demo.spring.batch.reader.correios.CorreiosOcorrenciaDTO;
import com.demo.spring.batch.reader.jadlog.JadlogOcorrenciaDTO;
import com.demo.spring.batch.reader.totalexpress.TotalExpressOcorrenciaRecord;
import com.demo.spring.batch.writer.OcorrenciaEntregaWriter;
import lombok.RequiredArgsConstructor;
import org.beanio.InvalidRecordException;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final CorreiosOcorrenciaProcessor correiosProcessor;
    private final JadlogOcorrenciaProcessor jadlogProcessor;
    private final TotalExpressOcorrenciaProcessor totalExpressProcessor;
    private final OcorrenciaEntregaWriter writer;
    private final TransportadoraSkipListener skipListener;

    private final BatchProperties props;

    @Bean
    public Job processarOcorrenciasJob() {
        return new JobBuilder("processarOcorrenciasJob", jobRepository)
                .start(correiosStep())
                .next(jadlogStep())
                .next(totalExpressStep())
                .build();
    }

    @Bean
    public Step correiosStep() {
        return new StepBuilder("correiosStep", jobRepository)
                .<CorreiosOcorrenciaDTO, OcorrenciaEntrega>chunk(props.getChunkSize())
                .transactionManager(transactionManager)
                .reader(correiosReader())
                .processor(correiosProcessor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(props.getSkipLimit())
                .skip(InvalidRecordException.class)
                .skip(IllegalArgumentException.class)
                .listener(skipListener)
                .build();
    }

    @Bean
    public Step jadlogStep() {
        return new StepBuilder("jadlogStep", jobRepository)
                .<JadlogOcorrenciaDTO, OcorrenciaEntrega>chunk(props.getChunkSize())
                .transactionManager(transactionManager)
                .reader(jadlogReader())
                .processor(jadlogProcessor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(props.getSkipLimit())
                .skip(InvalidRecordException.class)
                .skip(IllegalArgumentException.class)
                .listener(skipListener)
                .build();
    }

    @Bean
    public Step totalExpressStep() {
        return new StepBuilder("totalExpressStep", jobRepository)
                .<TotalExpressOcorrenciaRecord, OcorrenciaEntrega>chunk(props.getChunkSize())
                .transactionManager(transactionManager)
                .reader(totalExpressReader())
                .processor(totalExpressProcessor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(props.getSkipLimit())
                .skip(InvalidRecordException.class)
                .skip(IllegalArgumentException.class)
                .listener(skipListener)
                .build();
    }

    @Bean
    public TransportadoraItemDTO<TotalExpressOcorrenciaRecord> totalExpressReader() {
        return new TransportadoraItemDTO<>(
                new FileSystemResource(props.getArquivo().getTotalExpress()),
                "totalExpressStream");
    }

    @Bean
    public TransportadoraItemDTO<CorreiosOcorrenciaDTO> correiosReader() {
        return new TransportadoraItemDTO<>(
                new FileSystemResource(props.getArquivo().getCorreios()), "correiosStream");
    }

    @Bean
    public TransportadoraItemDTO<JadlogOcorrenciaDTO> jadlogReader() {
        return new TransportadoraItemDTO<>(
                new FileSystemResource(props.getArquivo().getJadlog()), "jadlogStream");
    }
}