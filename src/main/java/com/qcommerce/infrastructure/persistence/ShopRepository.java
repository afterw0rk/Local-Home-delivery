package com.qcommerce.infrastructure.persistence;

import com.qcommerce.domain.entities.Shop;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Shop Repository - Spatial Query Support
 * 
 * Key Features:
 * - Native MySQL spatial functions (ST_Distance_Sphere)
 * - Radius-based shop discovery
 * - Performance optimized with SPATIAL INDEX
 * 
 * @author Q-Commerce Engineering Team
 */
@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    Optional<Shop> findByEmail(String email);

    /**
     * CRITICAL QUERY: Find nearby shops within radius
     * 
     * Uses ST_Distance_Sphere() for accurate distance calculation on a sphere (Earth)
     * - More accurate than simple lat/long arithmetic
     * - Returns distance in meters
     * - SPATIAL INDEX on location column ensures O(log n) performance
     * 
     * Query Breakdown:
     * 1. ST_GeomFromText(): Converts WKT to POINT geometry
     * 2. 4326: SRID for GPS coordinates (WGS 84)
     * 3. ST_Distance_Sphere(): Calculates great-circle distance in meters
     * 4. Distance comparison done in meters (radiusKm * 1000)
     * 
     * Performance:
     * - Without SPATIAL INDEX: O(n) - scans all shops
     * - With SPATIAL INDEX: O(log n) - uses R-tree structure
     * 
     * Example Usage:
     * GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
     * Point userLocation = gf.createPoint(new Coordinate(77.5946, 12.9716));
     * List<Shop> nearbyShops = shopRepository.findNearbyShops(userLocation, 2.0);
     * 
     * @param userLocation User's current GPS location as JTS Point
     * @param radiusKm Search radius in kilometers
     * @return List of shops within radius, sorted by distance (nearest first)
     */
    @Query(value = """
        SELECT s.*, 
               ST_Distance_Sphere(s.location, :userLocation) AS distance_meters
        FROM shops s
        WHERE s.shop_status = 'APPROVED'
          AND s.is_open = true
          AND ST_Distance_Sphere(s.location, :userLocation) <= (:radiusKm * 1000)
        ORDER BY distance_meters ASC
        """, nativeQuery = true)
    List<Shop> findNearbyShops(
        @Param("userLocation") Point userLocation, 
        @Param("radiusKm") Double radiusKm
    );

    /**
     * Find shops within radius and return with calculated distance
     * Useful for displaying "2.3 km away" to users
     */
    @Query(value = """
        SELECT s.id, s.shop_name, s.owner_name, s.email, s.phone_number,
               s.location, s.address_line1, s.address_line2, s.city, s.state, s.pincode,
               s.gst_number, s.fssai_license, s.shop_status, s.opening_time, s.closing_time,
               s.is_open, s.delivery_radius_km, s.min_order_amount,
               s.created_at, s.updated_at, s.created_by, s.updated_by, s.version,
               ROUND(ST_Distance_Sphere(s.location, :userLocation) / 1000, 2) AS distance_km
        FROM shops s
        WHERE s.shop_status = 'APPROVED'
          AND s.is_open = true
          AND ST_Distance_Sphere(s.location, :userLocation) <= (:radiusKm * 1000)
        ORDER BY distance_km ASC
        """, nativeQuery = true)
    List<Object[]> findNearbyShopsWithDistance(
        @Param("userLocation") Point userLocation,
        @Param("radiusKm") Double radiusKm
    );

    /**
     * Find shops within their own delivery radius
     * Each shop can have different delivery coverage
     */
    @Query(value = """
        SELECT s.*
        FROM shops s
        WHERE s.shop_status = 'APPROVED'
          AND s.is_open = true
          AND ST_Distance_Sphere(s.location, :userLocation) <= (s.delivery_radius_km * 1000)
        ORDER BY ST_Distance_Sphere(s.location, :userLocation) ASC
        """, nativeQuery = true)
    List<Shop> findShopsWithinDeliveryRadius(@Param("userLocation") Point userLocation);

    /**
     * Standard queries
     */
    List<Shop> findByShopStatus(Shop.ShopStatus status);

    @Query("SELECT s FROM Shop s WHERE s.city = :city AND s.shopStatus = 'APPROVED'")
    List<Shop> findApprovedShopsByCity(@Param("city") String city);
}
