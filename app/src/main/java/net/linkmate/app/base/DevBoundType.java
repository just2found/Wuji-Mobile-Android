package net.linkmate.app.base;

public interface DevBoundType {
    int IN_THIS_NET = 1;
    int MY_DEVICES = 2;
    int SHARED_DEVICES = 3;
    int ALL_BOUND_DEVICES = 4;
    int LOCAL_DEVICES = 5;
}

//public enum DevBoundType {
//    IN_THIS_NET(1),
//    MY_DEVICES(2),
//    SHARED_DEVICES(3),
//    ALL_BOUND_DEVICES(4),
//    LOCAL_DEVICES(5);
//
//    private final int type;
//
//    DevBoundType(int type) {
//        this.type = type;
//    }
//
//    public int getType() {
//        return type;
//    }
//}
