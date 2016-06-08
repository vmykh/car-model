package dev.vmykh.diploma;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static dev.vmykh.diploma.DubinsCurveType.*;
import static java.lang.Math.*;
import static java.util.Arrays.asList;

public final class PathResolver {
	public static final double ONE_STEP_DISTANCE = 50.0;
	public static final double FRONT_AXIS_ROTATION_ANGLE = PI / 8.0;
	public static final double ACCEPTABLE_FINISH_POSITION_ERROR = 30.0;
	private static final int WEIGHTED_DIRECTIONS_PER_CELL = 32;

	public static final double ACCEPTABLE_HEURISTIC_DIFFERENCE = 50.0;

	private final Point targetPosition;
	private final Vector targetOrientation;
	private final CollisionDetectorDiscreteField collisionDetectorDiscreteField;

	private static final int WEIGHT_CELL_SIZE = 50;
	private final Map<IntegerPoint, CellWeight> weights = new HashMap<>();

	private final PathResolverListener listener;

	private final Field field;
	private final double obstacleSize;

	public PathResolver(PositionWithDirection target, CollisionDetectorDiscreteField collisionDetectorDiscreteField,
	                    Set<IntegerPoint> obstacles, double worldWidth, double worldHeight,
	                    double obstacleSize, PathResolverListener listener) {
		this.targetPosition = target.getPosition();
		this.targetOrientation = target.getDirection();
		this.collisionDetectorDiscreteField = collisionDetectorDiscreteField;
		this.listener = listener;

		this.obstacleSize = obstacleSize;
		int fieldWidth = (int)(worldWidth / obstacleSize);
		int fieldHeight = (int)(worldHeight / obstacleSize);
		this.field = new Field(fieldWidth, fieldHeight, obstacles);
	}

