import java.util.concurrent.locks.*;

/*
***LICENSE***
        Copyright (c) 2021 l33pf (https://github.com/l33pf)
        Permission is hereby granted, free of charge, to any person obtaining a copy
        of this software and associated documentation files (the "Software"), to deal
        in the Software without restriction, including without limitation the rights
        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        copies of the Software, and to permit persons to whom the Software is
        furnished to do so, subject to the following conditions:
        The above copyright notice and this permission notice shall be included in all
        copies or substantial portions of the Software.
        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        SOFTWARE.
**/

/**
 * Simple Stack data structure used for the URL Seed Set
 *
 * UPDATES:
 * 12/11/21 - Added in Reentrant Lock features
 * 22/11/21 - Updated and removed methods to reflect changes in Linked List class (Tested on 22/11/21)
 */

public final class CrabStack {

    //This is an abstract stack where we use a linked list to mirror a stack

    LinkedList lst = new LinkedList();

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    CrabStack(){
        lst.createNewList();
    }

    //Pushes an element onto the stack
    void push(String value){
        if(value.isBlank()){
            throw new IllegalArgumentException();
        }
        lst.insertAtTop(value);
    }

    //Pops an element off the stack
    String pop(){
        return lst.removeTop();
    }

    int size(){
        return lst.size();
    }

    public String safePop(){
        r.lock();
        try { return pop();}
        finally {
            r.unlock();
        }
    }

    public void safePush(final String val){
        w.lock();
        try{
            push(val);
        } finally{
            w.unlock();
        }
    }
}