package example.queue.module.api;

import example.queue.module.data.QueueEvent;

public interface QueueWriter {
    /***
     * 이벤트를 발행하여 큐 자료형에 append 합니다.
     *
     * 큐의 구분, 이름은 QueueEvent의 event.queueName() 의 큐에 append 됩니다.
     * @param event
     */
    void append( QueueEvent event);
}
