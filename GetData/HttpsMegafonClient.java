package GetData;

import Main.Main;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class HttpsMegafonClient {
    private HttpsURLConnection client;
    private boolean isCorrect;
    private String login;
    private String id;
    private String cookie;

    public HttpsMegafonClient() {
        isCorrect = false;
    }

    public HttpsMegafonClient(String login, String password) {
        isCorrect = false;
        this.auth(login, password);
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    private void setAuthCookie () {
        client.setRequestProperty("Accept", "*/*");
        client.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        client.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
        client.setRequestProperty("Connection", "keep-alive");
        client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        client.setRequestProperty("Host", "b2blk.megafon.ru");
        client.setRequestProperty("DNT", "1");
        client.setRequestProperty("Pragma", "no-cache");
        client.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:81.0) Gecko/20100101 Firefox/81.0");
    }

    private void setWorkingCookie () {
        client.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        client.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        client.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
        client.setRequestProperty("Connection", "keep-alive");
        client.setRequestProperty("Cookie", cookie);
        client.setRequestProperty("Host", "b2blk.megafon.ru");
        client.setRequestProperty("Upgrade-Insecure-Requests", "1");
        client.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:81.0) Gecko/20100101 Firefox/81.0");
    }

    public ArrayList<String> getCompanyAbonents() {
        try {
            if (!isCorrect)
                throw new WrongAuthDataException();
            String url = "https://b2blk.megafon.ru/d/expenses/account/" + id + "/export.csv";
            client = (HttpsURLConnection) new URL(url).openConnection();
            client.setRequestMethod("GET");
            setWorkingCookie();

            BufferedInputStream incomingBytes = new BufferedInputStream(client.getInputStream());
            ByteBuffer incoming = StandardCharsets.UTF_8.encode(Charset.forName("windows-1251").decode(ByteBuffer.wrap(incomingBytes.readAllBytes())));
            incomingBytes.close();
            client.disconnect();

            BufferedReader streamToLine = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(incoming.array())));
            ArrayList<String> result = new ArrayList<>();
            String line;
            while ((line = streamToLine.readLine()) != null) {
                result.add(line);
            }
            return result;
        }
        catch (Exception e) {
            Main.logAdd("Problem with getting company abonents (login: " + login + "), at: " + new Date());
            Main.logAdd(e.toString());
        }
        return null;
    }

    public ArrayList<String> getCompanyInfo() {
        try {
            if (!isCorrect)
                throw new WrongAuthDataException();

            String url = "https://b2blk.megafon.ru/account/accountInfo/" + id + ".html";
            client = (HttpsURLConnection) new URL(url).openConnection();
            client.setRequestMethod("GET");
            setWorkingCookie();
            BufferedReader incomingLines = new BufferedReader(new InputStreamReader(new GZIPInputStream(client.getInputStream())));
            String line;
            ArrayList<String> data = new ArrayList<>();
            while ((line = incomingLines.readLine()) != null) {
                data.add(line);
            }
            incomingLines.close();
            client.disconnect();
            return data;
        }
        catch (Exception e) {
            Main.logAdd("Problem with getting company info (login: " + login + "), at: " + new Date());
            Main.logAdd(e.toString());
        }
        return null;
    }

    public void auth (String login, String password) {
        this.login = login;
        String url = "https://b2blk.megafon.ru/ws/v1.0/auth/process";
        String postBody = "captchaTime=undefined&password=" + password + "&username=" + login;
        try {
            client = (HttpsURLConnection) new URL(url).openConnection();
            setAuthCookie();
            client.setRequestMethod("POST");
            client.setDoOutput(true);
            DataOutputStream postData = new DataOutputStream(client.getOutputStream());
            postData.writeBytes(postBody);
            postData.flush();
            Set<String> cookies = new HashSet<>(client.getHeaderFields().get("Set-Cookie"));
            StringBuilder cookie = new StringBuilder();
            for (String s : cookies) {
                cookie.append(s.split(";")[0]).append("; ");
            }
            BufferedReader companyData = new BufferedReader(new InputStreamReader(client.getInputStream()));
            ArrayList<String> incomingData = new ArrayList<>(110);
            String incomingLine;
            while ((incomingLine = companyData.readLine()) != null) {
                incomingData.addAll(Arrays.asList(incomingLine.split(",")));
            }
            companyData.close();
            client.disconnect();
            String id = null;
            for (String line : incomingData) {
                if (line.contains("user.incorrect.login.or.password"))
                    throw new WrongAuthDataException();
                if (line.contains("bisId")) {
                    id = line.split(":")[1];
                }
            }
            this.id = id;
            this.cookie = cookie.toString();
            Main.logAdd("Successful login to: " + login + "; At: " + new Date());
            isCorrect = true;
        }
        catch (Exception e) {
            isCorrect = false;
            Main.logAdd("Problem with authentication (login: " + login + "), at: " + new Date());
            Main.logAdd(e.toString());
        }
    }
}
