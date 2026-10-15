
# TP4 – Durand & Mazzone
---


# Partie A — Fiabiliser l'application


## Mission 1 — Casser l'application

### Situation 1 : filtrer par salaire minimum

- **Action** : option 3, puis saisie d'un salaire minimum négatif (par exemple `-5`).
- **Comportement observé** : la valeur négative était acceptée comme un salaire minimum valide et la recherche s'exécutait normalement.
- **Comportement souhaité** : une erreur claire, car un salaire ne peut pas être négatif.
- **Correction** : `Annuaire.salaireAuMoins(...)` lève une `IllegalArgumentException`. `HelloEfrei` l'intercepte et affiche "Erreur : Le salaire minimum ne peut pas être négatif."

### Situation 2 : augmentation de salaire négative

- **Action** : option 9, identifiant `C001`, pourcentage négatif (par exemple `-30`).
- **Comportement observé** : `HelloEfrei` affichait un message de confirmation alors que rien n'avait changé : `Salaire : 42000.00 € -> 42000.00 €`. La méthode `Collaborateur.augmenterSalaire` ignorait silencieusement la valeur.
- **Comportement souhaité** : une erreur explicite, du type "Le pourcentage d'augmentation doit être strictement positif."
- **Correction** : `augmenterSalaire` lève une `IllegalArgumentException` si le pourcentage n'est pas strictement positif. `HelloEfrei` affiche "Erreur : Le pourcentage d'augmentation doit être strictement positif."

### À réfléchir : une erreur de saisie et une règle métier violée sont-elles le même problème ? Où les traiter ?

Non, ce ne sont pas les mêmes problèmes.

**L'erreur de saisie est un problème de forme.** La donnée est mal construite : texte à la place d'un nombre, valeur vide, identifiant qui n'a pas le format `C001`. L'application ne devrait même pas essayer de la traiter ni interroger la base avec. Elle se traite au plus près de l'utilisateur, dans l'interface (`HelloEfrei`), avant tout appel à la logique métier. La vue doit filtrer, valider le format et s'assurer que les types sont corrects avant même d'appeler les méthodes métiers. L'interface utilisateur doit s'assurer que l'utilisateur a bien saisi un nombre avant d'envoyer la donnée à la logique métier et non un `str` ou autre par exemple.

**La règle métier violée est un problème de contexte.** La donnée est bien formée et compréhensible, mais elle entre en conflit avec les règles de l'entreprise ou l'état du système : salaire négatif, augmentation négative, identifiant déjà utilisé, collaborateur introuvable. L'application refuse de l'exécuter pour préserver la cohérence de ses données. Ces règles sont garanties au cœur du domaine : `Collaborateur` (constructeur, `augmenterSalaire`) et `Annuaire` (`ajouter`, `salaireAuMoins`, `mettreAJourSalaire`) défendent leur propre intégrité, quel que soit l'appelant. Elles signalent la violation par une exception, et l'interface l'intercepte pour afficher un message à l'utilisateur.



## Mission 3 — Un projet que l'on sait reconstruire

### À réfléchir : pourquoi déclarer une bibliothèque dans `pom.xml` plutôt que copier un `.jar` ? Que se passe-t-il quand un camarade récupère le code ?

Déclarer une dépendance dans le `pom.xml` standardise la gestion des bibliothèques.

- Maven télécharge automatiquement la bonne version dans son dépôt local à la première compilation
- Il télécharge aussi les **dépendances transitives** : `hibernate-core` en entraîne plusieurs dizaines d'autres , qu'il serait impossible de gérer manuellement.
- Tout le monde travaille avec les mêmes versions.
- Le dépôt reste léger : aucun `.jar` n'est versionné.

Si quelqu’un récupère le code, Maven se charge de télécharger automatiquement toutes les bonnes versions des dépendances lors de la première compilation, au lieu de télécharger et lier manuellement les fichiers `.jar`



---



# Partie B — Persister


## Mission 5 — Alice refuse de disparaître

### Quelle différence entre objet Java, entité JPA et ligne d'une table ?

