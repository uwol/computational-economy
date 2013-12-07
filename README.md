Computational Economy
=====================

Computational Economy is an agent-based computational economics simulator implemented in Java (https://github.com/uwol).

Features:
* Agent type *Household*: Households offer labour hours and consume goods according to CES or Cobb-Douglas utility functions. Intertemporal consumption and retirement saving preferences are modeled by Irving-Fisher and Modigliani intertemporal choice models.
* Agent type *Factory*: Factories produce arbitrary goods according to an input-output-model based on Cobb-Douglas, CES and root production functions.
* Agent type *Credit bank*: Credit banks manage bank accounts, create money by credit, trade currencies, follow minimum reserve requirements of central banks and buy bonds for deposits (fractional reserve banking).
* Agent type *Central bank*: Central banks adjust key interest rates based on price indices. Key interest rates induce changes in buying behaviour via a monetary transmission mechanism.
* Agent type *Trader*: Traders import goods from foreign markets for arbitrage.
* Agent type *State*: States emit bonds, which are bought by credit banks for customer savings deposits. Thereby, national debt represents retirement savings of households. 
* Currency: Multiple currency zones, exchange markets, arbitrage.
* Market: Sellers offer goods on markets. The settlement market transfers ownership of offered goods and money, automatically.
* Share: Joint-stock companies are owned by agents and pay dividends to them every period.
* Bonds: Bonds are given as security for credit in open market operations between central and credit banks.
* Time system: Agents register their actions / behaviour as events in the time system (observer pattern).
* Stand-alone high-performance calculation engine; separate from visualization dashboard.

Technical platform:
* Java 1.7
* Maven
* Optional: SQL server 

![screenshot1](http://img.literaturedb.com/compecon1.png)

![screenshot2](http://img.literaturedb.com/compecon2.png)

![screenshot3](http://img.literaturedb.com/compecon3.png)

![screenshot4](http://img.literaturedb.com/compecon4.png)

![screenshot5](http://img.literaturedb.com/compecon5.png)

![screenshot6](http://img.literaturedb.com/compecon6.png)

![screenshot7](http://img.literaturedb.com/compecon7.png)

![screenshot8](http://img.literaturedb.com/compecon8.png)