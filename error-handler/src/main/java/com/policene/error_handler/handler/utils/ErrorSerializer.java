package com.policene.error_handler.handler.utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.policene.error_handler.handler.dto.ErrorMessage;
import org.springframework.stereotype.Component;

@Component
public class ErrorSerializer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String convertToJson(ErrorMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter para JSON", e);
        }
    }

}
