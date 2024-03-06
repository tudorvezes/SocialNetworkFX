package cs.ubb.socialnetworkfx.utils.idGetter;

public interface IdGetter<E> {
    E getUniqueId();
    E getUniqueId(E expectedId);
    E getCurrentId();
}
