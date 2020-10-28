package PutData;    //This code is specifically for my workbases. You can use this, but I think it would be nice for you to write a convenient one for you :)
                    //In addition, it is in great need of reworking, since it was written before refactoring the main working part.
import GetData.Abonent;
import GetData.Company;
import Main.Main;

import java.text.SimpleDateFormat;
import java.util.*;

public class CompanyToSQL {
    private final ArrayList<Company> companies;
    private final SQL base;
    private final Map<String, Abonent> fullAbonentsPool;

    public CompanyToSQL(ArrayList<Company> companies, SQL base) {
        this.companies = companies;
        this.base = base;
        fullAbonentsPool = new HashMap<>();
        for (Company company : companies) {
            Set<Abonent> pool = company.getAbonents();
            for (Abonent abonent : pool) {
                fullAbonentsPool.put(abonent.getPhoneNumber(), abonent);
            }
        }
    }

    public void updateInnerBaseCompanies(String tableName) {
        try {
            if (!base.isConnected())
                throw new SQLNotConnected();
            String selectQuery = "SELECT * FROM `" + tableName + "`";
            int added = 0;
            int updated = 0;
            ArrayList<ArrayList<String>> result = base.doSelectQuery(selectQuery);
            for (ArrayList<String> strings : result) {
                String accountNumber = strings.get(0);
                for (Company company : companies) {
                    if (company.getAccountNumber().equals(accountNumber)) {
                        company.setTempLogicVar(true);
                    }
                }
            }
            ArrayList<String> updateQueries = new ArrayList<>(companies.size());
            for (Company company : companies) {
                if (company.isTempLogicVar()) {
                    updateQueries.add("UPDATE `" + tableName + "` SET `companyName` = '" + company.getCompanyName() + "" +
                            "', `companyBalance` = '" + company.getCurrentBalance() + "" +
                            "', `creditLimit` = '" + company.getCreditLimit() + "" +
                            "', `companyCosts` = '" + company.getCompanyCosts() + "" +
                            "', `subscribeCosts` = '" + company.getSubscribeCosts() + "" +
                            "', `otherCosts` = '" + company.getOtherCosts() + "" +
                            "' WHERE `" + tableName + "`.`accountNumber` = " + company.getAccountNumber() + ";");
                    updated++;
                }
                else {
                    updateQueries.add("INSERT INTO `" + tableName + "` (`accountNumber`, `companyName`, `companyBalance`, " +
                            "`creditLimit`, `companyCosts`, `subscribeCosts`, `otherCosts`) " +
                            "VALUES ('" + company.getAccountNumber() +
                            "', '" + company.getCompanyName() +
                            "', '" + company.getCurrentBalance() +
                            "', '" + company.getCreditLimit() +
                            "', '" + company.getCompanyCosts() +
                            "', '" + company.getSubscribeCosts() +
                            "', '" + company.getOtherCosts() + "');");
                    added++;
                }
            }
            for (String updateQuery : updateQueries) {
                base.doQuery(updateQuery);
            }
            Main.logAdd("Updated " + updated + " companies (base: " + base.getBase() + ") at: " + new Date());
            Main.logAdd("Inserted " + added + " new companies (base: " + base.getBase() + ") at: " + new Date());

        } catch (Exception e) {
            Main.logAdd("Problem with SQL query to update companies (base: " + base.getBase() + ") at: " + new Date());
            Main.logAdd(e.toString());
        }
    }

