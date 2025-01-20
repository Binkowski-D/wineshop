package com.wineshop.exception;

public class WineNotFoundException extends RuntimeException{
    public WineNotFoundException(String message){
        super(message);
    }
}
