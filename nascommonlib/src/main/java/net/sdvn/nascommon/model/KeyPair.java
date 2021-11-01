package net.sdvn.nascommon.model;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Container to ease passing around a tuple of two objects. This object provides a sensible
 * implementation of equals(), returning true if equals() is true on each of the contained
 * objects.
 */
@Keep
public class KeyPair<F, S> {
    public final F first;
    public final S second;

    /**
     * Constructor for a Pair.
     *
     * @param first  the first object in the Pair
     * @param second the second object in the pair
     */
    public KeyPair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Checks the two objects for equality by delegating to their respective
     * {@link Object#equals(Object)} methods.
     *
     * @param o the {@link KeyPair} to which this one is to be checked for equality
     * @return true if the underlying objects of the Pair are both considered
     * equal
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof KeyPair)) {
            return false;
        }
        KeyPair<?, ?> p = (KeyPair<?, ?>) o;
        return Objects.equals(p.first, first);
    }

    /**
     * Compute a hash code using the hash codes of the underlying objects
     *
     * @return a hashcode of the Pair
     */
    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode());
    }

    @NonNull
    @Override
    public String toString() {
        return "Pair{" + first + " " + second + "}";
    }

    /**
     * Convenience method for creating an appropriately typed pair.
     *
     * @param a the first object in the Pair
     * @param b the second object in the pair
     * @return a Pair that is templatized with the types of a and b
     */
    public static <A, B> KeyPair<A, B> create(A a, B b) {
        return new KeyPair<A, B>(a, b);
    }
}
