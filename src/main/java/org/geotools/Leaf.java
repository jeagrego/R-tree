package org.geotools;

import org.geotools.geometry.jts.GeometryBuilder;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

public class Leaf extends Node{
    private final String name;
    private final MultiPolygon complexPolygon;

    public Leaf(int maxN, String id, MultiPolygon p) {
        super(maxN, null);
        this.complexPolygon = p;
        this.name = id;
        super.setPolygon(createMBRfromMultiPolygon(p));
    }

    private Polygon createMBRfromMultiPolygon(MultiPolygon mp){
        GeometryBuilder gb = new GeometryBuilder();
        return gb.box(this.getXcoordsMultiPoly(mp)[0],
                this.getYcoordsMultiPoly(mp)[0],
                this.getXcoordsMultiPoly(mp)[1],
                this.getYcoordsMultiPoly(mp)[1]);
    }

    /***
     * Used only to create the MBR of the leaf
     * Get the min_x and the max_x coordinates of the MultiPolygon
     * @return int[0] min_x, int[1] max_x;
     */
    private double[] getXcoordsMultiPoly(MultiPolygon p){
        return new double[]{p.getBoundary().getEnvelope().getCoordinates()[0].getX(),  p.getBoundary().getEnvelope().getCoordinates()[2].getX()};
    }

    /***
     * Used only to create the MBR of the leaf
     * Get the min_y and the max_y coordinates of the MultiPolygon
     * @return int[0] min_y, int[1] max_y;
     */
    private double[] getYcoordsMultiPoly(MultiPolygon p){
        return new double[]{p.getBoundary().getEnvelope().getCoordinates()[0].getY(), p.getBoundary().getEnvelope().getCoordinates()[1].getY()};
    }

    public MultiPolygon getComplexPolygon(){
        return this.complexPolygon;
    }

    public String getName(){
        return this.name;
    }
}
