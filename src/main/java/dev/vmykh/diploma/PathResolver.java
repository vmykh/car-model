package dev.vmykh.diploma;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.*;
import static java.util.Arrays.asList;

public final class PathResolver {
	private static final int WEIGHTED_DIRECTIONS_PER_CELL = 32;


	private final double unitStepDistance;
	private final double leftSteeringAngle;
	private final double rightSteeringAngle;

	private final CollisionDetector collisionDetector;

//	public static final double FRONT_AXIS_ROTATION_ANGLE = PI / 8.0;
//	public static final double ACCEPTABLE_FINISH_POSITION_ERROR = 30.0;

	private final double acceptableHeuristicDifference;

//	private final Point targetPosition;
//	private final Vector targetOrientation;

	private final int weightCellSize;
	private final Map<IntegerPoint, CellWeight> weights = new HashMap<>();

	private final PathResolverListener listener;

	private final Field field;
	private final double obstacleSize;

	private final Car carModel;

	public PathResolver(double worldWidth, double worldHeight,
	                    Set<IntegerPoint> obstacles, double obstacleSize,
	                    CollisionDetector collisionDetector,
	                    Car car, CarMovementParameters carMovementParameters,
	                    PathResolverListener listener) {

//		this.targetPosition = target.getPosition();
//		this.targetOrientation = target.getDirection();
		this.collisionDetector = collisionDetector;
		this.listener = listener;

		this.carModel = car;

		this.unitStepDistance = carMovementParameters.getUnitStepDistance();
		this.leftSteeringAngle = carMovementParameters.getLeftSteeringAngle();
		this.rightSteeringAngle = carMovementParameters.getRightSteeringAngle();

		this.acceptableHeuristicDifference = unitStepDistance;
		this.weightCellSize = (int) unitStepDistance;

		this.obstacleSize = obstacleSize;
		int fieldWidth = (int)(worldWidth / obstacleSize);
		int fieldHeight = (int)(worldHeight / obstacleSize);
		this.field = new Field(fieldWidth, fieldHeight, obstacles);
	}

	public List<Movement> resolvePath(PositionWithDirection start, PositionWithDirection target) {
//		unitStepDistance = car.getChassisLength() * 1.25;
//		acceptableHeuristicDifference = unitStepDistance;
//		weightCellSize = (int) unitStepDistance;

		Car currentStartCar = Car.createWithSameWidthAndLength(carModel)
				.setInitialPosition(start.getPosition())
				.setInitialOrientation(start.getDirection());

		List<Movement> controls = new LinkedList<>();

		List<Movement> currentAttemntControls = new LinkedList<>();

		int attemptsToRestart = 0;
		while(attemptsToRestart < 10) {

			System.out.println("attemps to restart used: " + attemptsToRestart);

			try {
				List<Point> thetaStarPath;
				try {
					thetaStarPath = computeThetaStarPath(currentStartCar, target.getPosition());
				} catch (Exception e) {
					System.out.println("Cannot find path using theta star");
					e.printStackTrace();
					return new ArrayList<>();
				}
				listener.thetaStarPoints(thetaStarPath);

				for (int i = 1; i < thetaStarPath.size() - 1; i++) {
					Point previous = thetaStarPath.get(i - 1);
					Point current = thetaStarPath.get(i);
					Point next = thetaStarPath.get(i + 1);
					Vector targetDirection = new Vector(previous, current).add(new Vector(current, next));
					Point targetPosition = current.add(targetDirection.perpendicular().normalizedTo(unitStepDistance));

					double positionAcceptableError = unitStepDistance * 2;
					double directionAcceptableError = PI / 10;

					CarState finalState;

//					try {
						finalState = findNonHolonomicPath(currentStartCar, targetPosition, targetDirection,
								positionAcceptableError, directionAcceptableError);
//					} catch (IllegalStateException e) {
//						return new ArrayList<>();
//					}


					;
					currentStartCar = finalState.getCar();

					List<Movement> subpathControls = new LinkedList<>();
					CarState currentState = finalState;
					subpathControls.add(0, currentState.getCausedMovement());
					while (currentState.getPreviousCarState() != null) {
						currentState = currentState.getPreviousCarState();
						subpathControls.add(0, currentState.getCausedMovement());
					}
					currentAttemntControls.addAll(subpathControls);

				}

				double positionAcceptableError = unitStepDistance;
				double directionAcceptableError = PI / 16;

				CarState finalState;

//				try {
					finalState = findNonHolonomicPath(currentStartCar, target.getPosition(), target.getDirection(),
							positionAcceptableError, directionAcceptableError);
//				} catch (IllegalStateException e) {
//					return new ArrayList<>();
//				}

				List<Movement> subpathControls = new LinkedList<>();
				CarState currentState = finalState;
				subpathControls.add(0, currentState.getCausedMovement());
				while (currentState.getPreviousCarState() != null) {
					currentState = currentState.getPreviousCarState();
					subpathControls.add(0, currentState.getCausedMovement());
				}
				currentAttemntControls.addAll(subpathControls);

				controls.addAll(currentAttemntControls);
				return controls;
			} catch (TooManyIterationException e) {
				// make random movement
//				Movement randomMovement = chooseRandomMovement(currentStartCar);

				attemptsToRestart++;

				currentAttemntControls.clear();

				// TODO(vmykh): refactor this shit
				while(true) {
					Random random = new Random();
					int randomNumber = random.nextInt();
					if (randomNumber == Integer.MIN_VALUE) {
						randomNumber = Integer.MAX_VALUE;
					}

					int movementNumber = abs(randomNumber) % 6;

					Movement movement;
					Car movedCar;
					if (movementNumber == 0) {
						movement = Movement.FORWARD;
						movedCar = currentStartCar.movedBy(unitStepDistance);
					} else if (movementNumber == 1) {
						movement = Movement.FORWARD_LEFT;
						movedCar = currentStartCar.withFrontAxisAngle(leftSteeringAngle).movedBy(unitStepDistance);
					} else if (movementNumber == 2) {
						movement = Movement.FORWARD_RIGHT;
						movedCar = currentStartCar.withFrontAxisAngle(-rightSteeringAngle).movedBy(unitStepDistance);
					} else if (movementNumber == 3) {
						movement = Movement.BACKWARD;
						movedCar = currentStartCar.movedBy(-unitStepDistance);
 					} else if (movementNumber == 4) {
						movement = Movement.BACKWARD_LEFT;
						movedCar = currentStartCar.withFrontAxisAngle(leftSteeringAngle).movedBy(-unitStepDistance);
					} else if (movementNumber == 5) {
						movement = Movement.BACKWARD_RIGHT;
						movedCar = currentStartCar.withFrontAxisAngle(-rightSteeringAngle).movedBy(-unitStepDistance);
					} else {
						throw new RuntimeException();
					}

					if (!collisionDetector.collides(movedCar)) {
						controls.add(movement);
						currentStartCar = movedCar;
					}

					break;
				}


			}
		}
		System.out.println("returning empty list");
		return new ArrayList<>();
	}

