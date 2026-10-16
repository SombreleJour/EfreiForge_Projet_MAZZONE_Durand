package fr.efrei.java.exceptions;

/**
 * Règle métier violée lors d'une affectation ou d'un retrait :
 * collaborateur déjà dans une équipe, ou collaborateur qui n'appartient pas à l'équipe indiquée.
 * Le message précise la raison du refus.
 */
public class AffectationRefuseeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AffectationRefuseeException(String message) {
        super(message);
    }
}