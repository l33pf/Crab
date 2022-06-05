/*
 ***LICENSE***
Copyright 2022 https://github.com/l33pf
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
 * @About the CrabTag class is used to quantify URL's either based on the number
 * of keywords their anchor text contained (keyword crawl) or by the sentiment of their
 * anchor text (sentiment crawl).
 *
 * CrabTag is used for the frontier in order to ensure that the "best" links appear at the top
 * of the queue effectively implementing a best-first search method for crawling links.
 */

public class CrabTag {

    String link;
    /* quantity can be the number of keywords or the sentiment level provided from CoreNLP */
    int quantity;

    CrabTag(final String _url, final int _num){
        this.link = _url;
        this.quantity = _num;
    }

    public int getQuantity(){
        return quantity;
    }

}