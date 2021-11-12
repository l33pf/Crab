# Crab

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>

<!-- ABOUT THE PROJECT -->
## About Crab

[![Product Name Screen Shot][product-screenshot]](https://example.com)

Crab is a Sentiment focused web crawler built in Java. The idea of Crab was based off the following paper:
https://dl.acm.org/doi/10.1145/2396761.2398564 (copy also available in the repo). The original goal for Crab was just a refresh of the author's (l33pf) programming skills and 
knowledge whilst aiming to develop something practical as a potential aid for the author's trading, This however has morphed into a somewhat of a hobby project which can be used to do news mining through sentiment analysis. Crab is still currently in development as such bugs will exist, it is suggested that any results derived from it to be taken with a pinch of salt !

<p align="right">(<a href="#top">back to top</a>)</p>

### Built With

Crab use's Stanford's CoreNLP as its ML library, Jsoup for parsing and Postgres for storage. (<b>NOTE</b>: This list may change as development advances)


* [CoreNLP](https://stanfordnlp.github.io/CoreNLP/)
* [Jsoup](https://jsoup.org/)
* [PostgresSQL](https://www.postgresql.org/)

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- ROADMAP -->
## Roadmap

- [x] Resolve Multithreading issues
-  Testing of PostgresSQL capability 
-  Add "front-facing" components (react.js, Spring etc) to present results better
-  Improve on Sentiment results (see paper list in repo)

See the [open issues](https://github.com/othneildrew/Best-README-Template/issues) for a full list of proposed features (and known issues).

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- USAGE EXAMPLES -->
## Usage

Examples of URL 'sets' are within the .CSV files found in the repo these consist of links we class as the URL seed set and then crawl through each page. one example
is for Crawling Crypto newsites, another is for Sport newsites and Weather. 


<!-- CONTRIBUTING -->
## Contributing

People are free to contribute we suggest looking at the issues and the projects tab for some ideas to contribute, Currently need help on text mining extracted
pages for more in-depth sentiment analysis. Any suggestions to make the project better are always helpful.

<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE.txt` for more information.


