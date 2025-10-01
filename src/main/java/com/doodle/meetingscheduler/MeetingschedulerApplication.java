package com.doodle.meetingscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "com.doodle.meetingscheduler.data")
public class MeetingschedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeetingschedulerApplication.class, args);
	}

}
