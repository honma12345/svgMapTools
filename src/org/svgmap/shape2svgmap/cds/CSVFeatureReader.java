// This code is modified code of CSVFeatureReader.java introduced in geotools's tutorial

package org.svgmap.shape2svgmap.cds;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentState;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import com.csvreader.CsvReader;
//import com.opencsv.CsvReader;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class CSVFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {

    private ContentState state;
    private Query query;
    private CsvReader reader;
    private SimpleFeature next;
    private SimpleFeatureBuilder builder;
    private int row;
    private GeometryFactory geometryFactory;
	private boolean sjisInternalCharset;
	
	private int skipLines = 0;

    public CSVFeatureReader(ContentState contentState, Query query) throws IOException {
        this.state = contentState;
        this.query = query;
        CSVDataStore csv = (CSVDataStore) contentState.getEntry().getDataStore();
    	sjisInternalCharset = csv.sjisInternalCharset;
    	skipLines = csv.skipLines;
        reader = csv.read(); // this may throw an IOException if it could not connect
        boolean header = reader.readHeaders();
        if (! header ){
            throw new IOException("Unable to read csv header");
        }
    	
    	for ( int i = 0 ; i < skipLines ; i++ ){
    		boolean read = reader.readRecord(); // read and skip the "next" record
    	}
    	
        builder = new SimpleFeatureBuilder( state.getFeatureType() );
        geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        row = 0;
    }

    public SimpleFeatureType getFeatureType() {
        return (SimpleFeatureType) state.getFeatureType();
    }

    public SimpleFeature next() throws IOException, IllegalArgumentException,
            NoSuchElementException {
        SimpleFeature feature;
        if( next != null ){
            feature = next;
            next = null;
        }
        else {
            feature = readFeature();
        }
        return feature;
    }
    
    SimpleFeature readFeature() throws IOException {
    	SimpleFeature ans=null;
    	boolean failed = true;
    	while ( failed ){
	    	try{
	    		ans = readFeature_int();
	    		failed = false;
	    	} catch (Exception e ){
	    		failed = true;
	    	}
    	}
    	return ( ans );
    }
	
    SimpleFeature readFeature_int() throws IOException {
        if( reader == null ){
            throw new IOException("FeatureReader is closed; no additional features can be read");
        }
        boolean read = reader.readRecord(); // read the "next" record
        if( read == false ){
            close(); // automatic close to be nice
            return null; // no additional features are available
        }
        Coordinate coordinate = new Coordinate();
//    	String vals="";
        for( String column : reader.getHeaders() ){
            String value = reader.get(column);
//        	System.out.println("column:"+column + " val:"+value);
            if( "lat".equalsIgnoreCase(column) || "latitude".equalsIgnoreCase(column) || "lati".equalsIgnoreCase(column) || "�ܓx".equalsIgnoreCase(column) ){
                coordinate.y = Double.valueOf( value.trim() );
            } else if( "lon".equalsIgnoreCase(column) || "longitude".equalsIgnoreCase(column) || "lng".equalsIgnoreCase(column) || "long".equalsIgnoreCase(column) || "�o�x".equalsIgnoreCase(column) ){
                coordinate.x = Double.valueOf( value.trim() );
            } else {
//            	vals += ","+ value;
            	if (  sjisInternalCharset ){ // shapefile reader��charset���w��ł��邱�Ƃ����������̂ŁA���̃t���O�łǂ���ɂ��Ή��ł���悤�ɂ��� 2018/8/10
            		// Shapefile�̔߂��������ƃA���C�������邽�߂ɂ����ĕ�������������E�E�E
            		// deprecate�֐����폜 2017.11.2
            		String valSjis = sjisExt.getSjisStr(value);
            		String colSjis = sjisExt.getSjisStr(column);
            		builder.set(colSjis, valSjis );
            	} else {
            		builder.set(column, value );
            	}
            }
        }        
//        builder.set("Location", geometryFactory.createPoint( coordinate ) ); 
        builder.set("the_geom", geometryFactory.createPoint( coordinate ) ); 
//    	 --> the_geom ( for compatibility with shapefile source )
//    	System.out.println("readFeature:"+coordinate + " : "+ vals);
        row += 1;
        return builder.buildFeature( state.getEntry().getTypeName()+"."+row );
    }

    public boolean hasNext() throws IOException {
        if( next != null ){
            return true;
        }
        else {
            next = readFeature(); // read next feature so we can check
            return next != null;
        }
    }

    public void close() throws IOException {
    	if( reader == null ){
    	} else {
	        reader.close();
	        reader = null;
    	}
        builder = null;
        geometryFactory = null;
        next = null;
    }

}
