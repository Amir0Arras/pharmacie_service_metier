package pharmacie.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import pharmacie.dao.CommandeRepository;
import pharmacie.dao.DispensaireRepository;
import pharmacie.dao.LigneRepository;
import pharmacie.dao.MedicamentRepository;
import pharmacie.entity.Commande;
import pharmacie.entity.Ligne;

@Slf4j
@Service
@Validated // Les annotations de validation sont actives sur les méthodes de ce service
// (ex: @Positive)
public class CommandeService {
    // La couche "Service" utilise la couche "Accès aux données" pour effectuer les traitements
    private final CommandeRepository commandeDao;
    private final DispensaireRepository dispensaireDao;
    private final LigneRepository ligneDao;
    private final MedicamentRepository medicamentDao;

    // @Autowired
    // Spring initialisera automatiquement ces paramètres
    public CommandeService(CommandeRepository commandeDao, DispensaireRepository dispensaireDao, LigneRepository ligneDao, MedicamentRepository medicamentDao) {
        this.commandeDao = commandeDao;
        this.dispensaireDao = dispensaireDao;
        this.ligneDao = ligneDao;
        this.medicamentDao = medicamentDao;
    }

    /**
     * Service métier : Enregistre une nouvelle commande pour un dispensaire connu par sa clé
     * Règles métier :
     * - le dispensaire doit exister
     * - On initialise l'adresse de livraison avec l'adresse du dispensaire
     * - Si le dispensaire a déjà commandé plus de 100 articles, on lui offre une remise de 15%
     *
     * @param dispensaireCode la clé du dispensaire
     * @return la commande créée
     * @throws java.util.NoSuchElementException si le dispensaire n'existe pas
     */
    @Transactional
    public Commande creerCommande(@NonNull String dispensaireCode) {
        log.info("Service : Création d'une commande pour {}", dispensaireCode);
        // On vérifie que le dispensaire existe
        var dispensaire = dispensaireDao.findById(dispensaireCode).orElseThrow();
        // On crée une commande pour ce dispensaire
        var nouvelleCommande = new Commande(dispensaire);
        // On initialise l'adresse de livraison avec l'adresse du dispensaire
        nouvelleCommande.setAdresseLivraison(dispensaire.getAdresse());
        // Si le dispensaire a déjà commandé plus de 100 médicaments, on lui offre une remise de 15%
        // La requête SQL nécessaire est définie dans l'interface DispensaireRepository
        var nbArticles = dispensaireDao.nombreArticlesCommandesPar(dispensaireCode);
        if (nbArticles > 100) {
            nouvelleCommande.setRemise(new BigDecimal("0.15"));
        }
        // On enregistre la commande (génère la clé)
        commandeDao.save(nouvelleCommande);
        return nouvelleCommande;
    }

    /**
     * <pre>
     * Service métier :
     * Enregistre une nouvelle ligne de commande pour une commande connue par sa clé,
     * Incrémente la quantité totale commandée (Medicament.unitesCommandees) avec la quantite à commander
     * Règles métier :
     * - le médicament référencé doit exister et ne pas être indisponible
     * - la commande doit exister
     * - la commande ne doit pas être déjà envoyée (le champ 'envoyeele' doit être null)
     * - la quantité doit être positive
     * - La quantité en stock du médicament ne doit pas être inférieure au total des quantités commandées
     * - Si le médicament est déjà présent dans la commande, les quantités sont additionnées
     * <pre>
     *
     * @param commandeNum la clé de la commande
     * @param medicamentRef  la clé du médicament
     * @param quantite    la quantité commandée (positive)
     * @return la ligne de commande créée
     * @throws java.util.NoSuchElementException                si la commande ou le
     *                                                         médicament n'existe pas
     * @throws IllegalStateException                           si il n'y a pas assez
     *                                                         de stock, si la
     *                                                         commande a déjà été
     *                                                         envoyée, ou si le
     *                                                         médicament est
     *                                                         indisponible
     * @throws jakarta.validation.ConstraintViolationException si la quantité n'est
     *                                                         pas positive
     */
    @Transactional
    public Ligne ajouterLigne(int commandeNum, int medicamentRef, @Positive int quantite) {
        // TODO : implémenter la méthode
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * <pre>
     * Service métier :
     * Supprime une ligne de commande pour une commande connue par sa clé,
     * Décrémente la quantité totale commandée (Medicament.unitesCommandees) de la quantité commandée
     * Règles métier :
     * - la commande ne doit pas être déjà envoyée (le champ 'envoyeele' doit être null)
     * <pre>
     *
     * @param id la clé de la ligne
     * @throws IllegalStateException si la commande a déjà été envoyée
     */
    @Transactional
    public void supprimerLigne(int id) {
        // TODO : implémenter la méthode
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Service métier : Enregistre l'expédition d'une commande connue par sa clé
     * Règles métier :
     * - la commande doit exister
     * - la commande ne doit pas être déjà envoyée (le champ 'envoyeele' doit être null)
     * - On renseigne la date d'expédition (envoyeele) avec la date du jour
     * - Pour chaque médicament dans les lignes de la commande :
     * décrémente la quantité en stock (Medicament.unitesEnStock) de la quantité dans la commande
     * décrémente la quantité commandée (Medicament.unitesCommandees) de la quantité dans la commande
     *
     * @param commandeNum la clé de la commande
     * @return la commande mise à jour
     * @throws java.util.NoSuchElementException si la commande n'existe pas
     * @throws IllegalStateException            si la commande a déjà été envoyée
     */
    @Transactional
    public Commande enregistreExpedition(int commandeNum) {
        // TODO : implémenter la méthode
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Service métier : Récupère une commande connue par sa clé
     *
     * @param commandeNum la clé de la commande
     * @return la commande
     * @throws java.util.NoSuchElementException si la commande n'existe pas
     */
    @Transactional
    public Commande getCommande(int commandeNum) {
        return commandeDao.findById(commandeNum).orElseThrow();
    }

    @Transactional
    public List<Commande> getCommandeEnCoursPour(String dispensaireCode) {
        return commandeDao.commandesEnCoursPour(dispensaireCode);
    }
}
