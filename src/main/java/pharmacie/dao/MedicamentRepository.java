package pharmacie.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import pharmacie.entity.Medicament;

// Cette interface sera auto-implémentée par Spring

public interface MedicamentRepository extends JpaRepository<Medicament, Integer> {
    /**
     * Calcule le nombre d'unités vendues pour chaque médicament d'une catégorie donnée.
     *
     * @param codeCategorie la catégorie à traiter
     * @return le nombre d'unités vendus pour chaque médicament,
     * sous la forme d'une liste de DTO UnitesParMedicament
     */
    @Query("""
        SELECT l.medicament.nom as nom, SUM(l.quantite) AS unites
        FROM Ligne l
        WHERE l.medicament.categorie.code = :codeCategorie
        GROUP BY nom
    """)
    List<UnitesParMedicament> medicamentsCommandesPour(Integer codeCategorie);

    /**
     * Calcule le nombre d'unités vendues pour chaque médicament d'une catégorie donnée.
     * Version SQL natif.
     *
     * @param codeCategorie la catégorie à traiter
     * @return le nombre d'unités vendus pour chaque médicament
     */
    @Query(value = """
        SELECT m.nom as nom, SUM(l.quantite) AS unites
        FROM Categorie c
        INNER JOIN Medicament m ON c.code = m.categorie_code
        INNER JOIN Ligne l ON m.reference = l.medicament_reference
        WHERE c.code = :codeCategorie
        GROUP BY m.nom
        """, nativeQuery = true)
    List<UnitesParMedicament> medicamentsCommandesPourNative(Integer codeCategorie);

    /**
     * Calcule le nombre d'unités vendues pour chaque médicament d'une catégorie donnée.
     * pas d'utilisation de DTO
     *
     * @param codeCategorie la catégorie à traiter
     * @return le nombre d'unités vendus pour chaque médicament,
     * sous la forme d'une liste de tableaux de valeurs non typées
     */
    @Query("""
        SELECT m.nom, SUM(li.quantite)
        FROM Categorie c
        JOIN c.medicaments m
        JOIN m.lignes li
        WHERE c.code = :codeCategorie
        GROUP BY m.nom
    """)
    List<Object> medicamentsCommandesPourV2(Integer codeCategorie);



    @Query("""
       SELECT m from Medicament m
       WHERE m.indisponible = false
       AND m.unitesEnStock > m.unitesCommandees
     """)
    List<Medicament> medicamentsDisponibles();

}
