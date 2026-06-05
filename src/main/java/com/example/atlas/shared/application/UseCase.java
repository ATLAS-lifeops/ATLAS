package com.example.atlas.shared.application;

@FunctionalInterface
public interface UseCase<I, O> {

    O execute(I input);
}
