package org.geotools;

import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;

public class Node {
    ArrayList<Node> subnodes;
    Polygon MBR;
    public Node(int maxN, Polygon p){
        this.subnodes = new ArrayList<>(maxN);
        this.MBR = p;
    }

    public double[] getXcoords(){
        return new double[]{MBR.getCoordinates()[0].getX(), MBR.getCoordinates()[2].getX()};
    }

    public double[] getYcoords(){
        return new double[]{MBR.getCoordinates()[0].getY(), MBR.getCoordinates()[1].getY()};
    }

    public ArrayList<Node> getSubnodes() {
        return subnodes;
    }

    public void addNode(Node node) { //possible to expand MBR at the same time
        this.subnodes.add(node);
    }

    public Polygon getPolygon() {
        return this.MBR;
    }

    public void setPolygon(Polygon p) {
        this.MBR = p;
    }

    public void removeSubnodes() {
        this.subnodes = new ArrayList<>();
    }
}
