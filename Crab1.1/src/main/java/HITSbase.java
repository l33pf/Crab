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

/* HITSbase.java
    This class is used to extract a base set based on the keyword selected to search
    for within Crab's URL Seed Set. From there a subgraph is built from the base
    set and the HITS algorithm can then be applied.

    Created: 28/01/2022
 */

import java.io.IOException;
import java.util.HashSet;

import com.opencsv.exceptions.CsvException;
import org.jsoup.*;
import org.jsoup.nodes.Document;

class HITSbase {

    private static HashSet<String> baseSet = new HashSet<>();
    private static CrabStack stack = new CrabStack();

    public static void getBaseSet(String keyword) throws IOException, CsvException {
        //read in the initial seed set from the CSV file
        boolean stack_read_in = Utility.readIn(stack);

        if(stack_read_in){
            while(stack.size() != 0){
                final Document doc = Jsoup.connect(stack.pop()).get();
            }
        }


    }

}
