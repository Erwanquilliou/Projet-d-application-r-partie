const map = L.map('map').setView([48.6921, 6.1844], 14);
let restaurantMarkers = [];
let PROXY_URL = 'https://localhost:8443'; //URL par défaut

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
}).addTo(map);

function setProxyUrl() {
    const urlInput = document.getElementById('proxy-url');
    const url = urlInput.value.trim();

    if (url && isValidUrl(url)) {
        PROXY_URL = url.endsWith('/') ? url.slice(0, -1) : url;
        hideProxyModal();
        loadAllData();
    } else {
        alert('Veuillez saisir une URL valide (ex: https://localhost:8443)');
    }
}

function useDefaultProxy() {
    PROXY_URL = 'https://localhost:8443';
    hideProxyModal();
    loadAllData();
}

function showProxyConfig() {
    document.getElementById('proxy-modal').style.display = 'flex';
    document.getElementById('proxy-url').value = PROXY_URL;
}

function hideProxyModal() {
    document.getElementById('proxy-modal').style.display = 'none';
}

function isValidUrl(string) {
    try {
        new URL(string);
        return true;
    } catch (_) {
        return false;
    }
}

async function loadStations() {
    try {
        const gbfsResponse = await fetch('https://api.cyclocity.fr/contracts/nancy/gbfs/gbfs.json');
        const gbfsData = await gbfsResponse.json();

        const feeds = gbfsData.data.fr.feeds;
        const stationInfoUrl = feeds.find(feed => feed.name === 'station_information').url;
        const stationStatusUrl = feeds.find(feed => feed.name === 'station_status').url;

        const stationInfoResponse = await fetch(stationInfoUrl);
        const stationInfo = await stationInfoResponse.json();

        const stationStatusResponse = await fetch(stationStatusUrl);
        const stationStatus = await stationStatusResponse.json();

        displayStations(stationInfo.data.stations, stationStatus.data.stations);
    } catch (error) {
        console.error("Erreur lors du chargement des données Vélib:", error);
    }
}

function displayStations(stationInfos, stationStatuses) {
    stationInfos.forEach(station => {
        const status = stationStatuses.find(s => s.station_id === station.station_id);

        if (status && station.lat && station.lon) {
            const marker = L.marker([station.lat, station.lon], {
                icon: L.divIcon({
                    className: 'velib-marker',
                    html: '<div class="velib-icon">V</div>',
                    iconSize: [20, 20]
                })
            }).addTo(map);

            const velosDisponibles = status.num_bikes_available || 0;
            const placesLibres = status.num_docks_available || 0;

            const popupContent = `
                <div class="info-station">
                    <h4>${station.name}</h4>
                    <p><strong>Adresse:</strong> ${station.address || 'Non spécifiée'}</p>
                    <p><strong>Vélos disponibles:</strong> ${velosDisponibles}</p>
                    <p><strong>Places libres:</strong> ${placesLibres}</p>
                </div>
            `;

            marker.bindPopup(popupContent);
        }
    });
}

async function loadWazeData() {
    try {
        console.log("Tentative de récupération des données Waze...");

        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 30000);

        const response = await fetch(`${PROXY_URL}/api/waze-data`, {
            method: 'GET',
            mode: 'cors',
            cache: 'no-cache',
            headers: {
                'Accept': 'application/json'
            },
            signal: controller.signal
        });

        clearTimeout(timeoutId);

        if (!response.ok) {
            throw new Error(`Erreur HTTP: ${response.status} ${response.statusText}`);
        }

        const responseText = await response.text();

        if (!responseText || responseText.trim() === '') {
            throw new Error('Réponse vide reçue du serveur');
        }

        console.log("Longueur des données Waze reçues:", responseText.length);

        try {
            const wazeData = JSON.parse(responseText);

            if (wazeData.error) {
                console.error("Erreur renvoyée par le serveur:", wazeData.error);
                return;
            }

            displayWazeIncidents(wazeData);
        } catch (parseError) {
            console.error("Erreur lors du parsing JSON:", parseError);
            console.log("Début de la réponse:", responseText.substring(0, 100));
        }
    } catch (error) {
        console.error("Erreur lors du chargement des données Waze:", error);
    }
}