	public List<Movement> resolvePath(Car car) {
		// experimental theta *

//		List<Point> thetaStarPath = computeThetaStarPath(car);
//		listener.thetaStarPoints(thetaStarPath);

//		try {
//			Thread.sleep(Long.MAX_VALUE);
//		} catch (InterruptedException e) {
//			throw new RuntimeException(e);
//		}

		PriorityQueue<CarState> states = new PriorityQueue<>();
		CarState currentState = new CarState(car, null, null, computeHeuristic(car, null, null));
		List<Point> discardedStates = new ArrayList<>();
		int iterations = 0;
		int skippedIterations = 0;
		while (computeDistanceToTarget(currentState.getCar()) > ACCEPTABLE_FINISH_POSITION_ERROR
				||(abs(orientationError(currentState.getCar())) > PI / 16)) {
//			if (iterations > 10_000) {
//				System.out.println("bangura");
//			}


			if (currentState.previousCarState != null) {
				double currentStateHeuristic = currentState.getHeuristicDetails().getHeuristic();
				HeuristicDetails actualHeuristicDetails = recomputeHeuristic(currentState);
				double actualHeuristic = actualHeuristicDetails.getHeuristic();
				if (abs(currentStateHeuristic - actualHeuristic) > ACCEPTABLE_HEURISTIC_DIFFERENCE) {
					states.remove(currentState);
					states.add(
							new CarState(currentState.getCar(), currentState.causedMovement,
									currentState.getPreviousCarState(), actualHeuristicDetails)
					);

					iterations++;
					skippedIterations++;
					currentState = states.peek();
					continue;
				}
			}

			Point currentStateCenter = currentState.getCar().getCenter();

			IntegerPoint intPoint = roundPoint(currentStateCenter);
			if (!weights.containsKey(intPoint)) {
				weights.put(intPoint, new CellWeight(WEIGHTED_DIRECTIONS_PER_CELL));
			}
			weights.get(intPoint).addWeight(car.getOrientationAngle());

			discardedStates.add(currentStateCenter);
			states.remove(currentState);

			try {
				states.add(moveForward(currentState));
			} catch (ImpossibleMovementException e) {
				// do nothing
			}

			try {
				states.add(moveForwardLeft(currentState));
			} catch (ImpossibleMovementException e) {
				// do nothing
			}

			try {
				states.add(moveForwardRight(currentState));
			} catch (ImpossibleMovementException e) {
				// do nothing
			}

			try {
				states.add(moveBackward(currentState));
			} catch (ImpossibleMovementException e) {
				// do nothing
			}

			try {
				states.add(moveBackwardLeft(currentState));
			} catch (ImpossibleMovementException e) {
				// do nothing
			}

			try {
				states.add(moveBackwardRight(currentState));
			} catch (ImpossibleMovementException e) {
				// do nothing
			}

			if (states.size() == 0) {
				throw new RuntimeException("Cannot resolve path");
			}

			currentState = states.peek();

			iterations++;
//			if (iterations % 1000 == 0) {
				System.out.println("iter " + iterations + "   distance: " + currentState.getHeuristicDetails().getHeuristic());
				System.out.println("skipped " + skippedIterations);
				System.out.println("nodes " + (iterations - skippedIterations));
//				listener.intermediatePoints(new ArrayList<>(discardedStates));
				listener.intermediatePoints(asList(currentStateCenter));
				discardedStates.clear();
//			}

				if (iterations > 10_000_000) {
				throw new RuntimeException("Too many iterations");
			}

		}

		// go reverse
		List<Movement> controls = new LinkedList<>();
		controls.add(0, currentState.getCausedMovement());
		while (currentState.getPreviousCarState() != null) {
			currentState = currentState.getPreviousCarState();
			controls.add(0, currentState.getCausedMovement());
		}


//		// Dubins curves
//		double curvatureRadius = car.withFrontAxisAngle(FRONT_AXIS_ROTATION_ANGLE).getRotationCircleRadius();
//		Map<DubinsCurveType, DubinsCurveInfo> curves = DubinsCurves.computeCurves(
//				new PositionWithDirection(car.getBackAxleCenter(), car.getOrientationVector()),
//				new PositionWithDirection(targetPosition.subtract(targetOrientation
//						.normalized().multipliedBy(car.getChassisLength() / 2.0)), targetOrientation),
//				curvatureRadius
//				);
//
//
//		DubinsCurveType chosenCurveType = null;
//		DubinsCurveInfo chosenCurveInfo = null;
//		for (DubinsCurveType currentCurveType : curves.keySet()) {
//			DubinsCurveInfo currentCurveInfo = curves.get(currentCurveType);
//			if (chosenCurveType == null) {
//				chosenCurveType = currentCurveType;
//				chosenCurveInfo = curves.get(currentCurveType);
//			} else if (currentCurveInfo.getPathLength() < chosenCurveInfo.getPathLength()) {
//				chosenCurveInfo = currentCurveInfo;
//				chosenCurveType = currentCurveType;
//			}
//		}
//
//
//
//		int iter = 0;
//		Vector straightLineVector = new Vector(
//				chosenCurveInfo.getFirstCircleTangentPoint(),
//				chosenCurveInfo.getSecondCircleTangentPoint()
//		);
//		List<Movement> controls = new ArrayList<>();
//
//		double firstPartFrontAxisAngle;
//		Movement firstPartMovement;
//		if (chosenCurveType == RSR || chosenCurveType == RSL) {
//			firstPartFrontAxisAngle = -FRONT_AXIS_ROTATION_ANGLE;
//			firstPartMovement = Movement.FORWARD_RIGHT;
//		} else if (chosenCurveType == LSL || chosenCurveType == LSR) {
//			firstPartFrontAxisAngle = FRONT_AXIS_ROTATION_ANGLE;
//			firstPartMovement = Movement.FORWARD_LEFT;
//		} else {
//			throw new RuntimeException("Illegal curve type");
//		}
//
//		Car currentCar = car.withFrontAxisAngle(firstPartFrontAxisAngle);
//		double straightDirectionAngle = straightLineVector.angle();
//		while(abs(currentCar.getOrientationAngle() - straightDirectionAngle) > 0.05) {
//			currentCar = currentCar.movedBy(ONE_STEP_DISTANCE);
//			controls.add(firstPartMovement);
//			iter++;
//			if (iter > 2000) {
//				return controls;
//			}
//		}
//
//
//
//
//		currentCar = currentCar.withFrontAxisAngle(0.0);
//		PIDController pidController = new PIDController(1.0, 5.0, 0.0, 1.5);
//		double prevErrorToFinalPoint = Long.MAX_VALUE;
//		while(true) {
//			double currentErrorToFinalPoint =
//					currentCar.getBackAxleCenter().distanceTo(chosenCurveInfo.getSecondCircleTangentPoint());
//			if (currentErrorToFinalPoint > prevErrorToFinalPoint) {
//				break;
//			}
//			prevErrorToFinalPoint = currentErrorToFinalPoint;
//			double error = distanceFromPointToLine(currentCar.getBackAxleCenter(),
//					chosenCurveInfo.getFirstCircleTangentPoint(), chosenCurveInfo.getSecondCircleTangentPoint());
//			int steering = pidController.currentError(error);
//			Movement movement = Movement.FORWARD;
//			currentCar = currentCar.withFrontAxisAngle(0.0);
//			if (steering == 1) {
//				movement = Movement.FORWARD_LEFT;
//				currentCar = currentCar.withFrontAxisAngle(FRONT_AXIS_ROTATION_ANGLE);
//			} else if (steering == -1) {
//				movement = Movement.FORWARD_RIGHT;
//				currentCar = currentCar.withFrontAxisAngle(-FRONT_AXIS_ROTATION_ANGLE);
//			}
//			currentCar = currentCar.movedBy(ONE_STEP_DISTANCE);
//			controls.add(movement);
//			iter++;
//			if (iter > 2000) {
//				return controls;
//			}
//		}
//
//
//
//
////		double acceptableError = 1.5;
//		double secondPartFrontAxisAngle;
//		Movement secondPartMovement;
//		if (chosenCurveType == RSR || chosenCurveType == LSR) {
//			secondPartFrontAxisAngle = -FRONT_AXIS_ROTATION_ANGLE;
//			secondPartMovement = Movement.FORWARD_RIGHT;
//		} else if (chosenCurveType == LSL || chosenCurveType == RSL) {
//			secondPartFrontAxisAngle = FRONT_AXIS_ROTATION_ANGLE;
//			secondPartMovement = Movement.FORWARD_LEFT;
//		} else {
//			throw new RuntimeException("Illegal curve type");
//		}
//
//		currentCar = currentCar.withFrontAxisAngle(secondPartFrontAxisAngle);
////		while(currentCar.getCenter().distanceTo(targetPosition) > acceptableError) {
//		while(abs(currentCar.getOrientationAngle() - targetOrientation.angle()) > 0.05) {
//			currentCar = currentCar.movedBy(ONE_STEP_DISTANCE);
//			controls.add(secondPartMovement);
//			iter++;
//			if (iter > 2000) {
//				return controls;
//			}
//		}

		return controls;
	}
//
//	private boolean carOrientationIsProper(Car car, double precision) {
//		double carAngle = car.getOrientationAngle();
//		double targetAngle = targetOrientation.angle();
//
//		double deltaAngle = atan2(sin(carAngle - targetAngle), cos(carAngle - targetAngle));
//		return abs(deltaAngle) < precision;
//	}

