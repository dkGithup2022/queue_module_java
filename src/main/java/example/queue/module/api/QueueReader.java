package example.queue.module.api;

import example.queue.module.data.QueueEvent;

import java.util.List;

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
