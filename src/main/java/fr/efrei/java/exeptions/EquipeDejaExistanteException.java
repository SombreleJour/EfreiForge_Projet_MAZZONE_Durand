package fr.efrei.java.exeptions;

public class EquipeDejaExistanteException extends RuntimeException {
    public EquipeDejaExistanteException(String message) {
        super(message);
    }
}