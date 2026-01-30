package pharmacie.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class IntegrityConstraintTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testMedicamentCategorieOptionality() {
        // Try to save Medicament without Categorie
        Medicament m = new Medicament();
        m.setNom("Doliprane");
        
        assertThrows(jakarta.persistence.PersistenceException.class, () -> {
            entityManager.persistAndFlush(m);
        }, "Should not save Medicament without Categorie");
    }

    @Test
    public void testMedicamentCategorieCascade() {
        // Cascade Delete Categorie -> Medicament
        Categorie c = new Categorie();
        c.setLibelle("Antalgiques");
        
        Medicament m = new Medicament();
        m.setNom("Doliprane");
        m.setCategorie(c);
        
        // Use parent cascade
        c.getMedicaments().add(m);
        
        c = entityManager.persistAndFlush(c);
        // Refresh to get the generated reference of m? 
        // Or assume c.getMedicaments().get(0) is managed.
        m = c.getMedicaments().get(0);

        assertNotNull(m.getReference(), "Medicament should be saved via cascade");

        entityManager.remove(c);
        entityManager.flush();
        entityManager.clear();

        assertNull(entityManager.find(Medicament.class, m.getReference()), 
            "Medicament should be deleted when Categorie is deleted");
    }

    @Test
    public void testCommandeDispensaireOptionality() {
        // Try to save Commande without Dispensaire
        Commande commande = new Commande();
        commande.setDestinataire("Mr Smith");

        assertThrows(jakarta.persistence.PersistenceException.class, () -> {
            entityManager.persistAndFlush(commande);
        }, "Should not save Commande without Dispensaire");
    }

    @Test
    public void testCommandeDispensaireCascade() {
        // Cascade Delete Dispensaire -> Commande
        Dispensaire d = new Dispensaire();
        d.setCode("D001");
        d.setNom("Dispensaire Central");

        Commande commande = new Commande();
        commande.setDestinataire("Mr Smith");
        commande.setDispensaire(d);
        
        // Use parent cascade
        d.getCommandes().add(commande);
        
        d = entityManager.persistAndFlush(d);
        commande = d.getCommandes().get(0);

        assertNotNull(commande.getNumero(), "Commande should be saved via cascade");

        entityManager.remove(d);
        entityManager.flush();
        entityManager.clear();

        assertNull(entityManager.find(Commande.class, commande.getNumero()), 
            "Commande should be deleted when Dispensaire is deleted");
    }

    @Test
    public void testLigneOptionality() {
        // Try to save Ligne without Commande/Medicament
        Ligne l1 = new Ligne();
        l1.setQuantite(10);
        // Missing Commande and Medicament
        assertThrows(jakarta.persistence.PersistenceException.class, () -> {
            entityManager.persistAndFlush(l1);
        });
    }

    @Test
    public void testLigneUniqueConstraint() {
        // Setup deps
        Categorie cat = new Categorie();
        cat.setLibelle("Test Cat");
        
        Medicament med = new Medicament();
        med.setNom("Test Med");
        med.setCategorie(cat);
        // cat.getMedicaments().add(med); // explicit bidirectional
        
        // We can persist cat separately as usual for setup
        cat = entityManager.persistAndFlush(cat);
        // update med with managed cat
        med.setCategorie(cat);
        med = entityManager.persistAndFlush(med);

        Dispensaire d = new Dispensaire();
        d.setCode("D002");
        d.setNom("Test Disp");
        d = entityManager.persistAndFlush(d);

        Commande cmd = new Commande();
        cmd.setDispensaire(d);
        cmd = entityManager.persistAndFlush(cmd);

        // Unique Constraint (Commande, Medicament)
        // Here we can try direct persist as before since it passed
        Ligne l2 = new Ligne();
        l2.setCommande(cmd);
        l2.setMedicament(med);
        l2.setQuantite(5);
        entityManager.persistAndFlush(l2);

        Ligne l3Duplicate = new Ligne();
        l3Duplicate.setCommande(cmd);
        l3Duplicate.setMedicament(med);
        l3Duplicate.setQuantite(2);
        
        assertThrows(jakarta.persistence.PersistenceException.class, () -> {
            entityManager.persistAndFlush(l3Duplicate);
        }, "Should not allow duplicate lines for same Commande and Medicament");
    }

    @Test
    public void testOrphanRemovalLigne() {
        // Setup
        Categorie cat = new Categorie();
        cat.setLibelle("Orphan Cat");
        cat = entityManager.persistAndFlush(cat);

        Medicament med = new Medicament();
        med.setNom("Orphan Med");
        med.setCategorie(cat);
        med = entityManager.persistAndFlush(med);

        Dispensaire d = new Dispensaire();
        d.setCode("D003");
        d.setNom("Orphan Disp");
        d = entityManager.persistAndFlush(d);

        Commande cmd = new Commande();
        cmd.setDispensaire(d);
        cmd = entityManager.persistAndFlush(cmd);

        Ligne l = new Ligne();
        l.setCommande(cmd);
        l.setMedicament(med);
        l.setQuantite(1);
        
        // Add to collection to trigger orphan removal if configured
        cmd.getLignes().add(l);
        
        cmd = entityManager.persistAndFlush(cmd); // Should cascade persist Ligne
        
        Ligne persistedLigne = cmd.getLignes().get(0);
        Integer ligneId = persistedLigne.getId();
        assertNotNull(ligneId);

        // Remove from collection
        cmd.getLignes().remove(persistedLigne);
        
        entityManager.persistAndFlush(cmd);
        entityManager.clear();

        assertNull(entityManager.find(Ligne.class, ligneId), "Ligne should be deleted when removed from Commande's list (orphanRemoval)");
    }
}
