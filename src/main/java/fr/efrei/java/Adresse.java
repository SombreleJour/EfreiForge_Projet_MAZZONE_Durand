package fr.efrei.java;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Adresse postale d'un collaborateur (entité JPA).
 *
 * Une adresse n'a pas d'identifiant métier : sa clé primaire est technique,
 * fabriquée par la base (AUTO_INCREMENT grâce à GenerationType.IDENTITY).
 * Chaque adresse appartient à un seul collaborateur (relation 1-1).
 */
@Entity
@Table(name = "adresse")
public class Adresse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String rue;

    @Column(length = 10, nullable = false)
    private String codePostal;

    @Column(length = 50, nullable = false)
    private String ville;

    @Column(length = 50, nullable = false)
    private String pays;

    /** Constructeur sans argument exigé par JPA. */
    protected Adresse() {
    }

    public Adresse(String rue, String codePostal, String ville, String pays) {
        this.rue = rue;
        this.codePostal = codePostal;
        this.ville = ville;
        this.pays = pays;
    }

    public Long getId()           { return id; }
    public String getRue()        { return rue; }
    public String getCodePostal() { return codePostal; }
    public String getVille()      { return ville; }
    public String getPays()       { return pays; }

    public void setRue(String rue)               { this.rue = rue; }
    public void setCodePostal(String codePostal) { this.codePostal = codePostal; }
    public void setVille(String ville)           { this.ville = ville; }
    public void setPays(String pays)             { this.pays = pays; }

    /** Affichage multiligne (utilisé par afficherFiche() de Collaborateur). */
    public void afficher() {
        System.out.println(rue);
        System.out.println(codePostal + " " + ville);
        System.out.println(pays);
    }

    /** Représentation compacte sur une ligne. */
    @Override
    public String toString() {
        return rue + ", " + codePostal + " " + ville + " (" + pays + ")";
    }
}