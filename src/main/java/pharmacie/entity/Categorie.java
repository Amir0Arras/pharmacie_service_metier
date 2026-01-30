package pharmacie.entity;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @RequiredArgsConstructor @ToString
public class Categorie {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Setter(AccessLevel.NONE) // la clé est auto-générée par la BD, On ne veut pas de "setter"
	private Integer code;

	@NonNull
	@Size(min = 1, max = 255)
	@Column(unique=true, length = 255)
	@NotBlank // pour éviter les libellés vides
	private String libelle;

	@Size(max = 255)
	@Column(length = 255)
	private String description;

	@ToString.Exclude
	// CascadeType.ALL signifie que toutes les opérations CRUD sur la catégorie sont également appliquées à ses médicaments
	@OneToMany(cascade = {CascadeType.ALL}, mappedBy = "categorie")
	// pour éviter la boucle infinie si on convertit la catégorie en JSON
	@JsonIgnoreProperties({"categorie", "lignes"})
	private List<Medicament> medicaments = new LinkedList<>();

}
