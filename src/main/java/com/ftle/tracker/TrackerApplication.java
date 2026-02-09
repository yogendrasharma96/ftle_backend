package com.ftle.tracker;

import com.google.firebase.auth.FirebaseAuth;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class TrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrackerApplication.class, args);
	}

//	public class SetAdminClaim implements CommandLineRunner {
//
//		@Override
//		public void run(String... args) throws Exception {
//
//			String adminUid = "FIREBASE_UID_OF_ADMIN";
//
//			Map<String, Object> claims = new HashMap<>();
//			claims.put("role", "ADMIN");
//
//			FirebaseAuth.getInstance().setCustomUserClaims(adminUid, claims);
//
//			System.out.println("ADMIN role assigned");
//		}
//	}

}
