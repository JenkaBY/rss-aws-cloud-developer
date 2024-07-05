package by.jenka.rss.importservice.lambda.handler;

import by.jenka.rss.importservice.lambda.config.bean.S3Manager;
import by.jenka.rss.importservice.lambda.service.PrinterProductParser;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SystemStubsExtension.class)
class ImportFileParserHandlerTest {

    private static final String TEST_FILE_NAME = "test-file.csv";
    private static final String IMPORT_BUCKET = "test-import-bucket";
    private static final String UPLOADED = "test-uploaded";
    private static final String PARSED = "test-parsed";
    private S3Manager s3Manager;
    private PrinterProductParser productParser;
    private Context context;
    private ImportFileParserHandler underTest;

    @SystemStub
    private EnvironmentVariables variables =
            new EnvironmentVariables(
                    Map.of("FOLDER_FOR_UPLOAD", UPLOADED,
                            "FOLDER_FOR_PARSED", PARSED,
                            "IMPORT_BUCKET_NAME", IMPORT_BUCKET));


    @BeforeEach
    void setUp() {
        s3Manager = mock(S3Manager.class);
        productParser = mock(PrinterProductParser.class);
        context = mock(Context.class);
        when(context.getLogger()).thenReturn(mock(LambdaLogger.class));

        underTest = new ImportFileParserHandler();
        underTest.setProductParser(productParser);
        underTest.setS3Manager(s3Manager);
    }

    @Test
    void handleRequest() {
        var input = mock(S3Event.class);
        var record = mock(S3EventNotification.S3EventNotificationRecord.class);
        var s3Object = mock(S3EventNotification.S3ObjectEntity.class);
        when(s3Object.getKey()).thenReturn("%s/%s".formatted(UPLOADED, TEST_FILE_NAME));
        var s3 = mock(S3EventNotification.S3Entity.class);
        when(s3.getObject()).thenReturn(s3Object);
        var s3Client = mock(S3Client.class);
        when(s3Client.getObject(any(software.amazon.awssdk.services.s3.model.GetObjectRequest.class)))
                .thenReturn(mock(ResponseInputStream.class));
        when(s3Manager.getS3()).thenReturn(s3Client);
        when(record.getS3()).thenReturn(s3);
        when(input.getRecords()).thenReturn(List.of(record));

        var actual = underTest.handleRequest(input, context);

        assertEquals("Parsed successfully", actual);
        var expectedSourceKey = "%s/%s".formatted(UPLOADED, TEST_FILE_NAME);
        var expectedDestinationKey = "%s/%s".formatted(PARSED, TEST_FILE_NAME);
        verify(s3Manager).moveObject(IMPORT_BUCKET, expectedSourceKey, expectedDestinationKey);
        verify(productParser).parse(any(InputStreamReader.class), any(LambdaLogger.class));
    }
}