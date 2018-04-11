package ru.track.list;

import java.util.NoSuchElementException;

/**
 * Должен наследовать List
 *
 * Должен иметь 2 конструктора
 * - без аргументов - создает внутренний массив дефолтного размера на ваш выбор
 * - с аргументом - начальный размер массива
 */
public class MyArrayList extends List {

    int cap = 0;
    int[] Arr;

    public MyArrayList() {
        Arr = new int[10];
    }

    public MyArrayList(int capacity) {
        Arr = new int[capacity];
    }

    @Override
    void add(int item) {
        if (Arr.length == cap){
            int[] Arr2 = new int[(cap + 1) * 2];
            System.arraycopy(Arr, 0, Arr2, 0, cap);
            Arr = Arr2;
        }
        Arr[cap] = item;
        ++cap;
    }

    @Override
    int remove(int idx) throws NoSuchElementException {
        if (idx < cap){
            int item = Arr[idx];
            System.arraycopy(Arr, idx + 1, Arr, idx, cap);
            --cap;
            return item;
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    int get(int idx) throws NoSuchElementException {
        if (idx < cap){
            return Arr[idx];
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    int size() {
        return cap;
    }
}
