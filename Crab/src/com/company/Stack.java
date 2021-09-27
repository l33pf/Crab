package com.company;

public class  Stack {

    //This is an abstract stack where we use a linked list to mirror a stack

    LinkedList lst = new LinkedList();

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

}
