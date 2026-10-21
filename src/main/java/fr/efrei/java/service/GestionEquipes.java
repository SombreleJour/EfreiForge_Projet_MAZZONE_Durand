package fr.efrei.java.service;

import fr.efrei.java.exceptions.CollaborateurIntrouvableException;
import fr.efrei.java.exceptions.EquipeDejaExistanteException;
import fr.efrei.java.exceptions.EquipeIntrouvableException;
import fr.efrei.java.exceptions.EquipeNonVideException;
import fr.efrei.java.model.Collaborateur;
import fr.efrei.java.model.Equipe;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Gestion des équipes et des affectations : accès à la base via JPA.
 *
 * Comme Annuaire, cette classe ne parle jamais à l'utilisateur : elle signale les
 * problèmes par des exceptions et trace les événements avec SLF4J.
 *
 * Chaque écriture se fait dans UNE transaction, avec UN EntityManager : l'équipe et le
 * collaborateur chargés y sont des entités gérées, leurs modifications sont donc
 * enregistrées au commit sans persist ni merge (comme l'augmentation de salaire du TP4).
 */
public class GestionEquipes extends AccesJpa {

    private static final Logger logger = LoggerFactory.getLogger(GestionEquipes.class);

    /** Recherche d'une équipe par son nom, sans tenir compte de la casse. */
    private static final String REQUETE_EQUIPE_PAR_NOM =
            "select e from Equipe e where lower(e.nom) = lower(:nom)";

    /**
     * @param fabrique fabrique JPA, créée et fermée par l'appelant
     */
    public GestionEquipes(EntityManagerFactory fabrique) {
        super(fabrique);
    }

    // =========================================================================
    // Écriture
    // =========================================================================

    /**
     * Crée une nouvelle équipe.
     *
     * @return l'équipe créée
     * @throws IllegalArgumentException     si le nom est vide ou trop long
     * @throws EquipeDejaExistanteException si une équipe porte déjà ce nom (casse ignorée)
     */
    public Equipe creer(String nom) {
        String nomNormalise = Equipe.normaliserNom(nom);
        Equipe creee = ecrire(em -> {
            if (chercherEquipe(em, nomNormalise) != null) {
                throw new EquipeDejaExistanteException("L'équipe « " + nomNormalise + " » existe déjà.");
            }
            Equipe equipe = new Equipe(nomNormalise);
            em.persist(equipe);
            return equipe;
        });
        logger.info("Équipe {} créée", creee.getNom());
        return creee;
    }

    /**
     * Renomme une équipe existante.
     * Changer seulement la casse du nom de l'équipe elle-même est accepté ;
     * le nom d'une AUTRE équipe est refusé.
     *
     * @return l'équipe renommée
     * @throws IllegalArgumentException     si le nouveau nom est vide ou trop long
     * @throws EquipeIntrouvableException   si l'équipe à renommer n'existe pas
     * @throws EquipeDejaExistanteException si le nouveau nom est celui d'une autre équipe
     */
    public Equipe renommer(String ancienNom, String nouveauNom) {
        String nomNormalise = Equipe.normaliserNom(nouveauNom);
        Equipe renommee = ecrire(em -> {
            Equipe equipe = trouverEquipe(em, ancienNom);
            Equipe homonyme = chercherEquipe(em, nomNormalise);
            // On compare les identifiants : equals() compare les noms sans la casse,
            // il serait donc vrai aussi pour l'équipe elle-même.
            if (homonyme != null && !homonyme.getId().equals(equipe.getId())) {
                throw new EquipeDejaExistanteException("Une équipe nommée « " + nomNormalise + " » existe déjà.");
            }
            equipe.renommer(nomNormalise);
            return equipe;
        });
        logger.info("Équipe {} renommée en {}", ancienNom, renommee.getNom());
        return renommee;
    }

    /**
     * Supprime une équipe, à condition qu'elle ne contienne aucun collaborateur.
     *
     * @return l'équipe supprimée
     * @throws EquipeIntrouvableException si l'équipe n'existe pas
     * @throws EquipeNonVideException     si l'équipe contient encore des collaborateurs
     */
    public Equipe supprimer(String nom) {
        Equipe supprimee = ecrire(em -> {
            Equipe equipe = trouverEquipe(em, nom);
            long nombre = compterMembres(em, equipe);
            if (nombre > 0) {
                throw new EquipeNonVideException(equipe.getNom(), nombre);
            }
            em.remove(equipe);
            return equipe;
        });
        logger.info("Équipe {} supprimée", supprimee.getNom());
        return supprimee;
    }

    /**
     * Affecte un collaborateur sans équipe à une équipe.
     * La règle métier (refus si le collaborateur a déjà une équipe) est vérifiée par
     * Collaborateur.affecterA(...).
     *
     * @return l'équipe d'affectation
     * @throws CollaborateurIntrouvableException si le collaborateur n'existe pas
     * @throws EquipeIntrouvableException        si l'équipe n'existe pas
     * @throws fr.efrei.java.exceptions.AffectationRefuseeException si le collaborateur a déjà une équipe
     */
    public Equipe affecter(String identifiant, String nomEquipe) {
        Equipe equipe = ecrire(em -> {
            Collaborateur collaborateur = trouverCollaborateur(em, identifiant);
            Equipe cible = trouverEquipe(em, nomEquipe);
            collaborateur.affecterA(cible);
            return cible;
        });
        logger.info("{} affecté à l'équipe {}", identifiant, equipe.getNom());
        return equipe;
    }

    /**
     * Retire un collaborateur de l'équipe indiquée.
     * La règle métier (refus s'il n'appartient pas à cette équipe) est vérifiée par
     * Collaborateur.retirerDe(...).
     *
     * @return l'équipe quittée
     * @throws CollaborateurIntrouvableException si le collaborateur n'existe pas
     * @throws EquipeIntrouvableException        si l'équipe n'existe pas
     * @throws fr.efrei.java.exceptions.AffectationRefuseeException si le collaborateur n'est pas dans cette équipe
     */
    public Equipe retirer(String identifiant, String nomEquipe) {
        Equipe equipe = ecrire(em -> {
            Collaborateur collaborateur = trouverCollaborateur(em, identifiant);
            Equipe quittee = trouverEquipe(em, nomEquipe);
            collaborateur.retirerDe(quittee);
            return quittee;
        });
        logger.info("{} retiré de l'équipe {}", identifiant, equipe.getNom());
        return equipe;
    }

    // =========================================================================
    // Lecture
    // =========================================================================

    /**
     * Retrouve une équipe par son nom (casse ignorée).
     *
     * @throws EquipeIntrouvableException si l'équipe n'existe pas
     */
    public Equipe trouver(String nom) {
        return lire(em -> trouverEquipe(em, nom));
    }

    /** Toutes les équipes, par nom. */
    public List<Equipe> lister() {
        return lire(em -> em.createQuery("select e from Equipe e order by e.nom", Equipe.class)
                .getResultList());
    }

    /** Membres d'une équipe, par identifiant. */
    public List<Collaborateur> membres(Equipe equipe) {
        return lire(em -> em.createQuery(
                        "select c from Collaborateur c where c.equipe = :equipe order by c.identifiant",
                        Collaborateur.class)
                .setParameter("equipe", equipe)
                .getResultList());
    }

    /** Nombre de membres d'une équipe (compté par la base). */
    public long nombreMembres(Equipe equipe) {
        return lire(em -> compterMembres(em, equipe));
    }

    /** Collaborateurs sans équipe, par identifiant. */
    public List<Collaborateur> sansEquipe() {
        return lire(em -> em.createQuery(
                        "select c from Collaborateur c where c.equipe is null order by c.identifiant",
                        Collaborateur.class)
                .getResultList());
    }

    // =========================================================================
    // Méthodes utilitaires : elles utilisent l'EntityManager reçu, pour rester
    // dans la même transaction que l'opération appelante.
    // =========================================================================

    /** Équipe portant ce nom (casse ignorée), ou null s'il n'y en a pas. */
    private Equipe chercherEquipe(EntityManager em, String nom) {
        List<Equipe> resultats = em.createQuery(REQUETE_EQUIPE_PAR_NOM, Equipe.class)
                .setParameter("nom", nom.trim())
                .getResultList();
        return resultats.isEmpty() ? null : resultats.get(0);
    }

    /** Comme chercherEquipe(...), mais une équipe absente est une erreur. */
    private Equipe trouverEquipe(EntityManager em, String nom) {
        Equipe equipe = chercherEquipe(em, nom);
        if (equipe == null) {
            throw new EquipeIntrouvableException("Équipe introuvable : " + nom.trim());
        }
        return equipe;
    }

    private Collaborateur trouverCollaborateur(EntityManager em, String identifiant) {
        Collaborateur collaborateur = em.find(Collaborateur.class, identifiant);
        if (collaborateur == null) {
            throw new CollaborateurIntrouvableException(identifiant);
        }
        return collaborateur;
    }

    private long compterMembres(EntityManager em, Equipe equipe) {
        return em.createQuery("select count(c) from Collaborateur c where c.equipe = :equipe", Long.class)
                .setParameter("equipe", equipe)
                .getSingleResult();
    }
}
