package dev.vmykh.diploma;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

public final class Field {
	private final int width;
	private final int height;
	private final Set<IntegerPoint> obstacles;

	public Field(int width, int height) {
		this.width = width;
		this.height = height;
		obstacles = new HashSet<>();
	}

	public Field(int width, int height, Set<IntegerPoint> obstacles) {
		this.width = width;
		this.height = height;
		this.obstacles = new HashSet<>(obstacles);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void addObstacle(int x, int y) {
		checkArgument(x >= 0 && x < width);
		checkArgument(y >= 0 && y < height);

		IntegerPoint pos = new IntegerPoint(x, y);
		if (!obstacles.contains(pos)) {
			obstacles.add(pos);
		}
	}

	public boolean isObstacle(int x, int y) {
		IntegerPoint pos = new IntegerPoint(x, y);
		return obstacles.contains(pos);
	}

	public boolean isObstacle(IntegerPoint pos) {
		return obstacles.contains(pos);
	}
}
