package org.geotools;

import org.locationtech.jts.geom.MultiPolygon;

public class Leaf extends Node{
    private final MultiPolygon complexPolygon;

    public Leaf(int maxN, String id, MultiPolygon p) {
        super(maxN, id, null);
        this.complexPolygon = p;
    }
}