	private CarState findNonHolonomicPath(Car car, Point targetPosition, Vector targetDirection,
	                                      double positionAcceptableError, double directionAcceptableError)
			throws TooManyIterationException {
		PriorityQueue<CarState> states = new PriorityQueue<>();
		PositionWithDirection localTarget = new PositionWithDirection(targetPosition, targetDirection);
		PositionWithDirection localStart = new PositionWithDirection(car.getBackAxleCenter(), car.getOrientationVector());
		CarState currentState = new CarState(car, null, null, computeHeuristic(car, null, null, localTarget, localStart));
		List<Point> discardedStates = new ArrayList<>();
		int iterations = 0;
		int skippedIterations = 0;


//		while (computeDistanceToTarget(currentState.getCar()) > positionAcceptableError
//				||(abs(orientationError(currentState.getCar())) > directionAcceptableError)) {


		while (currentState.getCar().getBackAxleCenter().distanceTo(targetPosition) > positionAcceptableError
				||(abs(angleBetween(currentState.getCar().getOrientationVector(), targetDirection)) > directionAcceptableError)) {

//			System.out.println("distance to target: " + currentState.getCar().getBackAxleCenter().distanceTo(targetPosition));
//			System.out.println("angle error: " + abs(angleBetween(currentState.getCar().getOrientationVector(), targetDirection)));
//			if (iterations > 10_000) {
//				System.out.println("bangura");
//			}


			if (currentState.previousCarState != null) {
				double currentStateHeuristic = currentState.getHeuristicDetails().getHeuristic();
				HeuristicDetails actualHeuristicDetails = recomputeHeuristic(currentState);
				double actualHeuristic = actualHeuristicDetails.getHeuristic();
				if (abs(currentStateHeuristic - actualHeuristic) > acceptableHeuristicDifference) {
					states.remove(currentState);
					states.add(
							new CarState(currentState.getCar(), currentState.causedMovement,
									currentState.getPreviousCarState(), actualHeuristicDetails)
					);

					iterations++;
					skippedIterations++;
					currentState = states.peek();

//					System.out.println("distance to target: " + currentState.getCar().getBackAxleCenter().distanceTo(targetPosition));
//					System.out.println("angle error: " + abs(angleBetween(currentState.getCar().getOrientationVector(), targetDirection)));
//					System.out.println("before continue");
					continue;
				}
			}

			Point currentStateCenter = currentState.getCar().getCenter();

			IntegerPoint intPoint = roundPoint(currentStateCenter);
			if (!weights.containsKey(intPoint)) {
				weights.put(intPoint, new CellWeight(WEIGHTED_DIRECTIONS_PER_CELL));
			}
			weights.get(intPoint).addWeight(currentState.getCar().getOrientationAngle());

			discardedStates.add(currentStateCenter);
			states.remove(currentState);



			try {
				states.add(moveForward(currentState, localTarget, localStart));
			} catch (ImpossibleMovementException e) {
				// do nothing
			}

			try {
				states.add(moveForwardLeft(currentState, localTarget, localStart));
			} catch (ImpossibleMovementException e) {
				// do nothing
			}

			try {
				states.add(moveForwardRight(currentState, localTarget, localStart));
			} catch (ImpossibleMovementException e) {
				// do nothing
			}

			try {
				states.add(moveBackward(currentState, localTarget, localStart));
			} catch (ImpossibleMovementException e) {
				// do nothing
			}

			try {
				states.add(moveBackwardLeft(currentState, localTarget, localStart));
			} catch (ImpossibleMovementException e) {
				// do nothing
			}

			try {
				states.add(moveBackwardRight(currentState, localTarget, localStart));
			} catch (ImpossibleMovementException e) {
				// do nothing
			}

			if (states.size() == 0) {
				throw new RuntimeException("Cannot resolve path");
			}

			currentState = states.peek();

			iterations++;
//			if (iterations % 1000 == 0) {
//			System.out.println("iter " + iterations + "   distance: " + currentState.getHeuristicDetails().getHeuristic());
//			System.out.println("skipped " + skippedIterations);
//			System.out.println("nodes " + (iterations - skippedIterations));
//				listener.intermediatePoints(new ArrayList<>(discardedStates));
			listener.intermediatePoints(asList(currentStateCenter));
			discardedStates.clear();
//			}

//			if (iterations > 10_000_000) {
//				throw new RuntimeException("Too many iterations");
//			}

			if (iterations > 25_000) {
				throw new TooManyIterationException("Too many iterations");
			}


			if (iterations % 1000 == 0) {
				System.out.println("iterations: " + iterations);
//				System.out.println("distance to target: " + currentState.getCar().getBackAxleCenter().distanceTo(targetPosition));
//				System.out.println("angle error: " + abs(angleBetween(currentState.getCar().getOrientationVector(), targetDirection)));
//				System.out.println("target: " + targetPosition);
//				System.out.println("car: " + currentState.getCar());
			}

		}
		System.out.println("Finish!");
		return currentState;
	}
//
//	private boolean carOrientationIsProper(Car car, double precision) {
//		double carAngle = car.getOrientationAngle();
//		double targetAngle = targetOrientation.angle();
//
//		double deltaAngle = atan2(sin(carAngle - targetAngle), cos(carAngle - targetAngle));
//		return abs(deltaAngle) < precision;
//	}

//	private double orientationError(Car car) {
//		double carAngle = car.getOrientationAngle();
//		double targetAngle = targetOrientation.angle();
//
//		return atan2(sin(carAngle - targetAngle), cos(carAngle - targetAngle));
//	}

