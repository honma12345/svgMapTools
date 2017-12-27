package org.svgmap.shape2svgmap;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.awt.geom.*;

public class ClipPolygonDouble{

	PolygonDouble lastClipped;
	EdgeDouble currentEdge ; //current edge for clipping
	PolygonDouble clipped ;  //p clipped so far
	Point2D.Double lastp ; //previous point in algorithm
	Point2D.Double thisp ; //current point in algorithm
  	boolean thisCE; //Is current point Clipped Edge?
	
	
  ClipPolygonDouble(PolygonDouble p, Rectangle2D.Double r){
    lastClipped = p;
    lastClipped = clipSide(lastClipped,
       new EdgeDouble(r.x,r.y,r.x,r.y+r.height));
//System.out.println( "��" );
//lastClipped.print();
    lastClipped = clipSide(lastClipped,
       new EdgeDouble(r.x,r.y+r.height,r.x+r.width,r.y+r.height));
//System.out.println( "��" );
//lastClipped.print();
    lastClipped = clipSide(lastClipped,
       new EdgeDouble(r.x+r.width,r.y+r.height,r.x+r.width,r.y));
//System.out.println( "�E" );
//lastClipped.print();
    lastClipped = clipSide(lastClipped,
       new EdgeDouble(r.x+r.width,r.y,r.x,r.y));
//System.out.println( "��" );
//lastClipped.print();
//    return lastClipped;
  }
	
	public PolygonDouble getClippedPolygon(){
		return lastClipped;
	}

  /* clip p against Edge e and return result */
  PolygonDouble clipSide(PolygonDouble p, EdgeDouble e){
    Point2D.Double intersect;
    if (p.npoints == 0){
      return p; //nothing to do
    }
    currentEdge = e;
    clipped = new PolygonDouble((int)(p.npoints * 1.3));
  	clipped.clipped=p.clipped;
    lastp = new Point2D.Double(p.xpoints[p.npoints-1],p.ypoints[p.npoints-1]);
    for (int i = 0; i < p.npoints; i++){
		thisp = new Point2D.Double(p.xpoints[i],p.ypoints[i]);
    	thisCE = p.clippedEdge[i];
		if (e.inside(thisp) && e.inside(lastp)){ // �O�̓_�����̓_�������Ă����獡�̓_��ǉ�
			clipped.addPoint(thisp.x,thisp.y,thisCE);
		} else if (!e.inside(thisp) && e.inside(lastp)){ // �O�̓_���������Ă������_��ǉ�
			intersect = e.intersect(thisp,lastp);
			clipped.addPoint(intersect.x,intersect.y, thisCE); // ���̓_��ClipEdge�t���O�p��
		} else if (!e.inside(thisp) && !e.inside(lastp)){ // ���ɓ����Ė����ꍇ�����ǉ����Ȃ�
			/*nothing */
		} else if (e.inside(thisp) && !e.inside(lastp)){ // ���̓_���������Ă�����E�E
			intersect = e.intersect(lastp,thisp); // ��_��ǉ����āA���̓_���X�ɒǉ�
			clipped.addPoint(intersect.x,intersect.y, true); // ����������̓N���b�v���ꂽ�G�b�W�̏I�_
			clipped.addPoint(thisp.x,thisp.y,thisCE);
			clipped.clipped=true;
      	}
    	lastp = thisp;
    }
  	
  	
    currentEdge = null;  //so that paint won't draw currentEdge now we've 
                        //left the loop
    return clipped;
  }
}