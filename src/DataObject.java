
public class DataObject {
	String region;
	String weather_param;
	String year;
	
	public DataObject() {
		super();
	}

	public DataObject(String region, String weather_param, String year) {
		super();
		this.region = region;
		this.weather_param = weather_param;
		this.year = year;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getWeather_param() {
		return weather_param;
	}

	public void setWeather_param(String weather_param) {
		this.weather_param = weather_param;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}
}
