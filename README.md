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

At the time of writing, the library can not be retrieved from Maven central repository automatically through `project.clj`. Instead, the jar file has to be downloaded manually from [maven.org](https://repo1.maven.org/maven2/org/fnlp/fnlp-core/2.1/fnlp-core-2.1.jar) and placed in `resources/`. Additionally, all three NLP models (`seg.m`, `dep.m`, `pos.m`)must be downloaded from the project's GitHub releases and placed in a new folder called `models`. 

To do this from shell:

    $ cd resources
    $ wget "https://repo1.maven.org/maven2/org/fnlp/fnlp-core/2.1/fnlp-core-2.1.jar"
    $ cd ..
    $ mkdir models && cd models
    $ wget "https://github.com/FudanNLP/fnlp/releases/download/v2.1/dep.m"
    $ wget "https://github.com/FudanNLP/fnlp/releases/download/v2.1/seg.m"
    $ wget "https://github.com/FudanNLP/fnlp/releases/download/v2.1/pos.m"
