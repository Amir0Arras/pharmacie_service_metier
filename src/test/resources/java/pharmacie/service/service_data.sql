-- Mettre ici les données qu'on veut charger avant un test spécifique
-- en utilisant l'annotation @SQL("nom_du_fichier.sql")
INSERT INTO Dispensaire(code, nom, contact, fonction, adresse, ville, region, code_postal, pays, telephone, fax) VALUES
    ( 'XCOM', 'Ce dispensaire n''a pas de commande', 'Maria Anders', 'Représentant(e)', 'Obere Str. 57', 'Berlin', NULL, '12209', 'Allemagne', '030-0074321', '030-0076545');
