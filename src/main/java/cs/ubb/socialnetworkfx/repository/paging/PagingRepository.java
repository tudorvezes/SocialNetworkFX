package cs.ubb.socialnetworkfx.repository.paging;

import cs.ubb.socialnetworkfx.domain.Entity;
import cs.ubb.socialnetworkfx.dto.FilterDTO;
import cs.ubb.socialnetworkfx.repository.Repository;

public interface PagingRepository<ID, E extends Entity<ID>, Filter extends FilterDTO> extends Repository<ID, E, Filter> {
        /**
         * Returns all entities on a specific page.
         * @param pageable Pageable - the page
         * @return Page - the page
         */
        Page<E> findAllOnPage(Pageable pageable);

        /**
         * Returns all entities on a specific page, filtered by a filter.
         * @param pageable Pageable - the page
         * @param filter Filter - the filter
         * @return Page - the page
         */
        Page<E> findAllOnPageFiltered(Pageable pageable, Filter filter);
}
