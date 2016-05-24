package dev.vmykh.diploma;

import java.util.*;

import static dev.vmykh.diploma.DubinsCurveType.RSR;
import static java.lang.Math.PI;
import static java.lang.Math.abs;

public final class PathResolver {
	public static final double ONE_STEP_DISTANCE = 1.0;
	public static final double FRONT_AXIS_ROTATION_ANGLE = PI / 8.0;
	public static final double ACCEPTABLE_FINISH_POSITION_ERROR = 10.0;
	private static final int WEIGHTED_DIRECTIONS_PER_CELL = 8;

	public static final double ACCEPTABLE_HEURISTIC_DIFFERENCE = 50.0;

	private final Point targetPosition;
	private final Vector targetOrientation;
	private final CollisionDetector collisionDetector;

	private static final int WEIGHT_CELL_SIZE = 3;
	private final Map<IntegerPoint, CellWeight> weights = new HashMap<>();

	private final PathResolverListener listener;

	public PathResolver(PositionWithDirection target, CollisionDetector collisionDetector,
	                    PathResolverListener listener) {
		this.targetPosition = target.getPosition();
		this.targetOrientation = target.getDirection();
		this.collisionDetector = collisionDetector;
		this.listener = listener;
	}

	public List<Movement> resolvePath(Car car) {
//		PriorityQueue<CarState> states = new PriorityQueue<>();
//		CarState currentState = new CarState(car, null, computeHeuristic(car), null);
//		List<Point> discardedStates = new ArrayList<>();
//		int iterations = 0;
//		int skippedIterations = 0;
//		while (computeDistanceToTarget(currentState.getCar()) > ACCEPTABLE_FINISH_POSITION_ERROR) {
////			if (iterations > 10_000) {
////				System.out.println("bangura");
////			}
//
//
//			if (currentState.previousCarState != null) {
//				double currentStateHeuristic = currentState.getHeuristic();
//				double actualHeuristic = computeHeuristic(currentState.getCar());
//				if (abs(currentStateHeuristic - actualHeuristic) > ACCEPTABLE_HEURISTIC_DIFFERENCE) {
//					states.remove(currentState);
//					states.add(
//							new CarState(currentState.getCar(), currentState.causedMovement, actualHeuristic,
//									currentState.getPreviousCarState())
//					);
//
//					iterations++;
//					skippedIterations++;
//					currentState = states.peek();
//					continue;
//				}
//			}
//
//			Point currentStateCenter = currentState.getCar().getCenter();
//
//			IntegerPoint intPoint = roundPoint(currentStateCenter);
//			if (!weights.containsKey(intPoint)) {
//				weights.put(intPoint, new CellWeight(WEIGHTED_DIRECTIONS_PER_CELL));
//			}
//			weights.get(intPoint).addWeight(car.getOrientationAngle());
//
//			discardedStates.add(currentStateCenter);
//			states.remove(currentState);
//
//			try {
//				states.add(moveForward(currentState));
//			} catch (ImpossibleMovementException e) {
//				// do nothing
//			}
//
//			try {
//				states.add(moveForwardLeft(currentState));
//			} catch (ImpossibleMovementException e) {
//				// do nothing
//			}
//
//			try {
//				states.add(moveForwardRight(currentState));
//			} catch (ImpossibleMovementException e) {
//				// do nothing
//			}
//
//			try {
//				states.add(moveBackward(currentState));
//			} catch (ImpossibleMovementException e) {
//				// do nothing
//			}
//
//			try {
//				states.add(moveBackwardLeft(currentState));
//			} catch (ImpossibleMovementException e) {
//				// do nothing
//			}
//
//			try {
//				states.add(moveBackwardRight(currentState));
//			} catch (ImpossibleMovementException e) {
//				// do nothing
//			}
//
//			if (states.size() == 0) {
//				throw new RuntimeException("Cannot resolve path");
//			}
//
//			currentState = states.peek();
//
//			iterations++;
//			if (iterations % 1000 == 0) {
//				System.out.println("iter " + iterations + "   distance: " + currentState.getHeuristic());
//				listener.intermediatePoints(new ArrayList<>(discardedStates));
//				discardedStates.clear();
//			}
//
//			if (iterations > 10_000_000) {
//				throw new RuntimeException("Too many iterations");
//			}
//
//		}
//
//		// go reverse
//		List<Movement> controls = new LinkedList<>();
//		controls.add(0, currentState.getCausedMovement());
//		while (currentState.getPreviousCarState() != null) {
//			currentState = currentState.getPreviousCarState();
//			controls.add(0, currentState.getCausedMovement());
//		}


		// Dubins curves
		double curvatureRadius = car.withFrontAxisAngle(FRONT_AXIS_ROTATION_ANGLE).getRotationCircleRadius();
		DubinsCurveInfo rsrInfo = DubinsCurves.computeCurves(
				new PositionWithDirection(car.getBackAxisCenter(), car.getOrientationVector()),
				new PositionWithDirection(targetPosition.subtract(targetOrientation
						.normalized().multipliedBy(car.getLength() / 2.0)), targetOrientation),
				curvatureRadius
				).get(RSR);



		List<Movement> controls = new ArrayList<>();
		Car currentCar = car.withFrontAxisAngle(-FRONT_AXIS_ROTATION_ANGLE);
		double acceptableError = 1.5;
		int iter = 0;
//		while(currentCar.getCenter().distanceTo(rsrInfo.getFirstCircleTangentPoint()) > acceptableError) {
		Vector straightLineVector = new Vector(rsrInfo.getFirstCircleTangentPoint(), rsrInfo.getSecondCircleTangentPoint());
		double straightDirectionAngle = straightLineVector.angle();
		while(abs(currentCar.getOrientationAngle() - straightDirectionAngle) > 0.05) {
			currentCar = currentCar.movedBy(ONE_STEP_DISTANCE);
			controls.add(Movement.FORWARD_RIGHT);
			iter++;
			if (iter > 2000) {
				return controls;
			}
		}

//		while(currentCar.getCenter().distanceTo(rsrInfo.getFirstCircleTangentPoint()) < acceptableError) {
//			currentCar = currentCar.movedBy(ONE_STEP_DISTANCE);
//			controls.add(Movement.FORWARD_RIGHT);
//			iter++;
//			if (iter > 2000) {
//				return controls;
//			}
//		}

		currentCar = currentCar.withFrontAxisAngle(0.0);
		PIDController pidController = new PIDController(1.0, 5.0, 0.0, 1.5);
		double prevErrorToFinalPoint = Long.MAX_VALUE;
		while(true) {
			double currentErrorToFinalPoint = currentCar.getBackAxisCenter().distanceTo(rsrInfo.getSecondCircleTangentPoint());
			if (currentErrorToFinalPoint > prevErrorToFinalPoint) {
				break;
			}
			prevErrorToFinalPoint = currentErrorToFinalPoint;
			double error = distanceFromPointToLine(currentCar.getBackAxisCenter(),
					rsrInfo.getFirstCircleTangentPoint(), rsrInfo.getSecondCircleTangentPoint());
			int steering = pidController.currentError(error);
			Movement movement = Movement.FORWARD;
			currentCar = currentCar.withFrontAxisAngle(0.0);
			if (steering == 1) {
				movement = Movement.FORWARD_LEFT;
				currentCar = currentCar.withFrontAxisAngle(FRONT_AXIS_ROTATION_ANGLE);
			} else if (steering == -1) {
				movement = Movement.FORWARD_RIGHT;
				currentCar = currentCar.withFrontAxisAngle(-FRONT_AXIS_ROTATION_ANGLE);
			}
			currentCar = currentCar.movedBy(ONE_STEP_DISTANCE);
			controls.add(movement);
			iter++;
			if (iter > 2000) {
				return controls;
			}
		}
		currentCar = currentCar.withFrontAxisAngle(-FRONT_AXIS_ROTATION_ANGLE);
		while(currentCar.getCenter().distanceTo(targetPosition) > acceptableError) {
			currentCar = currentCar.movedBy(ONE_STEP_DISTANCE);
			controls.add(Movement.FORWARD_RIGHT);
			iter++;
			if (iter > 2000) {
				return controls;
			}
		}

		return controls;
	}

