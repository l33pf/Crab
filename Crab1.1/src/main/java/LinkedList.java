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
 * Updates:
 * (22/11/21) - Updated to use Sentinel node, allows to reduce the amount of constant operations and not having to update
 * head and tail pointers - Ideal given the amount of URL's expected in the Crawler's URL seed set. Tested on 22/11/21
 * on Sentiment Crawl set to Neutral/Positive.
 */

public final class LinkedList {

    private Node sentinel;
    int N;

    static private class Node {
        Node next;
        String URL;
    }

    public int size(){return N;}

    public boolean isEmpty(){return N==0;}

    //Creates an empty list
    //NOTE: head will point to tail
    public void createNewList(){
        sentinel = new Node();
        sentinel.URL = null;
        sentinel.next = null;
        N =0;
    }

    //Change from boolean value
    public void insertAtTop(String value){
        Node node = new Node();
        node.URL = value;
        Node old;
        old = sentinel.next;
        sentinel.next = node;
        node.next = old;
        N++;
    }

    public String removeTop(){
        Node old = sentinel.next;
        sentinel.next = old.next;
        N--;
        return old.URL;
    }

    //Used for Peek()
    public String returnHeadVal(){
        return sentinel.next.URL;
    }
}
