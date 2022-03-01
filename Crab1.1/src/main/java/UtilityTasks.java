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
 * An Enum class to map Utility codes to a label for correct tasking for Threads.
 *
 */

import java.util.NoSuchElementException;

public enum UtilityTasks {
    WRITE_VISIT_LIST,
    WRITE_OPTIMAL_RESULT_LIST,
    READ_VISIT_LIST,
    READ_OPTIMAL_RESULT_LIST
    ;

    public static UtilityTasks UtilityTaskDecode(int code){

        return switch(code){
            case 1 -> WRITE_VISIT_LIST;
            case 2 -> WRITE_OPTIMAL_RESULT_LIST;
            case 3 -> READ_VISIT_LIST;
            case 4 -> READ_OPTIMAL_RESULT_LIST;
            default -> throw new NoSuchElementException("No utility code for integer: " + code);
        };
    }
}



