package com.demo.spring.batch.listener;

import com.demo.spring.batch.domain.OcorrenciaEntrega;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransportadoraSkipListener implements SkipListener<Object, OcorrenciaEntrega> {

    @Override
    public void onSkipInRead(Throwable t) {
        log.error("[SKIP][READ] {}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(@NonNull Object item, Throwable t) {
        log.error("[SKIP][PROCESS] item={} erro={}", item, t.getMessage());
    }

    @Override
    public void onSkipInWrite(OcorrenciaEntrega item, Throwable t) {
        log.error("[SKIP][WRITE] rastreio={} erro={}", item.getCodigoRastreio(), t.getMessage());
    }
}