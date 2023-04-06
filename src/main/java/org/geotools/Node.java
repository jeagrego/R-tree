package org.geotools;

import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Objects;

public class Node {
    ArrayList<Node> nodes;
    Polygon p; //MBR or complex polygon
    public Node(int maxN){
        nodes = new ArrayList<>(maxN);
    }

    public boolean isMBR(){
        return !isLeaf();
    }
    public boolean isLeaf(){
        for (Node children_nodes: nodes){
            if (!Objects.equals(children_nodes, null)){
                return false;
            }
        }
        return true;
    }
}
