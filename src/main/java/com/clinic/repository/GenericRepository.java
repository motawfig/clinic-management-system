package com.clinic.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface defining common CRUD operations.
 * <p>
 * تعتمد الخدمات على هذه الواجهات لا على تفاصيل قاعدة البيانات. This keeps the
 * service layer testable and leaves production persistence as a replaceable boundary.
 *
 * @param <T>  the entity type
 * @param <ID> the identifier type
 */
public interface GenericRepository<T, ID> {

    /**
     * Saves a new entity.
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    T save(T entity);

    /**
     * Finds an entity by its identifier.
     *
     * @param id the identifier
     * @return an Optional containing the entity if found
     */
    Optional<T> findById(ID id);

    /**
     * Returns all entities.
     *
     * @return list of all entities
     */
    List<T> findAll();

    /**
     * Updates an existing entity.
     *
     * @param entity the entity with updated values
     * @return the updated entity
     */
    T update(T entity);

    /**
     * Deletes an entity by its identifier.
     *
     * @param id the identifier of the entity to delete
     * @return true if the entity was deleted, false otherwise
     */
    boolean delete(ID id);
}
