package example.queue.module;

public class LuaScriptUtils {

    /**
     * redis list, lua script 로  조회시 나오는 꼬투리 바이트 제거
     * -> 이거 json type cast 에 방해되서 제거 하는데, 나중에 더 새련된 방법 있으면 이거 안쓰고 바꿀 거임 .
     * */
    public static String makeReadable(String unReadable){
        return unReadable.replaceAll("[\\u0000-\\u001F\\uFFFD]", "");
    }
}
