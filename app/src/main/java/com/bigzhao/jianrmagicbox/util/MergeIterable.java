package com.bigzhao.jianrmagicbox.util;

import java.util.Iterator;

/**
 * Created by Roy on 16-7-9.
 */
public class MergeIterable<T> implements Iterable<T>{

    public T[][] arrays;

    public MergeIterable(T[]...arrays){
        this.arrays=arrays;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int i=0;
            private int j=0;

            @Override
            public boolean hasNext() {
                return adapt();
            }

            public boolean adapt(){
                while(true) {
                    if (i >= arrays.length) return false;
                    T[] array = arrays[i];
                    int len=0;
                    if (array != null) len=array.length;
                    if (j>=len){
                        ++i;
                        j-=len;
                        continue;
                    }
                    return true;
                }
            }

            @Override
            public T next() {
                if (adapt()) return arrays[i][j++];
                else return null;
            }

            @Override
            public void remove() {
                throw new RuntimeException("unsupported");
            }
        };
    }
}
