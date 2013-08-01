package temmental2;

import java.util.List;

public class Command {

    private String tag;
    private Element element;
    private List<Object> betweenTags;

    public Command(String tag, Element element, List<Object> betweenTags) {
        this.tag = tag;
        this.element = element;
        this.betweenTags = betweenTags;
    }

    @Override
    public String toString() {
        //TODO
        return "@Command===>TODO" + tag;
    }

}
