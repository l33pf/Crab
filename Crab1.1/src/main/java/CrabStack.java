import java.util.concurrent.locks.*;

/*
***LICENSE***
Copyright 2022 l33pf (https://github.com/l33pf)
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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

    public String peek(){
        return lst.returnHeadVal();
    }
}