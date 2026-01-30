package pharmacie.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * On définit une projection pour transmettre des informations sur une commande et ses lignes de commande.
 */
public interface CommandeProjection {
    // Pour les commandes on ne garde que le numéro et la date de saisie
    Integer getNumero();
    LocalDate getSaisiele();

    // Pour le client on ne garde que la société et le nom du contact
    interface ClientProjection {
        String getSociete();
        String getContact();
    }

    ClientProjection getClient();

    // Pour les lignes on ne garde que la quantité et le produit
    interface LigneProjection {
        Integer getQuantite();
        ProduitProjection getProduit();
    }

    List<LigneProjection> getLignes();

    // Pour les produits on ne garde que le nom et le prix unitaire
    interface ProduitProjection {
        String getNom();
        BigDecimal getPrixUnitaire();
    }
}
