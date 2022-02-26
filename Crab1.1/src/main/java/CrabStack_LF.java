import java.util.concurrent.atomic.*;

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
 * A Lock-Free stack based on Treiber Stack for Storing and Retrieving URL's
 *
 * By l33pf (https://github.com/l33pf)
 **/

public class CrabStack_LF {

    private static class Node <String> {
        public final String item;
        public Node<String> next;

        public Node(String item) {
            this.item = item;
        }
    }

    AtomicReference<Node<String>> top = new AtomicReference<Node<String>>();
    AtomicInteger stackSize = new AtomicInteger(0);

    public final void push(final String item) {
        Node<String> newHead = new Node<>(item);
        Node<String> oldHead;
        int size;
        do {
            oldHead = top.get();
            size = stackSize.get();
            newHead.next = oldHead;
        } while (!top.compareAndSet(oldHead, newHead) && (!stackSize.compareAndSet(size,size+1)));
    }

    public final String pop() {
        Node<String> oldHead;
        Node<String> newHead;
        int size;
        do {
            oldHead = top.get();
            size = stackSize.get();
            if (oldHead == null)
                return null;
            newHead = oldHead.next;
        } while (!top.compareAndSet(oldHead, newHead) && (!stackSize.compareAndSet(size,size-1)));
        return oldHead.item;
    }

    public final boolean isEmpty(){
        return top.get() == null;
    }

    public final String peek(){
        if(!isEmpty()){
            return top.get().toString();
        }
        return null;
    }

    public final int size(){
        return stackSize.get();
    }

}