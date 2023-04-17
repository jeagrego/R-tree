package org.geotools;

import org.geotools.geometry.jts.GeometryBuilder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import java.util.Arrays;

public class Rtree {
    private Node n;
    int N=3; // max amount of leaves or nodes

    public Rtree(int N){
        this.n = new Node(N, (Polygon) null);
        this.N = N;
    }

    public Node addLeaf(Node node, String label, MultiPolygon p) {
        if(node.getSubnodes().size() == 0 || node.getSubnodes().get(0).getSubnodes().size() ==0){
            node.addNode(new Leaf(3, label, p));
        }
        else{
            Node son_n = chooseNode(node, (Polygon) p.getEnvelope());
            Node new_node = addLeaf(son_n, label, p);
            if(new_node != null){
                node.addNode(new_node);
            }
        }
        node.setPolygon(expandPolygon(node.getPolygon(), (Polygon) p.getEnvelope()));
        if(node.getSubnodes().size() > N){
            System.out.println("SPLITTING "+node.getSubnodes().size());
            return splitQuadratic(node);
        }
        else{return null;}
    }

    private Polygon expandPolygon(Polygon p, Polygon p_leaf) {
        GeometryBuilder gb = new GeometryBuilder();
        if(p == null){
            return p_leaf;
        }
        double minX; double maxX; double minY; double maxY;
        minX = Math.min(this.getXcoordsPoly(p_leaf)[0], this.getXcoordsPoly(p)[0]);
        maxX = Math.max(this.getXcoordsPoly(p_leaf)[1], this.getXcoordsPoly(p)[1]);
        minY = Math.min(this.getYcoordsPoly(p_leaf)[0], this.getYcoordsPoly(p)[0]);
        maxY = Math.max(this.getYcoordsPoly(p_leaf)[1], this.getYcoordsPoly(p)[1]);
        return gb.box(minX,
                minY,
                maxX,
                maxY
        );
    }

    private Node chooseNode(Node node, Polygon p) {
        if (node.getSubnodes().size() == 0 || node.getSubnodes().get(0).getSubnodes().size() ==0){
            return node;
        }
        double best_area = -1;
        Node best_node = node;
        for(Node child: node.getSubnodes()) {
            if (child.getSubnodes().size() == 0 || child.getSubnodes().get(0).getSubnodes().size() ==0){
                System.out.println("name of country to test " + ((Leaf)child.getSubnodes().get(0)).getName());
            }
            double areaPrevious = pretendToExpandMBR(child.getPolygon(), child.getPolygon());
            double areaAfter = pretendToExpandMBR(child.getPolygon(), p);
            if(best_area== -1 || best_area>areaAfter-areaPrevious){
                best_area = areaAfter-areaPrevious;
                best_node = child;
            }
        }
        return best_node;
    }

    private double pretendToExpandMBR(Polygon p1, Polygon p2) {
        //pretend to expand MBR
        double minX = Math.min(this.getXcoordsPoly(p1)[0], this.getXcoordsPoly(p2)[0]);
        double maxX = Math.max(this.getXcoordsPoly(p1)[1], this.getXcoordsPoly(p2)[1]);
        double minY = Math.min(this.getYcoordsPoly(p1)[0], this.getYcoordsPoly(p2)[0]);
        double maxY = Math.max(this.getYcoordsPoly(p1)[1], this.getYcoordsPoly(p2)[1]);
        double distancex = calculateDistance(new double[]{minX, maxX});
        double distancey = calculateDistance(new double[]{minY, maxY});
        return distancex*distancey;
    }

    private double calculateDistance(double[] coords) {
        return Math.sqrt(Math.pow(coords[0] - coords[1],2));
    }

    private Node splitQuadratic(Node node) {
        int i_chosen = 0; int j_chosen = 0;
        double best_area = -1;
        for(int i = 0; i<node.getSubnodes().size(); i++){
            for(int j = 0; j<node.getSubnodes().size(); j++){
                if(i != j){
                    double area = pretendToExpandMBR(node.getSubnodes().get(i).getPolygon(), node.getSubnodes().get(j).getPolygon());
                    if(best_area== -1 || best_area<area){
                        best_area = area;
                        i_chosen = i; j_chosen = j;
                    }
                }
            }
        }
        Node n1 = new Node(3, node.getSubnodes().get(i_chosen).getPolygon());
        n1.addNode(node.getSubnodes().get(i_chosen));
        Node n2 = new Node(3, node.getSubnodes().get(j_chosen).getPolygon());
        n2.addNode(node.getSubnodes().get(j_chosen));
        for(int i = 0; i<node.getSubnodes().size(); i++){
            if(i!= i_chosen && i != j_chosen){
                Node cur_node = node.getSubnodes().get(i);
                double area1 = pretendToExpandMBR(n1.getPolygon(), cur_node.getPolygon());
                double area2 = pretendToExpandMBR(n2.getPolygon(), cur_node.getPolygon());
                if(area1 < area2){
                    n1.setPolygon(expandPolygon(n1.getPolygon(), cur_node.getPolygon()));
                    n1.addNode(cur_node);
                }else{
                    n2.setPolygon(expandPolygon(n2.getPolygon(), cur_node.getPolygon()));
                    n2.addNode(cur_node);
                }
            }
        }
        node.removeSubnodes();
        node.setPolygon(n1.getPolygon());
        node.setPolygon(expandPolygon(node.getPolygon(), n2.getPolygon()));
        node.addNode(n1);node.addNode(n2);
        return node;
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
