package fr.efrei.java.model;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Équipe de collaborateurs (entité JPA).
 *
 * L'association est unidirectionnelle : c'est Collaborateur qui référence son équipe
 * (colonne equipe_id), Equipe ne contient aucune liste de collaborateurs.
 *
 * Le nom est obligatoire et unique sans tenir compte de la casse. Le caractère obligatoire
 * est vérifié ici ; l'unicité est vérifiée par GestionEquipes (et par la contrainte
 * UNIQUE du script SQL).
 */
@Entity
@Table(name = "equipe")
public class Equipe {

    /** Longueur maximale du nom, identique à la colonne nom de la table equipe. */
    public static final int LONGUEUR_MAX_NOM = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = LONGUEUR_MAX_NOM)
    private String nom;

    /** Constructeur sans argument exigé par JPA. */
    protected Equipe() {
    }

    /**
     * @throws IllegalArgumentException si le nom est vide ou trop long
     */
    public Equipe(String nom) {
        this.nom = normaliserNom(nom);
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    /**
     * @throws IllegalArgumentException si le nouveau nom est vide ou trop long
     */
    public void renommer(String nouveauNom) {
        this.nom = normaliserNom(nouveauNom);
    }

    /**
     * Vérifie un nom d'équipe et le renvoie sans les espaces autour.
     *
     * @throws IllegalArgumentException si le nom est vide (ou fait d'espaces) ou trop long
     */
    public static String normaliserNom(String nom) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom de l'équipe ne peut pas être vide.");
        }
        String nomNormalise = nom.trim();
        if (nomNormalise.length() > LONGUEUR_MAX_NOM) {
            throw new IllegalArgumentException("Le nom de l'équipe ne doit pas dépasser "
                    + LONGUEUR_MAX_NOM + " caractères.");
        }
        return nomNormalise;
    }

    /** Deux équipes sont égales si elles ont le même nom, sans tenir compte de la casse. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Equipe equipe)) return false;
        return Objects.equals(nom != null ? nom.toLowerCase() : null,
                equipe.nom != null ? equipe.nom.toLowerCase() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nom != null ? nom.toLowerCase() : null);
    }

    @Override
    public String toString() {
        return "Équipe : " + nom;
    }
}
