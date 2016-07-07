package com.github.jfgiraud.temmental;

public class StackException extends RuntimeException {

    public StackException(String message) {
        super(message);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StackException))
            return false;
        return getMessage().equals(((StackException) obj).getMessage());
    }

}
