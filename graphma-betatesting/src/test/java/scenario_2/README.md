# Scenario 2: Compose and Run Graph Pipelines Using GraphMa

In this scenario, beta testers are required to implement three example pipelines using the existing operators found in the GraphMa library. This exercise will demonstrate how to construct graph processing pipelines from components provided within the GraphMa framework.

## Prerequisites
- Everything is working as described in the main README for betatesting.

## Overview
- **Operators Directory**: Operators can be found in the `graphma-core` subproject under `graphma.compute.operator`.
- **Example Pipeline**: See `scenario_2.ExampleTest.test_kcore_clustering` for an example of how to implement a pipeline.
- **Local Maven Repository**: The Magma project, which underlies the GraphMa framework, is included as a local Maven repository at `/graph-inceptor-graphma/localMavenRepo/magma/magma-core/0.0.4`.

## Pipeline Composition
When using GraphMa operators, note that the Seq API shown in `scenario_2.ExampleTest.test_sequence` is not yet available. Pipelines based on standard Magma operators (see localMavenRepo) should follow the Seq API, custom operators (GraphMa) require pipelines to be composed in a contravariant manner (opposite order).

### Implementation Freedom
- **Operator Choice**: You are not limited to using a specific set of operators. Feel free to use any operators that fit your pipeline design.
- **Starting Point**: Each pipeline must start with a `Traversable` or `DataSource`.

### Data Handling
- **Data Source**: Preliminary traverser implementations for various graph formats are available in the `graphma-data` project under the `formats` directory.
- **Test Data**: Test data is available in the `demoDataRepo`. It is recommended to use the SSDB database, which provides access to SuiteSparse by iterating over each .mtx file and passing the resulting traversers down the pipeline.

## Running the Pipelines
- Compile and run your pipelines using the standard Java execution commands or through your IDE. We use junit-jupiter tests. The well-established IDEs like Intellj or Eclipse should come with support.

## Additional Resources
For further examples of pipeline implementations, refer to the tests in the `graphma-core` subproject. These examples can provide insights into various ways to compose and execute complex graph processing tasks using GraphMa.

