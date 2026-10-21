package fr.efrei.java.exceptions;

/**
 * Règle métier violée : une équipe porte déjà ce nom (sans tenir compte de la casse).
 * Levée à la création et au renommage d'une équipe.
 */
public class EquipeDejaExistanteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EquipeDejaExistanteException(String message) {
        super(message);
    }
}
