package dev.vmykh.diploma;

import java.util.HashMap;
import java.util.Map;

import static dev.vmykh.diploma.DubinsCurveType.*;
import static java.lang.Math.PI;
import static java.lang.Math.acos;

public final class DubinsCurves {

	public static Map<DubinsCurveType, DubinsCurveInfo> computeCurves(PositionWithDirection source,
	                                                                PositionWithDirection target,
	                                                                double curvatureRadius) {

		Map<DubinsCurveType, DubinsCurveInfo> curves = new HashMap<>();
		Point srcPos = source.getPosition();
		Vector srcDir = source.getDirection();
		Point tarPos = target.getPosition();
		Vector tarDir = target.getDirection();
		Point R1 = srcPos.add(
				srcDir.perpendicular().normalized().multipliedBy(-curvatureRadius)
		);
		Point L1 = srcPos.add(
				srcDir.perpendicular().normalized().multipliedBy(curvatureRadius)
		);
		Point R2 = tarPos.add(
				tarDir.perpendicular().normalized().multipliedBy(-curvatureRadius)
		);
		Point L2 = tarPos.add(
				tarDir.perpendicular().normalized().multipliedBy(curvatureRadius)
		);


		curves.put(RSR, createRSRCurve(srcPos, tarPos, R1, R2, curvatureRadius));
		curves.put(LSL, createLSLCurve(srcPos, tarPos, L1, L2, curvatureRadius));
		if (R1.distanceTo(L2) > 2 * curvatureRadius) {
			curves.put(RSL, createRSLCurve(srcPos, tarPos, R1, L2, curvatureRadius));
		}
		if (L1.distanceTo(R2) > 2 * curvatureRadius) {
			curves.put(LSR, createLSRCurve(srcPos, tarPos, L1, R2, curvatureRadius));
		}



		return curves;
	}

	private static DubinsCurveInfo createRSRCurve(Point srcPos, Point tarPos, Point R1, Point R2,
	                                              double curvatureRadius) {
		Vector R1toR2 = new Vector(R1, R2);
		Point firstTangentPoint = R1.add(
				R1toR2.perpendicular().normalized().multipliedBy(curvatureRadius)
		);
		Point secondTangentPoint = R2.add(
				R1toR2.perpendicular().normalized().multipliedBy(curvatureRadius)
		);
		double firstCurveLength = new Vector(R1, firstTangentPoint).angleTo(new Vector(R1, srcPos)) * curvatureRadius;
		double straightLineLength = new Vector(firstTangentPoint, secondTangentPoint).length();
		double secondCurveLength = new Vector(R2, tarPos).angleTo(new Vector(R2, secondTangentPoint)) * curvatureRadius;
		double pathLength = firstCurveLength + straightLineLength + secondCurveLength;

		return new DubinsCurveInfo(firstTangentPoint, secondTangentPoint, pathLength);
	}

	private static DubinsCurveInfo createLSLCurve(Point srcPos, Point tarPos, Point L1, Point L2,
	                                              double curvatureRadius) {
		Vector L1toL2 = new Vector(L1, L2);
		Point firstTangentPoint = L1.add(
				L1toL2.perpendicular().normalized().multipliedBy(curvatureRadius).negative()
		);
		Point secondTangentPoint = L2.add(
				L1toL2.perpendicular().normalized().multipliedBy(curvatureRadius).negative()
		);
		double firstCurveLength = new Vector(L1, srcPos).angleTo(new Vector(L1, firstTangentPoint)) * curvatureRadius;
		double straightLineLength = new Vector(firstTangentPoint, secondTangentPoint).length();
		double secondCurveLength = new Vector(L2, secondTangentPoint).angleTo(new Vector(L2, tarPos)) * curvatureRadius;
		double pathLength = firstCurveLength + straightLineLength + secondCurveLength;

		return new DubinsCurveInfo(firstTangentPoint, secondTangentPoint, pathLength);
	}

	private static DubinsCurveInfo createRSLCurve(Point srcPos, Point tarPos, Point R1, Point L2,
	                                              double curvatureRadius) {
		Vector R1toL2 = new Vector(R1, L2);
		double alpha = acos(curvatureRadius / (R1toL2.length() / 2.0));
		Point firstTangentPoint = R1.add(
				Vector.fromAngle(R1toL2.angle() + alpha).normalized().multipliedBy(curvatureRadius)
		);
		Point secondTangentPoint = L2.add(
				Vector.fromAngle(R1toL2.angle() + alpha + PI).normalized().multipliedBy(curvatureRadius)
		);

		double firstCurveLength = new Vector(R1, firstTangentPoint).angleTo(new Vector(R1, srcPos)) * curvatureRadius;
		double straightLineLength = new Vector(firstTangentPoint, secondTangentPoint).length();
		double secondCurveLength = new Vector(L2, secondTangentPoint).angleTo(new Vector(L2, tarPos)) * curvatureRadius;
		double pathLength = firstCurveLength + straightLineLength + secondCurveLength;

		return new DubinsCurveInfo(firstTangentPoint, secondTangentPoint, pathLength);
	}

	private static DubinsCurveInfo createLSRCurve(Point srcPos, Point tarPos, Point L1, Point R2,
	                                              double curvatureRadius) {
		Vector L1toR2 = new Vector(L1, R2);
		double alpha = acos(curvatureRadius / (L1toR2.length() / 2.0));
		Point firstTangentPoint = L1.add(
				Vector.fromAngle(L1toR2.angle() - alpha).normalized().multipliedBy(curvatureRadius)
		);
		Point secondTangentPoint = R2.add(
				Vector.fromAngle(L1toR2.angle() + (PI - alpha)).normalized().multipliedBy(curvatureRadius)
		);

		double firstCurveLength = new Vector(L1, srcPos).angleTo(new Vector(L1, firstTangentPoint)) * curvatureRadius;
		double straightLineLength = new Vector(firstTangentPoint, secondTangentPoint).length();
		double secondCurveLength = new Vector(R2, tarPos).angleTo(new Vector(R2, secondTangentPoint)) * curvatureRadius;
		double pathLength = firstCurveLength + straightLineLength + secondCurveLength;

		return new DubinsCurveInfo(firstTangentPoint, secondTangentPoint, pathLength);
	}
}
