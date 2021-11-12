package com.company;
import java.util.concurrent.locks.*;

/**
 * Simple Stack data structure used for the URL Seed Set
   also implemented is Thread safe methods for a RW lock.
 */

public final class  Stack {

    //This is an abstract stack where we use a linked list to mirror a stack

    LinkedList lst = new LinkedList();
    String initialValue;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    Stack(String val){
        this.initialValue = val;
        push(initialValue);
    }

    Stack(){
    }

    void createStack(){
        lst.createNewList();
    }

    //Pushes an element onto the stack
    void push(String value){
        if(value.isBlank()){
            throw new IllegalArgumentException();
        }
        lst.insertAtHead(value);
    }

    //Pops an element off the stack
    String pop(){
        return lst.removeHead();
    }

    //Displays the current element at the top of the stack
    String peek(){
        return lst.returnHeadVal();
    }

    int size(){
        return lst.size();
    }

    //Clears stack - O(n) time
    void flushStack(Stack stck){
        int N = stck.size();

        if(N != 0){
            for(int i = 0; i < N; i++)
                stck.pop();
        }
    }

    public String safePop(){
        r.lock();
        try { return pop();}
        finally {
            r.unlock();
        }
    }

    public void safePush(final String val){
        r.lock();
        try{
            push(val);
        } finally{
            r.unlock();
        }
    }



}
