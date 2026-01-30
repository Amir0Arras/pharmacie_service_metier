package pharmacie.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pharmacie.entity.Commande;
import pharmacie.entity.Ligne;
import pharmacie.entity.Medicament;

import java.util.List;


// This will be AUTO IMPLEMENTED by Spring into a Bean called LigneRepository

public interface LigneRepository extends JpaRepository<Ligne, Integer> {
    List<Ligne> findByCommande(Commande commande);
    List<Ligne> findByMedicamentReference(Integer reference);
    List<Ligne> findByCommandeNumero(Integer numero);
    /**
     * On trouve au plus une ligne pour une commande donnée et un médicament donné
     * @param commande la commande cherchée
     * @param medicament le médicament cherché
     * @return la ligne correspondante (optionnelle)
     */
    Optional<Ligne> findByCommandeAndMedicament(Commande commande, Medicament medicament);
}
