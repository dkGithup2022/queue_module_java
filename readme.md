
# Java queue module

이번 프로젝트에서 사용하는 queue module 가 디자인이 좋은 것같아서 일부를 가져와서 올림. 

## 개발 컨셉 

**기능**
- 특정 이벤트에 대한 수 카운트
- 이벤트 정보를 FIFO 하게 처리할 수 있는 api 
- 위 기능을 제공할 수 있는 abstract  자료형 제공, 실제 사용에서는 상속받아서 생성자나  각각 이벤트 필드에 대한 메소드는 직접 구현 하세용


**성능**
- 모든 api 요청은 1번의 redis 통신으로 끝남.
- 만약 2개 이상의 redis 연산이 필요한 요청이 필요한 경우, lua script 로 묶어서 한번에 전송함

## 선행 요소 

1. redis 
   - 간단하게 테스트만 하려면 이 레포의 /redis/run.sh 으로 로컬 레디스 실행 후 테스트 가능
2. gradle 9
3. java 21



## 제공 기능 

1. FIFO 이벤트 정보 전송
2. bucket / field 단위 구분의 이벤트 카운팅 
3. 위 두 기능을 지원하는 추상 데이터 클래스 


## 노출 인터페이스 

### 카운터 

```java
public interface CounterWriter {
    /**
     * [간략]
     * 동시성 캐어를 하면서 카운트를 count만큼 증가시키는 api 입니다.
     *
     * CounterEvent impl class 에서 bucket 과 field 가 명시되어야 합니다.
     * bucket 이 작업 범위, field 가 각각 항목입니다.

     *
     * @param devwikiEvent : 이벤트 객체, bucket, field 를 redis hash 의 hash name 과 field 로 씁니다.
     * @param count : 증가 시킬 갯수
     */
    void countUpEvent(CounterEvent devwikiEvent, int count);
}

```


```java

public interface CounterReader {
    /**
     * 해당 버킷에 있는 모든 field, count 쌍을 가져오고 버킷을 제거합니다.
     *
     * [Throws]
     * 제이슨 파싱에 실패할 경우, 항목 내용을 로그로 남기고 해당 내용을 제외한 나머지 원소를 반환합니다.
     *
     * @param bucketName
     * @return List<{ 식별자:String }, { 카운트:Long }>
     */

    List<CounterPair> popBucket(String bucketName);


    /***
     *
     * 해당 버킷과 field 에 해당하는 count 를 읽어옵니다.
     *
     * [Throws]
     * 제이슨 파싱에 실패할 경우, 항목 내용을 로그로 남기고 해당 내용을 제외한 나머지 원소를 반환합니다.
     *
     * @param bucketName
     * @param field
     * @return : { 식별자:String }, { 카운트:Long }
     */
    CounterPair popField(String bucketName, String field);
}



```

### 큐 


```java

public interface QueueWriter {
    /***
     * 이벤트를 발행하여 큐 자료형에 append 합니다.
     *
     * 큐의 구분, 이름은 QueueEvent의 event.queueName() 의 큐에 append 됩니다.
     * @param event
     */
    void append( QueueEvent event);
}

```


```java
public interface QueueReader {

    /**
     * queue 의 항목을 from 번부터 count 갯수만큼 읽어옵니다.
     * queue 의 항목을 지우지는 않습니다.
     *
     * @param queueName
     * @param to
     * @param from
     * @return
     */

    List<QueueEvent> read(String queueName, Class<? extends QueueEvent> clazz, int from, int to);


    /**
     * queue 의 항목을 처음부터 count 갯수만큼 읽어옵니다.
     * 읽어온 항목을 지웁니다.
     * <p>
     * (잘 안된다면 discord )
     *
     * @param queueName
     * @param count
     * @return
     */
    List<QueueEvent> pop(String queueName, Class<? extends QueueEvent> clazz, int count);
}

```

## 데이터 스펙

##### 카운터 데이터

```java
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
    
   ....

```

##### 큐데이터


```java
@JsonSerialize(using = QueueEventSerializer.class)
public abstract class QueueEvent implements Serializable {

    String queueName;

    HashMap<String, String> specs;

    protected QueueEvent(String queueName, HashMap<String, String> specs) {
        this.queueName = queueName;
        this.specs = specs;
    }

    @JsonProperty("queueName")
    public String queueName() {
        return queueName;
    }


    protected HashMap<String, String> getSpecs() {
        return specs;
    }

    
   ...

```


## 사용 예시 



```java
    @Test
    void publishEvent_success() throws JsonProcessingException {
        var evt = LikeArticleAlarmEvent.of(1L, 1L);

        queueAppender.append(evt);
```

```java

  @Test
    @DisplayName("큐 조회 - 1건")
    void readQueue() {
        readyOneEvent();

        var evts = queueReader.read(LikeArticleAlarmEvent.queue(), LikeArticleAlarmEvent.class, 0, -1);
```


```java
    @Test
    @DisplayName("성공-  기존 이벤트 카운터 증가-> 두번하기  2")
    void write_count_success_02() {
        var evt = ArticleViewUpEvent.newEvent( 1L);
        countWriter.countUpEvent(evt, 1);
        countWriter.countUpEvent(evt, 1);

```


```java
    @Test
    void read_single_field_bucket() {

        String field = "1";

        var evt1 = ArticleViewUpEvent.newEvent(1L);
        var evt2 = ArticleViewUpEvent.newEvent(1L);
        var evt3 = ArticleViewUpEvent.newEvent(1L);

        writer.countUpEvent(evt1, 1);
        writer.countUpEvent(evt2, 1);
        writer.countUpEvent(evt3, 1);

        var pairs = countReader.popBucket(ArticleViewUpEvent.eventbucket());
        assertEquals(1, pairs.size());
        assertEquals(field, pairs.getFirst().field());
        assertEquals(3, pairs.getFirst().value());

    }
```