package pharmacie.dao;

/**
 * Utilisé pour représenter le résultat des requêtes statistiques
 * @see pharmacie.dao.MedicamentRepository
 * Cette interface sera auto-implémentée par Spring
 */
public interface UnitesParMedicament {
	String getNom();
	Long getUnites();
}
