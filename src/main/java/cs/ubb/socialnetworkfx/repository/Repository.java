package cs.ubb.socialnetworkfx.repository;

import cs.ubb.socialnetworkfx.domain.Entity;
import cs.ubb.socialnetworkfx.domain.User;
import cs.ubb.socialnetworkfx.domain.validator.ValidationException;
import cs.ubb.socialnetworkfx.dto.FilterDTO;
import cs.ubb.socialnetworkfx.dto.UserFilterDTO;

import java.util.Optional;
import java.util.Set;

public interface Repository<ID, E extends Entity<ID>, Filter extends FilterDTO> {
    /**
     * @param id -the id of the entity to be returned
     *           id must not be null
     * @return an {@code Optional} encapsulating the entity with the given id
     * @throws IllegalArgumentException if id is null.
     */
    Optional<E> findOne(ID id);

    /**
     * @return all entities
     */
    Iterable<E> findAll();

    /**
     * @param entity entity must be not null
     * @return an {@code Optional} - null if the entity was saved,
     * - the entity (id already exists)
     * @throws ValidationException      if the entity is not valid
     * @throws IllegalArgumentException if the given entity is null. *
     */
    Optional<E> save(E entity);

    /**
     * removes the entity with the specified id
     *
     * @param id id must be not null
     * @return an {@code Optional}
     * - null if there is no entity with the given id,
     * - the removed entity, otherwise
     * @throws IllegalArgumentException if the given id is null.
     */
    Optional<E> delete(ID id);

    /**
     * @param entity entity must not be null
     * @return an {@code Optional}
     * - null if the entity was updated
     * - otherwise (e.g. id does not exist) returns the entity.
     * @throws IllegalArgumentException if the given entity is null.
     * @throws ValidationException      if the entity is not valid.
     */
    Optional<E> update(E entity);

    /**
     * @return a set containing all the keys of the repository
     */
    Set<ID> getAllKeys();

    /**
     * @param filter the filter to be applied
     * @return all entities filtered by the given filter
     */
    Iterable<E> findAllFiltered(Filter filter);
}

