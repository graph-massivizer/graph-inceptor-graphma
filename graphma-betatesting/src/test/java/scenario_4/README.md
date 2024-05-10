# Scenario 4: Use GraphMa to Implement Custom Traverser Operators

In this scenario, beta-testers will extend the GraphMa library by implementing a custom traverser for CSV files that contain edge lists. This involves creating a traverser that can read and interpret the structure of an edge list stored in a CSV file.

## Objective
Implement a custom traverser for `edgeList.csv` to parse graph edges from the CSV and integrate it into a graph processing pipeline.

## Implementation Details
Your task is to implement the `EdgeListTraverser` class, which should extend the functionality of GraphMa's traverser to handle CSV files containing edge lists. Use the provided `BetaTest` class template to integrate your traverser and test its functionality through unit tests.

### Key Components to Implement:
- **EdgeListTraverser:** A custom traverser that reads edge list data from a CSV file.
- **Testing Methods:** Implement `test_scenario_4_tryNext`, `test_scenario_4_forNext`, and `test_scenario_4_whileNext` to demonstrate the functionality of your traverser.

## Examples
There are implementations of traversers for different graph formats in `graph-inceptor-graphma/graphma-data/src/main/java/formats`. Especially the mtx implementation could be interesting.