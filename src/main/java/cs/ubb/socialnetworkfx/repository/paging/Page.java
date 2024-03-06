package cs.ubb.socialnetworkfx.repository.paging;

import java.util.stream.Stream;

public class Page<E> {
    private Iterable<E> elements;
    private int totalNoOfElems;

    public Page(Iterable<E> elementsOnPage, int totalNoOfElems) {
        this.elements = elementsOnPage;
        this.totalNoOfElems = totalNoOfElems;
    }

    public Iterable<E> getElementsOnPage() {
        return elements;
    }

    public int getTotalNoOfElems() {
        return totalNoOfElems;
    }
}
