package org.geotools;

import org.geotools.geometry.jts.GeometryBuilder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

public class Rtree {
    private Node n;
    int N=3; // max amount of leaves or nodes

    public Rtree(int N){
        this.n = new Node(N,"root",null);
        this.N = N;
    }

    public Node addLeaf(Node node, String label, Geometry p) {
        if(node.isLeaf() || node.getSubnodes().get(0).isLeaf()){
            node.addNode(new Leaf(3, label, p));
        }
        else{
            Node son_n = chooseNode(node, (Polygon) p);
            Node new_node = addLeaf(son_n, label, p);
            if(new_node != null){
                node.addNode(new_node);
            }
        }
        node.setPolygon(expandPolygon(node.getPolygon(), p));
        if(node.getSubnodes().size() >= N){
            return splitQuadratic(node);
        }
        else{return null;}
    }

    private Polygon expandPolygon(Polygon p, Geometry p_leaf) {
        GeometryBuilder gb = new GeometryBuilder();
        if(p == null){
            return gb.box(this.getXcoordsMultiPoly((MultiPolygon) p_leaf)[0],
                    this.getYcoordsMultiPoly((MultiPolygon) p_leaf)[0],
                    this.getXcoordsMultiPoly((MultiPolygon) p_leaf)[1],
                    this.getYcoordsMultiPoly((MultiPolygon) p_leaf)[1]);
        }
        int minX; int maxX; int minY; int maxY;
        minX = Math.min(this.getXcoordsMultiPoly((MultiPolygon) p_leaf)[0], this.getXcoordsPoly(p)[0]);
        maxX = Math.max(this.getXcoordsMultiPoly((MultiPolygon) p_leaf)[1], this.getXcoordsPoly(p)[1]);
        minY = Math.min(this.getYcoordsMultiPoly((MultiPolygon) p_leaf)[0], this.getYcoordsPoly(p)[0]);
        maxY = Math.max(this.getYcoordsMultiPoly((MultiPolygon) p_leaf)[1], this.getYcoordsPoly(p)[1]);
        return gb.box(minX,
                minY,
                maxX,
                maxY
        );
    }

    private Node chooseNode(Node node, Polygon p) {
        if (node.isLeaf()){
            return node;
        }
        int best_area = -1;
        Node best_node = node;
        for(Node child: node.getSubnodes()) {
            //pretend to expand MBR
            int minX = Math.min(node.getXcoords()[0], this.getXcoordsPoly(p)[0]); int maxX = Math.max(node.getXcoords()[1], this.getXcoordsPoly(p)[1]);
            int minY = Math.min(node.getYcoords()[0], this.getYcoordsPoly(p)[0]); int maxY = Math.max(node.getYcoords()[1], this.getYcoordsPoly(p)[1]);
            int distancex = calculateDistance(new int[]{minX, maxX});
            int distancey = calculateDistance(new int[]{minY, maxY});
            int area = distancex*distancey; //calculate the area of the new MBR
            if(best_area== -1 || best_area>area){
                best_area = area;
                best_node = child;
            }
        }
        return chooseNode(best_node, p);
    }

    private int calculateDistance(int[] coords) {
        return (int) Math.sqrt(Math.pow(coords[0] - coords[1],2));
    }

    private Node splitQuadratic(Node node) {
        return null;
    }

    private int[] getXcoordsPoly(Polygon p){
        return new int[]{(int) p.getCoordinates()[0].getX(), (int) p.getCoordinates()[2].getX()};
    }

    private int[] getYcoordsPoly(Polygon p){
        return new int[]{(int) p.getCoordinates()[0].getY(), (int) p.getCoordinates()[1].getY()};
    }

    private int[] getXcoordsMultiPoly(MultiPolygon p){
        return new int[]{(int) p.getBoundary().getEnvelope().getCoordinates()[0].getX(), (int) p.getBoundary().getEnvelope().getCoordinates()[2].getX()};
    }

    private int[] getYcoordsMultiPoly(MultiPolygon p){
        return new int[]{(int) p.getBoundary().getEnvelope().getCoordinates()[0].getY(), (int) p.getBoundary().getEnvelope().getCoordinates()[1].getY()};
    }

    public Node getRoot() {
        return n;
    }
}
