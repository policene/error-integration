package com.policene.error_handler.mock.controller;


import com.policene.error_handler.mock.dto.ErrorMock;
import com.policene.error_handler.mock.service.ErrorMockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/***
 * Classe criada para simular erros gerados por outro serviço.
 * Basicamente vai jogar no Kafka os eventos que recebe da requisição.
 ***/
@RestController
@RequestMapping("/errorMock")
public class ErrorMockController {

    private final ErrorMockService errorMockService;

    public ErrorMockController(ErrorMockService errorMockService) {
        this.errorMockService = errorMockService;
    }

    @PostMapping
    public ResponseEntity<String> generateError (@RequestBody ErrorMock error) {
        return new ResponseEntity<>(errorMockService.generateError(error), HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    public ResponseEntity<String> generateErrorBatch (@RequestParam("count") int count, @RequestBody ErrorMock error) {
        return new ResponseEntity<>(errorMockService.generateErrorBatch(count, error), HttpStatus.CREATED);
    }

}
