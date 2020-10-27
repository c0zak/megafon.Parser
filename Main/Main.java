package Main;

import GetData.Company;
import GetData.HttpsMegafonClient;
import PutData.CompanyToSQL;
import PutData.SQL;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;


public class Main {
    private static Config config;

    public static void main(String[] args) {
        System.out.println("MEGAVNO!!!!");
        if (args.length>=2)
            config = new Config(args[0], args[1]);
        else
            config = new Config("logopass", "log");

        Map<String, String> logopass = config.getLogopass();
        config.openLog();
        for (String arg : args) {
            if (arg.contains("nolog"))
                config.LOG = false;
        }
        ArrayList<HttpsMegafonClient> httpsMegafonClients = new ArrayList<>(logopass.size());
        for (String login : logopass.keySet()) {
            String password = logopass.get(login);
            httpsMegafonClients.add(new HttpsMegafonClient(login, password));
        }

        ArrayList<Company> companies = new ArrayList<>();

        for (HttpsMegafonClient httpsMegafonClient : httpsMegafonClients) {
            if (httpsMegafonClient.isCorrect()) {
                companies.add(new Company(httpsMegafonClient.getCompanyInfo(), httpsMegafonClient.getCompanyAbonents()));
            }
        }

        ArrayList<Company> temp = new ArrayList<>();
        for (Company company : companies) {
            if (company.isCorrect())
                temp.add(company);
        }
        companies = temp;

        SQL innerBase = new SQL("ipadress", "baseName", "login", "password", false);
        CompanyToSQL innerRecords = new CompanyToSQL(companies, innerBase);

        
        SQL mainBase = new SQL("ipadress", "baseName", "login", "passwod", true);
        CompanyToSQL mainRecords = new CompanyToSQL(companies, mainBase);


        innerRecords.updateInnerBaseCompanies("companies");
        innerRecords.updateInnerBaseAbonentsList("abonents");
        innerRecords.addInnerBaseAbonentsCosts("abonentsCosts");
        innerBase.close();

        mainRecords.addMainCompanyBaseAbonentsData();
        //mainRecords.updateMainBaseCompanies("tmc_companyAccounts");
        mainRecords.addMainBaseCompaniesCosts("tmc_companiesCosts");
        mainBase.close();
        config.closeLog();

    }

    public static void logAdd (String s) {
        if (config.LOG) {
            try {
                config.log.write(s);
                config.log.newLine();
                config.log.flush();
            } catch (Exception e) {
                System.out.println("Problem with writing to log! At: " + new Date());
                System.out.println(e.toString());
            }
        }
        else {
            System.out.println(s);
        }
    }
}
