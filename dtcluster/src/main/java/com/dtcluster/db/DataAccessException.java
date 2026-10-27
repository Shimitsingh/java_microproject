package com.dtcluster.db;

public class DataAccessException extends RuntimeException {
    public DataAccessException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
