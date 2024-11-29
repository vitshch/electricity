package dev.vitalish.electricity.service.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import dev.vitalish.electricity.service.GoogleCalendarService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import static com.google.api.services.calendar.CalendarScopes.CALENDAR_EVENTS;

@Service
public class GoogleCalendarApIService {

    private static final String APPLICATION_NAME = "electricity-schedule";
    private static final String CREDENTIALS_FILE_PATH = "google/credentials.json";
    private static final List<String> SCOPES = Collections.singletonList(CALENDAR_EVENTS);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String ACCESS_TYPE = "offline";

    private final JsonFactory jsonGoogleFactory;

    public GoogleCalendarApIService(JsonFactory jsonGoogleFactory) {
        this.jsonGoogleFactory = jsonGoogleFactory;
    }

    public Calendar getCalendarService() {
        try {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            return new Calendar.Builder(httpTransport, jsonGoogleFactory, getCredentials(httpTransport))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Credential getCredentials(NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = GoogleCalendarService.class.getClassLoader().getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonGoogleFactory, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, jsonGoogleFactory, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType(ACCESS_TYPE)
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

}
