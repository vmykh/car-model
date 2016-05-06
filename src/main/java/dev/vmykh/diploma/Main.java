package dev.vmykh.diploma;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Main extends Application {
	private static final int CANVAS_WIDTH = 700;
	private static final int CANVAS_HEIGHT = 700;

	private GraphicsContext gc;
	private Canvas canvas;

	@Override
	public void start(Stage stage) {

		initUI(stage);
	}

	private void initUI(Stage stage) {

		Pane root = new Pane();

		canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
		gc = canvas.getGraphicsContext2D();

		Car car = new Car(80, 100).setInitialPosition(250, 250).setInitialOrientation(PI / 2).setInitialFrontAxisAngle(-0.3);

		drawCar(car);

		car.moveForward(150);

		drawCar(car);

		car.moveForward(150);

		drawCar(car);



		root.getChildren().add(canvas);

		Scene scene = new Scene(root, CANVAS_WIDTH, CANVAS_HEIGHT, Color.WHITESMOKE);

		stage.setTitle("Lines");
		stage.setScene(scene);
		stage.show();
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

	public void drawAxis(Point leftWheel, Point rightWheel) {
		drawLine(leftWheel, rightWheel, 3.0);

		double axisLength = leftWheel.distanceTo(rightWheel);

		double axisAngle = new Vector(leftWheel, rightWheel).getAngle();
		double wheelAngle = axisAngle + 0.5 * PI;

		double wheelXShift = 0.15 * axisLength * cos(wheelAngle);
		double wheelYShift = 0.15 * axisLength * sin(wheelAngle);

		Point wheelShift = new Point(wheelXShift, wheelYShift);

		drawLine(leftWheel.add(wheelShift), leftWheel.subtract(wheelShift), 10.0);
		drawLine(rightWheel.add(wheelShift), rightWheel.subtract(wheelShift), 10.0);
	}

	private void drawLine(Point p1, Point p2, double width) {
		gc.beginPath();
		gc.moveTo(p1.getX(), canvas.getWidth() - p1.getY());
		gc.lineTo(p2.getX(), canvas.getWidth() - p2.getY());
		gc.setLineWidth(width);
		gc.stroke();
	}



	public static void main(String[] args) {
		launch(args);
	}
}
