package a;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Test {

	public static void main(String[] args) throws IOException {
		ZonedDateTime currTime = ZonedDateTime.now();
		DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("E d MMM yyyy HH:mm:ss z");
		
		String formattedTime = currTime.format(timeFormat);
		System.out.println(formattedTime);
		
	}

}
