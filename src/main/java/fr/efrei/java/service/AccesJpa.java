package fr.efrei.java.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Classe mère des services qui accèdent à la base via JPA (Annuaire, GestionEquipes).
 *
 * Elle regroupe la gestion de l'EntityManager et des transactions, écrite une seule fois :
 *  - lire(...)    : un EntityManager le temps d'une lecture, sans transaction ;
 *  - ecrire(...)  : une transaction, avec commit si tout réussit et rollback sinon ;
 *  - executer(...) : variante de ecrire(...) pour une opération qui ne renvoie rien.
 *
 * Les services qui en héritent ne parlent jamais à l'utilisateur : ils signalent
 * les problèmes par des exceptions.
 */
public abstract class AccesJpa {

    private static final Logger logger = LoggerFactory.getLogger(AccesJpa.class);

    private final EntityManagerFactory fabrique;

    /**
     * @param fabrique fabrique JPA, créée et fermée par l'appelant
     */
    protected AccesJpa(EntityManagerFactory fabrique) {
        this.fabrique = fabrique;
    }

    /**
     * Exécute une lecture : un EntityManager le temps de l'opération, sans transaction.
     * Les entités renvoyées sont détachées une fois l'EntityManager fermé.
     */
    protected <T> T lire(Function<EntityManager, T> requete) {
        try (EntityManager em = fabrique.createEntityManager()) {
            return requete.apply(em);
        }
    }

    /**
     * Exécute une écriture dans une transaction : commit si tout réussit,
     * rollback si une exception survient (la base reste alors inchangée).
     */
    protected <T> T ecrire(Function<EntityManager, T> operation) {
        try (EntityManager em = fabrique.createEntityManager()) {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                T resultat = operation.apply(em);
                transaction.commit();
                return resultat;
            } catch (RuntimeException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                logger.debug("Transaction annulée (rollback) : {}", e.getMessage());
                throw e; // l'exception n'est jamais avalée : l'appelant décide quoi en faire
            }
        }
    }

    /** Variante de ecrire(...) pour une opération qui ne renvoie rien. */
    protected void executer(Consumer<EntityManager> operation) {
        ecrire(em -> {
            operation.accept(em);
            return null;
        });
    }
}