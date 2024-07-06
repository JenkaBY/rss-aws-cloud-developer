package by.jenka.rss.backend.importservice;

import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;

import java.util.Map;

public class TaskRunner {
    private static final String RSS_ALL_TASKS_STACK_NAME = "RssAwsCloudDeveloperImportFilesServiceStack";

    public static void main(String[] args) {
        System.out.println("-------------   Start import-files app stack");
        App app = new App(AppProps.builder()
                .context(Map.ofEntries(
                        Map.entry("school", "RSS")
                ))
                .build());
        new ImportServiceStack(app, RSS_ALL_TASKS_STACK_NAME)
                .createS3BucketForImportFiles()
                .createImportFileParserHandlerLambda()
                .createImportProductsFileHandlerLambda()
                .createImportFilesApiGateway()
                .grantRWPermissionsToS3()
                .outputStackVariables();

        app.synth();
        System.out.println("---------------  End app");
    }
}
