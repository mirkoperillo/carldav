package carldav;

import java.util.Locale;
import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@ImportResource("classpath:applicationContext-cosmo.xml")
@SpringBootApplication
public class CarldavApplication {

	static {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // required by hsqldb
		System.setProperty("file.encoding", "UTF-8");
		Locale.setDefault(Locale.US);
	}

	public static void main(String[] args) {
		SpringApplication.run(CarldavApplication.class, args);
	}
}
