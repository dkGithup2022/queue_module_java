package example.queue.module.data;

public abstract class CounterEvent {

    protected String bucket;
    protected String field;

    public String bucket() {
        return bucket;
    }

    public String field() {
        return field;
    }

    protected CounterEvent(String bucket, String field){
        this.bucket = bucket;
        this.field = field;
    }

    @Override
    public String toString() {
        return "CounterEvent{" +
                "bucket='" + bucket + '\'' +
                ", field='" + field + '\'' +
                '}';
    }
}
