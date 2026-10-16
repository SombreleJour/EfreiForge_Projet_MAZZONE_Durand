package fr.efrei.java.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Programmeur : collaborateur spécialisé dans le développement logiciel.
 * Implémente Formateur car un programmeur peut animer des formations internes.
 *
 * Stocké dans la table collaborateur (SINGLE_TABLE) avec la valeur discriminante "PROGRAMMEUR".
 * La colonne langagePrefere reste vide (NULL) pour les lignes des autres types.
 */
@Entity
@DiscriminatorValue("PROGRAMMEUR")
public class Programmeur extends Collaborateur implements Formateur {

    @Column(length = 30)
    private String langagePrefere;

    /** Constructeur sans argument exigé par JPA (chaque entité de la hiérarchie doit en avoir un). */
    protected Programmeur() {
    }

    public Programmeur(
            String identifiant,
            String prenom,
            String nom,
            double salaire,
            Adresse adresse,
            String langagePrefere) {
        super(identifiant, prenom, nom, salaire, adresse);
        this.langagePrefere = langagePrefere;
    }

    public String getLangagePrefere() { return langagePrefere; }

    @Override
    public String getMetier() {
        return "Programmeur (" + langagePrefere + ")";
    }

    @Override
    public void travailler() {
        System.out.println(getPrenom() + " développe une fonctionnalité en " + langagePrefere + ".");
    }

    @Override
    public void former() {
        System.out.println(getPrenom() + " anime une formation interne.");
    }
}