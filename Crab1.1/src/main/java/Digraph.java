
/* Digraph.java
    This class is for creation of a Directed Graph.

    Created: 1/02/2022
 */

import java.util.LinkedList;

public class Digraph {
    private final int V;
    private int E;
    private String URL;
    private LinkedList<Integer>[] adj;

    public Digraph(int V, String link) {
        this.V = V;
        this.E = 0;
        adj = new LinkedList[V];
        for(int i =0; i < V; i++)
            adj[i] = new LinkedList<Integer>();
    }

    //Gives the number of Vertices in the Digraph
    public int V() { return V;}

    //Gives the number of Edges in the Digraph
    public int E() { return E;}

    public void addEdge(int v, int w){
        adj[v].add(w);
        E++;
    }

    public void addEdge(int v, int w, int weight){
        adj[v].add(w);
        E++;
    }

}