	private double orientationError(Car car) {
		double carAngle = car.getOrientationAngle();
		double targetAngle = targetOrientation.angle();

		return atan2(sin(carAngle - targetAngle), cos(carAngle - targetAngle));
	}


	private List<Point> computeThetaStarPath(Car car) {
		int startXIndex = (int) floor(car.getX() / obstacleSize);
		int startYIndex = (int) floor(car.getY() / obstacleSize);
		IntegerPoint start = new IntegerPoint(startXIndex, startYIndex);

		int finishXIndex = (int) floor(targetPosition.getX() / obstacleSize);
		int finishYIndex = (int) floor(targetPosition.getY() / obstacleSize);
		IntegerPoint finish = new IntegerPoint(finishXIndex, finishYIndex);

		int minPassageWidth = (int)ceil(car.getBodyWidth() / obstacleSize);
		ThetaStar thetaStar = new ThetaStar(field, start, finish, minPassageWidth);

		List<IntegerPoint> pathInteger = thetaStar.findPath();
		List<Point> pathReal = new ArrayList<>(pathInteger.size());
		for (IntegerPoint integerPoint : pathInteger) {
			double x = integerPoint.getX() * obstacleSize;
			double y = integerPoint.getY() * obstacleSize;
			pathReal.add(new Point(x, y));
		}
		return pathReal;
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
		return createCarStateIfPossible(movedCar, Movement.FORWARD, carState);
	}

