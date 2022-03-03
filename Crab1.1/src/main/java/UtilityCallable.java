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
 *  a Callable which is used to handle Crab's utility tasks
 */

import java.util.Objects;
import java.util.concurrent.Callable;

public class UtilityCallable implements Callable<Boolean> {

    UtilityTasks tsk;
    String ctnt, lnk, ttle;
    int sent;

    UtilityCallable( final int code,  final String content){
        Objects.requireNonNull(this.tsk = UtilityTasks.UtilityTaskDecode(code));
        this.ctnt = content;
    }

    UtilityCallable(final int code, String link, int sentiment, String title){
        Objects.requireNonNull(this.tsk = UtilityTasks.UtilityTaskDecode(code));
        this.sent = sentiment;
        this.lnk = link;
        this.ttle = title;
    }

    UtilityCallable(final int code, final String content, final int sentiment){
        Objects.requireNonNull(this.tsk = UtilityTasks.UtilityTaskDecode(code));
        this.sent = sentiment;
        this.ctnt = content;
    }

    @Override
    public Boolean call(){

        switch (tsk) {
            case WRITE_VISIT_LIST -> {
                Utility.writeVisitList(ctnt);
                return true;
            }

            case WRITE_OPTIMAL_RESULT_LIST -> {
                Utility.writeURLOptimalSentimentResult(lnk, sent, ttle);
                return true;
            }

            case WRITE_SENTIMENT_RESULT -> {
                Utility.writeSentimentResult(ctnt,SentimentType.fromInt(sent));
                return true;
            }
        }
        return false;
    }
}
