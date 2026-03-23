package com.policene.error_handler.mock.service;

import com.policene.error_handler.mock.dto.ErrorMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class ErrorMockService {

    private static final Logger log = LoggerFactory.getLogger(ErrorMockService.class);
    private final KafkaTemplate<String, ErrorMock> kafkaTemplate;

    public ErrorMockService(KafkaTemplate<String, ErrorMock> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String generateError(ErrorMock error) {
        try {
            kafkaTemplate.send("errors", error);
            log.info("ERRO gerado: " + error);
            return "Erro gerado com sucesso.";
        } catch (Exception e) {
            return "Não foi possível gerar o erro.";
        }
    }

    public String generateErrorBatch(int count, ErrorMock error) {
        try {
            for (int i = 1; i <= count; i++) {
                kafkaTemplate.send("errors", error);
                log.info("ERRO gerado: " + error);
            }
            return "Erros em lote gerados com sucesso.";
        } catch (Exception e) {
            return "Não foi possível gerar o erro.";
        }
    }

}
