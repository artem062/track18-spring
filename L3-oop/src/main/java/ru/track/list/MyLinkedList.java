package ru.track.list;

import java.util.NoSuchElementException;

/**
 * Должен наследовать List
 * Односвязный список
 */
public class MyLinkedList extends List {

    /**
     * private - используется для сокрытия этого класса от других.
     * Класс доступен только изнутри того, где он объявлен
     * <p>
     * static - позволяет использовать Node без создания экземпляра внешнего класса
     */

    private static class Node {
        Node prev;
        Node next;
        int val;

        Node(Node prev, Node next, int val) {
            this.prev = prev;
            this.next = next;
            this.val = val;
        }
    }

    Node head = new Node(null, null, 0);
    @Override

    void add(int item) {

        Node last = new Node(head, head.next, item);
        while (last.next != null){
            last.prev = last.next;
            last.next = last.next.next;
        }
        last.prev.next = last;
    }

    @Override
    int remove(int idx) throws NoSuchElementException {
        Node node = new Node(head, head.next, 0);
        while (0 < idx && node.next != null){
            node.next = node.next.next;
            --idx;
        }
        if (node.next == null){
            throw new NoSuchElementException();
        } else {
            node.next.prev.next = node.next.next;
            if (node.next.next != null){
                node.next.next.prev = node.next.prev;
            }
            return node.next.val;
        }
    }

    @Override
    int get(int idx) throws NoSuchElementException {
        Node node = new Node(head, head.next, 0);
        while (0 < idx && node.next != null){
            node.next = node.next.next;
            --idx;
        }
        if (node.next == null){
            throw new NoSuchElementException();
        } else {
            return node.next.val;
        }
    }

    @Override
    int size() {
        int i = 0;
        Node node = new Node(head, head.next, 0);
        while (node.next != null){
            node.next = node.next.next;
            ++i;
        }
        return i;
    }
}
