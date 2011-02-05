
public class PressureBWildPointTest implements WildPointTest {

	@Override
	public boolean execute(double value) {
		if( (value > 80) || (value < 50) )
			return true;
		else
			return false;
	}

}
