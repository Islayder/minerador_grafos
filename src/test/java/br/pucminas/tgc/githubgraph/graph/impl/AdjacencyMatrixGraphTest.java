package br.pucminas.tgc.githubgraph.graph.impl;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;

class AdjacencyMatrixGraphTest extends AbstractGraphContractTest {

    @Override
    protected AbstractGraph createGraph(int vertexCount) {
        return new AdjacencyMatrixGraph(vertexCount);
    }
}
