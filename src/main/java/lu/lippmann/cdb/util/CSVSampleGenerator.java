/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Sample generator of csv.
 * Change it as you need
 * 
 * @author Jérôme Wax, Yoann Didry
 */
public final class CSVSampleGenerator {
	
	/**
	 * Private constructor.
	 */
	private CSVSampleGenerator() {}
	
	/**
	 * generate a random sample files.
	 *
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		final BufferedWriter out = new BufferedWriter(new FileWriter(
				"sample.csv"));
		out.write("age,wind,temperature, country, playable");

		int age;
		int wind;
		int temperature;
		String country = "";
		
		final Random r = new Random();

		for (int i = 0; i < 10000; i++) {
			
			if (r.nextBoolean()) {
				country ="France";
			} else {
				country ="USA";
			}
			
			age = r.nextInt(80);
			wind = r.nextInt(70);
			temperature = r.nextInt(70) - 20;
			final String playable;
			/*if (r.nextBoolean()) {
				//in half case return random :)
				playable = r.nextBoolean() ? "OK" : "NOK";
			} else {*/
			playable = isPlayable(age, wind, temperature, country) ? "OK" : "NOK";
			//}
			out.write("\n" + age + "," + wind + "," + temperature + ","
					+ country + ","
					+ playable);
		}

		out.close();
	}


	/**
	 * Checks if is playable.
	 *
	 * @param age the age
	 * @param wind the wind
	 * @param temperature the temperature
	 * @return true, if is playable
	 */
	private static boolean isPlayable(int age, int wind, int temperature, String country) {
		
		if (country.equals("USA")&&(age<23)) {
			return damnTooComplexIsPlayable(age,wind, temperature, country);
		}
		
		
		if (age < 8) {
			return false;
		}

		if (age < 12 && wind > 40) {
			return false;
		}

		if (wind > 75) {
			return false;
		}

		if (temperature < 10 || temperature > 30) {
			return false;
		}
		return true;
	}

	private static boolean damnTooComplexIsPlayable(int age, int wind,
			int temperature, String country) {
		if (age+wind> 60) {
			return false;
		}
		if (wind - temperature > 5) {
			return true;
		}
		
		if (age + temperature > 70) {
			return true;
		}
		
		return false;
	}
}
