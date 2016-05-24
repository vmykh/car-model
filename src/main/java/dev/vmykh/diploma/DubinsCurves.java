package dev.vmykh.diploma;

import java.util.HashMap;
import java.util.Map;

import static dev.vmykh.diploma.DubinsCurveType.RSR;

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


		// RSR
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

		curves.put(RSR, new DubinsCurveInfo(firstTangentPoint, secondTangentPoint, pathLength));

		return curves;
	}
}
