package fr.efrei.java;

import fr.efrei.java.exceptions.*;
import fr.efrei.java.model.*;
import fr.efrei.java.service.Annuaire;
import fr.efrei.java.service.GestionEquipes;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Scanner;

/**
 * Point d'entrée de l'application : interface console de l'annuaire des collaborateurs
 * et de la gestion des équipes.
 */
public class HelloEfrei {

    private static final Logger logger = LoggerFactory.getLogger(HelloEfrei.class);

    /** Nom de la persistence unit déclarée dans persistence.xml. */
    private static final String UNITE_DE_PERSISTANCE = "efreiforge-unit";

    /**
     * Logger JUL de Hibernate, conservé dans un champ statique : sans référence forte,
     * Java peut le supprimer de la mémoire et le niveau réglé ci-dessous serait perdu.
     */
    private static final java.util.logging.Logger LOGGER_HIBERNATE =
            java.util.logging.Logger.getLogger("org.hibernate");

    static {
        // Exécuté au chargement de la classe, donc avant tout démarrage de Hibernate
        LOGGER_HIBERNATE.setLevel(java.util.logging.Level.SEVERE);
    }

    public static void main(String[] args) {
        EntityManagerFactory fabrique = creerFabrique();
        if (fabrique == null) {
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            Annuaire annuaire = new Annuaire(fabrique);
            GestionEquipes equipes = new GestionEquipes(fabrique);
            avertirSiBaseVide(annuaire);
            boucleMenu(annuaire, equipes, scanner);
        } catch (NoSuchElementException e) {
            // Fin de la saisie (Ctrl+D) : plus rien à lire, on quitte proprement
            System.out.println();
            System.out.println("Fin de la saisie. À bientôt !");
        } catch (PersistenceException e) {
            signalerErreurBase(e);
        } finally {
            fabrique.close();
        }
    }

    /**
     * Crée la fabrique JPA.
     */
    private static EntityManagerFactory creerFabrique() {
        try {
            return Persistence.createEntityManagerFactory(UNITE_DE_PERSISTANCE);
        } catch (PersistenceException e) {
            logger.error("Impossible de créer l'EntityManagerFactory", e);
            System.out.println("Impossible de se connecter à la base de données. "
                    + "Vérifiez que MySQL est démarré dans XAMPP (port 3306) "
                    + "et que le script sql/efreiforge.sql a été importé.");
            return null;
        }
    }

    /**
     * Erreur technique d'accès à la base : le détail part dans le fichier de logs,
     * l'utilisateur reçoit un message simple.
     */
    private static void signalerErreurBase(PersistenceException e) {
        logger.error("Erreur d'accès à la base de données", e);
        System.out.println("Erreur d'accès à la base de données. "
                + "Consultez le fichier efreiforge.log pour le détail.");
    }

    private static void avertirSiBaseVide(Annuaire annuaire) {
        if (annuaire.nombre() == 0) {
            System.out.println("La base ne contient aucun collaborateur. "
                    + "Importez le script sql/efreiforge.sql (voir JOURNAL.md).");
        }
    }

    // =========================================================================
    // Menu Principal et Aiguillage
    // =========================================================================

    private static void boucleMenu(Annuaire annuaire, GestionEquipes equipes, Scanner scanner) {
        int choix = -1;

        while (choix != 0) {
            afficherMenu();

            OptionalInt saisie = lireChoix(scanner);
            if (saisie.isEmpty()) {
                continue;
            }
            choix = saisie.getAsInt();

            // Les erreurs sont traitées dans chaque sous-menu
            executerChoix(choix, annuaire, equipes, scanner);
        }
    }

    private static void executerChoix(int choix, Annuaire annuaire, GestionEquipes equipes, Scanner scanner) {
        switch (choix) {
            case 0 -> System.out.println("À bientôt !");
            case 1 -> menuCollaborateurs(annuaire, scanner);
            case 2 -> menuEquipes(equipes, scanner);
            default -> System.out.println("Option invalide. Choisissez entre 0 et 2.");
        }
    }

    // =========================================================================
    // SOUS-MENU : COLLABORATEURS
    // =========================================================================

