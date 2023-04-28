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

    /***
     * Get the min_x and the max_x coordinates of the MBR Polygon
     * @return int[0] min_x, int[1] max_x;
     */
    public double[] getXcoords(){
        return new double[]{MBR.getCoordinates()[0].getX(), MBR.getCoordinates()[2].getX()};
    }

    /***
     * Get the min_y and the max_y coordinates of the MBR Polygon
     * @return int[0] min_y, int[1] max_y;
     */
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
