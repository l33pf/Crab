package com.company;

//Simple linked list class
//Can store URL's within the list

public final class LinkedList {

    private Node head;
    private Node tail;
    int N;

    private class Node {
        Node next;
        String URL;
    }

    public int size(){return N;}

    public boolean isEmpty(){return N==0;}

    //Creates an empty list
    //NOTE: head will point to tail
    public Node createNewList(){
        head = new Node();
        tail = new Node();
        head.next = tail;
        tail.next = null;
        N=2;
        return head;
    }

    //Change from boolean value
    public boolean insertAtHead(String value){
        Node oldHead = head;
        head = new Node();
        head.URL = value;
        head.next = oldHead;
        N++;
        return true;
    }

    public void insertAtTail(String value){
        Node oldTail = tail;
        tail = new Node();
        tail.URL = value;
        oldTail.next = tail;
        tail.next = null;
        N++;
    }

    public boolean insertAtIndex(String value, int index){
        Node pointer = head;
        int counter = 0;

        if(index == 0 || index < 0){
            return false;
        }

        while(pointer.next != null){

            if(counter == index){
                  Node temp = new Node();
                  temp = pointer;
                  temp.URL = value;
                  temp.next = pointer.next;
                  N++;
                  return true; //success
            }
            counter++;
            pointer = pointer.next;
        }

        return false; //fail
    }

    //Note this runtime is proportional to N-1
    //Used to find if a URL already exists in the URL seed
    public boolean searchList(String value){
            Node pointer = head;

            if(value.isBlank()){
                return false;
            }

            while(pointer.next != null){

                if(pointer.URL.equals(value)){
                    return true;
                }
                pointer = pointer.next;
            }
            return false;
    }

    public String removeHead(){
        Node oldHead = head;
        head = oldHead.next;
        N--;
        return oldHead.URL;
    }

    public String removeTail(){
        int counter = 0;
        Node pointer = head;
        Node oldTail = new Node();

        while(pointer.next != null){

            if(pointer.next == tail){
                oldTail = tail;
                tail = pointer;
                tail.next = null;

            }
            pointer = pointer.next;
        }
        N--;
        return oldTail.URL;
    }

    //Used for Peek()
    public String returnHeadVal(){
        return head.URL;
    }
}
