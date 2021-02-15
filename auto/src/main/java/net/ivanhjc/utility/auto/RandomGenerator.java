package net.ivanhjc.utility.auto;

/**
 * A random generator that generates different type of values with the same method.
 *
 * @param <T> the type of value to be generated
 */
public interface RandomGenerator<T> {
    /**
     * Return a random value of the specified type according to each implementation
     *
     * @return a random value of the specified type
     */
    T generate();
}
