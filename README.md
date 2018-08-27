# kinese
*kinese* is a webapp to help mandarin learners to read chinese. The backend is written in clojure+ring, the frontend in clojurescript+reagent.

**Features:**
- Character dictionary lookup
- Character tone coloring

**Coming soon:**
- Word segmentation
- Word dictionary lookup

## Build
To build, you'll need ``leiningen``.
- In the project directory, run ``$ lein install``
- Run ``$ lein run``
- In a separate terminal (also in the project root), run ``$ lein figwheel``
- Visit at ``localhost:3000``

### FNLP dependency

The project depends on the [FudanNLP](https://github.com/FudanNLP/fnlp) library to segment text. 

At the time of writing, the project's POM on maven central is broken, and it can not be added to leiningen's managed dependencies. Additionally, the three needed model files have to be downloaded from the project's Github repo.

To do all this automatically, simply run

    $ ./download-fnlp.sh
