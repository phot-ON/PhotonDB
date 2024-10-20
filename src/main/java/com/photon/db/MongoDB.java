package com.photon.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class MongoDB {
	MongoClient mongoClient;
	MongoDatabase database;
	MongoCollection<Document> Users;
	MongoCollection<Document> Sessions;
	MongoCollection<Document> Photos;

	public MongoDB() {
		Scanner sc;
		try {
			sc = new Scanner(new File("./secret/MongoDB.secret"));
			String mongoDBSecret = "mongodb+srv://satindrar:" + sc.next()
					+ "@cluster0.z2ngp.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
			sc.close();
			mongoClient = MongoClients.create(mongoDBSecret);
			database = mongoClient.getDatabase("Photon");
			Users = database.getCollection("Users");
			Sessions = database.getCollection("Sessions");
			Photos = database.getCollection("Photos");

		} catch (FileNotFoundException e) {
			System.err.print("MongoDB.secret either does not exit or is not placed in ./secret");
			System.exit(1);
		}
	}

	public boolean createUser(String Username) {
		if (Username != null && Users.countDocuments(Filters.eq("Username", Username)) == 0) {
			Users.insertOne(new Document("Username", Username).append("FCM", "").append("SessionID", "").append("Friends", new ArrayList<String>()));
			System.out.println("User Created: " + Username);
			return true;
		} else {
			System.out.println("User Already Exists or Username is null: " + Username);
			return false;
		}
	}

	public String getUserFCM(String Username) {
		Document User = Users.find(Filters.eq("Username", Username)).first();
		if (User != null) {
			System.out.println("User FCM gotten: " + Username + " - " + User.get("FCM").toString());
			return User.get("FCM").toString();
		} else {
			System.out.println("User FCM not gotten(User not found): " + Username);
			return "";
		}
	}

	public boolean setUserFCM(String Username, String FCM) {
		Document User = Users.find(Filters.eq("Username", Username)).first();
		if (User != null) {
			Users.deleteOne(Filters.eq("Username", Username));
			Users.insertOne(new Document("Username", Username).append("FCM", FCM).append("SessionID", User.get("SessionID")).append("Friends", User.get("Friends")));
			System.out.println("User FCM set: " + Username + " - " + FCM);
			return true;
		} else {
			System.out.println("User FCM not set(User not found): " + Username);
			return false;
		}
	}

	public boolean addFriend(String Username1, String Username2) {
		Document User1 = Users.find(Filters.eq("Username", Username1)).first();
		Document User2 = Users.find(Filters.eq("Username", Username2)).first();
		if (User1 != null && User2 != null) {
			ArrayList<String> friends1 = (ArrayList<String>) User1.get("Friends");
			ArrayList<String> friends2 = (ArrayList<String>) User2.get("Friends");
			if (!friends1.contains(Username2) && !friends2.contains(Username1)) {
				friends1.add(Username2);
				friends2.add(Username1);
				Users.deleteOne(Filters.eq("Username", Username1));
				Users.deleteOne(Filters.eq("Username", Username2));
				Users.insertOne(new Document("Username", Username1).append("FCM", User1.get("FCM")).append("Friends", friends1).append("SessionID", User1.get("SessionID")));
				Users.insertOne(new Document("Username", Username2).append("FCM", User2.get("FCM")).append("Friends", friends2).append("SessionID", User2.get("SessionID")));
				System.out.println("Friend Added: " + Username1 + " and " + Username2);
				return true;
			} else {
				System.out.println("Friend Already Exists: " + Username1 + " and " + Username2);
				return false;
			}
		} else {
			System.out.println("User Not Found: " + Username1 + " or " + Username2);
			return false;
		}

	}

	public String[] getFriends(String Username) {
		Document User = Users.find(Filters.eq("Username", Username)).first();
		if (User != null) {
			String[] friends = new String[((ArrayList<String>) User.get("Friends")).size()];
			friends = ((ArrayList<String>) User.get("Friends")).toArray(friends);
			for (int i = 0; i < friends.length; i++) {
				System.out.println("Friend: " + friends[i]);
			}
			return friends;
		} else {
			System.out.println("User Not Found: " + Username);
			return null;
		}

	}

	public boolean createSession(String Username, String SessionID) {
		Document User = Users.find(Filters.eq("Username", Username)).first();
		if (User != null) {
			Users.deleteOne(Filters.eq("Username", Username));
			Users.insertOne(new Document("Username", Username).append("FCM", User.get("FCM")).append("SessionID", SessionID).append("Friends", User.get("Friends")));
			Sessions.insertOne(new Document("SessionID", SessionID).append("Images", new ArrayList<String>()));
			System.out.println("Session Created: " + SessionID);
			return true;
		} else {
			System.out.println("User non existent: " + Username + " - " + SessionID);
			return false;
		}

	}

	public boolean joinSession(String Username, String SessionID) {
		Document User = Users.find(Filters.eq("Username", Username)).first();
		Document Session = Sessions.find(Filters.eq("SessionID", SessionID)).first();

		if (User != null && Session != null) {
			if(User.get("SessionID")!="" && Users.countDocuments(Filters.eq("SessionID", User.get("SessionID"))) == 1){
				Sessions.deleteOne(Filters.eq("SessionID", User.get("SessionID")));
		}
			Users.deleteOne(Filters.eq("Username", Username));
			Users.insertOne(new Document("Username", Username).append("FCM", User.get("FCM")).append("SessionID", SessionID).append("Friends", User.get("Friends")));
			System.out.println("Session Joined: " + SessionID);
			return true;
		} else {
			System.out.println("Session Not Found or Already Joined or User Not Found: " + Username + " - " + SessionID);
			return false;
		}

	}

	public boolean uploadImage(String Image, String SessionID) {
		Document Session = Sessions.find(Filters.eq("SessionID", SessionID)).first();
		if (Session != null && !((ArrayList<String>) Session.get("Images")).contains(Image)) {
			ArrayList<String> Images = (ArrayList<String>) Session.get("Images");
			Sessions.deleteOne(Filters.eq("SessionID", SessionID));
			Images.add(Image);
			Sessions.insertOne(new Document("SessionID", SessionID).append("Images", Images));
			System.out.println("Image Uploaded: " + Image + " - " + SessionID);
			return true;
		} else {
			System.out.println("Session Not Found or Image Already Exists: " + " - " + SessionID);
			return false;
		}

	}

	public String[] getSessionImages(String SessionID) {
		Document Session = Sessions.find(Filters.eq("SessionID", SessionID)).first();
		if(Session != null) {
			String[] SessionsImages = ((ArrayList<String>) Session.get("Images")).toArray(new String[((ArrayList<String>) Session.get("Images")).size()]);
			for (int i = 0; i < SessionsImages.length; i++) {
				System.out.println("Session Image: " + SessionsImages[i]);
			}
			return SessionsImages;
		}else{
			System.out.println("Session Not Found: " + SessionID);
			return null;
		}
	}

	public String[] getSessionUsers(String SessionID) {
		Iterable<Document> UserIterable = Users.find(Filters.eq("SessionID", SessionID));
		int stringCount = 0;
		for (Document User : UserIterable) {
			stringCount++;
		}
		String[] SessionsUsers = new String[stringCount];
		int i = 0;
		for (Document User : UserIterable) {
			SessionsUsers[i] = User.get("Username").toString();
			i++;
		}
		for (int j = 0; j < stringCount; j++) {
			System.out.println("Session User: " + SessionsUsers[j]);
		}
		return SessionsUsers;
	}

	public boolean leaveSession(String Username) {
		Document User = Users.find(Filters.eq("Username", Username)).first();
		if (User != null && !User.get("SessionID").equals("")) {
		if (Users.countDocuments(Filters.eq("SessionID", User.get("SessionID"))) == 1) {
				Sessions.deleteOne(Filters.eq("SessionID", User.get("SessionID")));
			}
			Users.deleteOne(Filters.eq("Username", Username));
			Users.insertOne(new Document("Username", Username).append("FCM", User.get("FCM")).append("SessionID", "").append("Friends", User.get("Friends")));
			System.out.println("Session Left: " + Username);
			return true;
		} else {
			System.out.println("Session Not Found or User Not Found: " + Username);
			return false;
		}
	}

	public boolean clearDB(String confirm) {
		if (confirm.equals("photon")) {
			Users.deleteMany(new Document());
			Sessions.deleteMany(new Document());
			return true;
		}else{
			return false;
		}

	}
}
