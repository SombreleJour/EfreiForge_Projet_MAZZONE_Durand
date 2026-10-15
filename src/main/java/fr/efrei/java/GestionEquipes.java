package fr.efrei.java;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class GestionEquipes {

    private final EntityManager em;

    public GestionEquipes(EntityManager em) {
        this.em = em;
    }

    /**
     * Crée une nouvelle équipe si elle n'existe pas déjà.
     */
    public Equipe creerEquipe(String nom) {
        // Vérification des doublons ou normalisation à venir
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Equipe equipe = new Equipe(nom);
            em.persist(equipe);
            tx.commit();
            return equipe;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new EquipeDejaExistanteException("L'équipe '" + nom + "' existe déjà ou pose un problème.");
        }
    }
}