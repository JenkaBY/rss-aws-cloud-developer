package by.jenka.rss.importservice.lambda.config;


public class S3Config {

    public static final String IMPORT_BUCKET_NAME = System.getenv().getOrDefault("IMPORT_BUCKET_NAME", "replace-i-am-wrong");


}
