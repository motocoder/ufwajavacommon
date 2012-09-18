package llc.ufwa.util;

import llc.ufwa.geo.RawPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PointUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(PointUtils.class);
	
	private PointUtils() {}
	
	public static enum InsideOutside {
	    INSIDE_COMPLETELY,
	    INSIDE_PARTIALLY, //central point is inside other
	    OUTSIDE_PARTIALLY,
	    OUTSIDE_COMPLETELY //central point is outside others accuracy, but they interlap accuracy
	};
	
	public static InsideOutside getRelation(RawPoint inside, RawPoint outside) {
		
		final double readDelta = computeDistance(inside, outside);
		
		final InsideOutside returnVal;
		
		if(readDelta > (outside.getAccuracy() + inside.getAccuracy())) {
			returnVal = InsideOutside.OUTSIDE_COMPLETELY;
		}
		else if(readDelta > outside.getAccuracy()) {
			returnVal = InsideOutside.OUTSIDE_PARTIALLY;
		}
		else if((readDelta + inside.getAccuracy()) < outside.getAccuracy()) {
			returnVal = InsideOutside.INSIDE_COMPLETELY;
		}
		else {
			returnVal = InsideOutside.INSIDE_COMPLETELY;
		}
		
		return returnVal;
		
	}
	
	public static double computeDistance(RawPoint point1, RawPoint point2) {
		
		final double zDelta = Math.abs(point1.getAltitude() - point2.getAltitude());
	
		double lat1 = point1.getLatitude() / 1000000;
		double lon1 = point1.getLongitude() / 1000000;
	
		double lat2 = point2.getLatitude() / 1000000;
		double lon2 = point2.getLongitude() / 1000000;

        // Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
        // using the "Inverse Formula" (section 4)

        int MAXITERS = 20;
        // Convert lat/long to radians
        lat1 *= Math.PI / 180.0;
        lat2 *= Math.PI / 180.0;
        lon1 *= Math.PI / 180.0;
        lon2 *= Math.PI / 180.0;

        double a = 6378137.0; // WGS84 major axis
        double b = 6356752.3142; // WGS84 semi-major axis
        double f = (a - b) / a;
        double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);

        double L = lon2 - lon1;
        double A = 0.0;
        double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
        double U2 = Math.atan((1.0 - f) * Math.tan(lat2));

        double cosU1 = Math.cos(U1);
        double cosU2 = Math.cos(U2);
        double sinU1 = Math.sin(U1);
        double sinU2 = Math.sin(U2);
        double cosU1cosU2 = cosU1 * cosU2;
        double sinU1sinU2 = sinU1 * sinU2;

        double sigma = 0.0;
        double deltaSigma = 0.0;
        double cosSqAlpha = 0.0;
        double cos2SM = 0.0;
        double cosSigma = 0.0;
        double sinSigma = 0.0;
        double cosLambda = 0.0;
        double sinLambda = 0.0;

        double lambda = L; // initial guess
        for (int iter = 0; iter < MAXITERS; iter++) {
            double lambdaOrig = lambda;
            cosLambda = Math.cos(lambda);
            sinLambda = Math.sin(lambda);
            double t1 = cosU2 * sinLambda;
            double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
            double sinSqSigma = t1 * t1 + t2 * t2; // (14)
            sinSigma = Math.sqrt(sinSqSigma);
            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
            sigma = Math.atan2(sinSigma, cosSigma); // (16)
            double sinAlpha = (sinSigma == 0) ? 0.0 :
                cosU1cosU2 * sinLambda / sinSigma; // (17)
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
            cos2SM = (cosSqAlpha == 0) ? 0.0 :
                cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha; // (18)

            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
            A = 1 + (uSquared / 16384.0) * // (3)
                (4096.0 + uSquared *
                 (-768 + uSquared * (320.0 - 175.0 * uSquared)));
            double B = (uSquared / 1024.0) * // (4)
                (256.0 + uSquared *
                 (-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
            double C = (f / 16.0) *
                cosSqAlpha *
                (4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
            double cos2SMSq = cos2SM * cos2SM;
            deltaSigma = B * sinSigma * // (6)
                (cos2SM + (B / 4.0) *
                 (cosSigma * (-1.0 + 2.0 * cos2SMSq) -
                  (B / 6.0) * cos2SM *
                  (-3.0 + 4.0 * sinSigma * sinSigma) *
                  (-3.0 + 4.0 * cos2SMSq)));

            lambda = L +
                (1.0 - C) * f * sinAlpha *
                (sigma + C * sinSigma *
                 (cos2SM + C * cosSigma *
                  (-1.0 + 2.0 * cos2SM * cos2SM))); // (11)

            double delta = (lambda - lambdaOrig) / lambda;
            if (Math.abs(delta) < 1.0e-12) {
                break;
            }
        }

        final double latLongDelta = (b * A * (sigma - deltaSigma));

        return Math.sqrt((latLongDelta * latLongDelta) + (zDelta * zDelta));
//	        results[0] = distance;
//	        if (results.length > 1) {
//	            float initialBearing = (float) Math.atan2(cosU2 * sinLambda,
//	                cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
//	            initialBearing *= 180.0 / Math.PI;
//	            results[1] = initialBearing;
//	            if (results.length > 2) {
//	                float finalBearing = (float) Math.atan2(cosU1 * sinLambda,
//	                    -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
//	                finalBearing *= 180.0 / Math.PI;
//	                results[2] = finalBearing;
//	            }
//	        }
	    }

}
