package rental.controller;

import java.util.Collection;

import static java.util.stream.Collectors.joining;

class ValidationException extends RuntimeException {
    ValidationException(Collection<String> messages) {
        super(messages.stream().collect(joining(" ")));
    }
}
