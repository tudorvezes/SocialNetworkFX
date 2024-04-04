package cs.ubb.socialnetworkfx.utils.idGetter;

public interface IdGetter<E> {
    /**
     * Returns a unique id.
     * @return E - the unique id
     */
    E getUniqueId();

    /**
     * Returns a unique id different from the expected id.
     * @param expectedId E - the expected id
     * @return E - the unique id
     */
    E getUniqueId(E expectedId);

    /**
     * Returns the current id.
     * @return E - the current id
     */
    E getCurrentId();
}
