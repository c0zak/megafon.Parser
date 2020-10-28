package GetData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Abonent {
    private final Company company;
    private final String phoneNumber;
    private final String fio;
    private final String date;
    private final String total;
    private final String subscribeFee;
    private final String otherFee;
    private boolean tempLogicVar;  //Sometimes you need a boolean variable as a flag

    public Abonent(String phoneNumber, String fio, String total, String subscribeFee, Company company) {
        this.phoneNumber = phoneNumber;
        this.fio = fio;
        this.total = total;
        this.subscribeFee = subscribeFee;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        this.date = format.format(new Date());
        this.otherFee = String.format("%.2f", Double.parseDouble(total) - Double.parseDouble(subscribeFee)).replaceAll(",", ".");
        this.company = company;
        tempLogicVar = false;
    }

    public Company getCompany() {
        return company;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getFio() {
        return fio;
    }

    public String getDate() {
        return date;
    }

    public String getTotal() {
        return total;
    }

    public String getSubscribeFee() {
        return subscribeFee;
    }

    public String getOtherFee() {
        return otherFee;
    }

    public void setTempLogicVar(boolean tempLogicVar) {
        this.tempLogicVar = tempLogicVar;
    }

    public boolean isTempLogicVar() {
        return tempLogicVar;
    }
}
