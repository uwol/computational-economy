/*
This file is part of Comimport compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.property.Property;
import compecon.culture.sectors.state.law.security.debt.FixedRateBond;
import compecon.culture.sectors.state.law.security.equity.JointStockCompany;
import compecon.culture.sectors.state.law.security.equity.Share;
import compecon.engine.dao.DAOFactory;
import compecon.engine.util.HibernateUtil;
he
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ComputationalEconomy. If not, see <http://www.gnu.org/licenses/>.
 */

package compecon.engine;

import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.property.Property;
import compecon.culture.sectors.state.law.security.debt.FixedRateBond;
import compecon.culture.sectors.state.law.security.equity.JointStockCompany;
import compecon.culture.sectors.state.law.security.equity.Share;
import compecon.engine.dao.DAOFactory;
import compecon.engine.util.HibernateUtil;

public class PropertyFactory {
	public static FixedRateBond newInstanceFixedRateBond(Agent owner,
			Currency currency, BankAccount issuerBankAccount,
			String issuerBankAccountPassword, double faceValue, double coupon) {
		FixedRateBond fixedRateBond = new FixedRateBond();
		fixedRateBond.setOwner(owner);
		fixedRateBond.setIssuerBankAccount(issuerBankAccount);
		fixedRateBond.setIssuerBankAccountPassword(issuerBankAccountPassword);
		fixedRateBond.setFaceValue(faceValue);
		fixedRateBond.setCoupon(coupon);
		fixedRateBond.setIssuedInCurrency(currency);
		fixedRateBond.initialize();
		DAOFactory.getPropertyDAO().save(fixedRateBond);
		HibernateUtil.flushSession();
		return fixedRateBond;
	}

	public static Share newInstanceShare(Agent owner,
			JointStockCompany jointStockCompany) {
		Share share = new Share();
		share.setJointStockCompany(jointStockCompany);
		share.setOwner(owner);
		share.initialize();
		DAOFactory.getPropertyDAO().save(share);
		HibernateUtil.flushSession();
		return share;
	}

	public static void deleteProperty(Property property) {
		DAOFactory.getPropertyDAO().delete(property);
	}
}
