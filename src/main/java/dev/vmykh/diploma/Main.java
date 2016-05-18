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

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.*;

public class Main extends Application {
	private static final int CANVAS_WIDTH = 700;
	private static final int CANVAS_HEIGHT = 700;

	private static final double CAR_WIDTH = 30;
	private static final double CAR_LENGTH = 45;

	private GraphicsContext gc;
	private Canvas canvas;
	private volatile Car car =
			new Car(CAR_WIDTH, CAR_LENGTH)
			.setInitialPosition(250, 250)
			.setInitialOrientation(PI / 2);

	private List<Point> tracePoints = new ArrayList<>();

	private Timer timer = new Timer();

	private volatile boolean upKeyIsPressed = false;
	private volatile boolean downKeyIsPressed = false;
	private volatile boolean rightKeyIsPressed = false;
	private volatile boolean leftKeyIsPressed = false;

	private Point firstTargetPoint;
	private Point secondTargetPoint;

	private Point firstCarPoint;
	private Point secondCarPoint;

	private volatile List<Point> target;

	private List<Obstacle> obstacles = new ArrayList();

	private static final int POINTS_PER_CAR_SIDE = 8;

	// TODO(vmykh): remove this bullshit
	{
		obstacles.add(new Obstacle(new Point(200, 350), 50, 50));
	}

	@Override
	public void start(Stage stage) {

		initUI(stage);
	}

	private void initUI(Stage stage) {

		Pane root = new Pane();

		canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
		gc = canvas.getGraphicsContext2D();

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
					obstacles.clear();
					firstCarPoint = null;
					secondCarPoint = null;
					firstTargetPoint = null;
					secondTargetPoint = null;
				}
			}
		};

		canvas.addEventHandler(MouseEvent.MOUSE_CLICKED,
				new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						if (e.getButton() == MouseButton.PRIMARY) {
							if (car == null) {
								if (firstCarPoint == null) {
									firstCarPoint = new Point(e.getX(), canvas.getHeight() - e.getY());
								} else if (secondCarPoint == null) {
									secondCarPoint = new Point(e.getX(), canvas.getHeight() - e.getY());
									car = createCar(firstCarPoint, new Vector(firstCarPoint, secondCarPoint));
								}
							}
							else if (firstTargetPoint == null) {
								firstTargetPoint = new Point(e.getX(), canvas.getHeight() - e.getY());
							} else if (secondTargetPoint == null) {
								secondTargetPoint = new Point(e.getX(), canvas.getHeight() - e.getY());
								Vector direction = new Vector(firstTargetPoint, secondTargetPoint);
								createTarget(car, firstTargetPoint, direction);
							}
						} else if (e.getButton() == MouseButton.SECONDARY) {
							obstacles.add(new Obstacle(new Point(e.getX(), canvas.getHeight() - e.getY()), 50, 50));
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

	private static Car createCar(Point backCenterPoint, Vector direction) {
		return new Car(CAR_WIDTH, CAR_LENGTH)
				.setInitialPosition(backCenterPoint.add(direction.normalized().multipliedBy(CAR_LENGTH * 0.5)))
				.setInitialOrientation(direction.getAngle());
	}

	private void createTarget(Car car, Point backCenterPosition, Vector direction) {
		double w = car.getWidth();
		double l = car.getLength();

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

	private void drawObstacle(Obstacle obstacle) {
		Color color = Color.DARKBLUE;
		Vector fromCenterToLeftBottom = new Vector(obstacle.getWidth() / 2.0, obstacle.getHeight() / 2.0).negative();
		Point leftBottom = obstacle.getCenter().add(fromCenterToLeftBottom);
		fillRect(leftBottom.getX(), leftBottom.getY(), obstacle.getWidth(), obstacle.getHeight(), color);
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

	private TimerTask createTimerTask() {
		return new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (car == null) {
							gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
						} else {
							Point currentPos = new Point(car.getX(), car.getY());

							if (tracePoints.isEmpty()) {
								tracePoints.add(currentPos);
							} else if (!tracePoints.get(tracePoints.size() - 1).equals(currentPos)) {
								tracePoints.add(currentPos);
							}

							Car newCar;
							if (leftKeyIsPressed) {
								newCar = car.withFrontAxisAngle(PI / 8.0);
							} else if (rightKeyIsPressed) {
								newCar = car.withFrontAxisAngle(-PI / 8.0);
							} else {
								newCar = car.withFrontAxisAngle(0.0);
							}

							if (upKeyIsPressed) {
								newCar = newCar.movedBy(5);
							} else if (downKeyIsPressed) {
								newCar = newCar.movedBy(-5);
							}

							if (!carOverlapsWithObstacles(newCar)) {
								car = newCar;
							}

							gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
							if (target != null) {
								drawTarget(target);
							}
							drawTraсe(tracePoints);
							drawCar(car);
							drawTotalDistance(tracePoints);

							for (Obstacle obstacle : obstacles) {
								drawObstacle(obstacle);
							}
						}
						timer.schedule(createTimerTask(), 25L);
					}
				});
			}
		};
	}

	private boolean carOverlapsWithObstacles(Car car) {
		for (Obstacle obstacle : obstacles) {
			List<Point> carSidePoints = generateCarSidePoints(car);
			for (Point carSidePoint : carSidePoints) {
				if (obstacle.contains(carSidePoint)) {
					return true;
				}
			}
		}
		return false;
	}

	private List<Point> generateCarSidePoints(Car car) {
		Vector fromRearToFront = new Vector(car.getBackAxisCenter(), car.getFrontAxisCenter());
		Vector fromRightWheelToLeftWheel = fromRearToFront.perpendicular().normalized().multipliedBy(car.getWidth());

		Point rearLeft = car.getBackAxisCenter().add(fromRightWheelToLeftWheel.multipliedBy(0.5));
		Point frontLeft = rearLeft.add(fromRearToFront);
		Point frontRight = frontLeft.add(fromRightWheelToLeftWheel.negative());
		Point rearRight = rearLeft.add(fromRightWheelToLeftWheel.negative());

		List<Point> carSidePoints = new ArrayList<>(POINTS_PER_CAR_SIDE * 4);
		carSidePoints.addAll(pointsBetween(rearLeft, frontLeft, POINTS_PER_CAR_SIDE));
		carSidePoints.addAll(pointsBetween(frontLeft, frontRight, POINTS_PER_CAR_SIDE));
		carSidePoints.addAll(pointsBetween(rearLeft, rearRight, POINTS_PER_CAR_SIDE));
		carSidePoints.addAll(pointsBetween(rearRight, frontRight, POINTS_PER_CAR_SIDE));

		return carSidePoints;
	}

	private List<Point> pointsBetween(Point p1, Point p2, int amount) {
		double xStep = (p2.getX() - p1.getX()) / (double) amount;
		double yStep = (p2.getY() - p1.getY()) / (double) amount;
		List<Point> points = new ArrayList<>(amount);
		for (int i = 0; i < amount; i++) {
			Point current = new Point(p1.getX() + xStep * i, p1.getY() + yStep * i);
			points.add(current);
		}
		return points;
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
		double x = car.getX();
		double y = car.getY();
		double l = car.getLength();
		double w = car.getWidth();
		double orientAngle = car.getOrientationAngle();
		double frontAxisAngle = car.getFrontAxisAngle();

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

		double axisAngle = new Vector(leftWheel, rightWheel).getAngle();
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



	public static void main(String[] args) {
		launch(args);
	}


}
