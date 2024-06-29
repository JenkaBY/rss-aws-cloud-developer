package by.jenka.rss.importservice.lambda.handler;

import by.jenka.rss.importservice.lambda.config.S3Config;
import by.jenka.rss.importservice.lambda.config.bean.S3Manager;
import by.jenka.rss.importservice.lambda.service.ProductParser;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;

@Setter
public class ImportFileParserHandler implements RequestHandler<S3Event, String> {

    private static final String FOLDER_FOR_UPLOAD = System.getenv().getOrDefault("FOLDER_FOR_UPLOAD", "replace-me");
    private static final String FOLDER_FOR_PARSED = System.getenv().getOrDefault("FOLDER_FOR_PARSED", "replace-me");
    private S3Manager s3Manager = new S3Manager();
    private ProductParser productParser = new ProductParser();

    @Override
    public String handleRequest(S3Event input, Context context) {
        final var logger = context.getLogger();
        logger.log("Total records in the incoming S3Event: %s".formatted(input.getRecords().size()));
        for (var record : input.getRecords()) {
            var s3Object = record.getS3().getObject();
            logger.log("Processing %s object".formatted(s3Object.getKey()));

            parseCsv(s3Object, logger);
            moveToParsedFolder(s3Object, logger);

            logger.log("File %s parsed successfully".formatted(s3Object.getKey()));
        }
        logger.log("Handling S3 event processed successfully");
        return "Parsed successfully";
    }

    private void moveToParsedFolder(S3EventNotification.S3ObjectEntity s3Object,
                                    LambdaLogger logger) {
        var destinationKey = s3Object.getKey().replace(FOLDER_FOR_UPLOAD, FOLDER_FOR_PARSED);
        var bucketName = S3Config.IMPORT_BUCKET_NAME;
        s3Manager.moveObject(bucketName, s3Object.getKey(), destinationKey);
        logger.log("Moved file [bucket=%s key=%s] to [bucket=%s key=%s]"
                .formatted(bucketName, s3Object.getKey(), bucketName, destinationKey));
    }

    private void parseCsv(S3EventNotification.S3ObjectEntity s3Object, LambdaLogger logger) {
        logger.log("Parse CSV file %s".formatted(s3Object.getKey()));
        var inputStream = s3Manager.getS3().getObject(GetObjectRequest.builder()
                .bucket(S3Config.IMPORT_BUCKET_NAME)
                .key(s3Object.getKey())
                .build());
        var inputStreamReader = new InputStreamReader(inputStream, UTF_8);
        productParser.parse(inputStreamReader, logger);
    }
}
