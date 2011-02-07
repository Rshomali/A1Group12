

public class PressureCWildPointTest implements WildPointTest {

	@Override
	public boolean execute(double value) {
		if( (value > 90) || (value < 45) )
			return true;
		else
			return false;
	}

}
