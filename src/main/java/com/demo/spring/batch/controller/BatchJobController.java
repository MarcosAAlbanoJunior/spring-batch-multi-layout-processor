package com.demo.spring.batch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/batch")
@RequiredArgsConstructor
public class BatchJobController {

    private final JobOperator jobOperator;
    private final Job processarOcorrenciasJob;

    @PostMapping("/iniciar")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> iniciar() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLocalDateTime("iniciado_em", LocalDateTime.now())
                    .toJobParameters();

            JobExecution execution = jobOperator.start(processarOcorrenciasJob, params);

            Map<String, Object> steps = new LinkedHashMap<>();
            for (StepExecution step : execution.getStepExecutions()) {
                steps.put(step.getStepName(), Map.of(
                        "status",        step.getStatus(),
                        "lidos",         step.getReadCount(),
                        "processados",   step.getWriteCount(),
                        "ignorados",     step.getSkipCount(),
                        "erros",         step.getFailureExceptions()
                                .stream()
                                .map(Throwable::getMessage)
                                .toList()
                ));
            }

            return Map.of(
                    "executionId",  execution.getId(),
                    "status",       execution.getStatus(),
                    "inicio",       execution.getStartTime() != null ? execution.getStartTime() : "",
                    "fim",          execution.getEndTime() != null ? execution.getEndTime() : "",
                    "steps",        steps
            );

        } catch (Exception e) {
            return Map.of("erro", e.getMessage());
        }
    }
}