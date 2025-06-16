# Projet d'application répartie
RAY Marcelin  
QUILLIOU Erwan  
MOUGEL Antonin

# URL webetu

https://webetu.iutnc.univ-lorraine.fr/~ray13u/map/

# Comment lancer

### Configuration

Avant tout, il faut définir le fichier `conf.ini` à la racine du dossier `src` avec les paramètres suivants :

```
login=Login de votre compte Oracle Charlemagne  
mdp=Mot de passe de votre compte Oracle Charlemagne
```

### Droits d’exécution

Assurez-vous de rendre les scripts exécutables avec la commande suivante :

```
chmod +x script.sh
```

(sinon les scripts ne se lanceront pas)

---

## Lancement de l'application

Nous avons simplifié le lancement de l'application à l’aide de **trois scripts** :

### 1. Lancer le service base de données (RMI)

Le premier script à exécuter est `lancerBDServiceRestaurant.sh`. Il lance le service RMI lié à la base de données :

```
./lancerBDServiceRestaurant.sh
```

### 2. Lancer le service Waze

Ensuite, lancez le script `LancerServiceWaze.sh`. Il démarre le service RMI qui récupère les données d'accidents via l'API de Waze. Ce script prend un paramètre booléen qui indique si l'exécution se fait sur une machine de l’IUT. Si oui, le proxy de l’IUT doit être utilisé pour accéder à l’API.

```
./LancerServiceWaze.sh true   # ou false selon le contexte
```

### 3. Lancer le proxy

Enfin, exécutez le script `LancerProxy.sh`, qui fait le lien entre les deux services RMI et le site web. Il nécessite les adresses IP des deux machines hébergeant respectivement les services précédents :

```
./LancerProxy.sh ipServiceRestaurant ipServiceWaze
```

---

## Accès au site

Une fois les scripts lancés, vous pouvez accéder au site web. Il faudra indiquer l’URL du proxy en précisant l’adresse IP de la machine et le port `8443`.

---

Le site est maintenant opérationnel !




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
