# Crab

<!-- ABOUT THE PROJECT -->
## About Crab

Crab is a Sentiment focused web crawler developed in Java. Sentiment analysis is done in Crab on the anchor text of linked pages from a crawled page. Crab is useful for crawling web pages which are related to a  set of topics.

The idea of Crab was based off the following paper:

https://dl.acm.org/doi/10.1145/2396761.2398564

The Crawler is still in early stages of development.

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


