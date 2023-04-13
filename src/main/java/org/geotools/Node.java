package org.geotools;

import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;

public class Node {
    String name;
    ArrayList<Node> subnodes;
    Polygon MBR;
    public Node(int maxN, String id, Polygon p){
        this.subnodes = new ArrayList<>(maxN);
        this.name = id;
        this.MBR = p;
    }

    public int[] getXcoords(){
        if (isMBR()) {
            return new int[]{(int) MBR.getCoordinates()[0].getX(), (int) MBR.getCoordinates()[2].getX()};
        }
        return null;
    }

    public int[] getYcoords(){
        if (isMBR()) {
            return new int[]{(int) MBR.getCoordinates()[0].getY(), (int) MBR.getCoordinates()[1].getY()};
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

    public Polygon getPolygon() {
        return this.MBR;
    }

    public void setPolygon(Polygon p) {
        this.MBR = p;
    }
}
