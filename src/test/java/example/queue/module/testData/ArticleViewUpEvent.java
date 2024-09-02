package example.queue.module.testData;

import example.queue.module.data.CounterEvent;

public class ArticleViewUpEvent extends CounterEvent {

    private static final String BUCKET = "ARTICLE_VIEW_UP";

    public static String eventbucket(){
        return BUCKET;
    }

    protected ArticleViewUpEvent(String bucket, String field) {
        super(bucket, field);
    }

    public static ArticleViewUpEvent newEvent(long id) {
        return new ArticleViewUpEvent(BUCKET, String.valueOf(id));
    }
}
