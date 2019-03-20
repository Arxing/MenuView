package org.arxing.menuview;

import java.util.ArrayList;
import java.util.List;

public class AnimHandlerGroup {
    private List<AnimHandler> handlers = new ArrayList<>();

    private AnimHandlerGroup() {

    }

    public static AnimHandlerGroup newInstance() {
        return new AnimHandlerGroup();
    }

    public static AnimHandlerGroup of(AnimHandler... handlers) {
        AnimHandlerGroup group = new AnimHandlerGroup();
        for (AnimHandler handler : handlers) {
            group.add(handler);
        }
        return group;
    }

    public AnimHandlerGroup add(AnimHandler handler) {
        handlers.add(handler);
        return this;
    }

    public List<AnimHandler> getHandlers() {
        return handlers;
    }
}
