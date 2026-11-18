package com.todoapp.todorails.service;

public class DuplicateRegistrationException extends RuntimeException {

    private final String field;

    public DuplicateRegistrationException(String field) {
        super("Duplicate registration field");
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