	private CarState moveForwardLeft(CarState carState) throws ImpossibleMovementException {
		Car movedCar = carState.car.withFrontAxisAngle(FRONT_AXIS_ROTATION_ANGLE).movedBy(ONE_STEP_DISTANCE);
		return createCarStateIfPossible(movedCar, Movement.FORWARD_LEFT, carState);
	}

	private CarState moveForwardRight(CarState carState) throws ImpossibleMovementException {
		Car movedCar = carState.car.withFrontAxisAngle(-FRONT_AXIS_ROTATION_ANGLE).movedBy(ONE_STEP_DISTANCE);
		return createCarStateIfPossible(movedCar, Movement.FORWARD_RIGHT, carState);
	}

	private CarState moveBackward(CarState carState) throws ImpossibleMovementException {
		Car movedCar = carState.car.withFrontAxisAngle(0.0).movedBy(-ONE_STEP_DISTANCE);
		return createCarStateIfPossible(movedCar, Movement.BACKWARD, carState);
	}

	private CarState moveBackwardLeft(CarState carState) throws ImpossibleMovementException {
		Car movedCar = carState.car.withFrontAxisAngle(FRONT_AXIS_ROTATION_ANGLE).movedBy(-ONE_STEP_DISTANCE);
		return createCarStateIfPossible(movedCar, Movement.BACKWARD_LEFT, carState);
	}

	private CarState moveBackwardRight(CarState carState) throws ImpossibleMovementException {
		Car movedCar = carState.car.withFrontAxisAngle(-FRONT_AXIS_ROTATION_ANGLE).movedBy(-ONE_STEP_DISTANCE);
		return createCarStateIfPossible(movedCar, Movement.BACKWARD_RIGHT, carState);
	}

	private CarState createCarStateIfPossible(Car movedCar, Movement movement, CarState previousState)
			throws ImpossibleMovementException {
		if (collisionDetectorDiscreteField.collides(movedCar)) {
			throw new ImpossibleMovementException();
		}
		return new CarState(
				movedCar,
				movement,
				previousState,
				computeHeuristic(movedCar, movement, previousState)
		);
	}

	private HeuristicDetails computeHeuristic(Car car, Movement movement, CarState prevState) {
		double distanceToTarget = computeDistanceToTarget(car);

		if (prevState == null) {
			checkArgument(movement == null);

			return new HeuristicDetails(0.0, distanceToTarget, 0.0, distanceToTarget);
		}

//		Car car = prevState.getCar();
		double cellWeight = computeCellWeight(car);

//		Vector fromCarToTarget = new Vector(car.getCenter(), targetPosition);
//		double orientationWeight = abs(car.getOrientationAngle() - fromCarToTarget.angle()) * 20;

//		return computeDistanceToTarget(car) + positionWeight + orientationWeight;



		double finalOrientationError = abs(orientationError(car));
		double orientationErrorCoef = 50;
		double orientationErrorWeight = orientationErrorCoef / (distanceToTarget * distanceToTarget);
//		double orientationErrorWeight = orientationErrorCoef;
		double orientationErrorTerm = finalOrientationError * orientationErrorWeight;
//		double orientationErrorTerm = 0;

		double accumulatedWeight = prevState.getHeuristicDetails().getAccumulatedWeight();


		if (movement.isBackward()) {
			accumulatedWeight += 25;
		}


		if (prevState.getCausedMovement() != null) {
			if (movement != prevState.getCausedMovement()) {
				accumulatedWeight +=100;
			}
		}


		double prevDistance = prevState.getHeuristicDetails().getPreviousDistance();

		double heuristic = prevDistance + distanceToTarget + accumulatedWeight + cellWeight;

		HeuristicDetails details =
				new HeuristicDetails(prevDistance, distanceToTarget, accumulatedWeight, heuristic);
//		return distanceToTarget + carState.getPreviousDistance() + positionWeight + orientationErrorTerm;
//		return distanceToTarget + carState.getPreviousDistance() + positionWeight + carState.getAccumulatedWeight();
//		return distanceToTarget + carState.getPreviousDistance() + carState.getAccumulatedWeight() + orientationErrorCoef;
		return details;
//		return carState.getPreviousDistance() + abs(finalOrientationError * 10);
//		return distanceToTarget;
	}

