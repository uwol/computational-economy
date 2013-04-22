Computational Economy
=====================

Computational Economy is an agent-based computational economics simulator implemented in Java.

Features:
* Agent type *Household*: Households offer labour hours and consume goods according to a Cobb-Douglas utility function.
* Agent type *Farm*: Farms produce mega calories by combining the production factors *acre* and *labour hour*.
* Agent type *Factory*: Factories produce arbitrary goods by combining production factors *machine* and *labour hour*.
* Agent type *Credit bank*: Credit banks manage bank accounts, create money by credit and follow minimum reserve requirements of central banks.
* Agent type *Central bank*: Central banks adjust key interest rates based on price indices. Key interest rates induce changes in buying behaviour via a simulated transmission mechanism.
* Market: Sellers offer goods on markets. The settlement market transfers ownership of offered goods and money, automatically.
* Share: Joint-stock companies are owned by agents and pay dividends to them every period.
* Bonds: Bonds are given as security for credit in open market operations between central and credit banks.
* Time system: Agents register their actions / behaviour as events in the time system (observer pattern).

Technical platform:
* Java 1.6
* Maven
* Optionally: Database server 
	* configuration: hibernate.cfg.xml 
	* activation (vm argument): -Dactivatedb=true

![screenshot1](http://img.literaturedb.com/compecon1.png)

![screenshot2](http://img.literaturedb.com/compecon2.png)

![screenshot3](http://img.literaturedb.com/compecon3.png)