function displayWazeIncidents(data) {
    if (!data || !data.incidents) {
        console.error("Format de données Waze invalide");
        return;
    }

    data.incidents.forEach(incident => {
        if (incident.location && incident.location.polyline) {
            const coordinates = incident.location.polyline.split(' ');

            if (coordinates.length >= 2) {
                const lat = parseFloat(coordinates[0]);
                const lng = parseFloat(coordinates[1]);

                if (!isNaN(lat) && !isNaN(lng)) {
                    const marker = L.marker([lat, lng], {
                        icon: L.divIcon({
                            className: 'incident-marker',
                            html: '<div class="incident-icon">⚠️</div>',
                            iconSize: [24, 24]
                        })
                    }).addTo(map);

                    const startDate = new Date(incident.starttime).toLocaleDateString();
                    const endDate = new Date(incident.endtime).toLocaleDateString();

                    const popupContent = `
                        <div class="incident-info">
                            <h4>Incident de circulation</h4>
                            <p><strong>Type:</strong> ${incident.type || 'Non spécifié'}</p>
                            <p><strong>Description:</strong> ${incident.description || 'Aucune description'}</p>
                            <p><strong>Adresse:</strong> ${incident.location.street || incident.location.location_description || 'Non spécifiée'}</p>
                            <p><strong>Début:</strong> ${startDate}</p>
                            <p><strong>Fin:</strong> ${endDate}</p>
                        </div>
                    `;

                    marker.bindPopup(popupContent);
                }
            }
        }
    });
}

async function loadRestaurants() {
    try {
        const response = await fetch(`${PROXY_URL}/api/restaurants`);
        if (!response.ok) {
            throw new Error(`Erreur HTTP: ${response.status}`);
        }

        const restaurants = await response.json();
        displayRestaurants(restaurants);
    } catch (error) {
        console.error("Erreur lors du chargement des restaurants:", error);
    }
}

function displayRestaurants(restaurants) {
    restaurants.forEach(restaurant => {
        if (restaurant.latitude && restaurant.longitude) {
            const marker = L.marker([restaurant.latitude, restaurant.longitude], {
                icon: L.divIcon({
                    className: 'restaurant-marker',
                    html: '<div class="restaurant-icon">🍴</div>',
                    iconSize: [24, 24]
                })
            }).addTo(map);

            restaurantMarkers.push({
                id: restaurant.idrest,
                marker: marker
            });

            const popupContent = `
                <div class="restaurant-info">
                    <h4>${restaurant.nom}</h4>
                    <p><strong>Adresse:</strong> ${restaurant.adresse}</p>
                    <button class="btn-reservation" onclick="showReservationStep1(${restaurant.idrest})">Réserver une table</button>
                </div>
            `;

            marker.bindPopup(popupContent);
        }
    });
}

function showReservationStep1(restaurantId) {
    const today = new Date().toISOString().split('T')[0];
    const formHtml = `
        <div class="reservation-step1">
            <h4>Choisissez la date et l'heure</h4>
            <form id="form-step1" onsubmit="loadAvailableTables(event, ${restaurantId})">
                <div class="form-group">
                   <label for="date">Date:</label>
                   <input type="date" id="date" name="date" value="${today}" required>
                </div>
                <div class="form-group">
                    <label for="time">Heure:</label>
                    <input type="time" id="time" name="time" required>
                </div>
                <button type="submit">Voir les tables disponibles</button>
            </form>
        </div>
    `;

    const markerObj = restaurantMarkers.find(m => m.id === restaurantId);
    if (markerObj) {
        markerObj.marker.setPopupContent(formHtml);
    }
}

function loadAvailableTables(event, restaurantId) {
    event.preventDefault();
    const form = event.target;
    const date = form.date.value;
    const time = form.time.value;

    fetch(`${PROXY_URL}/api/tables-libres?id=${restaurantId}&date=${date}&time=${time}`)
        .then(response => response.json())
        .then(tables => {
            if (!Array.isArray(tables)) {
                showReservationStep2(restaurantId, date, time, []);
                console.error("Aucune table trouvée", tables);
                return;
            }
            showReservationStep2(restaurantId, date, time, tables);
        })
        .catch(error => {
            console.error("Erreur lors de la récupération des tables:", error);
        });
}

