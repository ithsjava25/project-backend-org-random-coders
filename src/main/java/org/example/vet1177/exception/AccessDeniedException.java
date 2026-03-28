package org.example.vet1177.exception;

public class AccessDeniedException extends RuntimeException{
    //"Du saknar behörighet"
    public AccessDeniedException(String message){
        super(message);
    }

}
