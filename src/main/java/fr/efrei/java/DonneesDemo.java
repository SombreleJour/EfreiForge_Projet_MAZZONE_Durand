package fr.efrei.java;

import fr.efrei.java.model.Adresse;
import fr.efrei.java.model.Collaborateur;
import fr.efrei.java.model.Programmeur;
import fr.efrei.java.model.Testeur;

import java.util.ArrayList;
import java.util.List;

/**
 * Fournit un jeu de données de démonstration : 20 collaborateurs (C001–C020).
 * Classe utilitaire — constructeur privé, uniquement des méthodes statiques.
 *
 * Chaque collaborateur reçoit sa PROPRE instance d'Adresse (relation 1-1) :
 * une même adresse ne doit jamais être partagée entre deux collaborateurs.
 */
public class DonneesDemo {

    private DonneesDemo() { }

    /**
     * Crée et retourne une liste de 20 collaborateurs répartis sur 3 sites.
     * Mix de Programmeur et Testeur, avec des langages variés.
     */
    public static List<Collaborateur> creerCollaborateurs() {

        List<Collaborateur> liste = new ArrayList<>();

        liste.add(new Programmeur("C001", "Alice",   "Martin",   42000, adresseParis(),     "Java"));
        liste.add(new Testeur(    "C002", "Bob",     "Dupont",   38000, adresseParis()));
        liste.add(new Programmeur("C003", "Clara",   "Bernard",  45000, adresseVillejuif(), "Python"));
        liste.add(new Testeur(    "C004", "David",   "Moreau",   39000, adresseVillejuif()));
        liste.add(new Programmeur("C005", "Emma",    "Simon",    47000, adresseLyon(),      "JavaScript"));
        liste.add(new Testeur(    "C006", "François","Laurent",  36000, adresseParis()));
        liste.add(new Programmeur("C007", "Grace",   "Thomas",   50000, adresseParis(),     "Java"));
        liste.add(new Testeur(    "C008", "Hugo",    "Petit",    40000, adresseLyon()));
        liste.add(new Programmeur("C009", "Iris",    "Robert",   43000, adresseVillejuif(), "Python"));
        liste.add(new Testeur(    "C010", "Julien",  "Richard",  37000, adresseParis()));
        liste.add(new Programmeur("C011", "Karim",   "Durand",   48000, adresseParis(),     "TypeScript"));
        liste.add(new Testeur(    "C012", "Laura",   "Leroy",    41000, adresseVillejuif()));
        liste.add(new Programmeur("C013", "Marc",    "Moreau",   46000, adresseLyon(),      "Java"));
        liste.add(new Testeur(    "C014", "Nina",    "Simon",    38500, adresseParis()));
        liste.add(new Programmeur("C015", "Oscar",   "Michel",   44000, adresseParis(),     "Kotlin"));
        liste.add(new Testeur(    "C016", "Pauline", "Garcia",   39500, adresseLyon()));
        liste.add(new Programmeur("C017", "Quentin", "Martinez", 52000, adresseVillejuif(), "Java"));
        liste.add(new Testeur(    "C018", "Rachel",  "Dubois",   40000, adresseParis()));
        liste.add(new Programmeur("C019", "Samuel",  "Lopez",    49000, adresseParis(),     "Scala"));
        liste.add(new Testeur(    "C020", "Théo",    "Gonzalez", 37500, adresseLyon()));

        return liste;
    }

    // Chaque appel crée une NOUVELLE adresse (jamais partagée entre collaborateurs).

    private static Adresse adresseParis() {
        return new Adresse("12 rue des Lilas", "75000", "Paris", "France");
    }

    private static Adresse adresseVillejuif() {
        return new Adresse("3 avenue de la République", "94800", "Villejuif", "France");
    }

    private static Adresse adresseLyon() {
        return new Adresse("7 place Bellecour", "69002", "Lyon", "France");
    }
}