# Journal de bord — EFREIForge

Projet Java LSI1 —  MAZZONE Milo / DURAND Tom
https://github.com/SombreleJour/EfreiForge_Projet_MAZZONE_Durand.git

## 1. Démarche
---

- **Organisation du travail :** nous sommes partis d'une copie de notre TP4, sur un dépôt Git partagé, chacun poussant ses modifications et récupérant celles de l'autre. Nous avons découpé le projet par couche : le modèle et l'accès aux données (entités JPA, services, script SQL) d'un côté, l'interface console de l'autre, puis nous avons rejoué ensemble les scénarios de recette.
- **Éléments du TP4 réutilisés :**
  - l'entité abstraite `Collaborateur` et ses sous-classes `Programmeur` et `Testeur` (héritage JPA `SINGLE_TABLE`, colonne `type_collaborateur`) ;
  - l'entité `Adresse`, liée à chaque collaborateur par un `@OneToOne` avec `CascadeType.PERSIST` ;
  - le service `Annuaire` (recherches et tris en JPQL, ajout, augmentation de salaire) et ses exceptions `CollaborateurDejaExistantException` et `CollaborateurIntrouvableException` ;
  - le `persistence.xml` et le menu console, que nous avons découpé en deux sous-menus (Collaborateurs / Équipes).
- **Ajouts du projet :** l'entité `Equipe`, l'association `@ManyToOne` **unidirectionnelle** de `Collaborateur` vers `Equipe` (colonne `equipe_id`, aucune liste dans `Equipe`), le service `GestionEquipes` et quatre exceptions métier (`EquipeDejaExistanteException`, `EquipeIntrouvableException`, `EquipeNonVideException`, `AffectationRefuseeException`).

## 2. Deux décisions techniques

**1. Structuration du projet en sous-paquetages fonctionnels (Clean Architecture) :**

- *Choix réalisé :* Refactorisation complète de l'arborescence du projet sous fr.efrei.java en répartissant les classes dans trois sous-paquets distincts : model (entités JPA), service (logique métier et transactions) et exceptions (exceptions personnalisées).
- *Raison du choix :* Cette séparation des responsabilités évite d'avoir une quinzaine de fichiers à la racine du paquetage principal. Elle améliore la lisibilité globale, facilite la maintenance et offre une structure logicielle conforme aux standards professionnels Java/JEE.

**2. Filtrage strict du logging Hibernate et masquage du SQL dans la console :**

- *Choix réalisé :* Désactivation des propriétés hibernate.show_sql dans persistence.xml et création d'un fichier efreiforge.log pour isoler les logs SLF4J
- *Raison du choix :* Au démarrage de JPA, Hibernate génère une quantité importante de logs d'information (INFO/WARN en rouge dans la console) ainsi que le détail de toutes les requêtes SQL exécutées. Ce bruit visuel altérait la lisibilité de l'interface console. Le filtrage permet d'offrir une expérience utilisateur propre et ergonomique sans polluer l'écran d'affichage.

## 3. État du projet

- **Ce qui fonctionne :** toutes les opérations demandées (créer, consulter, renommer, supprimer une équipe ; affecter, retirer, afficher les membres ; lister les collaborateurs sans équipe), en plus des fonctionnalités du TP4. Les équipes et affectations sont conservées en base entre deux lancements

**Ce qui reste incomplet / limites :** Aucun point sauf les questions bonus. L'ensemble des exigences du cahier des charges et des fonctionnalités du Projet a été traité et validé.

**Difficultés significatives :**
  - Comprendre quand JPA enregistre une modification : une entité chargée dans la transaction est synchronisée au commit, un `merge` est inutile ;
  - Gestion propre des exceptions en cascade lors des manipulations d'équipes non vides, et résolution des problèmes de synchronisation des modifications du persistence.xml entre les environnements locaux du binôme (résolu via recompilation Maven mvn clean compile).

### Tableau de recette

