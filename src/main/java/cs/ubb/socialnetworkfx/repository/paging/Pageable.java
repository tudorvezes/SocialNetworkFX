package cs.ubb.socialnetworkfx.repository.paging;

public class Pageable {

    private int pageNumber;
    private int pageSize;

    public Pageable(int pageNumber, int pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public int getPageNumber() {
        return this.pageNumber;
    }

    public int getPageSize() {
        return this.pageSize;
    }
}
