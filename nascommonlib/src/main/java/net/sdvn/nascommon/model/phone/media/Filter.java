package net.sdvn.nascommon.model.phone.media;

public interface Filter<T> {

    /**
     * Filter the file.
     *
     * @param attributes attributes of file.
     * @return filter returns true, otherwise false.
     */
    boolean filter(T attributes);

}