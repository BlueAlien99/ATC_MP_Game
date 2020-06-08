package test.com.atc.client.model;

import com.atc.client.GlobalConsts;
import com.atc.client.model.Airplane;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

class AirplaneTest {

	@Test
	void getCallsign() {
		for(int i = 0; i < 1000; ++i) {
			Airplane a = new Airplane(null, 0, 0, 5000, 180, 200);
			Assertions.assertNotNull(a.getCallsign());
			Assertions.assertTrue(a.getCallsign().length() > 2);
		}
	}

	@Test
	void getRadarsign() {
		for(int i = 0; i < 1000; ++i) {
			Airplane a = new Airplane(null, 0, 0, 5000, 180, 200);
			Assertions.assertNotNull(a.getRadarsign());
			Assertions.assertTrue(a.getRadarsign().length() > 3 && a.getRadarsign().length() < 8);
		}
	}

	@Test
	void setPosXY() {
		for(int i = 0; i < 1000; ++i) {
			double x = new Random().nextDouble() % GlobalConsts.CANVAS_WIDTH*10 - GlobalConsts.CANVAS_WIDTH*5;
			double y = new Random().nextDouble() % GlobalConsts.CANVAS_HEIGHT*10 - GlobalConsts.CANVAS_HEIGHT*5;
			Airplane a = new Airplane(null, 0, 0, 5000, 180, 200);
			a.setPosX(x);
			a.setPosY(y);
			Assertions.assertEquals(x, a.getPosX());
			Assertions.assertEquals(y, a.getPosY());
		}
	}

	@Test
	void getLastPosXY() {
		for(int j = 0; j < 10; ++j) {
			Airplane a = new Airplane(null, 0, 0, 5000, new Random().nextInt(360), 200);
			for (int i = 0; i < 100; ++i) {
				double x = a.getPosX();
				double y = a.getPosY();
				a.moveAirplane();
				Assertions.assertEquals(x, a.getLastPosX());
				Assertions.assertEquals(y, a.getLastPosY());
			}
		}
	}

	@Test
	void targetAltitude() {
		for(int i = 0; i < 1000; ++i) {
			double alt = new Random().nextInt((int) GlobalConsts.AIRPLANE_MAX_ALTITUDE*2);
			Airplane a = new Airplane(null, 0, 0, 5000, 180, 200);
			a.setTargetAltitude(alt);
			double aAlt = a.getTargetAltitude();
			Assertions.assertTrue((alt < GlobalConsts.AIRPLANE_MIN_ALTITUDE && aAlt == GlobalConsts.AIRPLANE_MIN_ALTITUDE) ||
					(alt > GlobalConsts.AIRPLANE_MAX_ALTITUDE && aAlt == GlobalConsts.AIRPLANE_MAX_ALTITUDE) ||
					(alt >= GlobalConsts.AIRPLANE_MIN_ALTITUDE && alt <= GlobalConsts.AIRPLANE_MAX_ALTITUDE && aAlt == alt));
		}
	}

	@Test
	void targetHeading() {
		for(int i = 0; i < 1000; ++i) {
			double heading = new Random().nextInt(720) - 360;
			Airplane a = new Airplane(null, 0, 0, 5000, 180, 200);
			a.setTargetHeading(heading);
			double aHeading = a.getTargetHeading();
			Assertions.assertTrue((heading % 360 == 0 && aHeading == 360) || ((heading + 720) % 360 == aHeading));
		}
	}

	@Test
	void targetSpeed() {
		for(int i = 0; i < 1000; ++i) {
			double speed = new Random().nextInt((int) GlobalConsts.DEFAULT_MAX_SPEED*2);
			Airplane a = new Airplane(null, 0, 0, 5000, 180, 200);
			a.setTargetSpeed(speed);
			double aSpeed = a.getTargetSpeed();
			Assertions.assertTrue((speed < GlobalConsts.DEFAULT_MIN_SPEED && aSpeed == GlobalConsts.DEFAULT_MIN_SPEED) ||
					(speed > GlobalConsts.DEFAULT_MAX_SPEED && aSpeed == GlobalConsts.DEFAULT_MAX_SPEED) ||
					(speed >= GlobalConsts.DEFAULT_MIN_SPEED && speed <= GlobalConsts.DEFAULT_MAX_SPEED && aSpeed == speed));
		}
	}
}