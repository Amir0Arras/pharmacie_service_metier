package pharmacie.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@ToString
public class Commande {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(nullable = false)
	@Setter(AccessLevel.NONE) // la clé est auto-générée par la BD, On ne veut pas de "setter"
	private Integer numero;

	@Basic(optional = false)
	@Column(nullable = false)
	@ToString.Exclude
	private LocalDate saisiele = LocalDate.now();

	@Basic(optional = true)
	private LocalDate envoyeele = null;

	// @Max(value=?) @Min(value=?)//if you know range of your decimal fields
	// consider using these annotations to enforce field validation
	@Column(precision = 18, scale = 2)
	@ToString.Exclude
	private BigDecimal port;

	@Size(max = 40)
	@Column(length = 40)
	private String destinataire;

	@Embedded
	private AdressePostale adresseLivraison;

	@Basic(optional = false)
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal remise = BigDecimal.ZERO;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "commande", orphanRemoval = true)
	@JsonIgnoreProperties({"commande"})
	private List<Ligne> lignes = new LinkedList<>();

	@ManyToOne(optional = false)
	@NonNull
	@JsonIgnoreProperties({"commandes"})
	private Dispensaire dispensaire;

}