	private double angleBetween(Vector first, Vector second) {
		double firstAngle = first.angle();
		double secondAngle = second.angle();
		return atan2(sin(firstAngle - secondAngle), cos(firstAngle - secondAngle));
	}


	private List<Point> computeThetaStarPath(Car car, Point targetPosition) {
		int startXIndex = (int) floor(car.getX() / obstacleSize);
		int startYIndex = (int) floor(car.getY() / obstacleSize);
		IntegerPoint start = new IntegerPoint(startXIndex, startYIndex);

		int finishXIndex = (int) floor(targetPosition.getX() / obstacleSize);
		int finishYIndex = (int) floor(targetPosition.getY() / obstacleSize);
		IntegerPoint finish = new IntegerPoint(finishXIndex, finishYIndex);

		int minPassageWidth = ((int)ceil((car.getBodyWidth() / obstacleSize)));
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
		int shiftedX = roundedX - (roundedX % weightCellSize);
		int shiftedY = roundedY - (roundedY % weightCellSize);
		return new IntegerPoint(shiftedX, shiftedY);
	}

	private CarState moveForward(CarState carState, PositionWithDirection localTarget, PositionWithDirection localStart) throws ImpossibleMovementException {
		Car movedCar = carState.car.withFrontAxisAngle(0.0).movedBy(unitStepDistance);
		return createCarStateIfPossible(movedCar, Movement.FORWARD, carState, localTarget, localStart);
	}