	private static double distanceFromPointToLine(Point point, Point linePoint1, Point linePoint2) {
		return (
				(linePoint2.getY() - linePoint1.getY()) * point.getX() -
				(linePoint2.getX() - linePoint1.getX()) * point.getY() +
				linePoint2.getX() * linePoint1.getY() - linePoint2.getY() * linePoint1.getX()
		) / linePoint1.distanceTo(linePoint2);
	}

	private IntegerPoint roundPoint(Point point) {
		int roundedX = (int) point.getX();
		int roundedY = (int) point.getY();
		int shiftedX = roundedX - (roundedX % WEIGHT_CELL_SIZE);
		int shiftedY = roundedY - (roundedY % WEIGHT_CELL_SIZE);
		return new IntegerPoint(shiftedX, shiftedY);
	}

	private CarState moveForward(CarState carState) throws ImpossibleMovementException {
		Car movedCar = carState.car.withFrontAxisAngle(0.0).movedBy(ONE_STEP_DISTANCE);
		if (collisionDetector.collides(movedCar)) {
			throw new ImpossibleMovementException();
		}
		return new CarState(
				movedCar,
				Movement.FORWARD,
				computeHeuristic(movedCar),
				carState
				);
	}

	private CarState moveForwardLeft(CarState carState) throws ImpossibleMovementException {
		Car movedCar = carState.car.withFrontAxisAngle(FRONT_AXIS_ROTATION_ANGLE).movedBy(ONE_STEP_DISTANCE);
		if (collisionDetector.collides(movedCar)) {
			throw new ImpossibleMovementException();
		}
		return new CarState(
				movedCar,
				Movement.FORWARD_LEFT,
				computeHeuristic(movedCar),
				carState
		);
	}

