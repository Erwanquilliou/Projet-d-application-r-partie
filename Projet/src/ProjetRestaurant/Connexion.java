package ProjetRestaurant;

import java.sql.*;
import java.util.HashMap;
import java.util.Scanner;

/*
TABL(numtab, nbplace)
PLAT(numplat, libelle, type, prixunit, qteservie)
SERVEUR(numserv, email, passwd, nomserv, grade)
RESERVATION(numres, numtab, datres, nbpers, datpaie, modpaie, montcom)
COMMANDE(numres, numplat, quantite)
AFFECTER(numtab, dataff, numserv)

RESERVATION(numtab) → TABL(numtab)
AFFECTER(numtab) → TABL(numtab)
AFFECTER(numserv) → SERVEUR(numserv)
COMMANDE(numres) → RESERVATION(numres)
COMMANDE(numplat) → PLAT(numplat)
 */
public class Connexion {
    private static Connexion instance = null;
    private static Connection con;

    private Connexion() {
        con = null;
    }

    public static Connexion getInstance() {
        if (instance == null) {
            instance = new Connexion();
        }
        return instance;
    }

    public static Connection getCon() {
        return con;
    }

    public static void connect(String login, String mdp) throws LoginFail {
        String url = "jdbc:oracle:thin:@charlemagne.iutnc.univ-lorraine.fr:1521:infodb";
        Connection con1 = null;

        try {
            //connexion con1
            con1 = DriverManager.getConnection(url, login, mdp);
            con1.setAutoCommit(false);
        } catch (SQLException e) {
            throw new LoginFail("Erreur de connexion - base login incorrects");
        }

        con = con1;
    }

