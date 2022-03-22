# Crab

## Update
<b>Crab 1.2</b> is the latest version of Crab please see the commit: (https://github.com/l33pf/Crab/commit/418d5c4f1fa339b9f63ac27df0a922fba2dc713f) for details. 

1.2 is a scaled down version of 1.1 with more of a focus on performance and correctness of the key crawler features implemented in 1.1, Serialization is now done with binary files. Crab now only takes in and puts out CSV files allowing any user to export their crawl data for further use in another application. 

<b>Note:</b> Crab 1.1 is now deprecated, the code however will stay on the repo for archive.

<!-- ABOUT THE PROJECT -->
## About Crab

Crab is a Sentiment focused web crawler developed in Java. Sentiment analysis is done in Crab on the anchor text of linked pages from a crawled page. Crab is useful for crawling web pages which are related to a  set of topics.

The idea of Crab was based off the following paper:

https://dl.acm.org/doi/10.1145/2396761.2398564

The Crawler is still in early stages of development.

<!-- FEATURES  -->
## Features
<b>Optimal Crawl</b>: 

Crab will crawl through the seed set, each seed page has an associated thread which will run sentiment analysis on links of the seed page find the optimally "best" link based on the sentiment analysis on its title then record it to file. If the optimal depth flag is set (see <i>Crab.java</i>) Crab will add all links from a given page onto the URL stack for crawling otherwise the crawl finishes when the set of links crawled is exhausted/empty the same applies for the optimal depth option.

The output of the crawl is a record of the best sentiment pages.

<b>NOTE</b>: Using Crab in this mode please be aware that currently the default settings of CoreNLP are used we don't use a classifer with a specific training set/lexicon in mind though this would be something welcomed given the accuracy on finding 'positively' labelled pages needs improving.

<b>Results</b> after several crawling runs can be found at:
https://github.com/l33pf/Crab/blob/main/Crab1.1/CrabOptimalCrawl.csv

<b>Keyword Crawl</b>:

Crab can crawl/scrape pages based on given keywords, you can start a keyword crawl by setting the keyword crawl flag (see Crab.java) and creating
Keyword objects and adding them to the keyword queue. Every keyword you want to retrieve pages for has an associated object which is added to the keyword queue. Crab
use's POS tagging on a page's title and then based on the tags derived see's if it can match a keyword if so it then runs a full sentiment (the whole page's content
has full sentiment analysis done) and then records the result. Each keyword object has three data structures which can be used to store sentiment results, these can then
be used further for post-crawl analysis. 

You can find a given record of <b>results</b> of a crawl based on keywords inputted at:
https://github.com/l33pf/Crab/blob/main/Crab1.1/CrabURLKeywordMatches.csv


(which had a crawl run of roughly 3 minutes before it was ended.)

a flow of the keyword crawl is demonstrated in the image below:


![keywordcrawl drawio](https://user-images.githubusercontent.com/15945205/158431131-1dd1c0dd-504d-4b1e-9cdc-7153818d95eb.png)

Note that for a keyword crawl to be successful you must add tags this can be done by:
```
Crab.tagsToSearch.add("NNP");
```
for a list of POS tags [click here](https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html)


Creating a Keyword object is done simply by calling the constructor:
```
KeywordClass example = new KeywordClass("MyKeyword");
```
Then simply add this to the Keyword Queue and set the keyword crawl flag
```
Crab.keyWordQueue.add(example);
Crab.keyWordCrawl = true;
```

Crab will need a inputted CSV file of a URL seed set to run correctly, an example is shown in <i>Test.csv</i>

<b>Note on Keyword Crawl:</b> Although complete as a feature the full sentiment part is still being worked on.

### Built With

Crab use's Stanford's CoreNLP as its ML library, Jsoup for HTML parsing, Log4j for logging purposes, OpenCSV for storage and Jackson for JSON exporting.(<b>NOTE</b>: This list may change as development advances)

* [CoreNLP](https://stanfordnlp.github.io/CoreNLP/)
* [Jsoup](https://jsoup.org/)
* [OpenCSV](http://opencsv.sourceforge.net/)

<!-- ROADMAP -->
## Roadmap
- [x] Crawl based on Keywords
- [ ] Database Integration (PostgreSQL)

See the [open issues](https://github.com/l33pf/Crab/issues) for a full list of proposed features (and known issues).

<!-- USAGE EXAMPLES -->
## Usage

Examples of URL 'sets' are within the .CSV files found in the repo these consist of links we class as the URL seed set and then crawl through each page. one example
is for Crawling Crypto newsites, another is for Sport and another for general news. 

<!-- JVM -->
## JVM
Crab utilises the following garbage collector:
```
-XX:UseParallelOldGC
```
given it has no impact on the running time until the collection occurs, and uses threads for both minor and major collections. More
info can be found [Here](https://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/parallel.html).
```
-XX:TieredCompilation
```
Tiered Compilation is used so that all methods are interpreted/compiled at C2, This is part of the JVM warmup.
```
-XgcPrio:throughput
```
This flag is set so the garbage collection is optimised for application throughput. Optimisation can also be achieved by
altering the min ```-Xms``` and max heap ```-Xmx``` sizes further info can be found [Here](https://docs.oracle.com/cd/E13150_01/jrockit_jvm/jrockit/geninfo/diagnos/bestpractices.html#wp1089834).

Profiling on an ``` Intel i5 @2.9 GHz ```   with the JVM optimisations in place showed a GC time of 1.4s on one crawling run.

<!-- CONTRIBUTING -->
## Contributing

People are free to contribute we suggest looking at the issues and the projects tab for some ideas to contribute.

<!-- LICENSE -->
## License

Distributed under the Apache License 2.0. See `LICENSE.txt` for more information.


