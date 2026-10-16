package fr.efrei.java.service;

import fr.efrei.java.exeptions.*;
import fr.efrei.java.model.Collaborateur;
import fr.efrei.java.model.Equipe;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class GestionEquipes extends AccesJpa {

    public GestionEquipes(EntityManagerFactory emf) {
        super(emf);
    }

    /**
     * Crée une nouvelle équipe.
     */
    public Equipe creer(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'équipe ne peut pas être vide.");
        }
        String nomNormalise = nom.trim();

        if (rechercherParNom(nomNormalise) != null) {
            throw new EquipeDejaExistanteException("L'équipe '" + nomNormalise + "' existe déjà.");
        }

        return ecrire(em -> {
            Equipe equipe = new Equipe(nomNormalise);
            em.persist(equipe);
            return equipe;
        });
    }

    /**
     * Liste toutes les équipes.
     */
    public List<Equipe> lister() {
        return lire(em -> {
            TypedQuery<Equipe> query = em.createQuery("SELECT e FROM Equipe e ORDER BY e.nom", Equipe.class);
            return query.getResultList();
        });
    }

    /**
     * Renomme une équipe existante.
     * Changer seulement la casse du nom de l'équipe elle-même est accepté ;
     * le nom d'une AUTRE équipe est refusé.
     */
    public void renommer(String ancienNom, String nouveauNom) {
        if (nouveauNom == null || nouveauNom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nouveau nom ne peut pas être vide.");
        }
        String nouvNomNormalise = nouveauNom.trim();

        executer(em -> {
            Equipe equipe = em.createQuery("SELECT e FROM Equipe e WHERE LOWER(e.nom) = LOWER(:nom)", Equipe.class)
                    .setParameter("nom", ancienNom.trim())
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new EquipeIntrouvableException("Équipe introuvable : " + ancienNom));

            Equipe existante = rechercherParNom(nouvNomNormalise);
            if (existante != null && !existante.getId().equals(equipe.getId())) {
                throw new EquipeDejaExistanteException("Une équipe nommée '" + nouvNomNormalise + "' existe déjà.");
            }

            equipe.renommer(nouvNomNormalise);
            em.merge(equipe);
        });
    }

    /**
     * Supprime une équipe si elle est vide.
     */
    public void supprimer(String nom) {
        executer(em -> {
            Equipe equipe = em.createQuery("SELECT e FROM Equipe e WHERE LOWER(e.nom) = LOWER(:nom)", Equipe.class)
                    .setParameter("nom", nom.trim())
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new EquipeIntrouvableException("Équipe introuvable : " + nom));

            long nombre = em.createQuery(
                            "SELECT COUNT(c) FROM Collaborateur c WHERE c.equipe = :equipe", Long.class)
                    .setParameter("equipe", equipe)
                    .getSingleResult();
            if (nombre > 0) {
                throw new EquipeNonVideException(equipe.getNom(), nombre);
            }

            em.remove(em.contains(equipe) ? equipe : em.merge(equipe));
        });
    }

    /**
     * Affecte un collaborateur à une équipe.
     */
    public void affecter(String idCollab, String nomEquipe) {
        executer(em -> {
            Collaborateur collab = em.find(Collaborateur.class, idCollab);
            if (collab == null) {
                throw new CollaborateurIntrouvableException(idCollab);
            }

            Equipe equipe = em.createQuery("SELECT e FROM Equipe e WHERE LOWER(e.nom) = LOWER(:nom)", Equipe.class)
                    .setParameter("nom", nomEquipe.trim())
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new EquipeIntrouvableException("Équipe introuvable : " + nomEquipe));

            if (collab.getEquipe() != null) {
                throw new AffectationRefuseeException("Le collaborateur est déjà affecté à une équipe.");
            }

            collab.affecterA(equipe);
            em.merge(collab);
        });
    }

    /**
     * Retire un collaborateur d'une équipe.
     */
    public void retirer(String idCollab, String nomEquipe) {
        executer(em -> {
            Collaborateur collab = em.find(Collaborateur.class, idCollab);
            if (collab == null) {
                throw new CollaborateurIntrouvableException(idCollab);
            }

            Equipe equipe = em.createQuery("SELECT e FROM Equipe e WHERE LOWER(e.nom) = LOWER(:nom)", Equipe.class)
                    .setParameter("nom", nomEquipe.trim())
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new EquipeIntrouvableException("Équipe introuvable : " + nomEquipe));

            collab.retirerDe(equipe);
            em.merge(collab);
        });
    }

    /**
     * Renvoie la liste des membres d'une équipe.
     * Requête explicite : la liste est entièrement chargée avant la fermeture de l'EntityManager.
     */
    public List<Collaborateur> membres(String nomEquipe) {
        return lire(em -> {
            Equipe equipe = em.createQuery("SELECT e FROM Equipe e WHERE LOWER(e.nom) = LOWER(:nom)", Equipe.class)
                    .setParameter("nom", nomEquipe.trim())
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new EquipeIntrouvableException("Équipe introuvable : " + nomEquipe));
            return em.createQuery(
                            "SELECT c FROM Collaborateur c WHERE c.equipe = :equipe ORDER BY c.identifiant",
                            Collaborateur.class)
                    .setParameter("equipe", equipe)
                    .getResultList();
        });
    }

    /**
     * Renvoie la liste des collaborateurs sans équipe.
     */
    public List<Collaborateur> sansEquipe() {
        return lire(em -> {
            TypedQuery<Collaborateur> query = em.createQuery(
                    "SELECT c FROM Collaborateur c WHERE c.equipe IS NULL", Collaborateur.class);
            return query.getResultList();
        });
    }

    /**
     * Méthode utilitaire de recherche.
     */
    private Equipe rechercherParNom(String nom) {
        return lire(em -> {
            TypedQuery<Equipe> query = em.createQuery(
                    "SELECT e FROM Equipe e WHERE LOWER(e.nom) = LOWER(:nom)", Equipe.class);
            query.setParameter("nom", nom.trim());
            List<Equipe> res = query.getResultList();
            return res.isEmpty() ? null : res.get(0);
        });
    }
}