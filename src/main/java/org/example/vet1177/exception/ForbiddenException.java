package org.example.vet1177.exception;

public class ForbiddenException extends RuntimeException{
    //"Du saknar behörighet"
    public ForbiddenException(String message){
        super(message);
    }

}