	private HeuristicDetails recomputeHeuristic(CarState state) {
		HeuristicDetails prevDetails = state.getHeuristicDetails();
		return new HeuristicDetails(
				prevDetails.getPreviousDistance(),
				prevDetails.getDistanceToTarget(),
				prevDetails.getAccumulatedWeight(),
				computeCellWeight(state.getCar())
		);
	}

	private double computeCellWeight(Car car) {
		double positionWeight = 0.0;
		CellWeight cellWeight = weights.get(roundPoint(car.getBackAxleCenter()));
		if (cellWeight != null) {
			positionWeight = cellWeight.getWeight(car.getOrientationAngle());
		}
		return positionWeight;
	}

	private double computeDistanceToTarget(Car car) {
		return car.getBackAxleCenter().distanceTo(targetPosition);
	}

	private static final class CarState implements Comparable<CarState> {
		private static final double WEIGHT_FOR_CHANGING_CONTROL = 100;

		private final Car car;
		private final CarState previousCarState;
		private final Movement causedMovement;
		private final HeuristicDetails heuristicDetails;



		public CarState(Car car, Movement causedMovement, CarState previousCarState,
		                HeuristicDetails heuristicDetails) {
			this.car = car;
			this.causedMovement = causedMovement;
			this.previousCarState = previousCarState;
			this.heuristicDetails = heuristicDetails;
		}

		public Car getCar() {
			return car;
		}

		public Movement getCausedMovement() {
			return causedMovement;
		}

		public CarState getPreviousCarState() {
			return previousCarState;
		}

		public HeuristicDetails getHeuristicDetails() {
			return heuristicDetails;
		}

		@Override
		public int compareTo(CarState another) {
			return Double.compare(this.heuristicDetails.getHeuristic(), another.heuristicDetails.getHeuristic());
		}

		@Override
		public String toString() {
			return "CarState{" +
					"x = " + car.getX() + ", y = " + car.getY() +
					", h = " + heuristicDetails;
		}
	}

	private static final class HeuristicDetails {
		private final double previousDistance;
		private final double distanceToTarget;
		private final double accumulatedWeight;
		private final double heuristic;

		public HeuristicDetails(double previousDistance, double distanceToTarget, double accumulatedWeight, double heuristic) {
			this.previousDistance = previousDistance;
			this.distanceToTarget = distanceToTarget;
			this.accumulatedWeight = accumulatedWeight;
			this.heuristic = heuristic;
		}

		public double getPreviousDistance() {
			return previousDistance;
		}

		public double getDistanceToTarget() {
			return distanceToTarget;
		}

		public double getAccumulatedWeight() {
			return accumulatedWeight;
		}

		public double getHeuristic() {
			return heuristic;
		}

		@Override
		public String toString() {
			return "HeuristicDetails{" +
					"previousDistance=" + previousDistance +
					", distanceToTarget=" + distanceToTarget +
					", accumulatedWeight=" + accumulatedWeight +
					", heuristic=" + heuristic +
					'}';
		}
	}

}
