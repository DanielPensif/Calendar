package com.example.Kalendar.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import kotlin.Triple;

public class MediatorLiveDataTriple<A,B,C> extends MediatorLiveData<Triple<A,B,C>> {
    public static <A,B,C> MediatorLiveDataTriple<A,B,C> of(
            LiveData<A> la, LiveData<B> lb, LiveData<C> lc) {
        MediatorLiveDataTriple<A,B,C> m = new MediatorLiveDataTriple<>();
        m.addSource(la, a -> m.setValue(new Triple<>(a, lb.getValue(), lc.getValue())));
        m.addSource(lb, b -> m.setValue(new Triple<>(la.getValue(), b, lc.getValue())));
        m.addSource(lc, c -> m.setValue(new Triple<>(la.getValue(), lb.getValue(), c)));
        return m;
    }
}