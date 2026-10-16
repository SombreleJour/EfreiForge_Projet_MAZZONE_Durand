package fr.efrei.java.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Testeur : collaborateur spécialisé dans les tests logiciels.
 *
 * Stocké dans la table collaborateur (SINGLE_TABLE) avec la valeur discriminante "TESTEUR".
 */
@Entity
@DiscriminatorValue("TESTEUR")
public class Testeur extends Collaborateur {

    /** Constructeur sans argument exigé par JPA (chaque entité de la hiérarchie doit en avoir un). */
    protected Testeur() {
    }

    public Testeur(
            String identifiant,
            String prenom,
            String nom,
            double salaire,
            Adresse adresse) {
        super(identifiant, prenom, nom, salaire, adresse);
    }

    @Override
    public String getMetier() {
        return "Testeur";
    }

    @Override
    public void travailler() {
        System.out.println(getPrenom() + " exécute une campagne de tests.");
    }
}