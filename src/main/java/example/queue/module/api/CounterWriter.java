package example.queue.module.api;

import example.queue.module.data.CounterEvent;

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
