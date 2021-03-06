package dev.vmykh.diploma;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.*;

public class Main extends Application {
	private static final int CANVAS_WIDTH = 700;
	private static final int CANVAS_HEIGHT = 700;

	private static final double CHASSIS_WIDTH = 15;
	private static final double CHASSIS_LENGTH = 20;
	private static final double BODY_WIDTH = CHASSIS_WIDTH * 1.3;
	private static final double BODY_LENGTH = CHASSIS_LENGTH * 1.65;

	private static final double OBSTACLE_SIZE = 10;

	private GraphicsContext gc;
	private Canvas canvas;
	private volatile Car car;

	private List<Point> tracePoints = new ArrayList<>();

	private Timer timer = new Timer();

	private CollisionDetectorDiscreteField collisionDetector = new CollisionDetectorDiscreteField(
			new HashSet<>(), OBSTACLE_SIZE, CANVAS_WIDTH, CANVAS_HEIGHT);

	private volatile boolean upKeyIsPressed = false;
	private volatile boolean downKeyIsPressed = false;
	private volatile boolean rightKeyIsPressed = false;
	private volatile boolean leftKeyIsPressed = false;

	private Point firstTargetPoint;
	private Point secondTargetPoint;

	private Point targetTemp;

	private Point firstCarPoint;
	private Point secondCarPoint;

	private volatile List<Point> target;

	private volatile boolean cancelOrdinaryTask = false;

//	private List<Obstacle> obstacles = new ArrayList();

	private final CarMovementParameters carMovementParameters = new CarMovementParameters(CHASSIS_LENGTH * 1.15, PI / 7, PI / 7);

	private Set<IntegerPoint> obstacleset = new HashSet<>();

	private CopyOnWriteArrayList<List<Point>> intermediatePath = new CopyOnWriteArrayList<>();
	private int drawedSubpathes = 0;
	private final AtomicBoolean computingPathNow = new AtomicBoolean(false);

	private CopyOnWriteArrayList<List<Point>> thetaStarPaths = new CopyOnWriteArrayList<>();

	@Override
	public void start(Stage stage) {
		initUI(stage);
	}

