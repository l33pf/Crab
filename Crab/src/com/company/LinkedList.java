package com.company;

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
 * Updates:
 * (22/11/21) - Updated to use Sentinel node, allows to reduce the amount of constant operations and not having to update
 * head and tail pointers - Ideal given the amount of URL's expected in the Crawler's URL seed set. Tested on 22/11/21
 * on Sentiment Crawl set to Neutral/Positive.
 */

public final class LinkedList {

    private Node sentinel;
    int N;

    private class Node {
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
        Node old = new Node();
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
