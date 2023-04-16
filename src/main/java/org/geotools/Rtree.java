package org.geotools;

import org.geotools.geometry.jts.GeometryBuilder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

public class Rtree {
    private Node n;
    int N=3; // max amount of leaves or nodes

    public Rtree(int N){
        this.n = new Node(N,"root", (Polygon) null);
        this.N = N;
    }

    public Node addLeaf(Node node, String label, MultiPolygon p) {
        if(node.isLeaf() || node.getSubnodes().get(0).isLeaf()){
            node.addNode(new Leaf(3, label, p));
        }
        else{
            Node son_n = chooseNode(node, p);
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
        double minX; double maxX; double minY; double maxY;
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

    private Node chooseNode(Node node, MultiPolygon p) {
        if (node.isLeaf()){
            return node;
        }
        double best_area = -1;
        Node best_node = node;
        for(Node child: node.getSubnodes()) {
            //pretend to expand MBR
            double minX = Math.min(node.getXcoords()[0], this.getXcoordsMultiPoly(p)[0]); double maxX = Math.max(node.getXcoords()[1], this.getXcoordsMultiPoly(p)[1]);
            double minY = Math.min(node.getYcoords()[0], this.getYcoordsMultiPoly(p)[0]); double maxY = Math.max(node.getYcoords()[1], this.getYcoordsMultiPoly(p)[1]);
            double distancex = calculateDistance(new double[]{minX, maxX});
            double distancey = calculateDistance(new double[]{minY, maxY});
            double area = distancex*distancey; //calculate the area of the new MBR
            if(best_area== -1 || best_area>area){
                best_area = area;
                best_node = child;
            }
        }
        return chooseNode(best_node, p);
    }

    private double calculateDistance(double[] coords) {
        return Math.sqrt(Math.pow(coords[0] - coords[1],2));
    }

    private Node splitQuadratic(Node node) {
        return null;
    }

    private double[] getXcoordsPoly(Polygon p){
        return new double[]{p.getCoordinates()[0].getX(), p.getCoordinates()[2].getX()};
    }

    private double[] getYcoordsPoly(Polygon p){
        return new double[]{p.getCoordinates()[0].getY(), p.getCoordinates()[1].getY()};
    }

    private double[] getXcoordsMultiPoly(MultiPolygon p){
        return new double[]{p.getBoundary().getEnvelope().getCoordinates()[0].getX(), p.getBoundary().getEnvelope().getCoordinates()[2].getX()};
    }

    private double[] getYcoordsMultiPoly(MultiPolygon p){
        return new double[]{p.getBoundary().getEnvelope().getCoordinates()[0].getY(), p.getBoundary().getEnvelope().getCoordinates()[1].getY()};
    }

    public Node getRoot() {
        return n;
    }
}