	private void initUI(Stage stage) {

		Pane root = new Pane();

		canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
		gc = canvas.getGraphicsContext2D();


		car = createInitialCar();
		createInitialTarget();

		drawCar(car);

		root.getChildren().add(canvas);

		EventHandler<KeyEvent> keyListener = new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.UP) {
					if (event.getEventType() == KeyEvent.KEY_PRESSED) {
						upKeyIsPressed = true;
					} else {
						upKeyIsPressed = false;
					}
				} else if (event.getCode() == KeyCode.DOWN) {
					if (event.getEventType() == KeyEvent.KEY_PRESSED) {
						downKeyIsPressed = true;
					} else {
						downKeyIsPressed = false;
					}
				} else if (event.getCode() == KeyCode.LEFT) {
					if (event.getEventType() == KeyEvent.KEY_PRESSED) {
						leftKeyIsPressed = true;
					} else {
						leftKeyIsPressed = false;
					}
				} else if (event.getCode() == KeyCode.RIGHT) {
					if (event.getEventType() == KeyEvent.KEY_PRESSED) {
						rightKeyIsPressed = true;
					} else {
						rightKeyIsPressed = false;
					}
				} else if (event.getCode() == KeyCode.R) {
					car = null;
					target = null;
					tracePoints.clear();
					obstacleset.clear();

					thetaStarPaths.clear();

					collisionDetector = new CollisionDetectorDiscreteField(
							new HashSet<>(), OBSTACLE_SIZE, CANVAS_WIDTH, CANVAS_HEIGHT);
					firstCarPoint = null;
					secondCarPoint = null;
					firstTargetPoint = null;
					secondTargetPoint = null;
				} else if (event.getCode() == KeyCode.P && event.getEventType() == KeyEvent.KEY_RELEASED) {
					Point targetCenter = target.get(0).add(new Vector(target.get(0), target.get(2)).multipliedBy(0.5));
					Vector targetOrientation = new Vector(target.get(0), target.get(1));
					Point targetBackCenter = targetCenter.subtract(targetOrientation
							.normalized().multipliedBy(car.getChassisLength() / 2.0));
					PositionWithDirection target = new PositionWithDirection(targetBackCenter, targetOrientation);
					computingPathNow.set(true);
					cancelOrdinaryTask = true;
					intermediatePath.clear();
					Executor executor = Executors.newSingleThreadExecutor();
					AtomicReference<List<Movement>> controls = new AtomicReference<>();

//					Point targetPosition = target.getPosition().subtract(targetOrientation
//							.normalized().multipliedBy(car.getChassisLength() / 2.0));

					targetTemp = targetBackCenter;

					executor.execute(() -> {
						controls.set(new PathResolver(
								CANVAS_WIDTH, CANVAS_HEIGHT, obstacleset, OBSTACLE_SIZE, collisionDetector, car,
								carMovementParameters, new IntermediatePathPainter()).resolvePath(
								new PositionWithDirection(car.getBackAxleCenter(), car.getOrientationVector()),
								target));
						computingPathNow.set(false);


						List<Movement> tripledControls = new LinkedList<Movement>();
						for (Movement movement : controls.get()) {
							tripledControls.add(movement);
							tripledControls.add(movement);
							tripledControls.add(movement);
						}


						timer.schedule(createTimerTaskAutonomousDriving(tripledControls), 25L);
					});
					System.out.println(controls);
				}
			}
		};

		canvas.addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						double mouseX = e.getX();
						double mouseY = canvas.getHeight() - e.getY();

						if (e.getButton() == MouseButton.PRIMARY) {


							if (car == null) {
								if (firstCarPoint == null) {
									firstCarPoint = new Point(mouseX, mouseY);
								} else if (secondCarPoint == null) {
									secondCarPoint = new Point(mouseX, mouseY);
									car = createCar(firstCarPoint, new Vector(firstCarPoint, secondCarPoint));
								}
							}
							else if (firstTargetPoint == null) {
								firstTargetPoint = new Point(mouseX, mouseY);
							} else if (secondTargetPoint == null) {
								secondTargetPoint = new Point(mouseX, mouseY);
								Vector direction = new Vector(firstTargetPoint, secondTargetPoint);
								createTarget(car, firstTargetPoint, direction);
							}
						}
					}
				});


		canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
				new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						double mouseX = e.getX();
						double mouseY = canvas.getHeight() - e.getY();

						if (e.getButton() == MouseButton.SECONDARY) {
							int obstacleXIndex = (int) floor(mouseX / OBSTACLE_SIZE);
							int obstacleYIndex = (int) floor(mouseY / OBSTACLE_SIZE);
							IntegerPoint point = new IntegerPoint(obstacleXIndex, obstacleYIndex);
							obstacleset.add(point);
							collisionDetector = new CollisionDetectorDiscreteField(obstacleset, OBSTACLE_SIZE,
									CANVAS_WIDTH, CANVAS_HEIGHT);
						}
					}
				});



		canvas.addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						double mouseX = e.getX();
						double mouseY = canvas.getHeight() - e.getY();

						if (e.getButton() == MouseButton.SECONDARY) {
							int obstacleXIndex = (int) floor(mouseX / OBSTACLE_SIZE);
							int obstacleYIndex = (int) floor(mouseY / OBSTACLE_SIZE);
							IntegerPoint point = new IntegerPoint(obstacleXIndex, obstacleYIndex);
							if (obstacleset.contains(point)) {
								obstacleset.remove(point);
							}
							collisionDetector = new CollisionDetectorDiscreteField(obstacleset, OBSTACLE_SIZE,
									CANVAS_WIDTH, CANVAS_HEIGHT);
						}
					}
				});






		timer.schedule(createTimerTask(), 0L);


		Scene scene = new Scene(root, CANVAS_WIDTH, CANVAS_HEIGHT, Color.WHITESMOKE);

		stage.addEventHandler(KeyEvent.ANY, keyListener);

		stage.setTitle("Lines");
		stage.setScene(scene);
		stage.show();
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				Platform.exit();
				System.exit(0);
			}
		});

	}

	private Car createInitialCar() {
		return new Car(BODY_WIDTH, BODY_LENGTH, CHASSIS_WIDTH, CHASSIS_LENGTH)
				.setInitialPosition(350, 550)
				.setInitialOrientation(PI / 2);
	}

	private void createInitialTarget() {
		createTarget(car, new Point(350, 150), Vector.fromAngle(-PI/2));
	}

	private static Car createCar(Point backCenterPoint, Vector direction) {
		return new Car(BODY_WIDTH, BODY_LENGTH, CHASSIS_WIDTH, CHASSIS_LENGTH)
				.setInitialPosition(backCenterPoint.add(direction.normalized().multipliedBy(CHASSIS_LENGTH * 0.5)))
				.setInitialOrientation(direction.angle());
	}

	private void createTarget(Car car, Point backCenterPosition, Vector direction) {
		double w = car.getBodyWidth();
		double l = car.getBodyLength();

		Vector directionNormalized = direction.normalized();
		Vector perpendicularDirectionNormalized = direction.perpendicular().normalized();
		Point firstPoint = backCenterPosition.add(perpendicularDirectionNormalized.multipliedBy(w * 0.5));
		Point secondPoint = firstPoint.add(directionNormalized.multipliedBy(l));
		Point thirdPoint = secondPoint.add(perpendicularDirectionNormalized.negative().multipliedBy(w));
		Point fourthPoint = thirdPoint.add(directionNormalized.negative().multipliedBy(l));

		target = new ArrayList<>(4);
		target.add(firstPoint);
		target.add(secondPoint);
		target.add(thirdPoint);
		target.add(fourthPoint);
	}

	private void drawTarget(List<Point> target) {
		double lineWidth = 3;
		Color lineColor = Color.RED;
		drawLine(target.get(0), target.get(1), lineWidth, lineColor);
		drawLine(target.get(1), target.get(2), lineWidth, lineColor);
		drawLine(target.get(2), target.get(3), lineWidth, lineColor);
		drawLine(target.get(3), target.get(0), lineWidth, lineColor);

		double width = target.get(0).distanceTo(target.get(3));

		Vector targetDirection = new Vector(target.get(0), target.get(1));
		Point arrowBase = target.get(3)
				.add(targetDirection.perpendicular().normalized().multipliedBy(width * 0.5))
				.add(targetDirection.multipliedBy(0.25));
		Point arrowHead = arrowBase.add(targetDirection.multipliedBy(0.5));
		drawArrow(arrowBase, arrowHead, lineWidth, lineColor);
	}

	private void drawObstacle(IntegerPoint obstacleIndexes) {
//		Color color = Color.DARKBLUE;
//		Vector fromCenterToLeftBottom = new Vector(obstacle.getWidth() / 2.0, obstacle.getHeight() / 2.0).negative();
//		Point leftBottom = obstacle.getCenter().add(fromCenterToLeftBottom);
//		fillRect(leftBottom.getX(), leftBottom.getY(), obstacle.getWidth(), obstacle.getHeight(), color);
		Color color = Color.DARKBLUE;
//		Vector fromCenterToLeftBottom = new Vector(obstacle.getWidth() / 2.0, obstacle.getHeight() / 2.0).negative();
//		Point leftBottom = obstacle.getCenter().add(fromCenterToLeftBottom);
		fillRect(obstacleIndexes.getX() * OBSTACLE_SIZE, obstacleIndexes.getY() * OBSTACLE_SIZE,
				OBSTACLE_SIZE, OBSTACLE_SIZE, color);
	}

	private void drawArrow(Point base, Point arrowHead, double width, Color color) {
		double length = base.distanceTo(arrowHead);

		Vector direction = new Vector(base, arrowHead);

		Vector arrowHeadRightPartShift =
				direction.add(direction.perpendicular())
						.negative()
						.normalized()
						.multipliedBy(length * 0.25);

		Vector arrowHeadLeftPartShift =
				direction.add(direction.perpendicular().negative())
						.negative()
						.normalized()
						.multipliedBy(length * 0.25);

		Point arrowHeadLeft = arrowHead.add(arrowHeadLeftPartShift);
		Point arrowHeadRight = arrowHead.add(arrowHeadRightPartShift);


		drawLine(base, arrowHead, width, color);
		drawLine(arrowHead, arrowHeadLeft, width, color);
		drawLine(arrowHead, arrowHeadRight, width, color);
	}

	private TimerTask createTimerTaskAutonomousDriving(List<Movement> movements) {
		return new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (!movements.isEmpty()) {
							Movement currentMovement = movements.get(0);

							// TODO(vmykh): refactor this shit
							int divider = 3;
							double distance =  (carMovementParameters.getUnitStepDistance() / divider);


							if (currentMovement != null) {
								switch (currentMovement) {
									case FORWARD:
										car = car.withFrontAxisAngle(0.0).movedBy(distance);
										break;
									case FORWARD_LEFT:
										car = car.withFrontAxisAngle(carMovementParameters.getLeftSteeringAngle())
												.movedBy(distance);
										break;
									case FORWARD_RIGHT:
										car = car.withFrontAxisAngle(-carMovementParameters.getRightSteeringAngle())
												.movedBy(distance);
										break;
									case BACKWARD:
										car = car.withFrontAxisAngle(0.0).movedBy(-distance);
										break;
									case BACKWARD_LEFT:
										car = car.withFrontAxisAngle(carMovementParameters.getLeftSteeringAngle())
												.movedBy(-distance);
										break;
									case BACKWARD_RIGHT:
										car = car.withFrontAxisAngle(-carMovementParameters.getRightSteeringAngle())
												.movedBy(-distance);
										break;
								}
							}
						}
						drawScene();
						if (movements.size() > 0) {
							movements.remove(0);
							timer.schedule(createTimerTaskAutonomousDriving(movements), 25L);
						}
					}
				});
			}
		};
	}

	private TimerTask createTimerTask() {
		return new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {

//						// TODO(vmykh): clear this
//						if (cancelOrdinaryTask) {
//							return;
//						}




						if (computingPathNow.get()) {
							if (drawedSubpathes < intermediatePath.size()) {
								List<Point> subpath = intermediatePath.get(drawedSubpathes++);
								for (Point point : subpath) {
									if (drawedSubpathes % 2 == 1) {
										drawCircle(point, 5, Color.DARKGREEN);
									} else {
										fillRect(point.getX(), point.getY(), 7, 7, Color.PURPLE);
									}
								}
							}
						} else {
							if (car == null) {
								gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
							} else {
								Point currentPos = car.getBackAxleCenter();

								if (tracePoints.isEmpty()) {
									tracePoints.add(currentPos);
								} else if (!tracePoints.get(tracePoints.size() - 1).equals(currentPos)) {
									tracePoints.add(currentPos);
								}

								Car newCar;
								if (leftKeyIsPressed) {
									newCar = car.withFrontAxisAngle(carMovementParameters.getLeftSteeringAngle());
								} else if (rightKeyIsPressed) {
									newCar = car.withFrontAxisAngle(-carMovementParameters.getRightSteeringAngle());
								} else {
									newCar = car.withFrontAxisAngle(0.0);
								}

								if (upKeyIsPressed) {
									newCar = newCar.movedBy(carMovementParameters.getUnitStepDistance() * 0.33);
								} else if (downKeyIsPressed) {
									newCar = newCar.movedBy(-carMovementParameters.getUnitStepDistance() * 0.33);
								}

								if (!collisionDetector.collides(newCar)) {
									car = newCar;
								}

								drawScene();
							}
						}
						timer.schedule(createTimerTask(), 25L);
					}
				});
			}
		};
	}

	private void drawScene() {
		gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
		if (target != null) {
			drawTarget(target);
		}

		if (targetTemp != null) {
			drawCircle(targetTemp, 5.0, Color.AZURE);
		}

		for (List<Point> path : thetaStarPaths) {
			if (path.size() >= 2) {
				Point prev = path.get(0);
				for (int i = 1; i < path.size(); i++) {
					Point current = path.get(i);
					drawLine(prev, current, 3, Color.GREENYELLOW);
					prev = current;
				}
			}
			for (Point point : path) {
				drawCircle(point, 5, Color.CORAL);
			}
		}

		drawTraсe(tracePoints);
		drawCar(car);
		drawTotalDistance(tracePoints);

		for (IntegerPoint obstacleIndexes : obstacleset) {
			drawObstacle(obstacleIndexes);
		}
	}

	private void drawTotalDistance(List<Point> tracePoints) {
		gc.setFont(Font.font(30));
		gc.fillText(String.valueOf(tracePoints.size()), 630, 50);
	}

	private void drawTraсe(List<Point> trace) {
		for (Point point : trace) {
			drawCircle(point, 3, Color.DARKGREY);
		}
	}

	private void drawCar(Car car) {
		drawCarChassis(car);
		drawCarBody(car);
	}

	private void drawCarBody(Car car) {
		Point backRightCorner = car.getBackAxleCenter()
				.add(
						car.getOrientationVector()
								.negative()
								.normalizedTo((car.getBodyLength() - car.getChassisLength()) / 2.0)
				)
				.add(
						car.getOrientationVector()
								.perpendicular()
								.negative()
								.normalizedTo(car.getBodyWidth() / 2.0)
				);

		Point frontRightCorner = backRightCorner.add(car.getOrientationVector().normalizedTo(car.getBodyLength()));
		Point frontLeftCorner = frontRightCorner.add(
				car.getOrientationVector().perpendicular().normalizedTo(car.getBodyWidth()));
		Point backLeftCorner = backRightCorner.add(
				car.getOrientationVector().perpendicular().normalizedTo(car.getBodyWidth()));

		drawLine(backRightCorner, frontRightCorner, 3.0, Color.DARKGREEN);
		drawLine(frontRightCorner, frontLeftCorner, 3.0, Color.DARKGREEN);
		drawLine(frontLeftCorner, backLeftCorner, 3.0, Color.DARKGREEN);
		drawLine(backLeftCorner, backRightCorner, 3.0, Color.DARKGREEN);
	}

	private void drawCarChassis(Car car) {
		double x = car.getCenter().getX();
		double y = car.getCenter().getY();
		double l = car.getChassisLength();
		double w = car.getChassisWidth();
		double orientAngle = car.getOrientationAngle();
		double frontAxisAngle = car.getSteeringAngle();

		double xShift = 0.5 * l * cos(orientAngle);
		double yShift = 0.5 * l * sin(orientAngle);

		double frontCenterX = x + xShift;
		double frontCenterY = y + yShift;
		double backCenterX = x - xShift;
		double backCenterY = y - yShift;

		double backAxisXShift = 0.5 * w * sin(orientAngle);
		double backAxisYShift = 0.5 * w * cos(orientAngle);

		double backLeftWheelX = backCenterX - backAxisXShift;
		double backLeftWheelY = backCenterY + backAxisYShift;
		double backRightWheelX = backCenterX + backAxisXShift;
		double backRightWheelY = backCenterY - backAxisYShift;

		double frontAxisXShift = 0.5 * w * sin(orientAngle + frontAxisAngle);
		double frontAxisYShift = 0.5 * w * cos(orientAngle + frontAxisAngle);

		double frontLeftWheelX = frontCenterX - frontAxisXShift;
		double frontLeftWheelY = frontCenterY + frontAxisYShift;
		double frontRightWheelX = frontCenterX + frontAxisXShift;
		double frontRightWheelY = frontCenterY - frontAxisYShift;

		Point frontAxisCenter = new Point(frontCenterX, frontCenterY);
		Point backAxisCenter = new Point(backCenterX, backCenterY);

		Point backLeftWheel = new Point(backLeftWheelX, backLeftWheelY);
		Point backRightWheel = new Point(backRightWheelX, backRightWheelY);
		Point frontLeftWheel = new Point(frontLeftWheelX, frontLeftWheelY);
		Point frontRightWheel = new Point(frontRightWheelX, frontRightWheelY);

		drawLine(frontAxisCenter, backAxisCenter, 3.0);
		drawAxis(backLeftWheel, backRightWheel);
		drawAxis(frontLeftWheel, frontRightWheel);

		drawCircle(frontAxisCenter, w * 0.15);
		drawCircle(backAxisCenter, w * 0.15);
	}

	public void drawCircle(Point center, double radius) {
		gc.fillOval(center.getX() - (0.5 * radius), canvas.getHeight() - (center.getY() + 0.5 * radius), radius, radius);
	}

	public void drawCircle(Point center, double radius, Color color) {
		Paint prevFill = gc.getFill();
		gc.setFill(color);
		drawCircle(center, radius);
		gc.setFill(prevFill);
	}

	public void drawAxis(Point leftWheel, Point rightWheel) {
		drawLine(leftWheel, rightWheel, 3.0);

		double axisLength = leftWheel.distanceTo(rightWheel);

		double axisAngle = new Vector(leftWheel, rightWheel).angle();
		double wheelAngle = axisAngle + 0.5 * PI;

		double wheelXShift = 0.15 * axisLength * cos(wheelAngle);
		double wheelYShift = 0.15 * axisLength * sin(wheelAngle);

		Vector wheelShift = new Vector(wheelXShift, wheelYShift);

		double wheelWidth = leftWheel.distanceTo(rightWheel) * 0.15;

		drawLine(leftWheel.add(wheelShift), leftWheel.subtract(wheelShift), wheelWidth);
		drawLine(rightWheel.add(wheelShift), rightWheel.subtract(wheelShift), wheelWidth);
	}

	private void drawLine(Point p1, Point p2, double width) {
		gc.beginPath();
		gc.moveTo(p1.getX(), canvas.getHeight() - p1.getY());
		gc.lineTo(p2.getX(), canvas.getHeight() - p2.getY());
		gc.setLineWidth(width);
		gc.stroke();
	}

	private void drawLine(Point p1, Point p2, double width, Color color) {
		Paint prevStroke = gc.getStroke();
		gc.setStroke(color);
		drawLine(p1, p2, width);
		gc.setStroke(prevStroke);
	}
	 void fillRect(double x, double y, double width, double height, Color color) {
		Paint prevFill = gc.getFill();
		gc.setFill(color);
		gc.fillRect(x, canvas.getHeight() - y - height, width, height);
		gc.setFill(prevFill);
	}

	private final class IntermediatePathPainter implements PathResolverListener {

		@Override
		public void intermediatePoints(List<Point> points) {
			List<Point> copy = new ArrayList<>(points);
			Platform.runLater(() -> {
						intermediatePath.add(copy);
					}
			);
		}

		@Override
		public void thetaStarPoints(List<Point> points) {
			thetaStarPaths.add(new ArrayList<>(points));
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
