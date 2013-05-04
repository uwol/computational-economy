package compecon.culture.sectors.state.law.property;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.engine.PropertyFactory;

@Entity
@Table(name = "Property")
@org.hibernate.annotations.Table(appliesTo = "Property", indexes = { @Index(name = "IDX_DTYPE", columnNames = { "DTYPE" }) })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE")
public abstract class Property {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	protected int id;

	@Column(name = "isDeconstructed")
	protected boolean isDeconstructed = false;

	public void initialize() {
	}

	/*
	 * accessors
	 */

	public boolean isDeconstructed() {
		return isDeconstructed;
	}

	public int getId() {
		return id;
	}

	public void setDeconstructed(boolean isDeconstructed) {
		this.isDeconstructed = isDeconstructed;
	}

	public void setId(int id) {
		this.id = id;
	}

	/*
	 * business logic
	 */

	@Transient
	protected void deconstruct() {
		this.isDeconstructed = true;

		// deregister from property rights system
		PropertyRegister.getInstance().deregister(this);

		// deregister from property rights system
		PropertyRegister.getInstance().deregister(this);

		PropertyFactory.deleteProperty(this);
	}
}
