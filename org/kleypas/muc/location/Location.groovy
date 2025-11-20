package org.kleypas.muc.location

/**
 * Represents a point in three‑dimensional space.
 *
 * <p>Each instance is immutable after construction – the latitude,
 * longitude, and altitude cannot be changed.  The values are stored
 * as {@code float} to allow sub‑degree precision for latitude/longitude
 * and fine altitude resolution.</p>
 */
class Location {
    /** Latitude in degrees. */
    float lat
    /** Longitude in degrees. */
    float lon
    /** Altitude in meters above sea level. */
    float alt

    /**
     * Constructs a {@code Location} with the specified coordinates.
     *
     * @param lat latitude
     * @param lon longitude
     * @param alt altitude
     */
    Location(float lat, float lon, float alt) {
        this.lat = lat
        this.lon = lon
        this.alt = alt
    }

    /**
     * {@inheritDoc}
     *
     * @return a comma‑separated representation of the coordinates
     */
    @Override
    String toString() {
        def output = [
            "lat: ${lat}",
            "lon: ${lon}",
            "alt: ${alt}"
        ]
        return output.join(", ")
    }
}
