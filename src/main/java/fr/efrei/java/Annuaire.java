package fr.efrei.java;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Annuaire des collaborateurs : accès à la base via JPA.
 *
 * La base est la source de vérité (la Map du TP3 a disparu).
 * Cette classe ne parle jamais à l'utilisateur : elle signale les problèmes par des
 * exceptions et trace les événements techniques avec SLF4J.
 *
 * Principes (chapitre 7) :
 *  - l'EntityManagerFactory est créée UNE fois (par HelloEfrei) et reçue ici ;
 *  - un EntityManager est créé PAR OPÉRATION, puis fermé ;
 *  - les écritures sont entourées d'une transaction (begin / commit / rollback).
 */
public class Annuaire extends AccesJpa {

    private static final Logger logger = LoggerFactory.getLogger(Annuaire.class);



    /**
     * @param fabrique fabrique JPA, créée et fermée par l'appelant
     */
    public Annuaire(EntityManagerFactory fabrique) {
        super(fabrique);
    }
    // =========================================================================
    // Écriture
    // =========================================================================

    /**
     * Ajoute un collaborateur en base.
     *
     * @throws CollaborateurDejaExistantException si l'identifiant est déjà utilisé
     */
    public void ajouter(Collaborateur collaborateur) {
        String identifiant = collaborateur.getIdentifiant();
        try {
            executer(em -> {
                // La règle du doublon est vérifiée ici : la clé primaire protège la base,
                // mais seule cette vérification donne une erreur compréhensible pour l'utilisateur.
                if (em.find(Collaborateur.class, identifiant) != null) {
                    throw new CollaborateurDejaExistantException(identifiant);
                }
                em.persist(collaborateur);
            });
            logger.info("Collaborateur {} ajouté", identifiant);
        } catch (CollaborateurDejaExistantException e) {
            logger.warn("Doublon refusé pour {}", identifiant);
            throw e;
        } catch (PersistenceException e) {
            // Accès concurrent : un autre utilisateur a pu insérer cet identifiant entre
            // notre vérification et notre commit. La base refuse alors l'insertion.
            if (trouver(identifiant) != null) {
                logger.warn("Doublon refusé pour {} (ajout simultané)", identifiant);
                throw new CollaborateurDejaExistantException(identifiant);
            }
            logger.error("Échec de l'ajout du collaborateur {}", identifiant, e);
            throw e;
        }
    }

    /**
     * Augmente le salaire d'un collaborateur et l'enregistre en base.
     * Aucun persist/merge : le collaborateur chargé dans la transaction est une entité gérée,
     * sa modification est synchronisée au commit.
     *
     * @return le nouveau salaire
     * @throws CollaborateurIntrouvableException si l'identifiant n'existe pas
     * @throws IllegalArgumentException          si le pourcentage n'est pas strictement positif
     */
    public double mettreAJourSalaire(String identifiant, double pourcentage) {
        Double nouveauSalaire = ecrire(em -> {
            Collaborateur collaborateur = em.find(Collaborateur.class, identifiant);
            if (collaborateur == null) {
                throw new CollaborateurIntrouvableException(identifiant);
            }
            double ancienSalaire = collaborateur.getSalaire();
            collaborateur.augmenterSalaire(pourcentage);
            logger.debug("Salaire de {} : {} -> {}", identifiant, ancienSalaire, collaborateur.getSalaire());
            return collaborateur.getSalaire();
        });
        logger.info("Salaire de {} augmenté de {} %", identifiant, pourcentage);
        return nouveauSalaire;
    }

    // =========================================================================
    // Lecture
    // =========================================================================

    /** Nombre de collaborateurs en base. */
    public long nombre() {
        return lire(em -> em.createQuery("select count(c) from Collaborateur c", Long.class)
                .getSingleResult());
    }

    /** Retrouve un collaborateur par son identifiant, ou null s'il n'existe pas. */
    public Collaborateur trouver(String identifiant) {
        return lire(em -> em.find(Collaborateur.class, identifiant));
    }

    /** Tous les collaborateurs, par identifiant. */
    public List<Collaborateur> tous() {
        return lire(em -> em.createQuery(
                        "select c from Collaborateur c order by c.identifiant", Collaborateur.class)
                .getResultList());
    }

    /** Collaborateurs dont le nom contient le fragment (sans tenir compte de la casse). */
    public List<Collaborateur> nomContenant(String fragment) {
        return lire(em -> em.createQuery(
                        "select c from Collaborateur c where lower(c.nom) like :motif order by c.nom",
                        Collaborateur.class)
                .setParameter("motif", "%" + fragment.toLowerCase() + "%")
                .getResultList());
    }

    /**
     * Collaborateurs dont le salaire est supérieur ou égal au minimum donné.
     * Le filtrage est fait par la base (JPQL), pas après chargement en Java.
     *
     * @throws IllegalArgumentException si le minimum est négatif
     */
    public List<Collaborateur> salaireAuMoins(double minimum) {
        // La forme "!(x >= 0)" refuse aussi NaN.
        if (!(minimum >= 0)) {
            throw new IllegalArgumentException("Le salaire minimum ne peut pas être négatif.");
        }
        return lire(em -> em.createQuery(
                        "select c from Collaborateur c where c.salaire >= :minimum order by c.salaire",
                        Collaborateur.class)
                .setParameter("minimum", minimum)
                .getResultList());
    }

    /**
     * Uniquement les programmeurs. La requête porte sur l'entité Programmeur :
     * la base filtre grâce à la colonne discriminante.
     */
    public List<Programmeur> programmeurs() {
        return lire(em -> em.createQuery(
                        "select p from Programmeur p order by p.nom", Programmeur.class)
                .getResultList());
    }

    /** Collaborateurs triés par nom. */
    public List<Collaborateur> triesParNom() {
        return lire(em -> em.createQuery(
                        "select c from Collaborateur c order by c.nom asc", Collaborateur.class)
                .getResultList());
    }

    /** Collaborateurs triés par salaire décroissant. */
    public List<Collaborateur> triesParSalaire() {
        return lire(em -> em.createQuery(
                        "select c from Collaborateur c order by c.salaire desc", Collaborateur.class)
                .getResultList());
    }

    /** Collaborateurs triés par nom puis par prénom. */
    public List<Collaborateur> triesParNomPuisPrenom() {
        return lire(em -> em.createQuery(
                        "select c from Collaborateur c order by c.nom asc, c.prenom asc", Collaborateur.class)
                .getResultList());
    }

}