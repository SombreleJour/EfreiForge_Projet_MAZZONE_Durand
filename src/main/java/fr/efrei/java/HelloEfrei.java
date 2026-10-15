package fr.efrei.java;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Scanner;

/**
 * Point d'entrée de l'application : interface console de l'annuaire des collaborateurs.
 *
 * Cette classe ne contient aucun code JPA métier : elle crée la fabrique, valide le FORMAT
 * des saisies (nombre, identifiant au format C001...), puis délègue à Annuaire.
 * Les règles métier sont protégées par le domaine (Collaborateur, Annuaire) et signalées
 * par des exceptions que l'on intercepte ici pour afficher un message à l'utilisateur.
 */
public class HelloEfrei {

    private static final Logger logger = LoggerFactory.getLogger(HelloEfrei.class);

    /** Nom de la persistence unit déclarée dans persistence.xml. */
    private static final String UNITE_DE_PERSISTANCE = "efreiforge-unit";

    public static void main(String[] args) {
        EntityManagerFactory fabrique = creerFabrique();
        if (fabrique == null) {
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            Annuaire annuaire = new Annuaire(fabrique);
            chargerDonneesDemo(annuaire);
            boucleMenu(annuaire, scanner);
        } catch (PersistenceException e) {
            logger.error("Erreur d'accès à la base de données au démarrage", e);
            System.out.println("Erreur d'accès à la base de données. Consultez les logs pour le détail.");
        } finally {
            fabrique.close();
        }
    }

    /**
     * Crée la fabrique JPA (une seule fois, elle est coûteuse).
     * Si la base est inaccessible : message simple pour l'utilisateur,
     * stack trace complète dans les logs pour le développeur.
     *
     * @return la fabrique, ou null si la connexion a échoué
     */
    private static EntityManagerFactory creerFabrique() {
        try {
            return Persistence.createEntityManagerFactory(UNITE_DE_PERSISTANCE);
        } catch (PersistenceException e) {
            logger.error("Impossible de créer l'EntityManagerFactory", e);
            System.out.println("Impossible de se connecter à la base de données. "
                    + "Vérifiez que MySQL est démarré dans XAMPP (port 3306).");
            return null;
        }
    }

    /** Insère les données de démonstration uniquement si la base est vide. */
    private static void chargerDonneesDemo(Annuaire annuaire) {
        if (annuaire.nombre() > 0) {
            return;
        }
        for (Collaborateur collaborateur : DonneesDemo.creerCollaborateurs()) {
            try {
                annuaire.ajouter(collaborateur);
            } catch (CollaborateurDejaExistantException e) {
                System.out.println("Le collaborateur " + e.identifiant() + " existe déjà : ignoré.");
            }
        }
        System.out.println("Données de démonstration chargées.");
    }

    // =========================================================================
    // Menu
    // =========================================================================

    private static void boucleMenu(Annuaire annuaire, Scanner scanner) {
        int choix = -1;

        while (choix != 0) {
            afficherMenu();

            // Fin de l'entrée standard (Ctrl+D) : on s'arrête proprement
            if (!scanner.hasNextLine()) {
                return;
            }

            OptionalInt saisie = lireChoix(scanner);
            if (saisie.isEmpty()) {
                continue; // message déjà affiché par lireChoix
            }
            choix = saisie.getAsInt();

            try {
                executerChoix(choix, annuaire, scanner);
            } catch (PersistenceException e) {
                logger.error("Erreur d'accès à la base de données", e);
                System.out.println("Erreur d'accès à la base de données. Consultez les logs pour le détail.");
            }
        }
    }

    private static void executerChoix(int choix, Annuaire annuaire, Scanner scanner) {
        switch (choix) {
            case 0 -> System.out.println("À bientôt !");
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
            default -> System.out.println("Option invalide. Choisissez entre 0 et 10.");
        }
    }

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
            // Règle métier violée (salaire négatif) : message de l'exception
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

        // Erreur de saisie (format) : traitée ici, avant d'appeler la logique métier
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
            // Règle métier violée dans le constructeur (ex. : salaire négatif)
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    // =========================================================================
    // Lecture des saisies (toujours par lignes entières : pas de piège de Scanner)
    // =========================================================================

    /** Lit le choix du menu. Affiche un message et renvoie vide si la saisie n'est pas un entier. */
    private static OptionalInt lireChoix(Scanner scanner) {
        String saisie = scanner.nextLine().trim();
        try {
            return OptionalInt.of(Integer.parseInt(saisie));
        } catch (NumberFormatException e) {
            System.out.println("Saisie invalide. Veuillez entrer un nombre.");
            return OptionalInt.empty();
        }
    }

    /**
     * Lit un nombre décimal. Accepte le point ET la virgule (clavier français).
     * Affiche un message et renvoie vide si la saisie n'est pas un nombre.
     */
    private static OptionalDouble lireNombre(Scanner scanner, String invite) {
        System.out.print(invite);
        String saisie = scanner.nextLine().trim().replace(',', '.');
        try {
            double valeur = Double.parseDouble(saisie);
            if (Double.isFinite(valeur)) { // refuse "NaN" et "Infinity", que parseDouble accepte
                return OptionalDouble.of(valeur);
            }
        } catch (NumberFormatException e) {
            // traité comme une saisie invalide, juste en dessous
        }
        System.out.println("Saisie invalide. Veuillez entrer un nombre.");
        return OptionalDouble.empty();
    }

    /** Lit un texte non vide : redemande tant que la saisie est vide. */
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

    // =========================================================================
    // Affichage
    // =========================================================================

    private static void afficherMenu() {
        System.out.println();
        System.out.println("=== Annuaire des collaborateurs (EFREI) ===");
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
        System.out.println("0. Quitter");
        System.out.print("Votre choix : ");
    }

    /** Affiche une liste de collaborateurs. */
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
}