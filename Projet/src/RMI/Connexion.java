package RMI;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connexion {
    private static Connexion instance = null;
    private Connection con;

    private Connexion() {
        this.con = null;
    }

    public static Connexion getInstance() {
        if (instance == null) {
            instance = new Connexion();
        }
        return instance;
    }

    public Connection getConnection() {
        return this.con;
    }

    public void connect(String login, String mdp) throws LoginFail {
        String url = "jdbc:mysql://localhost:3306/Restaurants?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";


        try {
            this.con = DriverManager.getConnection(url, login, mdp);
            this.con.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new LoginFail("Erreur de connexion - login ou mot de passe incorrects");
        }
    }

    public void closeConnection() {
        if (this.con != null) {
            try {
                this.con.close();
                this.con = null;
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }
    }
}