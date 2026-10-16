package fr.efrei.java.exceptions;

/**
 * Règle métier violée : une équipe ne peut être supprimée que si elle ne contient plus
 * aucun collaborateur.
 */
public class EquipeNonVideException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EquipeNonVideException(String message) {
        super(message);
    }

    public EquipeNonVideException(String nomEquipe, long nombreMembres) {
        super("L'équipe « " + nomEquipe + " » contient encore " + nombreMembres
                + " collaborateur(s) : suppression refusée.");
    }
}