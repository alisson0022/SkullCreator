package me.alissonlopes.skullcreator;

/**
 * Custom SkullCreator exceptions
 */
public class SkullCreatorException extends RuntimeException {
    public SkullCreatorException(String message) {
        super(message);
    }
    public SkullCreatorException(String message, Throwable throwable) {
        super(message, throwable);
    }
}