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
*
 **/

/*
 * KeywordClass:
 * This class is used to create objects related to keywords provided
 * to collect articles that are related to a keyword.
 *
 *
 * Created: 27/02/2022

 */

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class KeywordClass {

    final String keyword;

    /* Storage for articles based on their sentiment, with the date they were accessed */
    ConcurrentHashMap<String, Date> negativeSentiment;
    ConcurrentHashMap<String, Date> neutralSentiment;
    ConcurrentHashMap<String, Date> positiveSentiment;

    /* Overall sentiment average of articles for the keyword in question */
    double average;

    /* Variables for Sentiment Distribution */
    double negativePercent;
    double neutralPercent;
    double positivePercent;

    KeywordClass(String keyWord){
        this.keyword = keyWord;
    }
}