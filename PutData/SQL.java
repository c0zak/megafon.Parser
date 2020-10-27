package PutData;

import Main.Main;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class SQL {
    private final String server;
    private final String port;
    private final String base;
    private final String login;
    private final String password;
    private final boolean isANSIEncoding;
    private boolean isConnected;

    private Connection connection;
    private Statement statement;

    public SQL(String server, String base, String login, String password, boolean isANSIEncoding) {
        isConnected = false;
        this.server = server;
        this.isANSIEncoding = isANSIEncoding;
        this.port = "3306";
        this.base = base;
        this.login = login;
        this.password = password;
        connect();
    }

    public SQL(String server, String port, String base, String login, String password, boolean isANSIEncoding) {
        isConnected = false;
        this.server = server;
        this.port = port;
        this.base = base;
        this.login = login;
        this.password = password;
        this.isANSIEncoding = isANSIEncoding;
        connect();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getBase() {
        return base;
    }

    public void doQuery(String query) {
        try {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            Main.logAdd("Problem with query:");
            Main.logAdd(query);
            Main.logAdd("To base: " + base + "; At: " + new Date());
        }
    }

    public ArrayList<ArrayList<String>> doSelectQuery(String query) {
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        try
        {
            ResultSet resultSet = statement.executeQuery(query);
            int columns = resultSet.getMetaData().getColumnCount();
            int index = 0;
            while(resultSet.next()){
                result.add(new ArrayList<>(columns));
                for (int i = 1; i <= columns; i++) {
                    result.get(index).add(resultSet.getString(i));
                }
                index++;
            }
            resultSet.close();
        }
        catch (Exception e) {
            Main.logAdd("Problem with read from SQL base: " + base + "; At: " + new Date());
            Main.logAdd(e.toString());
        }
        return result;
    }

    private void connect() {
        try
        {
            String url = "jdbc:mysql://" + server + ":" + port + "/" + base + "?serverTimezone=Europe/Moscow&useSSL=false";
            if (isANSIEncoding)
                url += "&characterEncoding=Cp1251";
            connection = DriverManager.getConnection(url, login, password);
            statement = connection.createStatement();
            if (statement == null)
                throw new Exception();
            Main.logAdd("Successful connect to SQL base: " + base + "; At: " + new Date());
            isConnected = true;
        }
        catch (Exception e) {
            Main.logAdd("Problem with connection to SQL base: " + base + "; At: " + new Date());
            Main.logAdd(e.toString());
        }
    }
     public void close() {
         try
         {
             if (statement != null)
                 statement.close();
             if (connection != null)
                 connection.close();
         }
         catch (SQLException ignored) {}
     }
}
