package fr.efrei.java;

/**
 * Règle métier violée : un collaborateur portant le même identifiant existe déjà.
 *
 * Exception non vérifiée (RuntimeException) : elle est levée par Annuaire.ajouter(...)
 * et interceptée par le code qui dialogue avec l'utilisateur (HelloEfrei).
 */
public class CollaborateurDejaExistantException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Identifiant fautif, conservé pour le message affiché à l'utilisateur. */
    private final String identifiant;

    public CollaborateurDejaExistantException(String identifiant) {
        super("Le collaborateur " + identifiant + " existe déjà dans l'annuaire.");
        this.identifiant = identifiant;
    }

    public String identifiant() {
        return identifiant;
    }
}