public class Converter extends FilterFramework
{
	protected HashMap<Integer, ConversionFunction> conversionFunction;

	public void processIDAndValue(Integer ID, long value)
	{
		long result = conversionFunction.get(ID).execute(value);
		writeValue(ID, result);
	}
}
