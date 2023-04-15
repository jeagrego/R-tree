package org.geotools;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;

public class Leaf extends Node{
    private final MultiPolygon complexPolygon;

    public Leaf(int maxN, String id, Geometry p) {
        super(maxN, id, null);
        this.complexPolygon = (MultiPolygon) p;
    }
}
