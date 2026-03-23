package com.policene.error_handler.handler.service;

import com.policene.error_handler.config.MessagePublisher;
import com.policene.error_handler.handler.dto.ErrorMessage;
import com.policene.error_handler.handler.utils.ErrorSerializer;
import com.policene.error_handler.mock.dto.ErrorMock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.policene.error_handler.handler.enums.CommonErrors.findLimitByMessage;
import static com.policene.error_handler.handler.enums.CommonErrors.isCriticalError;

@Component
public class ErrorHandlerService {

    private static final Logger log = LogManager.getLogger(ErrorHandlerService.class);
    private final MessagePublisher publisher;
    private final RedisTemplate<String, Integer> template;
    private final ErrorSerializer errorSerializer;

    public ErrorHandlerService(MessagePublisher publisher, RedisTemplate<String, Integer> template, ErrorSerializer errorSerializer) {
        this.publisher = publisher;
        this.template = template;
        this.errorSerializer = errorSerializer;
    }

    /***
     * Função que executa a cada 15min e busca todos
     * os erros que passaram do limite proposto.
     ***/
    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void checkErrors () {
        try {
            log.info("Iniciando processamento em lote");
            Set<String> errors = template.keys("*");
            log.info(errors.size());
            for (String error : errors) {

                Map<Object, Object> data = template.opsForHash().entries(error);
                int count = Integer.parseInt((String) data.get("count"));
                String cause = (String) data.get("cause");
                if (count >= findLimitByMessage(error)) {
                    boolean isCritical = isCriticalError(error, count);
                    ErrorMessage errorToSend = new ErrorMessage(
                            error,
                            cause,
                            count,
                            isCritical
                    );
                    sendMessage(errorToSend);
                }
            }
        } catch (Exception e) {
            log.error(e.getCause());
        }
    }

    @KafkaListener(topics = "errors", groupId = "errors")
    public void errorHandling (ErrorMock mock) {
        String errorMessage = mock.message();
        try {
            log.info("Iniciando processamento do erro: {}", mock.message());
            if (errorAlreadyFound(errorMessage)) {
                updateOcurrenceCount(errorMessage);
            } else {
                insertError(mock);
            }
        } catch (Exception e) {
            log.error("Erro ao processar: {}", errorMessage);
            System.out.println(e.getCause());
        }
    }

    public boolean errorAlreadyFound (String error) {
        return template.hasKey(error);
    }

    /***
     * Somente aumenta em 1 o valor.
     */
    public void updateOcurrenceCount (String errorMessage) {
        Long newCount = template.opsForHash().increment(errorMessage, "count", 1);
        log.info("Erro '{}' atualizado para nova contagem: {}", errorMessage, newCount);
    }

    public void insertError (ErrorMock error) {
        template.opsForHash().put(error.message(), "count", "1");
        template.opsForHash().put(error.message(), "cause", error.cause());
        template.expire(error.message(), 20, TimeUnit.MINUTES);
        log.info("Erro adicionado: {}", error);
    }

    public void sendMessage (ErrorMessage message) {
        String json = errorSerializer.convertToJson(message);
        publisher.publish(json);
        log.info("Mensagem publicada para o erro: {}", json);
    }

}
