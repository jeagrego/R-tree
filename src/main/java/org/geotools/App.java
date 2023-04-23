package org.geotools;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.DoubleAdder;

public class App 
{
    public static void main( String[] args ) throws IOException {
        // display a data store file chooser dialog for shapefiles
        //String filename = "./src/main/resources/sh_statbel_statistical_sectors_31370_20220101.shp/sh_statbel_statistical_sectors_31370_20220101.shp";

        //String filename="./src/main/resources/regions-20180101-shp/regions-20180101.shp";

        String filename="./src/main/resources/50m_cultural/ne_50m_admin_0_countries.shp";

        //String filename="./src/main/resources/wb_countries_admin0_10m/WB_countries_Admin0_10m.shp";


        File file = new File(filename);
        if (!file.exists())
            throw new RuntimeException("Shapefile does not exist.");

        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource featureSource = store.getFeatureSource();

        SimpleFeatureCollection all_features=featureSource.getFeatures();

        store.dispose();

        ReferencedEnvelope global_bounds = featureSource.getBounds();


        Random r = new Random();


        GeometryBuilder gb = new GeometryBuilder();
        //Point p = gb.point(152183, 167679);// Plaine
        //Point p = gb.point(4.4, 50.8);//
        //Point p = gb.point(58.0, 47.0);
        //Point p = gb.point(10.6,59.9);// Oslo

        //Point p = gb.point(-70.9,-33.4);// Santiago
        //Point p = gb.point(169.2, -52.5);//NZ

        //Point p = gb.point(172.97365198326708, 1.8869725782923172);


        Point p = gb.point(r.nextInt((int) global_bounds.getMinX(), (int) global_bounds.getMaxX()),
                r.nextInt((int) global_bounds.getMinY(), (int) global_bounds.getMaxY()));

        System.out.println(all_features.size()+" features");

        MapContent map = new MapContent();
        map.setTitle("Projet INFO-F203");

        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(featureSource, style);
        map.addLayer(layer);

        ListFeatureCollection collectionTarget = new ListFeatureCollection(featureSource.getSchema());
        ListFeatureCollection collectionLeaves = new ListFeatureCollection(featureSource.getSchema());
        ListFeatureCollection collection1 = new ListFeatureCollection(featureSource.getSchema());
        ListFeatureCollection collection2 = new ListFeatureCollection(featureSource.getSchema());
        ListFeatureCollection collection3 = new ListFeatureCollection(featureSource.getSchema());
        ListFeatureCollection collection4 = new ListFeatureCollection(featureSource.getSchema());
        ListFeatureCollection collection5 = new ListFeatureCollection(featureSource.getSchema());
        ListFeatureCollection collection6 = new ListFeatureCollection(featureSource.getSchema());
        ListFeatureCollection collection7 = new ListFeatureCollection(featureSource.getSchema());
        ListFeatureCollection collection8 = new ListFeatureCollection(featureSource.getSchema());
        ListFeatureCollection collection9 = new ListFeatureCollection(featureSource.getSchema());
        ListFeatureCollection collection10 = new ListFeatureCollection(featureSource.getSchema());

        ArrayList<ListFeatureCollection> collections = new ArrayList<>();
        collections.add(collectionTarget); collections.add(collectionLeaves); collections.add(collection1);
        collections.add(collection2); collections.add(collection3); collections.add(collection4);
        collections.add(collection5); collections.add(collection6); collections.add(collection7);
        collections.add(collection8); collections.add(collection9); collections.add(collection10);
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureSource.getSchema());
        int maxLeaves = 5;
        Rtree t = new Rtree(maxLeaves);

        Node root = t.getRoot();
        int counterDEBUG = 0;
        try ( SimpleFeatureIterator iterator = all_features.features() ){
            while( iterator.hasNext()){

                SimpleFeature feature = iterator.next();
                MultiPolygon polygonComplex = (MultiPolygon) feature.getDefaultGeometry();//leaf

                t.addLeaf(root, feature.getProperty("NAME_EN").toString(), polygonComplex);
                counterDEBUG++;
               // if (counterDEBUG == 10){
                //    break;
                //}
            }
        }
        //DEBUG
        //showAllNodesAndLeaves(collections, featureBuilder, maxLeaves, t);
        MultiPolygon polyFound = (MultiPolygon) t.search(p);

        if (polyFound == null)
            System.out.println("Point not in any polygon!");
        else {
            // Add target polygon
            featureBuilder.add(polyFound);
            collectionTarget.add(featureBuilder.buildFeature(null));
        }

        // Add Point
        Polygon c= gb.circle(p.getX(), p.getY(), all_features.getBounds().getWidth()/200,10);
        featureBuilder.add(c);
        collectionTarget.add(featureBuilder.buildFeature(null));

        ArrayList<Color> colors = new ArrayList<>();
        colors.add(Color.RED);colors.add(Color.LIGHT_GRAY);colors.add(Color.BLUE);
        colors.add(Color.GREEN);colors.add(Color.GRAY);colors.add(Color.ORANGE);
        colors.add(Color.YELLOW);colors.add(Color.CYAN);colors.add(Color.LIGHT_GRAY);
        colors.add(Color.MAGENTA);colors.add(Color.PINK);colors.add(Color.DARK_GRAY);
        for (int i = 0; i<collections.size(); i++){
            style = SLD.createLineStyle(colors.get(i), 2.0f); layer = new FeatureLayer(collections.get(i), style);
            map.addLayer(layer);
        }

        // Now display the map
        JMapFrame.showMap(map);

    }

    private static void showAllNodesAndLeaves(ArrayList<ListFeatureCollection> collections, SimpleFeatureBuilder featureBuilder, int maxLeaves, Rtree t) {
        Node parent = t.getRoot();
        Queue<Node> nextparents = new LinkedList<>();
        Queue<Integer> colorIndexParent = new LinkedList<>();
        nextparents.add(parent);
        featureBuilder.add(parent.getPolygon());
        collections.get(3).add(featureBuilder.buildFeature(null));
        int counterIndex = 4; colorIndexParent.add(counterIndex);
        while (!nextparents.isEmpty()){
            ArrayList<Node> children = parent.getSubnodes();
            for (Node child : children) {
                featureBuilder.add(child.getPolygon());
                collections.get(counterIndex).add(featureBuilder.buildFeature(null));
                if (child.getSubnodes().size() != 0) {
                    nextparents.add(child);
                    colorIndexParent.add(counterIndex+1);
                }
            }
            parent = nextparents.remove();
            counterIndex = colorIndexParent.remove();
            if(counterIndex>=12){
                counterIndex = 2;
            }
        }
    }
}
