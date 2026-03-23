package com.policene.error_handler.handler.enums;

public enum CommonErrors {

    API_RELATORIOS("POST /relatorios", 10),
    FUNCIONARIOS("GET /funcionario", 10),
    LOGIN("POST /login", 50);

    private String message;
    private int limit;

    CommonErrors(String message, int limit) {
        this.message = message;
        this.limit = limit;
    }

    public static int findLimitByMessage (String message) {
        for (CommonErrors ce: CommonErrors.values()) {
            if (ce.message.equalsIgnoreCase(message)) {
                return ce.limit;
            }
        }
        // Valor padrão
        return 100;
    }

    public static boolean isCriticalError (String message, int count) {
        for (CommonErrors ce: CommonErrors.values()) {
            if (ce.message.equalsIgnoreCase(message)) {
                return count >= (ce.limit * 2);
            }
        }
        return count >= 200;
    }

}
