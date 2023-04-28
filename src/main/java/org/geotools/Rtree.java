package org.geotools;

import org.geotools.geometry.jts.GeometryBuilder;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;

public class Rtree {
    private final Node n;
    int N=3; // max amount of leaves or nodes

    public Rtree(int N) {
        this.n = new Node(N, null);
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
            return splitLinear(node); //return splitQuadratic(node);
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
            double areaPrevious = pretendToExpandMBR(child.getPolygon(), child.getPolygon());
            double areaAfter = pretendToExpandMBR(child.getPolygon(), p);
            if(best_area== -1 || best_area>areaAfter-areaPrevious){
                best_area = areaAfter-areaPrevious;
                best_node = child;
            }
        }
        return best_node;
    }

    /***
     * PretendToExpandMBR returns the area of a temporary envelope (MBR) encompassing polygon p1 and polygon p2
     * If p1 == p2 returns the area of polygon p1
     * @param p1 the original polygon
     * @param p2 the polygon added to the temporary envelope of p1
     * @return area of the envelope of p1 and p2 (L*W)
     */
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

    private double getMBRArea(Polygon p1){
        return pretendToExpandMBR(p1, p1);
    }

    private double calculateDistance(double[] coords) {
        return Math.sqrt(Math.pow(coords[0] - coords[1],2));
    }

    private Node splitQuadratic(Node node) {
        int[] tuple_ij = QuadraticPickSeeds(node);
        int i_chosen = tuple_ij[0]; int j_chosen = tuple_ij[1];
        Node n1 = new Node(3, node.getSubnodes().get(i_chosen).getPolygon());
        n1.addNode(node.getSubnodes().get(i_chosen));
        Node n2 = new Node(3, node.getSubnodes().get(j_chosen).getPolygon());
        n2.addNode(node.getSubnodes().get(j_chosen));
        for(int i = 0; i<node.getSubnodes().size(); i++){ // QuadraticPickNext
            if(i!= i_chosen && i != j_chosen){
                Node cur_node = node.getSubnodes().get(i);
                double area1 = pretendToExpandMBR(n1.getPolygon(), cur_node.getPolygon()) - getMBRArea(n1.getPolygon());
                double area2 = pretendToExpandMBR(n2.getPolygon(), cur_node.getPolygon()) - getMBRArea(n1.getPolygon());
                if(area1 <= area2){
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

    private int[] QuadraticPickSeeds(Node node) {
        int[] tuple_ij = new int[]{0,0};
        double best_area = -1;
        for(int i = 0; i< node.getSubnodes().size(); i++){
            for(int j = i; j< node.getSubnodes().size(); j++){// j = i  to avoid (0,1) and (1,0) which is == here
                if(i != j){
                    Node child1 = node.getSubnodes().get(i);
                    Node child2 = node.getSubnodes().get(j);
                    double area = pretendToExpandMBR(child1.getPolygon(), child2.getPolygon()) - getMBRArea(child1.getPolygon()) - getMBRArea((child2.getPolygon()));
                    if(best_area== -1 || best_area<area){
                        best_area = area;
                        tuple_ij[0] = i; tuple_ij[1] = j;
                    }
                }
            }
        }
        return tuple_ij;
    }

    private Node splitLinear(Node node) {
        int[] tuple_ij = LinearPickSeeds(node);
        int i_chosen = tuple_ij[0]; int j_chosen = tuple_ij[1] ;
        Node n1 = new Node(3, node.getSubnodes().get(i_chosen).getPolygon());
        n1.addNode(node.getSubnodes().get(i_chosen));
        Node n2 = new Node(3, node.getSubnodes().get(j_chosen).getPolygon());
        n2.addNode(node.getSubnodes().get(j_chosen));
        for(int i = 0; i<node.getSubnodes().size(); i++){ // LinearPickNext, trivial
            if(i!= i_chosen && i != j_chosen){
                Node cur_node = node.getSubnodes().get(i);
                double area1 = getMBRArea(n1.getPolygon());
                double area2 = getMBRArea(n2.getPolygon());
                if(area1 < area2){
                    n1.setPolygon(expandPolygon(n1.getPolygon(), cur_node.getPolygon()));
                    n1.addNode(cur_node);
                }else{
                    n2.setPolygon(expandPolygon(n2.getPolygon(), cur_node.getPolygon()));
                    n2.addNode(cur_node);
                }
                /*//more trivial but slower
                if(n1.getSubnodes().size() < n2.getSubnodes().size()){ // n1 has fewer children than n2 then add to n1
                    n1.setPolygon(expandPolygon(n1.getPolygon(), cur_node.getPolygon()));
                    n1.addNode(cur_node);
                }else{
                    n2.setPolygon(expandPolygon(n2.getPolygon(), cur_node.getPolygon()));
                    n2.addNode(cur_node);
                }*/
            }
        }
        node.removeSubnodes();
        node.setPolygon(n1.getPolygon());
        node.setPolygon(expandPolygon(node.getPolygon(), n2.getPolygon()));
        node.addNode(n1);node.addNode(n2);
        return node;
    }

    private int[] LinearPickSeeds(Node node) {
        int num_dimensions = 2;
        int[] tuple_ij = new int[]{0, 0};
        double best_norm = -1;
        for(int i = 0; i< node.getSubnodes().size(); i++){
            for(int j = i; j< node.getSubnodes().size(); j++){// j = i  because both (0,1) and (1,0) are tested
                if(i != j){
                    Node child1 = node.getSubnodes().get(i);
                    Node child2 = node.getSubnodes().get(j);
                    for(int i_dimension=0; i_dimension<num_dimensions; i_dimension++) {//dimension x and y
                        boolean flipped = false;
                        double L = getLalongDimensionD(child1, child2, i_dimension);// L value of pair (0,1) on axis x or y
                        double L_flipped = getLalongDimensionD(child2, child1, i_dimension);// L value of pair (1,0) on axis x or y
                        if (L > L_flipped) {//pair is to be flipped
                            L = L_flipped;
                            flipped = true;
                        }
                        double W = getWalongDimensionD(node, i_dimension);
                        double norm = L / W;
                        if (best_norm == -1 || best_norm < norm) {
                            best_norm = norm;
                            tuple_ij[0] = i;
                            tuple_ij[1] = j;
                            if (flipped) {
                                tuple_ij[0] = j;
                                tuple_ij[1] = i;
                            }
                        }
                    }
                }
            }
        }
        return tuple_ij;
    }

    private double getLalongDimensionD(Node c1, Node c2, int index_dimension){
        if(index_dimension == 0) {
            double minX = c1.getXcoords()[1]; //minX = lowest highside
            double maxX = c2.getXcoords()[0]; //maxX = highest lowside
            return calculateDistance(new double[]{minX, maxX});
        }
        if(index_dimension == 1) {
            double minX = c1.getYcoords()[1]; //minY = lowest highside
            double maxX = c2.getYcoords()[0]; //maxY = highest lowside
            return calculateDistance(new double[]{minX, maxX});
        }
        return 0;
    }

    private double getWalongDimensionD(Node n, int index_dimension){
        if(index_dimension == 0 ){
            return calculateDistance(new double[]{n.getXcoords()[1], n.getXcoords()[0]});
        }
        if(index_dimension == 1){
            return calculateDistance(new double[]{n.getXcoords()[1], n.getXcoords()[0]});
        }
        return 0;
    }

    /***
     * Get the min_x and the max_x coordinates of the Polygon p
     * @param p Polygon to test
     * @return int[0] min_x, int[1] max_x;
     */
    private double[] getXcoordsPoly(Polygon p){
        return new double[]{p.getCoordinates()[0].getX(), p.getCoordinates()[2].getX()};
    }

    /***
     * Get the min_y and the max_y coordinates of the Polygon p
     * @param p Polygon to test
     * @return int[0] min_y, int[1] max_y;
     */
    private double[] getYcoordsPoly(Polygon p){
        return new double[]{p.getCoordinates()[0].getY(), p.getCoordinates()[1].getY()};
    }

    public Node getRoot() {
        return n;
    }

    public Leaf search(Point p) {
        if(this.getRoot().getPolygon().contains(p)){
            return searchRecursive(this.n, p, new ArrayList<>());
        }else{return null;}
    }

    //The "visited" list is used to not get infinite loops, it slightly deviates from the original search function pseudocode
    public Leaf searchRecursive(Node n, Point p, ArrayList<Node> visited) {
        if (n.getSubnodes().size() == 0){//leaf
            Leaf l = (Leaf) n;
            if(l.getComplexPolygon().contains(p)){
                return l;
            }else{
                return null;}
        }else{//MBR
            visited.add(n);
            if(n.getPolygon().contains(p)){
                for(Node c: n.getSubnodes()){
                    if(c.getPolygon().contains(p) && !visited.contains(c)){
                        Leaf leafFound = searchRecursive(c, p, visited);
                        if(leafFound != null){
                            return leafFound;
                        }
                    }
                }
            }
        }
        return null;
    }
}
