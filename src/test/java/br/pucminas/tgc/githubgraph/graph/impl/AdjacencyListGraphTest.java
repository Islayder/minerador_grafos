package br.pucminas.tgc.githubgraph.graph.impl;

import br.pucminas.tgc.githubgraph.graph.AbstractGraph;

class AdjacencyListGraphTest extends AbstractGraphContractTest {

    @Override
    protected AbstractGraph createGraph(int vertexCount) {
        return new AdjacencyListGraph(vertexCount);
    }
}
