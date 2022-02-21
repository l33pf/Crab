# Crab

<!-- ABOUT THE PROJECT -->
## About Crab

Crab is a Sentiment focused web crawler developed in Java. Sentiment analysis is done in Crab on the anchor text of linked pages from a crawled page. Crab is useful for crawling web pages which are related to a  set of topics.

The idea of Crab was based off the following paper:

https://dl.acm.org/doi/10.1145/2396761.2398564

The Crawler is still in early stages of development.

<!-- FEATURES  -->
## Features
<b>Optimal Crawl</b>: Crab will crawl through the seed set, each seed page has an associated thread which will run sentiment analysis on links of the seed page find the optimally "best" link based on the sentiment analysis on its title then run a full sentiment analysis on the contents of the page whilst the page links are pushed into the seed set and the process is repeated until the crawl is exhausted. 

The output of the crawl is a series of files displaying the best sentiment pages, and the optimally best sentiment pages on each round of the crawl. In this mode Crab can also keep track of results and generate a sentiment distribution of links from the pages it has crawled. An example plot from a small sentiment distribution of Crypto news sites can be seen in the below figure.

![Figure 2022-02-09 221141](https://user-images.githubusercontent.com/15945205/153299177-f38ead78-e482-41cb-89e3-4ebe3320a577.png)

<b>NOTE</b>: Using Crab in this mode please be aware that currently the default settings of CoreNLP are used we don't use a classifer with a specific training set/lexicon in mind though this would be something welcomed given the accuracy on finding 'positively' labelled pages needs improving.

<b>Results</b> after several crawling runs can be found at:
https://github.com/l33pf/Crab/blob/main/Crab1.1/CrabOptimalCrawl.csv

### Built With

Crab use's Stanford's CoreNLP as its ML library, Jsoup for HTML parsing, Log4j for logging purposes, OpenCSV for storage and Jackson for JSON exporting.(<b>NOTE</b>: This list may change as development advances)

* [CoreNLP](https://stanfordnlp.github.io/CoreNLP/)
* [Jsoup](https://jsoup.org/)
* [Log4j](https://logging.apache.org/log4j/)
* [OpenCSV](http://opencsv.sourceforge.net/)
* [Jackson](https://github.com/FasterXML/jackson)

<!-- ROADMAP -->
## Roadmap
- [ ] Crawl based on Keywords
- [ ] Concurrent logging
- [ ] Spring Framework Implementation for web app deployment
- [ ] More export options (PDF etc.)

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


