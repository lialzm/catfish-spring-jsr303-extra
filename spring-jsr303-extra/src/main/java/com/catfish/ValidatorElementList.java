package com.catfish;

import com.catfish.util.CollectionUtils;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * 迭代方法
 * Created by A on 2017/4/12.
 */
public class ValidatorElementList<E> {

    private LinkedList<E> validatorElementLinkedList = CollectionUtils.createLinkedList();

    public Iterator<E> createIterator() {
        return validatorElementLinkedList.iterator();
    }

    public boolean isEmpty() {
        return validatorElementLinkedList.isEmpty();
    }

    public E getElement(int index) {
        if (index>size()-1){
            return null;
        }
        return validatorElementLinkedList.get(index);
    }

    public void clear(){
        validatorElementLinkedList.clear();
    }

    public int size() {
        return validatorElementLinkedList.size();
    }

    public void add(E validatorElement) {
        validatorElementLinkedList.add(validatorElement);
    }

    public void addAll(LinkedList<E> validatorElementLinkedList) {
        this.validatorElementLinkedList.addAll(validatorElementLinkedList);
    }

    public void remove(Iterator iterator) {
        iterator.remove();
    }


}
