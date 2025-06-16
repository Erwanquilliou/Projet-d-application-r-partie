# Projet d'application répartie
RAY Marcelin  
QUILLIOU Erwan  
MOUGEL Antonin

# Comment lancer

il faut :  
```
chmod +x script.sh 
```
sinon les scripts ne se lancent pas.

Pour lancer l'application nous avons décidé de simplifier ça à l'aide de script, 3 scripts.

Le premier script à lancer est lancerBDServiceRestaurant.sh, il va lancer le service RMI lié à la base de données.

```
./lancerBDServiceRestaurant.sh
```

Ensuite on peut lancer le script LancerServiceWaze.sh, il va lancer le service RMI qui récupère les accidents waze.

```
./LancerServiceWaze.sh
```

Enfin nous pouvons lancer le dernier script LancerProxy, qui va lancer le proxy liant les deux services RMI au site web, pour ce faire il a besoin des deux ip des deux machines qui propose les deux services lancés précédemment.

```
./LancerProxy.sh ipServiceRestaurant ipServiceWaze
```

Une fois que tous les scripts sont lancés on peut se connecter au site et remplir l'url permettant d'accéder au proxy, donc en renseignant l'ip de la machine ainsi que le port 8443 du proxy.

Et voila le site est opérationnel !




# Schéma final

<img width="624" alt="Capture d’écran 2025-06-16 à 13 27 18" src="https://github.com/user-attachments/assets/bc687365-9e66-4252-9369-7b26bb369b0a" />


# Présentation de l'API

## 1. Données Waze

```
GET /api/waze-data
```

**Description** : Récupère les données de trafic et navigation depuis le service Waze.

**Paramètres** : Aucun

**Réponse** : Données JSON provenant du service Waze.

## 2. Liste des restaurants

```
GET /api/restaurants
```

**Description** : Récupère la liste de tous les restaurants disponibles.

**Paramètres** : Aucun

**Réponse** : JSON contenant les informations sur tous les restaurants.

## 3. Détail d'un restaurant
```
GET /api/restaurant?id=<id_restaurant>
```

**Description** : Obtient les détails d'un restaurant spécifique.

**Paramètres** :
- `id` : Identifiant numérique du restaurant

**Réponse** : JSON avec les informations détaillées du restaurant.

## 4. Tables libres

```
GET /api/tables-libres?id=<id_restaurant>&date=<date_reservation>
```

**Description** : Liste les tables disponibles dans un restaurant à une date précise.

**Paramètres** :
- `id` : Identifiant du restaurant
- `date` : Date au format YYYY-MM-DD

**Réponse** : JSON contenant les tables disponibles.

## 5. Réservation de table

```
GET /api/reserver?id=<id_restaurant>&numTable=<numero_table>&date=<date_reservation>&nom=<nom_client>&prenom=<prenom_client>&telephone=<telephone_client>&nbPersonnes=<nombre_personnes>
```

**Description** : Permet de réserver une table dans un restaurant.

**Paramètres** :
- `id` : Identifiant du restaurant
- `numTable` : Numéro de la table à réserver
- `date` : Date de réservation (format YYYY-MM-DD)
- `nom` : Nom du client
- `prenom` : Prénom du client
- `telephone` : Numéro de téléphone du client
- `nbPersonnes` : Nombre de personnes

**Réponse** : JSON indiquant le statut de la réservation.

## Gestion des erreurs

Toutes les réponses d'erreur sont au format JSON avec la structure suivante :
```
{
  "status": "error",
  "message": "Description de l'erreur"
}
```
