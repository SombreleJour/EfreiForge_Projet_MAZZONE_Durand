-- ============================================================================
-- EFREIForge : base de données de l'application (MariaDB / MySQL, XAMPP)
--
-- Ce script :
--   1. crée la base efreiforge si elle n'existe pas ;
--   2. supprime puis recrée les trois tables (il peut donc être rejoué sans erreur,
--      et remet le jeu de données à zéro à chaque exécution) ;
--   3. insère un jeu de données pour essayer l'application.
--
-- Les noms de tables et de colonnes correspondent au mapping JPA :
--   adresse, equipe, collaborateur (une seule table pour Programmeur et Testeur,
--   colonne discriminante type_collaborateur).
-- ============================================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS efreiforge CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE efreiforge;

-- Suppression : d'abord la table qui référence les deux autres
DROP TABLE IF EXISTS collaborateur;
DROP TABLE IF EXISTS equipe;
DROP TABLE IF EXISTS adresse;

CREATE TABLE adresse (
                         id         BIGINT       NOT NULL AUTO_INCREMENT,
                         rue        VARCHAR(100) NOT NULL,
                         codePostal VARCHAR(10)  NOT NULL,
                         ville      VARCHAR(50)  NOT NULL,
                         pays       VARCHAR(50)  NOT NULL,
                         PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Le nom d'équipe est obligatoire et unique (la casse est ignorée par la collation
-- utf8mb4_general_ci : « Alpha » et « alpha » sont le même nom pour la base aussi).
CREATE TABLE equipe (
                        id  BIGINT      NOT NULL AUTO_INCREMENT,
                        nom VARCHAR(50) NOT NULL,
                        PRIMARY KEY (id),
                        UNIQUE KEY uk_equipe_nom (nom)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE collaborateur (
                               identifiant        VARCHAR(10) NOT NULL,
                               type_collaborateur VARCHAR(20) NOT NULL,
                               prenom             VARCHAR(50) NOT NULL,
                               nom                VARCHAR(50) NOT NULL,
                               salaire            DOUBLE      NOT NULL,
                               langagePrefere     VARCHAR(30) NULL,
                               adresse_id         BIGINT      NOT NULL,
                               equipe_id          BIGINT      NULL,
                               PRIMARY KEY (identifiant),
                               UNIQUE KEY uk_collaborateur_adresse (adresse_id),
                               CONSTRAINT fk_collaborateur_adresse FOREIGN KEY (adresse_id) REFERENCES adresse (id),
                               CONSTRAINT fk_collaborateur_equipe  FOREIGN KEY (equipe_id)  REFERENCES equipe (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ----------------------------------------------------------------------------
-- Jeu de données
-- ----------------------------------------------------------------------------

-- Une adresse par collaborateur (relation 1-1)
INSERT INTO adresse (id, rue, codePostal, ville, pays) VALUES
                                                           (1,  '12 rue des Lilas',          '75017', 'Paris',              'France'),
                                                           (2,  '3 avenue de la République', '94800', 'Villejuif',          'France'),
                                                           (3,  '7 place Bellecour',         '69002', 'Lyon',               'France'),
                                                           (4,  '25 rue de la Paix',         '75002', 'Paris',              'France'),
                                                           (5,  '8 boulevard Gambetta',      '94120', 'Fontenay-sous-Bois', 'France'),
                                                           (6,  '14 rue Victor Hugo',        '93100', 'Montreuil',          'France'),
                                                           (7,  '5 cours Lafayette',         '69003', 'Lyon',               'France'),
                                                           (8,  '19 avenue Jean Jaurès',     '94800', 'Villejuif',          'France'),
                                                           (9,  '2 rue du Commerce',         '75015', 'Paris',              'France'),
                                                           (10, '31 rue Nationale',          '59800', 'Lille',              'France');

-- Deux équipes déjà constituées. Les noms « Alpha » et « Beta » restent libres :
-- ils servent aux scénarios de la recette (création, doublon de casse...).
INSERT INTO equipe (id, nom) VALUES
                                 (1, 'Plateforme'),
                                 (2, 'Qualité');

-- 10 collaborateurs : 3 dans « Plateforme », 3 dans « Qualité », 4 sans équipe
INSERT INTO collaborateur
(identifiant, type_collaborateur, prenom, nom, salaire, langagePrefere, adresse_id, equipe_id) VALUES
                                                                                                   ('C001', 'PROGRAMMEUR', 'Alice',  'Martin',  42000, 'Java',       1,  1),
                                                                                                   ('C002', 'TESTEUR',     'Bob',    'Dupont',  38000, NULL,         2,  2),
                                                                                                   ('C003', 'TESTEUR',     'Chloé',  'Bernard', 39000, NULL,         3,  NULL),
                                                                                                   ('C004', 'PROGRAMMEUR', 'David',  'Moreau',  45000, 'Python',     4,  1),
                                                                                                   ('C005', 'TESTEUR',     'Hugo',   'Petit',   40000, NULL,         5,  NULL),
                                                                                                   ('C006', 'PROGRAMMEUR', 'Lina',   'Moreau',  46000, 'Kotlin',     6,  NULL),
                                                                                                   ('C007', 'PROGRAMMEUR', 'Emma',   'Simon',   47000, 'JavaScript', 7,  1),
                                                                                                   ('C008', 'TESTEUR',     'Karim',  'Durand',  41000, NULL,         8,  2),
                                                                                                   ('C009', 'PROGRAMMEUR', 'Oscar',  'Michel',  44000, 'Java',       9,  NULL),
                                                                                                   ('C010', 'TESTEUR',     'Nina',   'Roux',    37500, NULL,         10, 2);