    public void updateInnerBaseAbonentsList(String tableName) {
        try {
            if (!base.isConnected())
                throw new SQLNotConnected();
            int added = 0;
            int updated = 0;
            String selectQuery = "SELECT * FROM `" + tableName + "`";
            ArrayList<ArrayList<String>> result = base.doSelectQuery(selectQuery);
            for (ArrayList<String> strings : result) {
                String phoneNumber = strings.get(0);
                Abonent buffer = fullAbonentsPool.get(phoneNumber);
                if (buffer != null)
                    buffer.setTempLogicVar(true);
            }
            ArrayList<String> updateQueries = new ArrayList<>(fullAbonentsPool.size());
            for (Company company : companies) {
                Set<Abonent> pool = company.getAbonents();
                for (Abonent abonent : pool) {
                    if (abonent.isTempLogicVar()) {
                        updateQueries.add("UPDATE `" + tableName + "` SET `accountNumber` = '" + company.getAccountNumber() + "" +
                                "', `fio` = '" + abonent.getFio() + "" +
                                "' WHERE `" + tableName + "`.`phoneNumber` = " + abonent.getPhoneNumber() + ";");
                        updated++;
                    }
                    else {
                        updateQueries.add("INSERT INTO `" + tableName + "` (`phoneNumber`, `accountNumber`, `fio`) " +
                                "VALUES ('" + abonent.getPhoneNumber() + "', '"
                                + company.getAccountNumber() + "', '" + abonent.getFio() + "');");
                        added++;
                    }
                }
            }

            for (String updateQuery : updateQueries) {
                base.doQuery(updateQuery);
            }
            Main.logAdd("Updated " + updated + " abonents (base: " + base.getBase() + ") at: " + new Date());
            Main.logAdd("Inserted " + added + " new abonents (base: " + base.getBase() + ") at: " + new Date());
        }
        catch (Exception e) {
            Main.logAdd("Problem with SQL query to update abonents (base: " + base.getBase() + ") at: " + new Date());
            Main.logAdd(e.toString());
        }
    }

    public void addInnerBaseAbonentsCosts(String tableName) {
        try {
            if (!base.isConnected())
                throw new SQLNotConnected();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            String date = format.format(new Date());
            String prepareQuery = "DELETE FROM `" + tableName + "` WHERE `" + tableName + "`.`date` = '" + date + "'";
            base.doQuery(prepareQuery);
            String selectQuery = "SELECT MAX(ID) FROM `" + tableName + "`";
            ArrayList<ArrayList<String>> result = base.doSelectQuery(selectQuery);
            int ID = Integer.parseInt(result.get(0).get(0));
            for (Abonent abonent : fullAbonentsPool.values()) {
                String addQuery = "INSERT INTO `" + tableName + "` (`ID`, `phoneNumber`, `date`, `total`, `subscribeFee`, `otherFee`) VALUES " +
                        "('" + ++ID + "', " +
                        "'" + abonent.getPhoneNumber() + "', " +
                        "'" + abonent.getDate() + "', " +
                        "'" + abonent.getTotal() + "', " +
                        "'" + abonent.getSubscribeFee() + "', " +
                        "'" + abonent.getOtherFee() + "')";
                base.doQuery(addQuery);
            }
            Main.logAdd("Added " + fullAbonentsPool.size() + " new costs records (base: " + base.getBase() + ") at: " + new Date());
        } catch (Exception e) {
            Main.logAdd("Problem with SQL query to add abonents costs (base: " + base.getBase() + ") at: " + new Date());
            Main.logAdd(e.toString());
        }
    }

    public void addMainCompanyBaseAbonentsData() {
        try {
            if (!base.isConnected())
                throw new SQLNotConnected();
            Map<Abonent, String> abonentsWithAccountNumber = new HashMap<>(fullAbonentsPool.size());
            for (Company company : companies) {
                String companyName = company.getCompanyName().replaceAll("\"", "");
                Set<Abonent> abonents = company.getAbonents();
                for (Abonent abonent : abonents) {
                    abonentsWithAccountNumber.put(abonent, companyName);
                }
            }

            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            String date = format.format(new Date());
            for (Map.Entry<Abonent, String> abonentsEntry : abonentsWithAccountNumber.entrySet()) {
                String query =
                        "UPDATE `ourBase`.`ourTable` SET `rashod_f` = '" + abonentsEntry.getKey().getTotal() + "'," +
                                "`updateDate` = '" + date + "'," +
                                "`actualCompany` = '" + abonentsEntry.getValue() + "' WHERE `ourTable`.`number` = '" +
                                abonentsEntry.getKey().getPhoneNumber() + "';";
                base.doQuery(query);
            }
            Main.logAdd("Updated " + fullAbonentsPool.size() + " records (base: " + base.getBase() + ") at: " + new Date());
        } catch (Exception e) {
            Main.logAdd("Problem with SQL query to refresh abonents data (base: " + base.getBase() + ") at: " + new Date());
            Main.logAdd(e.toString());
        }
    }

