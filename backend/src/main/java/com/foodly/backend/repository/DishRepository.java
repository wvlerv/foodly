package com.foodly.backend.repository;

import com.foodly.backend.entity.Dish;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Dish entity. Provides database operations for dishes.
 */
@Repository
public interface DishRepository extends JpaRepository<Dish, UUID> {

	/**
	 * Fetch all available dishes, optionally sorted by proteins, calories, fats, or
	 * carbohydrates.
	 * @param sortBy the field to sort by ("proteins", "calories", "fats", or "carbs")
	 * @return a list of available dishes sorted by the specified field
	 */
	@Query("""
			SELECT d FROM Dish d
			WHERE d.isAvailable = true
			ORDER BY
			    CASE WHEN :sortBy = 'proteins' THEN d.proteins END DESC,
			    CASE WHEN :sortBy = 'calories' THEN d.calories END ASC,
			    CASE WHEN :sortBy = 'fats' THEN d.fats END ASC,
			    CASE WHEN :sortBy = 'carbs' THEN d.carbohydrates END ASC,
			    d.name ASC
			""")
	List<Dish> findAllAvailableSorted(@Param("sortBy") String sortBy);

	/**
	 * Fetch all available dishes ordered by name.
	 * @return a list of all available dishes ordered by name
	 */
	@Query("SELECT d FROM Dish d WHERE d.isAvailable = true ORDER BY d.name ASC")
	List<Dish> findAllAvailable();

}
