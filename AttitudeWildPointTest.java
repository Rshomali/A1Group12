package hw1;


public class AttitudeWildPointTest implements WildPointTest {

	@Override
	public boolean execute(double value) {
		if(value > 10)
			return true;
		else
			return false;
	}

}
