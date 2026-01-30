package pharmacie.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import pharmacie.entity.Commande;

// This will be AUTO IMPLEMENTED by Spring into a Bean called CommandeRepository

public interface CommandeRepository extends JpaRepository<Commande, Integer> {

    /**
     * Trouve la liste des commandes à partir du nom du dispensaire.
     * Spring trouve tout seul la requête SQL !
     *
     * @param nom le nom du dispensaire
     * @return la liste des commandes passées par ce dispensaire
     * @see https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
     * @see https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#appendix.query.method.subject
     */
    List<Commande> findByDispensaireNom(String nom);

    @Query("select c from Commande c where c.numero = :numero")
    CommandeProjection findProjectionByNumero(Integer numero);

    /**
     * Trouve la liste des commandes en cours pour un dispensaire donné
     * @param codeDispensaire la clé du dispensaire
     * @return la liste des commandes en cours pour ce dispensaire
     */
    @Query("""
        select c from Commande c where
            c.envoyeele is null and
            c.dispensaire.code = :codeDispensaire
            order by c.numero desc
        """)
    List<Commande> commandesEnCoursPour(String codeDispensaire);

}
