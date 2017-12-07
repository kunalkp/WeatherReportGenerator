import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Kunal Pardeshi
 *
 */
public class HtmlParser {

	private ArrayList<String> region = new ArrayList<>(Arrays.asList("UK ", "England ", "Wales ", "Scotland "));
	private ArrayList<String> weatherAttributes = new ArrayList<>(
			Arrays.asList("Date Tmax", "Date Tmin", "Date Tmean", "Date Sunshine", "Date Rainfall"));
	private ArrayList<File> arrayListFiles = new ArrayList<>();

	private File weatherCsv = new File("WeatherData.csv");
	private FileWriter writer = null;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;

	
	/* To get the link, download the file*/
	public void downloadURL() throws IOException {
		for (int i = 0; i < region.size(); i++) {
			for (int j = 0; j < weatherAttributes.size(); j++) {
				String title = region.get(i) + weatherAttributes.get(j);
				String fileLinks = getLinks(title);
				downloadFile(fileLinks, title);
			}
		}
		parseFiletoCSV();
	}

	/* It returns link using Jsoup to parse the html */
	public String getLinks(String title) {
		Document doc = null;
		String fileLink = null;
		String url = "https://www.metoffice.gov.uk/climate/uk/summaries/datasets#yearOrdered";

		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (Element table : doc.select("table.table")) {
			Elements hrefs = table.select("a[href][title]");
			for (Element el : hrefs) {
				if (el.attr("title").equals(title)) {
					fileLink = el.attr("href");
					System.out.println(title);
					System.out.println(fileLink);
				}
			}
		}
		return fileLink;
	}

	/* To Download the file from the given URL and create a arraylist of all files*/
	public void downloadFile(String url, String title) throws IOException {
		File fileDirectory = new File("downloaded files");
		fileDirectory.mkdirs();

		try {
			URL oldUrl = new URL(url);
			URL newUrl = new URL("https", oldUrl.getHost(), oldUrl.getPort(), oldUrl.getFile());
			String line = null;

			URLConnection con = newUrl.openConnection();
			InputStream inputStream = con.getInputStream();
			InputStreamReader streamReader = new InputStreamReader(inputStream);
			bufferedReader = new BufferedReader(streamReader);
			writer = new FileWriter(fileDirectory + "/" + title);

			// To skip first 8 lines of description
			for (int i = 1; i <= 8; i++) {
				bufferedReader.readLine();
			}

			while ((line = bufferedReader.readLine()) != null) {
				writer.write(line);
				writer.write("\n");
			}
			inputStream.close();
			writer.close();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		File downloadedfile = new File(fileDirectory + "/" + title);
		arrayListFiles.add(downloadedfile);
	}

	/* To parse the file to csv with the given format */
	public void parseFiletoCSV() throws IOException {
		ArrayList<String> months = new ArrayList<>(Arrays.asList("Jan", "Feb", "March", "April", "May", "June", "July",
				"August", "September", "October", "November", "December"));
		FileReader fileReader;
		String line;
		DataObject dataObject = new DataObject();

		if (!weatherCsv.exists()) {
			weatherCsv.createNewFile();
		}
		FileWriter csvWriter = new FileWriter(weatherCsv);
		bufferedWriter = new BufferedWriter(csvWriter);
		bufferedWriter.write("region_code, weather_param, year, key, value");
		bufferedWriter.newLine();

		for (File weatherFile : arrayListFiles) {
			ArrayList<String> tempValues = new ArrayList<>();
			try {
				fileReader = new FileReader(weatherFile);
				bufferedReader = new BufferedReader(fileReader);

				String fileName[] = weatherFile.getName().split(" ");
				dataObject.setRegion(fileName[0]); // Get region @param using file title
				dataObject.setWeather_param(fileName[2]); //Get weather @param using file title

				while ((line = bufferedReader.readLine()) != null) {
					line = line.replaceAll("\\s{5,}"," N/A ").trim();
					line = line.replaceAll("\\s+",",");
					
					String[] values = line.split(",");
					dataObject.setYear(values[0]);

					for (int i = 1; i < values.length; i++) {
						if (!values[i].equalsIgnoreCase(",")) {
							tempValues.add(values[i]);
						}
					}

					for (int month = 0; month < months.size(); month++) {
						bufferedWriter.write(dataObject.getRegion());
						bufferedWriter.write(',');
						bufferedWriter.write(dataObject.getWeather_param());
						bufferedWriter.write(',');
						bufferedWriter.write(dataObject.getYear());
						bufferedWriter.write(',');
						bufferedWriter.write(months.get(month));
						bufferedWriter.write(',');
						bufferedWriter.write(tempValues.get(month));
						bufferedWriter.newLine();
					}
					tempValues.clear();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		bufferedWriter.flush();
	}
}
