package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.*;

/*
RESTAURANT(idrest, nom, adresse, latitude, longitude)
TABL(#idrest, numtab, nbplace)
PLAT(numplat, libelle, type)
RESERVATION(numres, #idrest, #numtab, datres, nbpers, nom, prenom, telephone)
MENU(#idrest, #numplat, prixplat)

RESERVATION(numtab) → TABL(numtab)
RESERVATION(idrest) -> RESTAURANT(idrest)
MENU(idrest) -> RESTAURANT(idrest)
TABL(idrest) -> RESTAURANT(idrest)
MENU(numplat) -> PLAT(numplat)

 */
public class Restaurant implements ServiceRestaurant{
    @Override
    public String getRestaurants() throws RemoteException, RuntimeException{
        String query = "select * from RESTAURANT";
        try {
            Connection con = Connexion.getInstance().getConnection();

            if (con == null) {
                throw new SQLException("Erreur de connexion - connexion nulle");
            }

            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) {
                    json.append(",");
                }
                first = false;
                json.append("{");
                json.append("\"idrest\":").append(rs.getInt("idrest")).append(",");
                json.append("\"nom\":\"").append(rs.getString("nom")).append("\",");
                json.append("\"adresse\":\"").append(rs.getString("adresse")).append("\",");
                json.append("\"latitude\":").append(rs.getDouble("latitude")).append(",");
                json.append("\"longitude\":").append(rs.getDouble("longitude"));
                json.append("}");
            }

            json.append("]");

            rs.close();

            ps.close();

            return json.toString();
        } catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des restaurants", e);
        }
    }

    @Override
    public String getRestaurant(int indexRestaurant) throws RemoteException, RuntimeException {
        String query = "select * from RESTAURANT where idrest = ?";
        try {
            Connection con = Connexion.getInstance().getConnection();

            if (con == null) {
                throw new SQLException("Erreur de connexion - connexion nulle");
            }

            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, indexRestaurant);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return "{}"; //pas trouvé
            }

            StringBuilder json = new StringBuilder("{");
            json.append("\"idrest\":").append(rs.getInt("idrest")).append(",");
            json.append("\"nom\":\"").append(rs.getString("nom")).append("\",");
            json.append("\"adresse\":\"").append(rs.getString("adresse")).append("\",");
            json.append("\"latitude\":").append(rs.getDouble("latitude")).append(",");
            json.append("\"longitude\":").append(rs.getDouble("longitude"));
            json.append("}");

            rs.close();
            ps.close();

            return json.toString();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du restaurant");
        }
    }

    @Override
    public String getMenuRestaurant(int indexRestaurant) throws RemoteException, RuntimeException {
        //on croise idrest avec les plats pour récupérer leurs noms et prix
        String query = "select p.numplat, p.libelle, m.prixplat " +
                "from PLAT p, MENU m " +
                "where m.idrest = ? and m.numplat = p.numplat";
        try {
            Connection con = Connexion.getInstance().getConnection();

            if (con == null) {
                throw new SQLException("Erreur de connexion - connexion nulle");
            }

            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, indexRestaurant);
            ResultSet rs = ps.executeQuery();

            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) {
                    json.append(",");
                }
                first = false;
                json.append("{");
                json.append("\"numplat\":").append(rs.getInt("numplat")).append(",");
                json.append("\"libelle\":\"").append(rs.getString("libelle")).append("\",");
                json.append("\"prixplat\":").append(rs.getDouble("prixplat"));
                json.append("}");
            }

            json.append("]");

            rs.close();
            ps.close();

            return json.toString();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du menu du restaurant");
        }
    }

    @Override
    public String getTablesRestaurant(int indexRestaurant) throws RemoteException, RuntimeException {
        String query = "select * from TABL where idrest = ?";
        try {
            Connection con = Connexion.getInstance().getConnection();

            if (con == null) {
                throw new SQLException("Erreur de connexion - connexion nulle");
            }

            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, indexRestaurant);
            ResultSet rs = ps.executeQuery();

            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) {
                    json.append(",");
                }
                first = false;
                json.append("{");
                json.append("\"numtab\":").append(rs.getInt("numtab")).append(",");
                json.append("\"nbplace\":").append(rs.getInt("nbplace"));
                json.append("}");
            }

            json.append("]");

            rs.close();
            ps.close();

            return json.toString();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des tables du restaurant");
        }
    }

    @Override
    public String getTablesLibreRestaurant(int indexRestaurant, Date date) throws RemoteException, RuntimeException {
        String query = "SELECT t.numtab, t.nbplace FROM TABL t " +
                "WHERE t.idrest = ? AND t.numtab NOT IN " +
                "(SELECT r.numtab FROM RESERVATION r WHERE r.idrest = ? AND r.datres = ?)";

        try {
            Connection con = Connexion.getInstance().getConnection();

            if (con == null) {
                throw new SQLException("Erreur de connexion - connexion nulle");
            }

            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, indexRestaurant);
            ps.setInt(2, indexRestaurant);
            ps.setDate(3, date);
            ResultSet rs = ps.executeQuery();

            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) {
                    json.append(",");
                }
                first = false;
                json.append("{");
                json.append("\"numtab\":").append(rs.getInt("numtab")).append(",");
                json.append("\"nbplace\":").append(rs.getInt("nbplace"));
                json.append("}");
            }

            json.append("]");

            rs.close();
            ps.close();

            return json.toString();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des tables libres du restaurant");
        }
    }

    @Override
    public String reserverTable(int indexRestaurant, int numTable, Timestamp timestamp, String nom, String prenom, String telephone, int nbPersonnes) throws RemoteException, RuntimeException {
        int nextNumRes = 1;
        String seqQuery = "SELECT MAX(numres) + 1 FROM RESERVATION";
        Connection con = null;

        try {
            con = Connexion.getInstance().getConnection();
            if (con == null) {
                throw new SQLException("Erreur de connexion - connexion nulle");
            }

            boolean autoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            try (PreparedStatement seqPs = con.prepareStatement(seqQuery);
                 ResultSet rs = seqPs.executeQuery()) {
                if (rs.next()) {
                    Integer maxNumRes = rs.getInt(1);
                    if (!rs.wasNull()) {
                        nextNumRes = maxNumRes;
                    }
                }
            }

            String insertQuery = "INSERT INTO RESERVATION (numres, idrest, numtab, datres, nbpers, nom, prenom, telephone) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(insertQuery)) {
                ps.setInt(1, nextNumRes);
                ps.setInt(2, indexRestaurant);
                ps.setInt(3, numTable);
                ps.setTimestamp(4, timestamp);
                ps.setInt(5, nbPersonnes);
                ps.setString(6, nom);
                ps.setString(7, prenom);
                ps.setString(8, telephone);

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    con.commit();
                    return "{\"status\":\"success\",\"message\":\"Réservation réussie\"}";
                } else {
                    con.rollback();
                    return "{\"status\":\"error\",\"message\":\"Aucune ligne insérée\"}";
                }
            }
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erreur lors du rollback: " + ex.getMessage());
                }
            }

            String cleanMessage = e.getMessage()
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f");

            return "{\"status\":\"error\",\"message\":\"" + cleanMessage + "\"}";
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la restauration de l'autocommit: " + e.getMessage());
                }
            }
        }
    }
}
