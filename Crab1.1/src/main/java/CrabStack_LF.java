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

    AtomicReference<LinkedList.Node> top = new AtomicReference<>();

    public void Push(String url){
        LinkedList.Node _head = new LinkedList.Node(url);
        LinkedList.Node _oldHead;
        do{
            _oldHead = top.get(); //otherwise get what's replaced
        }while(!top.compareAndSet(_oldHead,_head)); // if we still have the oldHead then we exchange - CAS
    }

    public String pop(){
        LinkedList.Node _oldHead;
        LinkedList.Node head;
        do{
            _oldHead = top.get();
            if(_oldHead == null){
                return null;
            }
            head = _oldHead.next;
        }while(!top.compareAndSet(_oldHead,head));

        return _oldHead.URL;
    }

    public boolean isEmpty(){
        return top.get() == null;
    }

    public String peek(){
        if(!isEmpty()){
            return top.get().URL;
        }
        return null;
    }
}