| N°  | Scénario                                                                                                                                                  | Résultat attendu                                                                                                                      | Constaté                  |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- | ------------------------- |
| 1   | Créer l'équipe « Alpha », puis « alpha », puis une équipe sans nom (vide ou espaces).                                                                     | La première est créée ; les deux autres sont refusées.                                                                                | Comme le résultat attendu |
| 2   | Affecter à « Alpha » un collaborateur sans équipe, puis afficher « Alpha ».                                                                               | Le collaborateur apparaît dans l'équipe.                                                                                              | Comme le résultat attendu |
| 3   | Affecter ce même collaborateur à une autre équipe, puis de nouveau à « Alpha ».                                                                           | Deux refus ; il reste dans « Alpha ».                                                                                                 | Comme le résultat attendu |
| 4   | Retirer d'« Alpha » un collaborateur qui n'en fait pas partie (d'abord un sans équipe, puis un membre d'une autre équipe).                                | Deux refus, aucune donnée modifiée.                                                                                                   | Comme le résultat attendu |
| 5   | Afficher les collaborateurs sans équipe.                                                                                                                  | Le collaborateur affecté n'y figure pas ; les autres y sont.                                                                          | Comme le résultat attendu |
| 6   | Arrêter puis relancer l'application, consulter équipes et affectations.                                                                                   | Tout est identique.                                                                                                                   | Comme le résultat attendu |
| 7   | Renommer une équipe : changer seulement la casse de son nom, puis prendre le nom d'une autre équipe. Supprimer une équipe non vide, puis une équipe vide. | Casse seule : acceptée ; nom déjà pris : refusé. Suppression refusée tant que l'équipe contient des collaborateurs, acceptée ensuite. | Comme le résultat attendu |
| 8   | Utiliser une équipe ou un collaborateur inexistant ; saisir du texte quand un nombre est attendu.                                                         | Message clair, l'application ne plante pas.                                                                                           | Comme le résultat attendu |

## 4. README — Lancer et essayer l'application

**Prérequis**

- JDK 17 ou supérieur
- Apache Maven 3.8+ (ou IntelliJ IDEA)
- XAMPP avec MySQL/MariaDB démarré sur le port 3306 (utilisateur `root`, sans mot de passe)

**Préparer la base**

1. Dans le panneau XAMPP, démarrer **MySQL**.
2. Ouvrir phpMyAdmin (http://localhost/phpmyadmin), onglet **Importer**, choisir `sql/efreiforge.sql` puis **Importer**.

Le script crée lui-même la base `efreiforge` et ses tables : inutile de la créer avant. Il peut être rejoué : il supprime puis recrée les tables, ce qui remet le jeu de données à zéro (10 collaborateurs, équipes « Plateforme » et « Qualité », 4 collaborateurs sans équipe : C003, C005, C006, C009).

**Démarrage de l'application**

À la racine du projet :

```
mvn clean compile exec:java -Dexec.mainClass="fr.efrei.java.HelloEfrei"
```

Ou lancer la méthode `main` de `HelloEfrei.java` depuis IntelliJ IDEA. Les traces techniques sont écrites dans le fichier `efreiforge.log`, pour garder la console lisible.

**Quelques manipulations pour vérifier le résultat**

1. Menu 2 (Équipes) > 2 : on voit « Plateforme » et « Qualité » avec 3 collaborateurs chacune.
2. Menu 2 > 8 : la liste des collaborateurs sans équipe affiche C003, C005, C006 et C009.
3. Menu 2 > 1 : créer « Alpha », puis tenter « alpha » (refusé).
4. Menu 2 > 3 : affecter C003 à « Alpha », puis menu 2 > 5 pour afficher « Alpha ».
5. Quitter (0), relancer l'application et vérifier que « Alpha » et l'affectation de C003 sont toujours là.



## 5. Contribution de chacun

- **MAZZONE Milo :** reprise du TP4 et mise en place du projet (`pom.xml`, `persistence.xml` vers la base `efreiforge`), classe mère `AccesJpa` et adaptation d'`Annuaire`, script SQL et jeu de données, ajout des opérations sur les équipes dans le menu de `HelloEfrei`, règles de refus dans `affecterA` / `retirerDe` (`AffectationRefuseeException`, `EquipeNonVideException`), corrections de `GestionEquipes` (renommage avec changement de casse, suppression d'une équipe non vide), nettoyage final (fichiers du TP inutiles, paquet `exceptions`, validation du nom d'équipe, logs dans `efreiforge.log`) et rédaction du journal.

- **DURAND Tom :** création de l'entité `Equipe` et de l'association `@ManyToOne` dans `Collaborateur` (avec la déclaration dans `persistence.xml`), première version de `affecterA` / `retirerDe` et de `GestionEquipes` (création, liste, renommage, suppression, affectation, retrait), exceptions des équipes, réorganisation en paquets (`model`, `service`, exceptions), découpage de l'interface console en deux sous-menus (Collaborateurs / Équipes), masquage du SQL et premier filtrage des logs Hibernate.
