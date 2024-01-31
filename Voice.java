package com.mycompany.voice;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.CallFetcher;
import com.twilio.type.PhoneNumber;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Voice {
    public static final String ACCOUNT_SID = "AC176bcd77ba2204797111fb4a79011599";
    public static final String AUTH_TOKEN = "8cb9e5de84bb6ddd97f1018cf0f61a3f";

    // Database credentials
    public static final String DB_URL = "jdbc:postgresql://localhost:5432/calllogs";
    public static final String USER = "postgres";
    public static final String PASS = "root";

    public static void main(String[] args) throws URISyntaxException {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Call call = Call.creator(
                new PhoneNumber("+15512240006"),
                new PhoneNumber("+201152491439"),
                URI.create("https://raw.githubusercontent.com/SarASaEedm/telecom/main/callxml.xml"))
                .create();

        System.out.println("Call SID: " + call.getSid());
        printCallStatus(call.getSid());
        saveCallToDatabase(call.getSid(), "+15512240006", "+201152491439");
    }

    private static void printCallStatus(String callSid) {
        CallFetcher callFetcher = Call.fetcher(callSid);
        Call fetchedCall = callFetcher.fetch();

        System.out.println("Call Status: " + fetchedCall.getStatus());
    }

    private static void saveCallToDatabase(String callSid, String fromNumber, String toNumber) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "INSERT INTO call_logs (call_sid, from_number, to_number, timestamp) VALUES (?, ?, ?, now())";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, callSid);
                pstmt.setString(2, fromNumber);
                pstmt.setString(3, toNumber);
                pstmt.executeUpdate();
                System.out.println("Call log saved to the database.");
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
