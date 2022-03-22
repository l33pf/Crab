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
 * @About  class to pass data to the output/write method
 */
public final class writerObj{
    String url; String title; String keyword;
    int sentimentLevel;

    writerObj(final String _url){this.url = _url;}

    writerObj(final String _url, final int _sentiment){
        this.url = _url;
        this.sentimentLevel = _sentiment;
    }
    writerObj(final String _url, final int _sentiment, final String _keyword){
        this.url = _url;
        this.sentimentLevel = _sentiment;
        this.keyword = _keyword;
    }

    writerObj(final String _url, final String _keyword){
        this.url = _url;
        this.keyword = _keyword;
    }

}