    private static void menuCollaborateurs(Annuaire annuaire, Scanner scanner) {
        int choix = -1;
        while (choix != 0) {
            System.out.println();
            System.out.println("--- GESTION DES COLLABORATEURS ---");
            System.out.println("1. Afficher tous les collaborateurs");
            System.out.println("2. Rechercher par nom");
            System.out.println("3. Filtrer par salaire minimum");
            System.out.println("4. Afficher les programmeurs");
            System.out.println("5. Trier par nom");
            System.out.println("6. Trier par salaire");
            System.out.println("7. Trier par nom puis prénom");
            System.out.println("8. Afficher la fiche d'un collaborateur");
            System.out.println("9. Augmenter le salaire d'un collaborateur");
            System.out.println("10. Ajouter un collaborateur");
            System.out.println("0. Retour au menu principal");
            System.out.print("Votre choix : ");

            OptionalInt saisie = lireChoix(scanner);
            if (saisie.isEmpty()) continue;
            choix = saisie.getAsInt();

            try {
                switch (choix) {
                    case 0 -> System.out.println("Retour au menu principal...");
                    case 1 -> afficherListe(annuaire.tous());
                    case 2 -> rechercherParNom(annuaire, scanner);
                    case 3 -> filtrerParSalaire(annuaire, scanner);
                    case 4 -> afficherListe(annuaire.programmeurs());
                    case 5 -> afficherListe(annuaire.triesParNom());
                    case 6 -> afficherListe(annuaire.triesParSalaire());
                    case 7 -> afficherListe(annuaire.triesParNomPuisPrenom());
                    case 8 -> afficherFiche(annuaire, scanner);
                    case 9 -> augmenterSalaire(annuaire, scanner);
                    case 10 -> ajouterCollaborateur(annuaire, scanner);
                    default -> System.out.println("Option invalide entre 0 et 10.");
                }
            } catch (IllegalArgumentException | CollaborateurIntrouvableException e) {
                System.out.println("Erreur : " + e.getMessage());
            } catch (PersistenceException e) {
                signalerErreurBase(e);
            }
        }
    }

    // =========================================================================
    // SOUS-MENU : ÉQUIPES
    // =========================================================================

    private static void menuEquipes(GestionEquipes equipes, Scanner scanner) {
        int choix = -1;
        while (choix != 0) {
            System.out.println();
            System.out.println("--- GESTION DES ÉQUIPES ---");
            System.out.println("1. Créer une équipe");
            System.out.println("2. Afficher les équipes");
            System.out.println("3. Affecter un collaborateur à une équipe");
            System.out.println("4. Retirer un collaborateur d'une équipe");
            System.out.println("5. Afficher les collaborateurs d'une équipe");
            System.out.println("6. Renommer une équipe");
            System.out.println("7. Supprimer une équipe");
            System.out.println("8. Afficher les collaborateurs sans équipe");
            System.out.println("0. Retour au menu principal");
            System.out.print("Votre choix : ");

            OptionalInt saisie = lireChoix(scanner);
            if (saisie.isEmpty()) continue;
            choix = saisie.getAsInt();

            try {
                switch (choix) {
                    case 0 -> System.out.println("Retour au menu principal...");
                    case 1 -> creerEquipe(equipes, scanner);
                    case 2 -> afficherEquipes(equipes);
                    case 3 -> affecterCollaborateur(equipes, scanner);
                    case 4 -> retirerCollaborateur(equipes, scanner);
                    case 5 -> afficherMembres(equipes, scanner);
                    case 6 -> renommerEquipe(equipes, scanner);
                    case 7 -> supprimerEquipe(equipes, scanner);
                    case 8 -> afficherSansEquipe(equipes);
                    default -> System.out.println("Option invalide entre 0 et 8.");
                }
            } catch (EquipeDejaExistanteException | EquipeIntrouvableException | EquipeNonVideException
                     | AffectationRefuseeException | CollaborateurIntrouvableException
                     | IllegalArgumentException e) {
                System.out.println("Erreur : " + e.getMessage());
            } catch (PersistenceException e) {
                signalerErreurBase(e);
            }
        }
    }

    // =========================================================================
    // Méthodes Collaborateurs
    // =========================================================================

    private static void rechercherParNom(Annuaire annuaire, Scanner scanner) {
        String fragment = lireTexte(scanner, "Fragment de nom : ");
        afficherListe(annuaire.nomContenant(fragment));
    }