- **Objet Java** : instance d'une classe créée en mémoire avec `new`. Il n'existe que dans la RAM pendant l'exécution et disparaît à l'arrêt de l'application.
- **Entité JPA** : classe annotée avec `@Entity` et `@Id` (ou son instance), qui fait le pont entre le code Java et la base de données. Une instance gérée par un `EntityManager` correspond à une ligne précise, identifiée par sa clé primaire.
- **Ligne d'une table** : enregistrement physique dans la base SQL (colonnes `nom`, `prenom`...). Elle est stocké sur disque et survit à l'arrêt de l'application.

### Une seule table pour toute la hiérarchie, ou une table par classe ?

Nous avons choisi **une seule table** (`@Inheritance(strategy = SINGLE_TABLE)`). Une colonne discriminante, `type_collaborateur`, indique le type réel de chaque ligne (`PROGRAMMEUR` ou `TESTEUR`).

- Une seule table : Préférable car très rapide. Le filtre par salaire s'effectue par une simple requête `SELECT` sur une seule table, sans jointure.

- **Une table par classe** est plus complexe : avec `JOINED`, la recherche demande des jointures entre tables. Dans les deux cas, c'est plus lent et plus lourd.


### Arrêter la base et relancer : que voit-on ? Quel message pour l'utilisateur et pour le développeur ?

- **Constat** : la création de l'`EntityManagerFactory` échoue, donc l'application plante et renvoie une erreur de connexion JDBC.
- **Message pour le développeur** : La stack trace technique affichée dans la console IntelliJ, qui indique l'erreur réseau exacte et le port (3306).
- **Message pour l'utilisateur** : un message d'erreur simple, "Impossible de se connecter à la base de données. Vérifiez que MySQL est démarré dans XAMPP (port 3306)."



## Mission 6 — Alice entre dans la base

### Un identifiant inexistant (`C999`) : que retourne `find(...)` ?

Lorsqu'un identifiant n'existe pas dans la base de données (comme C999), la méthode `find(...)` ne lève pas d'exception : elle retourne `null`.

### À réfléchir : que vaut une clé comme `C001` ?

- **Le format "C + 3 chiffres" : combien de collaborateurs peut-on enregistrer ? Que se passe-t-il à C1000 ?**
On peut enregistrer 1000 collaborateurs au maximum (`C000` à `C999`). 
`C1000` dépasse le format prévu : il est refusé par le contrôle de format de l'interface, et le tri alphabétique est faux (`C1000` se place avant `C200`).

- **Qui décide du prochain identifiant ? Que se passe-t-il si deux utilisateurs créent en même temps le collaborateur C007 ?**
C'est l'utilisateur ou le code Java qui fabriquent le texte. En cas de création simultanée : Il y a un conflit d'accès concourant. Le premier utilisateur enregistre C007, et le second reçoit une erreur de violation de clé primaire (Duplicate entry / EntityExistsException).

- **Alice a été enregistrée par erreur sous C01 au lieu de C001. Si d’autres tables référencent cette clé, qu’est-ce que cela coûte de la corriger ?**
La clé primaire étant la référence utilisée comme clé étrangère dans les autres tables (comme la table des adresses), il faut modifier la clé dans la table principale et mettre à jour toutes les clés étrangères associées, dans le bon ordre. 

- **Persistez deux collaborateurs différents portant le même identifiant : que se passe-t-il, et à quel moment ?**
L'erreur apparaît  au commit, quand la base lève la violation de contrainte. JPA tente d'exécuter la requête SQL INSERT et MariaDB lève une exception de contrainte d'unicité.



## Mission 7 — Alice obtient une augmentation sans écrire `UPDATE`

- **Où est le SQL `UPDATE` ?** 
`Hibernate` le génère et l'exécute lui-même, au moment du flush déclenché par le commit.

- **Qu'est-ce qu'une entité gérée ?** 
Une instance associée au contexte de persistance d'un `EntityManager` ouvert (chargée par `find`, une requête ou `persist`). `Hibernate` garde une copie de son état d'origine.

