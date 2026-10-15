package fr.efrei.java;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "equipe")
public class Equipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nom;

    protected Equipe() {
    }

    public Equipe(String nom) {
        this.nom = nom;
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
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