    private static void filtrerParSalaire(Annuaire annuaire, Scanner scanner) {
        OptionalDouble minimum = lireNombre(scanner, "Salaire minimum (€) : ");
        if (minimum.isEmpty()) {
            return;
        }
        try {
            afficherListe(annuaire.salaireAuMoins(minimum.getAsDouble()));
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private static void afficherFiche(Annuaire annuaire, Scanner scanner) {
        String identifiant = lireTexte(scanner, "Identifiant du collaborateur : ").toUpperCase();
        Collaborateur trouve = annuaire.trouver(identifiant);
        if (trouve != null) {
            System.out.println();
            trouve.afficherFiche();
        } else {
            System.out.println("Collaborateur introuvable : " + identifiant);
        }
    }

    private static void augmenterSalaire(Annuaire annuaire, Scanner scanner) {
        String identifiant = lireTexte(scanner, "Identifiant du collaborateur : ").toUpperCase();
        Collaborateur collaborateur = annuaire.trouver(identifiant);
        if (collaborateur == null) {
            System.out.println("Collaborateur introuvable : " + identifiant);
            return;
        }

        OptionalDouble pourcentage = lireNombre(scanner, "Pourcentage d'augmentation : ");
        if (pourcentage.isEmpty()) {
            return;
        }

        double ancienSalaire = collaborateur.getSalaire();
        try {
            double nouveauSalaire = annuaire.mettreAJourSalaire(identifiant, pourcentage.getAsDouble());
            System.out.printf("Salaire : %.2f € -> %.2f €%n", ancienSalaire, nouveauSalaire);
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur : " + e.getMessage());
        } catch (CollaborateurIntrouvableException e) {
            System.out.println("Collaborateur introuvable : " + e.identifiant());
        }
    }

    private static void ajouterCollaborateur(Annuaire annuaire, Scanner scanner) {
        String type = lireTexte(scanner, "Type (P = programmeur, T = testeur) : ").toUpperCase();
        if (!type.equals("P") && !type.equals("T")) {
            System.out.println("Type invalide : saisissez P ou T.");
            return;
        }

        String identifiant = lireTexte(scanner, "Identifiant (format C001) : ").toUpperCase();
        if (!identifiant.matches("C\\d{3}")) {
            System.out.println("Identifiant invalide : le format attendu est C suivi de 3 chiffres.");
            return;
        }

        String prenom = lireTexte(scanner, "Prénom : ");
        String nom = lireTexte(scanner, "Nom : ");
        OptionalDouble salaire = lireNombre(scanner, "Salaire (€) : ");
        if (salaire.isEmpty()) {
            return;
        }
        String langage = type.equals("P") ? lireTexte(scanner, "Langage préféré : ") : null;

        Adresse adresse = new Adresse(
                lireTexte(scanner, "Rue : "),
                lireTexte(scanner, "Code postal : "),
                lireTexte(scanner, "Ville : "),
                lireTexte(scanner, "Pays : "));

        try {
            Collaborateur nouveau = type.equals("P")
                    ? new Programmeur(identifiant, prenom, nom, salaire.getAsDouble(), adresse, langage)
                    : new Testeur(identifiant, prenom, nom, salaire.getAsDouble(), adresse);
            annuaire.ajouter(nouveau);
            System.out.println("Collaborateur ajouté : " + nouveau);
        } catch (CollaborateurDejaExistantException e) {
            System.out.println("Erreur : l'identifiant " + e.identifiant()
                    + " est déjà utilisé. Aucun collaborateur n'a été ajouté.");
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    // =========================================================================
    // Méthodes Équipes
    // =========================================================================

    private static void creerEquipe(GestionEquipes equipes, Scanner scanner) {
        afficherEquipes(equipes);
        String nom = lireLigne(scanner, "Nom de la nouvelle équipe : ");
        Equipe creee = equipes.creer(nom);
        System.out.println("Équipe « " + creee.getNom() + " » créée.");
    }

    private static void afficherEquipes(GestionEquipes equipes) {
        List<Equipe> liste = equipes.lister();
        if (liste.isEmpty()) {
            System.out.println("Aucune équipe pour le moment.");
            return;
        }
        System.out.println("Équipes existantes (" + liste.size() + ") :");
        for (Equipe equipe : liste) {
            long nombre = equipes.nombreMembres(equipe);
            System.out.println("  " + equipe.getNom() + " - " + nombre + " collaborateur(s)");
        }
    }

    // Les messages de succès affichent le nom réel de l'équipe (getNom()),
    // et non la saisie de l'utilisateur, dont la casse peut être différente.

    private static void affecterCollaborateur(GestionEquipes equipes, Scanner scanner) {
        afficherEquipes(equipes);
        String identifiant = lireTexte(scanner, "Identifiant du collaborateur : ").toUpperCase();
        String nomEquipe = lireTexte(scanner, "Nom de l'équipe : ");
        Equipe equipe = equipes.affecter(identifiant, nomEquipe);
        System.out.println(identifiant + " a été affecté à l'équipe « " + equipe.getNom() + " ».");
    }

    private static void retirerCollaborateur(GestionEquipes equipes, Scanner scanner) {
        afficherEquipes(equipes);
        String identifiant = lireTexte(scanner, "Identifiant du collaborateur : ").toUpperCase();
        String nomEquipe = lireTexte(scanner, "Équipe dont le retirer : ");
        Equipe equipe = equipes.retirer(identifiant, nomEquipe);
        System.out.println(identifiant + " a été retiré de l'équipe « " + equipe.getNom() + " ».");
    }

    private static void afficherMembres(GestionEquipes equipes, Scanner scanner) {
        afficherEquipes(equipes);
        Equipe equipe = equipes.trouver(lireTexte(scanner, "Nom de l'équipe : "));
        List<Collaborateur> membres = equipes.membres(equipe);
        if (membres.isEmpty()) {
            System.out.println("L'équipe « " + equipe.getNom() + " » ne contient aucun collaborateur.");
            return;
        }
        afficherCollaborateursEtMetier("Équipe « " + equipe.getNom() + " »", membres);
    }

    private static void renommerEquipe(GestionEquipes equipes, Scanner scanner) {
        afficherEquipes(equipes);
        // On vérifie que l'équipe existe avant de demander le nouveau nom
        Equipe equipe = equipes.trouver(lireTexte(scanner, "Nom de l'équipe à renommer : "));
        String nouveauNom = lireLigne(scanner, "Nouveau nom : ");
        Equipe renommee = equipes.renommer(equipe.getNom(), nouveauNom);
        System.out.println("Équipe « " + equipe.getNom() + " » renommée en « " + renommee.getNom() + " ».");
    }

    private static void supprimerEquipe(GestionEquipes equipes, Scanner scanner) {
        afficherEquipes(equipes);
        String nomEquipe = lireTexte(scanner, "Nom de l'équipe à supprimer : ");
        Equipe supprimee = equipes.supprimer(nomEquipe);
        System.out.println("Équipe « " + supprimee.getNom() + " » supprimée.");
    }

    private static void afficherSansEquipe(GestionEquipes equipes) {
        List<Collaborateur> disponibles = equipes.sansEquipe();
        if (disponibles.isEmpty()) {
            System.out.println("Tous les collaborateurs ont une équipe.");
            return;
        }
        afficherCollaborateursEtMetier("Collaborateurs sans équipe", disponibles);
    }

    // =========================================================================
    // Lecture des saisies
    // =========================================================================

    private static OptionalInt lireChoix(Scanner scanner) {
        String saisie = scanner.nextLine().trim();
        try {
            return OptionalInt.of(Integer.parseInt(saisie));
        } catch (NumberFormatException e) {
            System.out.println("Saisie invalide. Veuillez entrer un nombre.");
            return OptionalInt.empty();
        }
    }

    private static OptionalDouble lireNombre(Scanner scanner, String invite) {
        System.out.print(invite);
        String saisie = scanner.nextLine().trim().replace(',', '.');
        try {
            double valeur = Double.parseDouble(saisie);
            if (Double.isFinite(valeur)) {
                return OptionalDouble.of(valeur);
            }
        } catch (NumberFormatException e) {
            // Ignoré, géré par le message en dessous
        }
        System.out.println("Saisie invalide. Veuillez entrer un nombre.");
        return OptionalDouble.empty();
    }

    private static String lireTexte(Scanner scanner, String invite) {
        String saisie;
        do {
            System.out.print(invite);
            saisie = scanner.nextLine().trim();
            if (saisie.isEmpty()) {
                System.out.println("La valeur ne peut pas être vide.");
            }
        } while (saisie.isEmpty());
        return saisie;
    }

    private static String lireLigne(Scanner scanner, String invite) {
        System.out.print(invite);
        return scanner.nextLine();
    }

    // =========================================================================
    // Affichage des menus et listes
    // =========================================================================

    private static void afficherMenu() {
        System.out.println();
        System.out.println("=== Annuaire des collaborateurs & Équipes (EFREI) ===");
        System.out.println("1. Gestion des Collaborateurs");
        System.out.println("2. Gestion des Équipes");
        System.out.println("0. Quitter");
        System.out.print("Votre choix : ");
    }

    private static void afficherListe(List<? extends Collaborateur> liste) {
        if (liste.isEmpty()) {
            System.out.println("Aucun collaborateur trouvé.");
        } else {
            System.out.println(liste.size() + " collaborateur(s) :");
            for (Collaborateur c : liste) {
                System.out.println("  " + c);
            }
        }
    }

    private static void afficherCollaborateursEtMetier(String titre, List<? extends Collaborateur> liste) {
        System.out.println(titre + " (" + liste.size() + ") :");
        for (Collaborateur c : liste) {
            System.out.println("  " + c.getIdentifiant() + " " + c.getPrenom() + " " + c.getNom()
                    + " - " + c.getMetier());
        }
    }
}