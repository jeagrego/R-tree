package org.geotools;

import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Objects;

public class Node {
    String name;
    ArrayList<Node> subnodes;
    Polygon p; //MBR or complex polygon
    public Node(int maxN, String id, Polygon p){
        this.subnodes = new ArrayList<>(maxN);
        this.name = id;
        this.p = p;
    }

    public int[] getXcoords(){
        if (isMBR()) {
            return new int[]{(int) p.getCoordinates()[0].getX(), (int) p.getCoordinates()[2].getX()};
        }
        return null;
    }

    public int[] getYcoords(){
        if (isMBR()) {
            return new int[]{(int) p.getCoordinates()[0].getY(), (int) p.getCoordinates()[1].getY()};
        }
        return null;
    }

    public boolean isMBR(){
        return !isLeaf();
    }
    public boolean isLeaf(){
        return subnodes.size() == 0;
    }

    public ArrayList<Node> getSubnodes() {
        return subnodes;
    }

    public void addNode(Node node) {
        this.subnodes.add(node);
    }
}
