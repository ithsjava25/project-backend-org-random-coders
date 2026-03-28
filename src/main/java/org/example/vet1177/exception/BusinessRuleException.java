package org.example.vet1177.exception;

public class BusinessRuleException extends RuntimeException{
    //"Stängda ärenden kan inte uppdateras"
    public BusinessRuleException(String message){
        super(message);
    }

}
