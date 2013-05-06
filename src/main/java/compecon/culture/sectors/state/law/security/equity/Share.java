/*
This file is part of ComputationalEconomy.

ComputationalEconomy is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

ComputationalEconomy is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ComputationalEconomy. If not, see <http://www.gnu.org/licenses/>.
 */

package compecon.culture.sectors.state.law.security.equity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import compecon.culture.sectors.state.law.property.Property;

@Entity
public class Share extends Property {

	@ManyToOne
	@JoinColumn(name = "jointstockcompany_id")
	protected JointStockCompany jointStockCompany;

	public void initialize() {
		super.initialize();
	}

	/*
	 * accessors
	 */

	public JointStockCompany getJointStockCompany() {
		return jointStockCompany;
	}

	public void setJointStockCompany(JointStockCompany jointStockCompany) {
		this.jointStockCompany = jointStockCompany;
	}

}
