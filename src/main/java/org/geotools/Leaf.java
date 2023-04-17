package org.geotools;

import org.geotools.geometry.jts.GeometryBuilder;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import java.util.Arrays;

public class Leaf extends Node{
    private String name;
    private final MultiPolygon complexPolygon;

    public Leaf(int maxN, String id, MultiPolygon p) {
        super(maxN, null);
        this.complexPolygon = p;
        this.name = id;
        super.setPolygon(createMBDfromMultiPolygon(p));
    }

    private Polygon createMBDfromMultiPolygon(MultiPolygon mp){
        GeometryBuilder gb = new GeometryBuilder();
        return gb.box(this.getXcoordsMultiPoly(mp)[0],
                this.getYcoordsMultiPoly(mp)[0],
                this.getXcoordsMultiPoly(mp)[1],
                this.getYcoordsMultiPoly(mp)[1]);
    }

    private double[] getXcoordsMultiPoly(MultiPolygon p){
        return new double[]{p.getBoundary().getEnvelope().getCoordinates()[0].getX(),  p.getBoundary().getEnvelope().getCoordinates()[2].getX()};
    }

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
