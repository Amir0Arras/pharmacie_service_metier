package pharmacie.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import pharmacie.dao.CommandeRepository;
import pharmacie.dao.DispensaireRepository;
import pharmacie.dao.LigneRepository;
import pharmacie.dao.MedicamentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
 // Ce test est basé sur le jeu de données dans "test_data.sql"
class CreationCommandeTest {
    private static final String ID_PETIT_CLIENT = "0COM";
    private static final String ID_GROS_CLIENT = "2COM";
    private static final BigDecimal REMISE_POUR_GROS_CLIENT = new BigDecimal("0.15");
    private static final int COMMANDE_EN_COURS = 99998;
    private static final int COMMANDE_DEJA_ENVOYEE = 99999;
    private static final int MED_DISPONIBLE = 93;
    private static final int MED_DISPONIBLE_AVEC_RESERVE = 98;
    private static final int MED_INDISPONIBLE = 97;

    @Autowired
    private CommandeService service;
    @Autowired
    private DispensaireRepository daoClient;
    @Autowired
    private CommandeRepository commandeDao;
    @Autowired
    private MedicamentRepository medicamentDao;
    @Autowired
    private LigneRepository ligneDao;

    @Test
    void testCreerCommandePourGrosClient() {
        var commande = service.creerCommande(ID_GROS_CLIENT);
        assertNotNull(commande.getNumero(), "On doit avoir la clé de la commande");
        assertEquals(REMISE_POUR_GROS_CLIENT, commande.getRemise(),
            "Une remise de 15% doit être appliquée pour les gros clients");
    }

    @Test
    void testCreerCommandePourPetitClient() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        assertNotNull(commande.getNumero());
        assertEquals(BigDecimal.ZERO, commande.getRemise(),
            "Aucune remise ne doit être appliquée pour les petits clients");
    }

    @Test
    void testCreerCommandeInitialiseAdresseLivraison() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        var client = daoClient.findById(ID_PETIT_CLIENT).orElseThrow();
        assertEquals(client.getAdresse(), commande.getAdresseLivraison(),
            "On doit recopier l'adresse du client dans l'adresse de livraison");
    }

    // ==================== Tests pour ajouterLigne ====================

    @Test
    void testAjouterLigneSuccessful() {
        var ligne = service.ajouterLigne(COMMANDE_EN_COURS, MED_DISPONIBLE, 10);
        assertNotNull(ligne.getId(), "La ligne doit avoir une clé générée");
        assertEquals(10, ligne.getQuantite(), "La quantité doit être 10");
        
        var med = medicamentDao.findById(MED_DISPONIBLE).orElseThrow();
        assertEquals(10, med.getUnitesCommandees(), 
            "Les unités commandées doivent être incrémentées de 10");
    }

    @Test
    void testAjouterLigneCommandeDejaSaisieThrows() {
        assertThrows(IllegalStateException.class,
            () -> service.ajouterLigne(COMMANDE_DEJA_ENVOYEE, MED_DISPONIBLE, 5),
            "Impossible d'ajouter une ligne à une commande déjà expédiée");
    }

    @Test
    void testAjouterLigneMedicamentIndisponibleThrows() {
        assertThrows(IllegalStateException.class,
            () -> service.ajouterLigne(COMMANDE_EN_COURS, MED_INDISPONIBLE, 5),
            "Impossible d'ajouter un médicament indisponible");
    }

    @Test
    void testAjouterLigneStockInsuffisantThrows() {
        assertThrows(IllegalStateException.class,
            () -> service.ajouterLigne(COMMANDE_EN_COURS, MED_DISPONIBLE_AVEC_RESERVE, 100),
            "Pas assez de stock: en stock=26, déjà commandé=20, demandé=100");
    }

   
    // ==================== Tests pour supprimerLigne ====================

    @Test
    void testSupprimerLigneSuccessful() {
        // Créer une ligne
        var ligne = service.ajouterLigne(COMMANDE_EN_COURS, MED_DISPONIBLE, 15);
        int ligneId = ligne.getId();
        var med = medicamentDao.findById(MED_DISPONIBLE).orElseThrow();
        var unitesAvant = med.getUnitesCommandees();
        
        // Supprimer la ligne
        service.supprimerLigne(ligneId);
        
        // Vérifier que la ligne est supprimée
        assertTrue(ligneDao.findById(ligneId).isEmpty(),
            "La ligne doit être supprimée");
        
        // Vérifier que les unités commandées sont décrémentées
        med = medicamentDao.findById(MED_DISPONIBLE).orElseThrow();
        assertEquals(unitesAvant - 15, med.getUnitesCommandees(),
            "Les unités commandées doivent être décrémentées de 15");
    }

    @Test
    void testSupprimerLigneCommandeDejaSaisieThrows() {
        // La commande 99999 a des lignes et est déjà envoyée
        var lignes = ligneDao.findByCommandeNumero(COMMANDE_DEJA_ENVOYEE);
        assertTrue(lignes.size() > 0, "La commande doit avoir au moins une ligne");
        int ligneId = lignes.get(0).getId();
        
        assertThrows(IllegalStateException.class,
            () -> service.supprimerLigne(ligneId),
            "Impossible de supprimer une ligne d'une commande déjà expédiée");
    }

    // ==================== Tests pour enregistreExpedition ====================

    @Test
    void testEnregistreExpeditionSuccessful() {
        var commande = commandeDao.findById(COMMANDE_EN_COURS).orElseThrow();
        assertNull(commande.getEnvoyeele(), "La commande ne doit pas être expédiée au départ");
        
        var commandeExpediee = service.enregistreExpedition(COMMANDE_EN_COURS);
        
        assertNotNull(commandeExpediee.getEnvoyeele(),
            "La date d'expédition doit être renseignée");
        assertEquals(LocalDate.now(), commandeExpediee.getEnvoyeele(),
            "La date d'expédition doit être aujourd'hui");
    }

    @Test
    void testEnregistreExpeditionDecrementerStock() {
        // Créer une nouvelle commande pour isoler ce test
        var nouvelleCommande = service.creerCommande(ID_GROS_CLIENT);
        int commandeId = nouvelleCommande.getNumero();
        
        // Ajouter une ligne de commande
        int qtyAjoutee = 1;
        service.ajouterLigne(commandeId, MED_DISPONIBLE_AVEC_RESERVE, qtyAjoutee);
        
        var med98 = medicamentDao.findById(MED_DISPONIBLE_AVEC_RESERVE).orElseThrow();
        var stockAvant = med98.getUnitesEnStock();
        var commandeesAvant = med98.getUnitesCommandees();
        
        // Expédier la commande
        service.enregistreExpedition(commandeId);
        
        // Vérifier que le stock et les unités commandées ont été décrémentés
        med98 = medicamentDao.findById(MED_DISPONIBLE_AVEC_RESERVE).orElseThrow();
        assertEquals(stockAvant - qtyAjoutee, med98.getUnitesEnStock(),
            "Le stock doit être décrémenté de la quantité commandée");
        assertEquals(commandeesAvant - qtyAjoutee, med98.getUnitesCommandees(),
            "Les unités commandées doivent être décrémentées");
    }

    @Test
    void testEnregistreExpeditionCommandeDejaSaisieThrows() {
        assertThrows(IllegalStateException.class,
            () -> service.enregistreExpedition(COMMANDE_DEJA_ENVOYEE),
            "Impossible d'expédier une commande déjà expédiée");
    }
}
