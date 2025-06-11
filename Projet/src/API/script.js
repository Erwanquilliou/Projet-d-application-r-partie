const map = L.map('map').setView([48.6921, 6.1844], 14);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
}).addTo(map);

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
        console.error("Erreur lors du chargement des données:", error);
    }
}

function displayStations(stationInfos, stationStatuses) {
    stationInfos.forEach(station => {
        const status = stationStatuses.find(s => s.station_id === station.station_id);

        if (status && station.lat && station.lon) {
            const marker = L.marker([station.lat, station.lon]).addTo(map);

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

loadStations();