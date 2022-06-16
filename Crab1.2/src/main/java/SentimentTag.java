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
 * @About The Sentiment-tag class acts as an object to hold sentiment statistics on a user-defined keyword
 *  that is a part of the keyword crawl.
 *
 *  The class also contains methods which provide an interface to calculate sentiment indicators.
 */

public class SentimentTag {
    int positive;
    int neutral;
    int negative;

    SentimentTag(){
    }

    /**
     * @About This method can be used to calculate an indicator based on the proportion
     * of links related to a keyword that are deemed 'positive'.
     * @return a positive indicator
     */
    public int calculatePositiveIndicator(){
        return positive / (positive + negative + neutral);
    }

    public int calculateNegativeIndicator(){
        return negative / (positive + negative + neutral);
    }

    public int calculateNeutralIndicator(){
        return neutral / (positive + negative + neutral);
    }

}