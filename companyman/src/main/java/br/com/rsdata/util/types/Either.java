package br.com.rsdata.util.types;

import java.util.Optional;

/**
 *
 * Tentativa de implementar união entre dois tipos genéricos L left e R right.
 * @param left: Optional<L>
 * @param right: Optional<R>
 */
public record Either<L, R>(Optional<L> left, Optional<R> right) {
    public static <L, R> Either<L, R> left(L value) {
        return new Either<>(Optional.of(value), Optional.empty());
    }

    public static <L, R> Either<L, R> right(R value) {
        return new Either<>(Optional.empty(), Optional.of(value));
    }
}
