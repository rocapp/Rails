package rails.game.specific._1851;

import java.util.Set;

import org.jgrapht.graph.SimpleGraph;

import rails.algorithms.NetworkEdge;
import rails.algorithms.NetworkGraphBuilder;
import rails.algorithms.NetworkGraphModifier;
import rails.algorithms.NetworkVertex;
import rails.game.GameManager;
import rails.game.GameManagerI;
import rails.game.MapHex;

public class BirminghamTileModifier implements NetworkGraphModifier {

    public void modifyGraph(NetworkGraphBuilder graphBuilder) {
        
        GameManagerI gm = GameManager.getInstance();
        SimpleGraph<NetworkVertex, NetworkEdge> graph = graphBuilder.getMapGraph();
        
        // 1. check Phase
        // this is a violation of the assumption that the track network only dependents on the map configuration
        // but not on other things (like phases)
        if (gm.getCurrentPhase().getIndex() >= 2 ) return;
        
        // 2. retrieve Birmingham vertices ...
        MapHex birmingHex = gm.getMapManager().getHex("J12");
        Set<NetworkVertex> birmingVertices = NetworkVertex.getVerticesByHex(graph.vertexSet(), birmingHex);

        // 3 ... and remove them from the graph
        graph.removeAllVertices(birmingVertices);
        
    }

}