	private CarState moveForwardLeft(CarState carState, PositionWithDirection localTarget, PositionWithDirection localStart) throws ImpossibleMovementException {
		Car movedCar = carState.car.withFrontAxisAngle(leftSteeringAngle).movedBy(unitStepDistance);
		return createCarStateIfPossible(movedCar, Movement.FORWARD_LEFT, carState, localTarget, localStart);
	}

	private CarState moveForwardRight(CarState carState, PositionWithDirection localTarget, PositionWithDirection localStart) throws ImpossibleMovementException {
		Car movedCar = carState.car.withFrontAxisAngle(-rightSteeringAngle).movedBy(unitStepDistance);
		return createCarStateIfPossible(movedCar, Movement.FORWARD_RIGHT, carState, localTarget, localStart);
	}

	private CarState moveBackward(CarState carState, PositionWithDirection localTarget, PositionWithDirection localStart) throws ImpossibleMovementException {
		Car movedCar = carState.car.withFrontAxisAngle(0.0).movedBy(-unitStepDistance);
		return createCarStateIfPossible(movedCar, Movement.BACKWARD, carState, localTarget, localStart);
	}

	private CarState moveBackwardLeft(CarState carState, PositionWithDirection localTarget, PositionWithDirection localStart) throws ImpossibleMovementException {
		Car movedCar = carState.car.withFrontAxisAngle(leftSteeringAngle).movedBy(-unitStepDistance);
		return createCarStateIfPossible(movedCar, Movement.BACKWARD_LEFT, carState, localTarget, localStart);
	}

	private CarState moveBackwardRight(CarState carState, PositionWithDirection localTarget, PositionWithDirection localStart) throws ImpossibleMovementException {
		Car movedCar = carState.car.withFrontAxisAngle(-rightSteeringAngle).movedBy(-unitStepDistance);
		return createCarStateIfPossible(movedCar, Movement.BACKWARD_RIGHT, carState, localTarget, localStart);
	}

	private CarState createCarStateIfPossible(Car movedCar, Movement movement, CarState previousState,
	                                          PositionWithDirection localTarget, PositionWithDirection localStart)
			throws ImpossibleMovementException {
		if (collisionDetector.collides(movedCar)) {
			throw new ImpossibleMovementException();
		}
		return new CarState(
				movedCar,
				movement,
				previousState,
				computeHeuristic(movedCar, movement, previousState, localTarget, localStart)
		);
	}

	private HeuristicDetails computeHeuristic(Car car, Movement movement, CarState prevState,
	                                          PositionWithDirection target, PositionWithDirection start) {
		double distanceToTarget = car.getBackAxleCenter().distanceTo(target.getPosition());

		if (prevState == null) {
			checkArgument(movement == null);

			return new HeuristicDetails(0.0, distanceToTarget, 0.0, distanceToTarget);
		}

		double cellWeight = computeCellWeight(car);

		double finalOrientationError = abs(angleBetween(
				car.getOrientationVector(),
				new Vector(car.getBackAxleCenter(), target.getPosition())));
		double orientationErrorCoef = 2.0;
		double orientationErrorWeight = orientationErrorCoef * distanceToTarget;
		double orientationErrorTerm = finalOrientationError * orientationErrorWeight;


		double straightLineError = abs(distanceFromPointToLine(car.getBackAxleCenter(), start.getPosition(), target.getPosition()));
		double moveOnStraightLineTerm = 0.1 * straightLineError * distanceToTarget;



		double accumulatedWeight = prevState.getHeuristicDetails().getAccumulatedWeight();


		if (movement.isBackward()) {
			accumulatedWeight += unitStepDistance * 0.75;
		} else if (movement == Movement.FORWARD_LEFT || movement == Movement.FORWARD_RIGHT) {
			accumulatedWeight += unitStepDistance * 1.5;
		}



		if (prevState.getCausedMovement() != null) {
			if (movement != prevState.getCausedMovement()) {
				accumulatedWeight += unitStepDistance * 10.0;
			}
		}


		double prevDistance = prevState.getHeuristicDetails().getPreviousDistance() + unitStepDistance;

		double heuristic = distanceToTarget + cellWeight * 1.5 + accumulatedWeight + prevDistance + moveOnStraightLineTerm + orientationErrorTerm;

		HeuristicDetails details =
				new HeuristicDetails(prevDistance, distanceToTarget, accumulatedWeight, heuristic);
		return details;
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
//
//	private double computeDistanceToTarget(Car car, Point targetPosition) {
//		return car.getBackAxleCenter().distanceTo(targetPosition);
//	}

	private static final class CarState implements Comparable<CarState> {
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
