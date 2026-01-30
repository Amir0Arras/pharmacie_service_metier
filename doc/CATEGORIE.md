classDiagram
direction TB
class CATEGORIE {
   character varying(255) DESCRIPTION
   character varying(255) LIBELLE
   integer CODE
}
class COMMANDE {
   date ENVOYEELE
   numeric(18,2) PORT
   numeric(10,2) REMISE
   date SAISIELE
   character varying(5) DISPENSAIRE_CODE
   character varying(10) CODE_POSTAL
   character varying(15) PAYS
   character varying(15) REGION
   character varying(15) VILLE
   character varying(40) DESTINATAIRE
   character varying(60) ADRESSE
   integer NUMERO
}
class DISPENSAIRE {
   character varying(10) CODE_POSTAL
   character varying(15) PAYS
   character varying(15) REGION
   character varying(15) VILLE
   character varying(24) FAX
   character varying(24) TELEPHONE
   character varying(30) CONTACT
   character varying(30) FONCTION
   character varying(40) NOM
   character varying(60) ADRESSE
   character varying(5) CODE
}
class LIGNE {
   integer COMMANDE_NUMERO
   integer MEDICAMENT_REFERENCE
   integer QUANTITE
   integer ID
}
class MEDICAMENT {
   integer CATEGORIE_CODE
   integer FOURNISSEUR
   boolean INDISPONIBLE
   integer NIVEAU_DE_REAPPRO
   numeric(38,2) PRIX_UNITAIRE
   integer UNITES_COMMANDEES
   integer UNITES_EN_STOCK
   character varying(500) IMAGEURL
   character varying(255) NOM
   character varying(255) QUANTITE_PAR_UNITE
   integer REFERENCE
}

COMMANDE  -->  DISPENSAIRE : DISPENSAIRE_CODE:CODE
LIGNE  -->  COMMANDE : COMMANDE_NUMERO:NUMERO
LIGNE  -->  MEDICAMENT : MEDICAMENT_REFERENCE:REFERENCE
MEDICAMENT  -->  CATEGORIE : CATEGORIE_CODE:CODE
