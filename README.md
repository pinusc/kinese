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
