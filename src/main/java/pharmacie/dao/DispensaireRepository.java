package pharmacie.dao;

import org.springframework.data.jpa.repository.Query;

import pharmacie.entity.Dispensaire;

import org.springframework.data.jpa.repository.JpaRepository;

// This will be AUTO IMPLEMENTED by Spring into a Bean called DispensaireRepository
// CRUD refers Create, Read, Update, Delete

public interface DispensaireRepository extends JpaRepository<Dispensaire, String> {
    /**
     * Calcule le nombre d'articles commandés par un dispensaire
     * @param dispensaireCode la clé du dispensaire
     */
    // Attention : SUM peut renvoyer NULL si on ne trouve pas d'enregistrement
    // On utilise COALESCE pour renvoyer 0 dans ce cas
    // http://www.h2database.com/html/functions.html#coalesce
    @Query("""
        SELECT COALESCE(SUM(l.quantite), 0)
        FROM Ligne l
        WHERE l.commande.dispensaire.code = :dispensaireCode
        AND l.commande.envoyeele IS NOT NULL
    """)
    int nombreArticlesCommandesPar(String dispensaireCode);
}
