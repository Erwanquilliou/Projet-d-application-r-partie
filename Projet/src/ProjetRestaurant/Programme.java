package ProjetRestaurant;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Programme {
    public static void main(String[] args) throws SQLException {
        System.out.println("Bienvenue dans le programme de gestion de restaurant.");
        Scanner sc = new Scanner(System.in);

        Connexion c = Connexion.getInstance();
        Connection con = null;

        try {
            c.connect(args[0], args[1]);
            con = c.getCon();
        } catch (LoginFail e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        //on demande les logins et mots de passe
        boolean test = false;
        String role = "";
        while (!test) {
            System.out.println("Entrez votre login :");
            String login = sc.nextLine();
            System.out.println("Entrez votre mot de passe :");
            String mdp = sc.nextLine();
            try {
                role = c.seConnecter(login, mdp);
                System.out.println("Vous etes connecte en tant que " + role);
                test = true;
            } catch (LoginFail e) {
                System.out.println(e.getMessage());
            }
        }

        //on stock les actions possibles
        List<String> actionServeur = new ArrayList<String>();
        actionServeur.add("Consulter les tables disponibles pour une date et heure données.");
        actionServeur.add("Réserver une table pour une date et heure données.");
        actionServeur.add("Consulter les plats disponibles pour une éventuelle commande.");
        actionServeur.add("Commander des plats.");

        List<String> actionGestionnaire = new ArrayList<String>();
        for(String s : actionServeur){
            actionGestionnaire.add(s);
        }
        actionGestionnaire.add("Consulter les affectations des serveurs.");
        actionGestionnaire.add("Affecter des serveurs à des tables.");
        actionGestionnaire.add("Calculer le montant total d’une réservation consommée (numéro de réservation) et mettre à jour la table RESERVATION pour l’encaissement.");

        test = false;
        while(!test){
            System.out.println("Choisissez une action :");
            afficherActions(role.equals("serveur") ? actionServeur : actionGestionnaire);
            int choix = sc.nextInt();

            //!obligé de vider ça sinon il prend pas le nextLine et il met le int précédent
            sc.nextLine();

            //switch case pour recup l'action et lancer la fonction correspondante
            switch(choix){
                case 1:
                    System.out.println("Entrez la date et l'heure: format(DD/MM/YYYY HH:MM)");
                    String date = sc.nextLine();
                    c.consulterTables(date);
                    break;
                case 2:
                    System.out.println("Entrez la date et l'heure: format(DD/MM/YYYY HH:MM)");
                    date = sc.nextLine();
                    c.reserverTable(date);
                    break;
                case 3:
                    c.consulterPlats();
                    break;
                case 4:
                    c.commanderPlats();
                    break;
                case 5:
                    if(role.equals("serveur")){
                        System.out.println("Vous n'avez pas les droits pour cette action.");
                        break;
                    }
                    c.consulterAffectations();
                    break;
                case 6:
                    if(role.equals("serveur")){
                        System.out.println("Vous n'avez pas les droits pour cette action.");
                        break;
                    }
                    c.affecterServeurs();
                    break;
                case 7:
                    if(role.equals("serveur")){
                        System.out.println("Vous n'avez pas les droits pour cette action.");
                        break;
                    }
                    System.out.println("Entrez le numéro de réservation :");
                    int numRes = sc.nextInt();
                    sc.nextLine();
                    c.calculerMontant(numRes);
                    break;
                default:
                    System.out.println("Action inconnue.");
            }
            System.out.println("Voulez-vous effectuer une autre action ? (oui/non)");
            if (sc.hasNextLine()) {
                String rep = sc.nextLine();
                if (rep.equals("non")) {
                    test = true;
                }
            }
        }

        System.out.println("Au revoir !");
        sc.close();
        c.close();
    }

    public static void afficherActions(List<String> actions){
        for(int i = 0; i < actions.size(); i++){
            System.out.println((i+1) + " - " + actions.get(i));
        }
    }
}
