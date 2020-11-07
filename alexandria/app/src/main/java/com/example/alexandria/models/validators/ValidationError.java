package com.example.alexandria.models.validators;

public class ValidationError {
    private final String errorField;
    private final String errorMessage;

    /**
     * Construct a validationError instance
     * @param errorField The field caused the error
     * @param errorMessage Addition error message
     */
    ValidationError(String errorField, String errorMessage){
        this.errorField = errorField;
        this.errorMessage = errorMessage;
    }

    public String getField(){
        return this.errorField;
    }

    public String getMessage(){
        return this.errorMessage;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof ValidationError){
            ValidationError other = (ValidationError) obj;
            return other.errorField.equals(this.errorField) && other.errorMessage.equals(this.errorMessage);
        }
        return false;
    }

    @Override
    public String toString(){
        return "field:"+this.errorField+",message"+this.errorMessage+";";
    }
}
