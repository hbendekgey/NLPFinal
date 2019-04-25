# Perplexity Episode Comparer

## Setup
Place a training file called `corpus.dat` containing each episode on its own line within `data/`.
Place a directory called `episodes` containing each each episode as its own file within `data/`.

## Building
Run `./build.sh` from this directory to bulid

## Running
Run `./perplexity.sh data/corpus.dat data/episodes/ <LM for lambda model | DI for discount model> <lambda/discount value> | sort -k 2'

To print and order the episodes by perplexity
