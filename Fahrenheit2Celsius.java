public class Fahrenheit2Celsius implements ConversionFunction
{
	public double execute(double fahrenheit)
	{
		return (5./9.)*(fahrenheit-32.);
	}
}
