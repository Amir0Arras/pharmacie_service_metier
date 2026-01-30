package pharmacie.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import pharmacie.entity.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Un jeu de tests vérifiant l'intégrité des données dans la base de données
 * Note : Les contraintes d'intégrité au moment où on enregistre les objets dans la BD.
 * Pour forcer l'enregistrement, on utilise saveAndFlush() au lieu de save(), deleteAndFlush() au lieu de delete()
 */
@DataJpaTest
class RepositoryIntegrityTest {

    @Autowired
    private CategorieRepository categorieRepository;
    @Autowired
    private MedicamentRepository medicamentRepository;
    @Autowired
    private DispensaireRepository dispensaireRepository;
    @Autowired
    private CommandeRepository commandeRepository;
    @Autowired
    private LigneRepository ligneRepository;

    @Test
    void unMedicamentSansCategorieEstInterdit() {
        // Try to save Medicament without Categorie
        Medicament m = new Medicament();
        m.setNom("Doliprane");

        assertThrows(DataIntegrityViolationException.class, () -> {
            medicamentRepository.saveAndFlush(m);
        }, "Should not save Medicament without Categorie");
    }

    @Test
    void detruireLaCategorieSupprimeSesMedicaments() {
        // Cascade Delete Categorie -> Medicament
        Categorie c = new Categorie();
        c.setLibelle("Antalgiques");

        Medicament m = new Medicament();
        m.setNom("Doliprane");
        m.setCategorie(c);

        // Use parent cascade
        c.getMedicaments().add(m);

        c = categorieRepository.saveAndFlush(c);
        m = c.getMedicaments().get(0);

        assertNotNull(m.getReference(), "Medicament should be saved via cascade");

        categorieRepository.delete(c);
        categorieRepository.flush();

        Optional<Medicament> found = medicamentRepository.findById(m.getReference());
        assertFalse(found.isPresent(), "Medicament should be deleted when Categorie is deleted");
    }

    @Test
    void uneCommandeSansDispensaireEstInterdite() {
        // Try to save Commande without Dispensaire
        Commande commande = new Commande();
        commande.setDestinataire("Mr Smith");

        assertThrows(DataIntegrityViolationException.class, () -> {
            commandeRepository.saveAndFlush(commande);
        }, "Should not save Commande without Dispensaire");
    }

    @Test
    void detruireLeDispensaireSupprimeSesCommandes() {
        // Cascade Delete Dispensaire -> Commande
        Dispensaire d = new Dispensaire();
        d.setCode("D001");
        d.setNom("Dispensaire Central");

        Commande commande = new Commande();
        commande.setDestinataire("Mr Smith");
        commande.setDispensaire(d);

        // Use parent cascade
        d.getCommandes().add(commande);

        d = dispensaireRepository.saveAndFlush(d);
        commande = d.getCommandes().get(0);

        assertNotNull(commande.getNumero(), "Commande should be saved via cascade");

        dispensaireRepository.delete(d);
        dispensaireRepository.flush();

        Optional<Commande> found = commandeRepository.findById(commande.getNumero());
        assertFalse(found.isPresent(), "Commande should be deleted when Dispensaire is deleted");
    }

    @Test
    void uneLigneSansCommandeOuMedicamentEstInterdite() {
        // Try to save Ligne without Commande/Medicament
        Ligne l1 = new Ligne();
        l1.setQuantite(10);

        assertThrows(DataIntegrityViolationException.class, () -> {
            ligneRepository.saveAndFlush(l1);
        });
    }

    @Test
    void medicamentDupliqueDansUneMemeCommandeEstInterdit() {
        // Setup via repositories preferably
        Categorie cat = new Categorie();
        cat.setLibelle("Test Cat");
        cat = categorieRepository.saveAndFlush(cat);

        Medicament med = new Medicament();
        med.setNom("Test Med");
        med.setCategorie(cat);
        med = medicamentRepository.saveAndFlush(med);

        Dispensaire d = new Dispensaire();
        d.setCode("D002");
        d.setNom("Test Disp");
        d = dispensaireRepository.saveAndFlush(d);

        Commande cmd = new Commande();
        cmd.setDispensaire(d);
        cmd = commandeRepository.saveAndFlush(cmd);

        // Unique Constraint (Commande, Medicament)
        Ligne l2 = new Ligne();
        l2.setCommande(cmd);
        l2.setMedicament(med);
        l2.setQuantite(5);
        ligneRepository.saveAndFlush(l2);

        Ligne l3Duplicate = new Ligne();
        l3Duplicate.setCommande(cmd);
        l3Duplicate.setMedicament(med);
        l3Duplicate.setQuantite(2);

        assertThrows(DataIntegrityViolationException.class, () -> {
            ligneRepository.saveAndFlush(l3Duplicate);
        }, "Should not allow duplicate lines for same Commande and Medicament");
    }

    @Test
    void testOrphanRemovalLigne() {
        // Setup
        Categorie cat = new Categorie();
        cat.setLibelle("Orphan Cat");
        cat = categorieRepository.saveAndFlush(cat);

        Medicament med = new Medicament();
        med.setNom("Orphan Med");
        med.setCategorie(cat);
        med = medicamentRepository.saveAndFlush(med);

        Dispensaire d = new Dispensaire();
        d.setCode("D003");
        d.setNom("Orphan Disp");
        d = dispensaireRepository.saveAndFlush(d);

        Commande cmd = new Commande();
        cmd.setDispensaire(d);

        Ligne l = new Ligne();
        l.setCommande(cmd);
        l.setMedicament(med);
        l.setQuantite(1);

        // Add to collection
        cmd.getLignes().add(l);

        cmd = commandeRepository.saveAndFlush(cmd);

        Ligne persistedLigne = cmd.getLignes().get(0);
        Integer ligneId = persistedLigne.getId();
        assertNotNull(ligneId);

        // Remove from collection
        cmd.getLignes().remove(persistedLigne);

        commandeRepository.saveAndFlush(cmd);

        Optional<Ligne> found = ligneRepository.findById(ligneId);
        assertFalse(found.isPresent(), "Ligne should be deleted when removed from Commande's list (orphanRemoval)");
    }

    @Test
    void neDetruitPasUneCategorieSiSesMedicamentsSontCommandes() {
        Categorie c = categorieRepository.findById(98).orElseThrow();
        // La catégorie avec code 98 dans le jeu de données de test a 7 médicaments

        int combienDeMedicaments = c.getMedicaments().size();
        assertEquals(7,combienDeMedicaments, "Categorie with code 98 should have 7 Medicaments");

        try {
            categorieRepository.delete(c);
            categorieRepository.flush();
            fail("Should not allow deletion of Categorie if its Medicaments are referenced in Commandes");
        } catch (DataIntegrityViolationException e) {
            // Expected exception
        }
    }
}
