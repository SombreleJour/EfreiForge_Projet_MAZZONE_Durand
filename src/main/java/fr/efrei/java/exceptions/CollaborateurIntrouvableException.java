package fr.efrei.java.exceptions;

/**
 * Règle métier violée : aucun collaborateur ne porte l'identifiant demandé.
 *
 * Levée par Annuaire.mettreAJourSalaire(...), qui ne doit pas valider
 * silencieusement une transaction vide.
 */
public class CollaborateurIntrouvableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Identifiant recherché sans succès. */
    private final String identifiant;

    public CollaborateurIntrouvableException(String identifiant) {
        super("Aucun collaborateur ne porte l'identifiant " + identifiant + ".");
        this.identifiant = identifiant;
    }

    public String identifiant() {
        return identifiant;
    }
}