function showReservationStep2(restaurantId, date, time, tables) {
    let tableOptions = '';
    if (tables.length === 0) {
        tableOptions = '<option value="">Aucune table disponible</option>';
    } else {
        tables.forEach(table => {
            tableOptions += `<option value="${table.numtab}">Table ${table.numtab} (${table.nbplace} places)</option>`;
        });
    }
    const formHtml = `
        <div class="reservation-step2">
            <h4>Réservation</h4>
            <form id="form-reservation" onsubmit="submitReservation(event, ${restaurantId}, '${date}', '${time}')">
                <div class="form-group">
                    <label for="table">Table:</label>
                    <select id="table" name="table" required>
                        ${tableOptions}
                    </select>
                </div>
                <div class="form-group">
                    <label for="date">Date:</label>
                    <input type="date" id="date" name="date" value="${date}" readonly>
                </div>
                <div class="form-group">
                    <label for="time">Heure:</label>
                    <input type="time" id="time" name="time" value="${time}" readonly>
                </div>
                <div class="form-group">
                    <label for="nbPersonnes">Nombre de personnes:</label>
                    <input type="number" id="nbPersonnes" name="nbPersonnes" min="1" required>
                </div>
                <div class="form-group">
                    <label for="nom">Nom:</label>
                    <input type="text" id="nom" name="nom" required>
                </div>
                <div class="form-group">
                    <label for="prenom">Prénom:</label>
                    <input type="text" id="prenom" name="prenom" required>
                </div>
                <div class="form-group">
                    <label for="telephone">Téléphone:</label>
                    <input type="tel" id="telephone" name="telephone" required>
                </div>
                <button type="submit">Réserver</button>
            </form>
        </div>
    `;
    const markerObj = restaurantMarkers.find(m => m.id === restaurantId);
    if (markerObj) {
        markerObj.marker.setPopupContent(formHtml);
    }
}

function submitReservation(event, restaurantId) {
    event.preventDefault();

    const form = event.target;
    const numTable = form.table.value;
    const date = form.date.value;
    const time = form.time.value;
    const nbPersonnes = form.nbPersonnes.value;
    const nom = encodeURIComponent(form.nom.value);
    const prenom = encodeURIComponent(form.prenom.value);
    const telephone = encodeURIComponent(form.telephone.value);

    const url = `${PROXY_URL}/api/reserver?id=${restaurantId}&numTable=${numTable}&date=${date}&time=${time}&nom=${nom}&prenom=${prenom}&telephone=${telephone}&nbPersonnes=${nbPersonnes}`;

    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Erreur HTTP: ${response.status}`);
            }
            return response.json();
        })
        .then(result => {
            let message = '';
            if (result.status === 'success') {
                message = '<div class="success">Réservation confirmée !</div>';
            } else {
                message = '<div class="error">Échec de la réservation: ' + (result.message || 'Erreur inconnue') + '</div>';
            }

            const markerObj = restaurantMarkers.find(m => m.id === restaurantId);
            if (markerObj) {
                markerObj.marker.setPopupContent(message);
                setTimeout(() => {
                    fetch(`${PROXY_URL}/api/restaurant?id=${restaurantId}`)
                        .then(response => response.json())
                        .then(restaurant => {
                            const popupContent = `
                                <div class="restaurant-info">
                                    <h4>${restaurant.nom}</h4>
                                    <p><strong>Adresse:</strong> ${restaurant.adresse}</p>
                                    <button class="btn-reservation" onclick="showReservationStep1(${restaurant.idrest})">Réserver une table</button>
                                </div>
                            `;
                            markerObj.marker.setPopupContent(popupContent);
                        });
                }, 3000);
            }
        })
        .catch(error => {
            console.error("Erreur lors de la réservation:", error);

            const markerObj = restaurantMarkers.find(m => m.id === restaurantId);
            if (markerObj) {
                markerObj.marker.setPopupContent('<div class="error">Erreur de communication avec le serveur: ' + error.message + '</div>');
            }
        });
}

async function loadAllData() {
    restaurantMarkers.forEach(markerObj => {
        map.removeLayer(markerObj.marker);
    });
    restaurantMarkers = [];

    await loadStations();
    await loadWazeData();
    await loadRestaurants();
}