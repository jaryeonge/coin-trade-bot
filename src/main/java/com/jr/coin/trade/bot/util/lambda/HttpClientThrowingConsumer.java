package com.jr.coin.trade.bot.util.lambda;

import java.util.function.Consumer;

@FunctionalInterface
public interface HttpClientThrowingConsumer<T, E extends Throwable> {

    void accept(T t) throws E;

    static <T, E extends Throwable> Consumer<T> unchecked(HttpClientThrowingConsumer<T, E> f) {
        return t -> {
            try {
                f.accept(t);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    static <T, E extends Throwable> Consumer<T> unchecked(HttpClientThrowingConsumer<T, E> f, Consumer<Throwable> c) {
        return t -> {
            try {
                f.accept(t);
            } catch (Throwable e) {
                c.accept(e);
            }
        };
    }
}
