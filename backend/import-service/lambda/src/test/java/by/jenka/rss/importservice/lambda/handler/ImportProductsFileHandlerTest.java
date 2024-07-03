package by.jenka.rss.importservice.lambda.handler;

import by.jenka.rss.importservice.lambda.service.S3PreSignedRequestService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SystemStubsExtension.class)
class ImportProductsFileHandlerTest {

    private static final String TEST_FILE_NAME = "test-file.csv";
    private static final String IMPORT_BUCKET = "test-import-bucket";
    private static final String UPLOADED = "test-uploaded";
    private static final String PARSED = "test-parsed";
    private S3PreSignedRequestService preSignedRequestService;

    @SystemStub
    private EnvironmentVariables variables =
            new EnvironmentVariables(
                    Map.of("FOLDER_FOR_UPLOAD", UPLOADED,
                            "FOLDER_FOR_PARSED", PARSED,
                            "IMPORT_BUCKET_NAME", IMPORT_BUCKET));
    private Context context;
    private ImportProductsFileHandler underTest;

    @BeforeEach
    void setUp() {
        preSignedRequestService = mock(S3PreSignedRequestService.class);
        context = mock(Context.class);
        when(context.getLogger()).thenReturn(mock(LambdaLogger.class));
        underTest = new ImportProductsFileHandler();
        underTest.setPreSignedRequestService(preSignedRequestService);
    }

    @Test
    void handleRequest_Should_Return400_When_NoNameQueryParamIsPassed() {
        var input = mock(APIGatewayProxyRequestEvent.class);
        when(input.getQueryStringParameters()).thenReturn(Map.of());

        var actual = underTest.handleRequest(input, context);

        assertNotNull(actual);
        assertEquals(400, actual.getStatusCode());
        assertEquals("{\"message\":\"name query parameter must present\"}", actual.getBody());
    }

    @SneakyThrows
    @Test
    void handleRequest_Should_Return200_When_NameQueryParamIsPassed() {
        var input = mock(APIGatewayProxyRequestEvent.class);
        when(input.getQueryStringParameters()).thenReturn(Map.of("name", TEST_FILE_NAME));
        underTest.setClock(Clock.fixed(Instant.parse("1970-01-01T00:00:00.101Z"), ZoneOffset.UTC));
        var presignedObject = mock(PresignedPutObjectRequest.class);
        when(presignedObject.url()).thenReturn(new URL("http://generate-presigned-url"));
        when(presignedObject.httpRequest()).thenReturn(mock(SdkHttpRequest.class));
        when(preSignedRequestService.preSignedPutRequest(eq(IMPORT_BUCKET), anyString(), eq("text/csv"))).thenReturn(presignedObject);

        var actual = underTest.handleRequest(input, context);

        assertNotNull(actual);
        assertEquals(200, actual.getStatusCode());
        assertEquals("{\"url\": \"http://generate-presigned-url\"}\n", actual.getBody());
        verify(preSignedRequestService).preSignedPutRequest(IMPORT_BUCKET, "%s/%s".formatted(UPLOADED, "test-file-101.csv"), "text/csv");
    }
}