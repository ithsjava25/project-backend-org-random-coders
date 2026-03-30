package org.example.vet1177.exception;

public class ResourceNotFoundException extends RuntimeException{
    //"MedicalRecord", id
    public ResourceNotFoundException(String resource, Object id){
        super(resource + " not found with id: " + id);
    }

}