    public void updateMainBaseCompanies(String tableName) {
        try {
            if (!base.isConnected())
                throw new SQLNotConnected();
            String selectQuery = "SELECT * FROM `" + tableName + "`";
            int added = 0;
            int updated = 0;
            ArrayList<ArrayList<String>> result = base.doSelectQuery(selectQuery);
            for (Company company : companies) {
                company.setTempLogicVar(false);
            }
            for (ArrayList<String> strings : result) {
                String accountNumber = strings.get(0);
                for (Company company : companies) {
                    if (company.getAccountNumber().equals(accountNumber)) {
                        company.setTempLogicVar(true);
                    }
                }
            }
            ArrayList<String> updateQueries = new ArrayList<>(companies.size());
            for (Company company : companies) {
                if (company.isTempLogicVar()) {
                    updateQueries.add("UPDATE `" + base.getBase() + "`.`" + tableName + "` SET `companyName` = '" + company.getCompanyName() + "" +
                            "', `creditLimit` = '" + company.getCreditLimit() + "" +
                            "' WHERE `" + tableName + "`.`accountNumber` = " + company.getAccountNumber() + ";");
                    updated++;
                }
                else {
                    updateQueries.add("INSERT INTO `" + base.getBase() + "`.`" + tableName + "` (`accountNumber`, `companyName`, " +
                            "`creditLimit`) " +
                            "VALUES ('" + company.getAccountNumber() +
                            "', '" + company.getCompanyName() +
                            "', '" + company.getCreditLimit() + "');");
                    added++;
                }
            }
            for (String updateQuery : updateQueries) {
                base.doQuery(updateQuery);
            }
            Main.logAdd("Updated " + updated + " companies (base: " + base.getBase() + ") at: " + new Date());
            Main.logAdd("Inserted " + added + " new companies (base: " + base.getBase() + ") at: " + new Date());

        } catch (Exception e) {
            Main.logAdd("Problem with SQL query to update companies (base: " + base.getBase() + ") at: " + new Date());
            Main.logAdd(e.toString());
        }
    }

    public void addMainBaseCompaniesCosts(String tableName) {
        try {
            if (!base.isConnected())
                throw new SQLNotConnected();
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            String date = format.format(new Date());
            String prepareQuery = "DELETE FROM `" + base.getBase() + "`.`" + tableName + "` WHERE `" + tableName + "`.`date` = '" + date + "'";
            base.doQuery(prepareQuery);
            String selectQuery = "SELECT MAX(ID) FROM `" + tableName + "`";
            ArrayList<ArrayList<String>> result = base.doSelectQuery(selectQuery);
            int ID;
            try
            {
                ID = Integer.parseInt(result.get(0).get(0));
            }
            catch (Exception e)
            {
                ID = 0;
            }
            for (Company company : companies) {
                String addQuery = "INSERT INTO `" + tableName + "` (`ID`, `accountNumber`, `companyBalance`, `companyCosts`, " +
                        "`subscribeFee`, `otherFee`, `creditLimit`, `date`) VALUES " +
                        "('" + ++ID + "', " +
                        "'" + company.getAccountNumber() + "', " +
                        "'" + company.getCurrentBalance() + "', " +
                        "'" + company.getCompanyCosts() + "', " +
                        "'" + company.getSubscribeCosts() + "', " +
                        "'" + company.getOtherCosts() + "', " +
                        "'" + company.getCreditLimit() + "', " +
                        "'" + date + "')";
                base.doQuery(addQuery);
            }
            Main.logAdd("Added " + companies.size() + " new costs records (base: " + base.getBase() + ") at: " + new Date());
        } catch (Exception e) {
            Main.logAdd("Problem with SQL query to add companies costs (base: " + base.getBase() + ") at: " + new Date());
            Main.logAdd(e.toString());
        }
    }
}
