
public class PressureWildPointTest implements WildPointTest {

	@Override
	public boolean execute(double value) {
		if(value > 65)
			return true;
		else
			return false;
	}

}