	private CarState moveForwardRight(CarState carState) throws ImpossibleMovementException {
		Car movedCar = carState.car.withFrontAxisAngle(-FRONT_AXIS_ROTATION_ANGLE).movedBy(ONE_STEP_DISTANCE);
		if (collisionDetector.collides(movedCar)) {
			throw new ImpossibleMovementException();
		}
		return new CarState(
				movedCar,
				Movement.FORWARD_RIGHT,
				computeHeuristic(movedCar),
				carState
		);
	}

	private CarState moveBackward(CarState carState) throws ImpossibleMovementException {
		Car movedCar = carState.car.withFrontAxisAngle(0.0).movedBy(-ONE_STEP_DISTANCE);
		if (collisionDetector.collides(movedCar)) {
			throw new ImpossibleMovementException();
		}
		return new CarState(
				movedCar,
				Movement.BACKWARD,
				computeHeuristic(movedCar),
				carState
		);
	}

	private CarState moveBackwardLeft(CarState carState) throws ImpossibleMovementException {
		Car movedCar = carState.car.withFrontAxisAngle(FRONT_AXIS_ROTATION_ANGLE).movedBy(-ONE_STEP_DISTANCE);
		if (collisionDetector.collides(movedCar)) {
			throw new ImpossibleMovementException();
		}
		return new CarState(
				movedCar,
				Movement.BACKWARD_LEFT,
				computeHeuristic(movedCar),
				carState
		);
	}

	private CarState moveBackwardRight(CarState carState) throws ImpossibleMovementException {
		Car movedCar = carState.car.withFrontAxisAngle(-FRONT_AXIS_ROTATION_ANGLE).movedBy(-ONE_STEP_DISTANCE);
		if (collisionDetector.collides(movedCar)) {
			throw new ImpossibleMovementException();
		}
		return new CarState(
				movedCar,
				Movement.BACKWARD_RIGHT,
				computeHeuristic(movedCar),
				carState
		);
	}

	private double computeHeuristic(Car car) {
		double positionWeight = 0.0;
		CellWeight cellWeight = weights.get(roundPoint(car.getCenter()));
		if (cellWeight != null) {
			positionWeight = cellWeight.getWeight(car.getOrientationAngle());
		}

		Vector fromCarToTarget = new Vector(car.getCenter(), targetPosition);
		double orientationWeight = abs(car.getOrientationAngle() - fromCarToTarget.angle()) * 20;

//		return computeDistanceToTarget(car) + positionWeight + orientationWeight;
		return computeDistanceToTarget(car) + positionWeight;
	}

	private double computeDistanceToTarget(Car car) {
		return car.getCenter().distanceTo(targetPosition);
	}

	private static final class CarState implements Comparable<CarState> {
		private final Car car;
		private final Movement causedMovement;
		private final double heuristic;
		private final CarState previousCarState;



		public CarState(Car car, Movement causedMovement, double heuristic, CarState previousCarState) {
			this.car = car;
			this.causedMovement = causedMovement;
			this.heuristic = heuristic;
			this.previousCarState = previousCarState;
		}

		public Car getCar() {
			return car;
		}

		public Movement getCausedMovement() {
			return causedMovement;
		}

		public double getHeuristic() {
			return heuristic;
		}

		public CarState getPreviousCarState() {
			return previousCarState;
		}

		@Override
		public int compareTo(CarState another) {
			return Double.compare(this.heuristic, another.heuristic);
		}
	}

}
