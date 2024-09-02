package example.queue.module.api;

import example.queue.module.counter.dto.CounterPair;

import java.util.List;

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
