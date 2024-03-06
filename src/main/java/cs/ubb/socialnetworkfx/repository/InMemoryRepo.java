package cs.ubb.socialnetworkfx.repository;

import cs.ubb.socialnetworkfx.domain.Entity;
import cs.ubb.socialnetworkfx.domain.validator.Validator;
import cs.ubb.socialnetworkfx.dto.FilterDTO;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

public class InMemoryRepo<ID, E extends Entity<ID>, Filter extends FilterDTO> implements Repository<ID, E, Filter> {
    Map<ID, E> entities;
    Validator validator = null;

    public InMemoryRepo(Validator validator) {
        entities = new HashMap<ID, E>();
        this.validator = validator;
    }

    public InMemoryRepo() {
        entities = new HashMap<ID, E>();
    }

    @Override
    public Optional<E> findOne(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }

        E entity = entities.get(id);
        if(entity == null)
            return Optional.empty();
        return Optional.of(entity);
    }

    private Optional<E> exists(E entity) {
        return StreamSupport.stream(findAll().spliterator(), false)
                .filter(e -> e.equals(entity))
                .findFirst();
    }

    @Override
    public Iterable<E> findAll() {
        return entities.values();
    }

    @Override
    public Optional<E> save(E entity) {
        if(entity == null)
            throw new IllegalArgumentException("entity must not be null");
        if(validator != null) {
            validator.validate(entity);
        }
        Optional<E> optional = exists(entity);
        if(optional.isPresent())
            return optional;

        entities.put(entity.getId(), entity);
        return Optional.empty();
    }

    @Override
    public Optional<E> delete(ID id) {
        if (id == null)
            throw new IllegalArgumentException("entity must not be null");

        E entity = entities.get(id);
        if (entity == null)
            return Optional.empty();

        entities.remove(id);
        return Optional.of(entity);
    }

    @Override
    public Optional<E> update(E entity) {
        Optional<E> optional = exists(entity);
        if(optional.isEmpty())
            return Optional.of(entity);
        if(validator != null) {
            validator.validate(entity);
        }

        if(entities.get(entity.getId()) != null) {
            entities.put(entity.getId(), entity);
            return Optional.empty();
        }
        return Optional.empty();
    }

    /**
     * Returns all the keys of the entities
     * @return a set of all the keys
     */
    public Set<ID> getAllKeys() {
        return entities.keySet();
    }

    @Override
    public Iterable<E> findAllFiltered(Filter filter) {
        return null;
    }

    public int size() {
        return entities.size();
    }

}
