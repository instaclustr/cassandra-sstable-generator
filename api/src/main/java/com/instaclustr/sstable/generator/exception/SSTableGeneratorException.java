package com.instaclustr.sstable.generator.exception;

public class SSTableGeneratorException extends RuntimeException {

    public SSTableGeneratorException(final String message) {
        super(message);
    }

    public SSTableGeneratorException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
