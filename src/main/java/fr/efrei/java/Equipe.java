package fr.efrei.java;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "equipe")
public class Equipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nom;

    // Relation un-à-plusieurs avec les collaborateurs
    @OneToMany(mappedBy = "equipe")
    private List<Collaborateur> membres = new ArrayList<>();

    protected Equipe() {
    }

    public Equipe(String nom) {
        setNomNormalise(nom);
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public List<Collaborateur> getMembres() {
        return membres;
    }

    public void renommer(String nouveauNom) {
        setNomNormalise(nouveauNom);
    }

    private void setNomNormalise(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'équipe ne peut pas être vide.");
        }
        this.nom = nom.trim();
    }

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