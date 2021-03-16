# graphql-test-source

A GraphQL server that provides queries to test the features of the
[Grafana](https://grafana.com) [GraphQL datasource
plugin](https://github.com/fifemon/graphql-datasource).

## Usage

1. Run the server with [leiningen](https://github.com/technomancy/leiningen):

    `lein run`
    
    or with Docker:
    
    `docker run -it --rm -p 8888:8888 retzkek/graphql-test-source`
    
2. [Install
   Grafana](https://grafana.com/docs/grafana/latest/installation/?pg=docs) and
   the [GraphQL Datasource
   Plugin](https://grafana.com/grafana/plugins/fifemon-graphql-datasource/?tab=installation).

3. Add a GraphQL Datasource to Grafana pointing at the server (`http://localhost:8888/api`)

5. Import the [sample dashboard](https://grafana.com/grafana/dashboards/14079) into Grafana.

## License

Copyright © 2021 Fermi National Accelerator Laboratory

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
