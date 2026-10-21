package fr.efrei.java.exceptions;

/**
 * Opération incohérente : aucune équipe ne porte le nom demandé.
 */
public class EquipeIntrouvableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EquipeIntrouvableException(String message) {
        super(message);
    }
}
