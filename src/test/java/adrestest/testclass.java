package adrestest;

import org.junit.Test;

import com.paritus.data.Coordinate;

public class testclass {

	//van
	private static Coordinate start = new Coordinate(38.496063, 43.383177);
	private static Coordinate end = new Coordinate(38.495918, 43.384771);
	private static Coordinate point1 = new Coordinate(38.496397, 43.384020);
	private static Coordinate point2 = new Coordinate(38.495598, 43.383884);
	private static Coordinate point3 = new Coordinate(38.495841, 43.384352);
	
	//eskisehir
	private static Coordinate start2 = new Coordinate(39.761563, 30.522222);
	private static Coordinate end2 = new Coordinate(39.761685, 30.523666);
	private static Coordinate point4 = new Coordinate(39.761338, 30.522691);
	
	//guney amerika
	private static Coordinate start3 = new Coordinate(-25.279369, -57.574137);
	private static Coordinate end3 = new Coordinate(-25.281610, -57.574512);
	private static Coordinate point5 = new Coordinate(-25.281047, -57.572828);
	//guney amerika on end point
	private static Coordinate point6 = new Coordinate(-25.281610, -57.574512);
	
	@Test
	public void testClosestCoordinate() throws Exception {
		Coordinate closest = getClosestPointOnSegment(start, end, point3);
		printClosest(closest);
		
		closest = getClosestPointOnSegment(start, end, point2);
		printClosest(closest);
		
		closest = getClosestPointOnSegment(start, end, point1);
		printClosest(closest);
		
		closest = getClosestPointOnSegment(start2, end2, point4);
		printClosest(closest);
		
		closest = getClosestPointOnSegment(start3, end3, point5);
		printClosest(closest);
		
		closest = getClosestPointOnSegment(start3, end3, point6);
		printClosest(closest);
	}

	private void printClosest(Coordinate closest) {
		System.out.println(String.format("%s %s", closest.getX(), closest.getY()));
	}
	
	public static Coordinate getClosestPointOnSegment(Coordinate start, Coordinate end, Coordinate point) {
		return getClosestPointOnSegment(start.getX(), start.getY(), end.getX(), end.getY(), point.getX(), point.getY());
	}
	
	public static Coordinate getClosestPointOnSegment(double startX, double startY, double endX, double endY, double pointX, double pointY) {
		final Coordinate closestPoint;
		double xDelta = endX - startX;
		double yDelta = endY - startY;

		if ((xDelta == 0) && (yDelta == 0)) {
			throw new IllegalArgumentException("Segment start equals segment end");
		}

		double u = ((pointX - startX) * xDelta + (pointY - startY) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

		if ( u < 0) {//Double.compare(u, 0.000) < 0
			closestPoint = new Coordinate(startX, startY);
		} else if (u > 1) { // Double.compare(u, 0.000) > 0
			closestPoint = new Coordinate(endX, endY);
		} else {
			closestPoint = new Coordinate(startX + u * xDelta, startY + u * yDelta);
		}

		return closestPoint;
	}
}
