package dev.vmykh.diploma;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
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

	private static final double CAR_WIDTH = 45;
	private static final double CAR_LENGTH = 60;

	private GraphicsContext gc;
	private Canvas canvas;
	private  Car car =
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

							if (leftKeyIsPressed) {
								car.setFrontAxisAngle(PI / 8.0);
							} else if (rightKeyIsPressed) {
								car.setFrontAxisAngle(-PI / 8.0);
							} else {
								car.setFrontAxisAngle(0.0);
							}

							if (upKeyIsPressed) {
								car.moveForward(5);
							} else if (downKeyIsPressed) {
								car.moveForward(-5);
							}

							gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
							if (target != null) {
								drawTarget(target);
							}
							drawTraсe(tracePoints);
							drawCar(car);
						}
						timer.schedule(createTimerTask(), 25L);
					}
				});
			}
		};
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

		Point wheelShift = new Point(wheelXShift, wheelYShift);

		double wheelWidth = leftWheel.distanceTo(rightWheel) * 0.15;

		drawLine(leftWheel.add(wheelShift), leftWheel.subtract(wheelShift), wheelWidth);
		drawLine(rightWheel.add(wheelShift), rightWheel.subtract(wheelShift), wheelWidth);
	}

	private void drawLine(Point p1, Point p2, double width) {
		gc.beginPath();
		gc.moveTo(p1.getX(), canvas.getWidth() - p1.getY());
		gc.lineTo(p2.getX(), canvas.getWidth() - p2.getY());
		gc.setLineWidth(width);
		gc.stroke();
	}

	private void drawLine(Point p1, Point p2, double width, Color color) {
		Paint prevStroke = gc.getStroke();
		gc.setStroke(color);
		drawLine(p1, p2, width);
		gc.setStroke(prevStroke);
	}



	public static void main(String[] args) {
		launch(args);
	}


}
