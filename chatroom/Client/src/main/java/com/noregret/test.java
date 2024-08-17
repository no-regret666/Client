package com.noregret;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class test {
    public static void main(String[] args) {
        Exception exception = new Exception();
        log.error(exception.getLocalizedMessage());
        log.error(exception.getMessage());
        log.error(exception.getCause().getLocalizedMessage());
        log.error(exception.getCause().getMessage());
        log.error(exception.toString());
    }
}
