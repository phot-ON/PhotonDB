package com.photon.db;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@RestController
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class})
public class PhotonDB {
	static MongoDB db;

	String getSHA256(String PlainText) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] SessionIDarr = md.digest(PlainText.getBytes(StandardCharsets.UTF_8));
			BigInteger number = new BigInteger(1, SessionIDarr);
			StringBuilder hexString = new StringBuilder(number.toString(16));
			while (hexString.length() < 64) {
				hexString.insert(0, '0');
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			System.err.print("Alternate universe detected");
			return "Alternate universe detected";
		}
	}

	String createSessionID(String Username) {
		Date now = new Date();
		String Timestamp = String.valueOf(now.getTime());
		String SessionID = getSHA256(Timestamp + Username);
		return SessionID;
	}


	@PostMapping("/UploadImage")
	boolean uploadImage(String Image, String SessionID) {
		System.out.println("uploading image: " + Image + " " + SessionID);
		return db.uploadImage(Image, SessionID);
	}

	@GetMapping("/GetSessionImages")
	String[] getSessionImages(String SessionID) {
		System.out.println("getting session images: " + SessionID);
		return db.getSessionImages(SessionID);
	}

	@PostMapping("/CreateUser")
	boolean createUser(String Username) {
		System.out.println("Creating user: " + Username);
		return db.createUser(Username);
	}

	@PostMapping("/SetUserFCM")
	boolean setUserFCM(String Username, String FCM) {
		System.out.println("setting user FCM: " + Username + " " + FCM);
		return db.setUserFCM(Username, FCM);
	}

	@GetMapping("/GetUserFCM")
	String getUserFCM(String Username) {
		System.out.println("getting user FCM: " + Username);
		return db.getUserFCM(Username);
	}

	@PostMapping("/AddFriend")
	boolean addFriend(String Username1, String Username2) {
		System.out.println("adding friend: " + Username1 + " " + Username2);
		return db.addFriend(Username1, Username2);
	}

	@GetMapping("/GetFriends")
	String[] getFriends(String Username) {
		System.out.println("getting friends: " + Username);
		return db.getFriends(Username);
	}

	@PostMapping("/CreateSession")
	String createSession(String Username) {
		String SessionID = createSessionID(Username);
		System.out.println("creating session: " + Username + " " + SessionID);
		if (db.createSession(Username, SessionID)) {
			return SessionID;
		} else {
			return "";
		}
	}

	@PostMapping("/JoinSession")
	boolean joinSession(String Username, String SessionID) {
		System.out.println("joining session: " + Username + " " + SessionID);
		return db.joinSession(Username, SessionID);
	}

	@GetMapping("/GetSessionUsers")
	String[] getSessionUsers(String SessionID) {
		System.out.println("getting session users: " + SessionID);
		return db.getSessionUsers(SessionID);
	}

	@DeleteMapping("/LeaveSession")
	boolean leaveSession(String Username) {
		System.out.println("leaving session: " + Username);
		return db.leaveSession(Username);
	}

	public static void main(String[] args) {
		db = new MongoDB();
		SpringApplication.run(PhotonDB.class, args);

	}

}