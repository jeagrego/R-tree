package org.geotools;

import org.locationtech.jts.geom.Polygon;

public class Rtree {
    private Node n;
    int N=3; // max amount of leaves or nodes

    public Rtree(int N){
        this.n = new Node(N,"root",null);
        this.N = N;
    }

    public Node addLeaf(Node node, String label, Polygon p) {
        if(n.isLeaf() || n.getSubnodes().get(0).isLeaf()){
            n.addNode(new Node(3, label, p));
        }
        else{
            Node son_n = chooseNode(node, p);
            Node new_node = addLeaf(son_n, label, p);
            if(new_node != null){
                this.n.addNode(new_node);
            }
        }
        //expand node mbr to include polygon;
        if(node.getSubnodes().size() >= N){
            return split(node);
        }
        else{return null;}
    }

    private Node chooseNode(Node node, Polygon p) {
        if (node.isLeaf()){
            return node;
        }
        int best_dist = -1;
        Node best_node = node;
        for(Node child: node.getSubnodes()) {
            int node_dist_x = calculateDistance(node.getXcoords());
            int node_dist_y = calculateDistance(node.getYcoords());
            int parent_dist_x = calculateDistance(this.getXcoordsPoly(p));
            int parent_dist_y = calculateDistance(this.getYcoordsPoly(p));
            int distancex = calculateDistance(new int[]{node_dist_x, parent_dist_x});
            int distancey = calculateDistance(new int[]{node_dist_y, parent_dist_y});
            int distance = distancex+distancey;
            if(best_dist== -1 || best_dist>distance){
                best_dist = distance;
                best_node = child;
            }
        }
        return best_node;
    }

    private int calculateDistance(int[] coordsX) {
        return (int) Math.sqrt(Math.pow(coordsX[0] - coordsX[2],2));
    }

    private Node split(Node node) {
        return null;
    }

    private int[] getXcoordsPoly(Polygon p){
        return new int[]{(int) p.getCoordinates()[0].getX(), (int) p.getCoordinates()[2].getX()};
    }

    private int[] getYcoordsPoly(Polygon p){
        return new int[]{(int) p.getCoordinates()[0].getY(), (int) p.getCoordinates()[1].getY()};
    }
}