    public static String seConnecter(String login, String mdp) throws LoginFail {
        //on verifie si le compte existe
        String query = "SELECT COUNT(*) FROM serveur WHERE email = ? AND passwd = ?";
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, login);
            ps.setString(2, mdp);
            ResultSet rs = ps.executeQuery();
            rs.next();
            //!= 1 comme ça on voit si on a pas de logins corrects ou si y'a des doublons
            if (rs.getInt(1) != 1) {
                throw new LoginFail("Erreur de connexion - serveur logins incorrects");
            }
        } catch (SQLException e) {
            throw new LoginFail("Erreur de connexion - serveur logins incorrects");
        }

        //on renvoie le role si y'en a un
        query = "SELECT grade FROM serveur WHERE email = ? AND passwd = ?";
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, login);
            ps.setString(2, mdp);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
            throw new LoginFail("Erreur de connexion - aucun role");
        } catch (SQLException e) {
            throw new LoginFail("Erreur de connexion - serveur logins incorrects");
        }
    }

    public static void close() {
        try {
            con.close();
            con = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Consulter les tables disponibles pour une date et heure données.
    public static void consulterTables(String date) {
        String query = "SELECT numtab, nbplace FROM tabl WHERE numtab NOT IN (SELECT numtab FROM reservation WHERE datres = TO_DATE(?, 'DD/MM/YYYY HH24:MI')) FOR UPDATE";
        try {
            if(con == null){
                throw new SQLException("Erreur de connexion - connexion nulle");
            }
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.println("- Numéro de table: " + rs.getInt(1) + ". Nombre de places: " + rs.getInt(2));
            }
            con.commit();//déverrouillage
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void reserverTable(int numtab, String date, int nbpers, String datpaie, String modpaie, double montcom) {
        //insert puis commit
        String query = "INSERT INTO reservation(numtab, datres, nbpers, datpaie, modpaie, montcom) VALUES(?, TO_DATE(?, 'DD/MM/YYYY HH24:MI'), ?, ?, ?, ?) FOR UPDATE";
        try {
            if(con == null){
                throw new SQLException("Erreur de connexion - connexion nulle");
            }
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, numtab);
            ps.setString(2, date);
            ps.setInt(3, nbpers);
            ps.setString(4, datpaie);
            ps.setString(5, modpaie);
            ps.setDouble(6, montcom);
            ps.executeUpdate();
            con.commit();//déverrouillage
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Réserver une table pour une date et heure données.
    public static void reserverTable(String date) throws SQLException {
        //on ne connait donc pas la table, pas de nbpers, et on doit les trouver si on veut juste reserver avec un date
        //chercher les tables dispo
        String query = "SELECT numtab FROM tabl WHERE numtab NOT IN (SELECT numtab FROM reservation WHERE datres = TO_DATE(?, 'DD/MM/YYYY HH24:MI'))";
        ResultSet rs = null;
        try {
            //throw si aucune
            if(con == null){
                throw new SQLException("Erreur de connexion - connexion nulle");
            }
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, date);
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Aucune table disponible pour cette date et heure.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //recup le nbplace de la table pour nbpers
        int nbpers = 0;
        int numtab = rs.getInt(1);
        query = "SELECT nbplace FROM tabl WHERE numtab = ?";
        try {
            if(con == null){
                throw new SQLException("Erreur de connexion - connexion nulle");
            }
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, numtab);
            rs = ps.executeQuery();
            rs.next();
            nbpers = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //appelle de l'autre méthode en complétant les paramètres
        reserverTable(numtab, date, nbpers, "inconnu", "inconnu", 0.0);
    }

    public static void consulterPlats() {
        String query = "SELECT numplat, libelle, type, prixunit, qteservie FROM plat";
        try {
            if(con == null){
                throw new SQLException("Erreur de connexion - connexion nulle");
            }
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.println("- Numéro de plat: " + rs.getInt(1) + ". Libellé: " + rs.getString(2) + ". Type: " + rs.getString(3) + ". Prix unitaire: " + rs.getDouble(4) + ". Quantité servie: " + rs.getInt(5));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void commanderPlats() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Entrez le numéro de table :");
        int numtab = sc.nextInt();
        sc.nextLine(); //vider la ligne

        try {
            if(con == null){
                throw new SQLException("Erreur de connexion - connexion nulle");
            }
            String lockRes = "SELECT * FROM reservation WHERE numtab = ? FOR UPDATE";
            PreparedStatement psLockRes = con.prepareStatement(lockRes);
            psLockRes.setInt(1, numtab);
            psLockRes.executeQuery();

            String lockPlats = "SELECT * FROM plat FOR UPDATE";
            PreparedStatement psLockPlats = con.prepareStatement(lockPlats);
            psLockPlats.executeQuery();

            consulterPlats();

            HashMap<Integer, Integer> plats = new HashMap<>();

            boolean test = false;
            while (!test) {
                System.out.println("Entrez le numéro de plat :");
                int numplat = sc.nextInt();
                sc.nextLine();
                System.out.println("Entrez la quantité :");
                int qte = sc.nextInt();
                sc.nextLine();

                plats.put(numplat, qte);

                System.out.println("Voulez-vous commander un autre plat ? (oui/non)");
                String rep = sc.nextLine();
                if (rep.equalsIgnoreCase("non")) {
                    test = true;
                }
            }

            //il faut verifier si les plats sont dispo (qteservie)
            String query = "SELECT qteservie FROM plat WHERE numplat = ?";
            for (int numplat : plats.keySet()) {
                PreparedStatement ps = con.prepareStatement(query);
                ps.setInt(1, numplat);
                ResultSet rs = ps.executeQuery();
                rs.next();
                if (plats.get(numplat) > rs.getInt(1)) {
                    con.rollback();
                    throw new SQLException("Quantité du plat" + numplat + "insuffisante!");
                }
            }

            query = "INSERT INTO commande(numres, numplat, quantite) VALUES(?, ?, ?)";
            for (int numplat : plats.keySet()) {
                PreparedStatement ps = con.prepareStatement(query);
                ps.setInt(1, numtab);
                ps.setInt(2, numplat);
                ps.setInt(3, plats.get(numplat));
                ps.executeUpdate();
            }

            //on baisse les qteservie maintenant
            query = "UPDATE plat SET qteservie = qteservie - ? WHERE numplat = ?";
            for (int numplat : plats.keySet()) {
                PreparedStatement ps = con.prepareStatement(query);
                ps.setInt(1, plats.get(numplat));
                ps.setInt(2, numplat);
                ps.executeUpdate();
            }
            con.commit();
            System.out.println("Commande enregistrée avec succès !");
        } catch (SQLException e) {
            try {
                con.rollback();
                System.out.println("Erreur lors de la commande, transaction annulée.");
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
        }
        sc.close();
    }

    public static void consulterAffectations() {
        String query = "SELECT numtab, dataff, numserv FROM affecter FOR UPDATE";
        try {
            if(con == null){
                throw new SQLException("Erreur de connexion - connexion nulle");
            }
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.println("- Numéro de table: " + rs.getInt(1) + ". Date d'affectation: " + rs.getString(2) + ". Numéro de serveur: " + rs.getInt(3));
            }
            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void affecterServeurs() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Entrez le numéro de table :");
        int numtab = sc.nextInt();
        sc.nextLine(); //vider la ligne

        try {
            if(con == null){
                throw new SQLException("Erreur de connexion - connexion nulle");
            }
            String lockRes = "SELECT * FROM reservation WHERE numtab = ? FOR UPDATE";
            PreparedStatement psLockRes = con.prepareStatement(lockRes);
            psLockRes.setInt(1, numtab);
            psLockRes.executeQuery();

            String lockAffect = "SELECT * FROM affecter WHERE numtab = ? FOR UPDATE";
            PreparedStatement psLockAffect = con.prepareStatement(lockAffect);
            psLockAffect.setInt(1, numtab);
            psLockAffect.executeQuery();

            String lockServ = "SELECT * FROM serveur FOR UPDATE";
            PreparedStatement psLockServ = con.prepareStatement(lockServ);
            psLockServ.executeQuery();

            System.out.println("Entrez le numéro de serveur :");
            int numserv = sc.nextInt();
            sc.nextLine();

            String query = "INSERT INTO affecter(numtab, dataff, numserv) VALUES(?, SYSDATE, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, numtab);
            ps.setInt(2, numserv);
            ps.executeUpdate();
            con.commit();
            System.out.println("Serveur affecté avec succès !");
        } catch (SQLException e) {
            try {
                con.rollback();
                System.out.println("Erreur lors de l'affectation, transaction annulée.");
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public static void calculerMontant(int numRes) {
        String query = "SELECT SUM(p.prixunit * c.quantite) FROM plat p, commande c WHERE c.numres = ? AND c.numplat = p.numplat";
        try {
            if(con == null){
                throw new SQLException("Erreur de connexion - connexion nulle");
            }
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, numRes);
            ResultSet rs = ps.executeQuery();
            rs.next();
            double montant = rs.getDouble(1);

            query = "UPDATE reservation SET montcom = ? WHERE numres = ?";
            ps = con.prepareStatement(query);
            ps.setDouble(1, montant);
            ps.setInt(2, numRes);
            ps.executeUpdate();
            con.commit();
            System.out.println("Montant total de la réservation " + numRes + " : " + montant + " €");
        } catch (SQLException e) {
            try {
                con.rollback();
                System.out.println("Erreur lors du calcul du montant, transaction annulée.");
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
        }
    }
}
