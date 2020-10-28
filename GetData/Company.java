package GetData;

import Main.Main;

import java.util.*;

public class Company {
    private String companyName;
    private String accountNumber;
    private String currentBalance;
    private String creditLimit;
    private String companyCosts;
    private String subscribeCosts;
    private String otherCosts;
    private boolean tempLogicVar;   //Sometimes you need a boolean variable as a flag
    private final Set<Abonent> abonents;
    private boolean isCorrect;      //Stability of megafon services... not exist :( And we need to check, that data really was readed

    public Company(ArrayList<String> companyInfo, ArrayList<String> companyAbonents) {
        abonents = new HashSet<>();
        if (companyInfo != null && companyAbonents != null)
        {
            parserCompanyInfo(companyInfo);
            parserCompanyAbonents(companyAbonents);
            isCorrect = true;
        }
        else
            isCorrect = false;

        tempLogicVar = false;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCurrentBalance() {
        return currentBalance;
    }

    public String getCreditLimit() {
        return creditLimit == null ? "0.0" : creditLimit;
    }

    public String getCompanyCosts() {
        return companyCosts == null ? "0.0" : companyCosts;
    }

    public String getSubscribeCosts() {
        return subscribeCosts == null ? "0.0" : subscribeCosts;
    }

    public String getOtherCosts() {
        return otherCosts == null ? "0.0" : otherCosts;
    }

    public boolean isTempLogicVar() {
        return tempLogicVar;
    }

    public void setTempLogicVar(boolean tempLogicVar) {
        this.tempLogicVar = tempLogicVar;
    }

    public Set<Abonent> getAbonents() {
        return abonents;
    }

    private void parserCompanyAbonents (ArrayList<String> incomingLines) {      //Create the abonents pool, parsing csv file
        int countOfRecords = 0;
        for (String incomingLine : incomingLines) {
            incomingLine = incomingLine.replaceAll(",", ".");
            if (!incomingLine.contains("Всего") && incomingLine.length()>5) {
                String[] buffer = incomingLine.split(";");
                if (buffer.length > 3){
                    abonents.add(new Abonent(buffer[0].trim(), buffer[1].trim(), buffer[2].trim(), buffer[3].trim(), this));
                    countOfRecords++;
                }
            }
            if (incomingLine.contains("Всего") && !incomingLine.contains("Номер")) {
                String[] buffer = incomingLine.split(";");
                subscribeCosts = buffer[3].trim();
                otherCosts = String.format("%.2f", Double.parseDouble(companyCosts) - Double.parseDouble(subscribeCosts)).replaceAll(",", ".");
            }
        }
        Main.logAdd("Read " + countOfRecords + " abonents from company " + companyName + " at: " + new Date());
    }

    private void parserCompanyInfo (ArrayList<String> incomingLines) {      //Fill company info by parsing. If you need, you can grab more helpful info,
        ArrayList<String> parsing = new ArrayList<>(1300);                  //but in this case i need little bit)
        for (String incomingLine : incomingLines) {
            parsing.addAll(Arrays.asList(incomingLine.split(">")));
        }
        for (String s : parsing) {

            if (s.contains("<button title=\"Лицевой счет")) {
                accountNumber = s.split("Лицевой счет ")[1].split("\"")[0];
            }

            if (s.contains("accountInfo_name")) {
                companyName = parsing.get(parsing.indexOf(s) + 1).replaceAll(" ", " ").trim();
            }

            if (s.contains("Текущий баланс")) {
                int i = parsing.indexOf(s);
                currentBalance = (parsing.get(i + 4).split(",")[0] + "." + parsing.get(i + 5).split("<")[0]).replaceAll(" ", "");
            }

            if (s.contains("Начислено по всем абонентам с начала периода")) {
                int i = parsing.indexOf(s);
                companyCosts = (parsing.get(i + 4).split(",")[0] + "." + parsing.get(i + 5).split("<")[0]).replaceAll(" ", "");
            }

            if (s.contains("Размер кредитного лимита")) {
                int i = parsing.indexOf(s);
                creditLimit = (parsing.get(i + 4).split(",")[0] + "." + parsing.get(i + 5).split("<")[0]).replaceAll(" ", "");
            }
        }
    }

}
