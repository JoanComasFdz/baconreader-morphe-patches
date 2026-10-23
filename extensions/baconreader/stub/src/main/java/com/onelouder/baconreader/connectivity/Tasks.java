package com.onelouder.baconreader.connectivity;

public class Tasks {
    public static abstract class OnCompleteListener<T> {
        public abstract void onCancel(String str);

        public abstract void onComplete(T t);
    }
}
