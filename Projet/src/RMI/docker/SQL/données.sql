-- Table: RESTAURANT
CREATE TABLE RESTAURANT (
                            idrest INT PRIMARY KEY,
                            nom VARCHAR(255) NOT NULL,
                            adresse VARCHAR(255),
                            latitude DECIMAL(9, 6),
                            longitude DECIMAL(9, 6)
);

-- Table: PLAT
CREATE TABLE PLAT (
                      numplat INT PRIMARY KEY,
                      libelle VARCHAR(255) NOT NULL,
                      type VARCHAR(100)
);

-- Table: TABL
CREATE TABLE TABL (
                      idrest INT NOT NULL,
                      numtab INT NOT NULL,
                      nbplace INT NOT NULL,
                      PRIMARY KEY (idrest, numtab),
                      FOREIGN KEY (idrest) REFERENCES RESTAURANT(idrest)
);

-- Table: MENU
CREATE TABLE MENU (
                      idrest INT NOT NULL,
                      numplat INT NOT NULL,
                      prixplat DECIMAL(10, 2) NOT NULL,
                      PRIMARY KEY (idrest, numplat),
                      FOREIGN KEY (idrest) REFERENCES RESTAURANT(idrest),
                      FOREIGN KEY (numplat) REFERENCES PLAT(numplat)
);

CREATE TABLE RESERVATION (
                             numres INT PRIMARY KEY,
                             idrest INT NOT NULL,
                             numtab INT NOT NULL,
                             datres DATE NOT NULL,
                             nbpers INT NOT NULL,
                             nom VARCHAR(255),
                             prenom VARCHAR(255),
                             telephone VARCHAR(20),
                             FOREIGN KEY (idrest) REFERENCES RESTAURANT(idrest),
                             FOREIGN KEY (idrest, numtab) REFERENCES TABL(idrest, numtab)
);

INSERT INTO RESTAURANT (idrest, nom, adresse, latitude, longitude) VALUES
                                                                       (1, 'Le Bit Gourmet', '12 Rue des Octets, 54000 Nancy', 48.692054, 6.184417),
                                                                       (2, 'Chez Kernel', '27 Boulevard des Protocoles, 54000 Nancy', 48.691250, 6.180000),
                                                                       (3, 'La Table du Dev', '5 Rue du Code Source, 54000 Nancy', 48.693300, 6.178500);

INSERT INTO PLAT (numplat, libelle, type) VALUES
                                              (1, 'Spaghetti Binaire', 'Plat'),
                                              (2, 'Burger HTML', 'Plat'),
                                              (3, 'Tartare Algorithmique', 'Entrée'),
                                              (4, 'Crème Brûlée Java', 'Dessert'),
                                              (5, 'Pizza Python', 'Plat'),
                                              (6, 'Salade CSS', 'Entrée'),
                                              (7, 'Fondant C++', 'Dessert');

-- Le Bit Gourmet
INSERT INTO TABL (idrest, numtab, nbplace) VALUES
                                               (1, 1, 2),
                                               (1, 2, 4),
                                               (1, 3, 6);

-- Chez Kernel
INSERT INTO TABL (idrest, numtab, nbplace) VALUES
                                               (2, 1, 2),
                                               (2, 2, 4),
                                               (2, 3, 4);

-- La Table du Dev
INSERT INTO TABL (idrest, numtab, nbplace) VALUES
                                               (3, 1, 2),
                                               (3, 2, 4),
                                               (3, 3, 6);

-- Le Bit Gourmet
INSERT INTO MENU (idrest, numplat, prixplat) VALUES
                                                 (1, 1, 14.90),
                                                 (1, 4, 6.50),
                                                 (1, 6, 5.20);

-- Chez Kernel
INSERT INTO MENU (idrest, numplat, prixplat) VALUES
                                                 (2, 2, 13.50),
                                                 (2, 3, 7.00),
                                                 (2, 7, 6.00);

-- La Table du Dev
INSERT INTO MENU (idrest, numplat, prixplat) VALUES
                                                 (3, 1, 13.90),
                                                 (3, 5, 15.00),
                                                 (3, 6, 5.50),
                                                 (3, 7, 6.20);

INSERT INTO RESERVATION (numres, idrest, numtab, datres, nbpers, nom,prenom, telephone) VALUES
                                                                                     (1001, 1, 2, '2025-06-15 19:30:00', 4, 'Dupont','Alice' , '0612345678'),
                                                                                     (1002, 2, 1, '2025-06-16 12:00:00', 2, 'Martin','Bob', '0698765432'),
                                                                                     (1003, 3, 3, '2025-06-17 20:00:00', 5, 'Bernard','Claire', '0678945612');