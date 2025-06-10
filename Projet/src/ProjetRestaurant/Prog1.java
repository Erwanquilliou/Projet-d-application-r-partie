package ProjetRestaurant;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Prog1 {
    public static void main(String[] args) {
        String url = "jdbc:oracle:thin:@charlemagne.iutnc.univ-lorraine.fr:1521:infodb";
        Connection con1 = null;
        Connection con2 = null;
        try {
            //connexion con1
            con1 = DriverManager.getConnection(url, args[0], args[1]);
            con1.setAutoCommit(false);
            //creation de la table ETUDIANT + commit
            ResultSet rs = con1.createStatement().executeQuery(
                    "SELECT COUNT(*) FROM user_tables WHERE table_name = 'ETUDIANT'"
            );
            rs.next();
            if (rs.getInt(1) > 0) {
                con1.createStatement().execute("DROP TABLE ETUDIANT");
            }
            con1.createStatement().execute("create table ETUDIANT (ID int, NOM varchar(100))");
            con1.commit();

            //connexion con2
            con2 = DriverManager.getConnection(url, args[0], args[1]);
            con2.setAutoCommit(false);
            //select * from ETUDIANT
            rs = con2.createStatement().executeQuery("select * from ETUDIANT");
            System.out.println("affichage avec con2:");
            while (rs.next()) {
                System.out.println(rs.getInt(1) + " " + rs.getString(2));
            }
            con1.close();
            con2.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