- **Quel rôle joue le contexte de persistance ?** 
 Il sert de mémoire tampon et garantit une seule instance par identifiant. Il détecte surtout  automatiquement les changements : au flush, `Hibernate` compare l'état actuel des entités gérées à l'état d'origine et génère un `UPDATE` pour celles qui ont changé. Ce n'est donc pas un suivi en temps réel.

- **Pourquoi la transaction est-elle importante ?** 
 La transaction garantit l'intégrité des données et déclenche la synchronisation automatique entre les objets en mémoire et la base de données.



## Mission 8 — Retrouver autrement que par identifiant

### JPQL travaille-t-il sur tables/colonnes ou sur entités/attributs ?

Sur des **entités et des attributs**. 

Dans `SELECT * FROM collaborateur WHERE salaire > 45000`, 
`collaborateur` et `salaire` sont une table et une colonne. 

Dans `SELECT c FROM Collaborateur c WHERE c.salaire > :salaire`, 
`Collaborateur` est une classe Java et `c.salaire` un attribut, avec la casse Java. 

Hibernate traduit ensuite la requête en SQL (visible dans la console grâce à `show_sql`).



## Mission 9 — Alice a toujours une adresse

- **Un collaborateur a-t-il une ou plusieurs adresses ?** 
Un collaborateur a une seule adresse. 

- **Deux collaborateurs peuvent-ils partager la même ?** 
Non, chaque collaborateur possède son propre enregistrement d'adresse distinct dans le cadre de notre modèle. 

- **Une adresse existe-t-elle sans collaborateur ?**
Non, l'adresse est directement liée à l'existence du collaborateur.

- **Comparez avec la clé de `Collaborateur` : laquelle est la plus facile à faire vivre ?**
La clé auto-générée d'`Adresse` (`IDENTITY`) est la plus simple à gérer. Elle est prise en charge automatiquement par la base de données, évitant ainsi la gestion manuelle des doublons, les erreurs de saisie et les problèmes de mise à jour en cascade des clés étrangères.

- **Quelle table porte la clé étrangère `adresse_id` ? Cela correspond-il à l'analyse ?**
La table `collaborateur` porte la colonne `adresse_id`, qui pointe vers la clé primaire de `adresse`. Cela correspond à notre analyse : c'est le collaborateur qui possède la référence à son adresse.



---



# Partie C — Prendre du recul

## Mission 11 — Bilan

| Point | Où le vérifier |
|---|---|
| Le projet se construit avec Maven | `pom.xml` (slf4j, hibernate-core, mariadb-java-client), `mvn clean compile` |
| Règle métier protégée par une exception précise | `CollaborateurDejaExistantException`, `CollaborateurIntrouvableException`, `IllegalArgumentException` (salaire, pourcentage) |
| Exceptions non avalées | `Annuaire.ecrire` fait le rollback puis relance ; `HelloEfrei` intercepte des types précis |
| Diagnostic avec le debugger | Mission 4 |
| Logs restant techniques | SLF4J dans `Annuaire` ; les `System.out` de `HelloEfrei` sont des affichages utilisateur |
| Entité persistée puis retrouvée | Options 1 et 8 après un relancement |
| Modification d'entité gérée synchronisée | `mettreAJourSalaire`, `UPDATE` au commit |
| Requête JPQL | Sept requêtes de `Annuaire` (missions 8 et challenge C) |
| Relation avec `Adresse` | `@OneToOne` + `@JoinColumn(name = "adresse_id")` (mission 9) |
| Limites d'une clé comme `C001` | Mission 6 |
| `HelloEfrei` ne concentre pas la logique | Il valide les saisies et délègue à `Annuaire` ; le seul code JPA est la création de la fabrique |



---



# Challenges

- **Challenge C** : `Annuaire.programmeurs()` filtre en JPQL (`select p from Programmeur p`) plutôt qu'après chargement en Java. La base fait le tri grâce à la colonne discriminante : on ne charge pas tous les collaborateurs en mémoire pour en jeter la plupart, et cela reste efficace quand la table grossit. 