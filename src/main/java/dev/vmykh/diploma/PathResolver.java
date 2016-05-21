package dev.vmykh.diploma;

import java.util.*;

import static java.lang.Math.PI;
import static java.lang.Math.abs;

public final class PathResolver {
	public static final double ONE_STEP_DISTANCE = 5.0;
	public static final double FRONT_AXIS_ROTATION_ANGLE = PI / 8.0;
	public static final double ACCEPTABLE_FINISH_POSITION_ERROR = 10.0;
	private static final int WEIGHTED_DIRECTIONS_PER_CELL = 32;

	public static final double ACCEPTABLE_HEURISTIC_DIFFERENCE = 50.0;

	private final Point target;
	private final CollisionDetector collisionDetector;

	private static final int WEIGHT_CELL_SIZE = 3;
	private final Map<IntegerPoint, CellWeight> weights = new HashMap<>();

	private final PathResolverListener listener;

	public PathResolver(Point target, CollisionDetector collisionDetector, PathResolverListener listener) {
		this.target = target;
		this.collisionDetector = collisionDetector;
		this.listener = listener;
	}

	public List<Movement> resolvePath(Car car) {
		PriorityQueue<CarState> states = new PriorityQueue<>();
		CarState currentState = new CarState(car, null, computeHeuristic(car), null);
		List<Point> discardedStates = new ArrayList<>();
		int iterations = 0;
		int skippedIterations = 0;
		while (computeDistanceToTarget(currentState.getCar()) > ACCEPTABLE_FINISH_POSITION_ERROR) {
//			if (iterations > 10_000) {
//				System.out.println("bangura");
//			}


			if (currentState.previousCarState != null) {
				double currentStateHeuristic = currentState.getHeuristic();
				double actualHeuristic = computeHeuristic(currentState.getCar());
				if (abs(currentStateHeuristic - actualHeuristic) > ACCEPTABLE_HEURISTIC_DIFFERENCE) {
					states.remove(currentState);
					states.add(
							new CarState(currentState.getCar(), currentState.causedMovement, actualHeuristic,
									currentState.getPreviousCarState())
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
			if (iterations % 1000 == 0) {
				System.out.println("iter " + iterations + "   distance: " + currentState.getHeuristic());
				listener.intermediatePoints(new ArrayList<>(discardedStates));
				discardedStates.clear();
			}

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

		return controls;
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

		Vector fromCarToTarget = new Vector(car.getCenter(), target);
		double orientationWeight = abs(car.getOrientationAngle() - fromCarToTarget.getAngle()) * 20;

//		return computeDistanceToTarget(car) + positionWeight + orientationWeight;
		return computeDistanceToTarget(car) + positionWeight;
	}

	private double computeDistanceToTarget(Car car) {
		return car.getCenter().distanceTo(target);
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
