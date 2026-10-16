package fr.efrei.java.model;

import fr.efrei.java.exeptions.AffectationRefuseeException;
import jakarta.persistence.*;

import java.util.Objects;

/**
 * Collaborateur de l'entreprise (classe abstraite et entité JPA).
 *
 * Toute la hiérarchie (Programmeur, Testeur) est stockée dans UNE SEULE table :
 * la colonne discriminante "type_collaborateur" indique le type réel de chaque ligne.
 * Ce choix évite les jointures lors d'une recherche transversale (ex. : salaire > seuil).
 *
 * Les règles métier (salaire positif, augmentation strictement positive) sont protégées
 * ici, dans le domaine, et signalées par IllegalArgumentException.
 */
@Entity
@Table(name = "collaborateur")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type_collaborateur", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class Collaborateur {

    /** Identifiant métier (C001, C002...) utilisé comme clé primaire dans ce TP. */
    @Id
    @Column(length = 10)
    private String identifiant;

    @ManyToOne
    @JoinColumn(name = "equipe_id")
    private Equipe equipe;

    @Column(length = 50, nullable = false)
    private String prenom;

    @Column(length = 50, nullable = false)
    private String nom;

    @Column(nullable = false)
    private double salaire;

    /**
     * Relation 1-1 : un collaborateur a toujours une adresse, qui lui est propre.
     * La clé étrangère adresse_id se trouve dans la table collaborateur.
     * CascadeType.PERSIST : persister le collaborateur enregistre aussi sa nouvelle adresse.
     */
    @OneToOne(cascade = CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "adresse_id", nullable = false)
    private Adresse adresse;

    /** Constructeur sans argument exigé par JPA (jamais utilisé directement dans le code métier). */
    protected Collaborateur() {
    }

    /**
     * @throws IllegalArgumentException si le salaire est négatif ou si l'adresse est absente
     */
    protected Collaborateur(String identifiant, String prenom, String nom, double salaire, Adresse adresse) {
        if (salaire < 0) {
            throw new IllegalArgumentException("Le salaire ne peut pas être négatif.");
        }
        if (adresse == null) {
            throw new IllegalArgumentException("Un collaborateur doit avoir une adresse.");
        }
        this.identifiant = identifiant;
        this.prenom = prenom;
        this.nom = nom;
        this.salaire = salaire;
        this.adresse = adresse;
    }

    // --- Accesseurs ---
    // Pas de setter sur l'identifiant, le nom, le prénom et le salaire :
    // le salaire ne change que par la méthode métier augmenterSalaire(...).
    // JPA n'a pas besoin de setters (accès direct aux champs, car @Id est sur un champ).

    public String getIdentifiant() { return identifiant; }

    public String getPrenom() { return prenom; }

    public String getNom() { return nom; }

    public double getSalaire() { return salaire; }

    public Adresse getAdresse() { return adresse; }

    /** Permet à un collaborateur de déménager. */
    public void setAdresse(Adresse adresse) {
        if (adresse == null) {
            throw new IllegalArgumentException("Un collaborateur doit avoir une adresse.");
        }
        this.adresse = adresse;
    }

    // --- Gestion de l'équipe ---

    public Equipe getEquipe() {
        return equipe;
    }

    public void setEquipe(Equipe equipe) {
        this.equipe = equipe;
    }

    /**
     * Affecte le collaborateur à une équipe.
     * Refusé s'il a déjà une équipe, y compris la même : il n'y a pas de déplacement implicite.
     *
     * @throws IllegalArgumentException    si l'équipe est absente
     * @throws AffectationRefuseeException si le collaborateur a déjà une équipe
     */
    public void affecterA(Equipe nouvelleEquipe) {
        if (nouvelleEquipe == null) {
            throw new IllegalArgumentException("L'équipe ne peut pas être nulle.");
        }
        if (this.equipe != null) {
            throw new AffectationRefuseeException(identifiant + " appartient déjà à l'équipe « "
                    + this.equipe.getNom() + " » : affectation refusée.");
        }
        this.equipe = nouvelleEquipe;
    }

    /**
     * Retire le collaborateur de l'équipe indiquée.
     * Refusé s'il n'a aucune équipe, ou s'il appartient à une autre équipe que celle indiquée.
     *
     * @throws IllegalArgumentException    si l'équipe est absente
     * @throws AffectationRefuseeException si le collaborateur n'est pas dans cette équipe
     */
    public void retirerDe(Equipe uneEquipe) {
        if (uneEquipe == null) {
            throw new IllegalArgumentException("L'équipe ne peut pas être nulle.");
        }
        if (this.equipe == null) {
            throw new AffectationRefuseeException(identifiant
                    + " n'appartient à aucune équipe : retrait refusé.");
        }
        if (!this.equipe.equals(uneEquipe)) {
            throw new AffectationRefuseeException(identifiant + " appartient à l'équipe « "
                    + this.equipe.getNom() + " » et non à « " + uneEquipe.getNom() + " » : retrait refusé.");
        }
        this.equipe = null;
    }

    // --- Méthodes métier ---

    /**
     * Augmente le salaire du pourcentage donné.
     * Sur une entité gérée, aucun appel à persist/merge n'est nécessaire :
     * la modification est détectée et écrite en base au commit.
     *
     * @param pourcentage pourcentage d'augmentation, strictement positif (ex. : 10 pour +10 %)
     * @throws IllegalArgumentException si le pourcentage n'est pas strictement positif
     */
    public void augmenterSalaire(double pourcentage) {
        // La forme "!(x > 0)" refuse aussi NaN, ce que "x <= 0" laisserait passer.
        if (!(pourcentage > 0)) {
            throw new IllegalArgumentException("Le pourcentage d'augmentation doit être strictement positif.");
        }
        salaire = salaire * (1 + pourcentage / 100);
    }

    /** Libellé du métier, défini par chaque sous-classe. */
    public abstract String getMetier();

    /** Action de travail propre à chaque métier. */
    public abstract void travailler();

    /** Affiche la fiche complète du collaborateur (et son adresse) sur la console. */
    public void afficherFiche() {
        System.out.printf("[%s] %s %s - Salaire : %.2f €%n", identifiant, prenom, nom, salaire);
        System.out.println("Métier : " + getMetier());
        if (equipe != null) {
            System.out.println("Équipe : " + equipe.getNom());
        } else {
            System.out.println("Équipe : Aucune");
        }
        if (adresse != null) {
            System.out.println("Adresse :");
            adresse.afficher();
        }
    }

    /** Deux collaborateurs sont égaux s'ils ont le même identifiant (égalité métier). */
    @Override
    public boolean equals(Object autre) {
        if (this == autre) return true;
        if (!(autre instanceof Collaborateur collaborateur)) return false;
        return Objects.equals(identifiant, collaborateur.identifiant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifiant);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s %s - %.2f €", identifiant, prenom, nom, salaire);
    }
}