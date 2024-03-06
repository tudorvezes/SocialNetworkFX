package cs.ubb.socialnetworkfx.repository.paging;

import cs.ubb.socialnetworkfx.domain.Entity;
import cs.ubb.socialnetworkfx.dto.FilterDTO;
import cs.ubb.socialnetworkfx.repository.Repository;

public interface PagingRepository<ID, E extends Entity<ID>, Filter extends FilterDTO> extends Repository<ID, E, Filter> {
        Page<E> findAllOnPage(Pageable pageable);
        Page<E> findAllOnPageFiltered(Pageable pageable, Filter filter);
}
