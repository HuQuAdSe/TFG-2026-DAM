package org.example.voluntariadomadrid;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;

public class FirebaseConfig {
    public static void init() throws Exception {
        FileInputStream serviceAccount =
                new FileInputStream("src/main/java/org/example/clvPrvd/voluntariado-madrid-firebase-adminsdk-fbsvc-e984f661ab.json");
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
    }
}